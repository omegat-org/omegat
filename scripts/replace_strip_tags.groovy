/* Remove tags in the current target or in selection
 * 
 * @author   Kos Ivantsov
 * @date     2014-06-05
 * @version  0.3
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
target = target.replaceAll(/<\/?[a-z]+[0-9]* ?\/?>/, '')
}

if (editor.selectedText){
	editor.insertText(target)
	}else{
	editor.replaceEditText(target)
	}