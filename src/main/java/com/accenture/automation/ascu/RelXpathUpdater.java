package com.accenture.automation.ascu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RelXpathUpdater {

    public static void update(JsonNode root, String locatorKey, String newXpath) {

        JsonNode objectsMap = root.get("objectsMap");
        if (objectsMap == null || !objectsMap.isObject()) {
            System.out.println("ObjectsMap Not Found Or Invalid");
            return;
        }

        ObjectNode objMap = (ObjectNode) objectsMap;
        JsonNode objNode = objMap.get(locatorKey);

        if (objNode == null) {
            System.out.println("[XPATH] No Entry Found In ObjectsMap For Locator");
            return;
        }

        if (!objNode.isObject()) {
            System.out.println("[XPATH] Locator Entry Is Not A JSON Object");
            return;
        }

        String finalXpath = (newXpath == null ? "" : newXpath.trim());

        System.out.println("[XPATH] New RelXpath Value : " + finalXpath);
        System.out.println("[XPATH] Updating RelXpath...");

        ((ObjectNode) objNode).put("RelXpath", finalXpath);

        System.out.println("[XPATH] RelXpath Updated Successfully");
    }
}