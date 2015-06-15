package dk.statsbiblioteket.medieplatform.autonomous;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
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
        Item item = createItem(Collections.singletonList(oldEvent));
        EventTrigger.Query query = createQuery(Collections.singletonList(EVENT_ID), Collections.<String>emptyList(),
                                               Collections.<String>emptyList());

        boolean match = sboiEventIndex.match(item, query);

        assertTrue(match);
    }

    @Test
    public void testMatchOldEventOneNewEvent() throws Exception {
        Item item = createItem(Collections.singletonList(newEvent));
        EventTrigger.Query query = createQuery(Collections.singletonList(EVENT_ID), Collections.<String>emptyList(),
                                               Collections.<String>emptyList());

        boolean match = sboiEventIndex.match(item, query);

        assertFalse(match);
    }

    @Test
    public void testMatchOldEventOneOldOneNewEvent() throws Exception {
        Item item = createItem(Arrays.asList(oldEvent, newEvent));
        EventTrigger.Query query = createQuery(Collections.singletonList(EVENT_ID), Collections.<String>emptyList(),
                                               Collections.<String>emptyList());

        boolean match = sboiEventIndex.match(item, query);

        assertFalse(match);
    }

    @Test
    public void testMatchFutureEventsFound() throws Exception {
        Item item = createItem(Collections.singletonList(oldEvent));
        EventTrigger.Query query = createQuery(Collections.<String>emptyList(), Collections.singletonList(EVENT_ID),
                                               Collections.<String>emptyList());

        boolean match = sboiEventIndex.match(item, query);

        assertFalse(match);
    }

    @Test
    public void testMatchFutureEventsNotFound() throws Exception {
        Item item = createItem(Collections.<Event>emptyList());
        EventTrigger.Query query = createQuery(Collections.<String>emptyList(), Collections.singletonList(EVENT_ID),
                                               Collections.<String>emptyList());

        boolean match = sboiEventIndex.match(item, query);

        assertTrue(match);
    }

    @Test
    public void testMatchOldSuccessfulEventsFound() throws Exception {
        Item item = createItem(Collections.singletonList(oldEvent));
        EventTrigger.Query query = createQuery(Collections.<String>emptyList(), Collections.<String>emptyList(),
                                               Collections.singletonList(EVENT_ID));

        boolean match = sboiEventIndex.match(item, query);

        assertTrue(match);
    }

    @Test
    public void testMatchOldSuccessfulEventsNotFound() throws Exception {
        Item item = createItem(Collections.<Event>emptyList());
        EventTrigger.Query query = createQuery(Collections.<String>emptyList(), Collections.<String>emptyList(),
                                               Collections.singletonList(EVENT_ID));

        boolean match = sboiEventIndex.match(item, query);

        assertFalse(match);
    }

    @Test
    public void testFilterOnlyNewestEvent() throws Exception {
        List list = sboiEventIndex.filterNewestEvent(Arrays.asList(oldEvent, newEvent));
        assertEquals(Collections.singletonList(newEvent), list);
    }

    private Item createItem(List<Event> eventList) {
        Item item = new Item();
        item.setLastModified(new Date(NOW_TIME));
        item.setEventList(eventList);
        return item;
    }

    private Event createEvent(long date) {
        Event event = new Event();
        event.setDate(new Date(date));
        event.setEventID("event");
        event.setSuccess(true);
        return event;
    }

    private EventTrigger.Query createQuery(List<String> oldEvents, List<String> futureEvents,
                                           List<String> pastSuccessfulEvents) {
        EventTrigger.Query query = new EventTrigger.Query();
        query.getOldEvents().addAll(oldEvents);
        query.getFutureEvents().addAll(futureEvents);
        query.getPastSuccessfulEvents().addAll(pastSuccessfulEvents);
        return query;
    }
}
