/*
 The rules are based on the Checkmate Quality check 
 http://www.opentag.com/okapi/wiki/index.php?title=CheckMate_-_Quality_Check_Configuration
 Each rule is a block of groovy code, 'source' and 'target' are the two parameters of this block
 */

console.println("Check rules.\n");

// Prefs
maxCharLengthAbove=200

rules = [
            
            // Text unit verification
            targetLeadingWhiteSpaces: { s, t ->  t =~ /^\s+/ },
            targetTrailingWhiteSpaces: { s, t -> t =~ /\s+$/ },
            // Segment verification
            doubledWords: { s, t -> t =~ /(?i)(\b\w+)\s+\1\b/ },
			// Length
		    //targetShorter: { s, t -> t.length <  }
			//targetLonger: { s, t -> (t.length() / s.length() * 100) > maxCharLengthAbove }
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
                segment_count++;
            }
        }
    }
}

console.println("Segments found : " + segment_count);



