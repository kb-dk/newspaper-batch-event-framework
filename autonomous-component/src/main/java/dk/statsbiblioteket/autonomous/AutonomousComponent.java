package dk.statsbiblioteket.autonomous;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.recipes.locks.InterProcessLock;
import com.netflix.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import dk.statsbibliokeket.newspaper.batcheventFramework.BatchEventClient;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.CommunicationException;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * This is the Autonomous Component main class. It should contain all the harnessing stuff that allows a system to work
 * in the autonomous mindset
 */
public class AutonomousComponent
        implements Callable<Map<String, Boolean>> {

    private static Logger log = org.slf4j
            .LoggerFactory
            .getLogger(AutonomousComponent.class);
    private final CuratorFramework lockClient;
    private final BatchEventClient batchEventClient;
    private final long timeoutSBOI;
    private final long timeoutBatch;
    private final RunnableComponent runnable;
    private final long pausePollTime = 1000;
    private int simultaneousProcesses;
    private boolean paused = false;
    private boolean stopped = false;
    private List<String> pastEvents;
    private List<String> pastEventsExclude;
    private List<String> futureEvents;


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
                               BatchEventClient batchEventClient,
                               int simultaneousProcesses) {

        this.runnable = runnable;
        this.lockClient = lockClient;
        this.batchEventClient = batchEventClient;
        this.simultaneousProcesses = simultaneousProcesses;
        timeoutSBOI = Long.parseLong(configuration.getProperty("timeout_SBOI", "5000"));
        timeoutBatch = Long.parseLong(configuration.getProperty("timeout_Batch", "2000"));
        this.lockClient
                .getConnectionStateListenable()
                .addListener(new ConcurrencyConnectionStateListener(this));
    }

    /**
     * Utility method to release locks, ignoring any errors being thrown. Will continue to release the lock until errors
     * are being thrown.
     *
     * @param lock the lock to release
     */
    protected static void releaseQuietly(InterProcessLock lock) {
        boolean released = false;
        while (!released) {
            try {
                lock.release();
            } catch (Exception e) {
                released = true;
            }
        }
    }

    protected static boolean acquireQuietly(InterProcessLock lock,
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
     * Get the zookeeper lockpath for the SBOI instance for this component
     *
     * @return the lock path
     */
    private static String getSBOILockpath(RunnableComponent runnable) {
        return "/SBOI/" + runnable.getComponentName();
    }

    /**
     * Get the lock path for this batch for this component
     *
     * @param batch the batch to lock
     *
     * @return the zookeepr lock path
     */
    private static String getBatchLockPath(RunnableComponent runnable,
                                           Batch batch) {
        return "/" + runnable.getComponentName() + getBatchFormattetID(batch);
    }

    protected static String getBatchFormattetID(Batch batch) {
        return "/B" + batch.getBatchID() + "-RT" + batch.getRoundTripNumber();
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
    @Override
    public Map<String, Boolean> call()
            throws
            Exception {

        InterProcessLock SBOI_lock = null;
        Map<String, Boolean> result = new HashMap<>();
        Map<BatchWorker, InterProcessLock> workers = new HashMap<>();
        try {
            //lock SBOI for this component name
            SBOI_lock = new InterProcessSemaphoreMutex(lockClient, getSBOILockpath(runnable));
            try {
                boolean sboi_locked = acquireQuietly(SBOI_lock, timeoutSBOI);
                if (!sboi_locked) {
                    throw new CouldNotGetLockException("Could not get lock of SBOI, so returning");
                }

                //get batches, lock n, release the SBOI
                //get batches
                Iterator<Batch> batches = batchEventClient.getBatches(pastEvents, pastEventsExclude, futureEvents);
                //for each batch
                while (batches.hasNext()) {
                    Batch batch = batches.next();

                    //attempt to lock
                    InterProcessLock batchlock =
                            new InterProcessSemaphoreMutex(lockClient, getBatchLockPath(runnable, batch));
                    boolean success = acquireQuietly(batchlock, timeoutBatch);
                    if (success) {//if lock gotten
                        BatchWorker worker = new BatchWorker(runnable, new ResultCollector(), batch, batchEventClient);
                        workers.put(worker, batchlock);
                        if (workers.size() >= simultaneousProcesses) {
                            break;
                        }
                    }
                }
            } catch (RuntimeException runtimeException) {
                for (InterProcessLock interProcessLock : workers.values()) {
                    releaseQuietly(interProcessLock);
                }
                throw runtimeException;
            } finally {
                releaseQuietly(SBOI_lock);
            }


            ExecutorService pool = Executors.newFixedThreadPool(simultaneousProcesses);




            ArrayList<Future<?>> futures = new ArrayList<>();
            for (BatchWorker batchWorker : workers.keySet()) {
                Future<?> future = pool.submit(batchWorker);
                futures.add(future);
            }

            pool.shutdown();
            while (!pool.isTerminated()) {
                pool.awaitTermination(100, TimeUnit.MILLISECONDS);
            }

            boolean allDone = false;
            while (!allDone){
                allDone = true;
                for (Future<?> future : futures) {
                    allDone = allDone && future.isDone();
                }
                Thread.sleep(100);
            }
            for (BatchWorker batchWorker : workers.keySet()) {
                result.put(getBatchFormattetID(batchWorker.getBatch()),batchWorker.getResultCollector().isSuccess());
            }
        } finally {
            for (InterProcessLock batchLock : workers.values()) {
                releaseQuietly(batchLock);
            }
            releaseQuietly(SBOI_lock);
        }
        return result;
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


    public void setPastEvents(List<String> pastEvents) {
        this.pastEvents = pastEvents;
    }

    public void setPastEventsExclude(List<String> pastEventsExclude) {
        this.pastEventsExclude = pastEventsExclude;
    }

    public void setFutureEvents(List<String> futureEvents) {
        this.futureEvents = futureEvents;
    }
}

