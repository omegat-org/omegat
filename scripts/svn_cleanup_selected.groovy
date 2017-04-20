/* :name=SVN - Cleanup :description=Perform SVN cleanup on any local SVN repository
 *
 * @author	Yu Tang
 * @author	Kos Ivatsov
 * @date	2016-04-28
 * @version	0.4
 */

// For 4.0 team project, this script has to be used with the project closed, otherwise the
// SVN repository will not be found.

import javax.swing.JFileChooser
import org.tmatesoft.svn.core.wc.*

def folder
console.clear()
console.println(res.getString("name")+"\n${"-"*15}")
if (project.isProjectLoaded()) {
	def prop = project.getProjectProperties()
	folder = new File(prop.getProjectRoot())
	}else{
	JFileChooser fc = new JFileChooser(
		dialogTitle: res.getString("title"),
		fileSelectionMode: JFileChooser.DIRECTORIES_ONLY, 
		multiSelectionEnabled: false
		)
	if(fc.showOpenDialog() != JFileChooser.APPROVE_OPTION) {
		console.println(res.getString("canceled"))
		return
		}
	folder = new File(fc.getSelectedFile().toString())
	}


public static boolean isSVNDirectory(File localDirectory) {
	File svnDir = new File(localDirectory, ".svn");
	return svnDir.exists() && svnDir.isDirectory();
	}

if (isSVNDirectory(folder)) { 
	def clientManager = SVNClientManager.newInstance()
	clientManager.getWCClient().doCleanup(folder)
	console.println(res.getString("done"))
	}else
	console.println(res.getString("notrepo"))
return
