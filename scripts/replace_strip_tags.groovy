/*
 * #Purpose: Remove tags in the current target 
 * #Details: http: // wp.me/p3fHEs-4W
 * 
 * @author   Kos Ivantsov
 * @date     2013-06-26
 * @version  0.1
 */
import static javax.swing.JOptionPane.*
import static org.omegat.util.Platform.*

// abort if a project is not opened yet
def prop = project.projectProperties
if (!prop) {
  final def title = 'Strip tags in current segment'
  final def msg   = 'Please try again after you open a project.'
  showMessageDialog null, msg, title, INFORMATION_MESSAGE
  return
}

target = editor.getCurrentTranslation()
if (target != null) {
target = target.replaceAll(/<\/?[a-z]+[0-9]* ?\/?>/, '')
}
editor.replaceEditText(target)