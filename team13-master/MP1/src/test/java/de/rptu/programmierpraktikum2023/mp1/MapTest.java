package de.rptu.programmierpraktikum2023.mp1;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import static org.junit.jupiter.api.Assertions.*;

public class MapTest {
    private static final String packageName = Map.class.getPackageName();

    @SuppressWarnings("unchecked")
    private static Stream<Map<String, Integer>> getMapInstances() {
        // Use Reflection in order to compile Tests without the Implementation
        boolean hasImplementation = false;
        Stream.Builder<Map<String, Integer>> sb = Stream.builder();
        for (String className : new String[]{"ListMap", "ArrayMap"}) {
            try {
                sb.add((Map<String, Integer>) Class.forName(packageName + "." + className).getConstructor().newInstance());
                hasImplementation = true;
            } catch (ClassNotFoundException e) {
                System.err.println(">>> Sie haben die Klasse " + className + " noch nicht implementiert!");
            } catch (Exception e) {
                System.err.println(">>> Fehler beim Instanziieren der Klasse " + className + ":");
                e.printStackTrace();
            }
        }
        try {
            sb.add((Map<String, Integer>) Class.forName(packageName + ".TreeMap")
                    .getConstructor(Comparator.class)
                    .newInstance(Comparator.<String>naturalOrder())
            );
            hasImplementation = true;
        } catch (ClassNotFoundException e) {
            System.err.println(">>> Sie haben die Klasse TreeMap noch nicht implementiert!");
        } catch (Exception e) {
            System.err.println(">>> Fehler beim Instanziieren der Klasse TreeMap:");
            e.printStackTrace();
        }
        if (!hasImplementation) {
            sb.add(null);
        }
        return sb.build();
    }

    // -----------------------------------------------------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("getMapInstances")
    public void mapBeispiele(Map<String, Integer> map) {
        if (map == null) {
            throw new RuntimeException("Keine Map Implementierung gefunden! Die Klassen müssen im Paket " + packageName + " abgelegt werden.");
        }
        System.out.println("Teste mapBeispiele mit " + map.getClass().getSimpleName());
        // get, put, size
        assertNull(map.get(null));
        assertEquals(0, map.size());
        map.put("a", 1);
        map.put("b", 2);
        assertNull(map.get("someKey"), "Zu diesem Schlüssel ist kein Wert gespeichert, es soll null zurückgegeben werden");
        assertEquals(1, map.get("a"));
        assertEquals(2, map.get("b"));
        map.put("b", 3);
        assertEquals(3, map.get("b"), "put überschreibt Wert von existierendem Schlüssel nicht");
        assertEquals(2, map.size());

        // keys
        assertThrows(IllegalArgumentException.class, () -> map.keys(null));
        assertThrows(IllegalArgumentException.class, () -> map.keys(new String[1]));
        String[] keys = new String[2];
        map.keys(keys);
        assertTrue(Arrays.asList(keys).contains("a"));
        assertTrue(Arrays.asList(keys).contains("b"));
        assertFalse(Arrays.asList(keys).contains("someKey"));
        String[] keysTooLarge = new String[3];
        keysTooLarge[2] = "unverändert";
        map.keys(keysTooLarge);
        assertEquals("unverändert", keysTooLarge[2], "Arrayeinträge an Positionen >= map.size() sollen unverändert bleiben");

        // remove (und Auswirkungen auf get, size)
        try {
            map.remove("someKey");
            assertEquals(1, map.get("a"), "Löschen eines nicht vorhandenen Schlüssels soll Map nicht verändern!");
            assertEquals(3, map.get("b"), "Löschen eines nicht vorhandenen Schlüssels soll Map nicht verändern!");
            map.remove("b");
            assertNull(map.get("b"));
            assertEquals(1, map.size());
            assertEquals(1, map.get("a"), "Eintrag zum Schlüssel a soll nicht verändert werden, wenn b gelöscht wird");
            map.remove("a");
            assertNull(map.get("a"));
            assertEquals(0, map.size());
        } catch (UnsupportedOperationException e) {
            if (map.getClass().getSimpleName().equals("TreeMap")) {
                System.out.println("\tÜberspringe Test für optionalen Teil von Aufgabe 3 (TreeMap: Methode remove(key) wirft UnsupportedOperationException).");
            } else {
                throw e;
            }
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    private static Stream<Arguments> getMapInstancesWithNumOpsAndSeed() {
        return Stream.of(10, 100, 1000).flatMap(numOps ->
                Stream.of(4711, 123, 42).flatMap(seed ->
                        getMapInstances().map(map -> Arguments.of(map, numOps, seed))
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getMapInstancesWithNumOpsAndSeed")
    public void mapZufall(Map<String, Integer> testMap, int numOps, long seed) {
        if (testMap == null) {
            throw new RuntimeException("Keine Map Implementierung gefunden! Die Klassen müssen im Paket " + packageName + " abgelegt werden.");
        }
        System.out.println(String.format("Teste mapZufall mit %s (%s Operationen, Seed %s)", testMap.getClass().getSimpleName(), numOps, seed));
        Map<String, Integer> libraryMap = new LibraryMap<>();
        boolean isTreeMap = testMap.getClass().getSimpleName().equals("TreeMap");

        // Deterministic with seed
        Random rand = new Random(seed);

        // Supplier for random alphabetic strings of length 1 to 20
        Supplier<String> randomKeySupplier = () ->
                rand.ints('a', 'z' + 1)
                        .limit(rand.nextInt(20) + 1)
                        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                        .toString();

        // Store existing keys for later access
        Set<String> existingKeys = new LinkedHashSet<>();
        Supplier<String> existingKeySupplier = () -> existingKeys.stream().skip(rand.nextInt(existingKeys.size())).findFirst().orElseThrow();

        // Execute random operations
        for (int i = 0; i < numOps; i++) {
            int op = rand.nextInt(3);
            if (op == 0) { // GET
                String key = existingKeys.size() > 0 && rand.nextInt(10) > 0 // 9:1 ratio (higher chance to get an existing key)
                        ? existingKeySupplier.get() // take a random existing key
                        : randomKeySupplier.get(); // take a completely random key
                System.out.println("\tOperation auf Map: get(\"" + key + "\")");
                assertEquals(libraryMap.get(key), testMap.get(key));
            } else if (op == 1) { // PUT
                String key;
                if (existingKeys.size() > 0 && rand.nextInt(2) > 0) { // 1:1 ratio of new and overwritten keys
                    key = existingKeySupplier.get();
                } else {
                    key = randomKeySupplier.get();
                    existingKeys.add(key);
                }
                int value = rand.nextInt();
                System.out.println("\tOperation auf Map: put(\"" + key + "\", " + value + ")");
                libraryMap.put(key, value);
                testMap.put(key, value);
            } else { // REMOVE
                String key = existingKeys.size() > 0 && rand.nextInt(2) > 0 // 1:1 ratio
                        ? existingKeySupplier.get() // take a random existing key
                        : randomKeySupplier.get(); // take a completely random key
                System.out.println("\tOperation auf Map: remove(\"" + key + "\")");
                try {
                    testMap.remove(key);

                    // only if testMap does not throw
                    libraryMap.remove(key);
                    existingKeys.remove(key);
                } catch (UnsupportedOperationException e) {
                    if (isTreeMap) {
                        System.out.println("\t\tÜberspringe Test für optionalen Teil von Aufgabe 3 (TreeMap: Methode remove(key) wirft UnsupportedOperationException).");
                    } else {
                        throw e;
                    }
                }
            }

            // Check the size after each operation
            assertEquals(libraryMap.size(), testMap.size(), "Falsches Ergebnis für size()");
        }

        // After all operations, check keys:
        String[] libraryKeys = new String[libraryMap.size()];
        libraryMap.keys(libraryKeys);
        String[] testKeys = new String[testMap.size()];
        testMap.keys(testKeys);
        assertEquals(Set.of(libraryKeys), Set.of(testKeys), "Falsches Ergebnis für keys()");
    }
}
