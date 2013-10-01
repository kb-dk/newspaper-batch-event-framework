package dk.statsbiblioteket.doms.iterator.fedora3;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import dk.statsbiblioteket.doms.AbstractTests;
import dk.statsbiblioteket.doms.iterator.common.ContentModelFilter;
import dk.statsbiblioteket.doms.iterator.common.SBIterator;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: abr
 * Date: 9/4/13
 * Time: 12:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class IteratorForFedora3Test extends AbstractTests {

    private SBIterator iterator;

    @Override
    public SBIterator getIterator() throws URISyntaxException {
        if (iterator == null){
            System.out.println(System.getProperty("fedora.admin.username"));
            Client client = Client.create();
            client.addFilter(new HTTPBasicAuthFilter(System.getProperty("fedora.admin.username"),System.getProperty("fedora.admin.password")));
            iterator = new IteratorForFedora3("doms:ContentModel_Program", client,
                    System.getProperty("fedora.server"),new TestFilter());
        }
        return iterator;
    }

    static class TestFilter implements ContentModelFilter{

        public boolean isAttributeDatastream(String dsid, List<String> types) {
            if (dsid.equals("DC")){
            return true;
            }
            return false;
        }

        public boolean isChildRel(String predicate, List<String> types) {
            if (predicate.contains("#extendsModel")){
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
