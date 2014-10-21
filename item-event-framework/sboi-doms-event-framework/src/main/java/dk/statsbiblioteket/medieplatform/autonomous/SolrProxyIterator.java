package dk.statsbiblioteket.medieplatform.autonomous;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class SolrProxyIterator<T extends Item> implements Iterator<T> {
    private static Logger log = org.slf4j.LoggerFactory.getLogger(SolrProxyIterator.class);


    Iterator<T> items = null;


    private String queryString;
    private boolean details;
    private HttpSolrServer summaSearch;
    private PremisManipulatorFactory<T> premisManipulatorFactory;
    private DomsEventStorage<T> domsEventStorage;
    private final int rows = 100;
    private int start = 0;
    private int position = 0;


    public SolrProxyIterator(String queryString, boolean details, HttpSolrServer summaSearch,
                             PremisManipulatorFactory<T> premisManipulatorFactory, DomsEventStorage<T> domsEventStorage) {
        this.queryString = queryString;
        this.details = details;
        this.summaSearch = summaSearch;
        this.premisManipulatorFactory = premisManipulatorFactory;
        this.domsEventStorage = domsEventStorage;
        search();
    }

    @Override
    public synchronized boolean hasNext() {
        if (position >= rows) {
            start += rows;
            position = 0;
            search();
        }
        return items.hasNext();
    }

    private void search() {
        try {
            SolrQuery query = new SolrQuery();
            query.setQuery(queryString);
            query.setRows(rows); //Fetch size. Do not go over 1000 unless you specify fields to fetch which does not include content_text
            query.setStart(start);
            //IMPORTANT!Only use facets if needed.
            query.set("facet", "false"); //very important. Must overwrite to false. Facets are very slow and expensive.
            query.setFields(SBOIEventIndex.UUID, SBOIEventIndex.LAST_MODIFIED);
            if (!details) {
                query.addField(SBOIEventIndex.PREMIS_NO_DETAILS);
            }

            query.addSort(SBOIEventIndex.SORT_DATE, SolrQuery.ORDER.asc);

            QueryResponse response = summaSearch.query(query);
            SolrDocumentList results = response.getResults();
            List<T> hits = new ArrayList<>();
            for (SolrDocument result : results) {
                T hit;
                String uuid = result.getFirstValue(SBOIEventIndex.UUID).toString();
                String lastModified = result.getFirstValue(SBOIEventIndex.LAST_MODIFIED).toString();

                if (!details) { //no details, so we can retrieve everything from Summa
                    final String blob = result.getFirstValue(SBOIEventIndex.PREMIS_NO_DETAILS).toString();
                    hit= premisManipulatorFactory.createFromStringBlob(blob).toItem();
                    hit.setDomsID(uuid);
                } else {//Details requested so go to DOMS
                    hit = domsEventStorage.getItemFromDomsID(uuid);
                }
                hit.setLastModified(parseDate(lastModified));
                hits.add(hit);
            }
            items = hits.iterator();
        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    private Date parseDate(String lastModified) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            return format.parse(lastModified);
        } catch (ParseException e) {
            log.warn("Failed to parse date {}",lastModified,e);
            return null;
        }
    }


    @Override
    public synchronized T next() {

        if (hasNext()) {
            position++;
            return items.next();
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
