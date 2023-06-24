/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Chihiro Hio, Aaron Madlon-Kay
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

package org.omegat.externalfinder.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.KeyStroke;

import org.omegat.util.OStrings;

/**
 * A data class representing an ExternalFinder "item". Immutable. Use
 * {@link Builder} to construct.
 * <p>
 * In the original plugin, the character <code>_</code> in the name was used to
 * indicate a mnemonic; upon setting the name the mnemonic character was
 * extracted and the <code>_</code> removed. We now do not modify the name, and
 * we leave mnemonic setting up to the Mnemonics library so <code>&amp;</code>
 * is the character to use.
 */
public final class ExternalFinderItem {

    public static final String PLACEHOLDER_TARGET = "{target}";

    public enum TARGET {

        // default BOTH for URL and command
        ASCII_ONLY, NON_ASCII_ONLY, BOTH;

        @Override
        public String toString() {
            return OStrings.getString("EXTERNALFINDER_TARGET_" + name());
        }
    }

    public enum ENCODING {

        // default DEFAULT for URL
        // default NONE for command
        DEFAULT, ESCAPE, NONE;

        @Override
        public String toString() {
            return OStrings.getString("EXTERNALFINDER_ENCODING_" + name());
        }
    }

    public enum SCOPE {
        GLOBAL, PROJECT
    }

    private final String name;
    private final List<ExternalFinderItemURL> urls;
    private final List<ExternalFinderItemCommand> commands;
    private final KeyStroke keystroke;
    private final boolean nopopup;
    private final SCOPE scope;

    private ExternalFinderItem(Builder builder) {
        this.name = builder.name;
        this.urls = builder.urls.isEmpty() ? Collections.emptyList() : new ArrayList<>(builder.urls);
        this.commands = builder.commands.isEmpty() ? Collections.emptyList()
                : new ArrayList<>(builder.commands);
        this.keystroke = builder.keystroke;
        this.nopopup = builder.nopopup;
        this.scope = builder.scope;
    }

    public String getName() {
        return name;
    }

    public List<ExternalFinderItemURL> getURLs() {
        return Collections.unmodifiableList(urls);
    }

    public List<ExternalFinderItemCommand> getCommands() {
        return Collections.unmodifiableList(commands);
    }

    public KeyStroke getKeystroke() {
        return keystroke;
    }

    public boolean isNopopup() {
        return nopopup;
    }

    public SCOPE getScope() {
        return scope;
    }

    public boolean isAsciiOnly() {
        return isTargetOnly(TARGET.ASCII_ONLY);
    }

    public boolean isNonAsciiOnly() {
        return isTargetOnly(TARGET.NON_ASCII_ONLY);
    }

    private boolean isTargetOnly(final TARGET target) {
        for (ExternalFinderItemURL url : urls) {
            if (url.getTarget() != target) {
                return false;
            }
        }

        for (ExternalFinderItemCommand command : commands) {
            if (command.getTarget() != target) {
                return false;
            }
        }

        return true;
    }

    public static boolean isASCII(String s) {
        for (int i = 0, n = s.length(); i < n; i++) {
            if ((int) s.charAt(i) > 0x7F) {
                return false;
            }
        }
        return true;
    }

    public Object getContentSummary() {
        StringBuilder sb = new StringBuilder();
        if (!getURLs().isEmpty()) {
            String urls = OStrings.getString("EXTERNALFINDER_CONTENT_TEMPLATE",
                    OStrings.getString("EXTERNALFINDER_CONTENT_URLS"), getURLs().size());
            sb.append(urls);
        }
        if (!getCommands().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(OStrings.getString("EXTERNALFINDER_CONTENT_DELIMITER"));
            }
            String commands = OStrings.getString("EXTERNALFINDER_CONTENT_TEMPLATE",
                    OStrings.getString("EXTERNALFINDER_CONTENT_COMMANDS"), getCommands().size());
            sb.append(commands);
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((urls == null) ? 0 : urls.hashCode());
        result = prime * result + ((commands == null) ? 0 : commands.hashCode());
        result = prime * result + ((keystroke == null) ? 0 : keystroke.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (nopopup ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ExternalFinderItem other = (ExternalFinderItem) obj;
        if (urls == null) {
            if (other.urls != null) {
                return false;
            }
        } else if (!urls.equals(other.urls)) {
            return false;
        }
        if (commands == null) {
            if (other.commands != null) {
                return false;
            }
        } else if (!commands.equals(other.commands)) {
            return false;
        }
        if (keystroke == null) {
            if (other.keystroke != null) {
                return false;
            }
        } else if (!keystroke.equals(other.keystroke)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (nopopup != other.nopopup) {
            return false;
        }
        return true;
    }

    public static class Builder {
        private String name;
        private List<ExternalFinderItemURL> urls = new ArrayList<>();
        private List<ExternalFinderItemCommand> commands = new ArrayList<>();
        private KeyStroke keystroke;
        private boolean nopopup = false;
        public SCOPE scope;

        public static Builder from(ExternalFinderItem item) {
            return new Builder().setName(item.getName()).setURLs(item.getURLs())
                    .setCommands(item.getCommands()).setKeyStroke(item.getKeystroke())
                    .setNopopup(item.isNopopup()).setScope(item.getScope());
        }

        /**
         * Optionally prepend <code>&amp;</code> to a character to set a
         * mnemonic for use in menus.
         */
        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public String getName() {
            return name;
        }

        public Builder addURL(ExternalFinderItemURL url) {
            urls.add(url);
            return this;
        }

        public Builder setURLs(List<ExternalFinderItemURL> urls) {
            this.urls.clear();
            this.urls.addAll(urls);
            return this;
        }

        public List<ExternalFinderItemURL> getURLs() {
            return urls;
        }

        public Builder addCommand(ExternalFinderItemCommand command) {
            commands.add(command);
            return this;
        }

        public Builder setCommands(List<ExternalFinderItemCommand> commands) {
            this.commands.clear();
            this.commands.addAll(commands);
            return this;
        }

        public List<ExternalFinderItemCommand> getCommands() {
            return commands;
        }

        public Builder setKeyStroke(KeyStroke keystroke) {
            this.keystroke = keystroke;
            return this;
        }

        public KeyStroke getKeyStroke() {
            return keystroke;
        }

        public Builder setNopopup(boolean nopopup) {
            this.nopopup = nopopup;
            return this;
        }

        public boolean isNopopup() {
            return nopopup;
        }

        public Builder setScope(SCOPE scope) {
            this.scope = scope;
            return this;
        }

        public SCOPE getScope() {
            return scope;
        }

        public ExternalFinderItem build() throws ExternalFinderValidationException {
            validate();
            return new ExternalFinderItem(this);
        }

        public void validate() throws ExternalFinderValidationException {
            if (name == null || name.isEmpty()) {
                throw new ExternalFinderValidationException(
                        OStrings.getString("EXTERNALFINDER_ITEM_ERROR_NAME"));
            }
            boolean hasUrls = urls != null && !urls.isEmpty();
            boolean hasCommands = commands != null && !commands.isEmpty();
            if (!hasUrls && !hasCommands) {
                throw new ExternalFinderValidationException(
                        OStrings.getString("EXTERNALFINDER_ITEM_ERROR_EMPTY", name));
            }
            if (scope == null) {
                throw new ExternalFinderValidationException(
                        OStrings.getString("EXTERNALFINDER_ITEM_ERROR_NOSCOPE"));
            }
        }
    }
}
