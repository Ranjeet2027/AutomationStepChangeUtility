package com.accenture.automation.ascu;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ScriptProcessor {

    public static void process(
            Path scriptFile,
            ObjectMapper mapper,
            List<Replacement> replacements,
            Set<String> modifiedScripts,
            AtomicInteger xpathCount) {

        try {
            System.out.println("----------------------------------------------");
            System.out.println("Processing Script : " + scriptFile.getFileName());

            JsonNode root = mapper.readTree(scriptFile.toFile());
            ArrayNode steps = (ArrayNode) root.get("steps");

            System.out.println("[SCRIPT] " + scriptFile.getFileName()+ " | Total Steps: " + steps.size());

            //Build map: step description -> locator
            Map<String, String> stepToLocatorMap = buildStepLocatorMap(root);
            System.out.println("Total Step to Locator Mappings : " + stepToLocatorMap.size());

            //Extract step descriptions from script
            List<String> scriptSteps = new ArrayList<>();
            for (JsonNode step : steps) {
                scriptSteps.add(step.get("description").asText().trim());
            }

            //Build match list (ONLY rows having Old Steps)
            List<Replacement> matchRows = new ArrayList<>();
            for (Replacement r : replacements) {
                if (r.currentStep != null && !r.currentStep.trim().isEmpty()) {
                    matchRows.add(r);
                }
            }

            // No valid match rows → exit
            if (matchRows.isEmpty()) {
                System.out.println("No Valid Match Rows Found In CSV. Skipping Script.");
                return;
            }
 
            //Find sequence using matchRows ONLY
            System.out.println("Matching Step Sequence Length : " + matchRows.size());
            int startIndex = SequenceMatcher.findSequenceStart(scriptSteps, matchRows);
            if (startIndex == -1) {
                System.out.println("No Matching Step Sequence Found. No Changes Applied.");
                return;
            }

            System.out.println("Step Sequence Matched At Index : " + startIndex);
            //Replace sequence ONCE using ALL replacements
            System.out.println("Replacing Step Sequence...");
            StepSequenceReplacer.replaceSequence(
                    steps,
                    startIndex,
                    replacements
            );

            //Update XPath ONLY for rows having Old Steps
            for (Replacement r : matchRows) {
                if (r.relativeXpath != null && !r.relativeXpath.trim().isEmpty()) {
                    String locatorKey = stepToLocatorMap.get(r.currentStep.trim());
                    if (locatorKey != null) {
                        System.out.println("Updating RelXpath For Locator : " + locatorKey);
                        RelXpathUpdater.update(root, locatorKey, r.relativeXpath);

                        xpathCount.incrementAndGet(); //Update total relative xpaths modified
                    } else {
                        System.out.println("Locator Not Found For Step : " + r.currentStep);
                    }
                }
            }

            //Save updated JSON
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(scriptFile.toFile(), root);

            modifiedScripts.add(scriptFile.getFileName().toString());
            System.out.println("[SUCCESS] Script Updated Successfully.");

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to process script : " + scriptFile.getFileName());
            System.err.println("[ERROR] Reason : " + e.getMessage());
        }
    }

    //Building step → locator mapping from eventsList
    private static Map<String, String> buildStepLocatorMap(JsonNode root) {

        Map<String, String> map = new HashMap<>();
        JsonNode eventsList = root.get("eventsList");

        if (eventsList != null && eventsList.isArray()) {
            for (JsonNode event : eventsList) {
                map.put(
                        event.get("line").asText().trim(),
                        event.get("locator").asText().trim()
                );
            }
        }
        return map;
    }
}