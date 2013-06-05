/*
 *  Open the writeable glossary in an editor
 *
 * @author  Yu Tang
 * @date    2013-06-05
 * @version 0.3
 */

import static javax.swing.JOptionPane.*
import static org.omegat.util.Platform.*

/**
 * Uncomment the next line if you want to set a default text editor
 * that will open glossary file
 */
// def textEditor = /path to your editor/
// E.g., /TextMate/
// /C:\Program Files (x86)\editor\editor.exe/
// ['x-terminal-emulator', '-e', 'vi']

// make a Closure to show message dialog
def showMessage = { msg -> showMessageDialog null, msg, 'Open glossary', INFORMATION_MESSAGE }

// abort if a project is not opened yet
def prop = project.projectProperties
if (!prop) {
  showMessage 'Please try again after you open a project.'
  return
}

// exit if file not found
def file = prop.writeableGlossary
if (! new File(file).exists()) {
  showMessage 'Glossary file not found.'
  return
}

// get command GString list to open a file
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
