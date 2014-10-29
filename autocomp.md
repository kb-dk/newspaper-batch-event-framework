Autonomous Components
=====================

The Autonomous components can be though of as little robots, working on an assembly line. They all know their place at the assembly line, relative to the other robots. That is to say, each robot knows which events must have transpired before it needs to do some work. Please note, the robots are virtual, so several robots can occupy the same spot on the assembly line, and several robots can work on the same items simultaneously.

Most of their life is spent sleeping. Periodically, they open their eyes, to check if a new piece of work have arrived for them. This is what we call "The Polling Step". They implement an Event Trigger, to find all items, which are ready to be worked on corresponding to this robot's place. 

If the robot finds that an item is ready to work on, it starts working on this item. The robot will not poll for more work while working. Some of the robots will be able to multitask, ie. work on multiple items at once, and others will not. Some robots will request a number of items to work on, even if it cannot work on them all at the same time. This behaviour is just a way to ensure that the robot have sufficient work to last it until the next polling step.

When a robot has finished work on an item, it must record this, so the assembly line can move forward. It records the event in the event storer for the item, in the form of an event somewhat like "I, <ROBOT>, did this <THIS WORK> on item <ID> with <THIS RESULT>".

For a typical implementation, the Event Trigger (in the polling step) will use our Summa Batch Object Index, SBOI, to find all items, which have experienced a set of events. It will store events in our Digital Object Management System, DOMS. The SBOI will then periodically (often) query DOMS for updates to items with events. When the SBOI discovers an item object update, it updates the index, so that robots further along the assembly line can work on the item.

Advanced Triggering
-------------------------------
To poll for work, a robot queries the SBOI. This query consists of a number of fields

 * The first part of the query is the Items, ie. the set of items which constrain the result set
 * The next part is the success events. Items must have these events with outcome success
 * The next part is the future events. Items must not have these events in with any outcome.
 * The next part is the old events. Items must either not have these events or must have received a change since this event was registered
 * The next part is the item types. These are the content models that the items must have. This is not about the events at all, but about the types of items that can be returned.
 
Autonomous Component Life Cycle
-------------------------------

 * Aquire SBOI lock for this component (or die)
 * Find triggered items
 * For each item (up to max number of items)
   * Aquire item lock for this component (or ignore this item)
   * Add item to work on to list
 * Concurrently (at most configured number simultaneously)
   * run the runnable for items in list
   * store result in Event Store
 * Wait for items to finish working (or for timeout)
 * Unlock all items for this component
 * Unlock SBOI for this component

Artifacts
---------

* Runnable (what the component should do when worked on)
* Lockserver
* EventStorer
* EventTrigger

Lockserver
----------

We use Zookeeper, and have named locks for
 * Each autonomous component, locked while polling for events and running
 (we could include the machinename here, if we would allow robots to live on several machines)
 * Each item for a given autonomous component, while running

Event triggers
--------------

The default event trigger is the SBOI Event trigger

  * We index events for items in Summa from DOMS (the event store). We harvest objects of ContentModel_Item
  * We make a search in Summa triggering on the following:
  * List all item objects, where 
    * the type of the item is of a specific kind
    * last modified is later than last event registered for this component (if any)
    * a set of specific events have been registered on this item and were succesful
    * a set of specific events have been registered on this item and were unsuccesful
    * a set of specific events have NOT been registered on this item
    
Note, lastModified is the timestamp of the latest audit entry in the AUDIT datastream, which does NOT concern the EVENTS
datastream. This is to guard against a deadlock between to components which both update the component indefinately.

Event storer
------------
We store events in DOMS using the Premis metadata format. The events will be stored in objects with ContentModel_Item in the datastream EVENTS.
DOMS can write changes to the EVENTS datastream, without the usual restriction that you cannot update published objects.
