//-------------------------------------------------------------------------
//  
//  OStrings.java - 
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
//  Build date:  8Mar2003
//  Copyright (C) 2002, Keith Godfrey
//  keithgodfrey@users.sourceforge.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------

class OStrings {
/*
// TransPanel
public static final String TP_SRC_TEXT		= "Source Text:";
public static final String TP_TRANSLATION	= "Translation:";
public static final String TP_NUM_STRINGS	= "Number of strings:";
public static final String TP_CURRENT_STRING	= "Current string:";
public static final String TP_FILENAME		= "Filename:";
public static final String TP_UNIQUE_ID		= "String unique ID:";
public static final String TP_SEARCH		= "Search";
public static final String TP_SEARCH_KEYWORD	= "Keyword";
public static final String TP_SEARCH_EXACT	= "Exact";
public static final String TP_BUTTON_NEXT	= "Next";
public static final String TP_BUTTON_PREV	= "Previous";
*/
// MessageDialog
public static final String MD_BUTTON_OK		= "OK";

// TransFrame
public static final String TF_NUM_NEAR_AND_GLOSSARY	= 
	"{0,number,integer} fuzzy matche(s) and {1,number,integer} " +
	"glossary term(s)";
public static final String TF_NUM_GLOSSARY			=
	"Found {1,number,integer} glossary term(s)";
public static final String TF_NUM_NEAR				=
	"Found {0,number,integer} fuzzy matche(s)";
public static final String TF_FUZZY		= "Fuzzy matches...";
public static final String TF_GLOSSARY		= "Glossary terms";
public static final String TF_SRCTEXT		= "Source text";
public static final String TF_TRANSLATION	= "Translation";
public static final String TF_SCORE		= "Score";
public static final String TF_WARNING		= "Warning";
public static final String TF_ERROR		= "Error";
public static final String TF_NONE		= "--none--";
public static final String TF_MENU_FILE		= "File";
public static final String TF_MENU_FILE_OPEN	= "Open";
public static final String TF_MENU_FILE_CREATE	= "Create";
public static final String TF_MENU_FILE_COMPILE	= "Compile";
public static final String TF_MENU_FILE_PROJWIN	= "Show file list";
public static final String TF_MENU_FILE_SAVE	= "Save";
public static final String TF_MENU_FILE_QUIT	= "Quit";
public static final String TF_MENU_EDIT		= "Edit";
public static final String TF_MENU_EDIT_UNDO	= "Undo";
public static final String TF_MENU_EDIT_REDO	= "Redo";
public static final String TF_MENU_EDIT_NEXT	= "Next entry";
public static final String TF_MENU_EDIT_PREV	= "Previous entry";
public static final String TF_MENU_EDIT_FIND	= "Find";
//public static final String TF_MENU_EDIT_FINDEXACT	= "Find exact (target)";
public static final String TF_MENU_EDIT_INSERT
			= "Insert Translation";
public static final String TF_MENU_EDIT_RECYCLE
			= "Recycle Translation";
public static final String TF_MENU_EDIT_GOTO	= "Goto entry";
public static final String TF_MENU_EDIT_COMPARE_1
			= "Compare fuzzy match #1";
public static final String TF_MENU_EDIT_COMPARE_2
			= "Compare fuzzy match #2";
public static final String TF_MENU_EDIT_COMPARE_3
			= "Compare fuzzy match #3";
public static final String TF_MENU_EDIT_COMPARE_4
			= "Compare fuzzy match #4";
public static final String TF_MENU_EDIT_COMPARE_5
			= "Compare fuzzy match #5";
public static final String TF_MENU_DISPLAY			= "Configuration";
public static final String TF_MENU_DISPLAY_FUZZY	= "Fuzzy match info";
public static final String TF_MENU_DISPLAY_GLOSSARY	= "Glossary";
public static final String TF_MENU_DISPLAY_SOURCE	= "Source text";
public static final String TF_MENU_DISPLAY_FONT	= "Font";
//public static final String TF_MENU_SERVER	= "Server";
//public static final String TF_MENU_LANGUAGE	= "Language";
//public static final String TF_MENU_LANGUAGE_RESCAN	= "rescan";
public static final String TF_MENU_TOOLS		= "Tools";
public static final String TF_MENU_TOOLS_PSEUDO	= "Pseudo translate";
public static final String TF_MENU_TOOLS_VALIDATE	= "Validate tags";
public static final String TF_MENU_TOOLS_MERGE_TMX	= "Merge TMX files";
public static final String TF_MENU_VERSION_HELP		= "Help";
public static final String TF_TITLE		= "OmegaT";
public static final String TF_SELECT_LANGUAGE_TITLE	=
			"Select Language";
public static final String TF_SELECT_LANGUAGE	=
			"Please select a language";
public static final String TF_SELECT_LANGUAGE_FAILED	=
		"You failed to indicate a language.  Selecting one for you";
public static final String TF_TM_LOAD_ERROR	=
			"Failed to load translation memory for project.";
public static final String TF_LOAD_ERROR	=
			"Failed to load specified project.";
public static final String TF_COMPILE_ERROR	=
			"Failed to compile project files.";
//public static final String TF_SEARCH		= "Keyword search (source)";
//public static final String TF_SEARCH_EXACT	= "Exact search (target)";
//public static final String TF_CUR_STRING
//		= "Segment {0,number,integer} of {1,number,integer}";
//public static final String TF_NUM_WORDS		=
//	"{0,number,integer} of {2,number,integer}" +
//	" words left ({1,number,integer})";
//public static final String TF_GOTO_ENTRY	= "Goto string:";
public static final String TF_BUTTON_OK		= "OK";
public static final String TF_BUTTON_CANCEL	= "Cancel";
public static final String TF_FUZZY_CURRENT_PROJECT	= 
		"Current project";
public static final String TF_PSEUDOTRANS_RUSURE_TITLE	= "Warning";
public static final String TF_PSEUDOTRANS_RUSURE	=
	"This operation creates false translations for\n" +
	"all strings in the current project and cannot be\n" +
	"undone.  Are you sure you want to proceed?";
public static final String TF_NUM_FUZZY_MATCHES	=
	"Found {0,number,integer} fuzzy matches";
public static final String TF_NUM_FUZZY_MATCH	=
	"Found 1 fuzzy match";
public static final String TF_NOTICE_BAD_TAGS	= "Entries with modified tags";
public static final String TF_NOTICE_OK_TAGS	= "No tag errors were detected";
public static final String TF_NOTICE_TITLE_TAGS	= "Validating tags";
// NOTE: segment start is assumed to contain "0000" string to overwrite
//	with entry number.  If zeros not detected, entry number will not be
//	displayed
public static final String TF_CUR_SEGMENT_START		= "<segment 0000> ";
public static final String TF_CUR_SEGMENT_END		= " <end segment>";
public static final String TF_SELECT_SOURCE_FONT	= "Font";
public static final String TF_SELECT_FONTSIZE		= "Size";
//public static final String TF_SELECT_TARGET_FONT	= "Target font";
public static final String TF_SELECT_FONTS_TITLE	= "Select display font";
public static final String TF_BAD_LOCATION_POSSIBLE_CORRUPTION =
	"Potentially serious error encountered - somehow engine lost " +
	"synchronization with UI text fields.  Aborting execution and " +
	"gracefully crashing before corruption occurs.";
public static final String TF_LOADING_FILE			= "Loading file: ";
public static final String TF_MATCH_VIEWER_TITLE	= 
	"Match and Glossary Viewer";

// ContextFrame
public static final String CF_SEARCH_RESULTS_SRC	= 
	"Source language search results for: ";
public static final String CF_SEARCH_RESULTS_LOC	= 
	"Target language results for: ";
public static final String CF_BUTTON_CLOSE	= "Close";

// Project frame
public static final String PF_BUTTON_CLOSE	= "Close";
public static final String PF_WINDOW_TITLE	= "Project Files";
public static final String PF_FILENAME		= "File name";
public static final String PF_NUM_SEGMENTS	= "Number of segments";

public static final String HF_BUTTON_CLOSE	= "Close";
public static final String HF_BUTTON_HOME	= "Index";
public static final String HF_BUTTON_BACK	= "Back";
public static final String HF_WINDOW_TITLE	= "User Manual";
public static final String HF_CANT_FIND_HELP	= "Can't locate help file: ";

// CommandThread
public static final String CT_FUZZY_X_OF_Y	=
"Analyzing strings - finished {0,number,integer} of {1,number,integer}";
public static final String CT_TM_X_OF_Y	=
"Analyzing Translation Memory - {0,number,integer} of {1,number,integer}";

public static final String CT_LOADING_PROJECT	= "Loading project";
public static final String CT_LOADING_INDEX	= "Building index tables";
public static final String CT_LOADING_GLOSSARY	= "Loading glossary";
public static final String CT_LOADING_FUZZY	= "Loading fuzzy tables";
public static final String CT_CANCEL_LOAD = "Aborted project load";
public static final String CT_LOADING_WORDCOUNT	= "Building word counts";
public static final String CT_ERROR_SAVING_PROJ	= 
		"Error saving project file";
public static final String CT_ERROR_WRITING_NEARLOG	= 
		"Error encountered while writing fuzzy file";
public static final String CT_ERROR_CREATE	= 
		"Error creating project";
public static final String CT_FATAL_ERROR	= 
	"Fatal error encountered - saving project file with '" + 
	OConsts.STATUS_RECOVER_EXTENSION + "' backup extension.\n" +
	"  Please consult the documentation about your recovery options and\n" +
	"  return the below information to the software manufacturer with \n" +
	"  a description of how this error was produced.\n";
public static final String CT_DONT_RECOGNIZE_GLOS_FILE =
	"Can't recognize glossary file: ";
public static final String CT_ERROR_LOADING_HANDLER_FILE =
	"Encountered error loading extension mapping file\n " +
	"Ignoring handling instructions";
public static final String CT_ERROR_FINDING_HANDLER_FILE =
	"Warning - unable to locate find extension mapping file.";
public static final String CT_ERROR_FINDING_IGNORE_FILE =
	"Warning - unable to locate ignore file.";
public static final String CT_ERROR_LOADING_IGNORE_FILE =
	"Encountered error loading ignore file\nIgnoring ignore instructions";
public static final String CT_NO_FILE_HANDLER =
	"Can't associate parser for file extension";
public static final String CT_PREF_LOAD_ERROR_MAPPINGS =
	"Internal error (problems recovering file mappings from preference file)";
public static final String CT_HTMLX_MASQUERADE	=
	"Found XML based HTML file - using HTMLX parser";
public static final String CT_LOAD_FILE_MX	= "Loading: ";
public static final String CT_COMPILE_FILE_MX	= "Compiling: ";
public static final String CT_COMPILE_DONE_MX	= "Compile complete";

// ProjectProperties
public static final String PP_CREATE_PROJ	= "Create new project";
public static final String PP_PROJ_ROOT		= "Project root: ";
public static final String PP_PROJ_INTERNAL		= "OmegaT project files: ";
public static final String PP_SRC_ROOT		= "Source file root: ";
public static final String PP_LOC_ROOT		= "Loc file root: ";
public static final String PP_GLOS_ROOT		= "Glossary root:";
public static final String PP_TM_ROOT		= "Translation Memory root:";
public static final String PP_SRC_LANG		= 
		"Source language (i.e. EN-US, EN, ES-NI, DE, etc)";
public static final String PP_LOC_LANG		= 
		"Target language (i.e. DE-DE, EN-IE, ES, EN, etc)";
public static final String PP_BUTTON_OK		= "OK";
public static final String PP_BUTTON_CANCEL	= "Cancel";
public static final String PP_BUTTON_ADVANCED	= "Edit paths";
public static final String PP_PROJECT_NAME		= "Project name";
public static final String PP_BUTTON_BROWSE_SRC		= "Browse Source";
public static final String PP_BUTTON_BROWSE_TAR		= "Browse Target";
public static final String PP_BUTTON_BROWSE_GL		= "Browse Glossary";
public static final String PP_BUTTON_BROWSE_TM		= "Browse TM";
public static final String PP_BUTTON_SELECT		= "Select";
public static final String PP_BROWSE_TITLE_SOURCE	= "Select Source Directory";
public static final String PP_BROWSE_TITLE_TARGET	= "Select Target Directory";
public static final String PP_BROWSE_TITLE_GLOS	= "Select Glossary Directory";
public static final String PP_BROWSE_TITLE_TM	= 
	"Select Translation Memory Directory";
public static final String PP_MESSAGE_BADLANG	=
	"Specified language codes are invalid (TMX requires correct codes)";
public static final String PP_MESSAGE_BADPROJ	=
	"Some project folders seem to have moved.  Please find them.";
public static final String PP_MESSAGE_CONFIGPROJ	=
	"Specify custom project folders here.";
public static final String PP_SAVE_PROJECT_FILE	=
	"Create a new project";
public static final String PP_DEFAULT_PROJECT_NAME = "omegat.proj";
//public static final String PP_OMEGAT_PROJ_FILE	= "OmegaT project (.proj)";

// LogDisplay
public static final String LD_WARNING		= "Warning:";
public static final String LD_ERROR		= "ERROR";

// HandlerMaster
public static final String HM_MISSING_DIR	= 
		"Project directory not found";

// ProjectFileChooser
public static final String PFC_OMEGAT_PROJECT	= "OmegaT project folder";

// NewFileChooser 
public static final String NDC_SELECT_UNIQUE	=
	"That filename already exists.  Please select a new name.";
public static final String NDC_SELECT_UNIQUE_TITLE	= "Error";

// FileHandler
public static final String FH_ERROR_WRITING_FILE	= 
		"Error writing compiled output file";

public static final String SW_SEARCH_TEXT	= "Search for: ";
public static final String SW_WORD_SEARCH	= "Keyword search";
public static final String SW_SEARCH		= "Search";
public static final String SW_EXACT_SEARCH	= "Exact search";
public static final String SW_SEARCH_TM		= "Search TMs";
public static final String SW_LOCATION		= "Location";
public static final String SW_BROWSE		= "Select folder";
public static final String SW_DIR_SEARCH	= "Search files";
public static final String SW_DIR_RECURSIVE	= "Recursive search";
public static final String SW_TITLE		= "Text search";
public static final String SW_DISMISS		= "Dismiss window";
public static final String SW_BUTTON_SELECT	= "Select";
public static final String SW_VIEWER_TEXT	=
	"Exact searches look for the specified query string within the " +
	"current project or in any directory or directory tree.  Exact search " +
	"supports the wildcard search characters '*' and '?'.  The character " +
	"'*' means to match zero or more characters (the search term 'run*' " +
	"would match 'run', 'runs', and 'running', for example).  " +
	"The character '?' means to match exactly one character ('run?' " +
	"would match 'runs' and 'rung', for example, but not 'run' or " +
	"'running').  This operation searches both source and target " +
	"languages.\n\n" +
	"Keyword searches examine the source text and return all segments " +
	"which contain all of the query words, in any order.  This operation " +
	"can only be done on the source text of loaded projects.\n\n" + 
	"See documentation for details";
public static final String SW_MAX_FINDS_REACHED	= 
	"Maximum number of finds (" + OConsts.ST_MAX_SEARCH_RESULTS + 
	") has been reached.";

public static final String ST_FILE_SEARCH_ERROR =
	"Error searching directory tree";
public static final String ST_FATAL_ERROR = 
	"Search thread unexpectedly died - please close search window";
public static final String ST_NOTHING_FOUND = "Search returned no matches";

public static final String TF_INTRO_MESSAGE = 
	"Welcome to OmegaT\n";
//	"Welcome to OmegaT.\n\nTo begin a new translation project, select " +
//	"'" + TF_MENU_FILE_CREATE + "' from the file menu and select a " +
//	"place to save the new project.  During project creation, you will " + 
//	"be prompted to select several folder locations - using the default " +
//	"values should work just fine (but feel free to change these if you " +
//	"so desire).\nAfter creating the project, unzip or copy your source " +
//	"files into your project's \"source\" directory and put whatever TMX " +
//	"files you have available to you in the \"tm\" folder.  Then you are " +
//	"ready to open your project.";
};
