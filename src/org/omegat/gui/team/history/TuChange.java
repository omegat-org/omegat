package org.omegat.gui.team.history;

public class TuChange {
    private final TuChangeType changeType;
    private final TranslationUnit oldTu;
    private final TranslationUnit newTu;

    public TuChange(TuChangeType changeType, TranslationUnit oldTu, TranslationUnit newTu) {
        this.changeType = changeType;
        this.oldTu = oldTu;
        this.newTu = newTu;
    }

    // Getters
    public TuChangeType getChangeType() {
        return changeType;
    }

    public TranslationUnit getOldTu() {
        return oldTu;
    }

    public TranslationUnit getNewTu() {
        return newTu;
    }

    @Override
    public String toString() {
        String sourceText;
        if (oldTu != null) {
            sourceText = oldTu.getSourceText();
        } else {
            sourceText = newTu.getSourceText();
        }
        return String.format("  %s: %s", changeType, sourceText);
    }
}
