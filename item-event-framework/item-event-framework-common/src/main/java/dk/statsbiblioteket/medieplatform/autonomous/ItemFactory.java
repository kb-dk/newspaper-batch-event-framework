package dk.statsbiblioteket.medieplatform.autonomous;

/**
 * This is the factory to create Items. Every kind of Item should have an implementation of this interface.
 *
 *
 * @param <T> the subclass of Item that this factory uses
 */
public interface  ItemFactory<T extends Item> {

    /**
     * Create a new item
     * @param id The identifier of the item. This should be unique among all items in DOMS. There is no guarantee
     *            that the item actually exist
     * @return a newly created Item object, or an existing object, if this factory does pooling.
     */
    public T create(String id);
}
