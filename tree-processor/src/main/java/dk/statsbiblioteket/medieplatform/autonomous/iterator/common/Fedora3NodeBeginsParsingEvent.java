package dk.statsbiblioteket.medieplatform.autonomous.iterator.common;

/**
 * This is a just a NodeBeginsParsingEvent decorated with a doms-pid.
 */
public class Fedora3NodeBeginsParsingEvent extends NodeBeginsParsingEvent {

    private String pid;

    public Fedora3NodeBeginsParsingEvent(NodeBeginsParsingEvent originalEvent, String pid) {
        super(originalEvent.getName());
        this.pid = pid;
    }

    public String getPid() {
        return pid;
    }



}
