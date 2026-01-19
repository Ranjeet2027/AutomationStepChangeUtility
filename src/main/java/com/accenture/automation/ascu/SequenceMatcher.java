package com.accenture.automation.ascu;

import java.util.List;

public class SequenceMatcher {

    public static int findSequenceStart(
            List<String> scriptSteps,  // From the JSON script
            List<Replacement> replacements) {  // From the CSV

        int scriptSize = scriptSteps.size();
        int seqSize = replacements.size();

        for (int i = 0; i <= scriptSize - seqSize; i++) {
            boolean match = true;

            for (int j = 0; j < seqSize; j++) {
                if (!scriptSteps.get(i + j)
                        .equals(replacements.get(j).oldStep.trim())) {
                    match = false;
                    break;
                }
            }
            if (match) return i;
        }
        return -1;
    }
}