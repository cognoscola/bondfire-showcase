package com.bondfire.app.javaUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alvaregd on 27/07/15.
 * This class allows us to get an index number based on
 * provided Key
 */
public class IndexedHashMap<K,V> extends HashMap<K, V> {

    ArrayList<String> indexer;

    public IndexedHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        init();
    }

    public IndexedHashMap(int initialCapacity) {
        super(initialCapacity);
        init();
    }

    public IndexedHashMap() {
        super();
        init();
    }

    public IndexedHashMap(Map m) {
        super(m);
        init();
    }

    public void init(){
        indexer = new ArrayList<String>();
    }

    @Override
    public Object put(Object key, Object value) {
        indexer.add((String)key);
        return super.put((K)key, (V)value);
    }

    public int getKeyIndex(String key){
        return indexer.indexOf(key);
    }

    @Override
    public void clear() {
        super.clear();
        indexer.clear();
    }
}
