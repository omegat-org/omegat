/*
 *  Open the writeable glossary in an editor
 *
 * @author  Yu Tang
 * @date    2013-05-23
 * @version 0.2
 */

import static javax.swing.JOptionPane.*
import static org.omegat.util.Platform.*

def textEditor = /path to your editor/
// E.g., /TextEdit/
// /C:\Program Files (x86)\editor\editor.exe/

// declaring local variables
def os   = osType
def prop = project.projectProperties

// abort if a project is not opened yet
if (!prop) {
  final def title = 'Open glossary'
  final def msg   = 'Please try again after you open a project.'
  showMessageDialog null, msg, title, INFORMATION_MESSAGE
  return
}

// get command GString to open a file
def file = prop.writeableGlossary
def command
switch (os) {
  case [OsType.WIN64, OsType.WIN32]:
    command = "\"$textEditor\" \"$file\""
    break
  case [OsType.MAC64, OsType.MAC32]:
    command = "open -a \"$textEditor\" \"$file\""
    break
  default:  // for Linux or others
    command = "\"$textEditor\" \"$file\""
    break
}

// open it
command.execute()