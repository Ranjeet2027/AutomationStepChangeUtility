package com.accenture.automation.ascu;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AutomationStepChangeUtility {

    static final String CSV_FILE = "step-replacements.csv";
    //static final String SCRIPTS_DIR = "scripts";
    static final String SCRIPTS_DIR = "C:/Self_Healing_Test_Automation/Self_Healing_Test_Automation/WORK/TestProjectGeneric/scripts";
    static final String LOG_FILE = "AutomationStepChangeUtilityLog.log";

    static AtomicInteger totalRelXpathModified = new AtomicInteger(0);
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  Automation Step Change Utility");
        System.out.println("--------------------------------------------------");
        System.out.println("  Simplifying Test Automation Maintenance Effort");
        System.out.println("  Initializing...");
        System.out.println("==================================================");

        try {
            CsvLoader.createCsvIfMissing(CSV_FILE);
            List<Replacement> replacements = CsvLoader.loadReplacements(CSV_FILE); // Load The Row from CSV For Replacements

            if (replacements.isEmpty()) {
                System.out.println("No Step Replacements Found. Exiting utility.");

                return;
            }

            ObjectMapper mapper = new ObjectMapper();  // ObjectMapper is Jackson's class for JSON processing creating object of it
            Set<String> modifiedScripts = new HashSet<>();

            //Scan and process each script in the scripts directory
            Files.list(Paths.get(SCRIPTS_DIR))
                    .filter(p -> p.toString().endsWith(".json"))
                    .forEach(script ->
                            ScriptProcessor.process(
                                    script,
                                    mapper,
                                    replacements,
                                    modifiedScripts,
                                    totalRelXpathModified
                            ));

            LogGenerator.generate(LOG_FILE, modifiedScripts);

            System.out.println("==================================================");
            System.out.println("Automation Step Change Utility Executed Successfully.");
            //System.out.println(" Total Scripts Modified : " + modifiedScripts.size());
            System.out.println("Total Modified Scripts        : " + modifiedScripts.size());
            System.out.println("Relative Xpath Modified Across Scripts: " + totalRelXpathModified.get());
            System.out.println("==================================================");

        } catch (Exception e) {
            System.out.println("\n[ERROR] Automation Step Change Utility failed");
            System.out.println("[ERROR] Reason: " + e.getMessage());
            System.out.println("[ERROR] Please review inputs and retry.");
        }
    }
}