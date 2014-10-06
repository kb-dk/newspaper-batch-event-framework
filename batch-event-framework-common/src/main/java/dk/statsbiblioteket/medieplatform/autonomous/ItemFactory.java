package dk.statsbiblioteket.medieplatform.autonomous;

public interface  ItemFactory<T extends Item> {

    public T createItem(String id);
}
