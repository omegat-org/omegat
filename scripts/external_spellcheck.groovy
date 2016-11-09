/* :name=External spellcheck :description=Writes all segments in a file named [project_name].doc and opens it
 *
 * @author: Didier Briel
 * @author: Kos Ivantsov
 * @date: 2016-11-08
 * @version 0.1
 */

// true if all source files should be used, false if only the current file should be used
def checkWholeProject = true

import static org.omegat.util.Platform.*

def prop = project.projectProperties
def folder = prop.projectRoot+'script_output/'
projname = new File(prop.getProjectRoot()).getName()
wordFile = new File(folder + projname + '.doc')
// create folder if it doesn't exist
if (! (new File (folder)).exists()) {
    (new File(folder)).mkdir()
}
wordFile.write('', 'UTF-8')
def nl = System.getProperty('line.separator')

   files = project.projectFiles
    if (!checkWholeProject) {
        files = project.projectFiles.subList(editor.@displayedFileIndex, editor.@displayedFileIndex + 1)
    }
	
    for (i in 0 ..< files.size()) {
        fi = files[i];
		
        for (j in 0 ..< fi.entries.size()) {
            ste = fi.entries[j];
            source = ste.getSrcText()
            target = project.getTranslationInfo(ste) ? project.getTranslationInfo(ste).translation : null
			
            if (target == null) {
                target = source 
            }
            wordFile.append(target + nl + nl, 'UTF-8')
            //console.println(target)
        }
    }
def command
switch (osType) {
		case [OsType.WIN64, OsType.WIN32]:
				command = "cmd /c start \"\" \"$wordFile\"" // for WinNT
				// command = "command /c start \"\" \"$wordFile\"" // for Win9x or WinME
				break
		case [OsType.MAC64, OsType.MAC32]:
				command = ['open', wordFile]
				break
		default:  // for Linux or others
				command = ['xdg-open', wordFile]
				break
		}
command.execute()
