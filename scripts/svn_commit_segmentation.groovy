/** :name=SVN - Commit Segmentation File :description=Commit segmentation file change to SVN remote repository
 *
 *  Commit segmentation file change to SVN remote repository
 *
 * Supported basic operations are shown in the next list.
 *
 * 1. svn add for added (unversioned) segmentation.conf.
 * 2. svn delete for deleting (missing) segmentation.conf.
 * 3. modified segmentation.conf.
 *
 * copy, move, merge or any other svn commands are not available.
 *
 * For project managers, you can move this script to
 * <your-scripts-folder>/project_changed/ sub-folder
 * for event driven automatically execution.
 *
 * @author  Yu Tang
 * @date    2014-09-12
 * @version 0.1
 */

// User defined settings

// If show ask prompt before commit for manually execution.
// This setting is ignored when automatically execution by project load event.
boolean askBeforeCommit = true // or false


import groovy.transform.Synchronized

import org.omegat.core.Core
import org.omegat.core.data.IProject
import org.omegat.core.segmentation.SRX
import org.omegat.core.team.SVNRemoteRepository
import org.omegat.gui.scripting.ScriptLogger
import org.omegat.util.Log
import org.tmatesoft.svn.core.SVNCommitInfo
import org.tmatesoft.svn.core.wc.SVNStatusType
import org.tmatesoft.svn.core.wc2.SvnCommit
import org.tmatesoft.svn.core.wc2.SvnGetStatus
import org.tmatesoft.svn.core.wc2.SvnOperationFactory
import org.tmatesoft.svn.core.wc2.SvnStatus
import org.tmatesoft.svn.core.wc2.SvnTarget
import org.tmatesoft.svn.core.wc2.SvnScheduleForAddition
import org.tmatesoft.svn.core.wc2.SvnScheduleForRemoval

import static javax.swing.JOptionPane.*
import static org.omegat.core.events.IProjectEventListener.PROJECT_CHANGE_TYPE.LOAD
import static org.omegat.util.FileUtil.computeRelativePath
import static org.omegat.util.Preferences.TEAM_AUTHOR
import static org.omegat.util.Preferences.getPreferenceDefault
import static org.tmatesoft.svn.core.wc.SVNStatusType.*

String THIS_SCRIPT_NAME = 'svn_commit_segmentation.groovy'
boolean isCalledByProjectEvent = binding.variables.containsKey('eventType')
String trigger
ILogger logger

if (isCalledByProjectEvent) {
    if (eventType == LOAD && SVNRemoteRepository.isSVNDirectory(new File(project.projectProperties.projectRoot))) {
        // Execute by Project Load event
        trigger = 'Project LOAD event'
        logger = new BasicLogger(console: console, thisScriptName: THIS_SCRIPT_NAME)
    } else {
        // No need executing
        return
    }
} else {
    // Execute by user
    trigger = 'user'
    logger = new InteractiveLogger(console: console, thisScriptName: THIS_SCRIPT_NAME)
}

def committer = new SegmentationCommitter(isCalledByProjectEvent || !askBeforeCommit, project, logger)
if (committer.committable()) {
    console.println "$THIS_SCRIPT_NAME is executed by $trigger (${(new Date()).timeString})"
    Thread.start {
        SegmentationCommitter.ExecutionStatus ret = committer.execute()
    }
    SegmentationCommitter.ExecutionStatus.COMMIT_EXECUTED_AND_WAIT
} else {
    logger.abortUpload SegmentationCommitter.ExecutionStatus.NO_COMMIT_ITEMS_FOUND.toString()
    return
}

/**
 * Commit segmentation.conf if modified
 */
class SegmentationCommitter {

// enum for execution status
    enum ExecutionStatus {
        NOW_EXECUTING('Now executing...'),
        COMMIT_EXECUTED('Your commit is executed.'),
        COMMIT_EXECUTED_AND_WAIT('Your commit is executed. Wait a little while and see detailed information.'),
        COMMIT_NOT_EXECUTED('Your commit is not executed.'),
        EMPTY_PROJECT('A project is not loaded yet.'),
        SVN_REPOSITORY_NOT_AVAILABLE('Writable SVN repository is not available.'),
        SEG_CONF_NOT_AVAILABLE('Segmentation config file is not available.'),
        NO_COMMIT_ITEMS_FOUND('No commit items found.'),
        UNEXPECTED_SVN_STATUS('Unexpected SVN status.'),
        COMMIT_CANCELED('SVN Commit operation has been canceled by user.'),
        COMMIT_FAILED('SVN Commit operation has been failed.');

        private final String message
        private ExecutionStatus(String message) {this.message = message}
        @Override
        String toString() {this.message}
    }

// return value constants (for v3.1.0 or later)
    //final def RET_VAL_COMMIT_NOT_EXECUTED = "Your commit is not executed."
    //final def RET_VAL_COMMIT_EXECUTED = "Your commit is executed. Wait a little while and see detailed information."

// fields
    private final boolean askBeforeCommit
    private final IProject project
    private final File file
    private final ILogger logger
    private final Closure<Integer> askUserForCommit

// Constructors
    SegmentationCommitter(boolean forceCommit, IProject project, ILogger logger) {
        this.askBeforeCommit = askBeforeCommit
        this.project = project
        this.file = getTargetFile(project)
        this.logger = logger
        this.askUserForCommit = getAskUserForCommit(forceCommit)
    }

    boolean committable() {
        SVNStatusType status = file.getSvnNodeStatus()
        switch (status) {
            case STATUS_UNVERSIONED:
            case STATUS_MISSING:
            case STATUS_MODIFIED:
            case STATUS_ADDED:   // just in case
            case STATUS_DELETED: // just in case
                // OK
                return true
            case STATUS_NORMAL:
                // exit if no need commit
                return false
            default:
                // illegal state
                logger.log "Unexpected SVN status '$status.'"
                return false
        }
    }

    @Synchronized
    ExecutionStatus execute() {
        ExecutionStatus executionStatus = initialExecutionStatus
        if (executionStatus != ExecutionStatus.NOW_EXECUTING) {
            logger.log executionStatus
            return executionStatus
        }

        SVNStatusType status = file.getSvnNodeStatus()
        def prepare = { /* do nothing for default */ }
        switch (status) {
            case STATUS_UNVERSIONED:
                prepare = {
                    logger.log 'file.svnAdd()'
                    file.svnAdd()
                }
                break
            case STATUS_MISSING:
                prepare = {
                    logger.log 'file.svnDelete()'
                    file.svnDelete()
                }
                break
            case STATUS_MODIFIED:
            case STATUS_ADDED:   // just in case
            case STATUS_DELETED: // just in case
                // OK
                break
            case STATUS_NORMAL:
                // exit if no need commit
                executionStatus = ExecutionStatus.NO_COMMIT_ITEMS_FOUND
                logger.log executionStatus
                return executionStatus
            default:
                // illegal state
                executionStatus = ExecutionStatus.UNEXPECTED_SVN_STATUS
                logger.log "Unexpected SVN status '$status.'"
                return executionStatus
        }

// ask user for commit
        Core.autoSave.disable()
        File rootDir = new File(project.projectProperties.projectRoot)
        def commitStatusString = getCommitStatusString(status, file, rootDir)
        int ret = askUserForCommit(commitStatusString)
        if (ret == CANCEL_OPTION) {
            executionStatus = ExecutionStatus.COMMIT_CANCELED
            logger.log executionStatus
            Core.autoSave.enable()
            return executionStatus
        }

// prepared for commit
        prepare()

// commit
        logger.startUpload()
        try {
            SVNCommitInfo info = file.commit(getCommitMessage(status))
            if (info.errorMessage) {
                throw new IOException(info.errorMessage.message)
            }
            executionStatus = ExecutionStatus.COMMIT_EXECUTED
            logger.finishUpload info, commitStatusString
        } catch (ex) {
            executionStatus = ExecutionStatus.COMMIT_FAILED
            logger.finishUpload ex.message
        } finally {
            Core.autoSave.enable()
        }

// exit
        executionStatus
    }

    File getTargetFile(IProject project) {
        if (!project.projectLoaded) {
            return null
        }

        File file = new File(project.projectProperties.projectInternal, SRX.CONF_SENTSEG)
        file.metaClass {
            svnAdd = { ->
                SvnOperationFactory svnOperationFactory = new SvnOperationFactory()
                try {
                    SvnScheduleForAddition op = svnOperationFactory.createScheduleForAddition()
                    op.setSingleTarget(SvnTarget.fromFile(delegate as File))
                    op.run()
                } finally {
                    svnOperationFactory.dispose()
                }
            }

            svnDelete = { ->
                SvnOperationFactory svnOperationFactory = new SvnOperationFactory()
                try {
                    SvnScheduleForRemoval op = svnOperationFactory.createScheduleForRemoval()
                    op.setSingleTarget(SvnTarget.fromFile(delegate as File))
                    op.run()
                } finally {
                    svnOperationFactory.dispose()
                }
            }

            commit = { String commitMessage ->
                SvnOperationFactory svnOperationFactory = new SvnOperationFactory()
                SVNCommitInfo ret = null
                try {
                    SvnCommit op = svnOperationFactory.createCommit()
                    op.setSingleTarget(SvnTarget.fromFile(delegate as File))
                    op.setCommitMessage(commitMessage)
                    ret = op.run()
                } finally {
                    svnOperationFactory.dispose()
                }
                ret
            }

            getSvnNodeStatus = { ->
                SvnOperationFactory svnOperationFactory = new SvnOperationFactory()
                SvnStatus stat = null
                try {
                    SvnGetStatus getStatus = svnOperationFactory.createGetStatus()
                    getStatus.setSingleTarget(SvnTarget.fromFile(delegate as File))
                    stat = getStatus.run()
                } catch (e) {
                    throw new RuntimeException(e.message)
                } finally {
                    svnOperationFactory.dispose()
                }

                if (stat == null) {
                    throw new RuntimeException('Segmentation config file is not available.')
                }
                stat.nodeStatus
            }
        }
        file
    }

// CommitMessage
    static String getCommitMessage(SVNStatusType status) {
        def author = getPreferenceDefault(TEAM_AUTHOR, System.getProperty('user.name'))
        def verb = status.toString().capitalize()
        "$verb segmentation file by $author"
    }

// validateState
    private ExecutionStatus getInitialExecutionStatus() {
        if (!project.projectLoaded) {
            return ExecutionStatus.EMPTY_PROJECT
        }
        if (!SVNRemoteRepository.isSVNDirectory(new File(project.projectProperties.projectRoot))) {
            return ExecutionStatus.SVN_REPOSITORY_NOT_AVAILABLE
        }
        ExecutionStatus.NOW_EXECUTING
    }

// ask user for commit
    static Closure<Integer> getAskUserForCommit(boolean forceOK) {
        if (forceOK) {
            { String commitStatusString ->
                OK_OPTION
            }
        } else {
            { String commitStatusString ->
                def title = 'Commit segmentation file'
                def msg = "Your commit includes this change:\n" + commitStatusString
                Core.mainWindow.showConfirmDialog(msg, title, OK_CANCEL_OPTION, PLAIN_MESSAGE)
            }
        }
    }

// commit status string
    static String getCommitStatusString(SVNStatusType status, File commitItem, File rootDir) {
        def path = computeRelativePath(rootDir, commitItem)
        def op = "Unk"
        switch (status) {
        // add
            case STATUS_UNVERSIONED:
            case STATUS_ADDED:
                op = "Add"
                break
        // delete
            case STATUS_MISSING:
            case STATUS_DELETED:
                op = "Del"
                break
        // modified
            case STATUS_MODIFIED:
                op = "Modified"
                break
        }
        "\n$op (file) $path"
    }
}

interface ILogger {
    void log(arg)
    void startUpload()
    void finishUpload(SVNCommitInfo info, String commitStatusString)
    void finishUpload(String exceptionMessage)
}

class BasicLogger implements ILogger {
    ScriptLogger console
    String thisScriptName

    void log(arg) {console.println(arg)}

    void startUpload() {
        Log.logInfoRB 'SVN_START', 'upload'
        Core.mainWindow.showStatusMessageRB 'TEAM_SYNCHRONIZE'
        this.log 'Now uploading ...'
    }

    void abortUpload(String message) {
        // in case no commit items found
    }

    void finishUpload(SVNCommitInfo info, String commitStatusString) {
        console.println "Commit completed.$commitStatusString"
        Log.logDebug SVNRemoteRepository.LOGGER, "SVN committed followed file into new revision {0}$commitStatusString", info.newRevision
        Log.logInfoRB "SVN_FINISH", "upload"
        Core.mainWindow.showStatusMessageRB null
    }

    void finishUpload(String exceptionMessage) {
        Log.logErrorRB "SVN_ERROR", "upload", exceptionMessage
        this.log "Commit failed: $exceptionMessage"
        Core.mainWindow.showStatusMessageRB "SVN_ERROR", "upload", exceptionMessage
    }
}

class InteractiveLogger extends BasicLogger {
    @Override
    void finishUpload(String exceptionMessage) {
        super.finishUpload exceptionMessage
        Core.mainWindow.showMessageDialog exceptionMessage
    }

    @Override
    void abortUpload(String message) {
        this.log message
    }
}


