package dk.statsbiblioteket.newspaper.batcheventFramework;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.central.connectors.fedora.templates.ObjectIsWrongTypeException;

import java.util.ArrayList;
import java.util.List;

public class FedoraMockupEmpty extends AbstractFedoraMockup {


    public FedoraMockupEmpty(List<String> log) {
        super(log);
    }

    @Override
    public String cloneTemplate(String templatepid, List<String> oldIDs, String logMessage) throws BackendInvalidCredsException, BackendMethodFailedException, ObjectIsWrongTypeException, BackendInvalidResourceException, PIDGeneratorException {
        String id = "uuid:" + oldIDs.get(0);
        addToLog("Created object " + id + " from template " + templatepid);
        return id;
    }


    @Override
    public void modifyObjectLabel(String pid, String name, String comment) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        addToLog("Changed the label of object " + pid + " to " + name);
    }


    @Override
    public void modifyDatastreamByValue(String pid, String datastream, String contents, String comment) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        addToLog("ModifiedDatastream in " + pid + "/" + datastream + " to contents '" + contents + "'");
    }


    @Override
    public List<String> listObjectsWithThisLabel(String label) throws BackendInvalidCredsException, BackendMethodFailedException {
        addToLog("Listing objects with label "+label);
        ArrayList<String> result = new ArrayList<String>();
        return result;
    }

    @Override
    public void addRelation(String pid, String subject, String predicate, String object, boolean literal, String comment) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        addToLog("Relation " + predicate + " from " + subject + " to " + object + " added");
    }

    @Override
    public String getXMLDatastreamContents(String pid, String datastream, Long asOfTime) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        addToLog("Attempted to get datastream contents from "+pid+"/"+datastream);
        throw new BackendInvalidResourceException("not found");
    }


}
