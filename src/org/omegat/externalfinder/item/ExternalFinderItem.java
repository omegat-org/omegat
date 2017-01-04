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
package org.omegat.externalfinder.item;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.KeyStroke;

public class ExternalFinderItem {

    public enum TARGET {

        // default BOTH for URL and command
        ASCII_ONLY, NON_ASCII_ONLY, BOTH
    }

    public enum ENCODING {

        // default DEFAULT for URL
        // default NONE for command
        DEFAULT, ESCAPE, NONE
    }

    private static final int UNDEFINED_KEYCODE = -2;

    private String name;
    private List<ExternalFinderItemURL> URLs;
    private List<ExternalFinderItemCommand> commands;
    private KeyStroke keystroke;
    private boolean nopopup = false;
    private Boolean asciiOnly = null;
    private Boolean nonAsciiOnly = null;
    private int keycode = UNDEFINED_KEYCODE;

    public ExternalFinderItem() {
        this.URLs = new ArrayList<ExternalFinderItemURL>();
        this.commands = new ArrayList<ExternalFinderItemCommand>();
    }

    public ExternalFinderItem(String name, List<ExternalFinderItemURL> URLs, List<ExternalFinderItemCommand> commands, KeyStroke keystroke, boolean nopopup) {
        int mnemonic = mnemonicPosition(name);
        if (mnemonic != -1) {
            this.name = name.substring(0, mnemonic) + name.substring(mnemonic + 1);
            this.keycode = (int) Character.toUpperCase(name.charAt(mnemonic + 1));
        } else {
            this.name = name;
            this.keycode = -1;
        }
        this.URLs = URLs;
        this.commands = commands;
        this.keystroke = keystroke;
        this.nopopup = nopopup;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        int mnemonic = mnemonicPosition(name);
        if (mnemonic != -1) {
            this.name = name.substring(0, mnemonic) + name.substring(mnemonic + 1);
            this.keycode = (int) Character.toUpperCase(name.charAt(mnemonic + 1));
        } else {
            this.name = name;
            this.keycode = -1;
        }
    }

    public List<ExternalFinderItemURL> getURLs() {
        return URLs;
    }

    public void setURLs(List<ExternalFinderItemURL> URLs) {
        this.asciiOnly = null;
        this.nonAsciiOnly = null;
        this.URLs = URLs;
    }

    public List<ExternalFinderItemCommand> getCommands() {
        return commands;
    }

    public void setCommands(List<ExternalFinderItemCommand> commands) {
        this.asciiOnly = null;
        this.nonAsciiOnly = null;
        this.commands = commands;
    }

    public KeyStroke getKeystroke() {
        return keystroke;
    }

    public void setKeystroke(KeyStroke keystroke) {
        this.keystroke = keystroke;
    }

    public boolean isNopopup() {
        return nopopup;
    }

    public void setNopopup(boolean nopopup) {
        this.nopopup = nopopup;
    }

    public boolean isAsciiOnly() {
        if (asciiOnly == null) {
            asciiOnly = isTargetOnly(TARGET.ASCII_ONLY);
        }

        return asciiOnly;
    }

    public boolean isNonAsciiOnly() {
        if (nonAsciiOnly == null) {
            nonAsciiOnly = isTargetOnly(TARGET.NON_ASCII_ONLY);
        }

        return nonAsciiOnly;
    }

    private boolean isTargetOnly(final TARGET target) {
        for (ExternalFinderItemURL url : URLs) {
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

    public int getKeycode() {
        return keycode;
    }

    private static int mnemonicPosition(String name) {
        int ret = name.indexOf("_");
        if (ret != -1 && (ret + 1) != name.length()) {
            char ch = name.charAt(ret + 1);
            if (ch == ' ') {
                ret = -1;
            }
        } else {
            ret = -1;
        }

        return ret;
    }

    public ExternalFinderItem replaceRefs(final ExternalFinderItem item) {
        this.name = item.name;
        this.URLs = item.URLs;
        this.commands = item.commands;
        this.keystroke = item.keystroke;
        this.nopopup = item.nopopup;
        this.keycode = item.keycode;

        this.asciiOnly = null; // item.isAsciiOnly();
        this.nonAsciiOnly = null; // item.isNonAsciiOnly();

        return this;
    }

    public static final boolean isASCII(String s) {
        for (int i = 0, n = s.length(); i < n; i++) {
            if ((int) s.charAt(i) > 0x7F) {
                return false;
            }
        }
        return true;
    }

    public static final URI generateURL(ExternalFinderItemURL url, String findingWords)
            throws UnsupportedEncodingException, URISyntaxException {
        String encodedWords;
        if (url.getEncoding() == ENCODING.NONE) {
            encodedWords = findingWords;
        } else {
            encodedWords = URLEncoder.encode(findingWords, "UTF-8");
            if (url.getEncoding() == ENCODING.ESCAPE) {
                encodedWords = encodedWords.replace("+", "%20");
            }
        }

        String replaced = url.getURL().replace("{target}", encodedWords);
        return new URI(replaced);
    }

    public static final String[] generateCommand(ExternalFinderItemCommand command, String findingWords) throws UnsupportedEncodingException {
        String encodedWords;
        if (command.getEncoding() == ENCODING.NONE) {
            encodedWords = findingWords;
        } else {
            encodedWords = URLEncoder.encode(findingWords, "UTF-8");
            if (command.getEncoding() == ENCODING.ESCAPE) {
                encodedWords = encodedWords.replace("+", "%20");
            }
        }

        String[] ret = command.getCommand().split(Pattern.quote(command.getDelimiter()));
        for (int i = 0; i < ret.length; i++) {
            String s = ret[i];
            ret[i] = s.replace("{target}", encodedWords);
        }

        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExternalFinderItem other = (ExternalFinderItem) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }
}
