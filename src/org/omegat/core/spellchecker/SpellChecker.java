/*
 * SpellChecker.java
 *
 * Created on Pondelok, 2007, júl 30, 14:50
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 - Zoltan Bartko - bartkozoltan@bartkozoltan.com
               Home page: http://www.omegat.org/omegat/omegat.html
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

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import org.omegat.core.threads.CommandThread;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.Platform;
import org.omegat.util.StaticUtils;
import org.omegat.util.Preferences;

/**
 * The singleton spell checker class. Use getInstance() to retrieve the instance.
 * @author Zoltan Bartko (bartkozoltan at bartkozoltan dot com)
 */
public class SpellChecker {
    
    /**
     * The spell checker instance
     */
    private static SpellChecker INSTANCE = null;
    
    /**
     * The spell checking interface
     */
    private Hunspell hunspell;
    
    /** the list of ignored words */
    private ArrayList ignoreList = new ArrayList();
    
    /** the list of learned (valid) words */
    private ArrayList learnedList = new ArrayList();
    
    /** the pointer to the hunspell class */
    private Pointer pHunspell = null;
    
    /** dictionary encoding */
    private String encoding;
    
    /**
     * the file name with the ignored words
     */
    private String ignoreFileName;
    
    /**
     * the file name with the learned words
     */
    private String learnedFileName;
    
    /**
     * return an instance of the spell checker
     */
    public static SpellChecker getInstance() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        
        INSTANCE = new SpellChecker();
        return INSTANCE;
    }
    
    /** Creates a new instance of SpellChecker */
    protected  SpellChecker() {
        String libraryPath = StaticUtils.installDir()
            + File.separator
            + OConsts.NATIVE_LIBRARY_DIR
            + File.separator
            + mapLibraryName(OConsts.SPELLCHECKER_LIBRARY_NAME);
        
        hunspell = (Hunspell) Native.loadLibrary(libraryPath, Hunspell.class);
    }
    
    /**
     * Initialize the library for the given project. Loads the lists of ignored 
     * and learned words for the project
     */
    public void initialize() {
        
        // initialize the spell checker - get the data from the preferences
        
        String language = 
                CommandThread.core.getProjectProperties().getTargetLanguage()
                .getLocaleCode();
        
        String dictionaryDir = Preferences.getPreference(
                Preferences.SPELLCHECKER_DICTIONARY_DIRECTORY);
        
        if (dictionaryDir != null) {
            String affixName = dictionaryDir + File.separator + language +
                    OConsts.SC_AFFIX_EXTENSION;

            String dictionaryName = dictionaryDir + File.separator + language +
                    OConsts.SC_DICTIONARY_EXTENSION;

            pHunspell = hunspell.Hunspell_create(affixName, dictionaryName);
            
            encoding = hunspell.Hunspell_get_dic_encoding(pHunspell);
            
            // find out the internal project directory
            String projectDir = 
                    CommandThread.core.getProjectProperties().getProjectInternal();

            // load the ignore list

            ignoreFileName = projectDir + OConsts.IGNORED_WORD_LIST_FILE_NAME;

            fillWordList(ignoreFileName, ignoreList);

            // now the correct words

            learnedFileName = projectDir + OConsts.LEARNED_WORD_LIST_FILE_NAME;

            fillWordList(learnedFileName, learnedList);
            try {
                // load the learned words into the spell checker
                for (int i = 0; i < learnedList.size(); i++) {
                    hunspell.Hunspell_put_word(pHunspell, 
                            prepareString((String) learnedList.get(i)));
                }
            } catch (UnsupportedEncodingException ex) {
                Log.logRB("Unsupported encoding " + encoding);
            }
        }
    }
    
    /**
     * destroy the library
     */
    public void destroy() {
        if (pHunspell != null) {
            hunspell.Hunspell_destroy(pHunspell);
            
            // write the ignored and learned words to the disk
            dumpWordList(ignoreList, ignoreFileName);
            dumpWordList(learnedList, learnedFileName);
            
            pHunspell = null;
        }
    }
    
    /**
     * fill the word list (ignore or learned) with contents from the disk
     */
    private void fillWordList(String filename, ArrayList list) {
        list = new ArrayList();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filename));
            
            String thisLine;
            while ((thisLine = br.readLine()) != null) {
                list.add(thisLine);
            } 
        } catch (FileNotFoundException ex) {
            // discard this
        } catch (IOException ex) {
            // so now what?
        } finally {
            try {
                if (br != null) 
                    br.close();
            } catch (IOException ex) {
                // so now what?
            }
        }
    }
    
    /**
     * dump word list to a file
     */
    private void dumpWordList(ArrayList list, String filename) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(filename));
            
            for (int i = 0; i < list.size(); i++) {
                String text = (String) list.get(i);
                bw.write(text);
                bw.newLine();
            }
        } catch (IOException ex) {
            // so now what?
        } finally {
            try {
                if (bw != null)
                    bw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Check the word. If it is ignored or learned (valid), returns true. 
     * Otherwise false.
     */
    public boolean isCorrect(String word) {
        
        // if it is valid (learned), it is ok
        if (learnedList.contains(word) || ignoreList.contains(word))
            return true;
        try {
       
            if (0 != hunspell.Hunspell_spell(pHunspell, prepareString(word)))
                return true;
        } catch (UnsupportedEncodingException ex) {
            Log.log("Unsupported encoding "+encoding);
        }
        
        return false;
    }
    
    /**
     * return a list of strings as suggestions
     */
    public ArrayList suggest(String word) {
        ArrayList aList = new ArrayList();
        
        if (isCorrect(word))
            return aList;
        
        // the pointer to the string reference to be sent
        PointerByReference strings = new PointerByReference();
        
        String[] strings2 = new String[100];
        
        // total suggestions
        int total = 0;
        try {
            // try some wrong word
            total = hunspell.Hunspell_suggest(
                    pHunspell, strings, prepareString(word));
        } catch (UnsupportedEncodingException ex) {
            Log.log("Unsupported encoding "+encoding);
        }
        
        Pointer pointer = strings.getValue();
        Pointer[] pointerArray = pointer.getPointerArray(0,total);
        
        // convert it back
        Charset charset = Charset.forName(encoding);
        CharsetDecoder decoder = charset.newDecoder();
        
        for (int i = 0; i < total; i++) {
            try {
                // get the string
                int bufferCursor = 0;
                byte[] buffer = new byte[100];
                byte current;
                while (bufferCursor < 100 && 
                        (current = pointerArray[i].getByte(bufferCursor)) != 0) {
                    buffer[bufferCursor]=current;
                    bufferCursor++;
                }
                
                CharBuffer cbuf = decoder.decode(ByteBuffer.wrap(buffer));
                aList.add(cbuf.toString().trim());
            } catch (CharacterCodingException ex) {
                Log.log("Unsupported encoding "+encoding);
            }
        }
        
        return aList;
    }
    
    /**
     * Add a word to the list of ignored words
     */
    public void ignoreWord(String word) {
        if (!ignoreList.contains(word)) {
            ignoreList.add(word);
        }
    }
    
    /**
     * Add a word to the list of correct words
     */
    public void learnWord(String word) {
        if (!learnedList.contains(word)) {
            learnedList.add(word);
            try {
                hunspell.Hunspell_put_word(pHunspell, prepareString(word));
            } catch (UnsupportedEncodingException ex) {
                Log.log("Unsupported encoding "+encoding);
            }
        }
    }
    
    /**
     * convert the string a byte array in the encoding of the dictionary and 
     * add a terminating NUL to the end.
     */
    protected byte[] prepareString(String word) throws UnsupportedEncodingException {
        return (word+"\u0000").getBytes(encoding);
    } 
    
    /**
     * amended version of System.mapLibraryName().
     * shamelessly stolen from JNA (https://jna.dev.java.net)
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
                return name.substring(0, name.lastIndexOf(".jnilib")) + ".dylib";
            }
            return name;
        } else if (Platform.isLinux()) {
            //
            // A specific version was requested - use as is for search
            //
            if (libName.matches("lib.*\\.so\\.[0-9]+$")) {
                return libName;
            }
        } 
        
        return System.mapLibraryName(libName);
    }
}
