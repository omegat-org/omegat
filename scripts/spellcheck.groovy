/* :name=Spellcheck :description=Global Spell Checking
 *
 * Global spell checking
 *
 * @author  Piotr Kulik
 * @date    2014-05-07
 * @version 0.2
 *
 * Changes:
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
// if TRUE specified escaped characters will be removed from target before tokenization
removeEscapedCharacters = false
ESCAPED_CHARACTERS_REGEX = "\\[\\abfnrtv]"
// if TRUE specified mnemonic chars will be removed from target before tokenization
removeMnemonicChars = false
MNEMONIC_CHARACTERS = "^.+[]{}()&|-:=!<>"

import groovy.swing.SwingBuilder
import groovy.beans.Bindable
import java.awt.Component
import java.awt.Point
import java.awt.Rectangle
import java.awt.Dimension
import javax.swing.JButton
import javax.swing.JTable
import javax.swing.table.*
import javax.swing.event.*
import javax.swing.RowSorter.SortKey
import javax.swing.RowSorter
import javax.swing.SortOrder
import java.awt.event.*
import static javax.swing.JOptionPane.*
import static org.omegat.util.Platform.*
import java.awt.BorderLayout as BL
import java.awt.GridBagConstraints
import java.util.HashSet
import java.util.Set
import org.omegat.gui.glossary.GlossaryEntry
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SpellcheckerData {
	@Bindable data = [];
}

ESCAPED_CHARACTERS_PATTERN = Pattern.compile(ESCAPED_CHARACTERS_REGEX, 0)
MNEMONIC_CHARACTERS_PATTERN_LIST = new ArrayList<Pattern>();
for (int i = 0; i < MNEMONIC_CHARACTERS.length(); i++) {
	MNEMONIC_CHARACTERS_PATTERN_LIST.add(Pattern.compile("\\" + MNEMONIC_CHARACTERS.charAt(i), 0));
}

def cleanupTarget(String text) {
	if (removeEscapedCharacters) {
		text = ESCAPED_CHARACTERS_PATTERN.matcher(text).replaceAll("")
	}

	if (removeMnemonicChars) {
		for (int i = 0; i < MNEMONIC_CHARACTERS_PATTERN_LIST.size(); i++) {
			text = text.replaceAll(MNEMONIC_CHARACTERS_PATTERN_LIST[i], "");
		}
	}

	return text
}

public class IntegerComparator implements Comparator<Integer> {
	public int compare(Integer o1, Integer o2) {
		return o1 - o2;
	}
}

def prop = project.projectProperties
if (!prop) {
  final def title = 'Spellchecker'
  final def msg   = 'Please try again after you open a project.'
  showMessageDialog null, msg, title, INFORMATION_MESSAGE
  return
}

def spellcheck() {
	Set<String> glossary1 = new HashSet<String>();
	
	if (useGlossary) {
		List<GlossaryEntry> glossEntries = org.omegat.core.Core.getGlossaryManager().getGlossaryEntries("");
		for (i in 0 ..< glossEntries.size()) {
			terms = glossEntries[i].getLocTerms(true)

			for (j in 0 ..< terms.size()) {
				term = terms[j]
				for (org.omegat.util.Token tok in org.omegat.core.Core.getProject().getTargetTokenizer().tokenizeWordsForSpelling(term)) {
					String word = tok.getTextFromString(term);
					if (!org.omegat.core.Core.getSpellChecker().isCorrect(word)) {
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
			
			if ( target == null ) {
				continue;
			}

			target = cleanupTarget(target)

			for (org.omegat.util.Token tok in org.omegat.core.Core.getProject().getTargetTokenizer().tokenizeWordsForSpelling(target)) {
				String word = tok.getTextFromString(target);
				if (!glossary1.contains(ignoreGlossaryCase ? word.toLowerCase() : word)) {
					if (!org.omegat.core.Core.getSpellChecker().isCorrect(word)) {
						model.data.add([ seg: ste.entryNum(), target: word, ignore: true, learn: true]);
					}
				}
			}
		}
	}

	console.println("Errors found : " + model.data.size());
	println("Errors found : " + model.data.size());
}

swing = new SwingBuilder()

def interfejs(locationxy = new Point(0, 0), width = 500, height = 550, scrollpos = 0, sortColumn = defaultSortColumn, sortOrderDescending = defaultSortOrderDescending) {
	def frame
	frame = swing.frame(title:'Spellcheck - errors found: ' + model.data.size(), minimumSize: [width, height], pack: true, show: true) {
		def tab
		def skroll
		skroll = scrollPane {
			tab = table() {
				tableModel(list: model.data) {
					propertyColumn(editable: true, header:'Segment', propertyName:'seg', minWidth: 80, maxWidth: 80, preferredWidth: 80,
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
					propertyColumn(editable: false, header:'Target', propertyName:'target', minWidth: 200, preferredWidth: 250)
					propertyColumn(editable: true, header:'Ignore', propertyName:'ignore', minWidth: 80, maxWidth: 80, preferredWidth: 80,
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
												table.setValueAt("IGNORED: " + svalue, i, 1);
												table.setValueAt(false, i, 2)
												table.setValueAt(false, i, 3)
											}
										}
										table.repaint();
										org.omegat.core.Core.getSpellChecker().ignoreWord(svalue);
//										org.omegat.core.Core.getSpellChecker().saveWordLists();
										org.omegat.core.Core.getEditor().remarkOneMarker(org.omegat.core.spellchecker.SpellCheckerMarker.class.getName());
										println("IGNORED: " + svalue);
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
										btn.setText('Ignore')
										return btn;
									}
									return null;
								}
							}
							)
					propertyColumn(editable: true, header:'Learn', propertyName:'learn', minWidth: 80, maxWidth: 80, preferredWidth: 80,
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
												table.setValueAt("LEARNED: " + svalue, i, 1);
												table.setValueAt(false, i, 2)
												table.setValueAt(false, i, 3)
											}
										}
										table.repaint();
										org.omegat.core.Core.getSpellChecker().learnWord(svalue);
//										org.omegat.core.Core.getSpellChecker().saveWordLists();
										org.omegat.core.Core.getEditor().remarkOneMarker(org.omegat.core.spellchecker.SpellCheckerMarker.class.getName());
										println("LEARNED: " + svalue);
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
										btn.setText('Learn')
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
			checkBox(text:'Check whole project',
				selected: checkWholeProject,
				actionPerformed: {
					checkWholeProject = !checkWholeProject;
				},
				constraints:gbc(gridx:0, gridy:0, weightx: 0.5, fill:GridBagConstraints.HORIZONTAL, insets:[5,5,0,0]))
			checkBox(text:'Use glossary',
				selected: useGlossary,
				actionPerformed: {
					useGlossary = !useGlossary;
				},
				constraints:gbc(gridx:1, gridy:0, weightx: 0.5, fill:GridBagConstraints.HORIZONTAL, insets:[5,5,0,0]))
			checkBox(text:'Ignore glossary case',
				selected: ignoreGlossaryCase,
				actionPerformed: {
					ignoreGlossaryCase = !ignoreGlossaryCase;
				},
				constraints:gbc(gridx:2, gridy:0, weightx: 0.5, fill:GridBagConstraints.HORIZONTAL, insets:[5,5,0,5]))
			checkBox(text:'Remove escaped',
				selected: removeEscapedCharacters,
				actionPerformed: {
					removeEscapedCharacters = !removeEscapedCharacters;
				},
				constraints:gbc(gridx:0, gridy:1, weightx: 0.5, fill:GridBagConstraints.HORIZONTAL, insets:[5,5,0,0]))
			checkBox(text:'Remove mnemonics',
				selected: removeMnemonicChars,
				actionPerformed: {
					removeMnemonicChars = !removeMnemonicChars;
				},
				constraints:gbc(gridx:1, gridy:1, weightx: 0.5, fill:GridBagConstraints.HORIZONTAL, insets:[5,5,0,5]))
			button(text:'Refresh',
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
				 constraints:gbc(gridx:0, gridy:2, gridwidth:3, weightx:1.0, fill:GridBagConstraints.HORIZONTAL, insets:[5,5,5,5]))
		}
	}

	frame.setLocation(locationxy);
}

spellcheck()
interfejs()