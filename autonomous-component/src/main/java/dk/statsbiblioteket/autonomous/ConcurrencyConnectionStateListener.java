package dk.statsbiblioteket.autonomous;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.state.ConnectionState;
import com.netflix.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ConcurrencyConnectionStateListener implements ConnectionStateListener{
    private static Logger log = org.slf4j.LoggerFactory.getLogger(ConcurrencyConnectionStateListener.class);

    private AutonomousComponent autonomousComponent;
    private List<BatchWorker> batchWorkerList = new ArrayList<>();

    public ConcurrencyConnectionStateListener(AutonomousComponent autonomousComponent) {
        this.autonomousComponent = autonomousComponent;
    }

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        switch (newState){
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
                log.error("Connection event: %",newState.name());
                autonomousComponent.setPaused(false);
                unpauseWorkers();
                break;
        }
    }

    private void unpauseWorkers() {
        for (BatchWorker batchWorker : batchWorkerList) {
            batchWorker.setPause(false);
        }
    }

    private void pauseWorkers() {
        for (BatchWorker batchWorker : batchWorkerList) {
            batchWorker.setPause(true);
        }
    }

    private void stopWorkers() {
        for (BatchWorker batchWorker : batchWorkerList) {
            batchWorker.setStop(true);
        }
    }

    public void add(BatchWorker batchWorker) {
        batchWorkerList.add(batchWorker);
    }
}
