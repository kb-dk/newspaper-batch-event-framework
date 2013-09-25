package dk.statsbiblioteket.newspaper.batcheventFramework;

import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;
import org.apache.log4j.spi.LoggerFactory;
import org.slf4j.Logger;

import java.util.Date;


/**
 * Created with IntelliJ IDEA.
 * User: jrg
 * Date: 9/24/13
 * Time: 2:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateBatch {
    public static Logger log = org.slf4j.LoggerFactory.getLogger(CreateBatch.class);

    public static void main(String[] args) {
        String domsUrl;
        String domsUser;
        String domsPass;
        String premisAgent;
        String batchId;
        String roundTrip;
        String urlToPidGen;
        DomsEventClientFactory domsEventClientFactory = new DomsEventClientFactory();
        DomsEventClient domsEventClient;
        Date now = new Date();

        log.info("Entered main");

        if (args.length != 7) {
            System.out.println("Not the right amount of arguments");
            System.exit(1);
        }

        batchId = args[0];
        roundTrip = args[1];
        premisAgent = args[2];
        domsUrl = args[3];
        domsUser = args[4];
        domsPass = args[5];
        urlToPidGen=args[6];

        domsEventClientFactory.setFedoraLocation(domsUrl);
        domsEventClientFactory.setUsername(domsUser);
        domsEventClientFactory.setPassword(domsPass);
        domsEventClientFactory.setPidGeneratorLocation(urlToPidGen);

        try {
            domsEventClient = domsEventClientFactory.createDomsEventClient();

            domsEventClient.addEventToBatch(Long.parseLong(batchId), Integer.parseInt(roundTrip), premisAgent, now, "",
                    EventID.Data_Received, true);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
