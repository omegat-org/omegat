/*
 *  Test script taking inspiration from the tag validation class :)
 */

console.println("Check for identical segments (case insensitive).\n");

var segment_count = 0;

var files = project.getProjectFiles();


for (var i = 0; i < files.size(); i++)
{
    var fi = files.get(i);

    console.println(fi.filePath);

    for (var j = 0; j < fi.entries.size(); j++)
    {
      var ste = fi.entries.get(j);
      var source = ste.getSrcText();
      var target = project.getTranslationInfo(ste) ? project.getTranslationInfo(ste).translation : null;

      if ( source.equalsIgnoreCase(target) )
      {
          console.println(ste.entryNum() + "\t" + source + "\t" + target);
          segment_count++;
      }
    }
}

console.println("Segments found: " + segment_count);


