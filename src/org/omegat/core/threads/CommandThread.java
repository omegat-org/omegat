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

import org.omegat.core.matching.FuzzyMatcher;
import org.omegat.util.*;
import org.omegat.gui.TransFrame;
import org.omegat.gui.ProjectProperties;
import org.omegat.gui.ProjectFrame;
import org.omegat.gui.messages.MessageRelay;
import org.omegat.filters.html.HTMLParser;
import org.omegat.filters.xml.XMLStreamReader;
import org.omegat.filters.FileHandler;
import org.omegat.filters.HandlerMaster;
import org.omegat.core.*;

import java.io.*;
import java.text.ParseException;
import java.util.*;
import org.omegat.core.matching.SourceTextEntry;

/**
 * CommandThread is a thread to asynchronously do the stuff
 *
 * @author Keith Godfrey
 */
public class CommandThread extends Thread
{
	
    public CommandThread(TransFrame tf)
	{
		if (core != null)
			return;
		core = this;
		setName("Command thread"); // NOI18N
		m_transFrame = tf;
		m_config = new ProjectProperties();
		m_strEntryHash = new HashMap(4096);
		m_strEntryList = new ArrayList();
		m_glosEntryHash = new HashMap(2048);
		m_glosEntryList = new ArrayList();
		m_srcTextEntryArray = new ArrayList(4096);
		m_tmList = new ArrayList();
		m_orphanedList = new ArrayList();
		m_modifiedFlag = false;

		m_extensionList = new ArrayList(32);
		m_extensionMapList = new ArrayList(32);

		m_requestQueue = new LinkedList();
		m_projWin = null;
		m_saveCount = -1;
		m_saveThread = null;

		// static initialization
		HTMLParser.initEscCharLookupTable();

		m_prefManager = PreferenceManager.pref;
		if (m_prefManager == null)
			m_prefManager = new PreferenceManager();
	}

	public void run()
	{
		RequestPacket pack = new RequestPacket();
		m_saveThread = new SaveThread();
		try 
		{
			while (!m_stop)
			{
				try { sleep(40); }
				catch (InterruptedException e) {
                }
				pack.reset();
				messageBoardCheck(pack);
				switch (pack.type)
				{
					case RequestPacket.NO_OP:
						// do nothing
						break;
	
					case RequestPacket.LOAD:
						requestLoad(pack);
						break;

					case RequestPacket.SAVE:
						save();
						break;
				}
			}
			m_prefManager.save();

			m_saveThread.signalStop();
			m_saveThread.interrupt();
			core = null;
		}
		catch (RuntimeException re)
		{
			forceSave(true);
			String msg = OStrings.CT_FATAL_ERROR;
			m_transFrame.fatalError(msg, re);
		}
	}

	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////
	// message handling for external requests

	public void messageBoardPost(RequestPacket pack)
	{
		messageBoard(true, pack);
	}

	private void messageBoardCheck(RequestPacket pack)
	{
		messageBoard(false, pack);
	}

	private void messageBoard(boolean post,
						RequestPacket pack)
    {
        if (CommandThread.core == null)
            return;
        if (post) {
            m_requestQueue.add(pack);
            CommandThread.core.interrupt();
        } else {
            if (m_requestQueue.size() > 0) {
                pack.set((RequestPacket)
                        m_requestQueue.removeFirst());
            }
        }
}
	
	public void signalStop()
	{
		m_stop = true;
		CommandThread.core.interrupt();
	}
	
	public boolean shouldStop()
	{
		return m_stop;
	}

	public void requestUnload()
	{
		if (m_strEntryList.size() > 0)
		{
			// disable future saves
			if (m_saveCount >= 0)
				m_saveCount = 1;
			save();
		}

		// TODO freeze UI to prevent race condition
		m_strEntryHash.clear();
		m_glosEntryHash.clear();

		m_tmList.clear();
		m_orphanedList.clear();
		
		m_extensionList.clear();
		m_extensionMapList.clear();

		m_strEntryList.clear();
		m_glosEntryList.clear();
		m_srcTextEntryArray.clear();

		if (m_projWin != null)
		{
			if (m_projWin.isVisible())
			{
				m_projWin.setVisible(false);
				m_projWin.reset();
				m_projWin.buildDisplay();
				m_projWin.setVisible(true);
			}
			else
			{
				m_projWin.reset();
				m_projWin.buildDisplay();
			}
		}

		if (m_transFrame != null)
		{
			MessageRelay.uiMessageUnloadProject(m_transFrame);
		}
		
		m_totalWords = 0;
		m_partialWords = 0;
		m_currentWords = 0;

		m_nearProj = null;
	}

	private void requestLoad(RequestPacket pack)
	{
		TransFrame tf = (TransFrame) pack.obj;
		// load new project
		try
		{
			requestUnload();

			String evtStr;

			evtStr = OStrings.CT_LOADING_PROJECT;
			MessageRelay.uiMessageSetMessageText(tf, evtStr);
			if (!loadProject())
			{
				// loading of project cancelled
				evtStr = OStrings.CT_CANCEL_LOAD;
				MessageRelay.uiMessageSetMessageText(tf, evtStr);
				return;
			}
			if (numEntries() <= 0)
				throw new IOException("empty project");  // NOI18N
			tf.finishLoadProject();
			MessageRelay.uiMessageDisplayEntry(tf);
			if (m_saveCount == -1)
				m_saveThread.start();

			evtStr = OStrings.CT_LOADING_GLOSSARY;
			MessageRelay.uiMessageSetMessageText(tf, evtStr);
			buildGlossary();
			// evaluate strings for fuzzy matching 
			String status = OStrings.CT_FUZZY_X_OF_Y;
			
			buildNearList(m_strEntryList, status);

			// load in translation database files
			try
			{
				loadTM();
			}
			catch (IOException e)
			{
				String msg = OStrings.TF_TM_LOAD_ERROR;
				displayError(msg, e);
				// allow project load to resume
			}
			evtStr = OStrings.CT_LOADING_WORDCOUNT;
			MessageRelay.uiMessageSetMessageText(tf, evtStr);
			buildWordCounts();
			MessageRelay.uiMessageFuzzyInfo(tf);
			MessageRelay.uiMessageSetMessageText(tf, "");  // NOI18N

			// enable normal saves
			m_saveCount = 2;
		}
		catch (InterruptedException e1)
		{
			// user said cancel - this is OK
        }
		catch (IOException e)
		{
			String msg = OStrings.TF_LOAD_ERROR;
			displayError(msg, e);
			// don't know what happened - cancel load to be on the safe side
			requestUnload();
		}
	}


	///////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////
	// public interface

	// 'translate' all untranslated strings by either finding an 
	//  appropriate translation or prepending some text
	public void pseudoTranslate()
	{
		String str;
		SourceTextEntry srcTE;
		StringEntry se;
		for (int i=0; i<m_srcTextEntryArray.size(); i++)
		{
			srcTE = (SourceTextEntry) m_srcTextEntryArray.get(i);
			se = srcTE.getStrEntry();
			str = srcTE.getTranslation();
			if (str == null || str.equals("")) // NOI18N
			{
				str = "omega - " + se.getSrcText(); // NOI18N
			}
			se.setTranslation(str);
		}
	}

	private void buildTMXFile(String filename) throws IOException
	{
		int i;
		String s;
		String t;

		// build translation database files
		StringEntry se;

		// we got this far, so assume lang codes are proper
		String srcLang = getPreference(OConsts.PREF_SRCLANG);
		String locLang = getPreference(OConsts.PREF_LOCLANG);

		FileOutputStream fos = new FileOutputStream(filename);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8"); // NOI18N
		BufferedWriter out = new BufferedWriter(osw);
		
		String str = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";			 // NOI18N
		str += "<!DOCTYPE tmx SYSTEM \"tmx11.dtd\">\n";		 // NOI18N
		str += "<tmx version=\"1.1\">\n";					 // NOI18N
		str += "  <header\n";								 // NOI18N
		str += "    creationtool=\"org.omegat.OmegaT\"\n";	 // NOI18N
		str += "    creationtoolversion=\"1\"\n";			 // NOI18N
		str += "    segtype=\"paragraph\"\n";				 // NOI18N
		str += "    o-tmf=\"org.omegat.OmegaT TMX\"\n";		 // NOI18N
		str += "    adminlang=\"EN-US\"\n";					 // NOI18N
		str += "    srclang=\"" + srcLang + "\"\n";			 // NOI18N
		str += "    datatype=\"plaintext\"\n";				 // NOI18N
		str += "  >\n";										 // NOI18N
		str += "  </header>\n";								 // NOI18N
		str += "  <body>\n";								 // NOI18N
		out.write(str, 0, str.length());
	
		for (i=0; i<m_strEntryList.size(); i++)
		{
			se = (StringEntry) m_strEntryList.get(i);
			s = XMLStreamReader.makeValidXML(se.getSrcText());
			t = XMLStreamReader.makeValidXML(se.getTrans());
			if (t.equals(""))								 // NOI18N
				continue;									 // NOI18N
			str =  "    <tu>\n";							 // NOI18N
			str += "      <tuv lang=\"" + srcLang + "\">\n"; // NOI18N
			str += "        <seg>" + s + "</seg>\n";		 // NOI18N
			str += "      </tuv>\n";						 // NOI18N
			str += "      <tuv lang=\"" + locLang + "\">\n"; // NOI18N
			str += "        <seg>" + t + "</seg>\n";		 // NOI18N
			str += "      </tuv>\n";						 // NOI18N
			str += "    </tu>\n";							 // NOI18N
			out.write(str, 0, str.length());				 // NOI18N
		}
		TransMemory transMem;
		for (i=0; i<m_orphanedList.size(); i++)
		{
			transMem = (TransMemory) m_orphanedList.get(i);
			s = XMLStreamReader.makeValidXML(transMem.source);
			t = XMLStreamReader.makeValidXML(transMem.target);
			if (t.equals(""))								 // NOI18N
				continue;		
			str =  "    <tu>\n";							 // NOI18N
			str += "      <tuv lang=\"" + srcLang + "\">\n"; // NOI18N
			str += "        <seg>" + s + "</seg>\n";		 // NOI18N
			str += "      </tuv>\n";						 // NOI18N
			str += "      <tuv lang=\"" + locLang + "\">\n"; // NOI18N
			str += "        <seg>" + t + "</seg>\n";		 // NOI18N
			str += "      </tuv>\n";						 // NOI18N
			str += "    </tu>\n";							 // NOI18N
			out.write(str, 0, str.length());
		}
		str =  "  </body>\n";								// NOI18N
		str += "</tmx>\n";									// NOI18N
		out.write(str, 0, str.length());
		out.close();
	}
	
	// scan project and build list of entries which are suspected of 
	//  having changed (i.e. invalid) tag structures
	public ArrayList validateTags()
	{
		int i, j;
		String s;
		String t;
		ArrayList srcTags = new ArrayList(32);
		ArrayList locTags = new ArrayList(32);
		ArrayList suspects = new ArrayList(16);

		StringEntry se;
		SourceTextEntry ste;
		
		for (i=0; i<m_srcTextEntryArray.size(); i++)
		{
			ste = (SourceTextEntry) m_srcTextEntryArray.get(i);
			se = ste.getStrEntry();
			s = se.getSrcText();
			t = se.getTrans();
			// extract tags from src and loc string
			StaticUtils.buildTagList(s, srcTags);
			StaticUtils.buildTagList(t, locTags);
			
			// make sure lists match
			// for now, insist on exact match
			if (srcTags.size() != locTags.size())
				suspects.add(ste);
			else 
			{
				// compare one by one
				for (j=0; j<srcTags.size(); j++)
				{
					s = (String) srcTags.get(j);
					t = (String) locTags.get(j);
					if (!s.equals(t))
					{
						suspects.add(ste);
						break;
					}
				}
			}

			srcTags.clear();
			locTags.clear();
		}
		return suspects;
	}

	// build all translated files and create a new TM file
	public void compileProject() 
			throws IOException
	{
		if (m_strEntryHash.size() == 0)
			return;

		int i;
		int j;
		String srcRoot = m_config.getSourceRoot();
		String locRoot = m_config.getLocRoot();
		// keep track of errors - try to continue through processing
		boolean err = false;
		String s;
		String t;

		// save project first
		save();

		// remap extensions where necessary and ignore specified files
		FileHandler fh;
		HandlerMaster hm = HandlerMaster.getInstance();
		ArrayList fileList = new ArrayList(256);
		String filename;
		String destFileName;

		String fname = m_config.getProjectRoot() + m_config.getProjectName() +
					OConsts.TMX_EXTENSION;
		try
		{
			buildTMXFile(fname);
		}
		catch (IOException e)
		{
			System.out.println(OStrings.getString("CT_ERROR_CREATING_TMX"));
			err = true;
		}
		
		// build mirror directory of source tree
		StaticUtils.buildDirList(fileList, new File(srcRoot));
		File destFile;
		for (i=0; i<fileList.size(); i++)
		{
			filename = (String) fileList.get(i);
			destFileName = m_config.getLocRoot() + 
					filename.substring(m_config.getSourceRoot().length());
			destFile = new File(destFileName);
			if (!destFile.exists())
			{
				// target directory doesn't exist - create it
				if (!destFile.mkdir())
				{
					throw new IOException(OStrings.getString("CT_ERROR_CREATING_TARGET_DIR") + destFileName);
				}
			}
		}

		// build translated files
		fileList.clear();
		StaticUtils.buildFileList(fileList, new File(srcRoot), true);
		for (i=0; i<fileList.size(); i++)
		{
			filename = (String) fileList.get(i);
			destFileName = m_config.getLocRoot()+ 
						filename.substring(m_config.getSourceRoot().length());

			// determine actual extension
			int extPos = filename.lastIndexOf('.') + 1;
			String ext = filename.substring(extPos);
			// look for mapping of this extension
			for (j=0; j<m_extensionList.size(); j++)
			{
				if (ext.equals(m_extensionList.get(j)))
				{
					ext = (String) m_extensionMapList.get(j);
					break;
				}
			}

			// file not ignored and extension is mapped - time to 
			//  process it
			fh = hm.findPreferredHandler(ext);
			if (fh == null)
			{
				System.out.println(OStrings.CT_NO_FILE_HANDLER + " (." + ext + ")");								 // NOI18N
				// copy file to target tree
				System.out.println(OStrings.CT_COPY_FILE + 
						" '" +											 // NOI18N
						filename.substring(m_config.getSourceRoot().length()) +
						"'");											 // NOI18N
				LFileCopy.copy(filename, destFileName);
				continue;
			}
			if (fh.getType().equals(OConsts.FH_HTML_TYPE))
			{
				// preview file to see if we should use the XHTML parser
				if (StaticUtils.isXMLFile(filename))
				{
					// look for XHTML parser
					FileHandler fhx = hm.findPreferredHandler(
							OConsts.FH_XML_BASED_HTML);
					if (fhx != null)
					{
						System.out.println(OStrings.CT_HTMLX_MASQUERADE + 
								" (" + filename + ")");						 // NOI18N
						fh = fhx;
					}
					// else - htmlx parser gone... give it our best shot
				}
			}
			// shorten filename to that which is relative to src root
			String midName = filename.substring(srcRoot.length());
			s = srcRoot + midName;
			t = locRoot + midName;
			m_transFrame.setMessageText(OStrings.CT_COMPILE_FILE_MX + midName);
			fh.write(s, t);
		}
		m_transFrame.setMessageText(OStrings.CT_COMPILE_DONE_MX);

		if (err)
		{
			throw new IOException(OStrings.getString("CT_ERROR_BUILDING_TMX"));
		}

	}

	public void save()
	{
		if (!m_modifiedFlag)
			return;

		forceSave(false);
	}

	public void markAsDirty()
	{
		m_modifiedFlag = true;
	}

	private void forceSave(boolean corruptionDanger)
	{
		//synchronized (m_saveCriticalSection)
		{
			m_prefManager.save();
			
			if (m_saveCount <= 0)
				return;
			else if (m_saveCount == 1)
				m_saveCount = 0;

			String s = m_config.getProjectInternal() + OConsts.STATUS_EXTENSION;
			if (corruptionDanger)
			{
				s += OConsts.STATUS_RECOVER_EXTENSION;
			}
			else
			{
				// rename existing project file in case a fatal error
				//  is encountered during the write procedure - that way
				//  everything won't be lost
				File backup = new File(s + OConsts.BACKUP_EXTENSION);
				File orig = new File(s);
				if (orig.exists())
					orig.renameTo(backup);
			}

			try
			{
				buildTMXFile(s);
				m_modifiedFlag = false;
			}
			catch (IOException e)
			{
				String msg = OStrings.CT_ERROR_SAVING_PROJ;
				displayError(msg, e);
				// try to rename backup file to original name
				if (!corruptionDanger)
				{
					s = m_config.getProjectInternal() + OConsts.STATUS_EXTENSION;
					File backup = new File(s + OConsts.BACKUP_EXTENSION);
					File orig = new File(s);
					if (backup.exists())
						backup.renameTo(orig);
				}
			}

			// if successful, delete backup file
			if (!m_modifiedFlag && !corruptionDanger)
			{
				s = m_config.getProjectInternal() + OConsts.STATUS_EXTENSION;
				File backup = new File(s + OConsts.BACKUP_EXTENSION);
				if (backup.exists())
					backup.delete();
			}
		}
	}

	public void addEntry(String srcText)
	{
		// if the source string is empty, don't add it to TM
		if( srcText.length()==0 || srcText.trim().length()==0 )
			return;
		
		SourceTextEntry srcTextEntry;
		StringEntry strEntry;

		srcTextEntry = new SourceTextEntry();
		strEntry = (StringEntry) m_strEntryHash.get(srcText);
		if (strEntry == null)
		{
			// entry doesn't exist yet - create and store it
			strEntry = new StringEntry(srcText);
			m_strEntryHash.put(srcText, strEntry);
			m_strEntryList.add(strEntry);
		}

		srcTextEntry.set(strEntry, m_curFile, m_srcTextEntryArray.size());
		m_srcTextEntryArray.add(srcTextEntry);

	}

	public void createProject()
	{
		// create project directories
		// save project files (.proj .handlers .ignore)
		try
		{
			if (!m_config.createNew())
				return;	// cancel pressed

			// create project root directory
			File proj = new File(m_config.getProjectRoot());
			if (!proj.isDirectory())
			{
				if (!proj.mkdirs())
				{
					String msg = OStrings.CT_ERROR_CREATE;
					throw new IOException(msg);
				}
			}
			
			// create internal directory
			File internal = new File(m_config.getProjectInternal());
			if (!internal.isDirectory())
			{
				if (!internal.mkdirs())
				{
					String msg = OStrings.CT_ERROR_CREATE;
					throw new IOException(msg);
				}
			}
			
			// populate internal directory with project files
			//buildDefaultHandlerFile(hand);
			//buildDefaultIgnoreFile();
			
			// create src dir
			File src = new File(m_config.getSourceRoot());
			if (!src.isDirectory())
			{
				if (!src.mkdirs())
				{
					String msg = OStrings.CT_ERROR_CREATE + " (.../src/)";  // NOI18N
					throw new IOException(msg);
				}
			}

			// create glos dir
			File glos = new File(m_config.getGlossaryRoot());
			if (!glos.isDirectory())
			{
				if (!glos.mkdirs())
				{
					String msg = OStrings.CT_ERROR_CREATE + " (.../glos/)";	 // NOI18N
					throw new IOException(msg);
				}
			}

			// create TM dir
			File tm = new File(m_config.getTMRoot());
			if (!tm.isDirectory())
			{
				if (!tm.mkdirs())
				{
					String msg = OStrings.CT_ERROR_CREATE + " (.../tm/)";	 // NOI18N
					throw new IOException(msg);
				}
			}

			// create loc dir
			File loc = new File(m_config.getLocRoot());
			if (!loc.isDirectory())
			{
				if (!loc.mkdirs())
				{
					String msg = OStrings.CT_ERROR_CREATE + " (.../target/)"; // NOI18N
					throw new IOException(msg);
				}
			}

//			hand.buildHandlerList(m_config.getSrcRoot(),
//				m_config.getProjRoot(), m_config.getProjName());
			//mirrorSrcTree();
			m_config.buildProjFile();
		}
		catch(IOException e)
		{
			// trouble in tinsletown...
			String msg = OStrings.getString("CT_ERROR_CREATING_PROJECT");
			displayError(msg, e);
		}
	}

	/////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	// protected functions

	private void loadTranslations()
	{
		File proj;
		// load translation file (project_name.bin)
		try 
		{
			proj = new File(m_config.getProjectInternal() + OConsts.STATUS_EXTENSION);
			if (!proj.exists())
			{ 
				System.out.println(OStrings.getString("CT_ERROR_CANNOT_FIND_TMX")+ 
						"'" + proj + "'"); // NOI18N
				// nothing to do here
				return;
			}
		}
		catch(SecurityException se)
		{
			// file probably exists, but something's wrong
			String msg = OStrings.getString("CT_ERROR_ACCESS_PROJECT_FILE");
			displayError(msg, se);
			return;
		}
		
		try
		{
			// recover existing translations
			// since the source files may have changed since the last time
			//  they were loaded, load each string then look for it's 
			//  owner
			loadTMXFile(proj.getAbsolutePath(), "UTF-8", true); // NOI18N
		}
		catch (IOException e)
		{
			String msg = OStrings.getString("CT_ERROR_LOADING_PROJECT_FILE");
			displayError(msg, e);
		}
	}

	private boolean loadProject()
			throws IOException, InterruptedIOException
	{
		int i;
		int j;
        if (!m_config.loadExisting())
			return false;

		// first load glossary files
		File dir = new File(m_config.getGlossaryRoot());
		String [] fileList;
		String fname;
		String src;
		String loc;
		LTabFileReader tab = new LTabFileReader();
		if (dir.isDirectory())
		{
			fileList = dir.list();
			for (i=0; i<fileList.length; i++)
			{
				fname = fileList[i];
				// only support 2 file types for input now - tab and txt
				// and implement txt later TODO
				// 
				// print warning if glossary file is not recognized
				if (fname.endsWith(".tab")) // NOI18N
				{
					System.out.println(OStrings.getString("CT_LOADING_GLOSSARY") + fname);
					tab.load(dir.getAbsolutePath() + File.separator + fname);
					for (j=0; j<tab.numRows(); j++)
					{
						src = tab.get(j, 0);
						loc = tab.get(j, 1);
						String com = tab.get(j, 2);
						if (m_glosEntryHash.get(src) == null)
						{
							GlossaryEntry glosEntry = new GlossaryEntry(src,
									loc, com);
							m_glosEntryHash.put(src, glosEntry);
							m_glosEntryList.add(glosEntry);
						}
					}
				}
				else
				{
					System.out.println(OStrings.CT_DONT_RECOGNIZE_GLOS_FILE +
								fname);
				}
			}
		}
		else
		{
			// uh oh - something is screwed up here
			throw new IOException(OStrings.getString("CT_ERROR_ACCESS_GLOSSARY_DIR"));
		}

		// now open source files
		// to allow user ability to modify source files arbitrarily,
		//  we need to reload _everything_ on each startup
		// load handler file if it exists.  Use file extension 
		//  mappings there to map 
		// check each file against handler list to see if special
		//  instructions exist for file.  If not, guess file type
		//  based on filename extension.  If no match, ignore file.
		// check each file against ignore list also

		// now read handler list
		// handlers now stored in preference file
		StaticUtils.loadFileMappings(m_extensionList, m_extensionMapList);

		// ready to read in files
		// remap extensions where necessary and ignore specified files
		FileHandler fh;
		HandlerMaster hm = HandlerMaster.getInstance();
		ArrayList srcFileList = new ArrayList(256);
		File root = new File(m_config.getSourceRoot());
		StaticUtils.buildFileList(srcFileList, root, true);
		String filename;
		// keep track of how many entries are in each file
		for (i=0; i<srcFileList.size(); i++)
		{
			filename = (String) srcFileList.get(i);

			// determine actual extension
			int extPos = filename.lastIndexOf('.');
			String ext = filename.substring(extPos+1);
			// look for mapping of this extension
			for (j=0; j<m_extensionList.size(); j++)
			{
				if (ext.equals(m_extensionList.get(j)))
				{
					ext = (String) m_extensionMapList.get(j);
					break;
				}
			}

			// file not ignored and extension is mapped - time to 
			//  process it
			fh = hm.findPreferredHandler(ext);
			if (fh == null)
			{
				// System.out.println(OStrings.CT_NO_FILE_HANDLER + " (." + ext + ")");  // NOI18N
				continue;
			}
			if (fh.getType().equals(OConsts.FH_HTML_TYPE))
			{
				// preview file to see if we should use the htmlx parser
				if (StaticUtils.isXMLFile(filename))
				{
					// look for htmlx parser
					FileHandler fhx = hm.findPreferredHandler(
							OConsts.FH_XML_BASED_HTML);
					if (fhx != null)
						fh = fhx;
					// else - htmlx parser gone... give it our best shot
				}
			}
			// strip leading path information; feed file name to project
			//  window
			String filepath = filename.substring(
						m_config.getSourceRoot().length());
			m_projWin.addFile(filepath, numEntries());
			//m_projWin.addFile(filename, numEntries());
			//System.out.println(OStrings.getString("CT_LOADING_FILE")+	"'" + filepath + "'");  // NOI18N
			m_transFrame.setMessageText(OStrings.CT_LOAD_FILE_MX + filepath);
			m_curFile = new ProjectFileData();
			m_curFile.name = filename;
			m_curFile.firstEntry = m_srcTextEntryArray.size();
			fh.load(filename);
			m_curFile.lastEntry = m_srcTextEntryArray.size()-1;
		}
		m_transFrame.setMessageText(OStrings.getString("CT_LOAD_SRC_COMPLETE"));
		m_curFile = null;
		m_projWin.setNumEntries(numEntries());
		loadTranslations();
		m_projWin.buildDisplay();
		m_projWin.setVisible(true);
		m_projWin.toFront();
		return true;
	}

    private void buildGlossary()
	{
		int i;
		GlossaryEntry glosEntry;
		StringEntry strEntry;
		String glosStr;
		String glosStrLow;
		String s;
		int pos;
		//TreeMap foundList;
		for (i=0; i<m_glosEntryList.size(); i++)
		{
			glosEntry = (GlossaryEntry) m_glosEntryList.get(i);
			glosStr = glosEntry.getSrcText();
			// foundList = findAll(glosStr);
			// TODO - set locale in toLower call
			glosStrLow = glosStr.toLowerCase();
			// TODO - strip formating info

			//if (foundList == null)
			continue;

            /*
			// have narrowed down search field to only strings
			// containing words of glossary entries - now check
			// for exact match
			Object obj;
			while (foundList.size() > 0)
			{
				obj = foundList.firstKey();
				strEntry = (StringEntry) foundList.remove(obj);
				if (strEntry == null)
					continue;
				// TODO - set locale in toLower call
				s = strEntry.getSrcText().toLowerCase();
				pos = s.indexOf(glosStrLow);
				if (pos >= 0)
				{
					// found a match
					strEntry.addGlosString(glosEntry);
				}
			}
            */
		}
	}

	/**
	 * Builds the list of fuzzy matches between the source text strings.
	 *
	 * Old (Keith's Version)
	 *
	 * @author Keith Godfrey
	 * @param seList the list of string entries to match
	 * @param status status string to display
	 */
	private void buildNearList(ArrayList seList, String status) 
				throws InterruptedException
	{
		String project = null;
		if( seList!=m_strEntryList )
			project = m_nearProj;
		
		FuzzyMatcher matcher = new FuzzyMatcher(status, OConsts.NEAR_THRASH, m_transFrame, project, this);
		matcher.match(seList);
	}
		
	private void loadTM() throws IOException
	{
		// build strEntryList for each file
		// send to buildNearList
		String [] fileList;
		File f;
		int i;
		String fname;
		ArrayList strEntryList = new ArrayList(m_strEntryList.size());

		// foreach lang
		// foreach file
		// build string entry list
		// call build near list (entry list, status+filename)
		//buildNearList(m_strEntryList, status + " (" + fname + ")");

		String ext;
		strEntryList.clear();
		f = new File(m_config.getTMRoot());
		fileList = f.list();
		for (i=0; i<fileList.length; i++)
		{
			strEntryList.clear();
			fname = fileList[i];
			ext = fname.substring(fname.lastIndexOf('.'));
			fname = m_config.getTMRoot();
			if (!fname.endsWith(File.separator))
				fname += File.separator;
			fname += fileList[i];
			m_nearProj = fileList[i];

			if (ext.equalsIgnoreCase(OConsts.TMX_EXTENSION))
				loadTMXFile(fname, "UTF-8", false); // NOI18N
			else if (ext.equalsIgnoreCase(OConsts.TMW_EXTENSION))
				loadTMXFile(fname, "ISO-8859-1", false); // NOI18N
		}
		m_nearProj = null;
	}

	private void loadTMXFile(String fname, String encoding, boolean isProject)
		throws IOException
	{
		TMXReader tmx = new TMXReader(encoding);
		String src;
		String trans;
		StringEntry se;
		TransMemory tm;
		try
		{
			tmx.loadFile(fname);
			
			// RFE 1001918 - backing up project's TMX upon successful read
			if( isProject )
				LFileCopy.copy(fname, fname+".bak");							// NOI18N
			
			int num = tmx.numSegments();
			ArrayList strEntryList = new ArrayList(num+2);
			for (int i=0; i<num; i++)
			{
				src = tmx.getSourceSegment(i);
				trans = tmx.getTargetSegment(i);

				if (isProject)
				{
					se = (StringEntry) m_strEntryHash.get(src);
					if (se == null)
					{
						// loading a project save file and the
						//	old entry can't be found - source files
						//	must have changed
						// remember it anyways
						tm = new TransMemory(src, trans, fname);
						m_orphanedList.add(tm);
						m_tmList.add(tm);
						se = new StringEntry(src);
						se.setTranslation(trans);
						int wc = StaticUtils.tokenizeText(src, null);
						se.setWordCount(wc);
					}
					se.setTranslation(trans);
					strEntryList.add(se);
				}
				else		
				{
					// not in a project - remember this as a translation
					//	memory string and add it to near list
					m_tmList.add(new TransMemory(src, trans, fname));
					se = new StringEntry(src);
					se.setTranslation(trans);
					int wc = StaticUtils.tokenizeText(src, null);
					se.setWordCount(wc);
					strEntryList.add(se);
				}
			}
			/*
			if (strEntryList.size() > 0)
			{
				buildNearList(strEntryList, status + " (" + fname + ")");		// NOI18N
			} //mihmax */
		}
		catch (ParseException e)
		{
			throw new IOException(OStrings.getString("CT_ERROR_PARSEERROR")+ 
					"'" + fname + "'\n" +  e); // NOI18N
		}
	}

    void displayError(String msg, Throwable e)
	{
		if (m_transFrame == null)
		{
			System.out.println(OStrings.LD_ERROR + " " + msg); // NOI18N
		}
		else
			MessageRelay.uiMessageDisplayError(m_transFrame, msg, e);
	}

	private void buildWordCounts()
	{
		ListIterator it;
		StringEntry se;
		LinkedList pl;
		m_totalWords = 0;
		m_partialWords = 0;
		int words;
		it = m_strEntryList.listIterator();
		while(it.hasNext())
		{
			se = (StringEntry) it.next();
			pl = se.getParentList();
			words = se.getWordCount();
			m_partialWords += words;
			m_totalWords += words * pl.size();
		}

		// now dump file based word counts to disk
		String fn = m_config.getProjectInternal() + OConsts.WORD_CNT_FILE_EXT;
		FileWriter ofp = null;
		try 
		{
			ofp = new FileWriter(fn);
			ofp.write(OStrings.getString("CT_WORD_COUNT_UNIQUE")+ 
					m_partialWords + "\n"); // NOI18N
			ofp.write(OStrings.getString("CT_WORD_COUNT_TOTAL")	+ 
					m_totalWords + "\n"); // NOI18N
			it = m_srcTextEntryArray.listIterator();
			SourceTextEntry ste;
			String curFile = ""; // NOI18N
			String file;
			int totWords = 0;
			while(it.hasNext())
			{
				ste = (SourceTextEntry) it.next();
				file = ste.getSrcFile().name;
				if (curFile.compareTo(file) != 0)
				{
					if (curFile.length() > 0)
						ofp.write(curFile + "\t" + totWords +"\n");  // NOI18N
					curFile = file;
					totWords = 0;
				}
				words = ste.getStrEntry().getWordCount();
				totWords += words;
				m_currentWords += words;
			}
			if (curFile.length() > 0)
			{
				ofp.write(curFile + "\t" + totWords +"\n\n"); // NOI18N
			}
			ofp.write(OStrings.getString("CT_WORD_REMAINING") + 
					m_currentWords + "\n"); // NOI18N
			ofp.close();
		}
		catch (IOException e)
		{
			try { if (ofp != null) ofp.close();	}
			catch (IOException e2) {
            }
		}
	}

//	protected void registerNear(org.omegat.StringEntry strEntry,
//					org.omegat.StringEntry cand, double ratio,
//					ArrayList parDataArray, ArrayList nearDataArray)
//	{
//		int i;
//		byte[] pData = new byte[parDataArray.size()];
//		for (i=0; i<pData.length; i++)
//			pData[i] = ((org.omegat.StringData) parDataArray.get(i)).getAttr();
//		byte[] nData = new byte[nearDataArray.size()];
//		for (i=0; i<nData.length; i++)
//			nData[i] = ((org.omegat.StringData)nearDataArray.get(i)).getAttr();
//
//		strEntry.addNearString(cand, ratio, pData, nData, null);
//	}
//
	/////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	// static functions

//	// return 0 for normal char, 1 for imbeddable, 2 for deletable
//	protected static int charType(char c)
//	{
//		int type = 0;
//		if ((c == '.') || (c == '-') || (c == '@'))
//		{
//			// allow URL, email and hypenated words
//			type = 1;
//		}
//		else
//		{
//			int t = Character.getType(c);
//			switch (t) {
//				case Character.DASH_PUNCTUATION:
//				case Character.START_PUNCTUATION:
//				case Character.END_PUNCTUATION:
//				case Character.CONNECTOR_PUNCTUATION:
//				case Character.OTHER_PUNCTUATION:
//				case Character.MATH_SYMBOL:
//				case Character.CURRENCY_SYMBOL:
//				case Character.MODIFIER_SYMBOL:
//					type = 2;
//				default:
//					type = 0;
//			};
//		}
//		return type;
//	}


	////////////////////////////////////////////////////////////////
	// preference interface
	
	// access in manager is synchronized, so out of sync requests are OK
	// make it available to external objects upon request
	public PreferenceManager getPrefManager()	{ return m_prefManager; }

	public String getPreference(String str)
	{
		return m_prefManager.getPreference(str);
	}

	public void setPreference(String name, String value)
	{
		m_prefManager.setPreference(name, value);
	}

	public void savePreferences()
	{
		m_prefManager.save();
	}

	public String getOrSetPreference(String name, String value)
	{
		String val = m_prefManager.getPreference(name);
		if (val.equals("")) // NOI18N
		{
			val = value;
			setPreference(name, value);
		}
		return val;
	}
	
	////////////////////////////////////////////////////////
	// 
	public SourceTextEntry getSTE(int num)
	{
		if (num >= 0)
			return (SourceTextEntry) m_srcTextEntryArray.get(num);
		else 
			return null;
	}

	public StringEntry getStringEntry(String srcText)
	{
		return (StringEntry) m_strEntryHash.get(srcText);
	}
	
	////////////////////////////////////////////////////////
	// simple project info
	
	public String	sourceRoot()	{ return m_config.getSourceRoot();		}
	public String	projName()		{ return m_config.getProjectName();	}
	public int		numEntries()	{ return m_srcTextEntryArray.size(); }
	public void		setProjWin(ProjectFrame win)	{ m_projWin = win;	}
	public TransFrame getTransFrame()			{ return m_transFrame;	}

	public ArrayList	getTransMemory()		{ return m_tmList;		}

    /////////////////////////////////////////////////////////

	private SaveThread	m_saveThread;
	// count=0		save disabled
	// count=1		one more save only
	// count=2		regular mode
	private int m_saveCount;
	
	public static CommandThread core;
	private ProjectProperties m_config;
    private boolean		m_modifiedFlag;

	private PreferenceManager	m_prefManager;

	// thread control flags
	private boolean		m_stop;
	private LinkedList	m_requestQueue;

	private int		m_totalWords;
	private int		m_partialWords;
	private int		m_currentWords;

	// project name of strings loaded from TM - store globally so to not
	// pass seperately on each function call

	// near proj is the project a near (fuzzy matched) string is from
	private String		m_nearProj;

	// keep track of file specific data to feed to org.omegat.SourceTextEntry objects
	//	so they can have a bigger picture of what's where
    private ProjectFileData	m_curFile;

	private TransFrame	m_transFrame;
	private ProjectFrame	m_projWin;

    private HashMap		m_strEntryHash;	// maps text to strEntry obj
	private ArrayList	m_strEntryList;
	private ArrayList	m_srcTextEntryArray;
	private HashMap		m_glosEntryHash;
	private ArrayList	m_glosEntryList;

	private ArrayList	m_tmList;
	private ArrayList	m_orphanedList;

	private ArrayList	m_extensionList;
    private ArrayList	m_extensionMapList;
}
