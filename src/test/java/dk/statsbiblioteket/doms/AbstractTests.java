package dk.statsbiblioteket.doms;

import dk.statsbiblioteket.doms.iterator.common.Event;
import dk.statsbiblioteket.doms.iterator.common.SBIterator;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: abr
 * Date: 9/4/13
 * Time: 2:44 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractTests {


    public abstract SBIterator getIterator() throws URISyntaxException;


    private static final String indentString = "..................................................";


    public void testIterator() throws Exception {


        printStructure(getIterator());
    }

    private String printEvent(Event next) {
        switch (next.getType()){
            case NodeBegin:
                return "<"+next.getLocalname()+">";
            case NodeEnd:
                return "</"+next.getLocalname()+">";
            case Attribute:
                return "<"+next.getLocalname()+"/>";
            default:
                return next.toString();
        }
    }

    public void testIteratorWithSkipping() throws Exception {

        List<SBIterator> avisIterators = new ArrayList<SBIterator>();


        System.out.println("Print the batch and film, and store the iterators for the aviser");
        int indent = 0;
        while (getIterator().hasNext()){
            Event next = getIterator().next();

            String s;
            switch (next.getType()){
                case NodeBegin:
                    s = getIndent(indent);
                    System.out.println(s+printEvent(next));
                    indent+=2;
                    if (indent > 4){
                        SBIterator avis = getIterator().skipToNextSibling();
                        avisIterators.add(avis);
                        indent-=2;
                    }
                    break;
                case NodeEnd:
                    indent-=2;
                    s = getIndent(indent);
                    System.out.println(s+printEvent(next));
                    break;
                case Attribute:
                    s = getIndent(indent);
                    System.out.println(s+printEvent(next));
                    break;
            }
        }

        System.out.println("Print each of the newspapers in order");
        for (SBIterator avisIterator : avisIterators) {
            System.out.println("We found this newspaper");
            printStructure(avisIterator);
        }

    }

    private void printStructure(SBIterator avisIterator) throws IOException {
        int indent = 0;
        while (avisIterator.hasNext()) {
            Event next = avisIterator.next();
            switch (next.getType()){
                case NodeBegin:
                {
                    String s;
                    s = getIndent(indent);
                    System.out.println(s+printEvent(next));
                    indent+=2;
                    break;
                }
                case NodeEnd:
                {
                    String s;
                    indent-=2;
                    s = getIndent(indent);
                    System.out.println(s+printEvent(next));
                    break;
                }
                case Attribute: {
                    String s = getIndent(indent);
                    List<String> content = IOUtils.readLines(next.getText());
                    System.out.println(s+printEvent(next));
                    s = getIndent(indent+2);
                    for (String s1 : content) {
                        System.out.println(s+s1);
                    }
                    break;
                }
            }

        }
    }


    private static String getIndent(int indent) {
        String s;
        if (indent > 0){
            s = indentString.substring(0,indent);
        } else {
            s = "";
        }
        return s;
    }
}
