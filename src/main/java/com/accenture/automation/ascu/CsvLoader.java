package com.accenture.automation.ascu;

import org.apache.commons.csv.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CsvLoader {

    public static void createCsvIfMissing(String csvFile) throws IOException {

        Path path = Paths.get(csvFile);
        if (!Files.exists(path)) {
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                writer.write("Old Steps,New Steps,Xpath\n");
                writer.write("<<OLD STEP HERE>>,<<NEW STEP HERE>>,<<XPATH HERE>>\n");
            }
            System.out.println("[INFO] Sample CSV created.");
        }
    }

    public static List<Replacement> loadReplacements(String csvFile) throws IOException {

        List<Replacement> list = new ArrayList<>();

        try (Reader reader = Files.newBufferedReader(Paths.get(csvFile))) {

            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .build();

            try (CSVParser parser = format.parse(reader)) {
                for (CSVRecord record : parser) {

                    Map<String, String> cleaned = new HashMap<>();
                    for (Map.Entry<String, String> e : record.toMap().entrySet()) {
                        cleaned.put(
                                e.getKey().replace("\uFEFF", "").trim(),
                                e.getValue()
                        );
                    }

                    if (!cleaned.containsKey("Old Steps")
                            || !cleaned.containsKey("New Steps")
                            || !cleaned.containsKey("Xpath")) {
                        throw new RuntimeException(
                                "CSV header must contain: Old Steps, New Steps, Xpath");
                    }

                    if ((cleaned.get("New Steps") == null
                            || cleaned.get("New Steps").trim().isEmpty())
                            && (cleaned.get("Xpath") == null
                            || cleaned.get("Xpath").trim().isEmpty())) {
                        throw new RuntimeException(
                                "Each CSV row must have either New Steps or Xpath");
                    }

                    list.add(new Replacement(
                            cleaned.get("Old Steps"),
                            cleaned.get("New Steps"),
                            cleaned.get("Xpath")
                    ));
                }
            }
        }
        return list;
    }
}