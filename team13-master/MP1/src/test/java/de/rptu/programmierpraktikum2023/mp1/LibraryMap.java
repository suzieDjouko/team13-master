package de.rptu.programmierpraktikum2023.mp1;

public class LibraryMap<K, V> implements Map<K, V> {
    private final java.util.Map<K, V> map;

    public LibraryMap() {
        this.map = new java.util.HashMap<>();
    }

    public LibraryMap(java.util.Map<K, V> map) {
        this.map = new java.util.HashMap<>(map);
    }

    @Override
    public V get(K key) {
        return map.get(key);
    }

    @Override
    public void put(K key, V value) {
        map.put(key, value);
    }

    @Override
    public void remove(K key) {
        map.remove(key);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public void keys(K[] array) {
        int i = 0;
        for (K key : map.keySet()) {
            array[i++] = key;
        }
    }
}
