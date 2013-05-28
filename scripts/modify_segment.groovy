// Segment modifier
def ste = editor.currentEntry;

if (ste .srcText != "enu"  ) 
{
    target = project.getTranslationInfo(ste)?.translation;
    //project.setTranslation(currentSegment, "FRA", true);

    console.println(editor.currentEntryNumber + "\t" + ste.srcText );
    console.println(target); // In-memory target, the edited text is not available.

    // See http://groovy.codehaus.org/Regular+Expressions for Groovy RegExp subtelties.
    target = target.replaceAll(/(\d+),(\d+)/, '$1 $2');

    editor.replaceEditText(target);
    editor.nextEntry();
}
