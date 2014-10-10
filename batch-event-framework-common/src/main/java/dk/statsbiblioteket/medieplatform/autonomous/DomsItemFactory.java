package dk.statsbiblioteket.medieplatform.autonomous;

/**
 * This is the itemfactory for generic items, whose fullID is the doms pid. It can create an item from any doms pid.
 */
public class DomsItemFactory implements ItemFactory<Item> {
    @Override
    public Item createItem(String pid) {
        Item item = new Item();
        item.setDomsID(pid);
        return item;
    }
}
