package org.omegat.gui.issues;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;

public class EmptyTranslationIssueProvider implements IIssueProvider {

    @Override
    public String getName() {
        return "Empty Translation Checker";
    }

    @Override
    public String getId() {
        return getClass().getCanonicalName();
    }

    @Override
    public List<IIssue> getIssues(SourceTextEntry sourceEntry, TMXEntry tmxEntry) {
        if (sourceEntry == null || tmxEntry == null) {
            return Collections.emptyList();
        }

        String source = sourceEntry.getSrcText();
        String translation = tmxEntry.translation;

        if (source != null
                && !source.trim().isEmpty()
                && (translation == null || translation.trim().isEmpty())) {
            return Arrays.asList(new EmptyTranslationIssue(sourceEntry, tmxEntry));
        }

        return Collections.emptyList();
    }

    private static class EmptyTranslationIssue extends SimpleIssue {

        EmptyTranslationIssue(SourceTextEntry sourceEntry, TMXEntry targetEntry) {
            super(sourceEntry, targetEntry);
        }

        @Override
        protected String getColor() {
            return "#FFA500";
        }

        @Override
        public String getTypeName() {
            return "Empty Translation";
        }

        @Override
        public String getDescription() {
            return "Translation is empty for a non-empty source segment.";
        }
    }
}