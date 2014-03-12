/*
 *  Simple search and replace script
 *
 * @author  Didier Briel
 * @author  Briac Pilpre
 * @date    2014-03-13
 * @version 0.2
 */
import groovy.swing.SwingBuilder
import java.awt.BorderLayout as BL

def search_string  = ""
def replace_string = ""

new SwingBuilder().edt {
  frame(title:"Search and Replace", size: [ 350, 200], show: true) {
  borderLayout(vgap: 5)

  panel(constraints: BL.CENTER,
    border: compoundBorder([emptyBorder(10), titledBorder('Simple search and replace')])) {
      tableLayout {
        tr {
           td { label "Search: " }
           td { textField search_string, columns: 20  }
        }
        tr {
           td { label "Replace: " }
           td { textField replace_string, columns: 20  }
        }
        tr {
           td { label "" }
           td {
              button(text:'Search & Replace',
                 actionPerformed: { doReplace(search_string, replace_string) },
                 constraints:BL.SOUTH)
           }
        }
      }
    }
  }
}

def doReplace(search_string, replace_string) {
  def segment_count = 0

  console.println("Do a search and replace.\n");

  project.allEntries.each { ste ->
    source = ste.getSrcText();
    target = project.getTranslationInfo(ste) ? project.getTranslationInfo(ste).translation : null;
    initial_target = target

    // Skip untranslated segments
    if (target == null) return

    target = target.replaceAll(search_string, replace_string)

    if (initial_target != target) {
      segment_count++
      editor.gotoEntry(ste.entryNum())
      console.println(ste.entryNum() + "\t" + ste.srcText + "\t" + target )
      editor.replaceEditText(target)
    }
  }

  console.println("Segments modified: " + segment_count);
}
