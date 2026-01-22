package com.accenture.automation.ascu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import java.util.*;
import java.util.regex.*;

public class EventsListBuilder {

    public static void rebuild(
            ObjectNode root,
            ArrayNode steps,
            List<Replacement> replacements) {

        ArrayNode oldEvents = (ArrayNode) root.get("eventsList");
        ArrayNode newEvents = root.putArray("eventsList");

        System.out.println("Rebuilding EventsList Safely...");

        for (int i = 0; i < steps.size(); i++) {

            String desc = steps.get(i).get("description").asText();

            ObjectNode event = findExistingEvent(oldEvents, desc);

            if (event == null) {
                event = createNewEvent(root, desc, replacements);
                System.out.println("New Event Created For: " + desc);
            } else {
                event = event.deepCopy(); //VERY IMPORTANT
                System.out.println("Reusing Existing Event For: " + desc);
            }

            event.put("stepNo", i);
            event.put("line", desc);

            newEvents.add(event);
        }
    }

    // ---------------- HELPER METHODS ----------------

    private static ObjectNode findExistingEvent(ArrayNode oldEvents, String desc) { // Find existing event by line
        if (oldEvents == null) return null;

        for (JsonNode e : oldEvents) {
            if (desc.equals(e.get("line").asText())) {  // Match found
                return (ObjectNode) e; // Cast to ObjectNode and return
            }
        }
        return null;
    }

    private static ObjectNode createNewEvent(   // Create a new event node
            ObjectNode root,
            String desc,
            List<Replacement> replacements) {

        String locatorText = extractSingle(desc);
        String locator = locatorText.replace(" ", "_");
        String data = extractDouble(desc);

        String action = ActionResolver.resolveAction(desc);
        Replacement r = findReplacement(desc, replacements);

        ObjectNode e = root.objectNode();
        e.put("stepId", randomId(20));
        e.put("action", action);
        e.put("data", data.isEmpty() ? "" : "${" + data + "}");
        e.put("locator", locator);
        e.put("displayName", "'" + locatorText + "'");
        e.put("scroll", r != null && r.scroll);
        e.put("skip", r != null && r.skip);
        e.put("userActionWord", action.toLowerCase());
        e.put(
            "selfHealingName",
            "'" + locatorText.toLowerCase().replace(" ", "-") + "'"
        );

        return e;
    }

    private static Replacement findReplacement(String desc, List<Replacement> list) {  // Find replacement by newStep
        for (Replacement r : list) {  // Match found
            if (r.newStep != null && r.newStep.equals(desc)) {    
                return r;
            }
        }
        return null;
    }

    private static String extractSingle(String text) {    // Extract text within single quotes
        Matcher m = Pattern.compile("'([^']+)'").matcher(text);
        return m.find() ? m.group(1) : "";
    }

    private static String extractDouble(String text) {  // Extract text within double quotes
        Matcher m = Pattern.compile("\"([^\"]+)\"").matcher(text);
        return m.find() ? m.group(1) : "";
    }

    private static String randomId(int len) {   // Generate random alphanumeric ID
        String chars =
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(r.nextInt(chars.length())));
        }
        return sb.toString();
    }
}