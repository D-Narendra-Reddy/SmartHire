package com.smarthire.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SmartMatchUtil {

    public static int calculateMatchPercentage(String requiredSkillsStr, String candidateSkillsStr) {
        if (requiredSkillsStr == null || requiredSkillsStr.trim().isEmpty()) {
            return 100; // If no skills required, it's a 100% match
        }
        
        if (candidateSkillsStr == null || candidateSkillsStr.trim().isEmpty()) {
            return 0; // Candidate has no skills
        }

        Set<String> requiredSkills = parseSkills(requiredSkillsStr);
        Set<String> candidateSkills = parseSkills(candidateSkillsStr);

        if (requiredSkills.isEmpty()) return 100;

        int matchCount = 0;
        for (String req : requiredSkills) {
            for (String can : candidateSkills) {
                if (req.contains(can) || can.contains(req)) {
                    matchCount++;
                    break;
                }
            }
        }

        double percentage = ((double) matchCount / requiredSkills.size()) * 100;
        return (int) Math.min(percentage, 100.0);
    }

    private static Set<String> parseSkills(String skillsStr) {
        return Arrays.stream(skillsStr.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
