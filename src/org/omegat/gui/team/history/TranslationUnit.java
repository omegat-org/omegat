package org.omegat.gui.team.history;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class TranslationUnit {
    private final String changer;
    private final long changed;
    private final String sourceText;
    private final String targetText;

    public TranslationUnit(String changer, long changed, String sourceText, String targetText) {
        this.changer = changer;
        this.changed = changed;
        this.sourceText = sourceText;
        this.targetText = targetText;
    }

    public String getChanger() {
        return changer;
    }

    public long getChanged() {
        return changed;
    }

    public String getSourceText() {
        return sourceText;
    }

    public String getTargetText() {
        return targetText;
    }
}
