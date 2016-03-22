/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.align;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.LanguageComboBoxRenderer;
import org.omegat.util.gui.StaticUIUtils;

/**
 * @author Aaron Madlon-Kay
 */
public class AlignFilePickerController {
    String sourceFile;
    String targetFile;

    String sourceDefaultDir;
    String targetDefaultDir;
    String defaultSaveDir;

    Language sourceLanguage = Language.LANGUAGES[0];
    Language targetLanguage = Language.LANGUAGES[Language.LANGUAGES.length - 1];

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public void setTargetFile(String targetFile) {
        this.targetFile = targetFile;
    }

    public void setSourceLanguage(Language sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public void setTargetLanguage(Language targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public void setSourceDefaultDir(String sourceDefaultDir) {
        this.sourceDefaultDir = sourceDefaultDir;
    }

    public void setTargetDefaultDir(String targetDefaultDir) {
        this.targetDefaultDir = targetDefaultDir;
    }

    public void setDefaultSaveDir(String defaultSaveDir) {
        this.defaultSaveDir = defaultSaveDir;
    }

    @SuppressWarnings("serial")
    public void show(final Component parent) {
        final JFrame frame = new JFrame(OStrings.getString("ALIGNER_FILEPICKER"));
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        StaticUIUtils.setEscapeClosable(frame);

        final AlignFilePicker picker = new AlignFilePicker();
        picker.sourceLanguagePicker.setModel(new DefaultComboBoxModel<>(Language.LANGUAGES));
        picker.sourceLanguagePicker.setRenderer(new LanguageComboBoxRenderer());
        picker.sourceLanguagePicker.setSelectedItem(sourceLanguage);
        picker.sourceLanguagePicker.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
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
                                OStrings.getString("NP_INVALID_SOURCE_LOCALE")
                                        + OStrings.getString("NP_LOCALE_SUGGESTION"),
                                OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
                        picker.sourceLanguagePicker.requestFocusInWindow();
                    }
                } else if (e.getItem() instanceof Language) {
                    sourceLanguage = (Language) e.getItem();
                } else {
                    throw new IllegalArgumentException();
                }
                updatePicker(picker);
            }
        });
        picker.targetLanguagePicker.setModel(new DefaultComboBoxModel<>(Language.LANGUAGES));
        picker.targetLanguagePicker.setRenderer(new LanguageComboBoxRenderer());
        picker.targetLanguagePicker.setSelectedItem(targetLanguage);
        picker.targetLanguagePicker.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
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
                                OStrings.getString("NP_INVALID_TARGET_LOCALE")
                                        + OStrings.getString("NP_LOCALE_SUGGESTION"),
                                OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
                        picker.targetLanguagePicker.requestFocusInWindow();
                    }
                } else if (e.getItem() instanceof Language) {
                    targetLanguage = (Language) e.getItem();
                } else {
                    throw new IllegalArgumentException();
                }
                updatePicker(picker);
            }
        });
        picker.sourceChooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File file = chooseFile(frame, OStrings.getString("ALIGNER_FILEPICKER_CHOOSE_SOURCE"),
                        StringUtil.isEmpty(sourceFile) ? sourceDefaultDir : sourceFile);
                if (file != null) {
                    sourceDefaultDir = file.getParent();
                    targetDefaultDir = targetDefaultDir == null ? sourceDefaultDir : targetDefaultDir;
                    defaultSaveDir = defaultSaveDir == null ? sourceDefaultDir : defaultSaveDir;
                    picker.sourceLanguageFileField.setText(file.getAbsolutePath());
                }
            }
        });
        picker.targetChooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File file = chooseFile(frame, OStrings.getString("ALIGNER_FILEPICKER_CHOOSE_TARGET"),
                        StringUtil.isEmpty(targetFile) ? targetDefaultDir : targetFile);
                if (file != null) {
                    targetDefaultDir = file.getParent();
                    sourceDefaultDir = sourceDefaultDir == null ? targetDefaultDir : sourceDefaultDir;
                    defaultSaveDir = defaultSaveDir == null ? targetDefaultDir : defaultSaveDir;
                    picker.targetLanguageFileField.setText(file.getAbsolutePath());
                }
            }
        });
        picker.sourceLanguageFileField.setText(sourceFile);
        picker.sourceLanguageFileField.getDocument().addDocumentListener(new DocumentListener() {
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
                sourceFile = picker.sourceLanguageFileField.getText();
                updatePicker(picker);
            }
        });
        picker.targetLanguageFileField.setText(targetFile);
        picker.targetLanguageFileField.getDocument().addDocumentListener(new DocumentListener() {
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
                targetFile = picker.targetLanguageFileField.getText();
                updatePicker(picker);
            }
        });

        TransferHandler transferHandler = new TransferHandler() {
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
                    List<?> list = (List<?>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
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
        picker.sourceLanguageFileField.setTransferHandler(transferHandler);
        picker.targetLanguageFileField.setTransferHandler(transferHandler);

        picker.setTransferHandler(new TransferHandler() {
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
                    List<?> list = (List<?>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
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
        });

        picker.okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                picker.progressBar.setVisible(true);
                new SwingWorker<Aligner, Object>() {
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
                            new AlignPanelController(aligner, defaultSaveDir).show(parent);
                        } catch (CancellationException e) {
                            // Ignore
                        } catch (Exception e) {
                            Log.log(e);
                            JOptionPane.showMessageDialog(frame, OStrings.getString("ALIGNER_ERROR_LOADING"),
                                    OStrings.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
                        }
                        frame.dispose();
                    }
                }.execute();
            }
        });
        picker.cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });

        picker.progressBar.setVisible(false);

        frame.getRootPane().setDefaultButton(picker.okButton);

        updatePicker(picker);

        frame.add(picker);
        frame.pack();
        frame.setLocationRelativeTo(parent);
        frame.setVisible(true);
    }

    private void updatePicker(final AlignFilePicker picker) {
        if (sourceFile == null || targetFile == null || sourceLanguage == null || targetLanguage == null) {
            picker.okButton.setEnabled(false);
            return;
        }
        final File srcFile = new File(sourceFile);
        final File trgFile = new File(targetFile);
        if (!srcFile.isFile() || !trgFile.isFile() || srcFile.equals(trgFile)) {
            picker.okButton.setEnabled(false);
            return;
        }
        new SwingWorker<Boolean, Object>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                FilterMaster fm = Core.getFilterMaster();
                return fm.isFileSupported(srcFile, false) && fm.isFileSupported(trgFile, false);
            }
            @Override
            protected void done() {
                boolean enabled = false;
                try {
                    enabled = get();
                } catch (CancellationException e) {
                    // Ignore
                } catch (Exception e) {
                    Log.log(e);
                }
                picker.okButton.setEnabled(enabled);
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

    static File chooseFile(Component parent, String title, String dir) {
        JFileChooser chooser = new JFileChooser(dir);
        chooser.setDialogTitle(title);
        chooser.setFileFilter(new FileFilter() {
            @Override
            public String getDescription() {
                return OStrings.getString("ALIGNER_FILEPICKER_SUPPORTEDFILES");
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

    public static void main(String[] args) throws Exception {
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        PluginUtils.loadPlugins(Collections.<String, String> emptyMap());
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
