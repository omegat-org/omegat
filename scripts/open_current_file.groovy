/*
 *  Open current file
 *
 * @author  Yu Tang
 * @date    2013-06-05
 * @version 0.3
 */

import static javax.swing.JOptionPane.*
import static org.omegat.util.Platform.*

// abort if a project is not opened yet
def prop = project.projectProperties
if (!prop) {
  final def title = 'open current file'
  final def msg   = 'Please try again after you open a project.'
  showMessageDialog null, msg, title, INFORMATION_MESSAGE
  return
}

// get command GString to open a file
def file = prop.sourceRoot + editor.currentFile
def command
switch (osType) {
  case [OsType.WIN64, OsType.WIN32]:
    command = "cmd /c start \"\" \"$file\"" // for WinNT
    // command = "command /c start \"\" \"$file\"" // for Win9x or WinME
    break
  case [OsType.MAC64, OsType.MAC32]:
    command = ['open', file]
    break
  default:  // for Linux or others
    command = ['xdg-open', file]
    break
}

// open it
command.execute()
