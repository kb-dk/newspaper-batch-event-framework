2.10
* Return an empty premis when an Item can be found but no Events datastream is present
* Update to version 1.17 of doms centralWebservice-lib

2.9
* Use newest doms libraries and sbutil. This removes the last remnants of SPARQL i iTQL.
* Exclude some duplicate classes

2.8
* Use only newest event when comparing events for triggering

2.7
* Add method in EventStorer to prepend events to premis

2.6
* Fix naming typo 'statsbibliokeket' to statsbiblioteket

2.5
* Fixed a nullpointer error in SBOIDatasource

2.4
* Removed commons-logging and log4j from the autonoumous components
* If finding an item in sboi which is not in doms, ignore it
* Find the latest roundtrip if not specified

2.3
* Added remove event functionality

2.2
* SBOI queries use POST not GET to avoid 414 on long queries due to many potential batches from MFPAK

2.1
* Retry on Fedora 409 errors
* Some cleanup in logging

2.0
* Make list of ignored files in file iterator configurable
* SBOI is now based on Solr
* SBOI is reworked and generalised to work on broader Items than just Batch Roundtrip objects
* Support for rerunning events, if objects have changed
* Support for working on a list of items in one run
* Configurable pageSize when requesting items to work on
* Configuration has been extended and changed and example config has been updated. Please update your configuration files.

1.11
* Add autonomous.component.fedoraRetries configuration option to ConfigConstants

1.10
* Add AbstractDomsReducer class for reduce operations that interact with with doms. 
* Updated to version 1.5 of doms central 
  * This changes identifier lookups from using 'like' to 'equals'. 
  * Calls to modifyDatastream and thus AddEvent* methods now return date to mark the server timestamp of the operation
* Make the event runner multithreaded. This caused changes to how InjectingTreeEventHandlers work
* Premis Events now have a correctly formattet date
* Introduced the event "Roundtrip_Approved"
* Add method to get all roundtrips from SBOI

1.9
* Record the duration as part of the result
* Fix handling of queries for batches with roundtrip 0 (from mfpak)

1.8
* Enable support for building with Java 8
* Fix PremisManipulator not being thread safe (which caused failures of registering events in DOMS after)

1.7
* Support alternative triggers to the SBOI/DOMS trigger, and make the SBOI-triggers support a limiting list of batches.

1.6
* Use newest sbutil 0.5.17
* Use the 1.2 central lib (enhanced fedora)
* Use the 1.2 newspaper parent
* Events datastream is mime text/xml. Tests updated to match. Fixed the annoying versioning test
* Remove the failsafe plugins from the modules
* Major restructuring of runnable components: Introducing interfaces for triggering events, storing results of events and exploring events.

1.5
* Updated to newspaper-parent 1.1, as part of new testability strategy
* Many updates for better and more maintainable tests
* New TreeNode iterator
* Support directories with both directory metadata and virtual metadata groups without files (for missing pages in newspaper project)

1.4.5
* Add default constructor to node begin event with a null location

1.4.4
* Add possibility for limiting the number over failures reported
* Add location to node begin event

1.4.3
* Add fault berrier in BatchWorker with logging
* Add various logging to framework

1.4.2
* Stop framework poluting stderr and stdout

1.4.1
* Fixed the comma separated list stuff when quering SBOI with details

1.4
* The SBOI datasource have been made less resource consuming. This seems to speed up the process monitor x3.
* The SBOI integration now respects the details=false flag. If details=true, the batch is retrieved from DOMS, as this is faster and memory lighter
* Changed the autonomous component locking strategy.1. SBOI locks are kept for the entire duration of the component execution. So two instances of the same component cannot run concurrently. 2. When requesting batches from SBOI, retrieve the list of events for each batch from DOMS, to guard against any race conditions.
* Added the AbstractHadoopComponent to make it easier to make autonomous hadoop components.
* Added various hadoop helper functions
* isPreservable added to the resultCollector to allow the not-event-setting components to work

1.3
* Autonomous components now will not stop until the SBOI instance have been reindexed and the batches they worked on are no longer eligible.
* updated to version 1.0.2 of doms client
* Added a few new config constants that the other components need
* Added functionality to premis-manipulator and doms-event-client to enable restart of workflow by manipulating the event list for a batch.

1.2
* Make iterator correctly traverse the DOMS hierarchy structure
* Remove hardcoded timeout
* Code beautifications

1.1
* Unify property names
* Update doms dependencies

1.0
* Updated to released doms 1.0 versions
* Consistent error messages for batch event framework
* Consistent error messages for batch framework
* Fault barrier in event runner
* Moved resultCollector to batch event framework common children of AbstractRunnableComponent from overriding version and component name
* Made the batch framework use the DC identifier, not the object label
* Batch Structure storage
* Checksum storing for content datastreams
* Event Runner uses a result collector to handle exceptions

0.2
* Second release

* Fixed issues:
 * Close the lockclient after use, so that the autonomous components can actually complete
 * Set the autonomous polling to sleep for 1000 millis and the "Waiting to Terminate" to be logged on the trace level
 * Throw a more useful runtime exception when the tree-processor iterator reaches a folder it cannot read
 * Injecting Event handlers added
 * Fixed autonomous component utils toEvents method to avoid generating lists with one empty element on empty properties.
 * Added default implementation of componentname and version in abstractrunablecomponent.
 * Maxthreads set correctly
 * Output better XML

* Fedora 3 Tree Processor added
 * Use DOM, not inputsource as this fixes a "bug" (http://stackoverflow.com/questions/1985509/saxon-xpath-api-returns-tinyelementimpl-instead-of-org-w3c-dom-node) in new versions of saxon
 * Use name of nodes, not just attributes
 * Replace regex with XPath
 * Cache checksum and better error message
 * Support checksums
 * Log ignored subitems
 * Read object name in constructor, rather than take it as input
 * The filters are no longer content model filters
 * Use <dc:identfier>path: as node name instead of uuid

0.1
* Initial release

