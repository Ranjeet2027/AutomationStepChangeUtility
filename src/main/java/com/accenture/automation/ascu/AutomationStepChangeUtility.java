package com.accenture.automation.ascu;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.csv.*;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

public class AutomationStepChangeUtility {

    private static final String CSV_FILE = "step-replacements.csv";
    //private static final String SCRIPTS_DIR = "scripts";
    private static final String SCRIPTS_DIR = "C:/Self_Healing_Test_Automation/Self_Healing_Test_Automation/WORK/TestProjectGeneric/scripts";
    private static final String LOG_FILE = "AutomationStepChangeLog.log";

    public static void main(String[] args) {

        try {
            createCsvIfMissing();
            List<Replacement> replacements = loadReplacements();
            Map<String, Queue<Replacement>> replacementMap = new LinkedHashMap<>();

            for (Replacement r : replacements) {
                replacementMap
                    .computeIfAbsent(r.oldStep.trim(), k -> new LinkedList<>())
                    .add(r);
            }


            if (replacements.isEmpty()) {
                System.out.println("No step replacements found. Exiting.");
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            Set<String> modifiedScripts = new HashSet<>();

            Files.list(Paths.get(SCRIPTS_DIR))
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(scriptFile -> processScript(scriptFile, mapper, replacementMap, modifiedScripts));

            generateLog(modifiedScripts);
            System.out.println("Automation Step Change Utility executed successfully.");

        } catch (Exception e) {
            System.out.println("\n[ERROR] Automation Step Change Utility failed");
            System.out.println("[ERROR] Reason: " + e.getMessage());
            System.out.println("[ERROR] Please check step-replacements.csv and try again.");
        }

    }

    private static void processScript(Path scriptFile, ObjectMapper mapper,
                                  Map<String, Queue<Replacement>> replacementMap,
                                  Set<String> modifiedScripts) {


        try {
            JsonNode root = mapper.readTree(scriptFile.toFile());
            ArrayNode steps = (ArrayNode) root.get("steps");
            boolean updated = false;

            for (JsonNode step : steps) {
                String description = step.get("description").asText();

                String key = description.trim();

                if (replacementMap.containsKey(key)) {
                    Queue<Replacement> queue = replacementMap.get(key);

                    if (!queue.isEmpty()) {
                        Replacement r = queue.poll(); // 🔑 consume one replacement only
                        ((ObjectNode) step).put("description", r.newStep);
                        updated = true;
                    }
                }

            }

            if (updated) {
                mapper.writerWithDefaultPrettyPrinter().writeValue(scriptFile.toFile(), root);
                modifiedScripts.add(scriptFile.getFileName().toString());
            }

        } catch (Exception e) {
            System.err.println("Error processing script: " + scriptFile.getFileName());
        }
    }

    private static void createCsvIfMissing() throws IOException {

        Path csvPath = Paths.get(CSV_FILE); // Check if CSV file exists

        if (!Files.exists(csvPath)) {
            try (BufferedWriter writer = Files.newBufferedWriter(csvPath)) {
                writer.write("Old Steps,New Steps,Xpath\n");
                writer.write("<<OLD STEP HERE>>,<<NEW STEP HERE>>,<<XPATH HERE>>\n");
            }

            System.out.println(
                "[INFO] step-replacements.csv not found.\n" +
                "[INFO] A sample CSV has been created. Please update it and re-run the AutomationStepChangeUtility."
            );
        }
    }


    private static List<Replacement> loadReplacements() throws IOException {

        List<Replacement> replacements = new ArrayList<>();

        try (Reader reader = Files.newBufferedReader(Paths.get(CSV_FILE))) {

            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .build();

            try (CSVParser parser = format.parse(reader)) {

                for (CSVRecord record : parser) {

                    // --- BOM-safe header resolution ---
                    Map<String, String> recordMap = record.toMap();
                    Map<String, String> cleanedMap = new HashMap<>();

                    for (Map.Entry<String, String> entry : recordMap.entrySet()) {
                        String cleanKey = entry.getKey()
                                .replace("\uFEFF", "")   // remove BOM
                                .trim();
                        cleanedMap.put(cleanKey, entry.getValue());
                    }

                    if (!cleanedMap.containsKey("Old Steps")
                            || !cleanedMap.containsKey("New Steps")
                            || !cleanedMap.containsKey("Xpath")) {

                        throw new RuntimeException(
                            "CSV header must contain: Old Steps, New Steps, Xpath"
                        );
                    }

                    replacements.add(new Replacement(
                            cleanedMap.get("Old Steps"),
                            cleanedMap.get("New Steps"),
                            cleanedMap.get("Xpath")
                    ));
                }
            }
        }

        return replacements;
    }

    private static void generateLog(Set<String> modifiedScripts) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(LOG_FILE))) {
            writer.write("AUTOMATION STEP CHANGE LOG\n");
            writer.write("Execution Date: " + LocalDate.now() + "\n\n");

            for (String script : modifiedScripts) {
                writer.write(script + "\n");
            }
        }
    }

    static class Replacement {
        String oldStep;
        String newStep;
        String xpath;

        Replacement(String oldStep, String newStep, String xpath) {
            this.oldStep = oldStep;
            this.newStep = newStep;
            this.xpath = xpath;
        }
    }
}