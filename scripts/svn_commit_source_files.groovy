/**
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
 * @date    2014-03-06
 * @since   2014-03-03
 * @version 0.3
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

def repo = project.getRepository()

// abort when valid svn repository is not available
if (!availSvnRepo(repo)) {
    return
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
    return
}

// ask user for commit
int ret = askUserForCommit(items2BAdded, items2BDeleted, packet.commitItems, sourceDir)
packet.dispose()
if (ret == CANCEL_OPTION) {
    console.println "Commit operation has been canceled by user."
    return
}

// add items
items2BAdded.each { status ->
    WCClient.doAdd(status.file, false, false, false, EMPTY, false, false)
}

// delete items
items2BDeleted.each { status ->
    WCClient.doDelete(status.file, false, false)
}

// commit
doCommit(man, sourceDir, repo)

// exit
return

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
def askUserForCommit(items2BAdded, items2BDeleted, commitItems, sourceRoot) {
    def title = "Commit source items"
    def msg = "Your commit includes these changes:\n"
    def op = "Add"
    items2BAdded.each {status ->
        def kind = status.file.isFile() ? FILE : DIR
        def path = computeRelativePath(sourceRoot, status.file)
        msg += "\n$op ($kind) $path"
    }
    op = "Del"
    items2BDeleted.each {status ->
        def path = computeRelativePath(sourceRoot, status.file)
        msg += "\n$op ($status.kind) $path"
    }
    commitItems.each { item ->
        op = "Unk"
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
def doCommit(manager, sourceDir, repository) {
    def author = getPreferenceDefault(TEAM_AUTHOR, System.getProperty("user.name"))
    def commitMessage = "Changed source item(s) by $author"
    Log.logInfoRB "SVN_START", "upload"
    console.println "Now uploading ..."  //@@@TODO move to another thread

    try {
        def client = manager.commitClient
        def packet = getPacket(client, sourceDir)
        def info = client.doCommit(packet, false, commitMessage)
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
    } finally {
        packet.dispose()
        manager.WCClient.doCleanup sourceDir, true
    }
}