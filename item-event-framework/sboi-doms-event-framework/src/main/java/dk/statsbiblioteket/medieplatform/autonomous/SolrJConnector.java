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
            //Silent all the debugs log from HTTP Client (used by SolrJ)
/*
            System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
            System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
            System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "ERROR");
            System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "ERROR");
            System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "ERROR");
            java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.OFF);
            java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.OFF);

*/

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