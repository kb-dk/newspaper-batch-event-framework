package dk.statsbiblioteket.medieplatform.autonomous;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTests {


    private static final String indentString = "                                                   ";

    private static String getIndent(int indent) {
        String s;
        if (indent > 0) {
            s = indentString.substring(0, indent);
        } else {
            s = "";
        }
        return s;
    }

    public abstract TreeIterator getIterator()
            throws
            URISyntaxException,
            IOException;

    public void testIterator(final boolean print, final boolean printContent)
            throws
            Exception {


        printStructure(getIterator(), print,printContent);
    }

    private String printEvent(ParsingEvent next)
            throws
            IOException {
        switch (next.getType()) {
            case NodeBegin:
                return "<node name=\"" + next.getName() + "\">";
            case NodeEnd:
                return "</node>";
            case Attribute:
                if (next instanceof AttributeParsingEvent) {
                    AttributeParsingEvent attributeParsingEvent = (AttributeParsingEvent) next;
                    return "<attribute name=\"" + next.getName() + "\" checksum=\"" + attributeParsingEvent
                            .getChecksum() + "\" />";
                }

            default:
                return next.toString();
        }
    }

    public void testIteratorWithSkipping(final boolean print,final boolean printContent)
            throws
            Exception {

        List<TreeIterator> avisIterators = new ArrayList<>();


        System.out.println("Print the batch and film, and store the iterators for the aviser");
        int indent = 0;
        while (getIterator().hasNext()) {
            ParsingEvent next = getIterator().next();

            String s;
            switch (next.getType()) {
                case NodeBegin:
                    s = getIndent(indent);
                    if (print) {
                        System.out.println(s + printEvent(next));
                    }
                    indent += 2;
                    if (indent > 4) {
                        TreeIterator avis = getIterator().skipToNextSibling();
                        avisIterators.add(avis);
                        indent -= 2;
                    }
                    break;
                case NodeEnd:
                    indent -= 2;
                    s = getIndent(indent);
                    if (print) {
                        System.out.println(s + printEvent(next));
                    }
                    break;
                case Attribute:
                    s = getIndent(indent);
                    if (print) {
                        System.out.println(s + printEvent(next));
                    }
                    break;
            }
        }
        if (print) {
            System.out.println("Print each of the newspapers in order");
        }
        for (TreeIterator avisIterator : avisIterators) {
            if (print) {
                System.out.println("We found this newspaper");
            }
            printStructure(avisIterator, print, printContent);
        }

    }

    private void printStructure(TreeIterator avisIterator,
                                final boolean print, final boolean printContent)
            throws
            IOException {
        int indent = 0;
        int files = 0;
        while (avisIterator.hasNext()) {
            ParsingEvent next = avisIterator.next();
            switch (next.getType()) {
                case NodeBegin: {
                    String s;
                    s = getIndent(indent);
                    if (print) {
                        System.out.println(s + printEvent(next));
                    }
                    indent += 2;
                    break;
                }
                case NodeEnd: {
                    String s;
                    indent -= 2;
                    s = getIndent(indent);
                    if (print) {
                        System.out.println(s + printEvent(next));
                    }
                    break;
                }
                case Attribute: {
                    String s = getIndent(indent);
                    AttributeParsingEvent attributeEvent = (AttributeParsingEvent) next;
                    List<String> content = IOUtils.readLines(attributeEvent.getData());
                    if (print) {
                        System.out.println(s + printEvent(next));
                    }
                    s = getIndent(indent + 2);
                    if (print && printContent) {
                        System.out.println(s + "[" + content.size() + " lines of content]");
                    }
                    files++;
                    break;

                }
            }

        }
        Assert.assertEquals(indent, 0, "Indent is not reset after iteration");
        Assert.assertTrue(files > 1, "We have not encountered very much, only "+files+", is the test data broken?");
    }
}
