/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

package org.omegat.core;

import org.omegat.core.data.CommandThread;
import org.omegat.core.data.IDataEngine;
import org.omegat.core.matching.ITokenizer;
import org.omegat.core.matching.Tokenizer;
import org.omegat.core.spellchecker.ISpellChecker;
import org.omegat.core.spellchecker.SpellChecker;
import org.omegat.gui.editor.EditorController;
import org.omegat.gui.editor.IEditor;
import org.omegat.gui.main.IMainWindow;
import org.omegat.gui.main.MainWindow;
import org.omegat.gui.matches.IMatcher;
import org.omegat.gui.tagvalidation.ITagValidation;
import org.omegat.gui.tagvalidation.TagValidationTool;
import org.omegat.util.Log;

/**
 * Class which contains all components instances.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class Core {
    private static IDataEngine dataEngine;
    private static IMainWindow mainWindow;
    private static IEditor editor;
    private static ITagValidation tagValidation;
    private static IMatcher matcher;
    private static ITokenizer tokenizer;
    private static ISpellChecker spellChecker;

    /** Get data engine instance. */
    public static IDataEngine getDataEngine() {
        return dataEngine;
    }

    /** Get main windows instance. */
    public static IMainWindow getMainWindow() {
        return mainWindow;
    }

    /** Get editor instance. */
    public static IEditor getEditor() {
        return editor;
    }

    /** Get tag validation component instance. */
    public static ITagValidation getTagValidation() {
        return tagValidation;
    }

    /** Get matcher component instance. */
    public static IMatcher getMatcher() {
        return matcher;
    }
    
    /** Get tokenizer component instance. */
    public static ITokenizer getTokenizer() {
        return tokenizer;
    }
    
    /** Get spell checker instance. */
    public static ISpellChecker getSpellChecker() {
        return spellChecker;
    }

    /**
     * Initialize application core from exists main components instances.
     * 
     * TODO: change initialization for instantiate component instances, instead
     * use already created instanced
     */
    public static void initialize(final String[] args) {
        MainWindow me = new MainWindow();
        
        // bugfix - Serious threading issue, preventing OmegaT from showing up...
        //          http://sourceforge.net/support/tracker.php?aid=1216514
        // we start command thread here...
        CommandThread.core = new CommandThread();
        
        me.setVisible(true);

        dataEngine = CommandThread.core;
        mainWindow = me;
        editor = new EditorController(me, me.editor, me.getEditorScroller());
        tagValidation = new TagValidationTool(me);
        matcher = me.matches;
        tokenizer = createComponent(ITokenizer.class, new Tokenizer(), args);
        spellChecker = new SpellChecker();
    }
    
    /**
     * Try to create component instance by class specified in command line.
     * 
     * @param <T>
     *                return type
     * @param interfaceClass
     *                component interface class
     * @param defaultImplementation
     *                default component implementation instance
     * @param args
     *                command line
     * @return component inmplementation
     */
    protected static <T> T createComponent(final Class<T> interfaceClass, final T defaultImplementation,
            final String[] args) {
        final String prefix = interfaceClass.getSimpleName() + "=";
        String implClassName = null;
        try {
            for (String arg : args) {
                if (arg.startsWith(prefix)) {
                    implClassName = arg.substring(prefix.length());
                    return (T) Class.forName(implClassName).newInstance();
                }
            }
        } catch (Exception ex) {
            Log.log(ex);
        }
        return defaultImplementation;
    }
}
