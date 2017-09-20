/* Perform SVN cleanup on current project or any folder (recursively)
 *
 * @author	Yu Tang
 * @author	Kos Ivatsov
 * @date		2017-09-20
 * @version	0.6
 */


import javax.swing.JFileChooser
import org.omegat.core.Core
import org.omegat.util.OStrings
import org.omegat.util.StaticUtils
import org.omegat.util.StringUtil
import org.tmatesoft.svn.core.wc.*

utils = (StringUtil.getMethods().toString().findAll("format")) ? StringUtil : StaticUtils
if  (OStrings.VERSION < '3.5.2') {
	console.clear()
	console.println(utils.format(res.getString("oldver"), OStrings.VERSION))
	return	
}

def directory
def dir_list = []
if (project.isProjectLoaded()) {
    def prop = project.projectProperties
    directory = new File(prop.getProjectRoot())
    } else {
        JFileChooser fc = new JFileChooser(
          dialogTitle: res.getString("title"),
          fileSelectionMode: JFileChooser.DIRECTORIES_ONLY,
          multiSelectionEnabled: false
          )
        fc.setFileHidingEnabled(false)

        if (fc.showOpenDialog() != JFileChooser.APPROVE_OPTION) {
          console.println(res.getString("canceled"))
          return
        }
        directory = new File(fc.getSelectedFile().toString())
    }

directory.eachDirRecurse () {
   dir -> if (dir =~ /\.svn/) {dir_list << dir.toString().replaceAll(/\.svn.*$/, '')}
   dir_list.unique()
}

public static boolean isSVNDirectory(File localDirectory) {
    File svnDir = new File(localDirectory, ".svn");
    return svnDir.exists() && svnDir.isDirectory();
}
def count=0
if (dir_list.size() < 1) {
   console.println(utils.format(res.getString("norepos"), directory))
   mainWindow.statusLabel.setText(utils.format(res.getString("count"), count))
   Timer timer = new Timer().schedule({mainWindow.statusLabel.setText(null)} as TimerTask, 10000)
   return 
}
org.omegat.util.gui.UIThreadsUtil.executeInSwingThread {
    dir_list.each(){
        folder = new File(it)
        if (isSVNDirectory(folder)) { 
            def clientManager = SVNClientManager.newInstance()
            try {
                clientManager.getWCClient().doCleanup(folder)
            } catch (Exception e) {
                console.println(utils.format(res.getString("notrepo"), folder))
                return
            }
        console.println(utils.format(res.getString("done"), folder))
        }
        count++   
     }
mainWindow.statusLabel.setText(utils.format(res.getString("count"), count))
Timer timer = new Timer().schedule({mainWindow.statusLabel.setText(null)} as TimerTask, 10000)
} as Runnable
return