package dk.statsbiblioteket.autonomous.batcheventFramework;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;

import java.util.ArrayList;
import java.util.List;

public class FedoraMockupBatchNoRoundTripObject extends FedoraMockupEmpty {


    public FedoraMockupBatchNoRoundTripObject(List<String> log) {
        super(log);
    }


    @Override
    public List<String> listObjectsWithThisLabel(String label) throws BackendInvalidCredsException, BackendMethodFailedException {
        addToLog("Listing objects with label "+label);
        ArrayList<String> result = new ArrayList<String>();
        if (label.contains("-")){
            result.add("uuid:"+label);
        }
        return result;
    }




}
