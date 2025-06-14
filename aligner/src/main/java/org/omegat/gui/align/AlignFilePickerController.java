/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.CancellationException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;

import org.omegat.core.Core;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.LanguageComboBoxRenderer;
import org.omegat.util.gui.StaticUIUtils;

/**
 * Controller for align file picker UI
 *
 * @author Aaron Madlon-Kay
 */
public class AlignFilePickerController {
    String sourceFile;
    String targetFile;

    String sourceDefaultDir;
    String targetDefaultDir;
    String defaultSaveDir;

    List<Language> allLangs = Language.getLanguages();
    Language sourceLanguage = allLangs.get(0);
    Language targetLanguage = allLangs.get(allLangs.size() - 1);

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("org.omegat.gui.align.Bundle");

    /**
     * Set the source file for alignment.
     * 
     * @param sourceFile
     *            source file path.
     */
    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    /**
     * Set target file for alignment.
     * 
     * @param targetFile
     *            target file path.
     */
    public void setTargetFile(String targetFile) {
        this.targetFile = targetFile;
    }

    /**
     * Set source language.
     * 
     * @param sourceLanguage
     *            source language.
     */
    public void setSourceLanguage(Language sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    /**
     * Target source language.
     * 
     * @param targetLanguage
     *            target langauge.
     */
    public void setTargetLanguage(Language targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    /**
     * Set source defualt directory.
     * 
     * @param sourceDefaultDir
     *            source default directory.
     */
    public void setSourceDefaultDir(String sourceDefaultDir) {
        this.sourceDefaultDir = sourceDefaultDir;
    }

    /**
     * Set target default directory.
     * 
     * @param targetDefaultDir
     *            target default directory.
     */
    public void setTargetDefaultDir(String targetDefaultDir) {
        this.targetDefaultDir = targetDefaultDir;
    }

    /**
     * Set default save directory.
     * 
     * @param defaultSaveDir
     *            default save directory to set.
     */
    public void setDefaultSaveDir(String defaultSaveDir) {
        this.defaultSaveDir = defaultSaveDir;
    }

    /**
     * Display the align tool file picker. The picker is not modal, so this call
     * will return immediately.
     *
     * @param parent
     *            Parent window of file picker and align window
     */
    public void show(final Component parent) {
        JFrame frame = initGUI(parent);
        frame.setLocationRelativeTo(parent);
        frame.setVisible(true);
    }

    // public for test only
    @SuppressWarnings("serial")
    public JFrame initGUI(Component parent) {
        final JFrame frame = new JFrame(BUNDLE.getString("ALIGNER_FILEPICKER"));
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        StaticUIUtils.setEscapeClosable(frame);
        frame.setName("ALIGNER_FILEPICKER");
        //
        AlignFilePicker picker = new AlignFilePicker();
        initializeFilePicker(picker);
        // add the main listener.
        picker.okButton.addActionListener(e -> {
            picker.bottomPanel.remove(picker.messageTextArea);
            picker.bottomPanel.add(picker.progressBar, BorderLayout.CENTER);
            picker.bottomPanel.revalidate();
            new SwingWorker<Aligner, Void>() {
                @Override
                protected Aligner doInBackground() throws Exception {
                    Aligner aligner = new Aligner(sourceFile, sourceLanguage, targetFile, targetLanguage);
                    aligner.loadFiles();
                    return aligner;
                }

                @Override
                protected void done() {
                    try {
                        Aligner aligner = get();
                        new AlignPanelController(defaultSaveDir).show(parent, aligner);
                    } catch (CancellationException e) {
                        // Ignore
                    } catch (Exception e) {
                        Log.log(e);
                        JOptionPane.showMessageDialog(frame, BUNDLE.getString("ALIGNER_ERROR_LOADING"),
                                BUNDLE.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
                    }
                    frame.dispose();
                }
            }.execute();
        });
        picker.cancelButton.addActionListener(e -> frame.dispose());
        addListeners(picker, frame);
        frame.getRootPane().setDefaultButton(picker.okButton);
        updatePicker(picker);
        frame.add(picker);
        frame.pack();
        return frame;
    }

    /**
     * Initialize FilePicker.
     * @return FilePicker frame.
     */
    private void initializeFilePicker(AlignFilePicker picker) {
        picker.setName("align_picker_panel");
        picker.sourceLanguagePicker
                .setModel(new DefaultComboBoxModel<>(new Vector<>(Language.getLanguages())));
        picker.sourceLanguagePicker.setRenderer(new LanguageComboBoxRenderer());
        picker.sourceLanguagePicker.setSelectedItem(sourceLanguage);
        picker.sourceLanguagePicker.setName("sourceLanguagePicker");

        picker.targetLanguagePicker
                .setModel(new DefaultComboBoxModel<>(new Vector<>(Language.getLanguages())));
        picker.targetLanguagePicker.setRenderer(new LanguageComboBoxRenderer());
        picker.targetLanguagePicker.setSelectedItem(targetLanguage);
        picker.targetLanguagePicker.setName("targetLanguagePicker");

        picker.sourceChooseFileButton.setName("sourceChooseFileButton");
        picker.targetChooseFileButton.setName("targetChooseFileButton");
        picker.sourceLanguageFileField.setText(sourceFile);
        picker.sourceLanguageFileField.setName("sourceLanguageFileField");

        picker.targetLanguageFileField.setText(targetFile);
        picker.targetLanguageFileField.setName("targetLanguageFileField");

        picker.sourceLanguageFileField.setTransferHandler(languageFileFieldTransferHandler);
        picker.targetLanguageFileField.setTransferHandler(languageFileFieldTransferHandler);

        picker.okButton.setName("OK");
        picker.cancelButton.setName("Cancel");
    }

    private final TransferHandler languageFileFieldTransferHandler = new TransferHandler() {
        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            try {
                List<?> list = (List<?>) support.getTransferable()
                        .getTransferData(DataFlavor.javaFileListFlavor);
                List<File> files = getSupportedFiles(list);
                if (files.isEmpty()) {
                    return false;
                }
                JTextComponent field = (JTextComponent) support.getComponent();
                field.setText(files.get(0).getAbsolutePath());
                return true;
            } catch (Exception e) {
                Log.log(e);
                return false;
            }
        }
    };

    private void addListeners(AlignFilePicker picker, JFrame frame) {
        picker.sourceLanguagePicker.addItemListener(e -> {
            if (e.getStateChange() != ItemEvent.SELECTED) {
                return;
            }
            if (e.getItem() instanceof String) {
                String newVal = (String) e.getItem();
                if (Language.verifySingleLangCode(newVal)) {
                    sourceLanguage = new Language(newVal);
                } else {
                    sourceLanguage = null;
                    JOptionPane.showMessageDialog(frame,
                            BUNDLE.getString("NP_INVALID_SOURCE_LOCALE")
                                    + BUNDLE.getString("NP_LOCALE_SUGGESTION"),
                            BUNDLE.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
                    picker.sourceLanguagePicker.requestFocusInWindow();
                }
            } else if (e.getItem() instanceof Language) {
                sourceLanguage = (Language) e.getItem();
            } else {
                throw new IllegalArgumentException();
            }
            updatePicker(picker);
        });

        picker.targetLanguagePicker.addItemListener(e -> {
            if (e.getStateChange() != ItemEvent.SELECTED) {
                return;
            }
            if (e.getItem() instanceof String) {
                String newVal = (String) e.getItem();
                if (Language.verifySingleLangCode(newVal)) {
                    targetLanguage = new Language(newVal);
                } else {
                    targetLanguage = null;
                    JOptionPane.showMessageDialog(frame,
                            BUNDLE.getString("NP_INVALID_TARGET_LOCALE")
                                    + BUNDLE.getString("NP_LOCALE_SUGGESTION"),
                            BUNDLE.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
                    picker.targetLanguagePicker.requestFocusInWindow();
                }
            } else if (e.getItem() instanceof Language) {
                targetLanguage = (Language) e.getItem();
            } else {
                throw new IllegalArgumentException();
            }
            updatePicker(picker);
        });
        picker.sourceChooseFileButton.addActionListener(e -> {
            File file = chooseFile(frame, BUNDLE.getString("ALIGNER_FILEPICKER_CHOOSE_SOURCE"),
                    StringUtil.isEmpty(sourceFile) ? sourceDefaultDir : sourceFile, "aligner_choose_source");
            if (file != null) {
                sourceDefaultDir = file.getParent();
                targetDefaultDir = targetDefaultDir == null ? sourceDefaultDir : targetDefaultDir;
                defaultSaveDir = defaultSaveDir == null ? sourceDefaultDir : defaultSaveDir;
                picker.sourceLanguageFileField.setText(file.getAbsolutePath());
                sourceFile = file.getAbsolutePath();
            }
        });
        picker.targetChooseFileButton.addActionListener(e -> {
            File file = chooseFile(frame, BUNDLE.getString("ALIGNER_FILEPICKER_CHOOSE_TARGET"),
                    StringUtil.isEmpty(targetFile) ? targetDefaultDir : targetFile, "aligner_choose_target");
            if (file != null) {
                targetDefaultDir = file.getParent();
                sourceDefaultDir = sourceDefaultDir == null ? targetDefaultDir : sourceDefaultDir;
                defaultSaveDir = defaultSaveDir == null ? targetDefaultDir : defaultSaveDir;
                picker.targetLanguageFileField.setText(file.getAbsolutePath());
                targetFile = file.getAbsolutePath();
            }
        });

        picker.sourceLanguageFileField.getDocument().addDocumentListener(new AlignFilePickerDocumentListener(() -> {
            sourceFile = picker.sourceLanguageFileField.getText();
            updatePicker(picker);
        }));
        picker.targetLanguageFileField.getDocument().addDocumentListener(new AlignFilePickerDocumentListener(() -> {
            targetFile = picker.targetLanguageFileField.getText();
            updatePicker(picker);
        }));
        picker.setTransferHandler(new PickerTransferHandler(picker));
    }

    private void updatePicker(final AlignFilePicker picker) {
        if (sourceFile == null || targetFile == null || sourceLanguage == null || targetLanguage == null) {
            picker.messageTextArea.setText(null);
            picker.okButton.setEnabled(false);
            return;
        }
        final File srcFile = new File(sourceFile);
        final File trgFile = new File(targetFile);
        if (!srcFile.isFile() || !trgFile.isFile() || srcFile.equals(trgFile)) {
            picker.messageTextArea.setText(null);
            picker.okButton.setEnabled(false);
            return;
        }
        new SwingWorker<boolean[], Void>() {
            @Override
            protected boolean[] doInBackground() throws Exception {
                FilterMaster fm = Core.getFilterMaster();
                return new boolean[] { fm.isFileSupported(srcFile, false),
                        fm.isFileSupported(trgFile, false) };
            }

            @Override
            protected void done() {
                boolean enabled = false;
                String message = null;
                try {
                    boolean[] results = get();
                    enabled = results[0] && results[1];
                    if (!results[0] && results[1]) {
                        message = StringUtil.format(BUNDLE.getString("ALIGNER_FILEPICKER_ERROR_ONE_FILE"),
                                srcFile.getName());
                    } else if (results[0] && !results[1]) {
                        message = StringUtil.format(BUNDLE.getString("ALIGNER_FILEPICKER_ERROR_ONE_FILE"),
                                trgFile.getName());
                    } else if (!results[0] && !results[1]) {
                        message = BUNDLE.getString("ALIGNER_FILEPICKER_ERROR_BOTH_FILES");
                    }
                } catch (CancellationException e) {
                    // Ignore
                } catch (Exception e) {
                    Log.log(e);
                    message = e.getLocalizedMessage();
                }
                picker.okButton.setEnabled(enabled);
                picker.messageTextArea.setText(message);
            }
        }.execute();
    }

    private static List<File> getSupportedFiles(List<?> files) {
        List<File> result = new ArrayList<File>(files.size());
        FilterMaster fm = Core.getFilterMaster();
        for (Object o : files) {
            File file = (File) o;
            if (fm.isFileSupported(file, true)) {
                result.add(file);
            }
        }
        return result;
    }

    static File chooseFile(Component parent, String title, String dir, String name) {
        JFileChooser chooser = new JFileChooser(dir);
        chooser.setName(name);
        chooser.setDialogTitle(title);
        chooser.setFileFilter(new FileFilter() {
            @Override
            public String getDescription() {
                return BUNDLE.getString("ALIGNER_FILEPICKER_SUPPORTEDFILES");
            }

            @Override
            public boolean accept(File f) {
                return f.isDirectory() || Core.getFilterMaster().isFileSupported(f, true);
            }
        });
        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    private static class AlignFilePickerDocumentListener implements DocumentListener {
        private final Runnable runnable;

        AlignFilePickerDocumentListener(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            update();
        }

        private void update() {
            runnable.run();
        }
    }

    @SuppressWarnings("serial")
    private static class PickerTransferHandler extends TransferHandler {
        private final AlignFilePicker picker;

        PickerTransferHandler(final AlignFilePicker picker) {
            this.picker = picker;
        }

        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            try {
                List<?> list = (List<?>) support.getTransferable()
                        .getTransferData(DataFlavor.javaFileListFlavor);
                List<File> files = getSupportedFiles(list);
                if (files.isEmpty()) {
                    return false;
                } else if (files.size() == 1) {
                    JTextComponent insertTarget = picker.sourceLanguageFileField;
                    if (picker.sourceLanguageFileField.getDocument().getLength() != 0
                            && picker.targetLanguageFileField.getDocument().getLength() == 0) {
                        insertTarget = picker.targetLanguageFileField;
                    }
                    insertTarget.setText(files.get(0).getAbsolutePath());
                } else {
                    picker.sourceLanguageFileField.setText(files.get(0).getAbsolutePath());
                    picker.targetLanguageFileField.setText(files.get(1).getAbsolutePath());
                }
                return true;
            } catch (Exception e) {
                Log.log(e);
                return false;
            }
        }
    }

    /**
     * Entry point for debugging or standalone use. Optionally accepts four
     * arguments to pre-fill the picker:
     * <ol>
     * <li>Source language
     * <li>Source file path
     * <li>Target language
     * <li>Target file path
     * </ol>
     *
     * @param args command arguments.
     * @throws Exception when failed to ininitalize OmegaT core.
     */
    public static void main(String[] args) throws Exception {
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        Preferences.init();
        PluginUtils.loadPlugins(Collections.emptyMap());
        Core.setFilterMaster(new FilterMaster(FilterMaster.createDefaultFiltersConfig()));
        Core.setSegmenter(new Segmenter(SRX.getDefault()));
        AlignFilePickerController picker = new AlignFilePickerController();
        if (args.length == 4) {
            picker.sourceLanguage = new Language(args[0]);
            picker.sourceFile = args[1];
            picker.targetLanguage = new Language(args[2]);
            picker.targetFile = args[3];
        }
        picker.show(null);
    }
}
