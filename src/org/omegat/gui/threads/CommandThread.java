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

package org.omegat.gui.threads;

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
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;
import java.util.zip.Adler32;

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
		m_indexHash = new HashMap(8192);
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
			m_prefManager = new PreferenceManager(OConsts.PROJ_PREFERENCE);
	}

	public void run()
	{
		RequestPacket pack = new RequestPacket();
		m_saveThread = new SaveThread();
		try 
		{
			while (m_stop == false)
			{
				try { sleep(40); }
				catch (InterruptedException e) { ; }
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

	public void messageBoardCheck(RequestPacket pack)
	{
		messageBoard(false, pack);
	}

	protected void messageBoard(boolean post, 
						RequestPacket pack)
	{
		//synchronized (m_messageBoardCritSection)
		{
			if (CommandThread.core == null)
				return;

			if (post == true)
			{
				m_requestQueue.add(pack);
				CommandThread.core.interrupt();
			}
			else
			{
				if (m_requestQueue.size() > 0)
				{
					pack.set((RequestPacket) 
							m_requestQueue.removeFirst());
				}
			}
		}
	}
	
	public void signalStop()
	{
		m_stop = true;
		CommandThread.core.interrupt();
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
		m_indexHash.clear();

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
				m_projWin.hide();
				m_projWin.reset();
				m_projWin.buildDisplay();
				m_projWin.show();
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

	protected void requestLoad(RequestPacket pack)
	{
		TransFrame tf = (TransFrame) pack.obj;
		// load new project
		try
		{
			requestUnload();

			String evtStr;

			evtStr = OStrings.CT_LOADING_PROJECT;
			MessageRelay.uiMessageSetMessageText(tf, evtStr);
			if (loadProject() == false)
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

			evtStr = OStrings.CT_LOADING_INDEX;
			MessageRelay.uiMessageSetMessageText(tf, evtStr);
			buildIndex();
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
		catch (InterruptedIOException e1)
		{
			// user said cancel - this is OK
			;
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
		int ps;
		SourceTextEntry srcTE = null;
		StringEntry se = null;
		for (int i=0; i<m_srcTextEntryArray.size(); i++)
		{
			srcTE = (SourceTextEntry) m_srcTextEntryArray.get(i);
			se = srcTE.getStrEntry();
			str = srcTE.getTranslation();
			if ((str == null) || (str.equals(""))) // NOI18N
			{
				str = "omega - " + se.getSrcText(); // NOI18N
			}
			se.setTranslation(str);
		}
	}

	protected void buildTMXFile(String filename) throws IOException
	{
		int i;
		String s;
		String t;

		// build translation database files
		File tm;
		DataOutputStream dos;
		StringEntry se;

		// we got this far, so assume lang codes are proper
		String srcLang = getPreference(OConsts.PREF_SRCLANG);
		String locLang = getPreference(OConsts.PREF_LOCLANG);

		FileOutputStream fos = new FileOutputStream(filename);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8"); // NOI18N
		BufferedWriter out = new BufferedWriter(osw);
		
		String str = "<?xml version=\"1.0\"?>\n";			 // NOI18N
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
			s = XMLStreamReader.makeValidXML(se.getSrcText(), null);
			t = XMLStreamReader.makeValidXML(se.getTrans(), null);
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
			s = XMLStreamReader.makeValidXML(transMem.source, null);
			t = XMLStreamReader.makeValidXML(transMem.target, null);
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
					if (s.equals(t) == false)
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
		FileHandler fh = null;
		HandlerMaster hm = new HandlerMaster();
		ArrayList fileList = new ArrayList(256);
		boolean ignore;
		String filename;
		String destFileName;
		int namePos;
		String shortName;

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
			ignore = false;
			destFile = new File(destFileName);
			if (!destFile.exists())
			{
				// target directory doesn't exist - create it
				if (destFile.mkdir() == false)
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
			ignore = false;

			// determine actual file name w/o no path info
			namePos = filename.lastIndexOf(File.separator)+1;
			shortName = filename.substring(namePos);

			int extPos = filename.lastIndexOf('.') + 1;
			String ext = filename.substring(extPos);
			// look for mapping of this extension
			for (j=0; j<m_extensionList.size(); j++)
			{
				if (ext.equals(m_extensionList.get(j)) == true)
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
				System.out.println(OStrings.CT_NO_FILE_HANDLER + 
						" (." + ext + ")");								 // NOI18N
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
				// preview file to see if we should use the htmlx parser
				//	since it uses UTF8 encoding
				if (StaticUtils.isXMLFile(filename) == true)
				{
					// look for htmlx parser
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

		if (err == true)
		{
			throw new IOException(OStrings.getString("CT_ERROR_BUILDING_TMX"));
		}

	}

	public void save()
	{
		if (m_modifiedFlag == false)
			return;

		forceSave(false);
	}

	public void markAsDirty()
	{
		m_modifiedFlag = true;
	}

	protected void forceSave(boolean corruptionDanger)
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
				if (corruptionDanger == false)
				{
					s = m_config.getProjectInternal() + OConsts.STATUS_EXTENSION;
					File backup = new File(s + OConsts.BACKUP_EXTENSION);
					File orig = new File(s);
					if (backup.exists())
						backup.renameTo(orig);
				}
			}

			// if successful, delete backup file
			if ((m_modifiedFlag == false) && (corruptionDanger == false))
			{
				s = m_config.getProjectInternal() + OConsts.STATUS_EXTENSION;
				File backup = new File(s + OConsts.BACKUP_EXTENSION);
				if (backup.exists())
					backup.delete();
			}
		}
	}

	public void addEntry(String srcText, String file)
	{
		SourceTextEntry srcTextEntry = null;
		StringEntry strEntry = null;

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
		HandlerMaster hand = new HandlerMaster();
		try 
		{
			if (m_config.createNew() == false)
				return;	// cancel pressed

			// create project root directory
			File proj = new File(m_config.getProjectRoot());
			if (!proj.isDirectory())
			{
				if (proj.mkdirs() == false)
				{
					String msg = OStrings.CT_ERROR_CREATE;
					throw new IOException(msg);
				}
			}
			
			// create internal directory
			File internal = new File(m_config.getProjectInternal());
			if (!internal.isDirectory())
			{
				if (internal.mkdirs() == false)
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
				if (src.mkdirs() == false)
				{
					String msg = OStrings.CT_ERROR_CREATE + " (.../src/)";  // NOI18N
					throw new IOException(msg);
				}
			}

			// create glos dir
			File glos = new File(m_config.getGlossaryRoot());
			if (!glos.isDirectory())
			{
				if (glos.mkdirs() == false)
				{
					String msg = OStrings.CT_ERROR_CREATE + " (.../glos/)";	 // NOI18N
					throw new IOException(msg);
				}
			}

			// create TM dir
			File tm = new File(m_config.getTMRoot());
			if (!tm.isDirectory())
			{
				if (tm.mkdirs() == false)
				{
					String msg = OStrings.CT_ERROR_CREATE + " (.../tm/)";	 // NOI18N
					throw new IOException(msg);
				}
			}

			// create loc dir
			File loc = new File(m_config.getLocRoot());
			if (!loc.isDirectory())
			{
				if (loc.mkdirs() == false)
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

    public TreeMap findAll(String tokens)
	{
		if (m_indexReady == false)
			return null;
		TreeMap foundList = null;
		// TODO - set locale in toLower call
		String str = tokens.toLowerCase();
		Token tok;
		ArrayList tokenList = new ArrayList();
		StaticUtils.tokenizeText(tokens, tokenList);
		for (int i=0; i<tokenList.size(); i++)
		{
			tok = (Token) tokenList.get(i);
			//if ((tok.hasText == false) || (tok.text.equals("")))
			if (tok.text.equals("")) // NOI18N
				continue;

			if (foundList == null)
				foundList = find(tok.text);
			else
				foundList = refineQuery(tok.text, foundList);
			if (foundList == null)
				break;
		}
		return foundList;
	}

	public TreeMap find(String wrd)
	{
		if (m_indexReady == false)
			return null;
		String local = wrd.toLowerCase();
		TreeMap tree = null;
		IndexEntry index = null;
		index = (IndexEntry) m_indexHash.get(local);
		if (index != null)
			tree = index.getTreeMap();
		return tree;
	}

	public TreeMap refineQuery(String wrd, TreeMap foundList)
	{
		if (m_indexReady == false)
			return null;
		if (foundList == null)
			return null;
		TreeMap tree = find(wrd);
		if (tree == null)
			return null;
		TreeMap queryTree = null;
		StringEntry strEntry;
		String s;
		Object obj;
		// merge trees
		while ((tree.size() > 0) && (foundList.size() > 0))
		{
			obj = tree.firstKey();
			strEntry = (StringEntry) foundList.remove(obj);
			if (strEntry != null)
			{
				// match
				if (queryTree == null)
					queryTree = new TreeMap();
				s = String.valueOf(strEntry.digest());
				queryTree.put(s, strEntry);
			}
			tree.remove(obj);
		}

		return queryTree;
	}


	/////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	// protected functions

	protected void loadTranslations()
	{
		String srcText;
		String trans;
		String s;
		StringEntry strEntry;
		SourceTextEntry srcTextEntry;
		Object obj;
		File proj;
		// load translation file (project_name.bin)
		try 
		{
			proj = new File(m_config.getProjectInternal() + OConsts.STATUS_EXTENSION);
			if (proj.exists() == false)
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

	protected boolean loadProject() 
			throws IOException, InterruptedIOException
	{
		int i;
		int j;
        if (m_config.loadExisting() == false)
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
		FileHandler fh = null;
		HandlerMaster hm = new HandlerMaster();
		ArrayList srcFileList = new ArrayList(256);
		File root = new File(m_config.getSourceRoot());
		StaticUtils.buildFileList(srcFileList, root, true);
		boolean ignore;
		int namePos;
		String shortName;
		String filename;
		// keep track of how many entries are in each file
		int numEntries = 0;
		for (i=0; i<srcFileList.size(); i++)
		{
			filename = (String) srcFileList.get(i);
			ignore = false;

			// determine actual file name w/o no path info
			namePos = filename.lastIndexOf(File.separator)+1;
			shortName = filename.substring(namePos);

			int extPos = filename.lastIndexOf('.');
			String ext = filename.substring(extPos+1);
			// look for mapping of this extension
			for (j=0; j<m_extensionList.size(); j++)
			{
				if (ext.equals(m_extensionList.get(j)) == true)
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
				System.out.println(OStrings.CT_NO_FILE_HANDLER 
								+ " (." + ext + ")");  // NOI18N
				continue;
			}
			if (fh.getType().equals(OConsts.FH_HTML_TYPE))
			{
				// preview file to see if we should use the htmlx parser
				if (StaticUtils.isXMLFile(filename) == true)
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
			System.out.println(OStrings.getString("CT_LOADING_FILE")+
						"'" + filepath + "'");  // NOI18N
			m_transFrame.setMessageText(OStrings.CT_LOAD_FILE_MX + filepath);
			m_curFile = new ProjectFileData();
			m_curFile.name = filename;
			m_curFile.firstEntry = m_srcTextEntryArray.size();
			fh.load(filename);
			m_curFile.lastEntry = m_srcTextEntryArray.size()-1;
		}
		m_curFile = null;
		m_projWin.setNumEntries(numEntries());
		loadTranslations();
		m_projWin.buildDisplay();
		m_projWin.show();
		m_projWin.toFront();
		return true;
	}

	protected void buildIndex()
	{
		StringEntry strEntry;
		int wordCount;
		String s;
		Token tok;
		int i, j;
		ArrayList tokenList = new ArrayList();

		IndexEntry indexEntry;
		for (i=0; i<m_strEntryList.size(); i++)
		{
			strEntry = (StringEntry) m_strEntryList.get(i);
			s = strEntry.getSrcText();
			wordCount = StaticUtils.tokenizeText(s, tokenList);
			for (j=0; j<tokenList.size(); j++)
			{
				tok = (Token) tokenList.get(j);

				if (tok.text.equals("")) // NOI18N
					continue;
				s = tok.text.toLowerCase(); // TODO set locale
				indexEntry = (IndexEntry) m_indexHash.get(s);
				if (indexEntry == null)
				{
					// didn't exist - make a new one
					indexEntry = new IndexEntry(s);
					m_indexHash.put(s, indexEntry);
				}
				indexEntry.addReference(strEntry);
			}
			strEntry.setWordCount(wordCount);
		}
	}

	protected void buildGlossary()
	{
		int i;
		GlossaryEntry glosEntry;
		StringEntry strEntry;
		String glosStr;
		String glosStrLow;
		String s;
		int pos;
		TreeMap foundList;
		for (i=0; i<m_glosEntryList.size(); i++)
		{
			glosEntry = (GlossaryEntry) m_glosEntryList.get(i);
			glosStr = glosEntry.getSrcText();
			foundList = findAll(glosStr);
			// TODO - set locale in toLower call
			glosStrLow = glosStr.toLowerCase();
			// TODO - strip formating info

			if (foundList == null)
				continue;
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
		}
	}

	protected void buildNearList(ArrayList seList, String status) 
				throws InterruptedIOException
	{
		String evtStr = status;
		// array lists maintain ordered lists of stringdata objs
		ArrayList pairList = new ArrayList(32);
		ArrayList candPairList = new ArrayList(32);
		ArrayList wordList = new ArrayList(32);
		ArrayList candList = new ArrayList(32);

		StringFreqData masterWordFreq = new StringFreqData(64);
		StringFreqData masterPairFreq = new StringFreqData(64);
		StringFreqData wordFreq = new StringFreqData(64);
		StringFreqData pairFreq = new StringFreqData(64);

		FreqList freqList = new FreqList(256);

		StringEntry 	strEntry;
		StringEntry 	cand;
		String		tok;
		String		curWord = null;
		String		lastWord = null;
		String		wordPair;
		int 		i, j;
		byte[]		candAttr;
		byte[]		strAttr;
		double		ratio;
		double		wordRatio;
		double		pairRatio;
		Integer		len = new Integer(seList.size());

		for (i=0; i<seList.size(); i++)
		{
			if (i%10 == 0)
			{
				Object[] obj = { new Integer(i), len};
				MessageRelay.uiMessageSetMessageText(m_transFrame,
					MessageFormat.format(evtStr, obj));
				try
				{
					sleep(10);
				}
				catch (InterruptedException ie) { ; }
			}
			if (i == 1)
			{
				// redisplay entry to force UI update, but DON'T 
				//  update the translation field
				MessageRelay.uiMessageFuzzyInfo(m_transFrame);
			}
			
			if (m_stop == true)
			{
				throw new InterruptedIOException(""); // NOI18N
			}

			pairList.clear();
			wordList.clear();
			masterWordFreq.reset();
			masterPairFreq.reset();
			freqList.reset();

			// tokenize string - make token lists out of 
			//    org.omegat.StringData objs
			// get candidate near strings
			strEntry = (StringEntry) seList.get(i);
//System.out.println("\nsrc text: "+strEntry.getSrcText());
			buildFreqTable(freqList, strEntry,
					masterWordFreq, wordList,
					masterPairFreq, pairList);
			// word list is a list the list of tokens in the segment
			// pair list is a list of word pairs in the segment - the 
			//	first word is prepended with a space and the last has
			//	a space appended to it (to differentiate first and last
			//	words in the list/freq map)
			// tokens take the form of org.omegat.StringData objects
			// freqList is the list of all StringEntrys that contain
			//	one or more words in common with the current string
			// it also includes information on how many words are
			//	shared between the strings (words were searched for
			//	one by one and a counter was incremented each time
			//	a matching string was found and added to the list)
	
//System.out.println("freq list len="+freqList.len());
			// for each candidate string, compare composition to 
			//	current segment
			for (j=0; j<freqList.len(); j++)
			{
				cand = (StringEntry) freqList.getObj(j);
				ratio = 1.0 * freqList.getCountN(j);
				if (cand.getWordCount() > strEntry.getWordCount())
					ratio /= strEntry.getWordCount();
				else
					ratio /= cand.getWordCount();
//System.out.println("- "+ratio+", "+freqList.getCountN(j)+", "+cand.getWordCount()+", "+strEntry.getWordCount()+"  '"+cand.getSrcText()+"'");

				if (ratio < OConsts.NEAR_THRESH)
					continue;
//System.out.println("comparing to '"+cand.getSrcText()+"'");

				candList.clear();
				candPairList.clear();
				wordFreq = (StringFreqData) 
							masterWordFreq.clone();
				pairFreq = (StringFreqData)
							masterPairFreq.clone();
	
				// tokenize cand string; update freq tables
				buildFreqTable(null, cand,
						wordFreq, candList,
						pairFreq, candPairList);
	
				// analyze word matches
				wordRatio = wordFreq.getMatchRatio();
//System.out.println("- word ratio: "+ratio);
				if (wordRatio < OConsts.NEAR_THRESH)
					continue;
		
				// analyze pair matches
				pairRatio = pairFreq.getMatchRatio();
//System.out.println("- pair ratio: "+pairRatio);
				if (pairRatio < OConsts.PAIR_THRESH)
					continue;

				ratio = Math.sqrt(wordRatio * pairRatio);
//System.out.println("near analysis: '"+strEntry.getSrcText()+"' vs. '"+cand.getSrcText()+"' "+((int) (ratio*100.0))+"%"+"   "+((int)(wordRatio*100.0))+"+"+((int)(pairRatio*100.0)));
		
				// build attr list (cand)
				candAttr = buildAttrList(candList, //candPairList,
							wordFreq,
							pairFreq);
		
				// build attr list (src)
				strAttr = buildAttrList(wordList, //pairList,
							wordFreq,
							pairFreq);
	
				if (seList == m_strEntryList)
				{
					// near strings need extra processing
					// if they're part of the project
					strEntry.addNearString(cand, ratio, strAttr, 
							candAttr, null);
//					registerNear(strEntry, cand, ratio, 
//							strAttr, candAttr);
				}
				else
				{
					cand.addNearString(strEntry, ratio,
						candAttr, strAttr, m_nearProj);
				}
			}
		}
	}
		
	protected void loadTM() throws IOException, FileNotFoundException, 
					InterruptedIOException
	{
		// build strEntryList for each file
		// send to buildNearList
		String [] fileList;
		String lang;
		File f;
		int i;
		String fname;
		ArrayList strEntryList = new ArrayList(m_strEntryList.size());
		DataInputStream dis;

		// foreach lang
		// foreach file
		// build string entry list
		// call build near list (entry list, status+filename)
		//buildNearList(m_strEntryList, status + " (" + fname + ")");

		String ext;
		String src;
		String trans;
		StringEntry se;
		String base = m_config.getProjectRoot() + File.separator;
		strEntryList.clear();
		f = new File(m_config.getTMRoot());
		fileList = f.list();
		for (i=0; i<fileList.length; i++)
		{
			strEntryList.clear();
			fname = fileList[i];
			ext = fname.substring(fname.lastIndexOf('.'));
			fname = m_config.getTMRoot();
			if (fname.endsWith(File.separator) == false)
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

	protected void loadTMXFile(String fname, String encoding, boolean isProject)
		throws IOException, FileNotFoundException, InterruptedIOException
	{
		String status = OStrings.CT_TM_X_OF_Y;
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
			if (strEntryList.size() > 0)
			{
				buildNearList(strEntryList, status + " (" + fname + ")");  // NOI18N
			}
		}
		catch (ParseException e)
		{
			throw new IOException(OStrings.getString("CT_ERROR_PARSEERROR")+ 
					"'" + fname + "'\n" +  e); // NOI18N
		}
	}

    protected void displayError(String msg, Throwable e)
	{
		if (m_transFrame == null)
		{
			System.out.println(OStrings.LD_ERROR + " " + msg); // NOI18N
		}
		else
			MessageRelay.uiMessageDisplayError(m_transFrame, msg, e);
	}

	protected void buildWordCounts()
	{
		ListIterator it;
		StringEntry se;
		LinkedList pl;
		m_totalWords = 0;
		m_partialWords = 0;
		int words = 0;
		it = m_strEntryList.listIterator();
		while(it.hasNext())
		{
			se = (StringEntry) it.next();
			pl = se.getParentList();
			words = se.getWordCount();
			m_partialWords += words;
			m_totalWords += (words * pl.size());
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
			words = 0;
			int totWords = 0;
			ListIterator it2;
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
			catch (IOException e2) { ; }
		}
	}

	protected void buildFreqTable(FreqList freqList, StringEntry strEntry,
			StringFreqData wordFreq, ArrayList wordList,
			StringFreqData pairFreq, ArrayList pairList)
	{
		String		tok = null;
		String		curWord = null;
		String		lastWord = null;
		String		wordPair = null;
		Adler32		ad = new Adler32();
		StringData	curSD = null;
		StringData	lastSD = null;
		StringData	pairSD = null;
		TreeMap		foundList = null;
		StringEntry	se = null;
		Object		obj = null;
		//org.omegat.Token		token = null;

		// consider the last token to be a space so to differentiate
		//	words coming at the start and end of a sequence in pair matches
		String lastTok = " "; // NOI18N
		long dig;

		ArrayList tokenList = new ArrayList();
		StaticUtils.tokenizeText(strEntry.getSrcText(), tokenList);
		for (int i=0; i<tokenList.size(); i++)
		{
			//token = (org.omegat.Token) tokenList.get(i);
			//if (token.hasText == false)
			//	continue;
			//tok = token.text;
			tok = ((Token) tokenList.get(i)).text;
			ad.reset();

			// build word and word pair lists
			lastWord = curWord;
			curWord = tok.toLowerCase();
			wordPair = lastWord + curWord;

			// make a record of each word
			ad.update(curWord.getBytes());
			curSD = new StringData(ad.getValue(), tok);
			wordList.add(curSD);
			// keep a count as well
			if (freqList == null)
				wordFreq.sub(curSD.getDigest(), tok);
			else
				wordFreq.add(curSD.getDigest(), tok);

			// make a record of each word pair
			// make this compare case sensitive
			dig = curSD.getDigest();
			if (lastSD != null)
				dig += (lastSD.getDigest() << 32);
			pairSD = new StringData(dig, lastTok + tok);
			pairList.add(pairSD);
			// ... and a count
			if (freqList == null)
				pairFreq.sub(pairSD.getDigest(), wordPair);
			else
				pairFreq.add(pairSD.getDigest(), wordPair);

			// associate each word w/ both pairs it's in
			curSD.setLow(pairSD.getDigest());
			if (lastSD != null)
				lastSD.setHigh(pairSD.getDigest());

			if (freqList != null)
			{
				// find all strings remotely similar to current 
				//    strEntry
				// for each word, find all strings where it 
				//    occurs and keep track of how many times 
				//    each of these strings is accessed
				foundList = find(curWord);
				if (foundList != null)
				{
					while(foundList.size() > 0)
					{
						obj = foundList.firstKey();
						se = (StringEntry) 
							foundList.remove(obj);
						if (se != strEntry)
							freqList.add(se);
					}
				}
			}
			lastTok = tok;
			lastSD = curSD;
		}
		if (curWord == null)
			return;
		// add last word pair to list
		ad.reset();
//		// append a space to the last word to differentiate it from
//		//	a word coming at the beginning of the segment
//		curWord += " ";
		ad.update(curWord.getBytes());
		pairSD = new StringData(ad.getValue(), tok);
		curSD.setHigh(pairSD.getDigest());
		pairList.add(pairSD);
		if (freqList == null)
			pairFreq.sub(curSD.getDigest(), tok);
		else
			pairFreq.add(curSD.getDigest(), tok);
	}

	// analyzes "nearness" of two strings
	// nearness value is based upon whether string is unique versus 
	//	compared-to string and whether or not it has the same neighbors
	// returned array contains nearness value for each token
	// the UNIQ flag is set if a word occurs in the current segment and 
	//	not in the compared-to segment
	// PAIR is set if a token doesn't have words with the same neighbors
	//	in the compared-to segment
	protected byte[] buildAttrList(ArrayList tokList, 
					StringFreqData wordFreq,
					StringFreqData pairFreq)
	{
		byte[] attr = new byte[tokList.size()];
		// information on token and its neighbors
		StringData tokData;
		StringData freqData;
		StringData low;
		StringData high;
		// first look for token uniqueness
		for (int i=0; i<tokList.size(); i++)
		{
			attr[i] = 0;
			tokData = (StringData) tokList.get(i);
			freqData = wordFreq.getObj(tokData.getDigest());
			if (freqData.isUnique())
			{
				attr[i] |= StringData.UNIQ;
			}
		}

		// now check for pair uniqueness
		// a word fails this check if either of it's neighbors are different
		// this can be detected if a word concatted w/ either neighbor
		//	is not in the pair list
		for (int i=0; i<tokList.size()-1; i++)
		{
			tokData = (StringData) tokList.get(i);

			// see if the neighbors are the same
			low = pairFreq.getObj(tokData.getLow());
			high = pairFreq.getObj(tokData.getHigh());
			if (low.isUnique() || high.isUnique())
			{
				attr[i] |= StringData.PAIR;
			}
		}
		return attr;
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

	private SaveThread	m_saveThread = null;
	// count=0		save disabled
	// count=1		one more save only
	// count=2		regular mode
	private int m_saveCount;
	
	public static CommandThread core = null;
	private ProjectProperties m_config;
    private boolean		m_modifiedFlag = false;

	protected PreferenceManager	m_prefManager = null;

	// thread control flags
	private boolean		m_stop = false;
	private boolean		m_indexReady = true;
	private LinkedList	m_requestQueue;

	private int		m_totalWords = 0;
	private int		m_partialWords = 0;
	private int		m_currentWords;

	// project name of strings loaded from TM - store globally so to not
	// pass seperately on each function call

	// near proj is the project a near (fuzzy matched) string is from
	private String		m_nearProj = null;

	// keep track of file specific data to feed to org.omegat.SourceTextEntry objects
	//	so they can have a bigger picture of what's where
	protected ProjectFileData	m_curFile = null;

	private TransFrame	m_transFrame = null;
	private ProjectFrame	m_projWin = null;

    private HashMap		m_strEntryHash;	// maps text to strEntry obj
	private ArrayList	m_strEntryList;
	private ArrayList	m_srcTextEntryArray;
	private HashMap		m_glosEntryHash;
	private ArrayList	m_glosEntryList;
	private HashMap		m_indexHash;

	protected ArrayList	m_tmList;
	protected ArrayList	m_orphanedList;

	private ArrayList	m_extensionList;;
	private ArrayList	m_extensionMapList;;
}
