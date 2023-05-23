package de.rptu.programmierpraktikum2023.mp1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Util {
    public static Iterable<String> getBibleWords() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("./src/main/resources/Martin_Luther_Uebersetzung_1912.txt", StandardCharsets.UTF_8));
            Pattern wordPattern = Pattern.compile("[\\p{L}0-9]+");
            return reader.lines().flatMap(line -> {
                Matcher matcher = wordPattern.matcher(line.split(" ", 3)[2]);
                Stream.Builder<String> sb = Stream.builder();
                while (matcher.find()) {
                    sb.add(matcher.group());
                }
                return sb.build();
            })::iterator;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
