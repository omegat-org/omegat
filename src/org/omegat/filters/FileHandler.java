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

package org.omegat.filters;

import org.omegat.util.OStrings;
import org.omegat.gui.threads.CommandThread;
import org.omegat.gui.threads.SearchThread;

import java.io.*;

/**
 * The base class for all filters (aka file handlers).
 * Each filter should extend this class or one of its decendants
 *
 * @author Keith Godfrey
 */
public abstract class FileHandler
{
	public FileHandler(String type, String extension)
	{
		m_type = new String(type);
		m_preferredExtension = new String(extension);
		m_testMode = false;
		m_outputMode = false;
		m_outFile = null;
		m_searchMode = false;
		m_searchThread = null;
	}

	public String type()
	{
		return m_type;
	}

	public String preferredExtension()
	{
		return m_preferredExtension;
	}

    // when mode is set, output is now written and strings are passed
	//	to  supplied search thread
	public void setSearchMode(boolean mode, SearchThread search)
	{
		m_searchMode = mode;
		m_searchThread = search;
	}

	public void fileWriteError(IOException e) throws IOException
	{
		String str = OStrings.FH_ERROR_WRITING_FILE;
		throw new IOException(str + " - " + e);	// NOI18N
	}

	public String formatString(String text) 
	{
		// override in subclasses when formatting important
		return text;
	}

	protected void processEntry(StringBuffer buf, String file)
					throws IOException
	{
		processEntry(buf.toString(), file);
	}
	
	protected void processEntry(LBuffer buf, String file)
					throws IOException
	{
		processEntry(buf.string(), file);
	}
	
	protected void processEntry(String srcText, String file)
					throws IOException
	{
		if ((m_testMode) && (!m_outputMode))
		{
			System.out.println(" val: " + srcText);	// NOI18N
			System.out.println("file: " + file);	// NOI18N
			System.out.println("");					// NOI18N
		}
		else if (m_searchMode)
		{
			m_searchThread.searchText(srcText);
		}
		else if (m_outputMode)
		{
			// fetch translation and write it to outfile
			String s;
			if (m_testMode)
			{
				s = srcText;
				s = formatString(s);
				m_outFile.write(s + "-trans");	// NOI18N
			}
			else
			{
				s = CommandThread.core.getStringEntry(srcText).getTrans();
				if ((s == null) || (s.equals("")))	// NOI18N
					s = srcText;
				s = formatString(s);
				m_outFile.write(s);
			}
		}
		else
		{
			CommandThread.core.addEntry(srcText, file);
		}
	}

	public void write(String infile, String outfile) throws IOException
	{
		m_file = infile;
		m_outputMode = true;
		File of = new File(outfile);
		File pd;
		try
		{
			if (!m_testMode)
			{
				pd = of.getParentFile();
				if (pd == null)
					throw new IOException(OStrings.getString("FH_ERROR_INVALID_PROJECT_TREE"));
				if ((pd.isDirectory() == false) &&
						(pd.mkdirs() == false))
				{
					throw new IOException(
						OStrings.getString("FH_CANNOT_CREATE_TARGET_DIR_TREE")+ 
						"(" + pd.getAbsolutePath() + ")");	// NOI18N
				}
			}
			m_outFile = createOutputStream(infile, outfile);
			load(infile);
			m_outFile.close();
		}
		catch(IOException e)
		{
			m_outputMode = false;
			m_outFile = null;
			throw e;
		}
		m_outFile = null;
		m_outputMode = false;
	}

	/**
	 * Method to create an input stream to read the source file
	 */
	public BufferedReader createInputStream(String infile)
			throws IOException
	{
		FileInputStream fis = new FileInputStream(infile);
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader br = new BufferedReader(isr);
		return br;
	}

	/**
	 * Create output stream.
	 * Allow stream to have access to source file if necessary
	 */
	public BufferedWriter createOutputStream(String infile, String outfile)
			throws IOException
	{
		FileOutputStream fos = new FileOutputStream(outfile);
		OutputStreamWriter osw = new OutputStreamWriter(fos);
		BufferedWriter bw = new BufferedWriter(osw);
		return bw;
	}

	public abstract void doLoad() throws IOException;
	public void load(String file) throws IOException
	{
		reset();
		m_file = file;
		//m_in = new DataInputStream(new FileReader(file));
		
		try 
		{
			m_in = createInputStream(file);
	
			if (m_in == null)
			{
				throw new IOException(OStrings.getString("FD_ERROR_CANT_OPEN_INPUT_FILE") + file + "'");	// NOI18N
			}
			doLoad();
			m_in.close();
		}
		catch (IOException e)
		{
			try				{ if (m_in != null) m_in.close(); }
			catch (IOException e2)		{ ; }
			m_in = null;
			throw e;
		}
		m_in = null;
	}

	// push a character in front of the active stream
	protected void pushNextChar(char c)
	{
		m_pushChar = c;
	}

	public int getNextChar() throws IOException
	{
		int i;
		if (m_pushChar != 0)
		{
			i = m_pushChar;
			m_pushChar = 0;
		}
		else
		{
			i = m_in.read();
			if (i == 10)
			{
				// don't increment counter again on /r/m
				if (m_cr == false)
					m_line++;
				m_cr = false;
			}
			else if (i == 13)
			{
				m_cr = true;
				m_line++;
			}
			else
				m_cr = false;
		}
		return i;
	}

	protected String getNextLine() throws IOException
	{
		return m_in.readLine();
	}

	protected void markStream() throws IOException
	{
		m_in.mark(16);
	}

	protected void resetToMark() throws IOException
	{
		m_in.reset();
	}

	public void reset()
	{
		m_line = 0;
		m_cr = false;
		m_file = "";	// NOI18N
	}


	public int	line()	{ return m_line;	}
	public String	getType()	{ return m_type;		}

	private String m_type;
	private String m_preferredExtension;
	protected BufferedReader m_in;
	protected boolean m_testMode;
	protected boolean m_outputMode;
	protected BufferedWriter	m_outFile;

	protected boolean		m_searchMode;
	protected SearchThread	m_searchThread;

	private int		m_line = 0;
	private boolean		m_cr = false;
	private int		m_pushChar = 0;

	protected String	m_file = "";	// NOI18N
}
