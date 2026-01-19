package com.accenture.automation.ascu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RelXpathUpdater {

    public static void update(JsonNode root, String locatorKey, String newXpath) {

        JsonNode objectsMap = root.get("objectsMap");
        if (objectsMap == null || !objectsMap.isObject()) return;

        ObjectNode objMap = (ObjectNode) objectsMap;
        JsonNode objNode = objMap.get(locatorKey);

        // Update relXpath independently
        if (objNode != null && objNode.isObject()) {
            ((ObjectNode) objNode).put(
                    "relXpath",
                    newXpath == null ? "" : newXpath.trim()
            );
        }
    }
}