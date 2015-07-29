package com.feedhenry.sdk.datamanager;

import org.jboss.aerogear.android.core.ReadFilter;
import org.jboss.aerogear.android.store.Store;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public final class FHStore<T> {

    private Store<T> store;

    public FHStore(Store store) {
        this.store = store;
    }

    /**
     * Reads all the data from the underlying storage system.
     *
     * @return List of T
     */
    public Collection<T> readAll() {
        return this.store.readAll();
    }

    /**
     * Reads a specific object/record from the underlying storage system.
     *
     * @param id id from the desired object
     * @return T
     */
    public T read(Serializable id) {
        return this.store.read(id);
    }

    /**
     * Search for objects/records from the underlying storage system.
     *
     * @param filter a filter to use to fetch an object
     * @return a list of elements, should not be null.
     */
    public List<T> readWithFilter(ReadFilter filter) {
        return this.store.readWithFilter(filter);
    }

    /**
     * Saves the given object in the underlying storage system.
     *
     * @param item Object to save
     */
    public void save(T item) {
        this.save(item);
    }

    /**
     * Saves the given objects in the underlying storage system.
     *
     * @param items List of objects to save
     */
    public void save(Collection<T> items) {
        this.store.save(items);
    }

    /**
     * Resets the entire storage system.
     */
    public void reset() {
        this.store.reset();
    }

    /**
     * Removes a specific object/record from the underlying storage system.
     *
     * @param id Id of item to remote
     */
    public void remove(Serializable id) {
        this.store.remove(id);
    }

    /**
     * Checks if the storage system contains no stored elements.
     *
     * @return true if the storage is empty, otherwise false.
     */
    public boolean isEmpty() {
        return this.store.isEmpty();
    }

}
