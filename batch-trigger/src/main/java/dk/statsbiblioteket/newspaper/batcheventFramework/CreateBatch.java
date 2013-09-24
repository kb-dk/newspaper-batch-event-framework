package dk.statsbiblioteket.newspaper.batcheventFramework;

/**
 * Created with IntelliJ IDEA.
 * User: jrg
 * Date: 9/24/13
 * Time: 2:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateBatch {
    public static void main(String[] args) {
        String domsUrl;
        String domsUser;
        String domsPass;
        String premisAgent;
        String batchId;
        String roundTrip;

        if (args.length != 6) {
            System.out.println("Not the right amount of arguments");
        }

        // For testing purposes, remove
        System.out.println("CreateBatch says:");
        System.out.println("Received:" + args[0]);
        System.out.println("Received:" + args[1]);
        System.out.println("Received:" + args[2]);
        System.out.println("Received:" + args[3]);
        System.out.println("Received:" + args[4]);
        System.out.println("Received:" + args[5]);

        batchId = args[0];
        roundTrip = args[1];
        premisAgent = args[2];
        domsUrl = args[3];
        domsUser = args[4];
        domsPass = args[0];

        // TODO

    }
}

