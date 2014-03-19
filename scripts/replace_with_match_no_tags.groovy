/* :name=Tag-Free Match :description=Replace current target with tag-free match
 * 
 * #Purpose: Replace current target with tag-free match 
 * #Details: href="http://wp.me/p3fHEs-4W
 * 
 * @author   Kos Ivantsov
 * @date     2013-06-26
 * @version  0.1
 */
 
import static javax.swing.JOptionPane.*
import static org.omegat.util.Platform.*
import org.omegat.core.Core;
 
// abort if a project is not opened yet
def prop = project.projectProperties
if (!prop) {
  final def title = 'Replace with Match (no tags)'
  final def msg   = 'Please try again after you open a project.'
  showMessageDialog null, msg, title, INFORMATION_MESSAGE
  return
}
 
def match = Core.getMatcher()
def near = match.getActiveMatch()
if (near != null) {
  def matchtranslation = "$near.translation"
  matchtranslation = matchtranslation.replaceAll(/<\/?[a-z]+[0-9]* ?\/?>/, '')
  editor.replaceEditText(matchtranslation);
}