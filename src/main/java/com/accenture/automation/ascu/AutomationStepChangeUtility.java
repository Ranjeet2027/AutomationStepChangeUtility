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
    // private static final String SCRIPTS_DIR = "scripts";
    private static final String SCRIPTS_DIR =
            "C:/Self_Healing_Test_Automation/Self_Healing_Test_Automation/WORK/TestProjectGeneric/scripts";
    private static final String LOG_FILE = "AutomationStepChangeLog.log";

    public static void main(String[] args) {

        try {
            createCsvIfMissing();
            List<Replacement> replacements = loadReplacements();

            if (replacements.isEmpty()) {
                System.out.println("No step replacements found. Exiting.");
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            Set<String> modifiedScripts = new HashSet<>();

            Files.list(Paths.get(SCRIPTS_DIR))
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(scriptFile ->
                            processScript(scriptFile, mapper, replacements, modifiedScripts));

            generateLog(modifiedScripts);
            System.out.println("Automation Step Change Utility executed successfully.");

        } catch (Exception e) {
            System.out.println("\n[ERROR] Automation Step Change Utility failed");
            System.out.println("[ERROR] Reason: " + e.getMessage());
            System.out.println("[ERROR] Please check step-replacements.csv and try again.");
        }
    }

    private static void processScript(
            Path scriptFile,
            ObjectMapper mapper,
            List<Replacement> replacements,
            Set<String> modifiedScripts) {

        try {
            JsonNode root = mapper.readTree(scriptFile.toFile());
            ArrayNode steps = (ArrayNode) root.get("steps");

            // Build map: step description -> locator
            Map<String, String> stepToLocatorMap = new HashMap<>();
            JsonNode eventsList = root.get("eventsList");

            if (eventsList != null && eventsList.isArray()) {
                for (JsonNode event : eventsList) {
                    stepToLocatorMap.put(
                            event.get("line").asText().trim(),
                            event.get("locator").asText().trim()
                    );
                }
            }

            // Extract step descriptions
            List<String> scriptSteps = new ArrayList<>();
            for (JsonNode step : steps) {
                scriptSteps.add(step.get("description").asText().trim());
            }

            // Find FULL sequence match
            int startIndex = findSequenceStart(scriptSteps, replacements);
            if (startIndex == -1) {
                return; // no match → no change
            }

            // Apply replacements
            for (int i = 0; i < replacements.size(); i++) {
                Replacement r = replacements.get(i);
                JsonNode step = steps.get(startIndex + i);

                // Update step text ONLY if provided
                if (r.newStep != null && !r.newStep.trim().isEmpty()) {
                    ((ObjectNode) step).put("description", r.newStep);
                }

                // Update relXpath independently
                if (r.xpath != null && !r.xpath.trim().isEmpty()) {
                    String locatorKey = stepToLocatorMap.get(r.oldStep.trim());
                    if (locatorKey != null) {
                        updateRelXpath(root, locatorKey, r.xpath);
                    }
                }
            }

            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(scriptFile.toFile(), root);

            modifiedScripts.add(scriptFile.getFileName().toString());

        } catch (Exception e) {
            System.err.println("Error processing script: " + scriptFile.getFileName());
        }
    }

    private static void updateRelXpath(JsonNode root, String locatorKey, String newXpath) {

        JsonNode objectsMap = root.get("objectsMap");
        if (objectsMap == null || !objectsMap.isObject()) {
            return;
        }

        ObjectNode objMap = (ObjectNode) objectsMap;
        JsonNode objNode = objMap.get(locatorKey);

        if (objNode != null && objNode.isObject()) {
            ((ObjectNode) objNode).put(
                    "relXpath",
                    newXpath == null ? "" : newXpath.trim()
            );
        }
    }

    private static int findSequenceStart(
            List<String> scriptSteps,
            List<Replacement> replacements) {

        int scriptSize = scriptSteps.size();
        int seqSize = replacements.size();

        for (int i = 0; i <= scriptSize - seqSize; i++) {
            boolean match = true;

            for (int j = 0; j < seqSize; j++) {
                if (!scriptSteps.get(i + j)
                        .equals(replacements.get(j).oldStep.trim())) {
                    match = false;
                    break;
                }
            }

            if (match) {
                return i;
            }
        }
        return -1;
    }

    private static void createCsvIfMissing() throws IOException {

        Path csvPath = Paths.get(CSV_FILE);
        if (!Files.exists(csvPath)) {
            try (BufferedWriter writer = Files.newBufferedWriter(csvPath)) {
                writer.write("Old Steps,New Steps,Xpath\n");
                writer.write("<<OLD STEP HERE>>,<<NEW STEP HERE>>,<<XPATH HERE>>\n");
            }

            System.out.println(
                    "[INFO] step-replacements.csv not found.\n" +
                    "[INFO] A sample CSV has been created. Please update it and re-run."
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

                    Map<String, String> cleanedMap = new HashMap<>();
                    for (Map.Entry<String, String> e : record.toMap().entrySet()) {
                        cleanedMap.put(
                                e.getKey().replace("\uFEFF", "").trim(),
                                e.getValue()
                        );
                    }

                    if (!cleanedMap.containsKey("Old Steps")
                            || !cleanedMap.containsKey("New Steps")
                            || !cleanedMap.containsKey("Xpath")) {
                        throw new RuntimeException(
                                "CSV header must contain: Old Steps, New Steps, Xpath");
                    }

                    if ((cleanedMap.get("New Steps") == null
                            || cleanedMap.get("New Steps").trim().isEmpty())
                            && (cleanedMap.get("Xpath") == null
                            || cleanedMap.get("Xpath").trim().isEmpty())) {
                        throw new RuntimeException(
                                "Each CSV row must have either New Steps or Xpath");
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