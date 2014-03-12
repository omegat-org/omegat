/*
 * Perform SVN cleanup on any local SVN repository
 *
 * @author	Yu Tang
 * @author	Kos Ivatsov
 * @date	2014-01-17
 * @version	0.2
 */

import javax.swing.JFileChooser
import org.omegat.core.team.SVNRemoteRepository
import org.tmatesoft.svn.core.wc.*

def folder

if (project.isProjectLoaded()) {
	def prop = project.getProjectProperties()
	folder = new File(prop.getProjectRoot())
	}else{
	JFileChooser fc = new JFileChooser(
		dialogTitle: "Choose SVN repository to perform cleanup",
		fileSelectionMode: JFileChooser.DIRECTORIES_ONLY, 
		multiSelectionEnabled: false
		)
	if(fc.showOpenDialog() != JFileChooser.APPROVE_OPTION) {
		console.println "Canceled"
		return
		}
	folder = new File(fc.getSelectedFile().toString())
	}

if (SVNRemoteRepository.isSVNDirectory(folder)) { 
	def clientManager = SVNClientManager.newInstance()
	clientManager.getWCClient().doCleanup(folder)
	console.println("Cleanup done!!")
	}
return
