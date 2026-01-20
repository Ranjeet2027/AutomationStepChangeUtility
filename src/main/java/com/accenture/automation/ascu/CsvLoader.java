package com.accenture.automation.ascu;

import org.apache.commons.csv.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CsvLoader {

    public static void createCsvIfMissing(String csvFile) throws IOException {

        Path path = Paths.get(csvFile);

        System.out.println("[CSV] Checking If CSV File Exists: " + csvFile);

        if (!Files.exists(path)) {

            System.out.println("[WARN] CSV File Not Found. Creating A Sample CSV At: " + csvFile);

            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                writer.write("Old Steps,New Steps,Xpath\n");
                writer.write("<<CURRENT STEP HERE>>,<<NEW STEP HERE>>,<<RELATIVE XPATH HERE>>\n");
            }
            System.out.println("Sample CSV Created Successfully");
        } else {
            System.out.println("CSV File Found. Using Existing File");
        }
    }

    public static List<Replacement> loadReplacements(String csvFile) throws IOException {

        System.out.println("Loading Replacements From: " + csvFile);
        List<Replacement> list = new ArrayList<>();

        try (Reader reader = Files.newBufferedReader(Paths.get(csvFile))) {

            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .build();

            try (CSVParser parser = format.parse(reader)) {

                //int rowNumber = 1;

                for (CSVRecord record : parser) {

                    //System.out.println("\nProcessing row #" + rowNumber);
                    Map<String, String> cleaned = new HashMap<>();
                    for (Map.Entry<String, String> e : record.toMap().entrySet()) {
                        cleaned.put(
                                e.getKey().replace("\uFEFF", "").trim(),
                                e.getValue()
                        );
                    }

                    if (!cleaned.containsKey("Current Steps")
                            || !cleaned.containsKey("New Steps")
                            || !cleaned.containsKey("Relative Xpath")) {
                        System.out.println("[CSV][ERROR] Invalid Header Detected In CSV File");
                        throw new RuntimeException(
                                "CSV Header Must Contain: Current Steps, New Steps, Relative Xpath");
                    }

                    String currentStep = cleaned.get("Current Steps");
                    String newStep = cleaned.get("New Steps");
                    String relativeXpath   = cleaned.get("Relative Xpath");

                    if ((newStep == null || newStep.trim().isEmpty())
                            && (relativeXpath == null || relativeXpath.trim().isEmpty())) {

                        System.out.println("[CSV][ERROR] Invalid Row Detected In CSV File");
                        throw new RuntimeException(
                                "CSV Row Must Have Either New Steps or Relative Xpath");
                    }

                    list.add(new Replacement(currentStep, newStep, relativeXpath));
                }
            }
        }
        System.out.println("\nTotal Replacements Loaded: " + list.size());
        return list;    
    }
}