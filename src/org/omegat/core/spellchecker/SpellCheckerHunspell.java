/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 Zoltan Bartko
               2009 Didier Briel
               2010 Alex Buloichik               
               Home page: http://www.omegat.org/               
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
 * @author Zoltan Bartko (bartkozoltan at bartkozoltan dot com)
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 */
public class SpellCheckerHunspell implements ISpellCheckerProvider {
    /** Local logger. */
    private static final Logger LOGGER = Logger
            .getLogger(SpellCheckerHunspell.class.getName());

    private Hunspell hunspell;

    /** the pointer to the hunspell class */
    private Pointer pHunspell;

    /** dictionary encoding */
    private String encoding;

    /**
     * Initialize the library for the given project. Loads the lists of ignored
     * and learned words for the project
     */
    public SpellCheckerHunspell(String language, String dictionaryName,
            String affixName) throws Exception {
        if (hunspell == null) {
            String libraryPath;
            if (Platform.isWebStart()) {
                libraryPath = Native
                        .getWebStartLibraryPath(OConsts.SPELLCHECKER_LIBRARY_NAME)
                        + File.separator
                        + mapLibraryName(OConsts.SPELLCHECKER_LIBRARY_NAME);
            } else {
                libraryPath = StaticUtils.installDir() + File.separator
                        + OConsts.NATIVE_LIBRARY_DIR + File.separator
                        + mapLibraryName(OConsts.SPELLCHECKER_LIBRARY_NAME);
            }

            hunspell = (Hunspell) Native.loadLibrary(libraryPath,
                    Hunspell.class);
            Log.log("Hunspell loaded successfully from " + libraryPath);
        }

        pHunspell = hunspell.Hunspell_create(affixName, dictionaryName);
        encoding = hunspell.Hunspell_get_dic_encoding(pHunspell);
        LOGGER.finer("Initialize SpellChecker by Hunspell for language '"
                + language + "' dictionary " + dictionaryName);
    }

    public void destroy() {
        hunspell.Hunspell_destroy(pHunspell);

        pHunspell = null;
    }

    public boolean isCorrect(String word) {
        boolean isCorrect = false;
        try {
            if (0 != hunspell.Hunspell_spell(pHunspell, prepareString(word))) {
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
            total = hunspell.Hunspell_suggest(pHunspell, strings,
                    prepareString(word));
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
                    while (bufferCursor < 100
                            && (current = pointerArray[i].getByte(bufferCursor)) != 0) {
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
            addWord(pHunspell, prepareString(word));
        } catch (UnsupportedEncodingException ex) {
            Log.log("Unsupported encoding " + encoding);
        }
    }

    /**
     * If Hunspell_add is not supported, whether this has already be recorded in
     * the log
     */
    private boolean addNotSupportedLogged = false;

    /**
     * Whether adding words to Hunspell works or not
     */
    private boolean addToHunspell = true;

    /**
     * Try to use Hunspell_add to add a word to the dictionnary. If that fails,
     * try to use Hunspell_put_word (old Hunspell librairies). If that fails
     * too, set hunspell to null
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
     * convert the string a byte array in the encoding of the dictionary and add
     * a terminating NUL to the end.
     */
    protected byte[] prepareString(String word)
            throws UnsupportedEncodingException {
        return (word + "\u0000").getBytes(encoding);
    }

    /**
     * amended version of System.mapLibraryName(). shamelessly stolen from JNA
     * (https://jna.dev.java.net)
     */
    private static String mapLibraryName(String libName) {

        if (Platform.isMac()) {
            if (libName.matches("lib.*\\.(dylib|jnilib)$")) {
                return libName;
            }
            String name = System.mapLibraryName(libName);
            // On MacOSX, System.mapLibraryName() returns the .jnilib extension
            // (the suffix for JNI libraries); ordinarily shared libraries have
            // a .dylib suffix
            if (name.endsWith(".jnilib")) {
                return name.substring(0, name.lastIndexOf(".jnilib"))
                        + ".dylib";
            }
            return name;
        } else if (Platform.isLinux()) {
            //
            // A specific version was requested - use as is for search
            //
            if (libName.matches("lib.*\\.so\\.[0-9]+$")) {
                return libName;
            }
            libName = libName + "-" + System.getProperty("os.arch");
        }

        return System.mapLibraryName(libName);
    }
}
