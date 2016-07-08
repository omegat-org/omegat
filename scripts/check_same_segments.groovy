// :name=Check Same Segment :description=Check for identical segments (case sensitive)

console.println("Check for identical segments (case sensitive).\n");

segment_count = 0;

files = project.projectFiles;

fileLoop:
for (i in 0 ..< files.size())
{
    fi = files[i];

    console.println(fi.filePath);
    for (j in 0 ..< fi.entries.size())
    {

      if (java.lang.Thread.interrupted()) {
          break fileLoop;
      }

      ste = fi.entries[j];
      source = ste.getSrcText();
      target = project.getTranslationInfo(ste) ? project.getTranslationInfo(ste).translation : null;

//	  if (target != null && source.charAt( source.length() - 1) != target.charAt( target.length()  - 1))
     if ( source == target )
      {
          console.println(ste.entryNum() + "\t" + source + "\t" + target);
          segment_count++;
      }
    }
}

console.println("Segments found: " + segment_count);

