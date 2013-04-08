/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 Zoltan Bartko
               2009 Didier Briel
               2010 Alex Buloichik               
               Home page: http://www.omegat.org/               
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.spellchecker;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.Platform;
import org.omegat.util.StaticUtils;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Spell check implementation for use Hunspell.
 * 
 * System.mapLibraryName() converts base library name into platform-specific by adding prefix and suffix. It
 * doesn't care about path in library name, i.e. it just converts "/tmp/hunspell" into "lib/tmp/hunspell.so".
 * 
 * Linux : "hunspell-os64" -> "libhunspell-os64.so"
 * 
 * MacOS : "hunspell-os64" -> "libhunspell-os64.jnilib"
 * 
 * Windows : "hunspell-os64" -> "hunspell-os64.dll"
 * 
 * @author Zoltan Bartko (bartkozoltan at bartkozoltan dot com)
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 */
public class SpellCheckerHunspell implements ISpellCheckerProvider {
    /** Local logger. */
    private static final Logger LOGGER = Logger.getLogger(SpellCheckerHunspell.class.getName());

    private Hunspell hunspell;

    /** the pointer to the hunspell class */
    private Pointer pHunspell;

    /** dictionary encoding */
    private String encoding;

    /**
     * Initialize hunspell native library.
     */
    public SpellCheckerHunspell(String language, String dictionaryName, String affixName) throws Exception {
        if (hunspell == null) {
            String baseHunspellLib = getBaseHunspellLibraryName();
            if (baseHunspellLib == null) {
                // system not detected
                throw new ExceptionInInitializerError("System not recognized: os.name="
                        + System.getProperty("os.name") + " os.arch=" + System.getProperty("os.arch"));
            }
            String libraryPath;
            if (Platform.isWebStart()) {
                libraryPath = Native.getWebStartLibraryPath(baseHunspellLib) + File.separator
                        + mapLibraryName(baseHunspellLib);
            } else {
                libraryPath = StaticUtils.installDir() + File.separator + OConsts.NATIVE_LIBRARY_DIR
                        + File.separator + mapLibraryName(baseHunspellLib);
            }

            hunspell = (Hunspell) Native.loadLibrary(libraryPath, Hunspell.class);
            Log.log("Hunspell loaded successfully from " + libraryPath);
        }

        pHunspell = hunspell.Hunspell_create(affixName, dictionaryName);
        encoding = hunspell.Hunspell_get_dic_encoding(pHunspell);
        LOGGER.finer("Initialize SpellChecker by Hunspell for language '" + language + "' dictionary "
                + dictionaryName);
    }

    public void destroy() {
        hunspell.Hunspell_destroy(pHunspell);

        pHunspell = null;
    }

    public boolean isCorrect(String word) {
        boolean isCorrect = false;
        try {
            if (0 != hunspell.Hunspell_spell(pHunspell, prepareString(word, encoding))) {
                isCorrect = true;
            }
        } catch (UnsupportedEncodingException ex) {
            Log.log("Unsupported encoding " + encoding);
        }
        return isCorrect;
    }

    public List<String> suggest(String word) {
        // the pointer to the string reference to be sent
        PointerByReference strings = new PointerByReference();

        // total suggestions
        int total = 0;
        try {
            // try some wrong word
            total = hunspell.Hunspell_suggest(pHunspell, strings, prepareString(word, encoding));
        } catch (UnsupportedEncodingException ex) {
            Log.log("Unsupported encoding " + encoding);
        }

        Pointer[] pointerArray = null;
        Pointer pointer = strings.getValue();
        if (pointer != null)
            try {
                pointerArray = pointer.getPointerArray(0, total);
            } catch (NullPointerException ex) {
                // Just eat exception
            }

        List<String> aList = new ArrayList<String>();
        if (pointerArray != null) { // If there are sugggestions
            // convert it back
            Charset charset = Charset.forName(encoding);
            CharsetDecoder decoder = charset.newDecoder();

            for (int i = 0; i < total; i++) {
                try {
                    // get the string
                    int bufferCursor = 0;
                    byte[] buffer = new byte[100];
                    byte current;
                    while (bufferCursor < 100 && (current = pointerArray[i].getByte(bufferCursor)) != 0) {
                        buffer[bufferCursor] = current;
                        bufferCursor++;
                    }

                    CharBuffer cbuf = decoder.decode(ByteBuffer.wrap(buffer));
                    aList.add(cbuf.toString().trim());
                } catch (CharacterCodingException ex) {
                    Log.log("Unsupported encoding " + encoding);
                }
            }
        }
        return aList;
    }

    public void learnWord(String word) {
        try {
            addWord(pHunspell, prepareString(word, encoding));
        } catch (UnsupportedEncodingException ex) {
            Log.log("Unsupported encoding " + encoding);
        }
    }

    /**
     * If Hunspell_add is not supported, whether this has already be recorded in the log
     */
    private boolean addNotSupportedLogged = false;

    /**
     * Whether adding words to Hunspell works or not
     */
    private boolean addToHunspell = true;

    /**
     * Try to use Hunspell_add to add a word to the dictionnary. If that fails, try to use Hunspell_put_word
     * (old Hunspell librairies). If that fails too, set hunspell to null
     * 
     * @param pHunspell
     *            Pointer to the Hunspell class
     * @param word
     *            Word to add
     */
    private void addWord(Pointer pHunspell, byte[] word) {
        if (!addToHunspell)
            return;
        try {
            hunspell.Hunspell_add(pHunspell, word);
        } catch (Error err1) {
            if (!addNotSupportedLogged) {
                Log.log("Hunspell_add not supported");
                addNotSupportedLogged = true;
            }
            try {
                hunspell.Hunspell_put_word(pHunspell, word);
            } catch (Error err2) {
                Log.log("Hunspell_put_word not supported");
                addToHunspell = false;
            }
        }
    }

    /**
     * convert the string a byte array in the encoding of the dictionary and add a terminating NUL to the end.
     */
    protected static byte[] prepareString(String word, String encoding) throws UnsupportedEncodingException {
        return (word + "\u0000").getBytes(encoding);
    }

    /**
     * It returns base hunspell library name depends of system.
     */
    protected static String getBaseHunspellLibraryName() {
        switch (Platform.getOsType()) {
        case LINUX64:
            return "hunspell-linux64";
        case MAC64:
            return "hunspell-macos64";
        case WIN64:
            return "hunspell-win64";
        case LINUX32:
            return "hunspell-linux32";
        case MAC32:
            return "hunspell-macos32";
        case WIN32:
            return "hunspell-win32";
        default:
            return null;
        }
    }

    /**
     * Get hunspell dynamic library base name by platform.
     * 
     * We have to working with base name, because getWebStartLibraryPath() requires base name, not real
     * library filename.
     */
    protected static String mapLibraryName(String libName) {
        String result = System.mapLibraryName(libName);

        switch (Platform.getOsType()) {
        case MAC64:
        case MAC32:
            // On MacOSX, System.mapLibraryName() returns the .jnilib extension
            // (the suffix for JNI libraries); ordinarily shared libraries have
            // a .dylib suffix
            if (result.endsWith(".jnilib")) {
                result = result.substring(0, result.lastIndexOf(".jnilib")) + ".dylib";
            }
            break;
        }

        return result;
    }
}
