package dk.statsbiblioteket.medieplatform.autonomous;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;

import java.util.ArrayList;
import java.util.List;

public class FedoraMockupBatchNoRoundTripObject extends FedoraMockupEmpty {


    public FedoraMockupBatchNoRoundTripObject(List<String> log) {
        super(log);
    }

    @Override
    public List<String> findObjectFromDCIdentifier(String identifier) throws
                                                                      BackendInvalidCredsException,
                                                                      BackendMethodFailedException {
        addToLog("Listing objects with label " + identifier);
        ArrayList<String> result = new ArrayList<String>();
        if (identifier.contains("-")) {
            result.add("uuid:" + identifier.replaceFirst("^path:", ""));
        }
        return result;
    }


}
