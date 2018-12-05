/** :name=Auto Open Last Project :description=Allow to automatically open last used OmegaT project
 *
 *  Allow to automatically open last used OmegaT project
 *
 * Usage: move this script to
 * <your-scripts-folder>/application_startup/ sub-folder
 * for event driven automatically execution.
 *
 * @author  Yu Tang
 * @date    2018-12-05
 * @version 0.3
 */

import org.omegat.util.Log

import javax.swing.JMenu
import javax.swing.JMenuItem

openLastProject()
return

void openLastProject() {
    final String SCRIPT_NAME = 'Auto Open Last Project'
    JMenuItem item = lastProjectMenuItem
    if (item == null) {
        Log.log "$SCRIPT_NAME: No ProjectRecentMenuItems found."
    } else if (!item.isEnabled()) {
        Log.log "$SCRIPT_NAME: First ProjectRecentMenuItem is disabled."
    } else if (org.omegat.Main.projectLocation) {
        Log.log "$SCRIPT_NAME: Another project is specified via commandline arguments."
    } else {
        Log.log "$SCRIPT_NAME: Open '${item.text}'"
        item.doClick()
    }
}

JMenuItem getLastProjectMenuItem() {
    JMenuItem item = null
    try {
        JMenu menu = (JMenu) mainWindow.mainMenu.projectOpenRecentMenuItem
        if (menu.itemCount > 0) {
            item = menu.getItem(0)
        }
    //} catch (Exception ex) {
    // java.lang.NoSuchMethodError not caught with Exception
    } catch (Throwable ex) {
        // ignore
    }
    item
}
