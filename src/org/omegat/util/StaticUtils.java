/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2004  Keith Godfrey et al
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

import java.util.List;
import org.omegat.core.threads.CommandThread;
import org.omegat.gui.messages.MessageRelay;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;


/**
 * Static functions taken from
 * CommandThread to reduce file size.
 *
 * @author Keith Godfrey
 */
public class StaticUtils
{
	private static void initPrefFileMappings()
	{
		PreferenceManager pref = CommandThread.core.getPrefManager();

		pref.setPreference(OConsts.PREF_FILE_MAPPING_N + "1", "htm html");		// NOI18N
		// common alternatives here are 'txt txt1' and 'txt txt2' for
		//	Latin1 and Latin2 encodings
		pref.setPreference(OConsts.PREF_FILE_MAPPING_N + "2", "txt utf8");		// NOI18N

		///////////////////////////////////////////////
		// OOo family mappings
		// spreadsheet
		pref.setPreference(OConsts.PREF_FILE_MAPPING_N + "3", "sxc sxw");		// NOI18N
		// master doc
		pref.setPreference(OConsts.PREF_FILE_MAPPING_N + "4", "sxg sxw");		// NOI18N
		// formula
		pref.setPreference(OConsts.PREF_FILE_MAPPING_N + "5", "sxm sxw");		// NOI18N
		// drawing
		pref.setPreference(OConsts.PREF_FILE_MAPPING_N + "6", "sxd sxw");		// NOI18N
		// presentation
		pref.setPreference(OConsts.PREF_FILE_MAPPING_N + "7", "sxi sxw");		// NOI18N

		pref.setPreference(OConsts.PREF_NUM_FILE_MAPPINGS, "7");				// NOI18N

		pref.save();
	}
	////////////////////////

	// load file mappings into specified handler master
	public static void loadFileMappings(ArrayList mapFrom, ArrayList mapTo)
	{
		PreferenceManager pref = CommandThread.core.getPrefManager();

		if (mapFrom == null || mapTo == null)
			return;

		String str = pref.getPreference(OConsts.PREF_NUM_FILE_MAPPINGS);
		if (str == null || str.equals(""))									// NOI18N
		{
			// probably an old pref file - update it
			initPrefFileMappings();

			// now try to read it again
			str = pref.getPreference(OConsts.PREF_NUM_FILE_MAPPINGS);
			if (str == null || str.equals(""))								// NOI18N
			{
				// we've got problems here
				String msg = OStrings.CT_PREF_LOAD_ERROR_MAPPINGS;
				MessageRelay.uiMessageDisplayError(
						CommandThread.core.getTransFrame(), msg, null);
				// just returning will cause trouble later, but at 
				//	least we're preparing the user
				// (this really shouldn't happen, but trap for it just
				//	in case)
				return;
			}
		}
		int num = Integer.decode(str).intValue();

		if (num <= 0)
			initPrefFileMappings();

		int pos;
		String mapn;
		for (int i=1; i<=num; i++)
		{
			mapn = OConsts.PREF_FILE_MAPPING_N + i;
			str = pref.getPreference(mapn);
			pos = str.indexOf(' ');
			mapFrom.add(str.substring(0, pos));
			mapTo.add(str.substring(pos+1));
System.out.println("mapping extension '"+str.substring(0,pos)+"' to '"+str.substring(pos+1)+"'");	// NOI18N
		}
	}

	// builds a list of format tags within the supplied string
	// format tags are HTML style <xxx> tags
	public static void buildTagList(String str, ArrayList tagList)
	{
		String tag = "";														// NOI18N
		char c;
		int state = 1;
		// extract tags from source string
		for (int j=0; j<str.length(); j++)
		{
			c = str.charAt(j);
			switch (state)
			{
				// 'normal' mode
				case 1:
					if (c == '<')
						state = 2;
					break;

				// found < - see if double or single
				case 2:
					if (c == '<')
					{
						// "<<" - let it slide
						state = 1;
					}
					else
					{
						tag = "";												// NOI18N
						tag += c;
						state = 3;
					}
					break;

				// copy tag
				case 3:
					if (c == '>')
					{
						tagList.add(tag);
						state = 1;
						tag = "";												// NOI18N
					}
					else
						tag += c;
					break;
			}
        }
	}

	// returns true if file starts w/ "<?xml"
	public static boolean isXMLFile(String filename)
	{
		try 
		{
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String s = in.readLine();
			if (s.charAt(0) == '<'		&&
                    s.charAt(1) == '?'	&&
                    s.charAt(2) == 'x'	&&
                    s.charAt(3) == 'm'	&&
                    s.charAt(4) == 'l')
			{
				// got a match
				// for now, assume version 1.0 and utf-8 encoding
				in.close();
				return true;
			}
			in.close();
		}
		catch (Exception e)
		{
			// if any exception occured, it is not an xml file 
		}
		return false;
	}

	// returns a list of all files under the root directory
	//  by absolute path
	public static void buildFileList(ArrayList lst, File rootDir, 
			boolean recursive)
	{
		int i;
		// read all files in current directory, recurse into subdirs
		// append files to supplied list
		File [] flist = rootDir.listFiles();
		for (i=0; i<Array.getLength(flist); i++)
		{
			if (flist[i].isDirectory())
			{
				continue;	// recurse into directories later
			}
			lst.add(flist[i].getAbsolutePath());
		}
		if (recursive)
		{
			for (i=0; i<Array.getLength(flist); i++)
			{
				if (flist[i].isDirectory())
				{
					// now recurse into subdirectories
					buildFileList(lst, flist[i], true);
				}
			}
		}
	}

    // returns a list of all files under the root directory
	//  by absolute path
	public static void buildDirList(ArrayList lst, File rootDir)
	{
		int i;
		// read all files in current directory, recurse into subdirs
		// append files to supplied list
		File [] flist = rootDir.listFiles();
		for (i=0; i<Array.getLength(flist); i++)
		{
			if (flist[i].isDirectory())
			{
				// now recurse into subdirectories
				lst.add(flist[i].getAbsolutePath());
				buildDirList(lst, flist[i]);
			}
		}
	}

	
	/**
	 * Builds a list of tokens and a list of their offsets w/in a file.
	 * It breaks string into tokens like in the following examples:
	 * <ul>
	 * <li> This is a semi-good way. -> "this", "is", "a", "semi-good", "way"
	 * <li> Fine, thanks, and you? -> "fine", "thanks", "and", "you"
	 * </ul>
	 * 
	 * @param str string to tokenize
	 * @param tokenList the list to add tokens to
	 * @return number of tokens
	 */
	public static int tokenizeText(String str, List tokenList)
	{
		str = str.toLowerCase();
		
		int len = str.length();
		boolean word = false;
		StringBuffer tokenString = new StringBuffer(len);
		int tokenStart = 0;
		int nTokens = 0;
		for(int i=0; i<len; i++)
		{
			char ch = str.charAt(i);
			if( word )
			{
				if( Character.isLetter(ch) )
				{
					tokenString.append(ch);
				}
				else
				{
					nTokens++;
					word = false;
					if( tokenList!=null )
					{
						Token token = new Token(tokenString.toString(), tokenStart);
						tokenList.add(token);
					}
				}
			}
			else
			{
				if( Character.isLetter(ch) )
				{
					if( !CJKUtils.isCJK(ch) )
					{
						word = true;
						tokenStart = i;
						tokenString.setLength(0);
						tokenString.append(ch);
					}
					else
					{
						nTokens++;
						if( tokenList!=null )
						{
							Token token = new Token(Character.toString(ch), i);
							tokenList.add(token);
						}
					}
					
				}
			}
		}
		
		if( word )
		{
			nTokens++;
			if( tokenList!=null )
			{
				Token token = new Token(tokenString.toString(), tokenStart);
				tokenList.add(token);
			}
		}
		
		return nTokens;
	}
	
    public static String[] getFontNames()
	{
		GraphicsEnvironment graphics;
		graphics = GraphicsEnvironment.getLocalGraphicsEnvironment();
		return graphics.getAvailableFontFamilyNames();
	}

}
