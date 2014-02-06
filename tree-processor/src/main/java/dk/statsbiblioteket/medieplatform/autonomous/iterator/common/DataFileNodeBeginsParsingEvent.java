package dk.statsbiblioteket.medieplatform.autonomous.iterator.common;

public class DataFileNodeBeginsParsingEvent extends NodeBeginsParsingEvent {

    public DataFileNodeBeginsParsingEvent(String name) {
           super(name, null);
       }

    public DataFileNodeBeginsParsingEvent(String name, String location) {
        super(name, location);
    }
}
