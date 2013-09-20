package dk.statsbiblioteket.newspaper.processmonitor.datasources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: abr
 * Date: 9/17/13
 * Time: 10:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class DataSourceMockup implements DataSource {

    private ArrayList<Batch> dummyBatches;

    private String username;
    private String password;

    public DataSourceMockup() {

/*
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
*/

        Event e1 = new Event();
        e1.setEventID("reels-sent");
        e1.setSuccess(true);

        Event e2 = new Event();
        e2.setEventID("data-received");
        e2.setSuccess(false);

        Event e3 = new Event();
        e3.setEventID("data-archived");
        e3.setSuccess(true);
        List<Event> b1Events = new ArrayList<Event>();
        b1Events.add(e1);
        b1Events.add(e2);
        b1Events.add(e3);

        Batch b1 = new Batch();
        b1.setBatchID("hans");
        b1.setEventList(b1Events);

        Event e4 = new Event();
        e4.setEventID("reels-sent");
        e4.setSuccess(true);

        Event e5 = new Event();
        e5.setEventID("data-received");
        e5.setSuccess(false);

        Event e6 = new Event();
        e6.setEventID("data-archived");
        e6.setSuccess(false);

        List<Event> b2Events = new ArrayList<Event>();
        b2Events.add(e4);
        b2Events.add(e5);
        b2Events.add(e6);

        Batch b2 = new Batch();
        b2.setBatchID("bjarne");
        b2.setEventList(b2Events);

        dummyBatches = new ArrayList<Batch>();
        dummyBatches.add(b1);
        dummyBatches.add(b2);
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean isRunNrInBatchID() {
        return false;
    }

    @Override
    public List<Batch> getBatches(boolean includeDetails, Map<String, String> filters) {
        return dummyBatches;
    }

    @Override
    public Batch getBatch(String batchID, boolean includeDetails) throws NotFoundException {
        Batch batch = null;
        for (Batch b : dummyBatches) {
            if (b.getBatchID().equals(batchID)) {
                batch = b;
            }
        }
        if (batch == null) {
            throw new NotFoundException("Batch not found" + batchID);
        }
        return batch;
    }

    @Override
    public Event getBatchEvent(String batchID, String eventID, boolean includeDetails) throws NotFoundException {
        Event event = null;
        for (Batch b : dummyBatches) {
            if (b.getBatchID().equals(batchID)) {
                for (Event e : b.getEventList()) {
                    if (e.getEventID().equals(eventID)) {
                        event = e;
                    }
                }
            }
        }
        if (event == null) {
            throw new NotFoundException("Event not found");
        }
        return event;
    }
}
