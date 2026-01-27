package com.accenture.automation.ascu;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;

public class StepSequenceReplacer {

    public static void replaceSequence(
            ArrayNode steps,
            int startIndex,
            List<Replacement> matchRows,   // ONLY rows with Current Steps
            List<Replacement> allRows) {   // ALL CSV rows

        ArrayNode newSteps = steps.arrayNode();

        for (Replacement r : matchRows) {
            ObjectNode step = steps.objectNode();
            step.put(
                "description",
                normalizeStep(
                r.newStep != null && !r.newStep.trim().isEmpty()
                    ? r.newStep
                    : r.currentStep
                )
            );
            newSteps.add(step);
        }


        // Insert NEW-ONLY rows (like R3)
        for (Replacement r : allRows) {
            if (r.currentStep == null || r.currentStep.trim().isEmpty()) {
                ObjectNode step = steps.objectNode();
                //step.put("description", r.newStep);
                step.put("description", normalizeStep(r.newStep));

                newSteps.add(step);
            }
        }

        // REMOVE ONLY matched old steps (THIS FIXES YOUR BUG)
        for (int i = 0; i < matchRows.size(); i++) {
            steps.remove(startIndex);
        }

        // INSERT rebuilt steps
        for (int i = 0; i < newSteps.size(); i++) {
            steps.insert(startIndex + i, newSteps.get(i));
        }

        // Re-number stepNo AND normalize field order
        for (int i = 0; i < steps.size(); i++) {

            ObjectNode oldStep = (ObjectNode) steps.get(i);
            String description = oldStep.get("description").asText();

            ObjectNode normalized = steps.objectNode();
            normalized.put("stepNo", i);
            normalized.put("description", description);

            steps.set(i, normalized);
        }
    }

    private static String normalizeStep(String step) {
        if (step == null) return null;

        return step.replaceAll("\"([^\"]+)\"", "\"\\${$1}\"");
    }

}