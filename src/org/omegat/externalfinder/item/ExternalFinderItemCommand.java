/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Chihiro Hio
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

public class ExternalFinderItemCommand {

    private String command;
    private ExternalFinderItem.TARGET target;
    private ExternalFinderItem.ENCODING encoding;
    private String delimiter;

    public ExternalFinderItemCommand() {
    }

    public ExternalFinderItemCommand(String command, ExternalFinderItem.TARGET target, ExternalFinderItem.ENCODING encoding, String delimiter) {
        this.command = command;
        this.target = target;
        this.encoding = encoding;
        this.delimiter = delimiter;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public ExternalFinderItem.TARGET getTarget() {
        return target;
    }

    public void setTarget(ExternalFinderItem.TARGET target) {
        this.target = target;
    }

    public ExternalFinderItem.ENCODING getEncoding() {
        return encoding;
    }

    public void setEncoding(ExternalFinderItem.ENCODING encoding) {
        this.encoding = encoding;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }
}
