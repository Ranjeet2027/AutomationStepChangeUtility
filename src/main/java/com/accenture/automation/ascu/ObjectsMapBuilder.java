package com.accenture.automation.ascu;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;

public class ObjectsMapBuilder {

    public static void update(
            ObjectNode root,
            List<Replacement> replacements) {

        ObjectNode objectsMap = root.withObject("objectsMap");

        System.out.println("[OBJECTSMAP] Updating objectsMap...");

        for (Replacement r : replacements) {

            if (r.relativeXpath == null || r.relativeXpath.trim().isEmpty()) {
                continue;
            }

            // Prefer newStep, fallback to currentStep
            String stepDesc =
                    (r.newStep != null && !r.newStep.isEmpty())
                            ? r.newStep
                            : r.currentStep;

            String locator = extractLocator(stepDesc);

            if (locator == null || locator.isEmpty()) {
                System.out.println("Locator Not found For Step: " + stepDesc);
                continue;
            }

            ObjectNode obj = objectsMap.withObject(locator);

            // Ensure essential fields exist
            obj.putIfAbsent("id", obj.textNode(""));
            obj.putIfAbsent("name", obj.textNode(""));
            obj.putIfAbsent("xpath", obj.textNode(""));

            // Update relXpath
            obj.put("relXpath", r.relativeXpath.trim());

            System.out.println("relXpath updated for locator: " + locator);
        }
    }

    // ---------------- HELPER METHODS ----------------
    private static String extractLocator(String desc) {  // Extract text within single quotes
        if (desc == null) return null;

        int a = desc.indexOf("'");
        int b = desc.lastIndexOf("'");

        if (a != -1 && b > a) { 
            return desc.substring(a + 1, b)    // Extract text within single quotes
                    .trim()
                    .replace(" ", "_");
        }
        return null;
    }
}