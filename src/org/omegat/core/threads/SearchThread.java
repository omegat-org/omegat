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

package org.omegat.core.threads;

import org.omegat.gui.TransFrame;
import org.omegat.gui.SearchWindow;
import org.omegat.util.StaticUtils;
import org.omegat.util.OStrings;
import org.omegat.util.OConsts;
import org.omegat.core.matching.SourceTextEntry;
import org.omegat.core.TransMemory;
import org.omegat.core.StringEntry;
import org.omegat.filters.FileHandler;
import org.omegat.filters.HandlerMaster;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Each search window has its own search thread to actually do the 
 * searching.  
 * This prevents lockup of the UI during intensive searches
 *
 * @author Keith Godfrey
 */
public class SearchThread extends Thread
{
	public SearchThread(TransFrame par, String startText)
	{
		m_window = new SearchWindow(par, this, startText);
		m_stop = false;
		m_searchDir = null;
		m_searchRecursive = false;
		m_searchText = "";	// NOI18N
		m_searching = false;
		m_tmSearch = false;

		m_numFinds = 0;
		m_curFileName = "";	// NOI18N

		m_extList = new ArrayList();
		m_extMapList = new ArrayList();

		// load mapping table
		// do this check so dialog cna be displayed independently
		StaticUtils.loadFileMappings(m_extList, m_extMapList);
	}

	/////////////////////////////////////////////////////////
	// public interface

    /**
     * Starts a search if another is not currently running.
     * To search current project only, set rootDir to null.
     *
     * @param text string to searh for
     * @param rootDir folder to search in
     * @param recursive search in subfolders of rootDir too
     * @param exact search for a substring
     * @param tm search in legacy and orphan TM strings too
     * @param keyword search for keywords
     * @return 0 on successful start, 1 on failure (i.e. search in progress)
     */
	public synchronized void requestSearch(String text, String rootDir,
			boolean recursive, boolean exact, boolean tm, boolean keyword)
	{
		if (!m_searching)
		{
			m_searchDir = rootDir;
			m_searchRecursive = recursive;
			m_searchText = text;
			m_exactSearch = exact;
			m_tmSearch = tm;
            m_keywordSearch = keyword;
			m_searching = true;
			interrupt();
			return;
		}
    }

	public void haltThread()
	{
		m_stop = true;
		interrupt();
	}

	///////////////////////////////////////////////////////////
	// thread main loop
	public void run()
	{
		// have search thread control search window to allow parent 
		//	window to avoid blocking
		// need to spawn subthread so we don't block either
		DialogThread dlgThread = new DialogThread(m_window);
		dlgThread.start();

		boolean firstPass = true;
		try 
		{
			while (!m_stop)
			{
				try { sleep(80); }
				catch (InterruptedException e) {
                }

				if (firstPass)
				{
					// on first pass send a request to place cursor in
					//	search field (otherwise search window has no
					//	control with default keyboard focus)
					// this is a hack, but can't find another way to do
					//	this gracefully
					firstPass = false;
					m_window.setSearchControlFocus();
				}
				
				if (m_searching)
				{
					// work to be done
					if (m_searchDir == null)
					{
						// if no search directory specified, then we are
						//	searching currnet project only
						searchProject();
					}
					else
					{
						// search specified directory tree
						try
						{
							searchFiles();
						}
						catch (IOException e)
						{
							// something bad happened 
							// alert user to badness
							String msg = OStrings.ST_FILE_SEARCH_ERROR;
							CommandThread.core.displayError(msg, e);
						}
					}
		
					// whatever the states is, error or not, display what's
					//	been found so far
					if (m_numFinds == 0)
					{
						// no match
						m_window.postMessage(OStrings.ST_NOTHING_FOUND);
					}
					m_window.displayResults();
					m_searching = false;
				}
			}
		}
		catch (RuntimeException re)
		{
			String msg = OStrings.ST_FATAL_ERROR;
			CommandThread.core.displayError(msg, re);
			m_window.threadDied();
		}
	}

	//////////////////////////////////////////////////////////////
	// internal functions

	private void foundString(int entryNum, String intro, String src,
			String target)
	{
		if (m_numFinds++ > OConsts.ST_MAX_SEARCH_RESULTS)
		{
			return;
		}
		
		if (entryNum >= 0)
		{
			// entries are referenced at offset 1 but stored at offset 0
			m_window.addEntry(entryNum+1, null, (entryNum+1)+"> "+src, target);	// NOI18N
		}
		else
		{
			m_window.addEntry(entryNum, intro, src, target);
		}

		if (m_numFinds >= OConsts.ST_MAX_SEARCH_RESULTS)
		{
			m_window.postMessage(OStrings.SW_MAX_FINDS_REACHED);
		}
	}

	private void searchProject()
	{
		m_numFinds = 0;
		if (m_exactSearch)
		{
			int i;
			for (i=0; i<CommandThread.core.numEntries(); i++)
			{
				SourceTextEntry ste = CommandThread.core.getSTE(i);
				String srcText = ste.getSrcText();
				String locText = ste.getTranslation();
				if (searchString(srcText, m_searchText) ||
                        searchString(locText, m_searchText))
				{
					// found a match - relay source and trans text
					foundString(i, null, srcText, locText);
					if (m_numFinds >= OConsts.ST_MAX_SEARCH_RESULTS)
					{
						break;
					}
				}
			}
			if (m_tmSearch)
			{
				ArrayList tmList = CommandThread.core.getTransMemory();
				TransMemory tm;
				for (i=0; i<tmList.size(); i++)
				{
					tm = (TransMemory) tmList.get(i);
					String srcText = tm.source;
					String locText = tm.target;
					if (searchString(srcText, m_searchText) ||
                            searchString(locText, m_searchText))
					{
						// found a match - relay source and trans text
						foundString(-1, tm.file, srcText, locText);
						if (m_numFinds >= OConsts.ST_MAX_SEARCH_RESULTS)
						{
							break;
						}
					}
				}
			}
		}
		else if( m_keywordSearch )
		{
            ArrayList searchTokens = new ArrayList();
            StaticUtils.tokenizeText(m_searchText, searchTokens);
			int i;
			for (i=0; i<CommandThread.core.numEntries(); i++)
			{
				SourceTextEntry ste = CommandThread.core.getSTE(i);
                List srcTokens = ste.getStrEntry().getSrcTokenList();
                List transTokens = ste.getStrEntry().getTransTokenList();
                
				if( StaticUtils.isSubset(searchTokens, srcTokens) || 
                        StaticUtils.isSubset(searchTokens, transTokens) )
				{
					// found a match - relay source and trans text
					foundString(i, null, ste.getStrEntry().getSrcText(), 
                            ste.getStrEntry().getTrans());
					if (m_numFinds >= OConsts.ST_MAX_SEARCH_RESULTS)
						break;
				}
			}
			if (m_tmSearch)
			{
				ArrayList tmList = CommandThread.core.getTransMemory();
				TransMemory tm;
				for (i=0; i<tmList.size(); i++)
				{
					tm = (TransMemory) tmList.get(i);
                    List srcTokens = new ArrayList();
                    StaticUtils.tokenizeText(tm.source, srcTokens);
                    List transTokens = new ArrayList();
                    StaticUtils.tokenizeText(tm.target, transTokens);
                    
    				if( StaticUtils.isSubset(searchTokens, srcTokens) || 
                            StaticUtils.isSubset(searchTokens, transTokens) )
					{
						// found a match - relay source and trans text
						foundString(-1, tm.file, tm.source, tm.target);
						if (m_numFinds >= OConsts.ST_MAX_SEARCH_RESULTS)
						{
							break;
						}
					}
				}
			}
		}
	}
	
	private void searchFiles() throws IOException
	{
		int i;
		int j;

		FileHandler fh;
		HandlerMaster hm = HandlerMaster.getInstance();
		ArrayList fileList = new ArrayList(256);
		if (!m_searchDir.endsWith(File.separator))
			m_searchDir += File.separator;

		StaticUtils.buildFileList(fileList, new File(m_searchDir), 
				m_searchRecursive);
		
		//TODO m_window.numberSearchFiles(fileList.size());
		int namePos = m_searchDir.length();

		String filename;

		for (i=0; i<fileList.size(); i++)
		{
			filename = (String) fileList.get(i);
			// determine actual file name w/ no root path info
			m_curFileName = filename.substring(namePos);

			int extPos = filename.lastIndexOf('.');
			String ext = filename.substring(extPos+1);
			// look for mapping of this extension
			for (j=0; j<m_extList.size(); j++)
			{
				if (ext.equals(m_extList.get(j)))
				{
					ext = (String) m_extMapList.get(j);
					break;
				}
			}

			// if file has handler, use it, otherwise do a binary search
			fh = hm.findPreferredHandler(ext);
			if (fh != null)
			{
				// make sure file hander in correct mode
				fh.setSearchMode(this);

				// don't bother to tell handler what we're looking for - 
				//	the search data is already known here (and the 
				//	handler is in the same thread, so info is not volatile)
				fh.load(filename);
			}
			else
			{
				// do binary search -- TODO
				System.out.println("Don't recognize file extension for '" +		// NOI18N
						filename + "' - omitting file from search");			// NOI18N
			}
		}
	}

	///////////////////////////////////////////////////////////////////////
	// search algorithm
	
	// look for the search text in the specified text
	// search supports wildcards * and ?
    private boolean searchString(String text, String search)
	{
		if (text == null || search == null)
			return false;

		char c, t;
//		String text = _text.toLowerCase();
//		String search = _search.toLowerCase();
		int searchLen = search.length();
		int textLen = text.length();
		int spos = 0;
		int pos = 0;
		int mark = 0;
		boolean inSeq = false;

		// handle the edge cases here
		if (textLen == 0)
		{	
			if (searchLen == 0)
				return true;
			else
			{
				// if only * remain in search string, that's OK
				while (spos < searchLen)
				{
					c = search.charAt(spos++);
					if (c != '*')
						return false;
				}
				return true;
			}
		}
		else if (searchLen == 0)
		{
			// text and no search string - must be OK
			return true;
		}

		// now do the search
		while (spos < searchLen)
		{
			c = search.charAt(spos);
			switch (c)
			{
				case '*':	// match zero or more characters
					spos++;
					if (inSeq)
					{
						// currently in a match sequence - recurse to check
						//	substring
						return searchString(text.substring(pos), 
									search.substring(spos));
					}
					// otherwise ignore
					break;

				case '?':	// match exactly one character
					spos++;
					if (++pos >= textLen)
					{
						// end of text encountered
						// we have match if search string finished or
						// if search string contains only *
						while (spos < searchLen)
						{
							c = search.charAt(spos++);
							if (c != '*')
								return false;
						}
						return true;
					}
					if (!inSeq)
					{
						// if not in a sequence, simply skip first text
						//	character (there is implied * at head of 
						//	each search string)
						return searchString(text.substring(pos), 
									search.substring(spos));
					}
					break;

				default:
					// regular text
					t = text.charAt(pos++);
					c = deflect(c);
					t = deflect(t);
					if (!inSeq)
					{
						// TODO case insensitive compare change goes here
						if (c == t)
						{
							inSeq = true;
							mark = pos;
							spos++;
						}
					}
					else 
					{
						if (c != t)
						{
							pos = mark;
							spos = 0;
							inSeq = false;
						}
						else
							spos++;
					}

					if (pos >= textLen)
					{
						if (inSeq)
						{
							// text overwith - if search string also done, 
							//	or if only has *s left, then a match
							//	otherwise not
							while (spos < searchLen)
							{
								c = search.charAt(spos++);
								if (c != '*')
									return false;
							}
							return true;
						}
						else
							return false;
					}
					break;
			}
        }

		// search string finished - if we're still in sequence, we must 
		//	have a successful match
		// otherwise, text ran out while searching and so no match
		return inSeq;
	}

	// this is a lookup table that is designed to map high ASCII 
	//	inflected characters to their non inflected equivalents, more 
	//	or less (i.e. ñ map to n)
	// it assumes that entered serach characters in the 192-255 range 
	//	match the UTF8 equivalents - something that may work on UTF8 
	//	operating systems (such as modern linux) and non-UTF8 operating 
	//	systems running Latin1, but maybe not on older systems using 
	//	different code pages
	// in the worst case certain extended characters will find matches
	//	in unrelated normal characters - in best case, someone will alter
	//	the below character map for their operating system and locale
	//	and recompile, should it not work as designed
	private final static char[] charMap =
	{	'A', 'A', 'A', 'A', 'A', 'A', 'A', 'C',
		'E', 'E', 'E', 'E', 'I', 'I', 'I', 'I', 
		'D', 'N', 'O', 'O', 'O', 'O', 'O', 'X',
		'O', 'U', 'U', 'U', 'U', 'Y', 'P', 'B', 
		'A', 'A', 'A', 'A', 'A', 'A', 'A', 'C', 
		'E', 'E', 'E', 'E', 'I', 'I', 'I', 'I', 
		'D', 'N', 'O', 'O', 'O', 'O', 'O', '/',
		'O', 'U', 'U', 'U', 'U', 'Y', 'B', 'Y' 
	};

	private static char deflect(char c)
	{
		if (c >= 'a' && c <= 'z')
			return (char) (c - ('a' - 'A'));
		else if (c >= 192 && c < 256)
			return charMap[c-192];
		else 
			return c;
	}

    /////////////////////////////////////////////////////////////////
	// interface used by FileHandlers
	
	public void searchText(String seg)
	{
		if (m_numFinds >= OConsts.ST_MAX_SEARCH_RESULTS)
		{
			return;
		}

		if (searchString(seg, m_searchText))
		{
			// found a match - do something about it
			foundString(-1, m_curFileName, seg, null);
		}
	}

	private SearchWindow	m_window;
	private boolean		m_stop;
	private boolean		m_searching;
	private String		m_searchText;
	private String		m_searchDir;
	private boolean		m_searchRecursive;
	private String		m_curFileName;
	private boolean		m_exactSearch;
	private boolean		m_tmSearch;
    private boolean     m_keywordSearch;

	private int			m_numFinds;

    private ArrayList		m_extList;
	private ArrayList		m_extMapList;
}

