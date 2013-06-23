/*
 *  Global spell checking
 *
 * @author  Piotr Kulik
 * @date    2013-06-23
 * @version 0.1
 */
 
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
import java.awt.event.*
import static javax.swing.JOptionPane.*
import static org.omegat.util.Platform.*
import java.awt.BorderLayout as BL
import java.util.HashSet
import java.util.Set
import org.omegat.gui.glossary.GlossaryEntry


def prop = project.projectProperties
if (!prop) {
  final def title = 'Spellchecker'
  final def msg   = 'Please try again after you open a project.'
  showMessageDialog null, msg, title, INFORMATION_MESSAGE
  return
}

useGlossary = false;

class SpellcheckerData {
	@Bindable data = [];
}

//model = new SpellcheckerData()


def spellcheck() {
	Set<String> glossary1 = new HashSet<String>();
	
	if (useGlossary) {
		List<GlossaryEntry> glossEntries = org.omegat.core.Core.getGlossaryManager().getGlossaryEntries("");
		for (i in 0 ..< glossEntries.size()) {
			terms = glossEntries[i].getLocTerms(true)

			for (j in 0 ..< terms.size()) {
				term = terms[j]
				for (org.omegat.util.Token tok in org.omegat.core.Core.getProject().getTargetTokenizer().tokenizeWordsForSpelling(term)) {
					int st = tok.getOffset();
					int en = tok.getOffset() + tok.getLength();
					String word = term.substring(st, en);
					if (!org.omegat.core.Core.getSpellChecker().isCorrect(word)) {
						glossary1.add(word);
					}
				}
			}
		}
	}

	model = new SpellcheckerData()

	files = project.projectFiles;
	
	for (i in 0 ..< files.size()) {
		fi = files[i];
		
		for (j in 0 ..< fi.entries.size()) {
			ste = fi.entries[j];
			source = ste.getSrcText();
			target = project.getTranslationInfo(ste) ? project.getTranslationInfo(ste).translation : null;
			
			if ( target == null ) {
				continue;
			}

			for (org.omegat.util.Token tok in org.omegat.core.Core.getProject().getTargetTokenizer().tokenizeWordsForSpelling(target)) {
				int st = tok.getOffset();
				int en = tok.getOffset() + tok.getLength();
				String word = target.substring(st, en);
				if (!glossary1.contains(word)) {
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

def interfejs(locationxy = new Point(0, 0), width = 500, height = 550, scrollpos = 0) {
	def frame
	frame = swing.frame(title:'Spellchecker', minimumSize: [width, height], pack: true, show: true) {
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
					propertyColumn(editable: false, header:'Target',propertyName:'target', minWidth: 200, preferredWidth: 250)
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
		skroll.getVerticalScrollBar().setValue(scrollpos);
		tab.scrollRectToVisible(new Rectangle (0, scrollpos, 1, scrollpos + 1));
		skroll.repaint()
		button(text:'Refresh',
			 actionPerformed: {
				spellcheck();
				locationxy = frame.getLocation();
				sizerw = frame.getWidth();
				sizerh = frame.getHeight();
				skropos = skroll.getVerticalScrollBar().getValue()
				frame.setVisible(false);
				frame.dispose();
				interfejs(locationxy, sizerw, sizerh, skropos)},
			 constraints:BL.SOUTH)
	}

	frame.setLocation(locationxy);
}

spellcheck()
interfejs()