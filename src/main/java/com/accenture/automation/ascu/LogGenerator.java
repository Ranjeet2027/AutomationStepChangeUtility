package com.accenture.automation.ascu;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class LogGenerator {

    public static void generate(String logFile, Set<String> modifiedScripts)
            throws IOException {

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(logFile))) {

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

            writer.write("AUTOMATION STEP CHANGE UTILITY LOG\n");
            writer.write("Execution Time: " + now.format(formatter) + "\n");
            writer.write("--------------------------------------------------\n");
            writer.write("Automation Scripts Modified:\n");
            writer.write("--------------------------------------------------\n");

            if (modifiedScripts.isEmpty()) {
                writer.write("No scripts were modified.\n");
            } else {
                for (String script : modifiedScripts) {
                    writer.write(" - " + script + "\n");
                }
            }
        }
    }
}