package dk.statsbiblioteket.medieplatform.autonomous;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.recipes.locks.InterProcessLock;
import com.netflix.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * This is the Autonomous Component main class. It should contain all the harnessing stuff that allows a system to work
 * in the autonomous mindset
 */
public class AutonomousComponent implements Callable<CallResult> {

    private static Logger log = org.slf4j.LoggerFactory.getLogger(AutonomousComponent.class);
    private final CuratorFramework lockClient;
    private final long timeoutSBOI;
    private final long timeoutBatch;
    private final RunnableComponent runnable;
    private final long pollTime = 1000;
    private final ConcurrencyConnectionStateListener concurrencyConnectionStateListener;
    private final long workerTimout;
    private int simultaneousProcesses;
    private List<String> pastSuccessfulEvents;
    private List<String> pastFailedEvents;
    private List<String> futureEvents;
    private boolean paused = false;
    private boolean stopped = false;
    private Integer maxResults;


    public AutonomousComponent(RunnableComponent runnable, CuratorFramework lockClient, int simultaneousProcesses, List<String> pastSuccessfulEvents,
                               List<String> pastFailedEvents, List<String> futureEvents, long timeoutSBOI, long timeoutBatch, long workerTimout) {
        this(runnable, lockClient, simultaneousProcesses, pastSuccessfulEvents, pastFailedEvents,
                futureEvents, timeoutSBOI, timeoutBatch, workerTimout, null);
    }

    public AutonomousComponent(RunnableComponent runnable, CuratorFramework lockClient, int simultaneousProcesses, List<String> pastSuccessfulEvents,
                               List<String> pastFailedEvents, List<String> futureEvents, long timeoutSBOI, long timeoutBatch, long workerTimout,
                               Integer maxResults) {

        this.lockClient = lockClient;
        this.timeoutSBOI = timeoutSBOI;
        this.timeoutBatch = timeoutBatch;
        this.runnable = runnable;
        this.workerTimout = workerTimout;
        this.simultaneousProcesses = simultaneousProcesses;
        this.pastSuccessfulEvents = pastSuccessfulEvents;
        this.pastFailedEvents = pastFailedEvents;
        this.futureEvents = futureEvents;
        concurrencyConnectionStateListener = new ConcurrencyConnectionStateListener(this);
        this.lockClient.getConnectionStateListenable().addListener(concurrencyConnectionStateListener);
        this.maxResults = maxResults;
    }

    /**
     * Utility method to release locks, ignoring any errors being thrown. Will continue to release the lock until
     * errors
     * are being thrown.
     *
     * @param lock the lock to release
     */
    protected static void releaseQuietly(InterProcessLock lock) {
        boolean released = false;
        while (!released) {
            try {
                lock.release();
            } catch (IllegalStateException e) {
                released = true;
            } catch (Exception e) {
                log.warn("Caught exception while trying to release lock", e);
                return;
            }
        }
    }

    protected static boolean acquireQuietly(InterProcessLock lock, long timeout) throws LockingException {
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
    private static String getBatchLockPath(RunnableComponent runnable, Batch batch) {
        return "/" + runnable.getComponentName() + "/" + batch.getFullID();
    }

    /**
     * Parse the propertyValue as a long, and if failing, return the default value
     *
     * @param propertyValue the string to parse
     * @param defaultValue  the default value
     *
     * @return the long value
     */
    private long parseLong(String propertyValue, long defaultValue) {
        try {
            return Long.parseLong(propertyValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * The primary method of the autonomous components. This method does the following
     *
     * <ul>
     * <li> Locks the SBOI</li>
     * <li> gets the batches in the right state</li>
     * <li> attempts to lock it</li>
     * <li> when #simultanousProcesses is locked or no more batches in the list</li>
     * <li> unlock SBOI</li>
     * <li> do the work on the batches and store the results for each</li>
     * <li> when all work is completed, unlock all the batches</li>
     * </ul>
     *
     * @return true if a batch was succesfully worked on. False if no batch was ready
     * @throws CouldNotGetLockException if no lock could be achieved within the set timeouts. This is not an anormal
     *                                  situation, as it just means that all the relevant batches are already being
     *                                  processed.
     * @throws LockingException         if the locking framework fails
     * @throws CommunicationException   if communication with SBOI fails
     */
    @Override
    public CallResult call() throws LockingException, CouldNotGetLockException, CommunicationException {

        InterProcessLock SBOILock = null;
        CallResult result = new CallResult();
        Map<BatchWorker, InterProcessLock> workers = new HashMap<>();
        try {
            //lock SBOI for this component name
            SBOILock = new InterProcessSemaphoreMutex(lockClient, getSBOILockpath(runnable));
            try {
                boolean sboi_locked = acquireQuietly(SBOILock, timeoutSBOI);
                if (!sboi_locked) {
                    throw new CouldNotGetLockException("Could not get lock of SBOI, so returning");
                }

                log.info("SBOI locked, quering for batches");
                //get batches, lock n, release the SBOI
                //get batches
                Iterator<Batch> batches = runnable.getEventTrigger().getTriggeredBatches(pastSuccessfulEvents,
                                                                                         pastFailedEvents, futureEvents);
                //for each batch
                while (batches.hasNext()) {
                    Batch batch = batches.next();

                    log.info("Found batch {}", batch.getFullID());
                    //attempt to lock
                    InterProcessLock batchlock = new InterProcessSemaphoreMutex(
                            lockClient, getBatchLockPath(runnable, batch));
                    boolean success = acquireQuietly(batchlock, timeoutBatch);
                    if (success) {//if lock gotten
                        log.info("Batch {} locked, creating a worker", batch.getFullID());
                        if (maxResults != null) {
                            log.debug("Worker will report a maximum of {} results.", maxResults);
                        }
                        BatchWorker worker = new BatchWorker(
                                runnable,
                                new ResultCollector(runnable.getComponentName(), runnable.getComponentVersion(), maxResults),
                                batch, runnable.getEventStorer());
                        workers.put(worker, batchlock);
                        if (workers.size() >= simultaneousProcesses) {
                            log.info("We now have sufficient workers, look for no more batches");
                            break;
                        }
                    }
                }
            } catch (RuntimeException runtimeException) {
                for (InterProcessLock interProcessLock : workers.values()) {
                    releaseQuietly(interProcessLock);
                }
                throw runtimeException;
            }

            checkLockServerConnectionState();
            ExecutorService pool = Executors.newFixedThreadPool(simultaneousProcesses);
            ArrayList<Future<?>> futures = new ArrayList<>();
            for (BatchWorker batchWorker : workers.keySet()) {
                log.info("Submitting worker for batch {}", batchWorker.getBatch().getBatchID());
                concurrencyConnectionStateListener.add(batchWorker);
                Future<?> future = pool.submit(batchWorker);
                futures.add(future);
            }
            log.info("Shutting down the pool, and waiting for the workers to terminate");
            pool.shutdown();

            //The wait loop for the running threads
            long start = System.currentTimeMillis();
            boolean allDone = false;
            while (!allDone) {
                log.trace("Waiting to terminate");
                allDone = true;
                for (Future<?> future : futures) {
                    allDone = allDone && future.isDone();
                }
                checkLockServerConnectionState(pool);
                try {
                    Thread.sleep(pollTime);
                } catch (InterruptedException e) {
                    //okay, continue
                }
                if (System.currentTimeMillis() - start > workerTimout) {
                    log.error("Worker timeout exceeded (" + workerTimout + "ms), shutting down all threads. We still need to wait for them" + " to terminate, however.");
                    pool.shutdownNow();
                    for (Future<?> future : futures) {
                        future.cancel(true);
                    }
                }
            }
            log.info("All is now done, all workers have completed");
            for (BatchWorker batchWorker : workers.keySet()) {
                result.addResult(batchWorker.getBatch(), batchWorker.getResultCollector());
            }
        } finally {
            for (InterProcessLock interProcessLock : workers.values()) {
                releaseQuietly(interProcessLock);
            }
            releaseQuietly(SBOILock);
        }
        return result;
    }

    private Set<Batch> asSet(Iterator<Batch> batches) {
        HashSet<Batch> result = new HashSet<>();
        while (batches.hasNext()) {
            Batch next = batches.next();
            result.add(next);
        }
        return result;
    }

    /**
     * Check the lock server connection state. If the connection is lost, all our locks are dirty, so the execution
     * should stop. An CommunicationException is thrown in this case. If the connection is suspended, enter into an
     * potentially infinite loop waiting for the connection to either be restored or lost.
     *
     * @throws CommunicationException if the connection was lost
     */
    private void checkLockServerConnectionState() throws CommunicationException {
        checkLockServerConnectionState(null);
    }

    /**
     * Check the lock server connection state. If the connection is lost, all our locks are dirty, so the execution
     * should stop. An CommunicationException is thrown in this case. If the connection is suspended, enter into an
     * potentially infinite loop waiting for the connection to either be restored or lost.
     *
     * @param pool this is the pool of executing threads. The threads will be stopped as best as the system is able, if
     *             the connection is lost.
     *
     * @throws CommunicationException if the connection was lost
     */
    private void checkLockServerConnectionState(ExecutorService pool) throws CommunicationException {
        checkStopped(pool);
        while (paused && !stopped) {
            try {
                Thread.sleep(pollTime);
            } catch (InterruptedException e) {

            }
        }
        checkStopped(pool);
    }

    /**
     * Check if the stopped flag is set. If set, and pool is non-null, shut down the pool
     *
     * @param pool the pool of worker threads
     *
     * @throws CommunicationException if the stopped flag is set
     */
    private void checkStopped(ExecutorService pool) throws CommunicationException {
        if (stopped) {
            if (pool != null) {
                pool.shutdownNow();
            }
            throw new CommunicationException("Lost connection to lock server");
        }
    }

    /**
     * Mark the connection to the lock server as suspended or not
     *
     * @param paused true if the connection is suspended
     */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    /**
     * Mark the connection to the lock server as lost
     *
     * @param stopped if true, the connection is lost
     *
     * @see #checkLockServerConnectionState()
     * @see #checkStopped(java.util.concurrent.ExecutorService)
     */
    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
}

