//-------------------------------------------------------------------------
//  
//  TransFrame.java - 
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
//  Build date:  16Apr2003
//  Copyright (C) 2002, Keith Godfrey
//  keithgodfrey@users.sourceforge.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import javax.swing.undo.*;

class TransFrame extends JFrame implements ActionListener
{
	// Initialization and display code
	public TransFrame()
	{
		super();
		String str;
		m_curEntryNum = -1;
		m_curNear = null;
		m_numEntries = -1;
		m_activeProj = "";
		m_activeFile = "";
		m_nearListNum = -1;

		m_shortcutKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

		m_undo = new UndoManager();

		m_docSegList = new ArrayList();

		createMenus();
		createUI();
		
		////////////////////////////////
		updateUIText();

		enableEvents(0);

		// SIB - find available screen real-estate and adjust size
		//	accordingly
		// KBG - don't be obnoxious and take too much of screen 
		//	(1200x1000 total area should be more than adequate)
		// KBG - in case center is offset (i.e. if taskbar on top of screen)
		//	then offset windows (14apr04)
		GraphicsEnvironment env = 
				GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle scrSize = env.getMaximumWindowBounds();
		Point center = env.getCenterPoint();
		int origX = scrSize.width/2 - center.x;
		int origY = scrSize.height/2 - center.y;
		scrSize.width = (int) (scrSize.width * 0.67);
		if (scrSize.width > 790)
			scrSize.width = 790;
		if (scrSize.height > 1000)
			scrSize.height = 1000;
		setSize(scrSize.width, scrSize.height );
		setLocation(origX, origY);

		addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{
					doQuit();
				}
			});

		if (CommandThread.core == null)
		{
			CommandThread.core = new CommandThread(this);
			int pri = CommandThread.core.getPriority();
			if (pri > 1)
				CommandThread.core.setPriority(pri-1);
			CommandThread.core.setProjWin(m_projWin);
			CommandThread.core.start();
		}
		m_xlPane.requestFocus();

		CommandThread core = CommandThread.core;
		m_font = core.getOrSetPreference(OConsts.TF_SRC_FONT_NAME,
				OConsts.TF_FONT_DEFAULT);
		m_fontSize = core.getOrSetPreference(OConsts.TF_SRC_FONT_SIZE,
				OConsts.TF_FONT_SIZE_DEFAULT);
		int fontSize = 12;
		try
		{
			fontSize = Integer.parseInt(m_fontSize);
		}
		catch (NumberFormatException nfe) { ; }
		m_xlPane.setFont(new Font(m_font, Font.PLAIN, fontSize));
		m_matchViewer.setFont(new Font(m_font, Font.PLAIN, fontSize));

		// check this only once as it can be changed only at compile time
		// should be OK, but customization might have messed it up
		String start = OStrings.TF_CUR_SEGMENT_START;
		int zero = start.lastIndexOf('0');
		if ((zero > 4) &&	// 4 to reserve room for 10000 digit
				(start.charAt(zero-1) == '0')	&&
				(start.charAt(zero-2) == '0')	&&
				(start.charAt(zero-3) == '0'))
		{
			m_segmentTagHasNumber = true;
		}
		else
			m_segmentTagHasNumber = false;
	}

	protected void createUI()
	{
		int i;

		// create translation edit field
		//m_xlPane = new JTextPane();
		m_xlPane = new XLPane();
		DefaultStyledDocument doc = new 
					DefaultStyledDocument(new StyleContext());
		doc.addUndoableEditListener(m_undo);
		m_xlPane.setDocument(doc);
		m_xlScroller = new JScrollPane(m_xlPane);
		m_xlScroller.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		m_xlPane.setText(OStrings.TF_INTRO_MESSAGE);
		
		m_statusLabel = new JLabel();

		Container cont = getContentPane();
		cont.add(m_statusLabel, BorderLayout.NORTH);
		cont.add(m_xlScroller, BorderLayout.CENTER);

		m_projWin = new ProjectFrame(this);
		m_matchViewer = new MatchWindow();
		m_matchViewer.show();
	}

	protected void createMenus()
	{
		////////////////////////////////
		// create menus
		JMenuBar mb = new JMenuBar();
		// file
		m_mFile = new JMenu();
		m_mFile.setMnemonic(KeyEvent.VK_F);
		m_miFileOpen = new JMenuItem();
		m_miFileOpen.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_O, m_shortcutKey));
		m_miFileOpen.setMnemonic(KeyEvent.VK_O);
		m_miFileOpen.addActionListener(this);
		m_mFile.add(m_miFileOpen);

		m_miFileCreate = new JMenuItem();
		// SIB: add accelerator to Create; add mnemonic
		m_miFileCreate.setMnemonic(KeyEvent.VK_N);
		m_miFileCreate.setAccelerator(
					KeyStroke.getKeyStroke(KeyEvent.VK_N, m_shortcutKey));
		m_miFileCreate.addActionListener(this);
		m_mFile.add(m_miFileCreate);

		m_miFileCompile = new JMenuItem();
		m_miFileCompile.setMnemonic(KeyEvent.VK_C);
		m_miFileCompile.addActionListener(this);
		m_mFile.add(m_miFileCompile);
		
		m_mFile.addSeparator();

		m_miFileProjWin = new JMenuItem();
		m_miFileProjWin.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_L,  m_shortcutKey));
		m_miFileProjWin.setMnemonic(KeyEvent.VK_L);
		m_miFileProjWin.addActionListener(this);
		m_mFile.add(m_miFileProjWin);
		
		m_miFileMatchWin = new JMenuItem();
		m_miFileMatchWin.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_M,  m_shortcutKey));
		m_miFileMatchWin.setMnemonic(KeyEvent.VK_M);
		m_miFileMatchWin.addActionListener(this);
		m_mFile.add(m_miFileMatchWin);
		
		m_mFile.addSeparator();

		m_miFileSave = new JMenuItem();
		m_miFileSave.addActionListener(this);
		m_miFileSave.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_S, m_shortcutKey));
		m_miFileSave.setMnemonic(KeyEvent.VK_S);
		m_mFile.add(m_miFileSave);

		m_miFileQuit = new JMenuItem();
		m_miFileQuit.addActionListener(this);
		m_mFile.addSeparator();
		m_miFileQuit.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_Q,  m_shortcutKey));
		m_miFileQuit.setMnemonic(KeyEvent.VK_Q);
		m_mFile.add(m_miFileQuit);
		mb.add(m_mFile);

		// edit
		m_mEdit = new JMenu();
		m_mEdit.setMnemonic(KeyEvent.VK_E);
		m_miEditUndo = new JMenuItem();
		m_miEditUndo.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_Z,  m_shortcutKey));
		m_miEditUndo.setMnemonic(KeyEvent.VK_Z);
		m_miEditUndo.addActionListener(this);
		m_mEdit.add(m_miEditUndo);
		
		m_miEditRedo = new JMenuItem();
		m_miEditRedo.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_Y,  m_shortcutKey));
		m_miEditRedo.addActionListener(this);
		m_miEditRedo.setMnemonic(KeyEvent.VK_Y);
		m_mEdit.add(m_miEditRedo);
		
		m_mEdit.addSeparator();

		m_miEditRecycle = new JMenuItem();
		m_miEditRecycle.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_R,  m_shortcutKey));
		m_miEditRecycle.setMnemonic(KeyEvent.VK_R);
		m_miEditRecycle.addActionListener(this);
		m_mEdit.add(m_miEditRecycle);

		m_miEditInsert = new JMenuItem();
		m_miEditInsert.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_I,  m_shortcutKey));
		m_miEditInsert.setMnemonic(KeyEvent.VK_I);
		m_miEditInsert.addActionListener(this);
		m_mEdit.add(m_miEditInsert);

		m_mEdit.addSeparator();

		m_miEditFind = new JMenuItem();
		m_miEditFind.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_F, m_shortcutKey));
		m_miEditFind.setMnemonic(KeyEvent.VK_F);
		m_miEditFind.addActionListener(this);
		m_mEdit.add(m_miEditFind);

		m_miEditUntrans = new JMenuItem();
		m_miEditUntrans.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_U,  m_shortcutKey));
		m_miEditUntrans.setMnemonic(KeyEvent.VK_U);
		m_miEditUntrans.addActionListener(this);
		m_mEdit.add(m_miEditUntrans);

		m_miEditPrev = new JMenuItem();
		m_miEditPrev.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_P,  m_shortcutKey));
		m_miEditPrev.setMnemonic(KeyEvent.VK_P);
		m_miEditPrev.addActionListener(this);
		m_mEdit.add(m_miEditPrev);

		m_mEdit.addSeparator();

		m_miEditCompare1 = new JMenuItem();
		m_miEditCompare1.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_1,  m_shortcutKey));
		m_miEditCompare1.setMnemonic(KeyEvent.VK_1);
		m_miEditCompare1.addActionListener(this);
		m_mEdit.add(m_miEditCompare1);

		m_miEditCompare2 = new JMenuItem();
		m_miEditCompare2.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_2,  m_shortcutKey));
		m_miEditCompare2.setMnemonic(KeyEvent.VK_2);
		m_miEditCompare2.addActionListener(this);
		m_mEdit.add(m_miEditCompare2);

		m_miEditCompare3 = new JMenuItem();
		m_miEditCompare3.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_3,  m_shortcutKey));
		m_miEditCompare3.setMnemonic(KeyEvent.VK_3);
		m_miEditCompare3.addActionListener(this);
		m_mEdit.add(m_miEditCompare3);

		m_miEditCompare4 = new JMenuItem();
		m_miEditCompare4.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_4,  m_shortcutKey));
		m_miEditCompare4.setMnemonic(KeyEvent.VK_4);
		m_miEditCompare4.addActionListener(this);
		m_mEdit.add(m_miEditCompare4);

		m_miEditCompare5 = new JMenuItem();
		m_miEditCompare5.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_5,  m_shortcutKey));
		m_miEditCompare5.setMnemonic(KeyEvent.VK_5);
		m_miEditCompare5.addActionListener(this);
		m_mEdit.add(m_miEditCompare5);

		mb.add(m_mEdit);

		// display
		m_mDisplay = new JMenu();
		m_mDisplay.setMnemonic(KeyEvent.VK_C);

		m_miDisplayFont = new JMenuItem();
		m_miDisplayFont.setMnemonic(KeyEvent.VK_F);
		m_miDisplayFont.addActionListener(this);
		m_mDisplay.add(m_miDisplayFont);

		mb.add(m_mDisplay);
		
		// tools
		m_mTools = new JMenu();
		m_mTools.setMnemonic(KeyEvent.VK_T);
		m_miToolsPseudoTrans = new JMenuItem();
		m_miToolsPseudoTrans.addActionListener(this);
		m_mTools.add(m_miToolsPseudoTrans);

		m_miToolsValidateTags = new JMenuItem();
		m_miToolsValidateTags.setMnemonic(KeyEvent.VK_T);
		m_miToolsValidateTags.addActionListener(this);
		m_mTools.add(m_miToolsValidateTags);

		m_miToolsMergeTMX = new JMenuItem();
		m_miToolsMergeTMX.setMnemonic(KeyEvent.VK_M);
		m_miToolsMergeTMX.setEnabled(false);
		m_miToolsMergeTMX.addActionListener(this);
		m_mTools.add(m_miToolsMergeTMX);

		mb.add(m_mTools);
		
		m_mVersion = new JMenu();
		m_mVersion.setMnemonic(KeyEvent.VK_O);
		m_miVersionHelp = new JMenuItem();
		m_miVersionHelp.setAccelerator(
					KeyStroke.getKeyStroke(KeyEvent.VK_F1,0));
		m_miVersionHelp.setMnemonic(KeyEvent.VK_H);
		m_miVersionHelp.addActionListener(this);
		m_mVersion.add(m_miVersionHelp);

		mb.add(m_mVersion);

		setJMenuBar(mb);
	}

	public void updateUIText()
	{
		String str;

		doSetTitle();

		m_matchViewer.setTitle(OStrings.TF_MATCH_VIEWER_TITLE);
		m_strFuzzy = OStrings.TF_FUZZY;
		m_strGlossaryItem = OStrings.TF_GLOSSARY;
		m_strSrcText = OStrings.TF_SRCTEXT;
		m_strTranslation = OStrings.TF_TRANSLATION;
		m_strScore = OStrings.TF_SCORE;
		m_strNone = OStrings.TF_NONE;

		m_mFile.setText(OStrings.TF_MENU_FILE);
		m_miFileOpen.setText(OStrings.TF_MENU_FILE_OPEN);
		m_miFileCreate.setText(OStrings.TF_MENU_FILE_CREATE);
		m_miFileCompile.setText(OStrings.TF_MENU_FILE_COMPILE);
		m_miFileProjWin.setText(OStrings.TF_MENU_FILE_PROJWIN);
		m_miFileMatchWin.setText(OStrings.TF_MENU_FILE_MATCHWIN);
		m_miFileSave.setText(OStrings.TF_MENU_FILE_SAVE);
		m_miFileQuit.setText(OStrings.TF_MENU_FILE_QUIT);

		m_mEdit.setText(OStrings.TF_MENU_EDIT);
		m_miEditUndo.setText(OStrings.TF_MENU_EDIT_UNDO);
		m_miEditRedo.setText(OStrings.TF_MENU_EDIT_REDO);
		m_miEditPrev.setText(OStrings.TF_MENU_EDIT_PREV);
		m_miEditCompare1.setText(OStrings.TF_MENU_EDIT_COMPARE_1);
		m_miEditCompare2.setText(OStrings.TF_MENU_EDIT_COMPARE_2);
		m_miEditCompare3.setText(OStrings.TF_MENU_EDIT_COMPARE_3);
		m_miEditCompare4.setText(OStrings.TF_MENU_EDIT_COMPARE_4);
		m_miEditCompare5.setText(OStrings.TF_MENU_EDIT_COMPARE_5);
		m_miEditRecycle.setText(OStrings.TF_MENU_EDIT_RECYCLE);
		m_miEditInsert.setText(OStrings.TF_MENU_EDIT_INSERT);
		m_miEditFind.setText(OStrings.TF_MENU_EDIT_FIND);
		m_miEditUntrans.setText(OStrings.TF_MENU_EDIT_NEXT_UNTRANS);
		
		m_mDisplay.setText(OStrings.TF_MENU_DISPLAY);
		m_miDisplayFont.setText(OStrings.TF_MENU_DISPLAY_FONT);
		
		m_mTools.setText(OStrings.TF_MENU_TOOLS);
		m_miToolsPseudoTrans.setText(OStrings.TF_MENU_TOOLS_PSEUDO);
		m_miToolsValidateTags.setText(OStrings.TF_MENU_TOOLS_VALIDATE);
		m_miToolsMergeTMX.setText(OStrings.TF_MENU_TOOLS_MERGE_TMX);

		m_mVersion.setText(OmegaTVersion.name());
		m_miVersionHelp.setText(OStrings.TF_MENU_VERSION_HELP);
		
		// KBG - the UI looks bad w/ misplaced mnemonics, but this is
		//	better than hard coding their location for localized versions
//		m_miFileProjWin.setDisplayedMnemonicIndex(10);
//		m_miFileMatchWin.setDisplayedMnemonicIndex(5);
//		m_miToolsValidateTags.setDisplayedMnemonicIndex(9);
	}

	protected void updateMenuSelectabilityStates()
	{
		// if not in project, disable most of menu items
		// otherwise, enable everything 
		// TODO XXX update menu selectability states
	}

	protected boolean checkClose()
	{
		// user has requested to close document - see if they really want
		//  to close and if so, if they want to save the current entry

		// display dialog
		// if user says cancel close, return false
		// if user says continue and save, call commitEntry() and return true
		// if user says continue w/ no save, simply return true
		
		//commitEntry();	// saves current entry
		
		return true;
	}

	///////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////
	// command handling
	
	protected void doQuit()
	{
		// shutdown
		doSave();
		// display dialog to verify that user wants to quit - see
		//	if they want to save current entry (if it's been modified)
		if ((m_projectLoaded == true) && (checkClose() == false))
			return;


		CommandThread.core.signalStop();
		for (int i=0; i<25; i++)
		{
			while (CommandThread.core != null)
			{
				try { Thread.sleep(10); }
				catch (InterruptedException e) { ; }
			}
			if (CommandThread.core == null)
				break;
		}

		System.exit(0);
	}

	protected void doPseudoTrans() 
	{
		if (m_projectLoaded == false)
			return;

		// since this is a destructive operation, verify the user
		// _REALLY_ wants to do this
		String title = OStrings.TF_PSEUDOTRANS_RUSURE_TITLE;
		String msg = OStrings.TF_PSEUDOTRANS_RUSURE;
		int choice = JOptionPane.showConfirmDialog(this, msg, title,
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE);
		if (choice == JOptionPane.CANCEL_OPTION)
			return;

		CommandThread.core.pseudoTranslate();
		activateEntry();
	}

	protected void doValidateTags()
	{
		ArrayList suspects = CommandThread.core.validateTags();
		if (suspects.size() > 0)
		{
			// create list of suspect strings - use ContextFrame for now
			ContextFrame cf = new ContextFrame(this, true, false);
			cf.show();
			cf.displayStringList(suspects, OStrings.TF_NOTICE_BAD_TAGS);
		}
		else
		{
			// show dialog saying all is OK
			JOptionPane.showMessageDialog(this, 
						OStrings.TF_NOTICE_OK_TAGS,
						OStrings.TF_NOTICE_TITLE_TAGS,
						JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public void doNextUntrans()
	{
		if (m_projectLoaded == false)
			return;
		
		int cnt = CommandThread.core.numEntries();
		int i;
		for (i=1; i<cnt; i++)
		{
			int cur = (m_curEntryNum + i) % cnt;
			StringEntry se = CommandThread.core.getStringEntry(cur);
			String loc = se.getTrans();
			String src = se.getSrcText();
			if ((src.length() > 0) && (src.equals(loc) == true))
			{
				m_curEntryNum = cur;
				if ((cur > m_xlLastEntry) || (cur < m_xlFirstEntry))
				{
					loadDocument();
				}
				commitEntry();
				activateEntry();
				i = -1;
				break;
			}
		}
		if (i > 0)
		{
			setMessageText(OStrings.TF_NO_MORE_UNTRANSLATED);
		}
	}

	public void doNextEntry()
	{
		if (m_projectLoaded == false)
			return;
		
		commitEntry();

		m_curEntryNum++;
		if (m_curEntryNum > m_xlLastEntry)
		{
			if (m_curEntryNum >= CommandThread.core.numEntries())
				m_curEntryNum = 0;
			loadDocument();
		}
		activateEntry();
	}

	public void doPrevEntry()
	{
		if (m_projectLoaded == false)
			return;
		
		commitEntry();

		m_curEntryNum--;
		if (m_curEntryNum < m_xlFirstEntry)
		{
			if (m_curEntryNum < 0)
				m_curEntryNum = CommandThread.core.numEntries() - 1;
			loadDocument();
		}
		activateEntry();
	}

	// insert current fuzzy match at cursor position
	public void doInsertTrans()
	{
		if (m_projectLoaded == false)
			return;
		
		if (m_curNear == null)
			return;

		StringEntry se = m_curNear.str;
		String s = se.getTrans();
		int pos = m_xlPane.getCaretPosition();
		m_xlPane.select(pos, pos);
		m_xlPane.replaceSelection(s);
	}

	// replace entire edit area with active fuzzy match
	public void doRecycleTrans()
	{
		if (m_projectLoaded == false)
			return;
		
		if (m_curNear == null)
			return;

		StringEntry se = m_curNear.str;
		doReplaceEditText(se.getTrans());
	}

	// overwrite current text in edit field with source translation
	protected void doCopySourceText()
	{
		doReplaceEditText(m_curEntry.getSrcText());
	}

	protected void doReplaceEditText(String text)
	{
		if (m_projectLoaded == false)
			return;
		
		if (m_curNear == null)
			return;

		if (text != null)
		{
			// remove current text
			// build local offsets
			int start = m_segmentStartOffset + m_sourceDisplayLength +
				OStrings.TF_CUR_SEGMENT_START.length() + 1;
			int end = m_xlPane.getText().length() - m_segmentEndInset -
				OStrings.TF_CUR_SEGMENT_END.length();

			// remove text
//System.out.println("removing text "+start+" -> "+end+" length:"+(end-start));
			m_xlPane.select(start, end);
			m_xlPane.replaceSelection(text);
		}
	}

	protected void doCompareN(int n)
	{
		if (m_projectLoaded == false)
			return;
		
		StringEntry se = m_curEntry.getStrEntry();
		if (n < se.getNearList().size())
			m_nearListNum = n;
		updateFuzzyInfo();
	}

	public void doUnloadProject()
	{
		if ((m_projectLoaded == true) && (checkClose() == false))
			return;

		m_projectLoaded = false;
		m_xlPane.setText("");
	}

	// display dialog allowing selection of source and target language fonts
	protected void doFont()
	{
		MFontSelection dlg = new MFontSelection(this);
		dlg.show();
		if (dlg.isChanged())
		{
			// fonts have changed  
			// first commit current translation
			commitEntry();
			int fontSize = 12;
			try
			{
				fontSize = Integer.parseInt(m_fontSize);
			}
			catch (NumberFormatException nfe) { ; }
			Font font = new Font(m_font, Font.PLAIN, fontSize);
			m_xlPane.setFont(font);
			m_matchViewer.setFont(font);
			activateEntry();
		}
	}

	protected void doMergeTMX()
	{
//System.out.println("merge TMX not implemented");
		// needs dialog to specify source and target languages, source
		//	dir and target file
		// if in project, supply default file values to TM folder and 
		//	<project>/merge.tmx
		//StaticUtils.mergeTmxFiles(File out, String root, srcLang, targetLang);
	}

	protected void doSave()
	{
		if (m_projectLoaded == false)
			return;
		
		RequestPacket pack;
		pack = new RequestPacket(RequestPacket.SAVE, this);
		CommandThread.core.messageBoardPost(pack);
	}

	protected void doLoadProject()
	{
		doUnloadProject();
		m_xlPane.setText("");
		m_matchViewer.reset();
		
		RequestPacket load;
		load = new RequestPacket(RequestPacket.LOAD, this);
		CommandThread.core.messageBoardPost(load);
	}

	protected void doGotoEntry(int entryNum)
	{
		if (m_projectLoaded == false)
			return;
		
		commitEntry();

		m_curEntryNum = entryNum - 1;
		if (m_curEntryNum < m_xlFirstEntry)
		{
			if (m_curEntryNum < 0)
				m_curEntryNum = CommandThread.core.numEntries();
			loadDocument();
		}
		else if (m_curEntryNum > m_xlLastEntry)
		{
			if (m_curEntryNum >= CommandThread.core.numEntries())
				m_curEntryNum = 0;
			loadDocument();
		}
		activateEntry();
	}

	public void doGotoEntry(String str)
	{
		int num = -1;
		try
		{
			num = Integer.parseInt(str);
			doGotoEntry(num);
		}
		catch (NumberFormatException e) { ; }
	}

	public void finishLoadProject()
	{
		m_numEntries = CommandThread.core.numEntries();
		m_activeProj = CommandThread.core.projName();
		m_activeFile = "";
		m_curEntryNum = 0;
		loadDocument();
		m_projectLoaded = true;
	}

	private void doCompileProject()
	{
		if (m_projectLoaded == false)
			return;
		try 
		{
			CommandThread.core.compileProject();
		}
		catch(IOException e)
		{
			displayError(OStrings.TF_COMPILE_ERROR, e);
		}
		// TODO - cleanup on error
	}

	protected void doSetTitle()
	{
		String s = OStrings.TF_TITLE;
		if (m_activeProj.compareTo("") != 0)
		{
			String file = m_activeFile.substring(
					CommandThread.core.sourceRoot().length());
			s += " - " + m_activeProj + " :: " + file;
		}
		setTitle(s);
	}

	protected void doFind()
	{
		int start = m_xlPane.getSelectionStart();
		int end = m_xlPane.getSelectionEnd();
		String selection = m_xlPane.getSelectedText();
		if (selection != null)
		{
			selection.trim();
			if (selection.length() < 3)
			{
				selection = null;
			}
		}

		SearchThread srch = new SearchThread(this, selection);
		srch.start();
	}

	public void setMessageText(String str)
	{
		m_statusLabel.setText(str);
	}

	/////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	// internal routines 

	class DocumentSegment
	{
		// length is the char count of the display value of the segment
		//	(i.e. trans if it exists, else src)
		// it also includes the 2 newlines used for spacing
		public int	length = 0;		// display length
	}

	// displays all segments in current document
	// displays translation for each segment if it's available (in dark gray)
	// otherwise displays source text (in black)
	// stores length of each displayed segment plus its starting offset
	protected void loadDocument() 
	{
		m_docReady = false;

		// clear old text
		m_xlPane.setText("");
		m_docSegList.clear();
		m_curEntry = CommandThread.core.getSTE(m_curEntryNum);
		setMessageText(OStrings.TF_LOADING_FILE + 
								m_curEntry.getSrcFile().name);
		Thread.currentThread().yield();	// let UI update
		m_xlFirstEntry = m_curEntry.getFirstInFile();
		m_xlLastEntry = m_curEntry.getLastInFile();

		int len = 0;
		DocumentSegment docSeg;
		StringBuffer textBuf = new StringBuffer();
		
		for (int entryNum=m_xlFirstEntry; entryNum<=m_xlLastEntry; entryNum++)
		{
			docSeg = new DocumentSegment();
			
			SourceTextEntry ste = CommandThread.core.getSTE(entryNum);
			String text = ste.getTranslation();
			// set text and font
			if (text.length() == 0) 
			{
				// no translation available - use source text
				text = ste.getSrcText(); 
			}
			text += "\n\n";
			
			textBuf.append(text);

			docSeg.length = text.length();
//System.out.println(entryNum+"\t"+docSeg.length+"\t"+text);
			m_docSegList.add(docSeg);
		}
		m_xlPane.setText(textBuf.toString());
		
		setMessageText("");
		Thread.currentThread().yield();
	}

	///////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////
	// display oriented code

	// display fuzzy matching info if it's available
	// don't call this directly - should only be called through doCompareN
	protected void updateFuzzyInfo() 
	{
		if (m_projectLoaded == false)
			return;

//System.out.println("updating fuzzy info - '"+m_curEntry.getSrcText()+"'");

//System.out.println("checking for near terms...");
				// see if there are any matches
			StringEntry se = m_curEntry.getStrEntry();
			if (se.getNearList().size() > 0)
			{
//System.out.println("  found "+ se.getNearList().size());
				m_nearList = (LinkedList) se.getNearList().clone();
				NearString ns = (NearString) m_nearList.get(m_nearListNum);
				m_curNear = ns;
			}
			else
			{
//System.out.println("  found none");
				// hide match windows if they're visible
				m_nearList = null;
				m_nearListNum = -1;
				m_curNear = null;
				m_matchViewer.updateMatchText();
				return;
			}

			//String srcText = m_curEntry.getSrcText();
			//formatNearText(srcText, m_curNear.parAttr, Color.red, 
			//		Color.darkGray, m_xlDoc, m_segmentStartOffset, 
			//		srcText.length());
			
			//String oldStr = m_curNear.str.getSrcText();
//System.out.println("old src text: "+oldStr);

			// remember length of base string (before fuzzy % added)
			//int oldStrLen = oldStr.length();
//		String proj = m_curNear.proj;

			//formatNearText(oldStr, m_curNear.attr, Color.blue, Color.black, 
			//		m_oldSrcDoc, 0, oldStrLen);
			//String locStr = m_curNear.str.getTrans();
//			m_matchViewer.addMatchTerm(oldStr, locStr, 
//					(int) (m_curNear.score * 100), "");
		StringEntry curEntry = m_curEntry.getStrEntry();
		if (curEntry.getNearList().size() > 0)
		{
			NearString ns;
			int ctr = 0;
			int offset;
			int start = -1;
			int end = -1;
			ListIterator li = curEntry.getNearList().listIterator();
			while (li.hasNext())
			{
				ns = (NearString) li.next();
				String oldStr = ns.str.getSrcText();
				String locStr = ns.str.getTrans();
				String proj = ns.proj;
				offset = m_matchViewer.addMatchTerm(oldStr, locStr, 
						(int) (ns.score * 100), proj);
				if (ctr == m_nearListNum)
					start = offset;
				else if (ctr == (m_nearListNum+1))
					end = offset;
				if (++ctr > 5)
					break;
			}
			m_matchViewer.hiliteRange(start, end);
		}
		m_matchViewer.updateMatchText();
	}
	
	private void commitEntry()
	{
		if (m_projectLoaded == false)
		{
			return;
		}
		// read current entry text and commit it to memory if it's changed
		// clear out segment markers while we're at it

		// +1 is for newline between source text and SEGMENT 
		int start = m_segmentStartOffset + m_sourceDisplayLength + 
					OStrings.TF_CUR_SEGMENT_START.length() + 1;
		int end = m_xlPane.getText().length() - m_segmentEndInset - 
					OStrings.TF_CUR_SEGMENT_END.length();
		String s = m_xlPane.getText().substring(start, end);
		m_xlPane.select(start, end);
		MutableAttributeSet mattr = null;
		mattr = new SimpleAttributeSet();
		StyleConstants.setForeground(mattr, Color.darkGray);
		m_xlPane.setCharacterAttributes(mattr, true);
		
		m_xlPane.select(end, m_xlPane.getText().length() - m_segmentEndInset);
		m_xlPane.replaceSelection("");
		m_xlPane.select(m_segmentStartOffset, start);
		m_xlPane.replaceSelection("");
			
		if (s.equals(""))
		{
			s = m_curEntry.getSrcText();
		}

		// convert hard return to soft
		s = s.replace((char) 0x0a, (char) 0x8d);
		m_curEntry.setTranslation(s);
		
		DocumentSegment docSeg = (DocumentSegment) 
							m_docSegList.get(m_curEntryNum - m_xlFirstEntry);
		docSeg.length = s.length() + "\n\n".length();	
		
		// update the length parameters of all changed segments
		// update strings in display
		if (s.equals(m_curTrans) == false)
		{
			// update memory
			m_curEntry.setTranslation(s);

			// update display
			// find all identical strings and redraw them
			SourceTextEntry ste = CommandThread.core.getSTE(m_curEntryNum);
			StringEntry se = ste.getStrEntry();
			ListIterator it = se.getParentList().listIterator();
			int entry;
			int offset;
			int i;
			while (it.hasNext())
			{
				ste = (SourceTextEntry) it.next();
				entry = ste.entryNum();
				if ((entry >= m_xlFirstEntry) && (entry <= m_xlLastEntry))
				{
					// found something to update
					// find offset to this segment, remove it and
					//	replace the updated text
					offset = 0;
					// current entry is already handled 
					if (entry == m_curEntryNum)
						continue;

					// build offset
					for (i=m_xlFirstEntry; i<entry; i++)
					{
						docSeg = (DocumentSegment) m_docSegList.get(
								i-m_xlFirstEntry);
						offset += docSeg.length;
					}
					// replace old text w/ new
					docSeg = (DocumentSegment) m_docSegList.get(
								entry - m_xlFirstEntry);
					m_xlPane.select(offset, offset+docSeg.length);
					m_xlPane.replaceSelection(s + "\n\n");
					docSeg.length = s.length() + "\n\n".length();
//					insertSegment(entry, offset, null, false, false);
				}
			}
		}
		m_undo.die();
	}

	// activate current entry by displaying source text and imbedding
	//	displaying text in markers
	// move document focus to current entry
	// make sure fuzzy info displayed if available and wanted
	public synchronized void activateEntry() 
	{
		if (m_projectLoaded == false)
			return;

		int i;
		DocumentSegment docSeg;

		// recover data about current entry
		m_curEntry = CommandThread.core.getSTE(m_curEntryNum);
		String srcText = m_curEntry.getSrcText();
		String trans = m_curEntry.getTranslation();;
		
		m_sourceDisplayLength = srcText.length();
		
		// sum up total character offset to current segment start
		m_segmentStartOffset = 0;
		for (i=m_xlFirstEntry; i<m_curEntryNum; i++)
		{
			docSeg = (DocumentSegment) m_docSegList.get(i-m_xlFirstEntry);
			m_segmentStartOffset += docSeg.length; // length includes \n
		}

		docSeg = (DocumentSegment) m_docSegList.get(
					m_curEntryNum - m_xlFirstEntry);
		// -2 to move inside newlines at end of segment
		int paneLen = m_xlPane.getText().length();
		m_segmentEndInset = paneLen - (m_segmentStartOffset + docSeg.length-2);

		// get label tags
		String startStr = "\n" + OStrings.TF_CUR_SEGMENT_START;
		String endStr = OStrings.TF_CUR_SEGMENT_END;
		if (m_segmentTagHasNumber)
		{
			// put entry number in first tag
			int num = m_curEntryNum + 1;
			int ones;
			// do it digit by digit - there's a better way (like sprintf)
			//	but this works just fine
			String disp = "";
			while (num > 0)
			{
				ones = num % 10;
				num /= 10;
				disp = ((int) ones) + disp;
			}
			int zero = startStr.lastIndexOf('0');
			startStr = startStr.substring(0, zero-disp.length()+1) + 
				disp + startStr.substring(zero+1);
		}

		// append to end of segment first because this operation is done
		//	by reference to end of file which will change after insert
		m_xlPane.select(paneLen-m_segmentEndInset, paneLen-m_segmentEndInset);
		MutableAttributeSet mattr = null;
		mattr = new SimpleAttributeSet();
		StyleConstants.setBold(mattr, true);
		m_xlPane.setCharacterAttributes(mattr, true);
		m_xlPane.replaceSelection(endStr);
		paneLen = -1;	// after replacement this is no longer accurate

		m_xlPane.select(m_segmentStartOffset, m_segmentStartOffset);
		String insertText = srcText + startStr;
		m_xlPane.replaceSelection(insertText);
		m_xlPane.select(m_segmentStartOffset, m_segmentStartOffset + 
				insertText.length() - 1);
		m_xlPane.setCharacterAttributes(mattr, true);
//System.out.println("inserting text '"+srcText+"' (+startString) at "+(m_segmentStartOffset));

		// TODO XXX format source text if there is near match
		
		if (m_curEntry.getSrcFile().name.compareTo(m_activeFile) != 0)
		{
			m_activeFile = m_curEntry.getSrcFile().name;
			doSetTitle();
		}
		
		// TODO set word counts

		doCompareN(0);

		// add glossary terms and fuzzy match info to match window
		StringEntry curEntry = m_curEntry.getStrEntry();
		if (curEntry.getGlosList().size() > 0)
		{
			// TODO do something with glossary terms
			m_glossaryLength = curEntry.getGlosList().size();
			ListIterator li = curEntry.getGlosList().listIterator();
			while (li.hasNext())
			{
				GlossaryEntry glos = (GlossaryEntry) li.next();
				m_matchViewer.addGlosTerm(glos.getSrcText(), glos.getLocText(),
						glos.getCommentText());
			}
		
		}
		else
			m_glossaryLength = 0;
		m_matchViewer.updateGlossaryText();

		int nearLength = curEntry.getNearList().size();
		if (nearLength > 5)
			nearLength = 5;
		
		if ((nearLength > 0) && (m_glossaryLength > 0))
		{
			// display text indicating both categories exist
			Object obj[] = { 
					new Integer(nearLength), 
					new Integer(m_glossaryLength) };
			m_statusLabel.setText(MessageFormat.format(
							OStrings.TF_NUM_NEAR_AND_GLOSSARY, obj));
		}
		else if (nearLength > 0)
		{
			Object obj[] = { new Integer(nearLength) };
			m_statusLabel.setText(MessageFormat.format(
							OStrings.TF_NUM_NEAR, obj));
		}
		else if (m_glossaryLength > 0)
		{
			Object obj[] = { new Integer(m_glossaryLength) };
			m_statusLabel.setText(MessageFormat.format(
							OStrings.TF_NUM_GLOSSARY, obj));
		}
		else
			m_statusLabel.setText("");

//System.out.println(" segment text '"+m_curEntry.getSrcText()+"' -> '"+m_curEntry.getTranslation()+"'");

		// TODO - hilite translation area in yellow

		// set caret position

		// try to scroll so next 3 entries are displayed after current entry
		//	or first entry ending after the 500 characters mark
		// to do this, set cursor 3 entries down, then reset it to 
		//	begging of source text in current entry, then finnaly into
		//	editing region
		int padding = 0;
		int j;
		for (i=m_curEntryNum+1-m_xlFirstEntry, j=0; 
				i<=m_xlLastEntry-m_xlFirstEntry; 
				i++, j++)
		{
			docSeg = (DocumentSegment) m_docSegList.get(i);
			padding += docSeg.length;
			if ((j > 2) || (padding > 500))
				break;
		}
		// don't try to set caret after end of document
		if (padding > m_segmentEndInset)
			padding = m_segmentEndInset;
		m_xlPane.setCaretPosition(m_xlPane.getText().length() - 
				m_segmentEndInset + padding);

		// try to make sure entire segment displays
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				// make sure 2 newlines above current segment are visible
				int loc = m_segmentStartOffset - 3;
				if (loc < 0)
					loc = 0;
				m_xlPane.setCaretPosition(loc);
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						checkCaret();
					}
				});

			}
		});
//
//		checkCaret();
		if (m_docReady == false)
		{
			m_docReady = true;
		}
		m_undo.die();
	}

	// colors text in the given document according to the text differences
	//	versus compared-to text.  The supplied color indicates
	//	new text and green text indicates the same word exists between
	//	strings but has different neighbors.
	protected void formatNearText(String text, byte[] attrList, 
			Color uniqColor, Color textColor, DefaultStyledDocument doc, 
			int startOffset, int length) throws BadLocationException
	{
//System.out.println("format near text");
		int start;
		int end;

		// reset color of text to default value
		ArrayList tokenList = new ArrayList();
		StaticUtils.tokenizeText(text, tokenList);
		int numTokens = tokenList.size();
//System.out.println("  "+numTokens+" tokens in string '"+text+"'");
		for (int i=0; i<numTokens; i++)
		{
			if (i == (numTokens-1))
				end = startOffset + length;
			else
				end = startOffset + ((Token) tokenList.get(i+1)).offset;
			start = startOffset + ((Token) tokenList.get(i)).offset;
//System.out.println("  range "+start+" to "+end);

//			mattr = new SimpleAttributeSet();
//			if ((attrList[i] & StringData.UNIQ) != 0)
//			{
//System.out.println("    uniq");
//				StyleConstants.setForeground(mattr, uniqColor);
//			}
//			else if ((attrList[i] & StringData.PAIR) != 0)
//			{
//System.out.println("    near");
//				StyleConstants.setForeground(mattr, Color.green);
//			}
//			doc.setCharacterAttributes(start, end-start, mattr, false);
		}
	}

	public void actionPerformed(ActionEvent evt)
	{
		Object evtSrc = evt.getSource();
		if (evtSrc instanceof JMenuItem)
		{
			if (evtSrc == m_miFileQuit)
			{ 
				doQuit();
			}
			else if (evtSrc == m_miFileOpen)
			{
				doLoadProject();
			}
			else if (evtSrc == m_miFileSave)
			{
				doSave();
			}
			else if (evtSrc == m_miFileCreate)
			{
				CommandThread.core.createProject();
			}
			else if (evtSrc == m_miFileCompile)
			{
				doCompileProject();
			}
			else if (evtSrc == m_miFileMatchWin)
			{
				if (m_matchViewer.isVisible() == true)
					m_matchViewer.hide();
				else
					m_matchViewer.show();
			}
			else if (evtSrc == m_miFileProjWin)
			{
				if (m_projWin != null)
				{
					m_projWin.show();
					m_projWin.toFront();
				}
			}
			else if (evtSrc == m_miEditUndo)
			{
				try 
				{
					m_undo.undo();
				}
				catch (CannotUndoException cue)	{ ; }
			}
			else if (evtSrc == m_miEditRedo)
			{
				try 
				{
					m_undo.redo();
				}
				catch (CannotRedoException cue)	{ ; }
			}
			else if (evtSrc == m_miEditPrev)
			{
				doPrevEntry();
			}
			else if (evtSrc == m_miEditUntrans)
			{
				doNextUntrans();
			}
			else if (evtSrc == m_miEditRecycle)
			{
				doRecycleTrans();
			}
			else if (evtSrc == m_miEditInsert)
			{
				doInsertTrans();
			}
			else if (evtSrc == m_miEditFind)
			{
				// doFind initiates the search - the menu option
				//  will reset the cursor to find field or copy hilited 
				//  text to find window then launch find
				doFind();
			}
			else if (evtSrc == m_miEditCompare1)
			{
				doCompareN(0);
			}
			else if (evtSrc == m_miEditCompare2)
			{
				doCompareN(1);
			}
			else if (evtSrc == m_miEditCompare3)
			{
				doCompareN(2);
			}
			else if (evtSrc == m_miEditCompare4)
			{
				doCompareN(3);
			}
			else if (evtSrc == m_miEditCompare5)
			{
				doCompareN(4);
			}
			else if (evtSrc == m_miDisplayFont)
			{
				doFont();
			}
			else if (evtSrc == m_miToolsPseudoTrans)
			{
				doPseudoTrans();
			}
			else if (evtSrc == m_miToolsValidateTags)
			{
				doValidateTags();
			}
			else if (evtSrc == m_miToolsMergeTMX)
			{
				doMergeTMX();
			}
			else if (evtSrc == m_miVersionHelp)
			{
				HelpFrame hf = new HelpFrame();
				hf.show();
				hf.toFront();
			}
		}
	}

	class MFontSelection extends JDialog 
	{
		public MFontSelection(JFrame par)
		{
			super(par, true);
			setSize(300, 100);
			setLocation(200, 200);
			Container cont = getContentPane();
			cont.setLayout(new GridLayout(3, 2, 8, 3));
			
			// create UI objects
			JLabel fontLabel = new JLabel();
			m_fontCB = new JComboBox(StaticUtils.getFontNames());
			m_fontCB.setEditable(true);
			if (m_font.equals("") == false)
				m_fontCB.setSelectedItem(m_font);
			cont.add("font label", fontLabel);
			cont.add("font box", m_fontCB);

			String[] fontSizes = new String[] 
				{	"8",	"9",	"10",	"11",	
					"12",	"14",	"16",	"18" };
			JLabel fontSizeLabel = new JLabel();
			m_fontSizeCB = new JComboBox(fontSizes);
			m_fontSizeCB.setEditable(true);
			if (m_fontSize.equals("") == false)
				m_fontSizeCB.setSelectedItem(m_fontSize);
			cont.add("size label", fontSizeLabel);
			cont.add("size box", m_fontSizeCB);
			cont.add("spacer", new Container());
			
			Box buttonBox = Box.createHorizontalBox();
			JButton okButton = new JButton();
			JButton cancelButton = new JButton();
			buttonBox.add(Box.createHorizontalGlue());
			buttonBox.add(okButton);
			buttonBox.add(Box.createHorizontalStrut(5));
			buttonBox.add(cancelButton);
			cont.add(buttonBox);
	
			// add text
			okButton.setText(OStrings.PP_BUTTON_OK);
			cancelButton.setText(OStrings.PP_BUTTON_CANCEL);

			fontLabel.setText(OStrings.TF_SELECT_SOURCE_FONT);
			fontSizeLabel.setText(OStrings.TF_SELECT_FONTSIZE);

			setTitle(OStrings.TF_SELECT_FONTS_TITLE);

			// arrange action listeners
			okButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					doOK();
				}
			});

			cancelButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					doCancel();
				}
			});
		}

		private void doOK()
		{
			String str;
			CommandThread core = CommandThread.core;
			str = m_fontCB.getSelectedItem().toString();
			if (str.equals(m_font) == false)
			{
				m_isChanged = true;
				m_font = str;
				core.setPreference(OConsts.TF_SRC_FONT_NAME, m_font);
			}
			
			str = m_fontSizeCB.getSelectedItem().toString();
			if (str.equals(m_fontSize) == false)
			{
				m_isChanged = true;
				m_fontSize = str;
				core.setPreference(OConsts.TF_SRC_FONT_SIZE, m_fontSize);
			}
			
			dispose();
		}

		private void		doCancel()		{ dispose();			}
		protected boolean	isChanged()		{ return m_isChanged;	}
		
		protected JComboBox	m_fontCB;
		protected JComboBox	m_fontSizeCB;

		protected boolean	m_isChanged = false;
	}

	public void displayWarning(String msg, Throwable e)
	{
		setMessageText(msg);
		String str = OStrings.TF_WARNING;
		JOptionPane.showMessageDialog(this, msg + "\n" + e.toString(), 
					str, JOptionPane.WARNING_MESSAGE);
	}

	public void displayError(String msg, Throwable e)
	{
		setMessageText(msg);
		String str = OStrings.TF_ERROR;
		JOptionPane.showMessageDialog(this, msg + "\n" + e.toString(), 
					str, JOptionPane.ERROR_MESSAGE);
	}

	class XLPane extends JTextPane
	{
		public XLPane()
		{
			addMouseListener(new MouseAdapter()
			{
				public void mouseClicked(MouseEvent e)
				{
					// ignore mouse clicks until document is ready
					if (m_docReady == false)
						return;

					super.mouseClicked(e);
					if (e.getClickCount() == 2)
					{
						// user double clicked on view pane - goto entry
						// that was clicked
						int pos = getCaretPosition();
						DocumentSegment docSeg = null;
						int i;
						if (pos < m_segmentStartOffset)
						{
							// before current entry
							int offset = 0;
							for (i=m_xlFirstEntry; i<m_curEntryNum; i++)
							{
								docSeg = (DocumentSegment) 
										m_docSegList.get(i-m_xlFirstEntry);
								offset += docSeg.length;
								if (pos < offset)
								{
									doGotoEntry(i+1);
									return;
								}
							}
						}
						else if (pos > (m_xlPane.getText().length() -
											m_segmentEndInset))
						{
							// after current entry
							int inset = m_xlPane.getText().length() -
											m_segmentEndInset;
							for (i=m_curEntryNum+1; i<=m_xlLastEntry; i++)
							{
								docSeg = (DocumentSegment) 
										m_docSegList.get(i-m_xlFirstEntry);
								inset += docSeg.length;
								if (pos < inset)
								{
									doGotoEntry(i+1);
									return;
								}
							}
						}
					}
				}
			});
		}
		
		// monitor key events - need to prevent text insertion 
		//	outside of edit zone while maintaining normal functionality
		//	across jvm versions
		protected void processKeyEvent(KeyEvent e)
		{
			if (m_projectLoaded == false)
			{
				if ((e.getModifiers() == m_shortcutKey) || 
						(e.getModifiers() == InputEvent.ALT_MASK));
					super.processKeyEvent(e);
				return;
			}
			int keyCode = e.getKeyCode();
			char c = e.getKeyChar();

			// let released keys go straight to parent - they should 
			//	have no effect on UI and so don't need processing
			if (e.getID() == KeyEvent.KEY_RELEASED)
			{
				// let key releases pass through as well
				// no events should be happening here
				super.processKeyEvent(e);
				return;
			}

			// let control keypresses pass through unscathed
			if ((e.getID() == KeyEvent.KEY_PRESSED)	&&
					((keyCode == KeyEvent.VK_CONTROL)	||
					(keyCode == KeyEvent.VK_ALT)		||
					(keyCode == KeyEvent.VK_META)		||
					(keyCode == KeyEvent.VK_SHIFT)))
			{
				super.processKeyEvent(e);
				return;
			}

			// if we've made it here, we have a keypressed or 
			//	key-typed event of a (presumably) valid key
			//	and we're in an open project
			// it could still be a keyboard shortcut though

			// look for delete/backspace events and make sure they're
			//	in an acceptable area
			switch (c)
			{
				case 8:
					if (checkCaretForDelete(false) == true)
					{
						super.processKeyEvent(e);
					}
					return;
				case 127:
					// this check shouldn't be necessary, but
					//	make it in case the key handling changes to make
					//	backspace and delete work the same way
					if (checkCaretForDelete(true) == true)
					{
						super.processKeyEvent(e);
					}
					return;
			}

			// for now, force all key presses to reset the cursor to
			//	the editing region unless it's a ctrl-c (copy)
			if (((e.getID() == KeyEvent.KEY_PRESSED)		&& 
					(e.getModifiers() == m_shortcutKey)		&&
					((keyCode == 'c') || (keyCode == 'C')))
					||
					((e.getID() == KeyEvent.KEY_TYPED)		&&
					(e.getModifiers() == m_shortcutKey)		&&
					(c == 3)))
			{
				// control-c pressed or typed
				super.processKeyEvent(e);
				return;
			}

			// every other key press should be within the editing zone
			//	so make sure the caret is there
			checkCaret();

			// if user pressed enter, see what modifiers were pressed
			//	so determine what to do
			if (keyCode == KeyEvent.VK_ENTER)  
			{
				if (e.isShiftDown() == true)
				{
					// convert key event to straight enter key
					KeyEvent ke = new KeyEvent(e.getComponent(), e.getID(), 
							e.getWhen(), 0, KeyEvent.VK_ENTER, '\n');
					super.processKeyEvent(ke);
				}
				else if (e.isControlDown() == true)
				{
					// go backwards on control return
					if (e.getID() == KeyEvent.KEY_PRESSED)
						doPrevEntry();
				}
				else 
				{
					// return w/o modifiers - swallow event and move on to 
					//  next segment
					if (e.getID() == KeyEvent.KEY_PRESSED)
						doNextEntry();
				}
				return;
			}
			
			// need to over-ride default text hiliting procedures because
			//	we're managing caret placement manually
			if (e.isShiftDown() == true)
			{
				// if navigation control, make sure things are hilited
				if ((keyCode == KeyEvent.VK_UP)				|| 
						(keyCode == KeyEvent.VK_LEFT)		||
						(keyCode == KeyEvent.VK_KP_UP)		||
						(keyCode == KeyEvent.VK_KP_LEFT))
				{
					super.processKeyEvent(e);
					if (e.getID() == KeyEvent.KEY_PRESSED)
					{
						int pos = m_xlPane.getCaretPosition();
						int start = m_segmentStartOffset + 
									m_sourceDisplayLength +
									OStrings.TF_CUR_SEGMENT_START.length() + 1;
						if (pos < start)
							m_xlPane.moveCaretPosition(start);
					}
				}
				else if ((keyCode == KeyEvent.VK_DOWN)		|| 
						(keyCode == KeyEvent.VK_RIGHT)		||
						(keyCode == KeyEvent.VK_KP_DOWN)	||
						(keyCode == KeyEvent.VK_KP_RIGHT))
				{
					super.processKeyEvent(e);
					if (e.getID() == KeyEvent.KEY_PRESSED)
					{
						int pos = m_xlPane.getCaretPosition();
						// -1 for space before tag, -2 for newlines
						int end = m_xlPane.getText().length() -
								m_segmentEndInset-
								OStrings.TF_CUR_SEGMENT_END.length();
						if (pos > end)
							m_xlPane.moveCaretPosition(end);
					}
				}
				else
				{
					// a regular shift-key press
					// handle it normally
					super.processKeyEvent(e);
				}
				return;
			}

			// shift key is not down
			// if arrow key pressed, make sure caret moves to correct side
			//	of hilite (if text hilited)
			if ((keyCode == KeyEvent.VK_UP)				|| 
					(keyCode == KeyEvent.VK_LEFT)		||
					(keyCode == KeyEvent.VK_KP_UP)		||
					(keyCode == KeyEvent.VK_KP_LEFT))
			{
				int end = m_xlPane.getSelectionEnd();
				int start = m_xlPane.getSelectionStart();
				if (end != start)
					m_xlPane.setCaretPosition(start);
				else
					super.processKeyEvent(e);
				checkCaret();
				return;
			}
			else if ((keyCode == KeyEvent.VK_DOWN)		|| 
					(keyCode == KeyEvent.VK_RIGHT)		||
					(keyCode == KeyEvent.VK_KP_DOWN)	||
					(keyCode == KeyEvent.VK_KP_RIGHT))
			{
				int end = m_xlPane.getSelectionEnd();
				int start = m_xlPane.getSelectionStart();
				int pos = m_xlPane.getCaretPosition();
				if (end != start)
					m_xlPane.setCaretPosition(end);
				else 
					super.processKeyEvent(e);
				checkCaret();
				return;
			}

			// no shift and no arrow and caret is in correct place
			// no more special handling required
			super.processKeyEvent(e);
		}
	}

	// make sure there's one character in the direction indicated for
	//	delete operation
	// returns true if space is available
	protected boolean checkCaretForDelete(boolean forward)
	{
		int pos = m_xlPane.getCaretPosition();
		
		// make sure range doesn't overlap boundaries
		checkCaret();

		if (forward)
		{
			// make sure we're not at end of segment
			// -1 for space before tag, -2 for newlines
			int end = m_xlPane.getText().length() - m_segmentEndInset -
				OStrings.TF_CUR_SEGMENT_END.length();
			if (pos >= end)
				return false;
		}
		else
		{
			// make sure we're not at start of segment
			int start = m_segmentStartOffset + m_sourceDisplayLength +
				OStrings.TF_CUR_SEGMENT_START.length() + 1;
			if (pos <= start)
			{
				return false;
			}
		}
		return true;
	}
	
	// returns true if position reset, false otherwise
	protected boolean checkCaret()
	{
		//int pos = m_xlPane.getCaretPosition();
		int spos = m_xlPane.getSelectionStart();
		int epos = m_xlPane.getSelectionEnd();
		int start = m_segmentStartOffset + m_sourceDisplayLength +
			OStrings.TF_CUR_SEGMENT_START.length() + 1;
		// -1 for space before tag, -2 for newlines
		int end = m_xlPane.getText().length() - m_segmentEndInset -
			OStrings.TF_CUR_SEGMENT_END.length();
		boolean reset = false;
		
		if (spos != epos)
		{
			// dealing with a selection here - make sure it's w/in bounds
			if (spos < start)
			{
				reset = true;
				m_xlPane.setSelectionStart(start);
			}
			else if (spos > end)
			{
				reset = true;
				m_xlPane.setSelectionStart(end);
			}
			if (epos > end)
			{
				reset = true;
				m_xlPane.setSelectionEnd(end);
			}
			else if (epos < start)
			{
				reset = true;
				m_xlPane.setSelectionStart(start);
			}
		}
		else
		{
			// non selected text 
			if (spos < start)
			{
				m_xlPane.setCaretPosition(start);
				reset = true;
			}
			else if (spos > end)
			{
				m_xlPane.setCaretPosition(end);
				reset = true;
			}
		}
		
		return reset;
	}

	public void fatalError(String msg, RuntimeException re)
	{
		System.out.println(msg);
		if (re != null)
			re.printStackTrace();

		// try to shutdown gracefully
		CommandThread.core.signalStop();
		for (int i=0; i<25; i++)
		{
			while (CommandThread.core != null)
			{
				try { Thread.sleep(10); }
				catch (InterruptedException e) { ; }
			}
			if (CommandThread.core == null)
				break;
		}
		System.exit(1);
	}

	public boolean	isProjectLoaded()	{ return m_projectLoaded;		}
	
	private String m_strFuzzy;
	private String m_strGlossaryItem;
	private String m_strSrcText;
	private String m_strTranslation;
	private String m_strScore;
	private String m_strNone;

	private JMenu 		m_mFile;
	private JMenuItem	m_miFileOpen;
	private JMenuItem	m_miFileCreate;
	private JMenuItem	m_miFileCompile;
	private JMenuItem	m_miFileProjWin;
	private JMenuItem	m_miFileMatchWin;
	private JMenuItem	m_miFileSave;
	private JMenuItem	m_miFileQuit;
	private JMenu m_mEdit;
	private JMenuItem	m_miEditUndo;
	private JMenuItem	m_miEditRedo;
	private JMenuItem	m_miEditPrev;
	private JMenuItem	m_miEditUntrans;
//	private JMenuItem	m_miEditGoto;
	private JMenuItem	m_miEditFind;
	private JMenuItem	m_miEditRecycle;
	private JMenuItem	m_miEditInsert;
	private JMenuItem	m_miEditCompare1;
	private JMenuItem	m_miEditCompare2;
	private JMenuItem	m_miEditCompare3;
	private JMenuItem	m_miEditCompare4;
	private JMenuItem	m_miEditCompare5;
	private JMenuItem	m_miDisplayFont;
	private JMenu		m_mDisplay;
	
	private JMenu		m_mTools;
	private JMenuItem	m_miToolsSpell;
	private JMenuItem	m_miToolsPseudoTrans;
	private JMenuItem	m_miToolsValidateTags;
	private JMenuItem	m_miToolsMergeTMX;

	private JMenu		m_mVersion;
	private JMenuItem	m_miVersionHelp;
//	private JMenuItem	m_miVersionNumber;

	// source and target font display info
	protected String	m_font;
	protected String	m_fontSize;

	// first and last entry numbers in current file
	protected int		m_xlFirstEntry;
	protected int		m_xlLastEntry;

	// starting offset and length of source lang in current segment
	protected int		m_segmentStartOffset = 0;
	protected int		m_sourceDisplayLength = 0;
	protected int		m_segmentEndInset = 0;
	// text length of glossary, if displayed
	protected int		m_glossaryLength = 0;

	// boolean set after safety check that OStrings.TF_CUR_SEGMENT_START
	//	contains empty "0000" for segment number
	protected boolean	m_segmentTagHasNumber = false;

	// indicates the document is loaded and ready for processing
	protected boolean	m_docReady = false;

	// indicates the boundary of editable text in xlPane (char offsets)
	protected int		m_lowTextLock = 0;
	protected int		m_highTextLock = 0;

	// list of text segments in current doc
	protected ArrayList	m_docSegList;

	// make a local copy of this instead of fetching it each time
	protected int	m_shortcutKey;
	protected UndoManager	m_undo = null;

	private XLPane		m_xlPane;
	private JScrollPane	m_xlScroller;

	private LinkedList	m_nearList;
	private int		m_nearListNum;

	private JLabel		m_statusLabel;
	// TODO fuzzy match and project info in status label
	//private JLabel		m_fuzzyProjLabel;
	protected SourceTextEntry		m_curEntry;

	private String	m_activeFile;
	private String	m_activeProj;
	private int m_curEntryNum;
	private NearString m_curNear;
	private int m_numEntries;
	protected String	m_curTrans = "";

	private ProjectFrame	m_projWin = null;
	protected MatchWindow	m_matchViewer = null;

	private boolean m_projectLoaded = false;
	
	public static final String RED = "FF0000";
	public static final String BLUE = "000099";
	public static final String GREEN = "009900";

	public static final int LOADING_INDEX		= 1;
	public static final int LOADING_GLOSSARY	= 2;
	public static final int LOADING_FUZZY		= 3;
	public static final int COMPUTING_FUZZY		= 4;
	public static final int LOAD_COMPLETE		= 5;

	private int	m_leftX = 350;

///////////////////////////////////////////////////////////////

	public static void main(String[] args)
	{
		JFrame f = new TransFrame();
		f.show();
	}
}

