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
//  Build date:  4Dec2002
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

//public static final String HANDLER_UNKNOWN	= "unknown";
//public static final String HANDLER_IGNORE	= "ignore";

public static final String DEFAULT_PROJNAME		= "omegat";

// project file consts
public static final String PROJ_EXTENSION	= ".proj";
public static final String PROJ_FILE_IDENT = "omegat-proj";
public static final int PROJ_CUR_VERSION = 2;

// tm file consts
public static final String TM_EXTENSION	= ".tm";
public static final String TM_FILE_IDENT = "omegat-tm";
public static final int TM_CUR_VERSION = 1;

// fuzzy file consts
public static final String FUZZY_EXTENSION		= ".fuzzy";
public static final String FUZZY_FILE_IDENT = "omegat-fuzzy";
public static final int FUZZY_CUR_VERSION = 1;

// status file consts
public static final String STATUS_EXTENSION	= "project_save";
public static final String STATUS_RECOVER_EXTENSION	= ".recover";
public static final String STATUS_FILE_IDENT = "omegat-status";
public static final String BACKUP_EXTENSION	= ".backup";
public static final int STATUS_CUR_VERSION = 2;

public static final String DEFAULT_SRC			= "source";
public static final String DEFAULT_LOC			= "target";
public static final String DEFAULT_GLOS			= "glossary";
public static final String DEFAULT_TM			= "tm";
public static final String DEFAULT_INTERNAL		= "omegat";
public static final String DEFAULT_PROJROOT		= "test";

public static final String UTF8_END_OF_LIST		= "_x_";
public static final String ERR_LOG_FILE			= "err.txt";

public static final String WORD_CNT_FILE_EXT		= "word_counts";

public static final int DEFAULT_PROP_STATE	= 1;
public static final int GLOSSARY_PROP_STATE	= -1;

public static final double NEAR_THRESH		= 0.4;
public static final double PAIR_THRESH		= 0.4;

//public static final int CUR_PROJFILE_VERS	= 1;
public static final int MAX_NEAR_STRINGS	= 5;

}
