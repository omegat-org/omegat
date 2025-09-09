package org.omegat.core.team2.fsm.operation;

import java.io.File;

/**
 * Interface for rebase operations that replaces RebaseAndCommit.IRebase
 * with better encapsulation and state management.
 */
public interface IRebaseOperation {
    /**
     * Parse BASE version of file for rebase operation.
     */
    void parseBaseFile(File file) throws Exception;

    /**
     * Parse HEAD version of file for rebase operation.
     */
    void parseHeadFile(File file) throws Exception;

    /**
     * Perform rebase and save result to output file.
     */
    void rebaseAndSave(File out) throws Exception;

    /**
     * Reload project data from the resulted file.
     */
    void reload(File file) throws Exception;

    /**
     * Generate commit comment for this operation.
     */
    String getCommentForCommit();

    /**
     * Get charset for file encoding conversion.
     */
    String getFileCharset(File file) throws Exception;
}
