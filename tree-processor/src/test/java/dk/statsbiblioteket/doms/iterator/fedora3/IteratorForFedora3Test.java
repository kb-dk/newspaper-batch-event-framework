package dk.statsbiblioteket.doms.iterator.fedora3;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import dk.statsbiblioteket.autonomous.iterator.fedora3.ContentModelFilter;
import dk.statsbiblioteket.autonomous.iterator.fedora3.IteratorForFedora3;
import dk.statsbiblioteket.doms.AbstractTests;
import dk.statsbiblioteket.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.doms.pidgenerator.CommunicationException;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


public class IteratorForFedora3Test extends AbstractTests {

    private TreeIterator iterator;

    @Override
    public TreeIterator getIterator() throws URISyntaxException, IOException {
        if (iterator == null){
            Properties properties = new Properties();
            properties.load(new FileReader(new File(System.getProperty(
                    "integration.test.newspaper.properties"))));
            System.out.println(properties.getProperty("fedora.admin.username"));
            Client client = Client.create();
            client.addFilter(new HTTPBasicAuthFilter(properties.getProperty("fedora.admin.username"),
                    properties.getProperty("fedora.admin.password")));

            // The uuid below is for a test object ingested by CSR that he never deletes as
            // part of clean-up
            try {
                iterator = new IteratorForFedora3("uuid:37d1cf35-099a-45f1-8647-d4dbbf7e68cc", client,
                        properties.getProperty("fedora.server"), new TestFilter());
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
        return iterator;
    }

    static class TestFilter implements ContentModelFilter {

        public boolean isAttributeDatastream(String dsid, List<String> types) {
            List<String> names = Arrays.asList("DC", "MODS", "FILM", "EDITION", "ALTO", "MIX");
            return names.contains(dsid);
        }

        public boolean isChildRel(String predicate, List<String> types) {
            if (predicate.contains("#hasPart")){
                return true;
            }
            return false;
        }
    }

    @Override
    @Test(groups = "integrationTest", enabled = true)
    public void testIterator() throws Exception {
        super.testIterator();
    }

    @Override
    @Test(groups = "integrationTest", enabled = false)
    public void testIteratorWithSkipping() throws Exception {
        super.testIteratorWithSkipping();
    }
}
