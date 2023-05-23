package de.rptu.programmierpraktikum2023.mp1;

public interface Map<K, V> {
    V get(K key);
    void put(K key, V value);
    void remove(K key);
    int size();
    void keys(K[] array);
}
