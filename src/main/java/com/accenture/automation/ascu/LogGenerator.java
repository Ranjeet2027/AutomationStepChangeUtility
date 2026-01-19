package com.accenture.automation.ascu;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Set;

public class LogGenerator {

    public static void generate(String logFile, Set<String> modifiedScripts)
            throws IOException {

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(logFile))) {
            writer.write("AUTOMATION STEP CHANGE LOG\n");
            writer.write("Execution Date: " + LocalDate.now() + "\n\n");
            for (String script : modifiedScripts) {
                writer.write(script + "\n");
            }
        }
    }
}