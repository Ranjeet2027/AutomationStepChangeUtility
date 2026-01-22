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

            JsonNode root = mapper.readTree(scriptFile.toFile());  // Load JSON
            ArrayNode steps = (ArrayNode) root.get("steps"); // Extract steps

            System.out.println("[SCRIPT] " + scriptFile.getFileName()+ " | Total Steps: " + steps.size());

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
            System.out.println("Replacing Step Sequence...");

            // 1. Replace step sequence
            StepSequenceReplacer.replaceSequence(
                    steps,
                    startIndex,
                    matchRows,
                    replacements
            );

            // 2. Rebuild eventsList (safe reuse)
            EventsListBuilder.rebuild(
                    (ObjectNode) root,
                    steps,
                    replacements
            );

            // 3. UPDATE OBJECTS MAP (THIS IS THE ONLY PLACE)
            ObjectsMapBuilder.update(
                    (ObjectNode) root,
                    replacements
            );

            // 4. Save JSON
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(scriptFile.toFile(), root);

            modifiedScripts.add(scriptFile.getFileName().toString()); // Record modified script

            System.out.println("[SUCCESS] Script Updated Successfully.");

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to process script : " + scriptFile.getFileName());
            System.err.println("[ERROR] Reason : " + e.getMessage());
        }
    }
}