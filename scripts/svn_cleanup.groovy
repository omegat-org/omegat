/*
 *  Perform SVN cleanup on a team project
 *
 * @author  Yu Tang
 * @date    2013-03-22
 * @version 0.1
 */

import org.omegat.core.team.SVNRemoteRepository
import org.tmatesoft.svn.core.wc.SVNClientManager
 
if (project.isProjectLoaded()) {
  def prop = project.getProjectProperties() 
  def folder = new File(prop.getProjectRoot()) 
  if (SVNRemoteRepository.isSVNDirectory(folder)) { 
    def clientManager = SVNClientManager.newInstance();
    clientManager.getWCClient().doCleanup(folder)
    console.println("Cleanup done!!")
  }
}
