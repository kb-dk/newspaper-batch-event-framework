newspaper-batch-event-framework
===============================

This component consists of the following parts

Batch-event-framework-common
-----------------------------
This module have the very most common classes, such as Batch and Event, and interfaces such as RunnableComponent,
EventTrigger, EventExplorer and EventStorer.

Everybody will need to depend on this.

SBOI-Doms-event-framework
---------------------------
Implement the framework interfaces using DOMS and SBOI

Tree-processor
----------------------------
This module have the tree processor that allows us to iterate over a batch irrespective of how the batch is stored

Tree-processor-runnable-component
----------------------------
A component containing a Runnable Component that provides easy access to a tree processor

Process-monitor-datasource
---------------------------
This module is a group of three modules concerning themselves with the process monitor datasources.

SBOI-datasource
--------------------------
This module is an implementation of the datasource interface by using the event explorer to query SBOI and DOMS for
batches.

Autonomous-component
--------------------------
This module holds the code that allows you to make an autonomous component

SBOI-Doms-autonomous-component
----------------------------
A component containing utilities for easily creating autonomous components with SBOI/Doms event framework.

Sample-autonomous-component
--------------------------
This is the sample autonomous component, that should help you get started writing your own

Hadoop-helpers
--------------------------
Helper code for writing hadoop jobs and integrating with this framework.

Hadoop-component
--------------------------
A runnable component superclass for hadoop jobs.

Batch-iterator
--------------------------
Helper to iterate over batches with types