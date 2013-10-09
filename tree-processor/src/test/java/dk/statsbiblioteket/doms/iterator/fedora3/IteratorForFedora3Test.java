package dk.statsbiblioteket.doms.iterator.fedora3;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import dk.statsbiblioteket.doms.AbstractTests;
import dk.statsbiblioteket.doms.iterator.common.TreeIterator;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
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

            // The uuid below is for a test object ingested by CSR that he never deletes as part of clean-up
            iterator = new IteratorForFedora3("uuid:269f14c0-8399-466e-b481-770c33cd0753", client,
                    properties.getProperty("fedora.server"), new TestFilter());
        }
        return iterator;
    }

    static class TestFilter implements ContentModelFilter{

        public boolean isAttributeDatastream(String dsid, List<String> types) {
            if (dsid.equals("DC")){
                return true;
            }
            if (dsid.equals("MODS")){
                return true;
            }
            if (dsid.equals("FILM")){
                return true;
            }
            if (dsid.equals("EDITION")){
                return true;
            }
            if (dsid.equals("ALTO")){
                return true;
            }
            if (dsid.equals("MIX")){
                return true;
            }
            return false;
        }

        public boolean isChildRel(String predicate, List<String> types) {
            if (predicate.contains("#hasPart")){
                return true;
            }
            return false;
        }
    }

    @Override
    @Test(groups = "integrationTest")
    public void testIterator() throws Exception {
        super.testIterator();
    }

    @Override
    @Test(groups = "integrationTest")
    public void testIteratorWithSkipping() throws Exception {
        super.testIteratorWithSkipping();
    }
}
