package org.omegat.gui.align;

import java.util.List;
import java.util.Locale;

import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.Rule;
import org.junit.Test;

import org.omegat.gui.main.BaseMainWindowMenu;
import org.omegat.gui.main.TestCoreGUI;
import org.omegat.util.Language;
import org.omegat.util.LocaleRule;

public class AlignerWindowTest extends TestCoreGUI {

   @Rule
   public final LocaleRule localeRule = new LocaleRule(new Locale("en"));

    @Test
    public void testAlignFilePickerOpenedWhenProjectNotOpen() {
        window.menuItem(BaseMainWindowMenu.TOOLS_MENU).click();
        window.menuItem("aligner").click();
        // find a file picker frame
        FrameFixture picker = WindowFinder.findFrame("ALIGNER_FILEPICKER").withTimeout(1000).using(robot());
        picker.requireTitle("Align Files");
        picker.panel("align_picker_panel").requireEnabled();
        // check language selectors
        List<Language> languages = Language.getLanguages();
        picker.comboBox("sourceLanguagePicker").requireItemCount(languages.size());
        picker.comboBox("targetLanguagePicker").requireItemCount(languages.size());
        //
        picker.button("sourceChooseFileButtion").requireEnabled();
        picker.button("targetChooseFileButtion").requireEnabled();
        //
        picker.button("OK").requireDisabled();
        picker.button("Cancel").requireEnabled();
        picker.button("Cancel").click();
    }
}
