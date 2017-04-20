/*
 *  Simple search and replace script
 *
 * @author  Didier Briel
 * @author  Briac Pilpre
 * @date    2016-07-20
 * @version 0.5
 */

 // The two following lines are there to allow the script to build a nice GUI for the text interface.
 // SwingBuilder is a Groovy class used to program GUI, nearly as easily as a HTML form. 
import groovy.swing.SwingBuilder
// BorderLayout -- shortened as 'BL' to be less verbose in the script -- is a Java component used to
// tell the GUI where to put the different elements.
import java.awt.BorderLayout as BL

// The @Bindable annotation is required to take into accounts the values in the form 
import groovy.beans.Bindable

// The SearchReplaceForm class is here to hold the data from the form.
// search and replace are two fields representing the text to search and to replace, 
// respectively. These fields are used by the SwingBuilder component which will 
// update the variables when the user changes them in the GUI. 
@Bindable
class FormData { String search, replace }

def gui() { // Since the script will call the Editor, we must wrap it in the gui() function
// The doReplace function above currently does nothing on its own. We will
// build a simple GUI to allow the user to enter the search and replace strings,
// and a button to launch the replacement.
def search_string  = "" // You can use these two variables to pre-define the values
def replace_string = "" // of the search and replace fields

def formData = new FormData()
formData.search = search_string
formData.replace = replace_string

new SwingBuilder().edt {
	frame(title:res.getString("name"), size: [350, 200], show: true) {
		borderLayout(vgap: 5)

		panel(constraints: BL.CENTER,
		border: compoundBorder([
			emptyBorder(10),
			titledBorder(res.getString("description"))
		])) {
	        // Just as in a HTML form, we can use a Table layout
	        // To with labels and Textfields.
			tableLayout {
				tr {
					td { label res.getString("search") }
					td { textField id:"searchField", text:formData.search, columns: 20  }
				}
				tr {
					td { label res.getString("replace") }
					td { textField id:"replaceField", text:formData.replace, columns: 20  }
				}
				tr {
					td { label "" }
					td {
						// When the button is clicked, the doReplace function will be called
						button(text:res.getString("button"),
						actionPerformed: { doReplace(formData.search, formData.replace) },
						constraints:BL.SOUTH)
					}
				}
			}

	 		// Binding of textfields to the formData object.
        		bean formData,
            		search:  bind { searchField.text },
            		replace: bind { replaceField.text }
		}
	}
}

}

// doReplace is a Groovy function. It will do the actual work of searching and replacing text. Note that the
// search_string and replace_string are the parameters of the function, and are not the same as the two 
// previously defined variables.
def doReplace(search_string, replace_string) {
	// The segment_count variable will be incremented each time a segment is modified.
	def segment_count = 0

	// "console" is an object from the OmegaT application. It represents the logging window, at the bottom of the 
	// script interface. It has only three methods: print, println and clear.
	// "res" is the ResourceBundle use to localize the script in different language.
	console.println(res.getString("description"));

	// Like "console", "project" is binded from OmegaT. This is the currently opened project.
	// The allEntries member will return all the segments of the project, and each segment will
	// be processed by the Groovy function between { ... }.
	// The current processed segment is named "ste" (for SourceTextEntry).
	project.allEntries.each { ste ->

		if (java.lang.Thread.interrupted()) { // If the Cancel button is pressed
          	throw new Exception("Cancel")    // We exit the "each" loop with an Exception
  		}
		
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

	console.println(res.getString("modified_segments") + segment_count);
}
