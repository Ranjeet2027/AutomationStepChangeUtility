package com.accenture.automation.ascu;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ActionResolver {

    private static final String ACTION_FILE = "C:/Self_Healing_Test_Automation/Self_Healing_Test_Automation/WORK/TestProjectGeneric/config/customAction.properties";

    private static final Map<String, String> actionMap = new LinkedHashMap<>();

    static {
        try {
            System.out.println("Loading Actions From CustomAction.properties File...");

            List<String> lines = Files.readAllLines(Paths.get(ACTION_FILE));

            for (String line : lines) {

                line = line.trim();

                if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
                    continue;
                }

                // Left side of '=' is the action
                String action = line.split("=", 2)[0].trim();

                actionMap.put(action.toLowerCase(), action);
            }

            System.out.println("Total Actions Loaded: " + actionMap.size());

        } catch (Exception e) {
            throw new RuntimeException(
                    "[ACTION][ERROR] Unable To Load CustomAction.properties File", e);
        }
    }

    //THIS METHOD NAME MATCHES EventsListBuilder
    public static String resolveAction(String stepDescription) {

        if (stepDescription == null || stepDescription.trim().isEmpty()) {
            return "DEFAULT";
        }

        String step = stepDescription.toLowerCase();

        for (String key : actionMap.keySet()) {
            if (step.contains(key)) {
                System.out.println("[ACTION] Matched Action: " + actionMap.get(key));
                return actionMap.get(key);
            }
        }

        throw new RuntimeException("[ACTION][ERROR] No Matching Action Found For Step: " + stepDescription);
    }
}