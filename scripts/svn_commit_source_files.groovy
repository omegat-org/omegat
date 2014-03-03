/*
* Commit source file changes to SVN remote repository
*
* @author Yu Tang
* @date 2014-03-03
* @version 0.2
*/
 
import static javax.swing.JOptionPane.*
 
import org.omegat.core.team.IRemoteRepository
import org.omegat.core.team.SVNRemoteRepository
import static org.omegat.util.FileUtil.computeRelativePath
import org.omegat.util.Log
import static org.omegat.util.Preferences.*
 
import org.tmatesoft.svn.core.SVNCommitInfo
import static org.tmatesoft.svn.core.SVNDepth.EMPTY
import static org.tmatesoft.svn.core.SVNDepth.INFINITY
import static org.tmatesoft.svn.core.SVNNodeKind.*
import org.tmatesoft.svn.core.wc.ISVNStatusHandler
import org.tmatesoft.svn.core.wc.SVNClientManager
import org.tmatesoft.svn.core.wc.SVNCommitClient
import org.tmatesoft.svn.core.wc.SVNCommitItem
import org.tmatesoft.svn.core.wc.SVNCommitPacket
import static org.tmatesoft.svn.core.wc.SVNRevision.WORKING
import org.tmatesoft.svn.core.wc.SVNStatus
import org.tmatesoft.svn.core.wc.SVNStatusClient
import static org.tmatesoft.svn.core.wc.SVNStatusType.*
import org.tmatesoft.svn.core.wc.SVNWCClient
 
IRemoteRepository repo = project.getRepository()
 
// abort when valid svn repository is not available
if (!availSvnRepo(repo)) {
return
}
 
File sourceDir = new File(project.projectProperties.sourceRoot)
SVNClientManager man = repo.ourClientManager
 
// do add/delete svn operation
addDelProbableItems(man.statusClient, man.WCClient, sourceDir)
 
// collect commit items
SVNCommitClient cc = man.getCommitClient()
SVNCommitPacket packet = cc.doCollectCommitItems([sourceDir] as File[], false, false, INFINITY, null)
SVNCommitItem[] commitItems = packet.getCommitItems()
 
// exit if no commit items
if (!commitItems) {
console.println "No commit items found."
return
}
 
// ask user for commit
int ret = askUserForCommit(commitItems, sourceDir)
if (ret == CANCEL_OPTION) {
man.WCClient.doCleanup sourceDir, true
//@@@todo man.WCClient.doUnlock
return
}
 
// commit
doCommit(cc, packet, man, sourceDir, repo)
 
// exit
return
 
// verify valid svn repository available
boolean availSvnRepo(repository) {
if (!repository) {
console.println "The project is not loaded yet or not a Team project."
return false
}
 
if (!(repository instanceof SVNRemoteRepository)) {
console.println "This repository type is not SVN."
return false
}
 
if (repository.readOnly) {
console.println "This repository is read-only. Could not upload."
return false
}
 
true
}
 
// do add/delete svn operation
def addDelProbableItems(statusClient, WCClient, sourceDir) {
statusClient.doStatus(sourceDir, WORKING, INFINITY, false, false, false, false, { status ->
switch (status.combinedNodeAndContentsStatus) {
// add
case STATUS_UNVERSIONED:
WCClient.doAdd(status.file, false, false, false, EMPTY, false, false)
break
// delete
case STATUS_MISSING:
WCClient.doDelete(status.file, false, false)
break
}
} as ISVNStatusHandler, null)
}
 
// ask user for commit
def askUserForCommit(commitItems, sourceRoot) {
def title = "Commit source items"
def msg = "Your commit includes these changed items:\n"
commitItems.each { item ->
def op = "Unk"
if (item.isAdded()) {
op = "Add"
} else if (item.isContentsModified()) {
op = "Mod"
} else if (item.isCopied()) {
op = "Copy"
} else if (item.isDeleted()) {
op = "Del"
}
def path = computeRelativePath(sourceRoot, item.file)
msg += "\n$op ($item.kind) $path"
}
mainWindow.showConfirmDialog(msg, title, OK_CANCEL_OPTION, PLAIN_MESSAGE)
}
 
// commit
def doCommit(client, packet, manager, sourceDir, repository) {
def author = getPreferenceDefault(TEAM_AUTHOR, System.getProperty("user.name"))
def commitMessage = "Added source item(s) by $author"
Log.logInfoRB "SVN_START", "upload"
console.println "Now uploading ..." //@@@TODO move to another thread
 
try {
SVNCommitInfo info = client.doCommit(packet, false, commitMessage)
if (info.errorMessage) {
Log.logErrorRB "SVN_ERROR", "upload", info.errorMessage
console.println "Commit failed: ${info.errorMessage}"
manager.WCClient.doCleanup sourceDir, true
} else {
console.println "Commit completed."
packet.commitItems.each { item ->
Log.logDebug repository.LOGGER, "SVN committed file {0} into new revision {1}", item.file, info.newRevision
}
Log.logInfoRB "SVN_FINISH", "upload"
}
} catch(ex) {
Log.logErrorRB "SVN_ERROR", "upload", ex.message
console.println "Commit failed: ${ex.message}"
manager.WCClient.doCleanup sourceDir, true
}
}