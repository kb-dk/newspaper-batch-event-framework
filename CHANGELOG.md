0.1
Initial release

0.2
Second release

Fixed issues:
	Close the lockclient after use, so that the autonomous components can actually complete
	Set the autonomous polling to sleep for 1000 millis and the "Waiting to Terminate" to be logged on the trace level
	Throw a more useful runtime exception when the tree-processor iterator reaches a folder it cannot read
    Injecting Event handlers added
	Fixed autonomous component utils toEvents method to avoid generating lists with one empty element on empty properties.
	Added default implementation of componentname and version in abstractrunablecomponent.
	Maxthreads set correctly
	Output better XML

	Fedora 3 Tree Processor added
	    Use DOM, not inputsource as this fixes a "bug" (http://stackoverflow.com/questions/1985509/saxon-xpath-api-returns-tinyelementimpl-instead-of-org-w3c-dom-node) in new versions of saxon
        Use name of nodes, not just attributes
        Replace regex with XPath
        Cache checksum and better error message
        Support checksums
        Log ignored subitems
        Read object name in constructor, rather than take it as input
        The filters are no longer content model filters
        Use <dc:identfier>path: as node name instead of uuid



1.0
Updated to released doms 1.0 versions
Consistent error messages for batch event framework
Consistent error messages for batch framework
Fault barrier in event runner
Moved resultCollector to batch event framework common
childs of AbstractRunnableComponent from overriding version and component name
Made the batch framework use the DC identifier, not the object label
Batch Structure storage
Checksum storing for content datastreams
Event Runner uses a result collector to handle exceptions