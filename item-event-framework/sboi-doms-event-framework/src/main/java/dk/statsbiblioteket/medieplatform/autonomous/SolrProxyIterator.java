package dk.statsbiblioteket.medieplatform.autonomous;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This is the solr proxy iterator. This is the thing that handles paged solr searched, without
 * exposing this behaivour. It implements Iterator and should be treatable as just a normal iterator.
 * When a fixed number of hits have been retrieved from solr, it performs a search to get the
 * next set of hits. It will continue to do so while there are hits in solr.
 *
 * All hits are sorted by item creation time. This way, the sorting is stable, and any changes or additions
 * will always happen in the end of the list. Thus, we can use offset in the list to do paging.
 * @param <T> the type of items
 */
public class SolrProxyIterator<T extends Item> implements Iterator<T> {
    public static final String PREMIS_NO_DETAILS = "premis_no_details";
    public static final String LAST_MODIFIED = "lastmodified_date";
    public static final String SORT_DATE = "initial_date";
    private static Logger log = org.slf4j.LoggerFactory.getLogger(SolrProxyIterator.class);


    protected Iterator<T> items = null;


    protected final String queryString;
    protected final boolean details;
    protected final HttpSolrServer summaSearch;
    protected final PremisManipulatorFactory<T> premisManipulatorFactory;
    protected final DomsEventStorage<T> domsEventStorage;
    protected final int rows;
    protected int start = 0;
    protected int position = 0;


    /**
     * Create a new solr proxy iterator
     * @param queryString the query string for solr
     * @param details should details be fetched from DOMS or Solr. True means that details are fetched from doms. False means use only what is in the sboi index, which lacks certain fields
     * @param summaSearch the http solr server to query
     * @param premisManipulatorFactory the premis factory to parse the premis into items
     * @param domsEventStorage the doms event storage to use, if details is true. Can be null if details are false
     */
    public SolrProxyIterator(String queryString, boolean details, HttpSolrServer summaSearch,
                             PremisManipulatorFactory<T> premisManipulatorFactory,
                             DomsEventStorage<T> domsEventStorage, int pageSize) {
        this.queryString = queryString;
        this.details = details;
        this.summaSearch = summaSearch;
        this.premisManipulatorFactory = premisManipulatorFactory;
        this.domsEventStorage = domsEventStorage;
        rows = pageSize;
        search();
    }

    @Override
    /**
     * If at least one item remains in the cache, return true. Otherwise, do a search in sboi for more hits.
     * If any more hits are found return true and put them in cache. Otherwise return false.
     */
    public synchronized boolean hasNext() {
        if (position >= rows) {
            start += rows;
            position = 0;
            search();
        }
        return items.hasNext();
    }

    /**
     * Perform a search in sboi and replace the field items with the result of this search
     * @see #items
     */
    protected void search() {
        try {
            SolrQuery query = new SolrQuery();
            query.setQuery(queryString);
            query.setRows(rows); //Fetch size. Do not go over 1000 unless you specify fields to fetch which does not include content_text
            query.setStart(start);
            //IMPORTANT!Only use facets if needed.
            query.set("facet", "false"); //very important. Must overwrite to false. Facets are very slow and expensive.
            query.setFields(SBOIEventIndex.UUID, LAST_MODIFIED);
            if (!details) {
                query.addField(PREMIS_NO_DETAILS);
            }

            query.addSort(SORT_DATE, SolrQuery.ORDER.asc);

            QueryResponse response = summaSearch.query(query, SolrRequest.METHOD.POST);
            SolrDocumentList results = response.getResults();
            List<T> hits = new ArrayList<>();
            for (SolrDocument result : results) {
                T hit;
                String uuid = result.getFirstValue(SBOIEventIndex.UUID).toString();
                String lastModified = result.getFirstValue(LAST_MODIFIED).toString();

                if (!details) { //no details, so we can retrieve everything from Summa
                    String blob;
                    if (result.getFirstValue(PREMIS_NO_DETAILS) == null) {
                        hit = premisManipulatorFactory.createInitialPremisBlob(uuid).toItem();
                    } else {
                        blob = result.getFirstValue(PREMIS_NO_DETAILS).toString();
                        hit = premisManipulatorFactory.createFromStringBlob(blob).toItem();
                    }
                } else {//Details requested so go to DOMS
                    try {
                        hit = domsEventStorage.getItemFromDomsID(uuid);
                    } catch (NotFoundException e) {
                        continue;
                    }
                }
                hit.setDomsID(uuid);
                hit.setLastModified(parseDate(lastModified));
                hits.add(hit);
            }
            items = hits.iterator();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse a annoying fedora date
     * @param lastModified the date
     * @return as a date
     */
    private Date parseDate(String lastModified) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        try {
            return format.parse(lastModified);
        } catch (ParseException e) {
            log.warn("Failed to parse date {}", lastModified, e);
            return null;
        }
    }


    /**
     * Get the next hit. If there is no next hit, perform a search for more hits. If no more hits throw
     * NoSuchElementOperation, otherwise return next hit.
     * @return next hit
     * @throws java.util.NoSuchElementException if no more cached hits and no more hits in sboi.
     */
    @Override
    public synchronized T next() {

        if (hasNext()) {
            position++;
            return items.next();
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Unsupported Exception
     * @throws java.lang.UnsupportedOperationException when called
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
