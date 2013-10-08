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

/**
 * This is the Autonomous Component main class. It should contain all the harnessing stuff that allows a system to work
 * in the autonomous mindset
 */
public class AutonomousComponent {

    private static Logger log = org.slf4j.LoggerFactory.getLogger(AutonomousComponent.class);
    private final CuratorFramework lockClient;
    private final BatchEventClient batchEventClient;
    private final long timeoutSBOI;
    private final long timeoutBatch;
    private final RunnableComponent runnable;
    private final long pausePollTime = 1000;
    private boolean paused = false;
    private boolean stopped = false;


    /**
     * Create a new Autonomous Component
     *
     * @param runnable         the is the class that will be doing the actual work
     * @param configuration    the Configuration as a set of properties
     * @param lockClient       Client to the netflix curator zookeeper lockserver
     * @param batchEventClient the client for quering and adding events
     */
    public AutonomousComponent(RunnableComponent runnable,
                               Properties configuration,
                               CuratorFramework lockClient,
                               BatchEventClient batchEventClient) {

        this.runnable = runnable;
        this.lockClient = lockClient;
        this.batchEventClient = batchEventClient;
        timeoutSBOI = Long.parseLong(configuration.getProperty("timeout_SBOI", "5000"));
        timeoutBatch = Long.parseLong(configuration.getProperty("timeout_Batch", "2000"));
        this.lockClient.getConnectionStateListenable().addListener(new ConcurrencyConnectionStateListener(this));
    }

    /**
     * The primary method of the autonomous components. This method does the following Locks the SBOI gets the batches
     * in the right state attempts to lock one when the first one is locked or no more batches in the list, unlock SBOI
     * do the work on the batch store the results unlock the batch
     *
     * @return true if a batch was succesfully worked on. False if no batch was ready
     * @throws CouldNotGetLockException if no lock could be achieved within the set timeouts. This is not an anormal
     *                                  situation, as it just means that all the relevant batches are already being
     *                                  processed.
     * @throws LockingException         if the locking framework fails
     * @throws CommunicationException   if communication with SBOI fails
     * @throws WorkException            if the runnable component threw an exception, it will be wrapped as a work
     *                                  exception
     */
    public synchronized boolean pollAndWork(List<String> pastEvents,
                                            List<String> pastEventsExclude,
                                            List<String> futureEvents)
            throws
            CouldNotGetLockException,
            LockingException,
            CommunicationException,
            WorkException {
        InterProcessLock SBOI_lock = null;
        InterProcessLock batchlock = null;
        try {
            //lock SBOI for this component name

            SBOI_lock = new InterProcessSemaphoreMutex(lockClient, getSBOILockpath());

            boolean sboi_locked = acquireQuietly(SBOI_lock, timeoutSBOI);
            if (!sboi_locked) {
                throw new CouldNotGetLockException("Could not get lock of SBOI, so returning");
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
                    boolean success = acquireQuietly(batchlock, timeoutBatch);
                    if (success) {//if lock gotten
                        //break loop
                        lockedBatch = batch;
                        break;
                    }
                }
            } catch (RuntimeException runtimeException) {
                releaseQuietly(batchlock);
                throw runtimeException;
            } finally {
                releaseQuietly(SBOI_lock);
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
                runnable.doWorkOnBatch(lockedBatch, result);

                return true;

            } catch (Exception e) {
                //the work failed
                result.setSuccess(false);
                result.addFailure(getBatchFormattetID(lockedBatch),
                                  "Component Failure",
                                  getComponentFormattetName(),
                                  "Component threw exception",
                                  e.getMessage());
                log.error("Failed", e);
                throw new WorkException(result.toReport(), e);

            } finally {
                try {
                    stated();
                    //preserve the result
                    preserveResult(lockedBatch, result);
                } finally {
                    //unlock Batch
                    releaseQuietly(batchlock);
                }
            }
        } finally {
            releaseQuietly(SBOI_lock);
            releaseQuietly(batchlock);
        }
    }

    private String getComponentFormattetName() {
        return runnable.getComponentName() + "-" + runnable.getComponentVersion();
    }

    /**
     * Utility method to release locks, ignoring any errors being thrown. Will continue to release the lock until errors
     * are being thrown.
     *
     * @param lock the lock to release
     */
    private void releaseQuietly(InterProcessLock lock) {
        boolean released = false;
        while (!released) {
            try {
                lock.release();
            } catch (Exception e) {
                released = true;
            }
        }
    }

    private boolean acquireQuietly(InterProcessLock lock,
                                   long timeout)
            throws
            LockingException {
        try {
            return lock.acquire(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new LockingException("Failed to acquire lock", e);
        }
    }

    /**
     * Checks the paused and stopped flags to pause or halt execution
     *
     * @throws CommunicationException If the component have been stopped
     */
    private void stated()
            throws
            CommunicationException {
        if (stopped) {
            throw new CommunicationException("Lost connection to lock server");
        }
        while (paused && !stopped) {
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
    private void preserveResult(Batch batch,
                                ResultCollector result)
            throws
            CommunicationException {
        batchEventClient.addEventToBatch(batch.getBatchID(),
                                         batch.getRoundTripNumber(),
                                         getComponentFormattetName(),
                                         result.getTimestamp(),
                                         result.toReport(),
                                         runnable.getEventID(),
                                         result.isSuccess());
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
     *
     * @return the zookeepr lock path
     */
    private String getBatchLockPath(Batch batch) {
        return "/" + runnable.getComponentName() + getBatchFormattetID(batch);
    }

    private String getBatchFormattetID(Batch batch) {
        return "/B" + batch.getBatchID() + "-RT" + batch.getRoundTripNumber();
    }

    /**
     * Pause the component. Will not immediately pause execution. Rather, it will hold at some predetermined points in
     * the exection flow
     */
    public void pause() {
        paused = true;
    }

    /**
     * Unpause the component, if it was paused already
     */
    public void unpause() {
        paused = false;
    }

    /**
     * Stop the component
     */
    public void stop() {
        stopped = true;
    }


}

