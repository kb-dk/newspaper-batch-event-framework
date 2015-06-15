package dk.statsbiblioteket.medieplatform.autonomous;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

public class SBOIEventIndexTest {

    private static final String EVENT_ID = "event";
    private static final long OLD_TIME = 0l;
    private static final long FUTURE_TIME = 2000l;
    private static final long NOW_TIME = 1000l;
    private SBOIEventIndex sboiEventIndex;
    private Event oldEvent;
    private Event newEvent;

    @BeforeMethod
    public void createSboiEventIndex() throws Exception {
        PremisManipulatorFactory pmf = mock(PremisManipulatorFactory.class);
        DomsEventStorage des = mock(DomsEventStorage.class);
        sboiEventIndex = new SBOIEventIndex("", pmf, des, 1);
    }

    @BeforeMethod
    public void createTestEvents() throws Exception {
        oldEvent = createEvent(OLD_TIME);
        newEvent = createEvent(FUTURE_TIME);
    }

    @Test
    public void testMatchOldEventOneOldEvent() throws Exception {
        Item item = createItem(oldEvent);
        EventTrigger.Query query = new EventTrigger.Query();
        query.getOldEvents().add(EVENT_ID);

        boolean match = sboiEventIndex.match(item, query);

        assertTrue(match);
    }

    @Test
    public void testMatchOldEventOneNewEvent() throws Exception {
        Item item = createItem(newEvent);
        EventTrigger.Query query = new EventTrigger.Query();
        query.getOldEvents().add(EVENT_ID);

        boolean match = sboiEventIndex.match(item, query);

        assertFalse(match);
    }

    @Test
    public void testMatchOldEventOneOldOneNewEvent() throws Exception {
        Item item = createItem(oldEvent, newEvent);
        EventTrigger.Query query = new EventTrigger.Query();
        query.getOldEvents().add(EVENT_ID);

        boolean match = sboiEventIndex.match(item, query);

        assertFalse(match);
    }

    @Test
    public void testMatchFutureEventsFound() throws Exception {
        Item item = createItem(oldEvent);
        EventTrigger.Query query = new EventTrigger.Query();
        query.getFutureEvents().add(EVENT_ID);

        boolean match = sboiEventIndex.match(item, query);

        assertFalse(match);
    }

    @Test
    public void testMatchFutureEventsNotFound() throws Exception {
        Item item = createItem();
        EventTrigger.Query query = new EventTrigger.Query();
        query.getFutureEvents().add(EVENT_ID);

        boolean match = sboiEventIndex.match(item, query);

        assertTrue(match);
    }

    @Test
    public void testMatchOldSuccessfulEventsFound() throws Exception {
        Item item = createItem(oldEvent);
        EventTrigger.Query query = new EventTrigger.Query();
        query.getPastSuccessfulEvents().add(EVENT_ID);

        boolean match = sboiEventIndex.match(item, query);

        assertTrue(match);
    }

    @Test
    public void testMatchOldSuccessfulEventsNotFound() throws Exception {
        Item item = createItem();
        EventTrigger.Query query = new EventTrigger.Query();
        query.getPastSuccessfulEvents().add(EVENT_ID);

        boolean match = sboiEventIndex.match(item, query);

        assertFalse(match);
    }

    @Test
    public void testFilterOnlyNewestEvent() throws Exception {
        List<Event> list = sboiEventIndex.filterNewestEvent(Arrays.asList(oldEvent, newEvent));
        assertEquals(Collections.singletonList(newEvent), list);
    }

    private Item createItem(Event... events) {
        Item item = new Item();
        item.setLastModified(new Date(NOW_TIME));
        item.setEventList(Arrays.asList(events));
        return item;
    }

    private Event createEvent(long date) {
        Event event = new Event();
        event.setDate(new Date(date));
        event.setEventID(EVENT_ID);
        event.setSuccess(true);
        return event;
    }
}
