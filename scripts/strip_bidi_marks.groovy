/* Remove bidi mark in the current target or in selection
 * 
 * @author   Manuel Souto Pico (based on Kos Ivantsov's Strip Tags script)
 * @date     2015-09-21
 * @version  0.1
 */
import static javax.swing.JOptionPane.*
import static org.omegat.util.Platform.*

// abort if a project is not opened yet
def prop = project.projectProperties
if (!prop) {
  final def title = res.getString("title")
  final def msg   = res.getString("msg")
  console.clear()
  console.println(res.getString("name") + "\n${"-"*15}\n" + msg)
  showMessageDialog null, msg, title, INFORMATION_MESSAGE
  return
}

if (editor.selectedText){
	target = editor.selectedText
	}else{
	target = editor.getCurrentTranslation()
}
if (target != null) {
  target = target.replaceAll(/[\u200E\u200F\u202A-\u202E\u2066-\u2069]/, '')
}

if (editor.selectedText){
	editor.insertText(target)
	}else{
	editor.replaceEditText(target)
}