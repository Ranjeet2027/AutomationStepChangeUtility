package com.accenture.automation.ascu;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.*;
import java.util.*;

public class AutomationStepChangeUtility {

    static final String CSV_FILE = "step-replacements.csv";
    //static final String SCRIPTS_DIR = "scripts";
    static final String SCRIPTS_DIR =
            "C:/Self_Healing_Test_Automation/Self_Healing_Test_Automation/WORK/TestProjectGeneric/scripts";
    static final String LOG_FILE = "AutomationStepChangeLog.log";

    public static void main(String[] args) {

        try {
            CsvLoader.createCsvIfMissing(CSV_FILE);
            List<Replacement> replacements = CsvLoader.loadReplacements(CSV_FILE);

            if (replacements.isEmpty()) {
                System.out.println("No step replacements found. Exiting.");
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            Set<String> modifiedScripts = new HashSet<>();

            //Scan and process each script in the scripts directory
            Files.list(Paths.get(SCRIPTS_DIR))
                    .filter(p -> p.toString().endsWith(".json"))
                    .forEach(script ->
                            ScriptProcessor.process(
                                    script, mapper, replacements, modifiedScripts));

            LogGenerator.generate(LOG_FILE, modifiedScripts);
            System.out.println("Automation Step Change Utility executed successfully.");

        } catch (Exception e) {
            System.out.println("\n[ERROR] Automation Step Change Utility failed");
            System.out.println("[ERROR] Reason: " + e.getMessage());
        }
    }
}