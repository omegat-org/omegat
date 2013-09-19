/*
 *  QA script
 *
 * @author  Briac Pilpre
 * @author  Kos Ivantsov
 * @author  Didier Briel
 * @date    2013-06-23
 * @version 0.2
 */

import groovy.swing.SwingBuilder
import java.awt.Component
import javax.swing.JButton
import javax.swing.JTable
import javax.swing.table.*
import javax.swing.event.*
import java.awt.event.*
import javax.swing.JOptionPane.*
import org.omegat.util.Platform.*
import java.awt.BorderLayout as BL
/*
 The rules are based on the Checkmate Quality check 
 http://www.opentag.com/okapi/wiki/index.php?title=CheckMate_-_Quality_Check_Configuration
 Each rule is a block of groovy code, 'source' and 'target' are the two parameters of this block
 */

def prop = project.projectProperties
if (!prop) {
  final def title = 'Check rules'
  final def msg   = 'Please try again after you open a project.'
  showMessageDialog null, msg, title, INFORMATION_MESSAGE
  return
}

data=[]
console.println("Check rules.\n");

// Prefs
maxCharLengthAbove=240
minCharLengthAbove=40

rules = [
            
            // Text unit verification
            targetLeadingWhiteSpaces: { s, t ->  t =~ /^\s+/ },
            targetTrailingWhiteSpaces: { s, t -> t =~ /\s+$/ },
            // Segment verification
            doubledWords: { s, t -> t =~ /(?i)(\b\w+)\s+\1\b/ },
            doubledBlanks: { s, t -> t =~ /[\s ]{2}/ },
           // Length
           targetShorter: { s, t -> (t.length() / s.length() * 100) < minCharLengthAbove },
           targetLonger: { s, t -> (t.length() / s.length() * 100) > maxCharLengthAbove }
        ];

segment_count = 0;

files = project.projectFiles;

for (i in 0 ..< files.size()) {
    fi = files[i];
    
    //console.println(fi.filePath);
    for (j in 0 ..< fi.entries.size()) {
        ste = fi.entries[j];
        source = ste.getSrcText();
        target = project.getTranslationInfo(ste) ? project.getTranslationInfo(ste).translation : null;
        
        if ( target == null ) {
            continue;
        }
        
        rules.each { k, v ->
            if (rules[k](source, target)) {
                console.println(ste.entryNum() + "\t" + k + /*"\t[" + source + "]" + */"\t[" + target + "]");
                data.add([ seg: ste.entryNum(), rule: k, source: source, target: target ]);	
                segment_count++;
            }
        }
    }
}

console.println("Segments found : " + segment_count);


swing = new SwingBuilder()

frame = swing.frame(title:'Check rules', preferredSize: [800, 500]) {
    scrollPane {
        table() {
            tableModel(list:data) {
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
                                println("value: " + value);
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
                propertyColumn(editable: false, header:'Rule',propertyName:'rule', minWidth: 120, maxWidth: 200, preferredWidth: 150)
                propertyColumn(editable: false, header:'Source',propertyName:'source', minWidth: 150, preferredWidth: 350)
                propertyColumn(editable: false, header:'Target',propertyName:'target', minWidth: 150, preferredWidth: 350)
            }
        }
        
    }
     panel(constraints: BL.SOUTH){
            button('Quit', actionPerformed:{
                frame.visible = false
            })
		}
}
frame.pack()
frame.show()
