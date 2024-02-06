package org.omegat.gui.editor.mark;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.util.gui.Styles;

import javax.swing.text.Highlighter;
import java.util.Collections;
import java.util.List;

public class AltTranslationsMarker extends AbstractMarker {

    protected static final Highlighter.HighlightPainter PAINTER = new TransparentHighlightPainter(
            Styles.EditorColor.COLOR_MARK_ALT_TRANSLATION.getColor(), 0.5F);

    public AltTranslationsMarker() throws Exception {
        super();
    }

    @Override
    protected boolean isEnabled() {
        return Core.getEditor().getSettings().isMarkAltTranslations();
    }

    @Override
    public List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText, String translationText, boolean isActive) throws Exception {
        if (!isEnabled()) {
            return null;
        }

        if (!Core.getProject().getTranslationInfo(ste).defaultTranslation) {
            Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, 0, translationText.length());
            m.painter = PAINTER;
            return Collections.singletonList(m);
        }

        return null;
    }
}
