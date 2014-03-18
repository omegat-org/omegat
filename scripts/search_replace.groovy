/*
 *  Simple search and replace script
 *
 * @author  Didier Briel
 * @author  Briac Pilpre
 * @date    2014-03-13
 * @version 0.3
 */

 // The two following lines are there to allow the script to build a nice GUI for the text interface.
 // SwingBuilder is a Groovy class used to program GUI, nearly as easily as a HTML form. 
import groovy.swing.SwingBuilder
// BorderLayout -- shortened as 'BL' to be less verbose in the script -- is a Java component used to
// tell the GUI where to put the different elements.
import java.awt.BorderLayout as BL

// search_string and replace_string are two variables representing the text to search and to replace, 
// respectively. These variables are used by the SwingBuilder component which will update the variables 
// when the user changes them in the GUI. 
def search_string  = ""
def replace_string = ""

// doReplace is a Groovy function. It will do the actual work of searching and replacing text. Note that the
// search_string and replace_string are the parameters of the function, and are not the same as the two 
// previously defined variables.
def doReplace(search_string, replace_string) {
	// The segment_count variable will be incremented each time a segment is modified.
	def segment_count = 0

	// "console" is an object from the OmegaT application. It represents the logging window, at the bottom of the 
	// script interface. It has only three methods: print, println and clear.
	console.println("Simple search and replace script");

	// Like "console", "project" is binded from OmegaT. This is the currently opened project.
	// The allEntries member will return all the segments of the project, and each segment will
	// be processed by the Groovy function between { ... }.
	// The current processed segment is named "ste" (for SourceTextEntry).
	project.allEntries.each { ste ->
		// Each segment has a source text, stored here in the source variable.
		source = ste.getSrcText();
		// If the segment has been translated, we get store translated text in the target variable.
		target = project.getTranslationInfo(ste) ? project.getTranslationInfo(ste).translation : null;
		
		// The translated text is copied to be able to compare it before and after the text replacement and
		// determine if the segment was modified.
		initial_target = target

		// Skip untranslated segments
		if (target == null) return

		// The search_string is replaced by the replace_string in the translated text. 
		target = target.replaceAll(search_string, replace_string)

		// The old translation is checked against the replaced text, if it is different,
		// we jump to the segment number and replace the old text by the new one.
		// "editor" is the OmegaT object used to manipulate the main OmegaT user interface.
		if (initial_target != target) {
			segment_count++
			// Jump to the segment number
			editor.gotoEntry(ste.entryNum())
			console.println(ste.entryNum() + "\t" + ste.srcText + "\t" + target )
			// Replace the translation
			editor.replaceEditText(target)
		}
	}

	console.println("Modified Segments:") + segment_count;
}


// The doReplace function above currently does nothing on its own. We will
// build a simple GUI to allow the user to enter the search and replace strings,
// and a button to launch the replacement.

new SwingBuilder().edt {
	frame(title:"Example - Search and Replace", size: [350, 200], show: true) {
		borderLayout(vgap: 5)

		panel(constraints: BL.CENTER,
		border: compoundBorder([
			emptyBorder(10),
			titledBorder("Simple search and replace script")
		])) {
	        // Just as in a HTML form, we can use a Table layout
	        // To with labels and Textfields.
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
						// When the button is clicked, the doReplace function will be called
						button(text: "Search and Replace",
						actionPerformed: { doReplace(search_string, replace_string) },
						constraints:BL.SOUTH)
					}
				}
			}
		}
	}
}

