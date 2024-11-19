package org.omegat.gui.align;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.assertj.swing.data.TableCell;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.timing.Timeout;
import org.junit.Rule;
import org.junit.Test;

import org.omegat.gui.main.BaseMainWindowMenu;
import org.omegat.gui.main.TestCoreGUI;
import org.omegat.util.Language;
import org.omegat.util.LocaleRule;

public class AlignerWindowTest extends TestCoreGUI {

    private static final String PROJECT_PATH = "test-acceptance/data/project/";
    private static final String SOURCE_PATH = "source/parseSource.txt";
    private static final String TARGET_PATH = "target/parseTarget.txt";

    @Rule
    public final LocaleRule localeRule = new LocaleRule(new Locale("en"));

    @Test
    public void testAligner() throws Exception {
        // load project
        openSampleProject(PROJECT_PATH);
        // Start aligner
        window.menuItem(BaseMainWindowMenu.TOOLS_MENU).click();
        window.menuItem("aligner").click();
        // find a file picker frame
        FrameFixture picker = WindowFinder.findFrame("ALIGNER_FILEPICKER").withTimeout(1000).using(robot());
        picker.requireTitle("Align Files");
        picker.panel("align_picker_panel").requireEnabled();
        List<Language> languages = Language.getLanguages();
        picker.comboBox("sourceLanguagePicker").requireItemCount(languages.size());
        picker.comboBox("targetLanguagePicker").requireItemCount(languages.size());
        //
        picker.button("sourceChooseFileButton").requireEnabled();
        picker.button("targetChooseFileButton").requireEnabled();
        //
        picker.button("OK").requireDisabled();
        picker.button("Cancel").requireEnabled();
        //
        picker.comboBox("sourceLanguagePicker").requireSelection("en - English");
        picker.comboBox("targetLanguagePicker").requireSelection("fr - French");
        //
        picker.button("sourceChooseFileButton").click();
        picker.fileChooser().selectFile(new File(tmpDir, SOURCE_PATH));
        picker.fileChooser().approve();
        picker.button("targetChooseFileButton").click();
        picker.fileChooser().selectFile(new File(tmpDir, TARGET_PATH));
        picker.fileChooser().approve();
        String srcFile = picker.textBox("sourceLanguageFileField").text();
        assertNotNull(srcFile);
        assertTrue(srcFile.endsWith(SOURCE_PATH.substring(SOURCE_PATH.indexOf('/') + 1)));
        String targetFile = picker.textBox("targetLanguageFileField").text();
        assertNotNull(targetFile);
        assertTrue(targetFile.endsWith(TARGET_PATH.substring(TARGET_PATH.indexOf('/') + 1)));
        //
        picker.button("OK").requireEnabled(Timeout.timeout(100));
        picker.button("OK").click();
        //
        FrameFixture aligner = WindowFinder.findFrame("ALIGN_MENU_FRAME").withTimeout(5000).using(robot());
        aligner.requireTitle("Align");
        aligner.panel("align_panel").requireVisible();
        aligner.panel("align_advanced_panel").requireVisible();
        aligner.panel("align_panel").checkBox("align_panel_segmenting_checkbox").requireSelected();
        aligner.panel("align_panel").checkBox("align_panel_remove_tags_checkbox").requireNotSelected();
        aligner.panel("align_panel").checkBox("align_panel_highlight_checkbox").requireSelected();
        aligner.panel("align_panel").button("align_panel_segmenting_rules_button").requireEnabled();
        aligner.panel("align_panel").button("align_panel_file_filter_settings_button").requireEnabled();
        aligner.panel("align_panel").table("align_panel_table").requireEnabled();
        //
        aligner.panel("align_panel").label("align_panel_instructions_label")
                .requireText("Step 1: Adjust alignment parameters");
        aligner.panel("align_panel").label("align_panel_average_distance_label")
                .requireText("Average Score: 1.011");
        //
        aligner.panel("align_panel").table("align_panel_table").requireRowCount(5);
        aligner.panel("align_panel").table("align_panel_table").requireColumnCount(3);
        //
        aligner.panel("align_panel").table("align_panel_table").requireCellValue(TableCell.row(0).column(0),
                "true");
        aligner.panel("align_panel").table("align_panel_table").requireCellValue(TableCell.row(0).column(1),
            "This is sentence one.");
        aligner.panel("align_panel").table("align_panel_table").requireCellValue(TableCell.row(0).column(2),
            "C'est la première phrase.");
        aligner.panel("align_panel").table("align_panel_table").requireCellValue(TableCell.row(1).column(1),
        "Short sentence.");
        aligner.panel("align_panel").table("align_panel_table").requireCellValue(TableCell.row(1).column(2),
            "Phrase courte.");
        aligner.panel("align_panel").table("align_panel_table").requireCellValue(TableCell.row(2).column(1),
                "And then this is a very, very, very long sentence.");
        aligner.panel("align_panel").table("align_panel_table").requireCellValue(TableCell.row(2).column(2),
                "Et puis c'est une phrase très, très, très longue.");
        //
        aligner.panel("align_panel").comboBox("align_panel_comparison_cb").requireSelection("Heapwise");
        aligner.panel("align_panel").comboBox("align_panel_algorithm_cb").requireSelection("Viterbi");
        aligner.panel("align_panel").comboBox("align_panel_calculator_cb").requireSelection("Normal");
        //
        aligner.button("align_panel_continue_button").click();
        aligner.button("align_panel_save_button").requireEnabled();
        aligner.panel("align_panel").label("align_panel_instructions_label")
                .requireText("Step 2: Make manual corrections");
        aligner.panel("align_controls_panel").requireVisible();
        //
        aligner.menuItem("align_menu_file").click();
        aligner.menuItem("align_menu_close_item").click();
     }

}
