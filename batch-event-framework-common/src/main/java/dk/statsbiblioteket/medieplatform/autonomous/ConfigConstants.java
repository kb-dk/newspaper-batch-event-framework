package dk.statsbiblioteket.medieplatform.autonomous;

public class ConfigConstants {

    //Doms
    /** The username for the doms fedora instance */
    public static final String DOMS_USERNAME = "doms.username";
    /** The password for the doms fedora instance */
    public static final String DOMS_PASSWORD = "doms.password";
    /** The url to the doms fedora instance */
    public static final String DOMS_URL = "doms.url";
    /** The url to the pidgenerator webservice. Only needed for components that creates new doms objects */
    public static final String DOMS_PIDGENERATOR_URL = "doms.pidgenerator.url";

    //MFPak
    /** The mfpak postgres connect url, not including username and password */
    public static final String MFPAK_URL = "mfpak.postgres.url";
    /** The mfpak postgres username */
    public static final String MFPAK_USER = "mfpak.postgres.user";

    /** The mfpak postgres passworkd */
    public static final String MFPAK_PASSWORD = "mfpak.postgres.password";

    /**
     * A boolean value representing if we are at ninestars (and thus do not have access to hadoop or doms or anything)
     * or we are inhouse
     */
    public static final String AT_NINESTARS = "ninestars";
    /** The path to the jpylyzer executable */
    public static final String JPYLYZER_PATH = "ninestars.jpylyzer.executable";

    /** The path to the scape control policies RDF document */
    public static final String SCAPE_CONTROL_POLICIES_PATH = "scape.control.policies.path";


    //Batch iterator
    /** Boolean value determining if we will iterate on the batch on the filesystem or in the doms. */
    public static final String ITERATOR_USE_FILESYSTEM = "iterator.useFileSystem";
    /**
     * When iterating the batch on the filesystem, this is the root folder where the batch should be found
     * i.e. the parent of the B4000..-RT1 folder
     */
    public static final String ITERATOR_FILESYSTEM_BATCHES_FOLDER = "iterator.filesystem.batches.folder";
    /** The pattern used to determine if the current file/folder represents a datafile */
    public static final String ITERATOR_DATAFILEPATTERN = "iterator.datafilePattern";
    /** The character that separates the prefix and the postfix in a filename. Normally '.' */
    public static final String ITERATOR_FILESYSTEM_GROUPINGCHAR = "iterator.filesystem.groupingChar";

    /** The postfix to add to a file to get the checksum file */
    public static final String ITERATOR_FILESYSTEM_CHECKSUMPOSTFIX = "iterator.filesystem.checksumPostfix";

    /** The names of datastreams that should be made into attributes in the iterator */
    public static final String ITERATOR_DOMS_ATTRIBUTENAMES = "iterator.doms.attributenames";

    /** The names of relations that denote a children in the iterator */
    public static final String ITERATOR_DOMS_PREDICATENAMES = "iterator.doms.predicatenames";

    //Autonomous component framework
    /** The url for the lockserver for the autonomous system */
    public static final String AUTONOMOUS_LOCKSERVER_URL = "autonomous.lockserver.url";
    /** The url for the sboi summa instance */
    public static final String AUTONOMOUS_SBOI_URL = "autonomous.sboi.url";
    /** Events that a batch must have experienced successfully to qualify for this component to work on it */
    public static final String AUTONOMOUS_PAST_SUCCESSFUL_EVENTS = "autonomous.pastSuccessfulEvents";
    /** * Events that a batch must have experienced unsuccessfully to qualify for this component to work on it */
    public static final String AUTONOMOUS_PAST_FAILED_EVENTS = "autonomous.pastFailedEvents";
    /**
     * * Events that a batch must NOT have experienced successfully or otherwise, to qualify for this component to work
     * on it
     */
    public static final String AUTONOMOUS_FUTURE_EVENTS = "autonomous.futureEvents";
    /** The number of batches a component can work on concurrently */
    public static final String AUTONOMOUS_MAXTHREADS = "autonomous.maxThreads";
    /** The max time a component can work on a batch before being forcibly stopped */
    public static final String AUTONOMOUS_MAX_RUNTIME = "autonomous.maxRuntimeForWorkers";

    /**
     * The folder where the batch structure is stored, when the system does not have access to DOMS (because we are at
     * ninestars)
     */
    public static final String AUTONOMOUS_BATCH_STRUCTURE_STORAGE_DIR = "autonomous.batch.structure.folder";


}
