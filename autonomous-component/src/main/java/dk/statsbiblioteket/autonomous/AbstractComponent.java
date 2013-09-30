package dk.statsbiblioteket.autonomous;

import com.google.common.io.Closeables;
import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.framework.recipes.locks.InterProcessLock;
import com.netflix.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import com.netflix.curator.retry.ExponentialBackoffRetry;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.DataSource;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public abstract class AbstractComponent {

    private static Logger log = org.slf4j.LoggerFactory.getLogger(AbstractComponent.class);

    private CuratorFramework lockClient;

    private long SBOI_timeout;
    private long batch_timeout;
    private List<String> futureEvents;
    private List<String> pastEvents;
    private String componentName;
    private String componentVersion;

    private boolean configured = false;

    public synchronized void configure(Properties properties){
        if (lockClient != null){
            lockClient.close();
        }
        lockClient = CuratorFrameworkFactory.newClient(properties.getProperty("zookeeper.connectString"), new ExponentialBackoffRetry(1000, 3));
        SBOI_timeout = Long.parseLong(properties.getProperty("SBOI.timeout", "5000"));
        batch_timeout = Long.parseLong(properties.getProperty("batch_timeout","2000"));
        pastEvents = Arrays.asList(properties.getProperty("events.past", "").split(","));
        futureEvents = Arrays.asList(properties.getProperty("events.future", "").split(","));
        lockClient.getConnectionStateListenable().addListener(new ConcurrencyConnectionStateListener());
        componentName = properties.getProperty("component.name");
        componentVersion = properties.getProperty("component.version");
        configured = true;

    }



    /**
     * This is the worker method for the component. Once a batch have been found that match the criteria and this batch
     * have been properly locked, this method is called.
     * The results of the work should be collected in the resultCollector. These will be added to the event system afterwards
     * @param batchId the batch id
     * @param roundTripNumber the round trip number
     * @param resultCollector the result collector
     * @throws Exception if something failed
     */
    public abstract void doWorkOnBatch(Long batchId, int roundTripNumber, ResultCollector resultCollector) throws Exception;


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
     */
    public synchronized void doWork() {
        if (!configured){
            throw new IllegalStateException("Component not configured");
        }
        try {
            lockClient.start();

            //lock SBOI for this component name
            InterProcessLock SBOI_lock = new InterProcessSemaphoreMutex(lockClient, getSBOILockpath());

            boolean sboi_locked = SBOI_lock.acquire(SBOI_timeout, TimeUnit.MILLISECONDS);
            if (!sboi_locked){
                //log this
                return;
            }
            //get batches
            List<Batch> batches = getSBOI().getBatches(false, null);


            Batch lockedBatch = null;
            InterProcessLock batchlock = null;

            //for each batch
            for (Batch batch : batches) {
                //attempt to lock

                batchlock = new InterProcessSemaphoreMutex(lockClient, getBatchLockPath(batch));
                boolean success = batchlock.acquire(batch_timeout, TimeUnit.MILLISECONDS);
                if (success){//if lock gotten
                    //break loop
                    lockedBatch = batch;
                    break;
                }
            }
            //unlock SBOI
            SBOI_lock.release();

            if (lockedBatch == null){
                //no batch available
                return;
            }

            ResultCollector result = new ResultCollector();
            try {
                //do work
                doWorkOnBatch(lockedBatch.getBatchID(),lockedBatch.getRoundTripNumber(),result);

            } catch (Exception e){
                //the work failed
                result.setSuccess(false);
                result.addMessage(e.getMessage());
                log.error("Failed",e);
            }
            finally {
                //preserve the result
                preserveResult(lockedBatch,result);
                //unlock Batch
                batchlock.release();
            }
        } catch (Throwable e) {
            log.error("Caugth throwable",e);
            System.exit(2);
        } finally {
            Closeables.closeQuietly(lockClient);
        }

    }

    /**
     * TODO
     * This method stores the event back into DOMS
     * @param batch the batch worked on
     * @param result the result of the work
     */
    private void preserveResult(Batch batch, ResultCollector result) {
        //To change body of created methods use File | Settings | File Templates.
    }


    /**
     * Get the zookeeper lockpath for the SBOI instance for this component
     * @return the lock path
     */
    private final String getSBOILockpath() {
        return "SBOI/"+componentName;
    }

    /**
     * Get the lock path for this batch for this component
     * @param batch the batch to lock
     * @return the zookeepr lock path
     */
    private String getBatchLockPath(Batch batch) {
        return componentName+"/B"+batch.getBatchID()+"-RT"+batch.getRoundTripNumber();
    }

    /**
     * TODO
     * Get the SBOI query interface
     * @return
     */
    private DataSource getSBOI() {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }


}

