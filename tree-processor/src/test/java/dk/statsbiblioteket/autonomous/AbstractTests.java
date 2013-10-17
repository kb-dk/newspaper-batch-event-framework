package dk.statsbiblioteket.autonomous;

import dk.statsbiblioteket.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.autonomous.iterator.common.ParsingEvent;
import dk.statsbiblioteket.autonomous.iterator.common.TreeIterator;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTests {


    public abstract TreeIterator getIterator() throws URISyntaxException, IOException;


    private static final String indentString = "..................................................";


    public void testIterator() throws Exception {


        printStructure(getIterator());
    }

    private String printEvent(ParsingEvent next) {
        switch (next.getType()){
            case NodeBegin:
                return "<'"+next.getName()+"'>";
            case NodeEnd:
                return "</'"+next.getName()+"'>";
            case Attribute:
                return "<'"+next.getName()+"'/>";
            default:
                return next.toString();
        }
    }

    public void testIteratorWithSkipping() throws Exception {

        List<TreeIterator> avisIterators = new ArrayList<TreeIterator>();


        System.out.println("Print the batch and film, and store the iterators for the aviser");
        int indent = 0;
        while (getIterator().hasNext()){
            ParsingEvent next = getIterator().next();

            String s;
            switch (next.getType()){
                case NodeBegin:
                    s = getIndent(indent);
                    System.out.println(s+printEvent(next));
                    indent+=2;
                    if (indent > 4){
                        TreeIterator avis = getIterator().skipToNextSibling();
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
        for (TreeIterator avisIterator : avisIterators) {
            System.out.println("We found this newspaper");
            printStructure(avisIterator);
        }

    }

    private void printStructure(TreeIterator avisIterator) throws IOException {
        int indent = 0;
        int files = 0;
        while (avisIterator.hasNext()) {
            ParsingEvent next = avisIterator.next();
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
                    AttributeParsingEvent attributeEvent = (AttributeParsingEvent) next;
                    List<String> content = IOUtils.readLines(attributeEvent.getData());
                    System.out.println(s+printEvent(next));
                    s = getIndent(indent+2);
                    System.out.println(s + "[" + content.size() +  " lines of content]");
                    String checksum = attributeEvent.getChecksum();
                    if (checksum != null){
                        System.out.println(s + "md5: " + checksum +  "");
                    }
                    //for (String s1 : content) {
                    //    System.out.println(s+s1);
                    // }
                    files++;
                    break;

                }
            }

        }
        Assert.assertEquals(indent,0,"Indent is not reset after iteration");
        Assert.assertTrue(files>10,"We have not encountered very much, is the test data broken?");
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
