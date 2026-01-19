package com.accenture.automation.ascu;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import java.nio.file.Path;
import java.util.*;

public class ScriptProcessor {

    public static void process(
            Path scriptFile,
            ObjectMapper mapper,
            List<Replacement> replacements,
            Set<String> modifiedScripts) {

        try {
            JsonNode root = mapper.readTree(scriptFile.toFile());
            ArrayNode steps = (ArrayNode) root.get("steps"); //actual test steps

            // Build map: step description -> locator
            Map<String, String> stepToLocatorMap = buildStepLocatorMap(root);

            List<String> scriptSteps = new ArrayList<>();
            for (JsonNode step : steps) {
                scriptSteps.add(step.get("description").asText().trim());  // Extract step descriptions from JSON
            }

            int startIndex = SequenceMatcher.findSequenceStart(scriptSteps, replacements);
            if (startIndex == -1) return;

            for (int i = 0; i < replacements.size(); i++) {
                Replacement r = replacements.get(i);
                JsonNode step = steps.get(startIndex + i);

                StepUpdater.update(step, r.newStep);

                if (r.xpath != null && !r.xpath.trim().isEmpty()) {
                    String locatorKey = stepToLocatorMap.get(r.oldStep.trim());
                    if (locatorKey != null) {
                        RelXpathUpdater.update(root, locatorKey, r.xpath);
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