/*
 *  Simple search and replace script
 *
 * @author  Didier Briel
 * @date    2012-02-17
 * @version 0.1
 */

def search_string = "xxx"
def replace_string = "yyy"

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
