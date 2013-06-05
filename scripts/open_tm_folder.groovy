/*
 *  Open the /tm folder
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
  final def title = 'open TM folder'
  final def msg   = 'Please try again after you open a project.'
  showMessageDialog null, msg, title, INFORMATION_MESSAGE
  return
}

// get command GString to open a folder
def folder = prop.TMRoot
def command
switch (osType) {
  case [OsType.WIN64, OsType.WIN32]:
    command = "explorer.exe \"$folder\""
    break
  case [OsType.MAC64, OsType.MAC32]:
    command = ['open', folder]
    break
  default:  // for Linux or others
    command = ['xdg-open', folder]
    break
}

// open it
command.execute()
