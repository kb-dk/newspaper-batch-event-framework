package dk.statsbiblioteket.medieplatform.autonomous;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.state.ConnectionState;
import com.netflix.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * This listener listens for connection events in regards to the lock server. Our locks are only valid
 * as long as we maintain a connection to the lock server.
 * When the connection is suspended, execution should be paused. When the connection is lost, all
 * execution should stop, as we cannot ensure that the batches are locked anymore.
 */
public class ConcurrencyConnectionStateListener implements ConnectionStateListener {
    private static Logger log = org.slf4j.LoggerFactory.getLogger(ConcurrencyConnectionStateListener.class);

    private AutonomousComponent autonomousComponent;
    private List<BatchWorker> batchWorkerList = new ArrayList<>();

    /**
     * Constructs a new state listener
     *
     * @param autonomousComponent the autonomous component it listens for
     */
    public ConcurrencyConnectionStateListener(AutonomousComponent autonomousComponent) {
        this.autonomousComponent = autonomousComponent;
    }

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        switch (newState) {
            case SUSPENDED:
                log.error("Connection suspended");
                autonomousComponent.setPaused(true);
                pauseWorkers();
                break;
            case LOST:
                log.error("Connection suspended");
                autonomousComponent.setStopped(true);
                stopWorkers();
                break;
            default:
                log.error("Connection event: %", newState.name());
                autonomousComponent.setPaused(false);
                unpauseWorkers();
                break;
        }
    }


    /** Unpause all workers */
    private void unpauseWorkers() {
        for (BatchWorker batchWorker : batchWorkerList) {
            batchWorker.setPause(false);
        }
    }

    /** Pause all workers */
    private void pauseWorkers() {
        for (BatchWorker batchWorker : batchWorkerList) {
            batchWorker.setPause(true);
        }
    }

    /** Stop all workers */
    private void stopWorkers() {
        for (BatchWorker batchWorker : batchWorkerList) {
            batchWorker.setStop(true);
        }
    }

    /**
     * Add a batch worker to the list of executions to stop or suspend
     *
     * @param batchWorker the batch worker
     */
    public void add(BatchWorker batchWorker) {
        batchWorkerList.add(batchWorker);
    }
}
