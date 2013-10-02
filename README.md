newspaper-batch-event-framework
===============================

This component consists of the following parts, in order of invocation:

trigger-on-new-batch script
---------------------------
Configured by IT Operations and run from a cron job. This script looks for new
(not previously processed) batches received from Ninestars. Then calls the
CreateBatch java module with the batch ID and round trip number from the
received batch directory structure, as well as the connection-information to
DOMS and a PID (Persistent ID) generator.

CreateBatch Java module
-----------------------
Bridges the script with DomsEventClient, by receiving command line arguments,
and calling methods on DomsEventClient to create a batch object in DOMS with a
Premis event representing the triggering attached.

DomsEventClient Java module
---------------------------
Creates a batch object in DOMS with a Premis event built using the
PremisManipulator.

PremisManipulator Java module
-----------------------------
Manipulates a Premis event, creates a new one if one is not given.
