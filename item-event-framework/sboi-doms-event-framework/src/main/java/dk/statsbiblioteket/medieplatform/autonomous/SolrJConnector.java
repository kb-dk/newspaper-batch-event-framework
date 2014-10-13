package dk.statsbiblioteket.medieplatform.autonomous;

import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrJConnector {

    private HttpSolrServer solrServer;

    private static final Logger log = LoggerFactory.getLogger(SolrJConnector.class);

    public SolrJConnector(String serverUrl) {
        try {
            solrServer = new HttpSolrServer(serverUrl);
            solrServer.setRequestWriter(new BinaryRequestWriter()); //To avoid http error code 413/414, due to monster URI. (and it is faster)
        } catch (Exception e) {
            System.out.println("Unable to connect to:" + serverUrl);
            e.printStackTrace();
            log.error("Unable to connect to to:" + serverUrl, e);
        }
    }

    public HttpSolrServer getSolrServer() {
        return solrServer;
    }
}