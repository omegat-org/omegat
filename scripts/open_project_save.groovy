/*
 *  Open project_save.tmx in an editor
 *
 * @author  Yu Tang
 * @date    2013-06-05
 * @version 0.3
 */

import static javax.swing.JOptionPane.*
import static org.omegat.util.Platform.*

/**
 * Uncomment the next line if you want to set a default text editor
 * that will open project_save.tmx
 */
// def textEditor = /path to your editor/
// E.g., /TextMate/
// /C:\Program Files (x86)\editor\editor.exe/
// ['x-terminal-emulator', '-e', 'vi']

// abort if a project is not opened yet
def prop = project.projectProperties
if (!prop) {
  final def title = 'open project_save.tmx'
  final def msg   = 'Please try again after you open a project.'
  showMessageDialog null, msg, title, INFORMATION_MESSAGE
  return
}

// get command GString list to open a file
def file = "${prop.projectInternal}project_save.tmx" 
def command
switch (osType) {
  case [OsType.WIN64, OsType.WIN32]:
    command = "cmd /c start \"\" \"$file\""  // default
    try { command = textEditor instanceof List ? [*textEditor, file] : "\"$textEditor\" \"$file\"" } catch (ignore) {}
    break
  case [OsType.MAC64, OsType.MAC32]:
    command = ['open', file]  // default
    try { command = textEditor instanceof List ? [*textEditor, file] : ['open', '-a', textEditor, file] } catch (ignore) {}
    break
  default:  // for Linux or others
    command = ['xdg-open', file] // default
    try { command = textEditor instanceof List ? [*textEditor, file] : [textEditor, file] } catch (ignore) {}
    break
}

// open it
console.println "command: $command"
command.execute()
