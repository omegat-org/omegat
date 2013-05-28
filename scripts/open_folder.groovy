/*
 *  Open project folder
 *
 * @author  Yu Tang
 * @date    2013-05-23
 * @version 0.2
 */

import static javax.swing.JOptionPane.*
import static org.omegat.util.Platform.*

// declaring local variables
def os   = osType
def prop = project.projectProperties

// abort if a project is not opened yet
if (!prop) {
  final def title = 'open project folder'
  final def msg   = 'Please try again after you open a project.'
  showMessageDialog null, msg, title, INFORMATION_MESSAGE
  return
}

// get command GString to open a folder
def folder = prop.projectRoot
def command
switch (os) {
  case [OsType.WIN64, OsType.WIN32]:
    command = "explorer.exe \"$folder\""
    break
  case [OsType.MAC64, OsType.MAC32]:
    command = "open \"$folder\""
    break
  default:  // for Linux or others
    command = "xdg-open \"$folder\""
    break
}

// open it
command.execute()
