package dk.statsbiblioteket.medieplatform.autonomous

import groovy.transform.Canonical

/** An event that have taken place on a batch */
@Canonical
class Event {

    String eventID;
    boolean success;
    String details;
    Date date;

}
