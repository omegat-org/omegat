/*
 *  Display a list of segments where source and target identical content
 *
 * @author  Kos Ivantsov
 * @date    2013-11-29
 * @version 0.1
 */

import groovy.swing.SwingBuilder
import java.awt.Component
import javax.swing.JButton
import javax.swing.JTable
import javax.swing.table.*
import javax.swing.event.*
import java.awt.event.*
import java.awt.BorderLayout as BL

def prop = project.projectProperties
if (!prop) {
  final def title = 'Show same segments'
  final def msg   = 'Please try again after you open a project.'
  showMessageDialog null, msg, title, INFORMATION_MESSAGE
  return
}


data = []

files = project.projectFiles;
segment_count=0

for (i in 0 ..< files.size())
{
    fi = files[i];

    console.println(fi.filePath);
    for (j in 0 ..< fi.entries.size()) {
        ste = fi.entries[j];
        source = ste.getSrcText();
        target = project.getTranslationInfo(ste) ? project.getTranslationInfo(ste).translation : null;

        if ( source == target) {
            data.add([ seg: ste.entryNum(), source: source, target: target ])
            console.println(ste.entryNum() + "\t" + source + "\t" + target);
            segment_count++;
        }
    }
}

swing = new SwingBuilder()

frame = swing.frame(title:'Same Segments', pack: true, show: true, preferredSize: [720, 500]) {
    scrollPane {
        table() {
            tableModel(list:data) {
                propertyColumn(editable: true, header:'Segment', propertyName:'seg', minWidth: 80, maxWidth: 80, preferredWidth: 80,
                        cellEditor: new TableCellEditor() {
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
                propertyColumn(editable: false, header:'Source',propertyName:'source', preferredWidth: 200)
                propertyColumn(editable: false, header:'Target',propertyName:'target', preferredWidth: 200)
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
