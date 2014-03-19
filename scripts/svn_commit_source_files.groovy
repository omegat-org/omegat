/** :name=SVN - Commit Source File :description=Commit source file changes to SVN remote repository
 * 
 *  Commit source file changes to SVN remote repository
 *
 * Supported basic operations are shown in the next list.
 *
 * 1. svn add for manually added (unversioned) files/folders.
 * 2. svn delete for manually deleting (missing) files/folders.
 * 3. modified files.
 *
 * copy, move, merge or any other svn commands are not available.
 *
 * @author  Yu Tang
 * @date    2014-03-09
 * @since   2014-03-03
 * @version 0.4
 */

import static javax.swing.JOptionPane.*

import org.omegat.core.team.SVNRemoteRepository
import static org.omegat.util.FileUtil.computeRelativePath
import org.omegat.util.Log
import static org.omegat.util.Preferences.*

import static org.tmatesoft.svn.core.SVNDepth.EMPTY
import static org.tmatesoft.svn.core.SVNDepth.INFINITY
import static org.tmatesoft.svn.core.SVNNodeKind.*
import org.tmatesoft.svn.core.wc.ISVNStatusHandler
import static org.tmatesoft.svn.core.wc.SVNRevision.WORKING
import static org.tmatesoft.svn.core.wc.SVNStatusType.*

// constants (for v3.1.0 or later)
final def RET_VAL_COMMIT_NOT_EXECUTED = "Your commit is not executed."
final def RET_VAL_COMMIT_EXECUTED     = "Your commit is executed. Wait a little while and see detailed information."

def repo = project.getRepository()

// abort when valid svn repository is not available
if (!availSvnRepo(repo)) {
    return RET_VAL_COMMIT_NOT_EXECUTED
}

def sourceDir = new File(project.projectProperties.sourceRoot)
def man = repo.ourClientManager
def WCClient = man.WCClient
def commitClient = man.commitClient

// SVNStatus[]
def (items2BAdded, items2BDeleted) = getStats2BAddedOrDeleted(man.statusClient, WCClient, sourceDir)

// SVNCommitItem[]
def packet = getPacket(commitClient, sourceDir)

// exit if no commit items
if (!items2BAdded && !items2BDeleted && !packet.commitItems) {
    packet.dispose()
    console.println "No commit items found."
    return RET_VAL_COMMIT_NOT_EXECUTED
}

// ask user for commit
def commitItemList = getCommitItemListString(items2BAdded, items2BDeleted, packet.commitItems, sourceDir)
int ret = askUserForCommit(commitItemList)
packet.dispose()
if (ret == CANCEL_OPTION) {
    console.println "Commit operation has been canceled by user."
    return RET_VAL_COMMIT_NOT_EXECUTED
}

// svn add items
items2BAdded.each { status ->
    WCClient.doAdd(status.file, false, false, false, EMPTY, false, false)
}

// svn delete items
items2BDeleted.each { status ->
    WCClient.doDelete(status.file, false, false)
}

// commit
Log.logInfoRB "SVN_START", "upload"
mainWindow.showStatusMessageRB "TEAM_SYNCHRONIZE"
console.println "Now uploading ..."
Thread.start {
    doCommit(man, sourceDir, repo, commitItemList)
}

// exit
return RET_VAL_COMMIT_EXECUTED

// ===================================================================

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

// Get statuses to be added or deleted to version control.
def getStats2BAddedOrDeleted(statusClient, WCClient, sourceDir) {
    def added = [], deleted = []
    statusClient.doStatus(sourceDir, WORKING, INFINITY, false, false, false, false, { status ->
        switch (status.combinedNodeAndContentsStatus) {
            // add
            case STATUS_UNVERSIONED:
                added << status
                break
            // delete
            case STATUS_MISSING:
                deleted << status
                break
        }
    } as ISVNStatusHandler, null)
    [added, deleted]
}

// Get packet
def getPacket(commitClient, sourceDir) {
    commitClient.doCollectCommitItems(
            [sourceDir] as File[], false, false, INFINITY, null)
}

// ask user for commit
def askUserForCommit(commitItemListString) {
    def title = "Commit source items"
    def msg = "Your commit includes these changes:\n" + commitItemListString
    mainWindow.showConfirmDialog(msg, title, OK_CANCEL_OPTION, PLAIN_MESSAGE)
}

// commit item list string
def getCommitItemListString(items2BAdded, items2BDeleted, commitItems, sourceRoot) {
    def ret = ""
    def op = "Add"
    items2BAdded.each {status ->
        def kind = status.file.isFile() ? FILE : DIR
        def path = computeRelativePath(sourceRoot, status.file)
        ret += "\n$op ($kind) $path"
    }
    op = "Del"
    items2BDeleted.each {status ->
        def path = computeRelativePath(sourceRoot, status.file)
        ret += "\n$op ($status.kind) $path"
    }
    commitItems.each { item ->
        op = "Unk"
        if (item.isAdded()) {
            op = "Add"
        } else if (item.isContentsModified()) {
            op = "Mod"
        } else if (item.isCopied()) {
            op = "Copy"  // just in case
        } else if (item.isDeleted()) {
            op = "Del"
        }
        def path = computeRelativePath(sourceRoot, item.file)
        ret += "\n$op ($item.kind) $path"
    }
    ret
}

// commit
def doCommit(manager, sourceDir, repository, commitItemList) {
    def author = getPreferenceDefault(TEAM_AUTHOR, System.getProperty("user.name"))
    def commitMessage = "Changed source item(s) by $author"

    try {
        def client = manager.commitClient
        def packet = getPacket(client, sourceDir)
        def info = client.doCommit(packet, false, commitMessage)
        if (info.errorMessage) {
            throw new IOException(info.errorMessage)
        }
        console.println "Commit completed." + commitItemList
        Log.logDebug SVNRemoteRepository.LOGGER, "SVN committed followed files into new revision {0}" + commitItemList, info.newRevision
        Log.logInfoRB "SVN_FINISH", "upload"
        mainWindow.showStatusMessageRB null
    } catch(ex) {
        Log.logErrorRB "SVN_ERROR", "upload", ex.message
        console.println "Commit failed: ${ex.message}"
        mainWindow.showStatusMessageRB "SVN_ERROR", "upload", ex.message
    } finally {
        packet.dispose()
        manager.WCClient.doCleanup sourceDir, true
    }
}