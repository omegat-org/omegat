/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
         with fuzzy matching, translation memory, keyword search,
         glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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
package org.omegat.gui.project;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.omegat.core.data.ProjectProperties;
import org.omegat.util.Language;
import org.omegat.util.OStrings;
import org.omegat.util.gui.LanguageComboBoxRenderer;
import org.openide.awt.Mnemonics;

/**
 * Minimal, streamlined dialog for capturing core Project Properties. Focuses on
 * source/target languages for quick project creation.
 */
@SuppressWarnings("serial")
class SimpleProjectPropertiesDialog extends JDialog {

    private final ProjectProperties props;
    private boolean cancelled = true;

    private JComboBox<Language> srcLang;
    private JComboBox<Language> trgLang;

    SimpleProjectPropertiesDialog(Frame parent, ProjectProperties props, ProjectConfigMode mode) {
        super(parent, true);
        this.props = props;
        setTitle(OStrings.getString("PP_TITLE"));
        buildUI();
        pack();
        setMinimumSize(new Dimension(420, 180));
        setLocationRelativeTo(parent);
    }

    private void buildUI() {
        JPanel content = new JPanel(new BorderLayout(10, 10));
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        // Languages
        JPanel row1 = new JPanel();
        row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
        JLabel srcLangLabel = new JLabel();
        Mnemonics.setLocalizedText(srcLangLabel, OStrings.getString("PP_SRC_LANG"));
        row1.add(srcLangLabel);
        row1.add(Box.createHorizontalStrut(8));
        var langs1 = Language.getLanguages();
        srcLang = new JComboBox<>(new DefaultComboBoxModel<>(langs1.toArray(new Language[0])));
        srcLang.setEditable(true);
        srcLang.setRenderer(new LanguageComboBoxRenderer());
        srcLang.setSelectedItem(props.getSourceLanguage());
        row1.add(srcLang);
        form.add(row1);

        form.add(Box.createVerticalStrut(6));

        JPanel row2 = new JPanel();
        row2.setLayout(new BoxLayout(row2, BoxLayout.X_AXIS));
        JLabel trgLangLabel = new JLabel();
        Mnemonics.setLocalizedText(trgLangLabel, OStrings.getString("PP_LOC_LANG"));
        row2.add(trgLangLabel);
        row2.add(Box.createHorizontalStrut(8));
        var langs2 = Language.getLanguages();
        trgLang = new JComboBox<>(new DefaultComboBoxModel<>(langs2.toArray(new Language[0])));
        trgLang.setEditable(true);
        trgLang.setRenderer(new LanguageComboBoxRenderer());
        trgLang.setSelectedItem(props.getTargetLanguage());
        row2.add(trgLang);
        form.add(row2);

        content.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton();
        Mnemonics.setLocalizedText(ok, OStrings.getString("BUTTON_OK"));
        ok.addActionListener(this::onOk);
        JButton cancel = new JButton();
        Mnemonics.setLocalizedText(cancel, OStrings.getString("BUTTON_CANCEL"));
        cancel.addActionListener(e -> {
            cancelled = true;
            setVisible(false);
        });
        buttons.add(cancel);
        buttons.add(ok);
        content.add(buttons, BorderLayout.SOUTH);

        setContentPane(content);
    }
    private void onOk(ActionEvent e) {
        String src = srcLang.getSelectedItem().toString();
        String trg = trgLang.getSelectedItem().toString();
        if (!Language.verifySingleLangCode(src)) {
            srcLang.requestFocusInWindow();
            return;
        }
        if (!Language.verifySingleLangCode(trg)) {
            trgLang.requestFocusInWindow();
            return;
        }
        props.setSourceLanguage(src);
        props.setTargetLanguage(trg);
        cancelled = false;
        setVisible(false);
    }

    boolean isCancelled() {
        return cancelled;
    }
}
