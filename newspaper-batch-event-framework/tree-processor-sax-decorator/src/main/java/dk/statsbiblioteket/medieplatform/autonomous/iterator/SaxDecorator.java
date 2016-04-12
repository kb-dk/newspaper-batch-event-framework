package dk.statsbiblioteket.medieplatform.autonomous.iterator;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * Created by abr on 12-04-16.
 */
public class SaxDecorator extends InputSource implements TreeEventHandler {

    private final PipedReader reader;
    int indentSize;

    /**
     * blank-string for the current indentation.
     */
    String currentIndent;

    /**
     * blank string of length indentSize for augmenting/decrementing the current indent.
     */
    String extraIndentPerLevel;

    Writer xmlBuilder;
    boolean finished = false;


    public SaxDecorator() throws IOException {
        reader = new PipedReader();
        xmlBuilder = new PipedWriter(reader);

        this.indentSize = 2;
        currentIndent = "";
        extraIndentPerLevel = "";
        for (int i = 0; i < indentSize; i++) {
            extraIndentPerLevel = extraIndentPerLevel + " ";
        }
    }

    /**
     * We use a constant "/" as file separator in DOMS, not the system-dependent file-separator, so this
     * method finds the last token in a path assuming that "/" is the file separator.
     * @param name
     * @return
     */
    public static String getLastTokenInPath(String name) {
        String [] nameSplit = name.split("/");
        return nameSplit[nameSplit.length -1];
    }

    @Override
    public Reader getCharacterStream() {
        return reader;
    }


    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        String shortName = getLastTokenInPath(event.getName());
        try {
            xmlBuilder.append(currentIndent + "<node name=\"" + event.getName() + "\" shortName=\"" + shortName + "\">\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        currentIndent = currentIndent + extraIndentPerLevel;
    }

    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
        currentIndent = currentIndent.replaceFirst(extraIndentPerLevel, "");
        try {
            xmlBuilder.append(currentIndent + "</node>\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        String shortName = getLastTokenInPath(event.getName());

        try {
            xmlBuilder.append(currentIndent + "<attribute name=\"" + event.getName() + "\" shortName=\"" + shortName + "\"  checksum=\"" + event
                    .getChecksum() + "\" />\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleFinish() {
        finished = true;
        try {
            xmlBuilder.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
