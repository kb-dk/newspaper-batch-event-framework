package dk.statsbiblioteket.medieplatform.autonomous;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.linkpatterns.LinkPattern;
import dk.statsbiblioteket.doms.central.connectors.fedora.methods.generated.Method;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.FedoraRelation;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.ObjectProfile;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.SearchResult;
import dk.statsbiblioteket.doms.central.connectors.fedora.templates.ObjectIsWrongTypeException;
import org.w3c.dom.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class AbstractFedoraMockup implements EnhancedFedora {
    public static final String UNEXPECTED_METHOD = "Unexpected Method";
    private List<String> log;


    public AbstractFedoraMockup(List<String> log) {
        this.log = log;
    }

    protected void addToLog(String logMessage) {
        log.add(logMessage);
    }

    @Override
    public String cloneTemplate(String templatepid, List<String> oldIDs, String logMessage) throws
                                                                                            BackendInvalidCredsException,
                                                                                            BackendMethodFailedException,
                                                                                            ObjectIsWrongTypeException,
                                                                                            BackendInvalidResourceException,
                                                                                            PIDGeneratorException {
        addToLog(UNEXPECTED_METHOD);
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ObjectProfile getObjectProfile(String pid, Long asOfTime) throws
                                                                     BackendMethodFailedException,
                                                                     BackendInvalidCredsException,
                                                                     BackendInvalidResourceException {
        addToLog(UNEXPECTED_METHOD);
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void modifyObjectLabel(String pid, String name, String comment) throws
                                                                           BackendInvalidCredsException,
                                                                           BackendMethodFailedException,
                                                                           BackendInvalidResourceException {
        addToLog(UNEXPECTED_METHOD);
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void modifyObjectState(String pid, String stateDeleted, String comment) throws
                                                                                   BackendInvalidCredsException,
                                                                                   BackendMethodFailedException,
                                                                                   BackendInvalidResourceException {
        addToLog(UNEXPECTED_METHOD);
        //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public String getXMLDatastreamContents(String pid, String datastream, Long asOfTime) throws
                                                                                         BackendInvalidCredsException,
                                                                                         BackendMethodFailedException,
                                                                                         BackendInvalidResourceException {
        addToLog(UNEXPECTED_METHOD);
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public List<String> listObjectsWithThisLabel(String label) throws
                                                               BackendInvalidCredsException,
                                                               BackendMethodFailedException {
        addToLog(UNEXPECTED_METHOD);
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addRelation(String pid, String subject, String predicate, String object, boolean literal,
                            String comment) throws
                                            BackendInvalidCredsException,
                                            BackendMethodFailedException,
                                            BackendInvalidResourceException {
        addToLog(UNEXPECTED_METHOD);
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<FedoraRelation> getNamedRelations(String pid, String predicate, Long asOfTime) throws
                                                                                               BackendInvalidCredsException,
                                                                                               BackendMethodFailedException,
                                                                                               BackendInvalidResourceException {
        addToLog(UNEXPECTED_METHOD);
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<FedoraRelation> getInverseRelations(String pid, String predicate) throws
                                                                                  BackendInvalidCredsException,
                                                                                  BackendMethodFailedException,
                                                                                  BackendInvalidResourceException {
        addToLog(UNEXPECTED_METHOD);
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteRelation(String pid, String subject, String predicate, String object, boolean literal,
                               String comment) throws
                                               BackendInvalidCredsException,
                                               BackendMethodFailedException,
                                               BackendInvalidResourceException {
        addToLog(UNEXPECTED_METHOD);
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Document createBundle(String pid, String viewAngle, Long asOfTime) throws
                                                                              BackendInvalidCredsException,
                                                                              BackendMethodFailedException,
                                                                              BackendInvalidResourceException {
        addToLog(UNEXPECTED_METHOD);
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> findObjectFromDCIdentifier(String string) throws
                                                                  BackendInvalidCredsException,
                                                                  BackendMethodFailedException {
        addToLog(UNEXPECTED_METHOD);
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<SearchResult> fieldsearch(String query, int offset, int pageSize) throws
                                                                                  BackendInvalidCredsException,
                                                                                  BackendMethodFailedException {
        addToLog(UNEXPECTED_METHOD);
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void flushTripples() throws BackendInvalidCredsException, BackendMethodFailedException {
        addToLog(UNEXPECTED_METHOD);
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getObjectsInCollection(String collectionPid, String contentModelPid) throws
                                                                                             BackendInvalidCredsException,
                                                                                             BackendMethodFailedException {
        addToLog(UNEXPECTED_METHOD);
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String invokeMethod(String cmpid, String methodName, Map<String, List<String>> parameters,
                               Long asOfTime) throws
                                              BackendInvalidCredsException,
                                              BackendMethodFailedException,
                                              BackendInvalidResourceException {
        addToLog(UNEXPECTED_METHOD);
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Method> getStaticMethods(String cmpid, Long asOfTime) throws
                                                                      BackendInvalidCredsException,
                                                                      BackendMethodFailedException,
                                                                      BackendInvalidResourceException {
        addToLog(UNEXPECTED_METHOD);
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Method> getDynamicMethods(String objpid, Long asOfTime) throws
                                                                        BackendInvalidCredsException,
                                                                        BackendMethodFailedException,
                                                                        BackendInvalidResourceException {
        addToLog(UNEXPECTED_METHOD);
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<LinkPattern> getLinks(String pid, Long asOfTime) throws
                                                                 BackendInvalidCredsException,
                                                                 BackendMethodFailedException,
                                                                 BackendInvalidResourceException {
        addToLog(UNEXPECTED_METHOD);
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String newEmptyObject(List<String> oldIDs, List<String> collections, String logMessage) throws
                                                                                                   BackendInvalidCredsException,
                                                                                                   BackendMethodFailedException,
                                                                                                   PIDGeneratorException {
        addToLog(UNEXPECTED_METHOD);
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public void deleteObject(String s, String s2) throws
                                                  BackendInvalidCredsException,
                                                  BackendMethodFailedException,
                                                  BackendInvalidResourceException {
        addToLog(UNEXPECTED_METHOD);

    }

    @Override
    public Date modifyDatastreamByValue(String s, String s2, String s3, List<String> strings, String s4) throws
                                                                                                         BackendInvalidCredsException,
                                                                                                         BackendMethodFailedException,
                                                                                                         BackendInvalidResourceException {
        addToLog(UNEXPECTED_METHOD);
        return new Date();
    }

    @Override
    public Date modifyDatastreamByValue(String s, String s2, String s3, String s4, List<String> strings,
                                        String s5) throws
                                                   BackendInvalidCredsException,
                                                   BackendMethodFailedException,
                                                   BackendInvalidResourceException {
        addToLog(UNEXPECTED_METHOD);
        return new Date();
    }

    @Override
    public Date modifyDatastreamByValue(String s, String s2, String s3, String s4, String s5, List<String> strings,
                                        String s6) throws
                                                   BackendInvalidCredsException,
                                                   BackendMethodFailedException,
                                                   BackendInvalidResourceException {
        addToLog(UNEXPECTED_METHOD); return new Date();
    }

    @Override
    public void deleteDatastream(String s, String s2, String s3) throws
                                                                 BackendInvalidCredsException,
                                                                 BackendMethodFailedException,
                                                                 BackendInvalidResourceException {
        addToLog(UNEXPECTED_METHOD);
    }

    @Override
    public Date addExternalDatastream(String s, String s2, String s3, String s4, String s5, String s6,
                                      List<String> strings, String s7) throws
                                                                       BackendInvalidCredsException,
                                                                       BackendMethodFailedException,
                                                                       BackendInvalidResourceException {
        addToLog(UNEXPECTED_METHOD);
        return new Date();
    }

    @Override
    public Date addExternalDatastream(String s, String s2, String s3, String s4, String s5, String s6, String s7,
                                      String s8, List<String> strings, String s9) throws
                                                                                  BackendInvalidCredsException,
                                                                                  BackendMethodFailedException,
                                                                                  BackendInvalidResourceException {
        addToLog(UNEXPECTED_METHOD);
        return new Date();
    }

    @Override
    public Date addExternalDatastream(String s, String s2, String s3, String s4, String s5, String s6, String s7,
                                      List<String> strings, String s8) throws
                                                                       BackendInvalidCredsException,
                                                                       BackendMethodFailedException,
                                                                       BackendInvalidResourceException {
        addToLog(UNEXPECTED_METHOD);
        return new Date();
    }
}
