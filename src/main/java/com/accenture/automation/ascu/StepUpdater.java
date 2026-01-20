package com.accenture.automation.ascu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class StepUpdater {

    public static void update(JsonNode step, String newStep) {
        //Updating step description
        if (newStep != null && !newStep.trim().isEmpty()) {
            ((ObjectNode) step).put("description", newStep);  //Step updated successfully in JSON description field
        }
    }
}