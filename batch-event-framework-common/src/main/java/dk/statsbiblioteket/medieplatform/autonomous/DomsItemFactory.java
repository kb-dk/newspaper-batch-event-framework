package dk.statsbiblioteket.medieplatform.autonomous;

public class DomsItemFactory implements  ItemFactory<Item>{
    @Override
    public Item createItem(String pid) {
        Item item = new Item();
        item.setDomsID(pid);
        return item;
    }
}
