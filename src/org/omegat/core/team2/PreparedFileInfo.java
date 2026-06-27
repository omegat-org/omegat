/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008-2016 Alex Buloichik
               2025 Hiroshi Miura
               Home page: https://www.omegat.org/
               Support center: https://omegat.org/support

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.team2;

import java.io.File;
import java.util.Objects;

/**
 * Immutable container for prepared file information.
 * Replaces RebaseAndCommit.Prepared with better encapsulation.
 */
public final class PreparedFileInfo {
    private final String path;
    private final File fileBase;
    private final File fileHead;
    private final String versionBase;
    private final String versionHead;
    private final boolean needToCommit;
    private final String commitComment;
    private final String charset;

    private PreparedFileInfo(Builder builder) {
        this.path = builder.path;
        this.fileBase = builder.fileBase;
        this.fileHead = builder.fileHead;
        this.versionBase = builder.versionBase;
        this.versionHead = builder.versionHead;
        this.needToCommit = builder.needToCommit;
        this.commitComment = builder.commitComment;
        this.charset = builder.charset;
    }

    public PreparedFileInfo(RebaseAndCommit.Prepared r) {
        this.path = r.path;
        this.fileBase = r.fileBase;
        this.fileHead = r.fileHead;
        this.versionBase = r.versionBase;
        this.versionHead = r.versionHead;
        this.needToCommit = r.needToCommit;
        this.commitComment = r.commitComment;
        this.charset = r.charset;
    }

    // Getters
    public String getPath() {
        return path;
    }
    public File getFileBase() {
        return fileBase;
    }
    public File getFileHead() {
        return fileHead;
    }
    public String getVersionBase() {
        return versionBase;
    }
    public String getVersionHead() {
        return versionHead;
    }
    public boolean needToCommit() {
        return needToCommit;
    }
    public String getCommitComment() {
        return commitComment;
    }
    public String getCharset() {
        return charset;
    }

    // Builder for creating instances with validation
    public static class Builder {
        private final String path;
        private File fileBase;
        private File fileHead;
        private String versionBase;
        private String versionHead;
        private boolean needToCommit = false;
        private String commitComment;
        private String charset;

        public Builder(String path) {
            this.path = Objects.requireNonNull(path, "Path cannot be null");
        }

        public Builder withVersions(String baseVersion, String headVersion) {
            this.versionBase = baseVersion;
            this.versionHead = headVersion;
            return this;
        }

        public Builder withFiles(File baseFile, File headFile) {
            this.fileBase = baseFile;
            this.fileHead = headFile;
            return this;
        }

        public Builder withCommitInfo(boolean needToCommit, String commitComment, String charset) {
            this.needToCommit = needToCommit;
            this.commitComment = commitComment;
            this.charset = charset;
            return this;
        }

        public PreparedFileInfo build() {
            if (versionBase == null || versionHead == null) {
                throw new IllegalStateException("Both base and head versions must be set");
            }
            return new PreparedFileInfo(this);
        }
    }

    // Create a copy with updated commit information
    public PreparedFileInfo withCommitInfo(boolean needToCommit, String commitComment, String charset) {
        return new Builder(this.path)
                .withVersions(this.versionBase, this.versionHead)
                .withFiles(this.fileBase, this.fileHead)
                .withCommitInfo(needToCommit, commitComment, charset)
                .build();
    }
}
