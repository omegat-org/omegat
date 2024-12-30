/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2024 Hiroshi Miura
               Home page: https://www.omegat.org/
               Support center: https://omegat.org/support

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.align;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import org.apache.commons.io.FileUtils;
import org.assertj.swing.data.TableCell;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.timing.Timeout;
import org.junit.Rule;
import org.junit.Test;

import org.omegat.gui.main.TestCoreGUI;
import org.omegat.util.Language;
import org.omegat.util.LocaleRule;

public class AlignerWindowTest extends TestCoreGUI {

    private FrameFixture picker;

    private static final String PROJECT_PATH = "test-acceptance/data/project/";
    private static final String SOURCE_PATH = "source/parseSource.txt";
    private static final String TARGET_PATH = "target/parseTarget.txt";

    @Rule
    public final LocaleRule localeRule = new LocaleRule(new Locale("en"));

    @Test
    public void testAligner() throws Exception {
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
        File sourceFile = new File(tmpDir, SOURCE_PATH);
        File translationFile = new File(tmpDir, TARGET_PATH);

        picker.textBox("sourceLanguageFileField").setText(sourceFile.getAbsolutePath());
        picker.textBox("targetLanguageFileField").setText(translationFile.getAbsolutePath());
        //
        picker.button("OK").requireEnabled(Timeout.timeout(100));
        picker.button("OK").click();
        robot().waitForIdle();
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
        aligner.panel("align_panel").comboBox("align_panel_comparison_cb").requireSelection("Heapwise");
        aligner.panel("align_panel").comboBox("align_panel_algorithm_cb").requireSelection("Viterbi");
        aligner.panel("align_panel").comboBox("align_panel_calculator_cb").requireSelection("Normal");
        //
        aligner.panel("align_panel").table("align_panel_table").requireRowCount(5);
        aligner.panel("align_panel").table("align_panel_table").requireColumnCount(3);
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
        aligner.panel("align_panel").label("align_panel_average_distance_label")
                .requireText(Pattern.compile("Average Score:\\s(-|\\d\\.\\d{3})"));
        //
        aligner.button("align_panel_continue_button").click();
        aligner.button("align_panel_save_button").requireEnabled(Timeout.timeout(100));
        aligner.panel("align_panel").label("align_panel_instructions_label")
                .requireText("Step 2: Make manual corrections");
        aligner.panel("align_controls_panel").requireVisible();
        //
        aligner.menuItem("align_menu_file").click();
        aligner.menuItem("align_menu_close_item").click();
        //
     }

    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        tmpDir = Files.createTempDirectory("omegat-sample-project-").toFile();
        File projSrc = new File(PROJECT_PATH);
        FileUtils.copyDirectory(projSrc, tmpDir);
        FileUtils.forceDeleteOnExit(tmpDir);
        // Start aligner
        JFrame frame = GuiActionRunner.execute(() -> {
            AlignFilePickerController picker = new AlignFilePickerController();
            picker.setSourceDefaultDir(tmpDir.toPath().resolve("source").toString());
            picker.setDefaultSaveDir(tmpDir.toPath().resolve("tm").toString());
            picker.setSourceLanguage(new Language("en"));
            picker.setTargetLanguage(new Language("fr"));
            return picker.initGUI(window.target());
        });
        picker = new FrameFixture(robot(), Objects.requireNonNull(frame));
        picker.show();
    }
}
