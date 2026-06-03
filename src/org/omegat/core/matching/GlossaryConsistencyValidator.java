package org.omegat.core.matching;

import java.util.List;
import java.util.ArrayList;
import org.omegat.gui.glossary.GlossaryEntry;

/**
 * Technical implementation for the automated glossary checker.
 * Aligns with the core architecture and runs under Checkstyle validation constraints.
 */
public class GlossaryConsistencyValidator {

    /**
     * Validates whether the translated text contains the mandatory glossary targets.
     * @param sourceTokens Tokens of the source segment to identify glossary hits
     * @param targetText The raw string input provided by the translator
     * @param activeGlossaries List of technical terms registered for this segment
     * @return List of entries that violate consistency rules
     */
    public List<GlossaryEntry> validateConsistency(String[] sourceTokens, String targetText, List<GlossaryEntry> activeGlossaries) {
        List<GlossaryEntry> violations = new ArrayList<>();

        if (targetText == null || targetText.trim().isEmpty() || activeGlossaries == null) {
            return violations; // Safe null/empty guard rail isolation
        }

        String normalizedTarget = targetText.toLowerCase();

        for (GlossaryEntry entry : activeGlossaries) {
            String srcTerm = entry.getSrcText().toLowerCase();
            String expectedLocTerm = entry.getLocText().toLowerCase();

            // 检查当前原文分词中是否激活了该术语
            boolean sourceContainsTerm = false;
            for (String token : sourceTokens) {
                if (token.toLowerCase().contains(srcTerm) || srcTerm.contains(token.toLowerCase())) {
                    sourceContainsTerm = true;
                    break;
                }
            }

            // 若原文触发了术语，但译文中却没有使用标准的指定译法，则判定为一条违规记录
            if (sourceContainsTerm && !normalizedTarget.contains(expectedLocTerm)) {
                violations.add(entry);
            }
        }
        return violations;
    }
}