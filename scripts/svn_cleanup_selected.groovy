/*
 * Perform SVN cleanup on any local SVN repository
 *
 * @author	Yu Tang
 * @author	Kos Ivatsov
 * @date	2014-06-04
 * @version	0.3
 */

import javax.swing.JFileChooser
import org.omegat.core.team.SVNRemoteRepository
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

if (SVNRemoteRepository.isSVNDirectory(folder)) { 
	def clientManager = SVNClientManager.newInstance()
	clientManager.getWCClient().doCleanup(folder)
	console.println(res.getString("done"))
	}else
	console.println(res.getString("notrepo"))
return
