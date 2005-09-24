/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2005  Keith Godfrey et al
                          keithgodfrey@users.sourceforge.net
                          907.223.2039

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

package org.omegat.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Localizable strings.
 * After refactoring this class is useless (except for getString method),
 * but it's still here for legacy reasons.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 */
public class OStrings
{
	
	/** Resource bundle that contains all the strings */
	private static final ResourceBundle bundle = ResourceBundle.getBundle("org/omegat/Bundle");
	
	/** Returns a localized String for a key */
	public static String getString(String key) {
		return bundle.getString(key);
	}

	/** OmegaT Version */
	public static final String VERSION = 
		MessageFormat.format(getString("version-template"), 
			new Object[] {ResourceBundle.getBundle("org/omegat/Version").getString("version")});
            
	/** Human-Readable OmegaT Version */
	public static final String HUMAN_VERSION = 
		ResourceBundle.getBundle("org/omegat/Version").getString("version");

    // TransFrame
	public static final String TF_NUM_NEAR_AND_GLOSSARY	= getString("TF_NUM_NEAR_AND_GLOSSARY");
	public static final String TF_NUM_GLOSSARY = getString("TF_NUM_GLOSSARY");
	public static final String TF_NUM_NEAR = getString("TF_NUM_NEAR");
    public static final String TF_WARNING		= getString("TF_WARNING");
	public static final String TF_ERROR		= getString("TF_ERROR");
    public static final String TF_MENU_FILE		= getString("TF_MENU_FILE");
	public static final String TF_MENU_FILE_OPEN	= getString("TF_MENU_FILE_OPEN");
	public static final String TF_MENU_FILE_CREATE	= getString("TF_MENU_FILE_CREATE");
	public static final String TF_MENU_FILE_CLOSE	= getString("TF_MENU_FILE_CLOSE");
	public static final String TF_MENU_FILE_COMPILE	= getString("TF_MENU_FILE_COMPILE");
	public static final String TF_MENU_FILE_PROJWIN	= getString("TF_MENU_FILE_PROJWIN");
	public static final String TF_MENU_FILE_MATCHWIN= getString("TF_MENU_FILE_MATCHWIN");
	public static final String TF_MENU_FILE_SAVE	= getString("TF_MENU_FILE_SAVE");
	public static final String TF_MENU_FILE_QUIT	= getString("TF_MENU_FILE_QUIT");
	public static final String TF_MENU_EDIT		= getString("TF_MENU_EDIT");
	public static final String TF_MENU_EDIT_UNDO	= getString("TF_MENU_EDIT_UNDO");
	public static final String TF_MENU_EDIT_REDO	= getString("TF_MENU_EDIT_REDO");
	public static final String TF_MENU_EDIT_NEXT	= getString("TF_MENU_EDIT_NEXT");
	public static final String TF_MENU_EDIT_PREV	= getString("TF_MENU_EDIT_PREV");
	public static final String TF_MENU_EDIT_UNTRANS = getString("TF_MENU_EDIT_UNTRANS");
	public static final String TF_MENU_EDIT_FIND	= getString("TF_MENU_EDIT_FIND");
	public static final String TF_MENU_EDIT_INSERT	= getString("TF_MENU_EDIT_INSERT");
	public static final String TF_MENU_EDIT_RECYCLE	= getString("TF_MENU_EDIT_RECYCLE");

	public static final String TF_MENU_EDIT_COMPARE_1 = getString("TF_MENU_EDIT_COMPARE_1");
	public static final String TF_MENU_EDIT_COMPARE_2 = getString("TF_MENU_EDIT_COMPARE_2");
	public static final String TF_MENU_EDIT_COMPARE_3 = getString("TF_MENU_EDIT_COMPARE_3");
	public static final String TF_MENU_EDIT_COMPARE_4 = getString("TF_MENU_EDIT_COMPARE_4");
	public static final String TF_MENU_EDIT_COMPARE_5 = getString("TF_MENU_EDIT_COMPARE_5");
	public static final String TF_MENU_DISPLAY        = getString("TF_MENU_DISPLAY");

    public static final String TF_MENU_DISPLAY_FONT	= getString("TF_MENU_DISPLAY_FONT");
	public static final String TF_MENU_DISPLAY_ADVANCE	= getString("TF_MENU_DISPLAY_ADVANCE");

	public static final String TF_MENU_TOOLS		= getString("TF_MENU_TOOLS");
	public static final String TF_MENU_TOOLS_VALIDATE	= getString("TF_MENU_TOOLS_VALIDATE");

    public static final String TF_TM_LOAD_ERROR	= getString("TF_TM_LOAD_ERROR");
	public static final String TF_LOAD_ERROR	= getString("TF_LOAD_ERROR");
	public static final String TF_COMPILE_ERROR	= getString("TF_COMPILE_ERROR");

    public static final String TF_NOTICE_BAD_TAGS	= getString("TF_NOTICE_BAD_TAGS");
	public static final String TF_NOTICE_OK_TAGS	= getString("TF_NOTICE_OK_TAGS");
	public static final String TF_NOTICE_TITLE_TAGS	= getString("TF_NOTICE_TITLE_TAGS");

	// NOTE: segment start is assumed to contain "0000" string to overwrite
	//	with entry number.  If zeros not detected, entry number will not be
	//	displayed
	public static final String TF_CUR_SEGMENT_START		= 
            "\n"                                                                // NOI18N
            + getString("TF_CUR_SEGMENT_START");
	public static final String TF_CUR_SEGMENT_END		= 
            " "                                                                 // NOI18N
            + getString("TF_CUR_SEGMENT_END");
	
	public static final String TF_SELECT_SOURCE_FONT	= getString("TF_SELECT_SOURCE_FONT");
	public static final String TF_SELECT_FONTSIZE		= getString("TF_SELECT_FONTSIZE");
	public static final String TF_SELECT_FONTS_TITLE	= getString("TF_SELECT_FONTS_TITLE");
    public static final String TF_LOADING_FILE         = getString("TF_LOADING_FILE");
	public static final String TF_MATCH_VIEWER_TITLE   = getString("TF_MATCH_VIEWER_TITLE");

    // ContextFrame
	public static final String CF_SEARCH_RESULTS_SRC = getString("CF_SEARCH_RESULTS_SRC");
	public static final String CF_SEARCH_RESULTS_LOC = getString("CF_SEARCH_RESULTS_LOC");
	public static final String CF_BUTTON_CLOSE	= getString("BUTTON_CLOSE");
	
	// Project frame
	public static final String PF_BUTTON_CLOSE	= getString("BUTTON_CLOSE");
	public static final String PF_WINDOW_TITLE	= getString("PF_WINDOW_TITLE");
	public static final String PF_FILENAME		= getString("PF_FILENAME");
	public static final String PF_NUM_SEGMENTS	= getString("PF_NUM_SEGMENTS");
	
	// Help Frame
	public static final String HF_BUTTON_CLOSE	= getString("BUTTON_CLOSE");
	public static final String HF_BUTTON_HOME	= getString("BUTTON_HOME");
	public static final String HF_BUTTON_BACK	= getString("BUTTON_BACK");
	public static final String HF_WINDOW_TITLE	= getString("HF_WINDOW_TITLE");
	public static final String HF_CANT_FIND_HELP= getString("HF_CANT_FIND_HELP");
	// immortalize the BeOS 404 messages (some modified a bit for context)
	public static final String HF_HAIKU_1 = getString("HF_HAIKU_1");
	public static final String HF_HAIKU_2 = getString("HF_HAIKU_2");
	public static final String HF_HAIKU_3 = getString("HF_HAIKU_3");
	public static final String HF_HAIKU_4 = getString("HF_HAIKU_4");
	public static final String HF_HAIKU_5 = getString("HF_HAIKU_5");
	public static final String HF_HAIKU_6 = getString("HF_HAIKU_6");
	public static final String HF_HAIKU_7 = getString("HF_HAIKU_7");
	public static final String HF_HAIKU_8 = getString("HF_HAIKU_8");
	public static final String HF_HAIKU_9 = getString("HF_HAIKU_9");
	public static final String HF_HAIKU_10= getString("HF_HAIKU_10");
	public static final String HF_HAIKU_11= getString("HF_HAIKU_11");
	
	// CommandThread
	public static final String CT_FUZZY_X_OF_Y = getString("CT_FUZZY_X_OF_Y");
	public static final String CT_LOADING_PROJECT	= getString("CT_LOADING_PROJECT");
	public static final String CT_LOADING_GLOSSARY	= getString("CT_LOADING_GLOSSARY");
    public static final String CT_CANCEL_LOAD = getString("CT_CANCEL_LOAD");
	public static final String CT_LOADING_WORDCOUNT	= getString("CT_LOADING_WORDCOUNT");
	public static final String CT_ERROR_SAVING_PROJ	= getString("CT_ERROR_SAVING_PROJ");
    public static final String CT_ERROR_CREATE	= getString("CT_ERROR_CREATE");
	public static final String CT_FATAL_ERROR	= getString("CT_FATAL_ERROR");
	public static final String CT_DONT_RECOGNIZE_GLOS_FILE = getString("CT_DONT_RECOGNIZE_GLOS_FILE");
    public static final String CT_NO_FILE_HANDLER = getString("CT_NO_FILE_HANDLER");
	public static final String CT_LOAD_FILE_MX	= getString("CT_LOAD_FILE_MX");
	public static final String CT_COMPILE_FILE_MX	= getString("CT_COMPILE_FILE_MX");
	public static final String CT_COMPILE_DONE_MX	= getString("CT_COMPILE_DONE_MX");
	public static final String CT_COPY_FILE	= getString("CT_COPY_FILE");
	
	
	// ProjectProperties
	public static final String PP_CREATE_PROJ	= getString("PP_CREATE_PROJ");
	public static final String PP_OPEN_PROJ     = getString("PP_OPEN_PROJ");
    public static final String PP_SRC_ROOT		= getString("PP_SRC_ROOT");
	public static final String PP_LOC_ROOT		= getString("PP_LOC_ROOT");
	public static final String PP_GLOS_ROOT		= getString("PP_GLOS_ROOT");
	public static final String PP_TM_ROOT		= getString("PP_TM_ROOT");
	public static final String PP_SRC_LANG		= getString("PP_SRC_LANG");
	public static final String PP_LOC_LANG		= getString("PP_LOC_LANG");
	public static final String PP_BUTTON_OK		= getString("BUTTON_OK");
	public static final String PP_BUTTON_CANCEL	= getString("BUTTON_CANCEL");
    public static final String PP_BUTTON_BROWSE_SRC		= getString("PP_BUTTON_BROWSE_SRC");
	public static final String PP_BUTTON_BROWSE_TAR		= getString("PP_BUTTON_BROWSE_TAR");
	public static final String PP_BUTTON_BROWSE_GL		= getString("PP_BUTTON_BROWSE_GL");
	public static final String PP_BUTTON_BROWSE_TM		= getString("PP_BUTTON_BROWSE_TM");
	public static final String PP_BUTTON_SELECT			= getString("BUTTON_SELECT");
	public static final String PP_BROWSE_TITLE_SOURCE	= getString("PP_BROWSE_TITLE_SOURCE");
	public static final String PP_BROWSE_TITLE_TARGET	= getString("PP_BROWSE_TITLE_TARGET");
	public static final String PP_BROWSE_TITLE_GLOS		= getString("PP_BROWSE_TITLE_GLOS");
	public static final String PP_BROWSE_TITLE_TM		= getString("PP_BROWSE_TITLE_TM");
	public static final String PP_MESSAGE_BADLANG		= getString("PP_MESSAGE_BADLANG");
	public static final String PP_MESSAGE_BADPROJ		= getString("PP_MESSAGE_BADPROJ");
	public static final String PP_MESSAGE_CONFIGPROJ	= getString("PP_MESSAGE_CONFIGPROJ");
	public static final String PP_SAVE_PROJECT_FILE		= getString("PP_SAVE_PROJECT_FILE");

    public static final String LD_ERROR		= getString("LD_ERROR");

    // ProjectFileChooser
	public static final String PFC_OMEGAT_PROJECT = getString("PFC_OMEGAT_PROJECT");
	
	// NewFileChooser
	public static final String NDC_SELECT_UNIQUE = getString("NDC_SELECT_UNIQUE");
	public static final String NDC_SELECT_UNIQUE_TITLE	= getString("NDC_SELECT_UNIQUE_TITLE");
	
	// FileHandler
	public static final String FH_ERROR_WRITING_FILE = getString("FH_ERROR_WRITING_FILE");
	
	// SearchWindow
	public static final String SW_SEARCH_TEXT	= getString("SW_SEARCH_TEXT");
	public static final String SW_WORD_SEARCH	= getString("SW_WORD_SEARCH");
	public static final String SW_SEARCH		= getString("BUTTON_SEARCH");
	public static final String SW_EXACT_SEARCH	= getString("SW_EXACT_SEARCH");
	public static final String SW_SEARCH_TM		= getString("SW_SEARCH_TM");
	public static final String SW_LOCATION		= getString("SW_LOCATION");
	public static final String SW_BROWSE		= getString("SW_BROWSE");
	public static final String SW_DIR_SEARCH	= getString("SW_DIR_SEARCH");
	public static final String SW_DIR_RECURSIVE	= getString("SW_DIR_RECURSIVE");
	public static final String SW_TITLE			= getString("SW_TITLE");
	public static final String SW_BUTTON_SELECT	= getString("BUTTON_SELECT");
	public static final String SW_VIEWER_TEXT	= getString("SW_VIEWER_TEXT");
	public static final String SW_MAX_FINDS_REACHED	= getString("SW_MAX_FINDS_REACHED") + 
			" ("+OConsts.ST_MAX_SEARCH_RESULTS+")."; // NOI18N
	
	public static final String ST_FILE_SEARCH_ERROR = getString("ST_FILE_SEARCH_ERROR");
	public static final String ST_FATAL_ERROR =   getString("ST_FATAL_ERROR");
	public static final String ST_NOTHING_FOUND = getString("ST_NOTHING_FOUND");
	
	public static final String TF_INTRO_MESSAGE = getString("TF_INTRO_MESSAGE");
}
