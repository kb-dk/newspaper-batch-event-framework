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
        System.out.println("CreateBatch says:");
        if (args.length != 6) {
			System.out.println("Not the right amount of arguments");
		}

        System.out.println("Received:" + args[0]);
        System.out.println("Received:" + args[1]);
        System.out.println("Received:" + args[2]);
        System.out.println("Received:" + args[3]);
        System.out.println("Received:" + args[4]);
        System.out.println("Received:" + args[5]);
    }
}

