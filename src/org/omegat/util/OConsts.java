/**************************************************************************
 * OmegaT - Java based Computer Assisted Translation (CAT) tool
 * Copyright (C) 2002-2004  Keith Godfrey et al
 * keithgodfrey@users.sourceforge.net
 * 907.223.2039
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.util;

/**
 * OmegaT-wide Constants
 *
 * @author Keith Godfrey
 */
public class OConsts
{

	// project file consts
	public static final String PROJ_EXTENSION	= ".project";						// NOI18N
	public static final String PROJ_FILENAME	= "omegat" + PROJ_EXTENSION;		// NOI18N
	public static final String PROJ_PREFERENCE	= "omegat.prefs";					// NOI18N
	public static final String PROJ_CUR_VERSION = "1.0";							// NOI18N
	
	public static final String TMX_EXTENSION	= ".tmx";							// NOI18N
	public static final String TMW_EXTENSION	= ".wf";	// for wordfast			// NOI18N
	
	// help
	public static final String HELP_HOME		= "en_OmegaT.html";					// NOI18N
	public static final String HELP_DIR			= "docs";							// NOI18N
	
	// file handler extensions
	public static final String FH_HTML_TYPE			= "htmlfile";					// NOI18N
	public static final String FH_XML_BASED_HTML	= "xhtml";						// NOI18N
	
	// status file consts
	public static final String STATUS_EXTENSION	= "project_save.tmx";				// NOI18N
	public static final String STATUS_RECOVER_EXTENSION	= ".recover";				// NOI18N
	public static final String BACKUP_EXTENSION	= ".backup";						// NOI18N
	
	public static final String DEFAULT_SRC			= "source";						// NOI18N
	public static final String DEFAULT_LOC			= "target";						// NOI18N
	public static final String DEFAULT_GLOS			= "glossary";					// NOI18N
	public static final String DEFAULT_TM			= "tm";							// NOI18N
	public static final String DEFAULT_INTERNAL		= "omegat";						// NOI18N
	
	public static final String DEFAULT_FOLDER_MARKER	= "__DEFAULT__";			// NOI18N
	
	public static final String WORD_CNT_FILE_EXT		= "word_counts";			// NOI18N
	
	public static final double NEAR_THRESH		= 0.6;								// NOI18N
	public static final double PAIR_THRESH		= 0.6;								// NOI18N
	
	// preference names
	public static final String PREF_SRCLANG		= "source_lang";					// NOI18N
	public static final String PREF_LOCLANG		= "target_lang";					// NOI18N
	public static final String PREF_CUR_DIR		= "current_folder";					// NOI18N
	public static final String PREF_SRC_DIR		= "source_folder";					// NOI18N
	public static final String PREF_LOC_DIR		= "target_folder";					// NOI18N
	public static final String PREF_TM_DIR		= "tm_folder";						// NOI18N
	public static final String PREF_GLOS_DIR	= "glossary_folder";				// NOI18N
	
	public static final String PREF_DISPLAY_W	= "screen_width";					// NOI18N
	public static final String PREF_DISPLAY_H	= "screen_height";					// NOI18N
	public static final String PREF_DISPLAY_X	= "screen_x";						// NOI18N
	public static final String PREF_DISPLAY_Y	= "screen_y";						// NOI18N
	public static final String PREF_MATCH_W		= "match_width";					// NOI18N
	public static final String PREF_MATCH_H		= "match_height";					// NOI18N
	public static final String PREF_MATCH_X		= "match_x";						// NOI18N
	public static final String PREF_MATCH_Y		= "match_y";						// NOI18N
	public static final String PREF_MNEMONIC	= "mnemonics";						// NOI18N
	public static final String PREF_TAB			= "tab_advance";					// NOI18N
	
	public static final String PREF_NUM_FILE_MAPPINGS	= "num_file_mappings";		// NOI18N
	public static final String PREF_FILE_MAPPING_N		= "file_mapping";			// NOI18N
	
	public static final String PREF_SEARCH_DIR	= "search_dir";						// NOI18N
	
	public static final String SW_DIR_CB_CHECKED_CMD	= "dir_ck";					// NOI18N
	
	public static final int		ST_MAX_SEARCH_RESULTS	= 50;
	
	public static final String TF_SRC_FONT_NAME		= "source_font";				// NOI18N
	public static final String TF_SRC_FONT_SIZE		= "source_font_size";			// NOI18N
	public static final String TF_FONT_DEFAULT		= "Courier";					// NOI18N
	public static final String TF_FONT_SIZE_DEFAULT	= "12";							// NOI18N
	
	public static final String XB_COMMENT_SHORTCUT	= "!comment";					// NOI18N
	
	public static final int MAX_NEAR_STRINGS	= 5;
	
}
