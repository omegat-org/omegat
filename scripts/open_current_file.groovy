/* :name=Open Current File		:description=Open the current source file
 * 
 * Open current file
 *
 * @author  Yu Tang
 * @date    2014-05-14
 * @version 0.4
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
    java.awt.Desktop.desktop.open new File(file)
    return
  case [OsType.MAC64, OsType.MAC32]:
    command = ['open', file]
    break
  default:  // for Linux or others
    command = ['xdg-open', file]
    break
}

// open it
command.execute()
