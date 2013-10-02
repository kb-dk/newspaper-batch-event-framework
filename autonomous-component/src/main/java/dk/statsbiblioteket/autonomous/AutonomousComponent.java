package dk.statsbiblioteket.autonomous;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.recipes.locks.InterProcessLock;
import com.netflix.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import dk.statsbibliokeket.newspaper.batcheventFramework.BatchEventClient;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.CommunicationException;
import org.slf4j.Logger;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class AutonomousComponent {

    private static Logger log = org.slf4j.LoggerFactory.getLogger(AutonomousComponent.class);

    private final CuratorFramework lockClient;
    private final BatchEventClient batchEventClient;

    private final long SBOI_timeout;
    private final long batch_timeout;

    private final RunnableComponent runnable;

    private boolean paused = false;

    private boolean stopped = false;
    private final long pausePollTime = 1000;


    public AutonomousComponent(RunnableComponent runnable,
                               Properties configuration,
                               CuratorFramework lockClient,
                               BatchEventClient batchEventClient) {

        this.runnable = runnable;
        this.lockClient = lockClient;
        this.batchEventClient = batchEventClient;
        SBOI_timeout = Long.parseLong(configuration.getProperty("SBOI.timeout", "5000"));
        batch_timeout = Long.parseLong(configuration.getProperty("batch_timeout", "2000"));
        this.lockClient.getConnectionStateListenable().addListener(new ConcurrencyConnectionStateListener(this));


        /*if (lockClient != null){
            lockClient.close();
        }
        lockClient = CuratorFrameworkFactory.newClient(properties.getProperty("zookeeper.connectString"), new ExponentialBackoffRetry(1000, 3));
        */
    }


    /**
     * The primary method of the autonomous components. This method does the following
     * Locks the SBOI
     * gets the batches in the right state
     * attempts to lock one
     * when the first one is locked or no more batches in the list, unlock SBOI
     * do the work on the batch
     * store the results
     * unlock the batch
     *
     * @return true if a batch was succesfully worked on. False if no batch was ready or the component could not achieve the
     *         nessesary locks, or the execution failed on the batch
     */
    public synchronized boolean pollAndWork(List<String> pastEvents,
                                            List<String> pastEventsExclude,
                                            List<String> futureEvents) throws Exception {
        InterProcessLock SBOI_lock = null;
        InterProcessLock batchlock = null;
        try {
            //lock SBOI for this component name

            SBOI_lock = new InterProcessSemaphoreMutex(lockClient, getSBOILockpath());

            boolean sboi_locked = SBOI_lock.acquire(SBOI_timeout, TimeUnit.MILLISECONDS);
            if (!sboi_locked) {
                //log this
                return false;
            }

            Batch lockedBatch = null;


            //get batches, lock one, release the SBOI
            try {
                //get batches
                Iterator<Batch> batches = batchEventClient.getBatches(pastEvents, pastEventsExclude, futureEvents);
                //for each batch
                while (batches.hasNext()) {
                    Batch batch = batches.next();

                    //attempt to lock
                    batchlock = new InterProcessSemaphoreMutex(lockClient, getBatchLockPath(batch));
                    boolean success = batchlock.acquire(batch_timeout, TimeUnit.MILLISECONDS);
                    if (success) {//if lock gotten
                        //break loop
                        lockedBatch = batch;
                        break;
                    }
                }
            } catch (Throwable throwable) {
                //ugly, but we must release the lock, when this fails
                if (batchlock != null && batchlock.isAcquiredInThisProcess()) {
                    batchlock.release();
                }
                throw throwable;
            } finally {
                //unlock SBOI
                SBOI_lock.release();
            }

            //If the lockedbatch is != null, it is locked now
            if (lockedBatch == null) {
                //no batch available
                return false;
            }

            ResultCollector result = new ResultCollector();
            try {
                //do work
                stated();
                result.setTimestamp(new Date());
                runnable.doWorkOnBatch(lockedBatch,
                        result);

                return true;

            } catch (Exception e) {
                //the work failed
                result.setSuccess(false);
                result.addMessage(e.getMessage());
                log.error("Failed", e);
                return false;
            } finally {
                try {
                    stated();
                    //preserve the result
                    preserveResult(lockedBatch, result);
                } finally {
                    //unlock Batch
                    batchlock.release();
                }

            }
        } finally {
            releaseQuietly(SBOI_lock);
            releaseQuietly(batchlock);
        }

    }

    private void releaseQuietly(InterProcessLock lock) {
        boolean released = false;
        while (!released){
            try {
                lock.release();
            } catch (Exception e) {
                released = true;
            }
        }
    }

    private void stated() throws CommunicationException {
        if (stopped) {
            throw new CommunicationException("Lost connection to lock server");
        }
        while (paused) {
            try {
                Thread.sleep(pausePollTime);
            } catch (InterruptedException e) {

            }
        }
        if (stopped) {
            throw new CommunicationException("Lost connection to lock server");
        }

    }

    /**
     * This method stores the event back into DOMS, so it should be visible to the SBOI soonish
     *
     * @param batch  the batch worked on
     * @param result the result of the work
     */
    private void preserveResult(Batch batch, ResultCollector result) throws CommunicationException {
        batchEventClient.addEventToBatch(batch.getBatchID(),
                batch.getRoundTripNumber(),
                runnable.getComponentName() + "-" + runnable.getComponentVersion(),
                result.getTimestamp(),
                result.toSummary(),
                runnable.getEventID(),
                result.isSuccess()
        );
    }


    /**
     * Get the zookeeper lockpath for the SBOI instance for this component
     *
     * @return the lock path
     */
    private final String getSBOILockpath() {
        return "/SBOI/" + runnable.getComponentName();
    }

    /**
     * Get the lock path for this batch for this component
     *
     * @param batch the batch to lock
     * @return the zookeepr lock path
     */
    private String getBatchLockPath(Batch batch) {
        return "/"+runnable.getComponentName() + "/B" + batch.getBatchID() + "-RT" + batch.getRoundTripNumber();
    }

    public void pause() {
        paused = true;
    }

    public void unpause() {
        paused = false;
    }

    public void stop() {
        stopped = true;
    }


}

