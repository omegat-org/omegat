/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Chihiro Hio, Aaron Madlon-Kay
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.externalfinder.item;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.omegat.util.OStrings;

/**
 * A data class representing an ExternalFinder "command". Immutable. Optionally
 * use {@link Builder} to construct.
 */
public class ExternalFinderItemCommand {

    private final String command;
    private final ExternalFinderItem.TARGET target;
    private final ExternalFinderItem.ENCODING encoding;
    private final String delimiter;

    public ExternalFinderItemCommand(String command, ExternalFinderItem.TARGET target,
            ExternalFinderItem.ENCODING encoding, String delimiter) {
        this.command = command;
        this.target = target;
        this.encoding = encoding;
        this.delimiter = delimiter;
    }

    public String getCommand() {
        return command;
    }

    public ExternalFinderItem.TARGET getTarget() {
        return target;
    }

    public ExternalFinderItem.ENCODING getEncoding() {
        return encoding;
    }

    public String getDelimiter() {
        return delimiter;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((command == null) ? 0 : command.hashCode());
        result = prime * result + ((delimiter == null) ? 0 : delimiter.hashCode());
        result = prime * result + ((encoding == null) ? 0 : encoding.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
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
        ExternalFinderItemCommand other = (ExternalFinderItemCommand) obj;
        if (command == null) {
            if (other.command != null) {
                return false;
            }
        } else if (!command.equals(other.command)) {
            return false;
        }
        if (delimiter == null) {
            if (other.delimiter != null) {
                return false;
            }
        } else if (!delimiter.equals(other.delimiter)) {
            return false;
        }
        if (encoding != other.encoding) {
            return false;
        }
        if (target != other.target) {
            return false;
        }
        return true;
    }

    public final String[] generateCommand(String findingWords) throws UnsupportedEncodingException {
        return generateCommand(command, delimiter, encoding, findingWords);
    }

    private static String[] generateCommand(String command, String delimiter,
            ExternalFinderItem.ENCODING encoding, String findingWords) throws UnsupportedEncodingException {
        String encodedWords;
        if (encoding == ExternalFinderItem.ENCODING.NONE) {
            encodedWords = findingWords;
        } else {
            encodedWords = URLEncoder.encode(findingWords, StandardCharsets.UTF_8.name());
            if (encoding == ExternalFinderItem.ENCODING.ESCAPE) {
                encodedWords = encodedWords.replace("+", "%20");
            }
        }

        String[] ret = command.split(Pattern.quote(delimiter));
        for (int i = 0; i < ret.length; i++) {
            String s = ret[i];
            ret[i] = s.replace(ExternalFinderItem.PLACEHOLDER_TARGET, encodedWords);
        }

        return ret;
    }

    public static final class Builder {
        private String command;
        private ExternalFinderItem.TARGET target = ExternalFinderItem.TARGET.BOTH;
        private ExternalFinderItem.ENCODING encoding = ExternalFinderItem.ENCODING.NONE;
        private String delimiter = "|";

        public static Builder from(ExternalFinderItemCommand item) {
            return new Builder().setCommand(item.getCommand()).setTarget(item.getTarget())
                    .setEncoding(item.getEncoding()).setDelimiter(item.getDelimiter());
        }

        public Builder setCommand(String command) {
            this.command = command;
            return this;
        }

        public String getCommand() {
            return command;
        }

        public Builder setTarget(ExternalFinderItem.TARGET target) {
            this.target = target;
            return this;
        }

        public ExternalFinderItem.TARGET getTarget() {
            return target;
        }

        public Builder setEncoding(ExternalFinderItem.ENCODING encoding) {
            this.encoding = encoding;
            return this;
        }

        public ExternalFinderItem.ENCODING getEncoding() {
            return encoding;
        }

        public Builder setDelimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public String getDelimiter() {
            return delimiter;
        }

        public ExternalFinderItemCommand build() throws ExternalFinderValidationException {
            validate();
            return new ExternalFinderItemCommand(command, target, encoding, delimiter);
        }

        /**
         * Check the current builder parameters to see if they constitute a valid command.
         *
         * @return A sample array of arguments illustrating what the output will look like
         * @throws ExternalFinderValidationException
         *             If any parameter is not valid
         */
        public String[] validate() throws ExternalFinderValidationException {
            if (command == null) {
                throw new ExternalFinderValidationException(
                        OStrings.getString("EXTERNALFINDER_COMMAND_ERROR_NOCOMMAND"));
            }
            if (!command.contains(ExternalFinderItem.PLACEHOLDER_TARGET)) {
                throw new ExternalFinderValidationException(OStrings.getString(
                        "EXTERNALFINDER_COMMAND_ERROR_NOTOKEN", ExternalFinderItem.PLACEHOLDER_TARGET));
            }
            if (target == null) {
                throw new ExternalFinderValidationException(
                        OStrings.getString("EXTERNALFINDER_COMMAND_ERROR_NOTARGET"));
            }
            if (encoding == null) {
                throw new ExternalFinderValidationException(
                        OStrings.getString("EXTERNALFINDER_COMMAND_ERROR_NOENCODING"));
            }
            if (delimiter == null) {
                throw new ExternalFinderValidationException(
                        OStrings.getString("EXTERNALFINDER_COMMAND_ERROR_NODELIMITER"));
            }
            if (delimiter.isEmpty()) {
                throw new ExternalFinderValidationException(
                        OStrings.getString("EXTERNALFINDER_COMMAND_ERROR_DELIMITEREMPTY"));
            }
            try {
                return generateSampleCommand();
            } catch (Throwable e) {
                throw new ExternalFinderValidationException(e);
            }
        }

        public String[] generateSampleCommand() throws UnsupportedEncodingException {
            String findingWords = target == ExternalFinderItem.TARGET.NON_ASCII_ONLY
                    ? "f\u00f8\u00f8 b\u00e5r" : "foo bar";
            return generateCommand(command, delimiter, encoding, findingWords);
        }
    }
}
