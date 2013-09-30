package dk.statsbiblioteket.autonomous;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.state.ConnectionState;
import com.netflix.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;

public class ConcurrencyConnectionStateListener implements ConnectionStateListener{
    private static Logger log = org.slf4j.LoggerFactory.getLogger(ConcurrencyConnectionStateListener.class);

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        switch (newState){
            case SUSPENDED:
                log.error("Connection suspended");
                break;
            case LOST:
                log.error("Connection suspended");
                break;
            default:
                log.error("Connection event: %",newState.name());
                break;
        }
    }
}
