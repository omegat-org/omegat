/* :name=Spellcheck :description=Global spell checking
 *
 * Global spell checking
 *
 * @author  Piotr Kulik
 * @date    2015-10-03
 * @version 0.5
 *
 * Changes since 0.4:
 * - update to current changes in Tokenizers code
 * - added possibility to skip segments where target is the same as source
 *
 * Changes since 0.3:
 * - added localization
 *
 * Changes since 0.2:
 * - fragments defined for removal in Tag Verification are always removed first
 * - much shorter mnemonic chars list which works better with Tokenizers and
 *   produces less unwanted results
 * - escaped sequences are now replaced with space instead of removing
 * - added possibility to replace custom tags with space
 * - added possibility to remove of OmegaT-like tags with lower case letter
 *   before and after it
 * - one regex instead of list for mnemonic chars removal
 *
 * Changes since 0.1:
 * - added possibility to sort Segment and Target columns with way to set defaults in script
 * - added possibility to remove from translation escaped and mnemonic characters (user defined)
 * - when using glossary as source of correct terms, character case can be ignored
 * - added possibility to check only current file
 */

// if FALSE only current file will be checked
checkWholeProject = true
// if TRUE terms in glossary will be treated as correct and not reported
useGlossary = false;
// if TRUE terms in glossary will be checked in non case sensitive way
ignoreGlossaryCase = true;
// 0 - segment number, 1 - word
defaultSortColumn = 0;
// if TRUE column will be sorted in reverse
defaultSortOrderDescending = false
// if TRUE specified escaped characters in target will be replaced with space before tokenization
replaceEscapedCharacters = false
ESCAPED_CHARACTERS_REGEX = "\\\\[abfnrtv]"
// if TRUE specified mnemonic chars will be removed from target before tokenization
removeMnemonicChars = true
MNEMONIC_CHARACTERS = "&~"
// if TRUE OmegaT-like tags between two lower case letters will be removed from target before tokenization
removeOmegaTags = true
// if TRUE user defined tags in target will be replaced with space before tokenization
replaceCustomTags = true
// if TRUE fragments defined for removal will be removed
removeDefinedFragments = true
// if TRUE segments where source=target will be removed
skipIdenticalSegments = false

import groovy.swing.SwingBuilder
import groovy.beans.Bindable
import javax.swing.JButton
import javax.swing.JTable
import javax.swing.table.*
import javax.swing.event.*
import javax.swing.RowSorter.SortKey
import javax.swing.RowSorter
import javax.swing.SortOrder
import static javax.swing.JOptionPane.*
import java.awt.Component
import java.awt.Point
import java.awt.Rectangle
import java.awt.Dimension
import java.awt.event.*
import java.awt.BorderLayout as BL
import java.awt.GridBagConstraints
import java.util.HashSet
import java.util.Set
import org.omegat.util.Platform.*
import org.omegat.gui.glossary.GlossaryEntry
import org.omegat.util.PatternConsts
import org.omegat.core.Core
import org.omegat.util.Token
import java.util.Comparator
import java.util.regex.Matcher
import java.util.regex.Pattern
import org.omegat.tokenizer.ITokenizer.StemmingMode

class SpellcheckerData {
    @Bindable data = [];
}

def ESCAPED_CHARACTERS_PATTERN
def MNEMONIC_CHARACTERS_PATTERN_LIST
def REMOVE_PATTERN
def CUSTOM_TAG_PATTERN
def OMEGAT_TAG_REPLACE_PATTERN
def OMEGAT_TAG_REMOVE_PATTERN

def prepare() {
    ESCAPED_CHARACTERS_PATTERN = Pattern.compile(ESCAPED_CHARACTERS_REGEX, 0)
    temp = "[";
    for (int i = 0; i < MNEMONIC_CHARACTERS.length(); i++) {
        temp += "\\" + MNEMONIC_CHARACTERS.charAt(i);
    }
    MNEMONIC_CHARACTERS_PATTERN = Pattern.compile(temp + "]");

    REMOVE_PATTERN = PatternConsts.getRemovePattern();
    CUSTOM_TAG_PATTERN = PatternConsts.getCustomTagPattern();

    OMEGAT_TAG_REPLACE_PATTERN = PatternConsts.OMEGAT_TAG;
    OMEGAT_TAG_REMOVE_PATTERN = Pattern.compile("(\\p{L}\\p{M}*)(<\\/?[a-zA-Z]+[0-9]+\\/?>)+(\\p{L}\\p{M}*)");
}

def cleanupTarget(String text) {
    // remove fragments defined for removal in Tag Verification
    if (removeDefinedFragments) {
        if (REMOVE_PATTERN != null) {
            text = REMOVE_PATTERN.matcher(text).replaceAll("");
        }
    }

    // remove OmegaT-like tags which have a lower case letter before and after it
    if (removeOmegaTags) {
        text = OMEGAT_TAG_REMOVE_PATTERN.matcher(text).replaceAll("\$1\$3");
    }
    
    // remove OmegaT-like tags
    //  can be skipped - tokenizers handle such tags properly and break at it
    text = OMEGAT_TAG_REPLACE_PATTERN.matcher(text).replaceAll(" ");

    // replace with space custom tags defined in Tag Verification
    if (replaceCustomTags) {
        if (CUSTOM_TAG_PATTERN != null) {
            text = CUSTOM_TAG_PATTERN.matcher(text).replaceAll(" ");
        }
    }

    // replace escaped characters with space
    if (replaceEscapedCharacters) {
        text = ESCAPED_CHARACTERS_PATTERN.matcher(text).replaceAll(" ");
    }

    // remove menemonic chars
    if (removeMnemonicChars) {
        text = text.replaceAll(MNEMONIC_CHARACTERS_PATTERN, "");
    }

    return text;
}

public class IntegerComparator implements Comparator<Integer> {
    public int compare(Integer o1, Integer o2) {
        return o1 - o2;
    }
}

def prop = project.projectProperties
if (!prop) {
    final def title = res.getString("msgTitle")
    final def msg   = res.getString("msgNoProject")
    showMessageDialog null, msg, title, INFORMATION_MESSAGE
    return
}

def spellcheck() {
    Set<String> glossary1 = new HashSet<String>();
	
    if (useGlossary) {
        List<GlossaryEntry> glossEntries = Core.getGlossaryManager().getGlossaryEntries("");
        for (i in 0 ..< glossEntries.size()) {
            terms = glossEntries[i].getLocTerms(true)

            for (j in 0 ..< terms.size()) {
                term = terms[j]
                for (Token tok in Core.getProject().getTargetTokenizer().tokenizeWords(term, StemmingMode.NONE)) {
                    String word = tok.getTextFromString(term);
                    if (!Core.getSpellChecker().isCorrect(word)) {
                        glossary1.add(ignoreGlossaryCase ? word.toLowerCase() : word);
                    }
                }
            }
        }
    }

    model = new SpellcheckerData()

    files = project.projectFiles;
    if (!checkWholeProject) {
        files = project.projectFiles.subList(editor.@displayedFileIndex, editor.@displayedFileIndex + 1);
    }
	
    for (i in 0 ..< files.size()) {
        fi = files[i];
		
        for (j in 0 ..< fi.entries.size()) {
            ste = fi.entries[j];
            source = ste.getSrcText();
            target = project.getTranslationInfo(ste) ? project.getTranslationInfo(ste).translation : null;
			
            if (target == null) {
                continue;
            }

	    if (skipIdenticalSegments && source == target) {
		continue;
	    }

            target = cleanupTarget(target)

            for (Token tok in Core.getProject().getTargetTokenizer().tokenizeWords(target, StemmingMode.NONE)) {
                String word = tok.getTextFromString(target);
                if (!glossary1.contains(ignoreGlossaryCase ? word.toLowerCase() : word)) {
                    if (!Core.getSpellChecker().isCorrect(word)) {
                        model.data.add([ seg: ste.entryNum(), target: word, ignore: true, learn: true]);
                    }
                }
            }
        }
    }

    console.println(res.getString("logErrorsFound") + model.data.size());
    println(res.getString("logErrorsFound") + model.data.size());
}

swing = new SwingBuilder()

def interfejs(locationxy = new Point(0, 0), width = 500, height = 550, scrollpos = 0, sortColumn = defaultSortColumn, sortOrderDescending = defaultSortOrderDescending) {
    def frame
    frame = swing.frame(title:res.getString("windowTitle") + model.data.size(), minimumSize: [width, height], pack: true, show: true) {
        def tab
        def skroll
        skroll = scrollPane {
            tab = table() {
                tableModel(list: model.data) {
                    propertyColumn(editable: true, header:res.getString("tabHeaderSegment"), propertyName:'seg', minWidth: 80, maxWidth: 80, preferredWidth: 80,
                        cellEditor: new TableCellEditor()
                        {
                            public void cancelCellEditing()                             {}
                            public boolean stopCellEditing()                            {   return false;   }
                            public Object getCellEditorValue()                          {   return value;   }
                            public boolean isCellEditable(EventObject anEvent)          {   return true;    }
                            public boolean shouldSelectCell(EventObject anEvent)        {   return true;   }
                            public void addCellEditorListener(CellEditorListener l)     {}
                            public void removeCellEditorListener(CellEditorListener l)  {}
                            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
                            {
                                org.omegat.core.Core.getEditor().gotoEntry(value);
                            }
								
                        },
                        cellRenderer: new TableCellRenderer()
                        {
                            public Component getTableCellRendererComponent(JTable table,
                                Object value,
                                boolean isSelected,
                                boolean hasFocus,
                                int row,
                                int column)
                            {
                                def btn = new JButton()
                                btn.setText(value.toString())
                                return btn
									
                            }
                        }
                    )
                    propertyColumn(editable: false, header:res.getString("tabHeaderTarget"), propertyName:'target', minWidth: 200, preferredWidth: 250)
                    propertyColumn(editable: true, header:res.getString("tabHeaderIgnore"), propertyName:'ignore', minWidth: 80, maxWidth: 80, preferredWidth: 80,
                        cellEditor: new TableCellEditor()
                        {
                            public void cancelCellEditing()                             {}
                            public boolean stopCellEditing()                            {   return false;   }
                            public Object getCellEditorValue()                          {   return value;   }
                            public boolean isCellEditable(EventObject anEvent)          {   return true;    }
                            public boolean shouldSelectCell(EventObject anEvent)        {   return true;   }
                            public void addCellEditorListener(CellEditorListener l)     {}
                            public void removeCellEditorListener(CellEditorListener l)  {}
                            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
                            {
                                if (value) {
                                    String svalue = table.getValueAt(row, 1)
                                    for (i in 0 ..< table.getRowCount()) {
                                        if (table.getValueAt(i, 1) == svalue) {
                                            table.setValueAt(res.getString("tabTextIgnored") + svalue, i, 1);
                                            table.setValueAt(false, i, 2)
                                            table.setValueAt(false, i, 3)
                                        }
                                    }
                                    table.repaint();
                                    org.omegat.core.Core.getSpellChecker().ignoreWord(svalue);
                                    //										org.omegat.core.Core.getSpellChecker().saveWordLists();
                                    org.omegat.core.Core.getEditor().remarkOneMarker(org.omegat.core.spellchecker.SpellCheckerMarker.class.getName());
                                    println(res.getString("logIgnored") + svalue);
                                }
                            }
								
                        },
                        cellRenderer: new TableCellRenderer()
                        {
                            public Component getTableCellRendererComponent(JTable table,
                                Object value,
                                boolean isSelected,
                                boolean hasFocus,
                                int row,
                                int column)
                            {
                                if (value) {
                                    def btn = new JButton()
                                    btn.setText(res.getString("btnIgnore"))
                                    return btn;
                                }
                                return null;
                            }
                        }
                    )
                    propertyColumn(editable: true, header:res.getString("tabHeaderLearn"), propertyName:'learn', minWidth: 80, maxWidth: 80, preferredWidth: 80,
                        cellEditor: new TableCellEditor()
                        {
                            public void cancelCellEditing()                             {}
                            public boolean stopCellEditing()                            {   return false;   }
                            public Object getCellEditorValue()                          {   return value;   }
                            public boolean isCellEditable(EventObject anEvent)          {   return true;    }
                            public boolean shouldSelectCell(EventObject anEvent)        {   return true;   }
                            public void addCellEditorListener(CellEditorListener l)     {}
                            public void removeCellEditorListener(CellEditorListener l)  {}
                            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
                            {
                                if (value) {
                                    String svalue = table.getValueAt(row, 1)
                                    for (i in 0 ..< table.getRowCount()) {
                                        if (table.getValueAt(i, 1) == svalue) {
                                            table.setValueAt(res.getString("tabTextLearned") + svalue, i, 1);
                                            table.setValueAt(false, i, 2)
                                            table.setValueAt(false, i, 3)
                                        }
                                    }
                                    table.repaint();
                                    org.omegat.core.Core.getSpellChecker().learnWord(svalue);
                                    //										org.omegat.core.Core.getSpellChecker().saveWordLists();
                                    org.omegat.core.Core.getEditor().remarkOneMarker(org.omegat.core.spellchecker.SpellCheckerMarker.class.getName());
                                    println(res.getString("logLearned") + svalue);
                                }
                            }
								
                        },
                        cellRenderer: new TableCellRenderer()
                        {
                            public Component getTableCellRendererComponent(JTable table,
                                Object value,
                                boolean isSelected,
                                boolean hasFocus,
                                int row,
                                int column)
                            {
                                if (value) {
                                    def btn = new JButton()
                                    btn.setText(res.getString("btnLearn"))
                                    return btn;
                                }
                                return null;
                            }
                        }
                    )
                }
            }
            tab.getTableHeader().setReorderingAllowed(false);
        }
        rowSorter = new TableRowSorter(tab.model);
        rowSorter.setComparator(0, new IntegerComparator());
        rowSorter.setSortable(2, false);
        rowSorter.setSortable(3, false);
        sortKeyz = new ArrayList<RowSorter.SortKey>();
        sortKeyz.add(new RowSorter.SortKey(sortColumn, sortOrderDescending ? SortOrder.DESCENDING : SortOrder.ASCENDING));
        rowSorter.setSortKeys(sortKeyz);
        tab.setRowSorter(rowSorter);

        skroll.getVerticalScrollBar().setValue(scrollpos);
        tab.scrollRectToVisible(new Rectangle (0, scrollpos, 1, scrollpos + 1));
        skroll.repaint();
        panel(constraints:BL.SOUTH) {
            gridBagLayout();
            checkBox(text:res.getString("cbCheckWholeProject"),
                selected: checkWholeProject,
                actionPerformed: {
                    checkWholeProject = !checkWholeProject;
                },
                constraints:gbc(gridx:0, gridy:0, weightx: 0.5, fill:GridBagConstraints.HORIZONTAL, insets:[0,5,0,0]))
            checkBox(text:res.getString("cbUseGlossary"),
                selected: useGlossary,
                actionPerformed: {
                    useGlossary = !useGlossary;
                },
                constraints:gbc(gridx:1, gridy:0, weightx: 0.5, fill:GridBagConstraints.HORIZONTAL, insets:[0,5,0,0]))
            checkBox(text:res.getString("cbIgnoreGlossaryCase"),
                selected: ignoreGlossaryCase,
                actionPerformed: {
                    ignoreGlossaryCase = !ignoreGlossaryCase;
                },
                constraints:gbc(gridx:2, gridy:0, weightx: 0.5, fill:GridBagConstraints.HORIZONTAL, insets:[0,5,0,5]))
            checkBox(text:'<html>' + res.getString("cbReplaceEscaped") + '<br> (\\a, \\b, \\f, \\n, \\r, \\t, \\v)</html>',
                selected: replaceEscapedCharacters,
                actionPerformed: {
                    replaceEscapedCharacters = !replaceEscapedCharacters;
                },
                constraints:gbc(gridx:0, gridy:1, weightx: 0.5, fill:GridBagConstraints.HORIZONTAL, insets:[0,5,0,0]))
            checkBox(text:res.getString("cbReplaceCustomTags"),
                selected: replaceCustomTags,
                actionPerformed: {
                    replaceCustomTags = !replaceCustomTags;
                },
                constraints:gbc(gridx:1, gridy:1, weightx: 0.5, fill:GridBagConstraints.HORIZONTAL, insets:[0,5,0,0]))
            checkBox(text:res.getString("cbSkipIdenticalSegments"),
                selected: skipIdenticalSegments,
                actionPerformed: {
                    skipIdenticalSegments = !skipIdenticalSegments;
                },
                constraints:gbc(gridx:2, gridy:1, weightx: 0.5, fill:GridBagConstraints.HORIZONTAL, insets:[0,5,0,5]))
	    checkBox(text:'<html>' + res.getString("cbRemoveOmegaTTags") + '</html>',
                selected: removeOmegaTags,
                actionPerformed: {
                    removeOmegaTags = !removeOmegaTags;
                },
                constraints:gbc(gridx:0, gridy:2, weightx: 0.5, fill:GridBagConstraints.HORIZONTAL, insets:[0,5,0,0]))
            checkBox(text:res.getString("cbRemoveMnemonics") + ' (' + MNEMONIC_CHARACTERS + ')',
                selected: removeMnemonicChars,
                actionPerformed: {
                    removeMnemonicChars = !removeMnemonicChars;
                },
                constraints:gbc(gridx:1, gridy:2, weightx: 0.5, fill:GridBagConstraints.HORIZONTAL, insets:[0,5,0,0]))
            checkBox(text:res.getString("cbRemoveDefinedFragments"),
                selected: removeDefinedFragments,
                actionPerformed: {
                    removeDefinedFragments = !removeDefinedFragments;
                },
                constraints:gbc(gridx:2, gridy:2, weightx: 0.5, fill:GridBagConstraints.HORIZONTAL, insets:[0,5,0,5]))
            button(text:res.getString("btnRefresh"),
                actionPerformed: {
                    spellcheck();
                    locationxy = frame.getLocation();
                    sizerw = frame.getWidth();
                    sizerh = frame.getHeight();
                    skropos = skroll.getVerticalScrollBar().getValue();
                    sort = tab.getRowSorter().getSortKeys()[0];
                    frame.setVisible(false);
                    frame.dispose();
                    interfejs(locationxy, sizerw, sizerh, skropos, sort.getColumn(), sort.getSortOrder() == javax.swing.SortOrder.DESCENDING)},
                constraints:gbc(gridx:0, gridy:3, gridwidth:3, weightx:1.0, fill:GridBagConstraints.HORIZONTAL, insets:[5,5,5,5]))
        }
    }

    frame.setLocation(locationxy);
}

prepare()
spellcheck()
interfejs()