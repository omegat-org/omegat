/*
 Copyright (C) 2016 Chihiro Hio

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.hiohiohio.omegat.plugin.externalfinder.item;

public class ExternalFinderItemURL {

    private String URL;
    private ExternalFinderItem.TARGET target;
    private ExternalFinderItem.ENCODING encoding;

    public ExternalFinderItemURL() {
    }

    public ExternalFinderItemURL(String URL, ExternalFinderItem.TARGET target, ExternalFinderItem.ENCODING encoding) {
        this.URL = URL;
        this.target = target;
        this.encoding = encoding;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
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
}
