//-------------------------------------------------------------------------
//  
//  OConsts.java - 
//  
//  Copyright (C) 2002, Keith Godfrey
//  
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//  
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//  
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//  
//  Build date:  23Feb2002
//  Copyright (C) 2002, Keith Godfrey
//  aurora@coastside.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------

class OConsts
{
public static final String HANDLER_LIST		= "file_extension_mapping";
public static final String IGNORE_LIST		= "ignore_file_list";

// project file consts
public static final String PROJ_FILENAME	= "omegat.project";
public static final String PROJ_PREFERENCE	= "omegat.prefs";
public static final String PROJ_CUR_VERSION = "1.0";

// tm file consts
public static final String TM_EXTENSION	= ".tm";
public static final String TM_FILE_IDENT = "omegat-tm";
public static final int TM_CUR_VERSION	= 1;
public static final String TMX_EXTENSION	= ".tmx";
public static final String TAB_EXTENSION	= ".tab";
public static final String TMW_EXTENSION	= ".wf";	// for wordfast

// status file consts
public static final String STATUS_EXTENSION	= "project_save.tmx";
public static final String STATUS_RECOVER_EXTENSION	= ".recover";
public static final String STATUS_FILE_IDENT = "omegat-status";
public static final String BACKUP_EXTENSION	= ".backup";
public static final int STATUS_CUR_VERSION = 2;

public static final String DEFAULT_SRC			= "source";
public static final String DEFAULT_LOC			= "target";
public static final String DEFAULT_GLOS			= "glossary";
public static final String DEFAULT_TM			= "tm";
public static final String DEFAULT_INTERNAL		= "omegat";

public static final String DEFAULT_FOLDER_MARKER	= "__DEFAULT__";

public static final String UTF8_END_OF_LIST		= "_x_";
public static final String ERR_LOG_FILE			= "err.txt";

public static final String WORD_CNT_FILE_EXT		= "word_counts";

public static final double NEAR_THRESH		= 0.4;
public static final double PAIR_THRESH		= 0.4;

// preference names
public static final String PREF_SRCLANG		= "source_lang";
public static final String PREF_LOCLANG		= "target_lang";
public static final String PREF_CUR_DIR		= "current_folder";
public static final String PREF_SRC_DIR		= "source_folder";
public static final String PREF_LOC_DIR		= "target_folder";
public static final String PREF_TM_DIR		= "tm_folder";
public static final String PREF_GLOS_DIR	= "glossary_folder";

//public static final int CUR_PROJFILE_VERS	= 1;
public static final int MAX_NEAR_STRINGS	= 5;

}
