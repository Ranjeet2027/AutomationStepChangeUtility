package com.accenture.automation.ascu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatasetListUpdater {

    public static void update(
            ObjectNode root,
            List<Replacement> replacements) {

        JsonNode datasetsNode = root.get("datasetList");
        if (datasetsNode == null || !datasetsNode.isArray()) {
            System.out.println("[DATASET] datasetList not found");
            return;
        }

        ArrayNode datasetList = (ArrayNode) datasetsNode;

        for (Replacement r : replacements) {

            if (r.newStep == null || r.newStep.isEmpty()) {
                continue;
            }

            // Only ENTER type steps
            if (!isEnterStep(r.newStep)) {
                continue;
            }

            String dataKey = extractKey(r.newStep);
            String dataValue = extractValue(r.newStep);

            if (dataKey == null || dataValue == null) {
                continue;
            }

            // Update ALL datasets
            for (JsonNode ds : datasetList) {
                if (ds.isObject()) {
                    ((ObjectNode) ds).put(dataKey, dataValue);
                }
            }

            System.out.println("[DATASET] Updated key=" + dataKey + " value=" + dataValue);
        }
    }

    // ---------------- HELPERS ----------------

    private static boolean isEnterStep(String step) {
        return step.contains("SFEnter") || step.contains("Secure enter");
    }

    // Extract text inside single quotes
    private static String extractKey(String step) {
        Matcher m = Pattern.compile("'([^']+)'").matcher(step);
        return m.find() ? m.group(1) : null;
    }

    // Extract text inside double quotes
    private static String extractValue(String step) {
        Matcher m = Pattern.compile("\"([^\"]+)\"").matcher(step);
        return m.find() ? m.group(1) : null;
    }
}