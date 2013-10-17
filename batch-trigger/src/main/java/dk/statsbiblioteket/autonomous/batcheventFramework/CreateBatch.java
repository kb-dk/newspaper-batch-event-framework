package dk.statsbiblioteket.autonomous.batcheventFramework;

import org.slf4j.Logger;

import java.util.Date;


/**
 * Called from shell script with arguments to create a batch object in DOMS with proper Premis event added.
 *
 * @author jrg
 */
public class CreateBatch {
    public static Logger log = org.slf4j.LoggerFactory.getLogger(CreateBatch.class);

    /**
     * Receives the following arguments to create a batch object in DOMS:
     * Batch ID, roundtrip number, Premis agent name, URL to DOMS/Fedora, DOMS username, DOMS password,
     * URL to PID generator.
     *
     * @param args The command line arguments received from calling shell script. Explained above.
     */
    public static void main(String[] args) {
        String batchId;
        String roundTrip;
        String premisAgent;
        String domsUrl;
        String domsUser;
        String domsPass;
        String urlToPidGen;
        DomsEventClientFactory domsEventClientFactory = new DomsEventClientFactory();
        DomsEventClient domsEventClient;
        Date now = new Date();

        log.info("Entered main");

        if (args.length != 7) {
            System.out.println("Not the right amount of arguments");
            System.out.println("Receives the following arguments (in this order) to create a batch object in DOMS:");
            System.out.println("Batch ID, roundtrip number, Premis agent name, URL to DOMS/Fedora, DOMS username, DOMS password,");
            System.out.println("URL to PID generator.");
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
                    "Data_Received", true);
        } catch (Exception e) {
            System.err.println("Failed adding event to batch, due to: " + e.getMessage());
            log.error("Caught exception: ", e);
            System.exit(1);
        }
    }
}
