package com.accenture.automation.ascu;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;

public class StepSequenceReplacer {

    public static void replaceSequence(
            ArrayNode steps,
            int startIndex,
            List<Replacement> replacements) {

        ArrayNode newSteps = steps.arrayNode();

        int cursor = startIndex;

        for (Replacement r : replacements) {

            //Old + New → replace
            if (hasText(r.currentStep) && hasText(r.newStep)) {
                ObjectNode step = steps.objectNode();
                step.put("description", r.newStep);
                newSteps.add(step);
                cursor++;
            }

            //Old only → keep original
            else if (hasText(r.currentStep)) {
                newSteps.add(steps.get(cursor));
                cursor++;
            }

            // Case New only → insert extra
            else if (hasText(r.newStep)) {
                ObjectNode step = steps.objectNode();
                step.put("description", r.newStep);
                newSteps.add(step);
            }
        }

        // Remove old matched steps
        for (int i = 0; i < replacements.size(); i++) {
            steps.remove(startIndex);
        }

        // Insert rebuilt steps
        for (int i = 0; i < newSteps.size(); i++) {
            steps.insert(startIndex + i, newSteps.get(i));
        }

        // Recalculate stepNo AND normalize field order
        for (int i = 0; i < steps.size(); i++) {

            ObjectNode oldStep = (ObjectNode) steps.get(i);
            String description = oldStep.get("description").asText();

            ObjectNode newStep = steps.objectNode();
            newStep.put("stepNo", i);
            newStep.put("description", description);

            steps.set(i, newStep);
        }
    }

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}