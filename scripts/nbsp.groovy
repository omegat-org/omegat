/* :name=Non-breaking space :description=Replace spaces with non-breakable spaces where appropriate in French
 *
 * @author  Didier Briel
 * @author  Briac Pilpre
 * @date    2016-07-19
 * @version 0.5
 */

// search_string_before and replace_string_before are two variables representing the text to search and to replace, 
// respectively. search_string_after and replace_string_after have the same function
def gui() {
def search_string_before  = /\s([:?!;»])/
def replace_string_before = /\u00A0$1/
def search_string_percent = /(\d)\s%/
def replace_string_percent = /$1\u00A0%/
def search_string_after = /«\s/
def replace_string_after = /«\u00A0/

def fix_apostrophes = false;

// The segment_count variable will be incremented each time a segment is modified.
def segment_count = 0
// cur_num stores the current segment number
def cur_num = editor.getCurrentEntry().entryNum()

project.allEntries.each { ste ->

  if (java.lang.Thread.interrupted()) {
          throw new Exception("Cancel")
  }
  source = ste.getSrcText();
  // If the segment has been translated, we get store translated text in the target variable.
  target = project.getTranslationInfo(ste) ? project.getTranslationInfo(ste).translation : null;
      
  // The translated text is copied to be able to compare it before and after the text replacement and
  // determine if the segment was modified.
  initial_target = target

  // Skip untranslated segments
  if ( target == null || target.length()== 0) return

  // The search_string is replaced by the replace_string in the translated text. 
  target = target.replaceAll(search_string_before, replace_string_before)
  target = target.replaceAll(search_string_percent, replace_string_percent)
  target = target.replaceAll(search_string_after, replace_string_after)

  if (fix_apostrophes) {
  	target = target.replaceAll(/'/, "’")
  }

  // The old translation is checked against the replaced text, if it is different,
  // we jump to the segment number and replace the old text by the new one.
  // "editor" is the OmegaT object used to manipulate the main OmegaT user interface.
  if (initial_target != target) {
    segment_count++
    // Jump to the segment number
    editor.gotoEntry(ste.entryNum())
    console.println(ste.entryNum() + "\t" + initial_target + "\t" + target )
    // Replace the translation
    editor.replaceEditText(target)
  }
 
}

 // We return to the initial segment
 editor.gotoEntry(cur_num)
 console.println("modified_segments: " + segment_count);
}