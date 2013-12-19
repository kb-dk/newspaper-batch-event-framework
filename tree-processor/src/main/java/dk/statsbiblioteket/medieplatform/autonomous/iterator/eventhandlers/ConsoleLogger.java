package dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.List;

/** Prints the tree to the console. Used for testing purposes. */
public class ConsoleLogger extends DefaultTreeEventHandler {
    private static final String indentString = "..................................................";
    int indentLevel = 0;

    /**
     * Prints a begin node and indents a step
     *
     * @param event The event to handle
     */
    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        printIndentNode(event);
        indentLevel += 2;
    }

    /**
     * Prints an end node and unindents a step
     *
     * @param event The event to handle
     */
    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
        // We have exited a node, decrease indentLevel-level again
        indentLevel -= 2;
        printIndentNode(event);
    }

    /**
     * Prints an attribute event and its attributes, properly indented
     *
     * @param event The event to handle
     */
    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        try {
            List<String> content = IOUtils.readLines(event.getData());

            String checksum = event.getChecksum();
            printIndentNode(event);
            printIndentAttribute("[" + content.size() + " lines of content]");
            printIndentAttribute("Checksum: " + checksum);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prints an indented node
     *
     * @param event The event to handle
     */
    private void printIndentNode(ParsingEvent event) {
        System.out.println(getIndentString() + printEvent(event));
    }

    /**
     * Prints an indented attribute event
     *
     * @param attributeString The event to handle
     */
    private void printIndentAttribute(String attributeString) {
        System.out.println(getIndentString() + ".." + attributeString);
    }

    /**
     * Create a string of dots, used for indenting.
     *
     * @return A string of dots, as long as specified by input parameter 'indentLevel'
     */
    private String getIndentString() {
        String s;
        if (indentLevel > 0) {
            s = indentString.substring(0, indentLevel);
        } else {
            s = "";
        }
        return s;
    }

    /**
     * Print the name of an event to a string
     *
     * @param event The event to handle
     *
     * @return The name of the event as a string
     */
    private String printEvent(ParsingEvent event) {
        switch (event.getType()) {
            case NodeBegin:
                return "<" + event.getName() + ">";
            case NodeEnd:
                return "</" + event.getName() + ">";
            case Attribute:
                return "<" + event.getName() + "/>";
            default:
                return event.toString();
        }
    }
}
