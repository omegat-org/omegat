//-------------------------------------------------------------------------
//  
//  CommandThread.java - 
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
//  Build date:  21Dec2002
//  Copyright (C) 2002, Keith Godfrey
//  aurora@coastside.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------

import java.util.*;
import java.util.zip.*;
import java.io.*;
import java.awt.*;
import java.text.*;
import java.awt.event.*;
import java.lang.Math.*;
import java.lang.reflect.*;
import javax.swing.*;
import java.util.*;

//TODO make exception strings multilingual
class CommandThread extends Thread
{
	public CommandThread(TransFrame tf)
	{
		setName("Command thread");
		m_transFrame = tf;
		m_config = new ProjectProperties();
		m_strEntryHash = new HashMap(4096);
		m_strEntryList = new ArrayList();
		m_glosEntryHash = new HashMap(2048);
		m_glosEntryList = new ArrayList();
		m_srcTextEntryArray = new ArrayList(4096);
		m_indexHash = new HashMap(8192);
		m_modifiedFlag = false;

		m_extensionList = new ArrayList(32);
		m_extensionMapList = new ArrayList(32);

		m_requestQueue = new LinkedList();
		m_projWin = null;
		m_saveCount = -1;
		m_saveThread = null;
	}

	public void run()
	{
		RequestPacket pack = new RequestPacket();
//		m_saveThread = new SaveThread();
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
//			m_saveThread.signalStop();
//			m_saveThread.interrupt();
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

	protected synchronized void messageBoard(boolean post, 
						RequestPacket pack)
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
	
	public void signalStop()
	{
		m_stop = true;
		CommandThread.core.interrupt();
	}

	protected void requestUnload()
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
		
		m_extensionList.clear();
		m_extensionMapList.clear();

		m_strEntryList.clear();
		m_glosEntryList.clear();
		m_srcTextEntryArray.clear();

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
			uiMessageSetMessageText(tf, evtStr);
			if (loadProject() == false)
			{
				// loading of project cancelled
				evtStr = OStrings.CT_CANCEL_LOAD;
				uiMessageSetMessageText(tf, evtStr);
				return;
			}
			if (numEntries() <= 0)
				throw new IOException("empty project");
			tf.finishLoadProject();
			uiMessageDisplayEntry(tf, true);
			if (m_saveCount == -1)
//				m_saveThread.start();

			evtStr = OStrings.CT_LOADING_INDEX;
			uiMessageSetMessageText(tf, evtStr);
			buildIndex();
			evtStr = OStrings.CT_LOADING_GLOSSARY;
			uiMessageSetMessageText(tf, evtStr);
			buildGlossary();
			// evaluate strings for fuzzy matching 
			String status = OStrings.CT_FUZZY_X_OF_Y;
			buildNearList(m_strEntryList, status);

			// load in translation database files
			loadTM();
			evtStr = OStrings.CT_LOADING_WORDCOUNT;
			uiMessageSetMessageText(tf, evtStr);
			buildWordCounts();
			uiMessageDisplayEntry(tf, false);
			uiMessageSetMessageText(tf, "");

			// enable normal saves
			m_saveCount = 2;
		}
		catch (InterruptedIOException e1)
		{
			// user said cancel - this is OK
			;
//			// if nearlog is open, interrupt occured during
//			// file creation - only safe thing to do is close
//			// and destroy it
//			if (m_nearLog != null)
//			{
//				try 
//				{
//					File f = new File(
//						m_config.getProjNameBase() +
//						OConsts.FUZZY_EXTENSION);
//					m_nearLog.close();
//					boolean b = f.delete();
//				}
//				catch (IOException e2) { ; }
//				m_nearLog = null;
//			}
		}
		catch (IOException e)
		{
			String msg = OStrings.TF_LOAD_ERROR;
			displayError(msg, e);
		}
		// TODO - cleanup on error
	}


	///////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////
	// methods to send messages to UI objects

	// command codes
	//		1		doPseudoTrans
	//		2		doNextEntry
	//		3		doPrevEntry
	//		4		doRecycleTrans
	//		5		displayEntry(bool)
	//		6		doGotoEntry(str)
	//		7		setMessageText(str)
	//		8		displayWarning(str, throwable)
	//		9		displayError(str, throwable)

	static class MessageRelay implements Runnable
	{
		public MessageRelay(TransFrame tf, int cmd)
		{
			m_cmdNum = 0;
			m_tf = tf;

			// check for errors 
			if ((cmd >= 1) || (cmd <= 4))
				m_cmdNum = cmd;
		}

		public MessageRelay(TransFrame tf, int cmd, boolean param)
		{
			m_cmdNum = 0;
			m_tf = tf;

			// check for errors 
			if (cmd == 5)
			{
				m_bParam = param;
				m_cmdNum = cmd;
			}
		}

		public MessageRelay(TransFrame tf, int cmd, String msg)
		{
			m_cmdNum = 0;
			m_tf = tf;

			// check for errors 
			if ((cmd == 6) || (cmd == 7))
			{
				m_cmdNum = cmd;
				m_msg = msg;
			}
		}

		public MessageRelay(TransFrame tf, int cmd, String msg, Throwable e)
		{
			m_cmdNum = 0;
			m_tf = tf;

			// check for errors 
			if ((cmd == 8) || (cmd == 9))
			{
				m_cmdNum = cmd;
				m_msg = msg;
				m_throw = e;
			}
		}

		public void run()
		{
			switch (m_cmdNum)
			{
				case 1:
					m_tf.doPseudoTrans();
					break;
				case 2:
					m_tf.doNextEntry();
					break;
				case 3:
					m_tf.doPrevEntry();
					break;
				case 4:
					m_tf.doRecycleTrans();
					break;
				case 5:
					m_tf.displayEntry(m_bParam);
					break;
				case 6:
					m_tf.doGotoEntry(m_msg);
					break;
				case 7:
					m_tf.setMessageText(m_msg);
					break;
				case 8:
					m_tf.displayWarning(m_msg, m_throw);
					break;
				case 9:
					m_tf.displayError(m_msg, m_throw);
					break;
				default:
					// do nothing
					break;
			};
		}

		String	m_msg = "";
		boolean	m_bParam = false;
		int		m_cmdNum = 0;
		Throwable	m_throw = null;
		TransFrame	m_tf = null;
	}

	public static void uiMessageDoPseudoTrans(TransFrame tf)
	{
		MessageRelay msg = new MessageRelay(tf, 1);
		SwingUtilities.invokeLater(msg);
	}

	public static void uiMessageDoNextEntry(TransFrame tf)
	{
		MessageRelay msg = new MessageRelay(tf, 2);
		SwingUtilities.invokeLater(msg);
	}

	public static void uiMessageDoPrevEntry(TransFrame tf)
	{
		MessageRelay msg = new MessageRelay(tf, 3);
		SwingUtilities.invokeLater(msg);
	}

	public static void uiMessageDoRecycleTrans(TransFrame tf)
	{
		MessageRelay msg = new MessageRelay(tf, 4);
		SwingUtilities.invokeLater(msg);
	}

	public static void uiMessageDisplayEntry(TransFrame tf, boolean update)
	{
		MessageRelay msg = new MessageRelay(tf, 5, update);
		SwingUtilities.invokeLater(msg);
	}

	public static void uiMessageDoGotoEntry(TransFrame tf, String str)
	{
		MessageRelay msg = new MessageRelay(tf, 6, str);
		SwingUtilities.invokeLater(msg);
	}

	public static void uiMessageSetMessageText(TransFrame tf, String str)
	{
		MessageRelay msg = new MessageRelay(tf, 7, str);
		SwingUtilities.invokeLater(msg);
	}

	public static void uiMessageDisplayWarning(TransFrame tf, 
					String str, Throwable e)
	{
		MessageRelay msg = new MessageRelay(tf, 8, str, e);
		SwingUtilities.invokeLater(msg);
	}

	public static void uiMessageDisplayError(TransFrame tf, 
					String str, Throwable e)
	{
		MessageRelay msg = new MessageRelay(tf, 9, str, e);
		SwingUtilities.invokeLater(msg);
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
			if ((str == null) || (str.equals("")))
			{
				str = "omega - " + se.getSrcText();
			}
			se.setTranslation(str);
		}
	}

	// fetch strings above and below current string for translation
	//  to provide a 'context'
	public ArrayList getContext(int num, int low, int high)
	{
		// TODO - lookup string entries by last mod date
		// instead of returning everything automatically
		ArrayList arr = new ArrayList(low + high + 1);
		StringEntry strEntry;
		SourceTextEntry srcTextEntry;
		String s;
		SourceTextEntry baseSTE = (SourceTextEntry) 
				m_srcTextEntryArray.get(num);
		String sFile = baseSTE.getSrcFile();
		for (int i=num-low; i<num+high+1; i++)
		{
			if (i == num)
				continue;
			if ((i < 0) || (i >= numEntries()) || (i == num))
			{
				arr.add(null);
				continue;
			}
			srcTextEntry = (SourceTextEntry)
					m_srcTextEntryArray.get(i);
			if (sFile.compareTo(srcTextEntry.getSrcFile()) != 0)
			{
				arr.add(null);
				continue;
			}

			EntryData data = getEntryTextOnly(i);
			if (data.trans.equals(""))
				s = data.srcText;
			else
				s = data.trans;
			arr.add(s);
		}
		return arr;
	}

	// get full data about segment entry
	public EntryData getEntry(int num)
	{
		ExchangeRequest exchange = new ExchangeRequest();
		exchange.detail		= 1;
		exchange.entryNum	= num;
		exchangeEntryData(exchange);
		return exchange.data;
	}

	// for use by save and context getEntry calls
	public EntryData getEntryTextOnly(int num)
	{
		ExchangeRequest exchange = new ExchangeRequest();
		exchange.detail		= 2;
		exchange.entryNum	= num;
		exchangeEntryData(exchange);
		return exchange.data;
	}

	// doesn't return source language string if translation is null
	public String getTranslationOnly(int num)
	{
		ExchangeRequest exchange = new ExchangeRequest();
		exchange.detail		= 2;
		exchange.entryNum	= num;
		exchangeEntryData(exchange);
		return exchange.data.trans;
	}

	// returns source language string if translation is null/blank
	public String getTranslation(String src)
	{
		ExchangeRequest exchange = new ExchangeRequest();
		exchange.detail		= 3;
		exchange.srcText	= src;
		exchangeEntryData(exchange);
		return exchange.trans;
	}
	
	public void setTranslation(int entryNum, String trans)
	{
		ExchangeRequest exchange = new ExchangeRequest();
		exchange.detail		= 4;
		exchange.entryNum	= entryNum;
		exchange.trans		= trans;
		exchangeEntryData(exchange);
	}
	
	// have single access point for external threads to access 
	//  string and translation data to allow synchronization and
	//  prevent race conditions
	protected synchronized void exchangeEntryData(ExchangeRequest exch)
	{
		switch (exch.detail)
		{
			case 1:
				getEntrySynchronized(exch);
				break;

			case 2:
				getEntryTextOnlySynchronized(exch);
				break;

			case 3:
				getTranslationSynchronized(exch);
				break;

			case 4:
				setTranslationSynchronized(exch);
				break;

			default:
System.out.println("ERROR - untrapped exchange call");
		}
	}
	
	//////////////////////////////////////////////////////////
	// protected data exchange classes
	
	// support class for internal synchronized messaging
	protected class ExchangeRequest
	{
		ExchangeRequest()
		{
			detail = 0;
		}
		
		public int	entryNum;
		public String	trans;
		public String	srcText;

		public EntryData	data;

		public int	detail;
	}

	// called when detail code == 1	(getEntryextOnly and getTranslationOnly)
	protected void getEntrySynchronized(ExchangeRequest exchange)
	{
		EntryData data = new EntryData();
		SourceTextEntry srcTextEntry = (SourceTextEntry)
					m_srcTextEntryArray.get(exchange.entryNum);
		StringEntry strEntry = srcTextEntry.getStrEntry();

		data.srcText = strEntry.getSrcText();
		data.trans = strEntry.getTrans();
		data.file = srcTextEntry.getSrcFile();
		data.partialWords = m_partialWords;
		data.totalWords = m_totalWords;
		data.currentWords = m_currentWords;
	
		// near terms
		LinkedList lst = strEntry.getNearList();
		NearString ns;
		double score = 0;
		if (lst.size() > 0)
		{
			data.addNearTerms(lst);
		}
	
		// glos terms
		lst = strEntry.getGlosList();
		if (lst.size() > 0)
		{
			data.setGlosTerms(lst);
		}
	
		exchange.data = data;
	}
	

	// called when detail code == 2 (getEntry)
	protected void getEntryTextOnlySynchronized(ExchangeRequest exchange)
	{
		EntryData data = new EntryData();
		SourceTextEntry srcTextEntry = (SourceTextEntry)
					m_srcTextEntryArray.get(exchange.entryNum);
		StringEntry strEntry = srcTextEntry.getStrEntry();

		data.srcText = strEntry.getSrcText();
		data.trans = strEntry.getTrans();
		
		exchange.data = data;
	}

	// called when detail code == 3 (getTrnaslation)
	protected void getTranslationSynchronized(ExchangeRequest exchange)
	{
		// fetch the translation for a given string
		exchange.trans = exchange.srcText;

		SourceTextEntry srcTE;
		StringEntry strEntry;
		strEntry = (StringEntry) m_strEntryHash.get(exchange.srcText);

		if (strEntry != null)
		{
			String s;
			s = strEntry.getTrans();
			if (s != null)
				exchange.trans = s;
		}
	}
	
	// called when detail code == 4 (setTranslation)
	public void setTranslationSynchronized(ExchangeRequest exchange)
	{
		m_modifiedFlag = true;
		SourceTextEntry srcTextEntry = (SourceTextEntry)
					m_srcTextEntryArray.get(exchange.entryNum);
		StringEntry strEntry = srcTextEntry.getStrEntry();
		int num = 0;
		if (strEntry.getTrans() == null)
		{
			// no previous translatoin for this string - need to 
			//  update word count
			num = strEntry.getParentList().size();
			m_currentWords -= (num * strEntry.getWordCount());
		}
		strEntry.setTranslation(exchange.trans);
	}

	// build all translated files and create a new TM file
	public void compileProject() 
			throws IOException
	{
		int i;
		int j;
		String srcRoot = m_config.getSrcRoot();
		String locRoot = m_config.getLocRoot();
		String t;
		String s;
		// keep track of errors - try to continue through processing
		boolean err = false;

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

		try 
		{
			// build translation database files
			File tm;
			DataOutputStream dos;
			StringEntry se;
			tm = new File(m_config.getProjFileBase() + OConsts.TM_EXTENSION);
			dos = new DataOutputStream(new FileOutputStream(tm));
			dos.writeUTF(OConsts.TM_FILE_IDENT);
			dos.writeInt(OConsts.TM_CUR_VERSION);
			dos.writeUTF(m_config.getProjName());
	
			for (i=0; i<m_strEntryList.size(); i++)
			{
				se = (StringEntry) m_strEntryList.get(i);
				s = se.getSrcText();
				t = se.getTrans();
				dos.writeUTF(s);
				dos.writeUTF(t);
			}
			dos.writeUTF(OConsts.UTF8_END_OF_LIST);
			dos.close();
		}
		catch (IOException e)
		{
			System.out.println("Exception encountered: " + e);	
			System.out.println("Unable to build TM file");
			err = true;
		}
		
		// build mirror directory of source tree
		buildDirList(fileList, new File(srcRoot));
		File fname;
		for (i=0; i<fileList.size(); i++)
		{
			filename = (String) fileList.get(i);
			destFileName = m_config.getLocRoot() + 
					filename.substring(m_config.getSrcRoot().length()+1);
			ignore = false;
			fname = new File(destFileName);
			if (!fname.exists())
			{
				// target directory doesn't exist - create it
				if (fname.mkdir() == false)
				{
					throw new IOException("Can't create target language " +
						"directory " + destFileName);
				}
			}
		}

		// build translated files
		fileList.clear();
		buildFileList(fileList, new File(srcRoot));
		for (i=0; i<fileList.size(); i++)
		{
			filename = (String) fileList.get(i);
			destFileName = m_config.getLocRoot()+ 
						filename.substring(m_config.getSrcRoot().length()+1);
			ignore = false;

			// determine actual file name w/o no path info
			namePos = filename.lastIndexOf(File.separator)+1;
			shortName = filename.substring(namePos);

			// see if this file is to be ignored
			for (j=0; j<m_ignoreList.size(); j++)
			{
				if (shortName.equals(m_ignoreList.get(j)))
				{
					// ignore this file
					ignore = true;
					// copy file to loc tree
					LFileCopy.copy(filename, destFileName);
					break;
				}
			}
			if (ignore == true)
				continue;

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
				System.out.println(OStrings.CT_NO_FILE_HANDLER 
								+ " (." + ext + ")");
				continue;
			}
			// shorten filename to that which is relative to src root
			String midName = filename.substring(srcRoot.length());
			s = srcRoot + midName;
			t = locRoot + midName;
			fh.write(s, t);
		}

		if (err == true)
		{
			throw new IOException("Can't build TM file");
		}

	}

	public void save()
	{
		if (m_modifiedFlag == false)
			return;

		forceSave(false);
	}

	protected synchronized void forceSave(boolean corruptionDanger)
	{
		if (m_saveCount <= 0)
			return;
		else if (m_saveCount == 1)
			m_saveCount = 0;

		try 
		{
if (corruptionDanger == false) 
	System.out.println("changes detected - saving project");
			EntryData entryData;
			ListIterator it;
			String trans;
			StringEntry strEntry;
			String s = m_config.getInternal() + OConsts.STATUS_EXTENSION;
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

			File outFile = new File(s);
			DataOutputStream dos;
			dos = new DataOutputStream(new FileOutputStream(outFile));
			dos.writeUTF(OConsts.STATUS_FILE_IDENT);
			dos.writeInt(OConsts.STATUS_CUR_VERSION);
			for (int i=0; i<m_srcTextEntryArray.size(); i++)
			{
				entryData = getEntryTextOnly(i);
				dos.writeUTF(entryData.srcText);
				dos.writeUTF(entryData.trans);
			}
			dos.writeUTF(OConsts.UTF8_END_OF_LIST);
			dos.close();
			m_modifiedFlag = false;
		}
		catch(IOException e)
		{
			String msg = OStrings.CT_ERROR_SAVING_PROJ;
			displayError(msg, e);
			// try to rename backup file to original name
			if (corruptionDanger == false)
			{
				String s = m_config.getInternal() + 
								OConsts.STATUS_EXTENSION;
				File backup = new File(s + OConsts.BACKUP_EXTENSION);
				File orig = new File(s);
				if (backup.exists())
					backup.renameTo(orig);
			}
		}

		// if successful, delete backup file
		if ((m_modifiedFlag == false) && (corruptionDanger == false))
		{
			String s = m_config.getInternal() + OConsts.STATUS_EXTENSION;
			File backup = new File(s + OConsts.BACKUP_EXTENSION);
			if (backup.exists())
				backup.delete();
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

		srcTextEntry.set(strEntry, file, m_srcTextEntryArray.size());
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
			File proj = new File(m_config.getProjRoot());
			if (!proj.isDirectory())
			{
				if (proj.mkdirs() == false)
				{
					String msg = OStrings.CT_ERROR_CREATE;
					throw new IOException(msg);
				}
			}
			
			// create internal directory
			File internal = new File(m_config.getInternal());
			if (!internal.isDirectory())
			{
				if (internal.mkdirs() == false)
				{
					String msg = OStrings.CT_ERROR_CREATE;
					throw new IOException(msg);
				}
			}
			
			// populate internal directory with project files
			buildDefaultHandlerFile(hand);
			buildDefaultIgnoreFile();
			
			// create src dir
			File src = new File(m_config.getSrcRoot());
			if (!src.isDirectory())
			{
				if (src.mkdirs() == false)
				{
					String msg = OStrings.CT_ERROR_CREATE + " (.../src/)";
					throw new IOException(msg);
				}
			}

			// create glos dir
			File glos = new File(m_config.getGlosRoot());
			if (!glos.isDirectory())
			{
				if (glos.mkdirs() == false)
				{
					String msg = OStrings.CT_ERROR_CREATE + " (.../glos/)";
					throw new IOException(msg);
				}
			}

			// create TM dir
			File tm = new File(m_config.getTMRoot());
			if (!tm.isDirectory())
			{
				if (tm.mkdirs() == false)
				{
					String msg = OStrings.CT_ERROR_CREATE + " (.../tm/)";
					throw new IOException(msg);
				}
			}

			// create loc dir
			File loc = new File(m_config.getLocRoot());
			if (!loc.isDirectory())
			{
				if (loc.mkdirs() == false)
				{
					String msg = OStrings.CT_ERROR_CREATE + " (.../tm/)";
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
			String msg = "failed to create project";
			displayError(msg, e);
		}
	}

	public TreeMap findAll(String tokenList)
	{
		if (m_indexReady == false)
			return null;
		TreeMap foundList = null;
		// TODO - set locale in toLower call
		String str = tokenList.toLowerCase();
		String s;
		StringTokenizer st = new StringTokenizer(str);
		while (st.hasMoreTokens())
		{
			s = stripString(st.nextToken());
			if (s == null)
				continue;
			if (foundList == null)
				foundList = find(s);
			else
				foundList = refineQuery(s, foundList);
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
			proj = new File(m_config.getInternal() + 
							OConsts.STATUS_EXTENSION);
			if (proj.exists() == false)
			{ 
				System.out.println("Can't find saved translation file '" 
						+ proj + "'");
				// nothing to do here
				return;
			}
		}
		catch(SecurityException se)
		{
			// file probably exists, but something's wrong
			String msg = "Security error encountered loading " +
				"project file";
			displayError(msg, se);
			return;
		}
		try
		{
			DataInputStream dis = new DataInputStream(new 
											FileInputStream(proj));
			String ident = dis.readUTF();
			if (ident.compareTo(OConsts.STATUS_FILE_IDENT) != 0)
			{
				throw new IOException(
						"unrecognized status file");
			}
			int vers = dis.readInt();
			if (vers != OConsts.STATUS_CUR_VERSION)
				throw new IOException("unsupported version");

			// recover existing translations
			// since the source files may have changed since the last time
			//  they were loaded, load each string then look for it's 
			//  owner
			srcText = dis.readUTF();
			int cnt = 0;
			while (srcText.equals(OConsts.UTF8_END_OF_LIST) == false)
			{
				// find stringEntry
				obj = m_strEntryHash.get(srcText);
				strEntry = (StringEntry) obj;
				trans = dis.readUTF();

				if (strEntry != null)
					strEntry.setTranslation(trans);

				srcText = dis.readUTF();
			}
			dis.close();
		}
		catch (IOException e)
		{
			String msg = "problem encountered loading " +
				"project status file";
			displayError(msg, e);
		}
	}

	protected boolean loadProject() 
			throws IOException, InterruptedIOException
	{
		int i;
		int j;
		m_ignoreNearLog = false;	// TODO identify this variable
		if (m_config.loadExisting() == false)
			return false;;

		// first load glossary files
		File dir = new File(m_config.getGlosRoot());
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
				if (fname.endsWith(".tab"))
				{
					System.out.println("Loading glossary file " + fname);
					tab.load(dir.getAbsolutePath() + File.separator + fname);
					for (j=0; j<tab.numRows(); j++)
					{
						src = tab.get(j, 0);
						loc = tab.get(j, 1);
						if (loc.equals(""))
							addGlosEntry(src);
						else
							addGlosEntry(src, loc);
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
			throw new IOException("can't access glossary directory");
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

		// read ignore list first
		m_ignoreList = new ArrayList(32);
		String ignoreName = m_config.getInternal()+ OConsts.IGNORE_LIST;
		File ignoreFile = new File(ignoreName);
		tab.reset();
		if (ignoreFile.exists())
		{
			String str;
			try
			{
				tab.load(ignoreName);
				for (i=0; i<tab.numRows(); i++)
				{
					str = tab.get(i, 0);
					str.trim();
					System.out.println("Setting ignore file: " + str);
					m_ignoreList.add(str);
				}
			}
			catch (IOException e)
			{
				// problem reading ignore file - complain but move on
				System.out.println(OStrings.CT_ERROR_LOADING_IGNORE_FILE);
			}
		}
		else
			System.out.println(OStrings.CT_ERROR_FINDING_IGNORE_FILE);

		// now read handler list
		String handlerName = m_config.getInternal() + OConsts.HANDLER_LIST;
		File handlerFile = new File(handlerName);
		tab.reset();
		if (handlerFile.exists())
		{
			String ext;
			String map;
			try
			{
				tab.load(handlerName);
				for (i=0; i<tab.numRows(); i++)
				{
					ext = tab.get(i, 0);
					map = tab.get(i, 1);
					if (ext.equals("") || (map.equals("")))
						continue;
					ext.trim();
					map.trim();
					// removing leading '.' if it's there
					if (ext.charAt(0) == '.')
						ext = ext.substring(1);
					if (map.charAt(0) == '.')
						map = map.substring(1);
					m_extensionList.add(ext);
					m_extensionMapList.add(map);
					System.out.println("mapping '." + ext + 
											"' to '." + map + "'");
				}
			}
			catch (IOException e)
			{
				// problem reading cwignore file - complain but move on
				System.out.println(OStrings.CT_ERROR_LOADING_HANDLER_FILE);
			}
		}
		else
			System.out.println(OStrings.CT_ERROR_FINDING_HANDLER_FILE);

		// ready to read in files
		// remap extensions where necessary and ignore specified files
		FileHandler fh = null;
		HandlerMaster hm = new HandlerMaster();
		ArrayList srcFileList = new ArrayList(256);
		File root = new File(m_config.getSrcRoot());
		buildFileList(srcFileList, root);
		boolean ignore;
		int namePos;
		String shortName;
		String filename;
		for (i=0; i<srcFileList.size(); i++)
		{
			filename = (String) srcFileList.get(i);
			ignore = false;

			// determine actual file name w/o no path info
			namePos = filename.lastIndexOf(File.separator)+1;
			shortName = filename.substring(namePos);

			// see if this file is to be ignored
			for (j=0; j<m_ignoreList.size(); j++)
			{
				if (shortName.equals(m_ignoreList.get(j)))
				{
					// ignore this file
					ignore = true;
					break;
				}
			}
			if (ignore == true)
				continue;

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
								+ " (." + ext + ")");
				continue;
			}
			// strip leading path information; feed file name to project
			//  window
			m_projWin.addFile(filename.substring(
						m_config.getSrcRoot().length()), numEntries());
			//m_projWin.addFile(filename, numEntries());
			System.out.println("loading file '" + filename + "'");
			fh.load(filename);
		}
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
		StringTokenizer st;
		IndexEntry indexEntry;
		for (int i=0; i<m_strEntryList.size(); i++)
		{
			wordCount = 0;
			strEntry = (StringEntry) m_strEntryList.get(i);
			s = strEntry.getSrcText();
			st = new StringTokenizer(s);
			while (st.hasMoreTokens())
			{
				s = stripString(st.nextToken());
				if (s == null)
					continue;
				s = s.toLowerCase(); // TODO set locale
				indexEntry = (IndexEntry) m_indexHash.get(s);
				if (indexEntry == null)
				{
					// didn't exist - make a new one
					indexEntry = new IndexEntry(s);
					m_indexHash.put(s, indexEntry);
				}
				indexEntry.addReference(strEntry);
				wordCount++;
			}
			strEntry.setWordCount(wordCount);
		}
	}

	protected void buildGlossary()
	{
		int i;
		StringEntry glosEntry;
		StringEntry strEntry;
		String glosStr;
		String glosStrLow;
		String s;
		int pos;
		TreeMap foundList;
		for (i=0; i<m_glosEntryList.size(); i++)
		{
			glosEntry = (StringEntry) m_glosEntryList.get(i);
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
		StringTokenizer st;
		String		tok;
		String		curWord = null;
		String		lastWord = null;
		String		wordPair;
		int 		i, j;
		byte[]		candAttr;
		byte[]		strAttr;
		double		ratio;
		double		pairRatio;
		Integer		len = new Integer(seList.size());

		for (i=0; i<seList.size(); i++)
		{
			if (i%10 == 0)
			{
				Object[] obj = { new Integer(i), len};
				uiMessageSetMessageText(m_transFrame,
					MessageFormat.format(evtStr, obj));
			}
			if (i == 1)
			{
				// redisplay entry to force UI update, but DON'T 
				//  update the translation field
				uiMessageDisplayEntry(m_transFrame, false);
			}
			
			if (m_stop == true)
			{
				throw new InterruptedIOException("");
			}

			pairList.clear();
			wordList.clear();
			masterWordFreq.reset();
			masterPairFreq.reset();
			freqList.reset();

			// tokenize string - make token lists out of 
			//    StringData objs
			// get candidate near strings
			strEntry = (StringEntry) seList.get(i);
			buildFreqTable(freqList, strEntry,
					masterWordFreq, wordList,
					masterPairFreq, pairList);
	
			// for each candidate string
			for (j=0; j<freqList.len(); j++)
			{
				cand = (StringEntry) freqList.getObj(j);
				ratio = 1.0 * freqList.getCountN(j) / 
							cand.getWordCount();
				if (ratio < OConsts.NEAR_THRESH)
					continue;

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
				ratio = wordFreq.getMatchRatio();
				if (ratio < OConsts.NEAR_THRESH)
					continue;
		
				// analyze pair matches
				pairRatio = pairFreq.getMatchRatio();
				if (pairRatio < OConsts.PAIR_THRESH)
					continue;
		
				// build attr list (cand)
				candAttr = buildAttrList(candList, 
							wordFreq,
							pairFreq);
		
				// build attr list (src)
				strAttr = buildAttrList(wordList,
							wordFreq,
							pairFreq);
	
				ratio = Math.sqrt(ratio * pairRatio);
				if (seList == m_strEntryList)
				{
					// near strings need extra processing
					// if they're part of the project
					registerNear(strEntry, cand, ratio, 
							strAttr, candAttr);
				}
				else
				{
					cand.addNearString(strEntry, ratio,
						candAttr, strAttr, m_nearProj);
				}
			}
		}
		if (m_nearLog != null)
		{
			try 
			{
				m_nearLog.close();
				m_nearLog = null;
			}
			catch (IOException e)
			{
				;
			}
		}
	}
		
	protected void loadTM() throws IOException, FileNotFoundException, 
					InterruptedIOException
	{
		// build strEntryList for each file
		// send to buildNearList
		String status = OStrings.CT_TM_X_OF_Y;
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

		String src;
		String trans;
		StringEntry se;
		String base = m_config.getProjRoot() + File.separator;
		strEntryList.clear();
		f = new File(m_config.getTMRoot());
		fileList = f.list();
		for (i=0; i<fileList.length; i++)
		{
			strEntryList.clear();
			fname = fileList[i];
			f = new File(m_config.getTMRoot() + fname);
			dis = new DataInputStream(new 
					FileInputStream(f));
			String ident = dis.readUTF();
			if (ident.compareTo(OConsts.TM_FILE_IDENT) 
								!= 0)
			{
				dis.close();
				continue;
			}
			int vers = dis.readInt();
			if (vers != OConsts.TM_CUR_VERSION)
			{
				throw new IOException(
					"unsupported translation memory file version (" +
					fname + ")");
			}

			System.out.println("Processing TM file '" + fname + "'");

			m_nearProj = dis.readUTF();
			while (true)
			{
				if (dis.available() <= 0)
					break;
				src = dis.readUTF();
				if (src.compareTo(
					OConsts.UTF8_END_OF_LIST) == 0)
				{
					break;
				}
				trans = dis.readUTF();
				se = new StringEntry(src);
				se.setTranslation(trans);
				strEntryList.add(se);
			}
			buildNearList(strEntryList, 
					status + " (" + fname + ")");
			dis.close();
		}
		m_nearProj = null;
	}

	protected void addGlosEntry(String srcText)
	{
		if (m_glosEntryHash.get(srcText) == null)
		{
			StringEntry strEntry = new StringEntry(srcText);
			m_glosEntryHash.put(srcText, strEntry);
			m_glosEntryList.add(strEntry);
		}
	}

	protected void addGlosEntry(String srcText, String locText)
	{
		if (m_glosEntryHash.get(srcText) == null)
		{
			StringEntry strEntry = new StringEntry(srcText);
			strEntry.setTranslation(locText);
			m_glosEntryHash.put(srcText, strEntry);
			m_glosEntryList.add(strEntry);
		}
	}

	// returns a list of all files under the root directory
	//  by absolute path
	protected void buildFileList(ArrayList lst, File rootDir)
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
		for (i=0; i<Array.getLength(flist); i++)
		{
			if (flist[i].isDirectory())
			{
				// now recurse into subdirectories
				buildFileList(lst, flist[i]);
			}
		}
	}

	// returns a list of all files under the root directory
	//  by absolute path
	protected void buildDirList(ArrayList lst, File rootDir)
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

	protected void displayWarning(String msg, Throwable e)
	{
		if (m_transFrame == null)
		{
			System.out.println(OStrings.LD_WARNING + " " + msg);
		}
		else
			uiMessageDisplayWarning(m_transFrame, msg, e);
			
	}

	protected void displayError(String msg, Throwable e)
	{
		if (m_transFrame == null)
		{
			System.out.println(OStrings.LD_ERROR + " " + msg);
		}
		else
			uiMessageDisplayError(m_transFrame, msg, e);
			
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
		String fn = m_config.getInternal() + OConsts.WORD_CNT_FILE_EXT;
		FileWriter ofp = null;
		try 
		{
			ofp = new FileWriter(fn);
			ofp.write("Word count in unique segments: "+ m_partialWords + "\n");
			ofp.write("Total word count: "	+ m_totalWords + "\n");
			it = m_srcTextEntryArray.listIterator();
			SourceTextEntry ste;
			String curFile = "";
			String file;
			words = 0;
			int totWords = 0;
			ListIterator it2;
			while(it.hasNext())
			{
				ste = (SourceTextEntry) it.next();
				file = ste.getSrcFile();
				if (curFile.compareTo(file) != 0)
				{
					if (curFile.length() > 0)
						ofp.write(curFile + "\t" 
							+ totWords +"\n");
					curFile = file;
					totWords = 0;
				}
				words = ste.getStrEntry().getWordCount();
				totWords += words;
				m_currentWords += words;
			}
			if (curFile.length() > 0)
			{
				ofp.write(curFile + "\t" + totWords +"\n\n");
			}
			ofp.write("Words remaining to translate: " + m_currentWords + "\n");
			ofp.close();
		}
		catch (IOException e)
		{
			try { ofp.close();	}
			catch (IOException e2) { ; }
		}
	}

	protected void buildFreqTable(FreqList freqList, StringEntry strEntry,
			StringFreqData wordFreq, ArrayList wordList,
			StringFreqData pairFreq, ArrayList pairList)
	{
		StringTokenizer	st;
		String		tok = null;
		String		s = null;
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

		String lastTok = "";
		long dig;

		st = new StringTokenizer(strEntry.getSrcText());
		while (st.hasMoreTokens())
		{
			tok = st.nextToken();
			ad.reset();

			// get rid of punctuation
			if ((s = stripString(tok)) == null)
				continue;

			// build word and word pair lists
			lastWord = curWord;
			// TODO = set locale in toLower call
			curWord = s.toLowerCase();
			wordPair = lastWord + curWord;

			// make a record each word
			ad.update(curWord.getBytes());
			curSD = new StringData(ad.getValue(), tok);
			wordList.add(curSD);
			if (freqList == null)
				wordFreq.sub(curSD.getDigest(), tok);
			else
				wordFreq.add(curSD.getDigest(), tok);

			// make a record of each word pair
			dig = curSD.getDigest();
			if (lastSD != null)
				dig += (lastSD.getDigest() << 32);
			pairSD = new StringData(dig, lastTok + tok);
			pairList.add(pairSD);
			if (freqList != null)
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
		ad.update(curWord.getBytes());
		pairSD = new StringData(ad.getValue(), tok);
		curSD.setHigh(pairSD.getDigest());
		pairList.add(pairSD);
		if (freqList != null)
			pairFreq.sub(curSD.getDigest(), tok);
		else
			pairFreq.add(curSD.getDigest(), tok);
	}

	protected byte[] buildAttrList(ArrayList tokList, 
					StringFreqData wordFreq,
					StringFreqData pairFreq)
	{
		byte[] attr = new byte[tokList.size()];
		StringData tokData;
		StringData freqData;
		StringData low;
		StringData high;
		int i=0; 
		byte uniqNearMask = (StringData.UNIQ | StringData.NEAR);
		for (i=0; i<tokList.size(); i++)
		{
			attr[i] = 0;
			tokData = (StringData) tokList.get(i);
			freqData = wordFreq.getObj(tokData.getDigest());
			attr[i] |= freqData.getAttr() & uniqNearMask;
			low = pairFreq.getObj(tokData.getLow());
			high = pairFreq.getObj(tokData.getHigh());
			if (freqData.hasAttr(StringData.NEAR))
			{
				// clear near attribute if pair strings are
				// neither uniq or near
				if (((low.getAttr() | high.getAttr()) & 
							uniqNearMask) == 0)
				{
					attr[i] &= ~StringData.NEAR;
				}
			}
			if (low.isUnique() || high.isUnique())
				attr[i] |= StringData.PAIR;
		}
		return attr;
	}

	protected boolean loadNearList()
	{
		boolean res = false;
		int cnt = 0;
		byte[] parData;
		byte[] nearData;
		int len;
		// attempt to load near list from disk
		try 
		{
			StringEntry strEntry;
			StringEntry cand;
			String src;
			String trans;
			double ratio;
			String s = m_config.getProjFileBase() + OConsts.FUZZY_EXTENSION;
			FileInputStream fis = new FileInputStream(s);
			DataInputStream dis = new DataInputStream(fis);
//			DataInputStream dis = new DataInputStream(new
//				GZIPInputStream(fis));
			
			String ident = dis.readUTF();
			if (ident.compareTo(OConsts.FUZZY_FILE_IDENT) != 0)
			{
				throw new IOException(
						"unrecognized fuzzy file");
			}
			int vers = dis.readInt();
			if (vers != 1)
				throw new IOException("unsupported version");
			while (dis.available() > 0)
			{
				src = dis.readUTF();
				trans = dis.readUTF();
				ratio = dis.readDouble();
				len = dis.readInt();
				parData = new byte[len];
				dis.read(parData);
				len = dis.readInt();
				nearData = new byte[len];
				dis.read(nearData);
			
				strEntry = (StringEntry) 
						m_strEntryHash.get(src);
				cand = (StringEntry) m_strEntryHash.get(trans);
				if ((strEntry == null) || (cand == null))
					throw new IOException();
				m_ignoreNearLog = true;
				registerNear(strEntry, cand, ratio,
							parData, nearData);
				cnt++;
			}


			res = true;
		}
		catch(IOException e)
		{
			// above code will throw exception when datastream is
			// exhausted 
			// signal OK if any near strings were loaded 
			if (cnt > 0)
				res = true;
		}
		
		return res;
	}

	protected void registerNear(StringEntry strEntry, 
					StringEntry cand, 
					double ratio, 
					ArrayList parDataArray, 
					ArrayList nearDataArray)
	{
		int i;
		byte[] pData = new byte[parDataArray.size()];
		for (i=0; i<pData.length; i++)
			pData[i] = ((StringData) parDataArray.get(i)).getAttr();
		byte[] nData = new byte[nearDataArray.size()];
		for (i=0; i<nData.length; i++)
			nData[i] = ((StringData)nearDataArray.get(i)).getAttr();
		registerNear(strEntry, cand, ratio, pData, nData);
	}

	protected void registerNear(StringEntry strEntry, 
					StringEntry cand, 
					double ratio, 
					byte[] parData, 
					byte[] nearData)
	{
		strEntry.addNearString(cand, ratio, parData, nearData, null);
//		String s = m_config.getProjRoot() + File.separator + 
//			m_config.getProjName() + OConsts.FUZZY_EXTENSION;
//		File f = new File(s);
//		if ((m_nearLog == null) && (m_ignoreNearLog == false))
//		{
//			// open it
//			try 
//			{
//				m_nearLog = new DataOutputStream(new 
//					FileOutputStream(s));
//				m_nearLog = new DataOutputStream(new 
//					GZIPOutputStream(new
//					FileOutputStream(s)));
//				m_nearLog.writeUTF(OConsts.FUZZY_FILE_IDENT);
//				m_nearLog.writeInt(OConsts.FUZZY_CUR_VERSION);
//			}
//			catch(IOException e)
//			{
//				try 
//				{
//					if (m_nearLog != null)
//						m_nearLog.close();
//					f.delete();
//				}
//				catch (IOException e2) { ; }
//				m_nearLog = null;
//				m_ignoreNearLog = true;
//				// forget about it for now - user will just 
//				// have to regenerate near strings each time
//				// around
//				String msg = m_langManager.getString(
//					OStrings.CT_ERROR_WRITING_NEARLOG);
//				displayWarning(msg, e);
//			}
//		}
//
//		// do this check in the event that the log file wasn't opened
//		if (m_nearLog != null)
//		{
//			try
//			{
//				m_nearLog.writeUTF(strEntry.getSrcText());
//				m_nearLog.writeUTF(cand.getSrcText());
//				m_nearLog.writeDouble(ratio);
//				m_nearLog.writeInt(parData.length);
//				m_nearLog.write(parData, 0, parData.length);
//				m_nearLog.writeInt(nearData.length);
//				m_nearLog.write(nearData, 0, nearData.length);
//			}
//			catch (IOException e)
//			{
//				// unexpected error 
//				String msg = m_langManager.getString(
//					OStrings.CT_ERROR_WRITING_NEARLOG);
//				displayError(msg, e);
//				try 
//				{
//					if (m_nearLog != null)
//						m_nearLog.close();
//					f.delete();
//				}
//				catch (IOException e2) { ; }
//				m_nearLog = null;
//				m_ignoreNearLog = true;
//			}
//		}
//		strEntry.addNearString(cand, ratio, parData, nearData, null);
	}

	protected void buildDefaultHandlerFile(HandlerMaster hand) 
				throws IOException
	{
		String name = m_config.getInternal() + OConsts.HANDLER_LIST;
		File handler = new File(name);
		if (handler.exists())
		{
			// don't overwrite existing file
		}

		LTabFileReader tab = new LTabFileReader();
		int i;
		String ext = "";
		ArrayList handlers = hand.getHandlerList();
		for (i=0; i<handlers.size(); i++)
		{
			ext += ((FileHandler) handlers.get(i)).preferredExtension();
			ext += " ";
		}
		tab.addLine("# Filename Extension Mapping File");
		tab.addLine("#");
		tab.addLine("# This version of OmegaT recognizes the following " +
					"file types: ");
		tab.addLine("#     " + ext);
		tab.addLine("# If there are files in the project that conform to " +
					"one of these");
		tab.addLine("# formats but have a different extension, you can " +
					"map the existing");
		tab.addLine("# file to one of these types below.");
		tab.addLine("#");
		tab.addLine("# To create a mapping, type in the file extension " +
					"used in your project");
		tab.addLine("# and then the extension of the file parser you wish " +
					"to be used for this");
		tab.addLine("# file, seperated by a tab character.  For example, " +
					"to map the file ");
		tab.addLine("# extension .swx to .html enter");
		tab.addLine("# swx <tab> html");
		tab.addLine("# without the '#' character or spaces, and where <tab> " +
					"represents the");
		tab.addLine("# tab key on the keyboard.  Do not use space, periods " +
					" or any other");
		tab.addLine("# punctuation when specifying the file extensions");
		tab.addLine("#");
		tab.addLine("# The following defines the default mapping from " +
					".htm to .html");
		tab.addLine("htm\thtml");

		tab.write(name);
	}

	protected void buildDefaultIgnoreFile() throws IOException
	{
		String name = m_config.getInternal() + OConsts.IGNORE_LIST;
		File ignore = new File(name);
		if (ignore.exists())
		{
			// don't overwrite existing file
			return;
		}

		LTabFileReader tab = new LTabFileReader();
		tab.addLine("# Ignore File List");
		tab.addLine("# ");
		tab.addLine("# You can cause certain files in the source language " +
					"directory to");
		tab.addLine("# be omitted from processing, and hence translation, " +
					"by listing them");
		tab.addLine("# in this file");
		tab.addLine("# ");
		tab.addLine("# To cause a file to be skipped, type it in below, " +
					"one filename per ");
		tab.addLine("# line, and do not use any spaces or extra characters " +
					"(like '#' for example");
		tab.addLine("# The single default entry below will cause the file " +
					"'DO_NOT_TRANSLATE.txt'");
		tab.addLine("# to be ignored.");
		tab.addLine("# ");
		tab.addLine("DO_NOT_TRANSLATE.txt");
		tab.addLine("");

		tab.write(name);
	}

	/////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	// static functions

	// return 0 for normal char, 1 for imbeddable, 2 for deletable
	protected static int charType(char c)
	{
		int type = 0;
		if ((c == '.') || (c == '-') || (c == '@'))
		{
			// allow URL, email and hypenated words
			type = 1;
		}
		else
		{
			int t = Character.getType(c);
			switch (t) {
				case Character.DASH_PUNCTUATION:
				case Character.START_PUNCTUATION:
				case Character.END_PUNCTUATION:
				case Character.CONNECTOR_PUNCTUATION:
				case Character.OTHER_PUNCTUATION:
				case Character.MATH_SYMBOL:
				case Character.CURRENCY_SYMBOL:
				case Character.MODIFIER_SYMBOL:
					type = 2;
				default:
					type = 0;
			};
		}
		return type;
	}

	public static String stripString(String token)
	{
		// remove punctuation chars from token
		char[] str = new char[token.length()+1];
		char[] tmp = new char[token.length()+1];
		String s;
		char c;
		int cnt = 0;
		int tmpCnt = 0;
		boolean textFound = false;
		int type;
		boolean ignore;
		int j;
		token.getChars(0, token.length(), str, 0);
		for (int i=0; i<token.length(); i++)
		{
			ignore = false;
			c = str[i];
			type = charType(c);
			if (textFound == false)
			{
				if (type == 0)
					textFound = true;	
				else if (type == 1)
					ignore = true;
			}
			if (type == 2)
			{
				ignore = true;
			}
			if (ignore == false)
			{
				if (type == 0)
				{
					// copy tmp buffer back
					for (j=0; j<tmpCnt; j++)
					{
						str[cnt++] = tmp[j];
					}
					tmpCnt = 0;
					str[cnt++] = c;
				}
				else if (type == 1)
				{
					// write to tmp buf
					tmp[tmpCnt++] = c;
				}
			}
		}
		if (textFound == false)
			return null;
		else
		{
			s = new String(str, 0, cnt);
			return s;
		}
	}

	// for FileHandlers in test mode
	public static void dumpEntry(String val, String file)
	{
		System.out.println(" val: " + val);
		System.out.println("file: " + file);
		System.out.println("");
	}


	////////////////////////////////////////////////////////
	// simple project info
	
	public String sourceRoot()
	{
		return m_config.getSrcRoot();
	}

	public String projName()
	{
		return m_config.getProjName();
	}

	public int numEntries()
	{
		return m_srcTextEntryArray.size();
	}

	public void setProjWin(ProjectFrame win)
	{
		m_projWin = win;
	}

	private SaveThread	m_saveThread = null;
	// count=0		save disabled
	// count=1		one more save only
	// count=2		regular mode
	private int m_saveCount;
	
	public static CommandThread core = null;
	private ProjectProperties m_config;
	private DataOutputStream	m_nearLog = null;
	private boolean		m_ignoreNearLog = false;
	private boolean		m_modifiedFlag = false;

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

	private TransFrame	m_transFrame = null;
	private ProjectFrame	m_projWin = null;

	private boolean		m_glosFlag;
	private HashMap		m_strEntryHash;	// maps text to strEntry obj
	private ArrayList	m_strEntryList;
	private ArrayList	m_srcTextEntryArray;
	private HashMap		m_glosEntryHash;
	private ArrayList	m_glosEntryList;
	private HashMap		m_indexHash;

	private ArrayList	m_ignoreList;
	private ArrayList	m_extensionList;;
	private ArrayList	m_extensionMapList;;
}
