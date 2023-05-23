package de.rptu.programmierpraktikum2023.mp1;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import static org.junit.jupiter.api.Assertions.*;

public class BibleAnalyzerTest {
    @Test
    public void countWords() {
        Map<String, Integer> map = new LibraryMap<>();
        BibleAnalyzer.countWords(map);
        java.util.Map.of(
                "Gott", 2921,
                "Engel", 266,
                "Jesus", 657,
                "mir", 2460,
                "foobar", 0
        ).forEach((word, count) -> assertEquals(
                count == 0 ? null : count,
                map.get(word), "Falsches Ergebnis für map.get(\"" + word + "\")"
        ));
        assertEquals(22229, map.size(), "Falsches Ergebnis für size()");
    }

    // -----------------------------------------------------------------------------------------------------------------

    private static Stream<java.util.Map<String, Integer>> getRandomWordCounts() {
        return Stream.of(10, 100, 1000, 10000).flatMap(size ->
                Stream.of(4711, 123, 42).map(Random::new).map(rand -> {
                    java.util.Map<String, Integer> map = new HashMap<>();
                    Set<Integer> counts = new HashSet<>();
                    for (int i = 0; i < size; i++) {
                        // Generate a random word as key
                        String key = rand.ints('a', 'z' + 1)
                                .limit(rand.nextInt(20) + 1)
                                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                                .toString();

                        // Generate a random unique int ad value
                        int value;
                        do {
                            value = rand.nextInt(Integer.MAX_VALUE) + 1;
                        } while (!counts.add(value));
                        map.put(key, value);
                    }
                    return map;
                })
        );
    }

    @ParameterizedTest
    @MethodSource("getRandomWordCounts")
    public void sort(java.util.Map<String, Integer> counts) {
        System.out.println(counts);
        Map<String, Integer> map = new LibraryMap<>(counts);

        // Fill two arrays with the words
        String[] expected = new String[map.size()];
        String[] actual = new String[map.size()];
        map.keys(expected);
        map.keys(actual);

        // Sort with Java library
        Arrays.sort(expected, Comparator.comparing(map::get));

        // Sort with user implementation
        BibleAnalyzer.sort(actual, map);

        // Check
        assertArrayEquals(actual, expected);
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Test
    public void sortWithDuplicates() {
        Map<String, Integer> counts = new LibraryMap<>(java.util.Map.of(
                "abc", 1,
                "ab", 7,
                "z", 7,
                "xyz", 5,
                "ghij", 2,
                "asdf", 19
        ));

        // Fill array with the words
        String[] words = new String[counts.size()];
        counts.keys(words);

        // Sort with user implementation
        BibleAnalyzer.sort(words, counts);

        // Check
        assertEquals("abc", words[0]);
        assertEquals("ghij", words[1]);
        assertEquals("xyz", words[2]);
        assertTrue("ab".equals(words[3]) && "z".equals(words[4]) || "z".equals(words[3]) && "ab".equals(words[4]));
        assertEquals("asdf", words[5]);
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Test
    public void main() {
        PrintStream defaultOutputStream = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try {
            BibleAnalyzer.main(new String[0]);
            final String stdout = baos.toString();

            assertTrue(stdout.contains("739 Du"));
            assertTrue(stdout.contains("928 sollst"));
            assertTrue(stdout.contains("7542 nicht"));
            assertTrue(stdout.contains("14 Klauen"));
            assertTrue(stdout.contains("377 deines"));
            //assertTrue(stdout.contains("112 Nächsten")); Umlaut ggf. problematisch
            assertFalse(stdout.contains("Implementierung"));
        } finally {
            System.setOut(defaultOutputStream);
        }
    }
}
