package com.accenture.automation.ascu;

import java.util.List;

public class SequenceMatcher {

    public static int findSequenceStart(
            List<String> scriptSteps,          // From the JSON script
            List<Replacement> replacements) {  // From the CSV file

        int scriptSize = scriptSteps.size();
        int seqSize = replacements.size();

        // If CSV sequence is longer than script → impossible to match
        if (seqSize > scriptSize) {
            System.out.println("CSV Sequence Length Is Greater Than Script Steps. Skipping.");
            return -1;
        }

        // Try each possible start index in the script
        for (int i = 0; i <= scriptSize - seqSize; i++) {
            boolean match = true;

            for (int j = 0; j < seqSize; j++) {
                if (!scriptSteps.get(i + j)
                        .equals(replacements.get(j).currentStep.trim())) {
                    //System.out.println("[MISMATCH] Sequence Broken At Offset " + j);
                    match = false;
                    break;
                } else {
                    System.out.println("[MATCH] Step Matched");
                }
            }
             if (match) {
                System.out.println("Full Sequence Matched At Index : " + i);
                return i;
            }
        }

        System.out.println("No Matching Step Sequence Found In Script.");
        return -1;
    }
}