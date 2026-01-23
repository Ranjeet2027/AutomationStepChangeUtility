package com.accenture.automation.ascu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.*;

public class ObjectsMapBuilder {

    public static void update(
            ObjectNode root,
            List<Replacement> replacements) {

        ObjectNode objectsMap;

        JsonNode existing = root.get("objectsMap");
        if (existing != null && existing.isObject()) {
            objectsMap = (ObjectNode) existing;
        } else {
            objectsMap = root.putObject("objectsMap");
        }

        System.out.println("[OBJECTSMAP] Updating objectsMap...");

        // Collect locators to REMOVE (old steps)
        Set<String> locatorsToRemove = new HashSet<>();

        // Collect locators to ADD / UPDATE (new steps)
        Map<String, String> locatorsToAdd = new LinkedHashMap<>();

        for (Replacement r : replacements) {

            // OLD step → mark for removal
            if (hasText(r.currentStep)) {
                String oldLocator = extractLocator(r.currentStep);
                if (hasText(oldLocator)) {
                    locatorsToRemove.add(oldLocator);
                }
            }

            // NEW step → mark for add/update
            if (hasText(r.newStep) && hasText(r.relativeXpath)) {
                String newLocator = extractLocator(r.newStep);
                if (hasText(newLocator)) {
                    locatorsToAdd.put(newLocator, r.relativeXpath.trim());
                }
            }
        }

        // REMOVE old locators
        for (String locator : locatorsToRemove) {
            if (objectsMap.has(locator)) {
                objectsMap.remove(locator);
                System.out.println("[OBJECTSMAP] Removed old locator: " + locator);
            }
        }

        // ADD / UPDATE new locators
        for (Map.Entry<String, String> entry : locatorsToAdd.entrySet()) {

            String locator = entry.getKey();
            String relXpath = entry.getValue();

            ObjectNode obj = objectsMap.has(locator)
                    ? (ObjectNode) objectsMap.get(locator)
                    : objectsMap.putObject(locator);

            obj.putIfAbsent("id", obj.textNode(""));
            obj.putIfAbsent("name", obj.textNode(""));
            obj.putIfAbsent("xpath", obj.textNode(""));
            String newXpath = relXpath.trim();
            String oldXpath = obj.has("relXpath") ? obj.get("relXpath").asText() : "";

            if (!newXpath.equals(oldXpath)) {
                obj.put("relXpath", newXpath);
                AutomationStepChangeUtility.totalRelXpathModified.incrementAndGet();
            }

            // obj.put("relXpath", relXpath);
            // AutomationStepChangeUtility.totalRelXpathModified.incrementAndGet();
            System.out.println("[OBJECTSMAP] Updated locator: " + locator);
        }
    }

    // ---------------- helpers ----------------

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static String extractLocator(String desc) {
        if (!hasText(desc)) return null;

        int a = desc.indexOf("'");
        int b = desc.lastIndexOf("'");

        if (a != -1 && b > a) {
            return desc.substring(a + 1, b)
                    .trim()
                    .replace(" ", "_");
        }
        return null;
    }
}