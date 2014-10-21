package dk.statsbiblioteket.medieplatform.autonomous;

import java.lang.String;

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
    /** The Doms collection object which all new objects will belong to */
    public static final String DOMS_COLLECTION = "doms.collection.pid";

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
    /** Events that a batch must have experienced unsuccessfully to qualify for this component to work on it */
    public static final String AUTONOMOUS_PAST_FAILED_EVENTS = "autonomous.pastFailedEvents";
    /**
     * Events that an item must have experienced, after any other updates to the item
     */
    public static final java.lang.String AUTONOMOUS_UP2DATE_EVENTS = "autonomous.up2DateEvents";
    /**
     * Events that an item must have experienced, but which came before the last update to the item
     */
    public static final java.lang.String AUTONOMOUS_OUTDATED_EVENTS = "autonomous.outdatedEvents";
    /**
     * Events that are either missing from an item, or which are outdated
     */
    public static final java.lang.String AUTONOMOUS_OUTDATED_OR_MISSING_EVENTS = "autonomous.outdatedOrMissingEvents";
    /**
     * Types of Items.
     */
    public static final java.lang.String AUTONOMOUS_ITEM_TYPES = "autonomous.itemTypes";


    /**
     * * Events that a batch must NOT have experienced successfully or otherwise, to qualify for this component to work
     * on it
     */
    public static final String AUTONOMOUS_FUTURE_EVENTS = "autonomous.futureEvents";
    /** The number of batches a component can work on concurrently */
    public static final String AUTONOMOUS_MAXTHREADS = "autonomous.maxThreads";
    /** The number of items a component should lock in it's work queue. Default 1*/
    public static final java.lang.String AUTONOMOUS_QUEUELENGTH = "autonomous.component.workQueueSize";
    /** The max time a component can work on a batch before being forcibly stopped */
    public static final String AUTONOMOUS_MAX_RUNTIME = "autonomous.maxRuntimeForWorkers";
    /**
     * The folder where the batch structure is stored, when the system does not have access to DOMS (because we are at
     * ninestars)
     */
    public static final String AUTONOMOUS_BATCH_STRUCTURE_STORAGE_DIR = "autonomous.batch.structure.folder";


    //-----------HADOOP OPTIONS ------------//
    /** Used to set the batch ID to the hadoop job. Never to be used in a normal config file */
    public static final String BATCH_ID = "batchID";
    /**
     * The folder (in hdfs) where the hadoop job can put its input and output files. It will create batch specific
     * folder names below this path
     */
    public static final String JOB_FOLDER = "job.folder";


    /**
     * Path to the (nfs) folder where the data files can be read. Must end with /. Used as a prefix before all the
     * filenames
     */
    public static final String PREFIX = "file.storage.path";

    /** The username to use when communicating with the hadoop instance */
    public static final String HADOOP_USER = "hadoop.user";

    /**
     * The number of data files to handle in each map task. You will want map tasks whose execution time is measured
     * in minutes, not seconds, otherwise the ratio of overhead to work will be to great. Tweak this value to achieve
     * this.
     */
    public static final String FILES_PER_MAP_TASK = "hadoop.files.per.map.tasks";


    /** This property specifies where the dissemination files should be written to. */
    public static final String DISSEMINATION_FOLDER = "dissemination.files.folder";

    /**
     * The number of threads to concurrently start in components that support parallel execution.
     */
    public static final String THREADS_PER_BATCH = "autonomous.component.threadsPerBatch";

    /**
     * The maximum number of results collected/reported by the component.
     */
    public static final String MAX_RESULTS_COLLECTED = "autonomous.component.maxResults";

    /**
     * The number of times a component should retry operations on fedora, where supported.
     */
    public static final String FEDORA_RETRIES = "autonomous.component.fedoraRetries";

}
