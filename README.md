newspaper-batch-event-framework
===============================

This component consists of the following parts, in order of invocation:


Batch-trigger
-----------------------
Bridges the script with DomsEventClient, by receiving command line arguments,
and calling methods on DomsEventClient to create a batch object in DOMS with a
Premis event representing the triggering attached.

trigger-on-new-batch script:
Configured by IT Operations and run from a cron job. This script looks for new
(not previously processed) batches received from Ninestars. Then calls the
CreateBatch java module with the batch ID and round trip number from the
received batch directory structure, as well as the connection-information to
DOMS and a PID (Persistent ID) generator.



Doms-event-client
---------------------------
Creates a batch object in DOMS with a Premis event.
Depends on the premis-manipulator

Premis-manipulator
-----------------------------
Manipulates a Premis event, creates a new one if one is not given.

Batch-event-framework-common
-----------------------------
This module have the very most common classes, such as Batch and Event. Everybody will need to depend on this

Tree-processor
----------------------------
This module have the tree processor that allows us to iterate over a batch irrespective of how the batch is stored

Batch-event-client
---------------------------
This module combines the Doms-event-client with the SBOI summa client code to create an implementation where
you can both query for  and add events to batches.

Process-monitor-datasource
---------------------------
This module is a group of three modules concerning themselves with the process monitor datasources.

SBOI-datasource
--------------------------
This module is an implemenation of the datasource interface by using the Batch-event-client to query SBOI for
batches.

Autonomous-component
--------------------------
This module holds the code that allows you to make an autonomous component

Runnable-component
--------------------------
This code holds the runnable component interfaces, that you must implement to make a component for this framework
or the ninestars suite

Sample-autonomous-component
--------------------------
This is the sample autonomous component, that should help you get started writing your own

