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
//  Build date:  16Sep2003
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

		m_docSegList = new ArrayList();

		createMenus();
		createUI();
		
		////////////////////////////////
		updateUIText();

		enableEvents(0);

                GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
                Rectangle scrSize = env.getMaximumWindowBounds();
		setSize( scrSize.width, scrSize.height );
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
		m_srcFont = core.getOrSetPreference(OConsts.TF_SRC_FONT_NAME,
				OConsts.TF_FONT_DEFAULT);
		m_locFont = core.getOrSetPreference(OConsts.TF_LOC_FONT_NAME,
				OConsts.TF_FONT_DEFAULT);
		m_srcFontSize = core.getOrSetPreference(OConsts.TF_SRC_FONT_SIZE,
				OConsts.TF_FONT_SIZE_DEFAULT);
		m_locFontSize = core.getOrSetPreference(OConsts.TF_LOC_FONT_SIZE,
				OConsts.TF_FONT_SIZE_DEFAULT);

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

		// create 'old' src html field
		m_oldSrcPane = new JTextPane();
		JScrollPane oldSrcScroller = new JScrollPane(m_oldSrcPane);
		m_oldSrcDoc = new DefaultStyledDocument(new StyleContext());
		m_oldSrcPane.setDocument(m_oldSrcDoc);
///		m_oldSrcPane.setEditable(false);
//		m_oldSrcPane.setVisible(false);
		oldSrcScroller.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

		// create 'old' loc html field
		m_oldLocPane = new JTextPane();
		JScrollPane oldLocScroller = new JScrollPane(m_oldLocPane);
		m_oldLocDoc = new DefaultStyledDocument(new StyleContext());
		m_oldLocPane.setDocument(m_oldLocDoc);
//		m_oldLocPane.setEditable(false);
//		m_oldLocPane.setVisible(false);
		oldLocScroller.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

		// create translation edit field
		//m_xlPane = new JTextPane();
		m_xlPane = new XLPane();
		m_xlScroller = new JScrollPane(m_xlPane);
		m_xlDoc = new DefaultStyledDocument(new StyleContext());
		m_xlPane.setDocument(m_xlDoc);
		m_xlScroller.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		
		m_statusLabel = new JLabel();

		/////////////////////////////////////////
		// create stat container
		// 4 rows are goto entry, word count, find label, find field
		Container oldContainer = new Container();
		oldContainer.setLayout(new GridLayout(1,2));
		oldContainer.add(oldSrcScroller);
		oldContainer.add(oldLocScroller);

		getContentPane().add(m_statusLabel, BorderLayout.NORTH);
		getContentPane().add(m_xlScroller, BorderLayout.CENTER);
		getContentPane().add(oldContainer, BorderLayout.SOUTH);

		m_projWin = new ProjectFrame(this);

		try 
		{
			m_xlDoc.insertString(0, OStrings.TF_INTRO_MESSAGE, null);
		}
		catch (BadLocationException ble)
		{
			;	// let it go
		}
	}

	protected void createMenus()
	{
		////////////////////////////////
		// create menus
		JMenuBar mb = new JMenuBar();
		// file
		m_mFile = new JMenu();
		m_miFileOpen = new JMenuItem();
		m_miFileOpen.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_O, m_shortcutKey));
		m_miFileOpen.addActionListener(this);
		m_mFile.add(m_miFileOpen);

		m_miFileCreate = new JMenuItem();
		m_miFileCreate.addActionListener(this);
		m_mFile.add(m_miFileCreate);

		m_miFileCompile = new JMenuItem();
		m_miFileCompile.addActionListener(this);
		m_mFile.add(m_miFileCompile);
		
		m_mFile.addSeparator();

		m_miFileProjWin = new JMenuItem();
		m_miFileProjWin.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_L,  m_shortcutKey));
		m_miFileProjWin.addActionListener(this);
		m_mFile.add(m_miFileProjWin);
		
		m_mFile.addSeparator();

		m_miFileSave = new JMenuItem();
		m_miFileSave.addActionListener(this);
		m_miFileSave.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_S, m_shortcutKey));
		m_mFile.add(m_miFileSave);

		m_miFileQuit = new JMenuItem();
		m_miFileQuit.addActionListener(this);
		m_mFile.addSeparator();
		m_miFileQuit.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_Q,  m_shortcutKey));
		m_mFile.add(m_miFileQuit);
		mb.add(m_mFile);

		// edit
		m_mEdit = new JMenu();
		m_miEditNext = new JMenuItem();
		m_miEditNext.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_N,  m_shortcutKey));
		m_miEditNext.addActionListener(this);
		m_mEdit.add(m_miEditNext);

		m_miEditPrev = new JMenuItem();
		m_miEditPrev.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_P,  m_shortcutKey));
		m_miEditPrev.addActionListener(this);
		m_mEdit.add(m_miEditPrev);

		m_mEdit.addSeparator();

		m_miEditRecycle = new JMenuItem();
		m_miEditRecycle.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_R,  m_shortcutKey));
		m_miEditRecycle.addActionListener(this);
		m_mEdit.add(m_miEditRecycle);

		m_miEditInsert = new JMenuItem();
		m_miEditInsert.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_I,  m_shortcutKey));
		m_miEditInsert.addActionListener(this);
		m_mEdit.add(m_miEditInsert);

		m_mEdit.addSeparator();

		m_miEditFind = new JMenuItem();
		m_miEditFind.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_F, m_shortcutKey));
		m_miEditFind.addActionListener(this);
		m_mEdit.add(m_miEditFind);

//		m_miEditFindLoc = new JMenuItem();
//		m_miEditFindLoc.setAccelerator(KeyStroke.getKeyStroke(
//				KeyEvent.VK_E,  m_shortcutKey));
//		m_miEditFindLoc.addActionListener(this);
//		m_mEdit.add(m_miEditFindLoc);
//
//		m_miEditGoto = new JMenuItem();
//		m_miEditGoto.setAccelerator(KeyStroke.getKeyStroke(
//				KeyEvent.VK_G,  m_shortcutKey));
//		m_miEditGoto.addActionListener(this);
//		m_mEdit.add(m_miEditGoto);
//		
//		m_mEdit.addSeparator();

		m_miEditCompare1 = new JMenuItem();
		m_miEditCompare1.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_1,  m_shortcutKey));
		m_miEditCompare1.addActionListener(this);
		m_mEdit.add(m_miEditCompare1);

		m_miEditCompare2 = new JMenuItem();
		m_miEditCompare2.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_2,  m_shortcutKey));
		m_miEditCompare2.addActionListener(this);
		m_mEdit.add(m_miEditCompare2);

		m_miEditCompare3 = new JMenuItem();
		m_miEditCompare3.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_3,  m_shortcutKey));
		m_miEditCompare3.addActionListener(this);
		m_mEdit.add(m_miEditCompare3);

		m_miEditCompare4 = new JMenuItem();
		m_miEditCompare4.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_4,  m_shortcutKey));
		m_miEditCompare4.addActionListener(this);
		m_mEdit.add(m_miEditCompare4);

		m_miEditCompare5 = new JMenuItem();
		m_miEditCompare5.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_5,  m_shortcutKey));
		m_miEditCompare5.addActionListener(this);
		m_mEdit.add(m_miEditCompare5);

		mb.add(m_mEdit);

		// display
		m_mDisplay = new JMenu();
		m_miDisplayFuzzyMatch = new JCheckBoxMenuItem();
		m_miDisplayFuzzyMatch.addActionListener(this);
		m_miDisplayFuzzyMatch.setState(true);
		m_mDisplay.add(m_miDisplayFuzzyMatch);

		m_miDisplayGlossaryMatch = new JCheckBoxMenuItem();
		m_miDisplayGlossaryMatch.addActionListener(this);
		m_miDisplayGlossaryMatch.setState(false);
		m_miDisplayGlossaryMatch.setEnabled(false);
		m_mDisplay.add(m_miDisplayGlossaryMatch);

		m_miDisplayFont = new JMenuItem();
		m_miDisplayFont.addActionListener(this);
		m_mDisplay.add(m_miDisplayFont);

		mb.add(m_mDisplay);
		
		// tools
		m_mTools = new JMenu();
		m_miToolsPseudoTrans = new JMenuItem();
		m_miToolsPseudoTrans.addActionListener(this);
		m_mTools.add(m_miToolsPseudoTrans);

		m_miToolsValidateTags = new JMenuItem();
		m_miToolsValidateTags.addActionListener(this);
		m_mTools.add(m_miToolsValidateTags);

		m_miToolsMergeTMX = new JMenuItem();
		m_miToolsMergeTMX.addActionListener(this);
		m_mTools.add(m_miToolsMergeTMX);

		mb.add(m_mTools);
		
		m_mVersion = new JMenu();
		m_miVersionHelp = new JMenuItem();
		m_miVersionHelp.addActionListener(this);
		m_mVersion.add(m_miVersionHelp);

		mb.add(m_mVersion);

		setJMenuBar(mb);
	}

	public void updateUIText()
	{
		String str;

		doSetTitle();

		m_strFuzzy = OStrings.TF_FUZZY;
		m_strGlossaryItem = OStrings.TF_GLOSSARY;
		m_strSrcText = OStrings.TF_SRCTEXT;
		m_strTranslation = OStrings.TF_TRANSLATION;
		m_strScore = OStrings.TF_SCORE;
		m_strNone = OStrings.TF_NONE;

//		m_findLabel.setText(OStrings.TF_SEARCH);
//		m_findExactLabel.setText(OStrings.TF_SEARCH_EXACT);
//		int blockHeight = m_findLabel.getPreferredSize().height;
//		m_statusLabel.setMinimumSize(new Dimension(m_leftX, blockHeight));
//		m_curString = OStrings.TF_CUR_STRING;
//		str = OStrings.TF_GOTO_ENTRY;

		m_mFile.setText(OStrings.TF_MENU_FILE);
		m_miFileOpen.setText(OStrings.TF_MENU_FILE_OPEN);
		m_miFileCreate.setText(OStrings.TF_MENU_FILE_CREATE);
		m_miFileCompile.setText(OStrings.TF_MENU_FILE_COMPILE);
		m_miFileProjWin.setText(OStrings.TF_MENU_FILE_PROJWIN);
		m_miFileSave.setText(OStrings.TF_MENU_FILE_SAVE);
		m_miFileQuit.setText(OStrings.TF_MENU_FILE_QUIT);

		m_mEdit.setText(OStrings.TF_MENU_EDIT);
		m_miEditNext.setText(OStrings.TF_MENU_EDIT_NEXT);
		m_miEditPrev.setText(OStrings.TF_MENU_EDIT_PREV);
		m_miEditCompare1.setText(OStrings.TF_MENU_EDIT_COMPARE_1);
		m_miEditCompare2.setText(OStrings.TF_MENU_EDIT_COMPARE_2);
		m_miEditCompare3.setText(OStrings.TF_MENU_EDIT_COMPARE_3);
		m_miEditCompare4.setText(OStrings.TF_MENU_EDIT_COMPARE_4);
		m_miEditCompare5.setText(OStrings.TF_MENU_EDIT_COMPARE_5);
//		m_miEditGoto.setText(OStrings.TF_MENU_EDIT_GOTO);
		m_miEditRecycle.setText(OStrings.TF_MENU_EDIT_RECYCLE);
		m_miEditInsert.setText(OStrings.TF_MENU_EDIT_RECYCLE);
		m_miEditFind.setText(OStrings.TF_MENU_EDIT_FIND);
		
		m_mDisplay.setText(OStrings.TF_MENU_DISPLAY);
		m_miDisplayFuzzyMatch.setText(OStrings.TF_MENU_DISPLAY_FUZZY);
		m_miDisplayGlossaryMatch.setText(OStrings.TF_MENU_DISPLAY_GLOSSARY);
		m_miDisplayFont.setText(OStrings.TF_MENU_DISPLAY_FONT);

		m_mTools.setText(OStrings.TF_MENU_TOOLS);
		m_miToolsPseudoTrans.setText(OStrings.TF_MENU_TOOLS_PSEUDO);
		m_miToolsValidateTags.setText(OStrings.TF_MENU_TOOLS_VALIDATE);
		m_miToolsMergeTMX.setText(OStrings.TF_MENU_TOOLS_MERGE_TMX);

		m_mVersion.setText(OmegaTVersion.name());
		m_miVersionHelp.setText(OStrings.TF_MENU_VERSION_HELP);
	}

	protected void updateMenuSelectabilityStates()
	{
		// if not in project, disable most of menu items
		// otherwise, enable everything 
		// TODO XXX update menu selectability states
	}

	///////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////
	// command handling
	
	protected void doQuit()
	{
		// shutdown
		doSave();
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

	public void doNextEntry()
	{
		if (m_projectLoaded == false)
			return;
		
		checkEntry();

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
		
		checkEntry();

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
		try 
		{
			if (s != null)
			{
				int fontsize = 12;
				try 
				{
					fontsize = Integer.valueOf(m_locFontSize).intValue();
				}
				catch (NumberFormatException nfe)
				{
					fontsize = 12;
				}
				int pos = m_xlPane.getCaretPosition();
				MutableAttributeSet mattr = null;
				mattr = new SimpleAttributeSet();
				StyleConstants.setFontFamily(mattr, m_locFont);
				StyleConstants.setFontSize(mattr, fontsize);
				m_xlDoc.insertString(pos, s, mattr);
			}
		}
		catch (BadLocationException e)
		{
			// something's wrong - print error 
			// this should never happen, but if it does let it go as
			//	no immediate harm is done.  maybe app will crash
			//	in a critical section, or maybe nothing'll happen
			System.out.println("INTERNAL ERROR - doInsertTrans: bad location");
		}
	}

	// replace entire edit area with active fuzzy match
	public void doRecycleTrans()
	{
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

		try 
		{
			if (text != null)
			{
				// remove current text
				// build local offsets
				int start = m_segmentStartOffset + m_segmentDisplayLength +
					OStrings.TF_CUR_SEGMENT_START.length() + 1;
				int end = m_xlDoc.getLength() - m_segmentEndInset -
					OStrings.TF_CUR_SEGMENT_END.length() - 3;

				// remove text
//System.out.println("removing text "+start+" -> "+end+" length:"+(end-start));
				m_xlDoc.remove(start, end-start);

				// insert fuzzy match
				int fontsize = 12;
				try 
				{
					fontsize = Integer.valueOf(m_locFontSize).intValue();
				}
				catch (NumberFormatException nfe)
				{
					fontsize = 12;
				}
				MutableAttributeSet mattr = null;
				mattr = new SimpleAttributeSet();
				StyleConstants.setFontFamily(mattr, m_locFont);
				StyleConstants.setFontSize(mattr, fontsize);
				m_xlDoc.insertString(start, text, mattr);
			}
		}
		catch (BadLocationException e)
		{
			// something's wrong - print error 
			// this should never happen, but if it does let it go as
			//	no immediate harm is done.  maybe app will crash
			//	in a critical section, or maybe nothing'll happen
			System.out.println("INTERNAL ERROR - doInsertTrans: bad location");
		}
	}

	protected void doCompareN(int n)
	{
		if (m_projectLoaded == false)
			return;
		
		// first clean out old display value
		try
		{
			int len = m_oldSrcDoc.getLength();
			if (len > 0)
				m_oldSrcDoc.remove(0, len);
			len = m_oldLocDoc.getLength();
			if (len > 0)
				m_oldLocDoc.remove(0, len);
		}
		catch (BadLocationException e)
		{
			// shouldn't happen - swallow it for now
			System.out.println("Unexpected bad location exception " +
					"encountered reading old source/target field.  " + 
					"Ignoring it.");
		}
		
		StringEntry se = m_curEntry.getStrEntry();
		if (n < se.getNearList().size())
			m_nearListNum = n;
		updateFuzzyInfo();
	}

	public void doUnloadProject()
	{
		m_projectLoaded = false;
		try
		{
			m_xlDoc.remove(0, m_xlDoc.getLength());
		}
		catch (BadLocationException e)	{ ; }	// ignore	
	}

	// display dialog allowing selection of source and target language fonts
	protected void doFont()
	{
		MFontSelection dlg = new MFontSelection(this);
		dlg.show();
		if (dlg.isChanged())
		{
			// fonts have changed - reload the document
			// first commit current translation
			checkEntry();
			loadDocument();
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
		m_oldSrcPane.setText("");
		m_oldLocPane.setText("");
		m_xlPane.setText("");
		
		RequestPacket load;
		load = new RequestPacket(RequestPacket.LOAD, this);
		CommandThread.core.messageBoardPost(load);
	}

	protected void doGotoEntry(int entryNum)
	{
		if (m_projectLoaded == false)
			return;
		
		checkEntry();

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
		if (m_defaultCursor == null)
			m_defaultCursor = m_xlPane.getCursor();

		m_xlPane.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		try
		{
			// clear old text
			m_xlDoc.remove(0, m_xlDoc.getLength());
			m_docSegList.clear();
			m_curEntry = CommandThread.core.getSTE(m_curEntryNum);
			setMessageText(OStrings.TF_LOADING_FILE + 
									m_curEntry.getSrcFile().name);
			m_xlFirstEntry = m_curEntry.getFirstInFile();
			m_xlLastEntry = m_curEntry.getLastInFile();

			// offset and len are char counts for the entry point
			//	and length of the current displayed text segment
			int offset = 0;
			int len = 0;
			int fontsize = 12;
			String text;
			MutableAttributeSet attr = null;
			DocumentSegment docSeg;
			
			for (int i=m_xlFirstEntry; i<=m_xlLastEntry; i++)
			{
				docSeg = new DocumentSegment();
				
				len = insertSegment(i, offset, false, false);
				offset += len;
				
				// keep track of segment size and location
//				docSeg.transOffset = offset;
				docSeg.length = len;
				m_docSegList.add(docSeg);
			}
//
//			activateEntry();
		}
		catch (BadLocationException e)
		{
System.out.println("LOAD DOCUMENT EXCEPTION");
		}
		setMessageText("");
	}

	private void checkEntry()
	{
//System.out.println("\nchecking entry ("+m_xlDoc.getLength()+")");
		try
		{
			// see if previous entry changed - if so, update core
			int start = m_segmentStartOffset + m_segmentDisplayLength + 
					OStrings.TF_CUR_SEGMENT_START.length() + 1;
			// -1 for space between tag and text, -2 for newlines 
			int end = m_xlDoc.getLength() - m_segmentEndInset - 
					OStrings.TF_CUR_SEGMENT_END.length() - 3;
			String s = m_xlDoc.getText(start, end-start);
//System.out.println("recovered translated text: '"+s+"'");
			
			// convert hard return to soft
			s = s.replace((char) 0x0a, (char) 0x8d);
			
			// only record translation if the field is not blank
			if ((s.equals(m_curTrans) == false) && (s.equals("") == false))
			{
				// translation has changed - do some housekeeping
				m_curEntry.setTranslation(s);
			}
			
			// deactivate entry now before updating identical segments
			//	in display - starting offset and end inset might change
			deactivateEntry();

			// update the length parameters of all changed segments
			// update strings in display
			if ((s.equals(m_curTrans) == false) && (s.equals("") == false))
			{
				SourceTextEntry ste = CommandThread.core.getSTE(m_curEntryNum);
				StringEntry se = ste.getStrEntry();
				ListIterator it = se.getParentList().listIterator();
				int entry;
				int offset;
				int i;
				DocumentSegment docSeg;
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
						// current entry is handled in deactivateEntry
						if (entry == m_curEntryNum)
							continue;

						// build offset
						for (i=m_xlFirstEntry; i<entry; i++)
						{
							docSeg = (DocumentSegment) m_docSegList.get(
									i-m_xlFirstEntry);
							offset += docSeg.length;
						}
						// extract old text
						docSeg = (DocumentSegment) m_docSegList.get(
									entry - m_xlFirstEntry);
						m_xlDoc.remove(offset, docSeg.length);
						// insert new
						docSeg.length = s.length() + 2;
						insertSegment(entry, offset, false, false);
					}
				}
			}
		}
		catch (BadLocationException e)
		{
			String msg = OStrings.TF_BAD_LOCATION_POSSIBLE_CORRUPTION;
			System.out.println(msg + "\nfunction: checkEntry");
			fatalError(msg, null);
		}
	}

	///////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////
	// display oriented code
	protected void updateMatchPane()
	{
//System.out.println("display fuzzy match summary + handle glossary terms");
	}
	
	// display fuzzy matching info if it's available
	// don't call this directly - should only be called through doCompareN
	protected void updateFuzzyInfo() 
	{
		if (m_projectLoaded == false)
			return;

//System.out.println("updating fuzzy info - '"+m_curEntry.getSrcText()+"'");

		try
		{
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

//				m_oldSrcPane.setVisible(false);
//				m_oldLocPane.setVisible(false);
				return;
			}

			String srcText = m_curEntry.getSrcText();
			formatNearText(srcText, m_curNear.parAttr, Color.red, 
					Color.darkGray, m_xlDoc, m_segmentStartOffset, 
					srcText.length());
			
			String oldStr = m_curNear.str.getSrcText();
//System.out.println("old src text: "+oldStr);

			// remember length of base string (before fuzzy % added)
			int oldStrLen = oldStr.length();
//		String proj = m_curNear.proj;

			// set appropriate font size
			int fontsize = 12;
			try 
			{
				fontsize = Integer.valueOf(m_srcFontSize).intValue();
			}
			catch (NumberFormatException nfe)
			{
				fontsize = 12;
			}

			MutableAttributeSet mattr = null;
			mattr = new SimpleAttributeSet();
			StyleConstants.setFontFamily(mattr, m_srcFont);
			StyleConstants.setFontSize(mattr, fontsize);
			m_oldSrcDoc.insertString(0, oldStr, mattr);
			formatNearText(oldStr, m_curNear.attr, Color.blue, Color.black, 
					m_oldSrcDoc, 0, oldStrLen);
			Double sc = new Double(m_curNear.score * 100);
			String nearStr = "\n\n" + sc.intValue() + "%";
			StyleConstants.setForeground(mattr, Color.black);
			m_oldSrcDoc.insertString(m_oldSrcDoc.getLength(), nearStr, mattr);

			try 
			{
				fontsize = Integer.valueOf(m_locFontSize).intValue();
			}
			catch (NumberFormatException nfe)
			{
				fontsize = 12;
			}
			oldStr = m_curNear.str.getTrans();
//System.out.println("old translation: "+oldStr);
			mattr = new SimpleAttributeSet();
			StyleConstants.setFontFamily(mattr, m_locFont);
			StyleConstants.setFontSize(mattr, fontsize);
			m_oldLocDoc.insertString(0, oldStr, mattr);

			m_oldSrcPane.setCaretPosition(0);
			m_oldLocPane.setCaretPosition(0);

			// TODO XXX say where this fuzzy string came from
	//		if (proj == null)
	//			proj = OStrings.TF_FUZZY_CURRENT_PROJECT;
	//		m_fuzzyProjLabel.setText(proj);
		}
		catch (BadLocationException e)
		{
System.out.println("BAD LOCATION EXCEPTION UPDATE FUZZY");
		}
	}
	
	// if useSource == true, displays source text
	// otherwise displays trans if available, src if not
	// 'hotSegment' means that this is the text that's put up for edit
	protected int insertSegment(int entryNum, int offset, boolean useSource,
			boolean hotSegment)
		throws BadLocationException
	{
		int len = 0;
		m_curTrans = "";
//System.out.println("inserting segment "+entryNum+" at offset "+offset);
		
		SourceTextEntry ste = CommandThread.core.getSTE(entryNum);
		MutableAttributeSet attr = new SimpleAttributeSet();
		
		String fontSizeStr = "";
		int fontsize = 12;
		String text = ste.getTranslation();
		// set text and font
		if ((text.length() == 0) || (useSource == true))
		{
			// no translation available - display source text
			text = ste.getSrcText();
			if (hotSegment == true)
			{
				// this is a to-be-translated segment, use the target font
				fontSizeStr = m_locFontSize;
				StyleConstants.setFontFamily(attr, m_locFont);
				StyleConstants.setForeground(attr, Color.darkGray);
			}
			else
			{
				fontSizeStr = m_srcFontSize;
				StyleConstants.setFontFamily(attr, m_srcFont);
			}
		}
		else
		{
			fontSizeStr = m_locFontSize;
			StyleConstants.setFontFamily(attr, m_locFont);
			StyleConstants.setForeground(attr, Color.darkGray);
		}
		
		// convert hard soft return to hard for display
		text = text.replace((char) 0x8d, (char) 0x0a);
			

		// set appropriate font size
		try 
		{
			fontsize = Integer.valueOf(fontSizeStr).intValue();
		}
		catch (NumberFormatException nfe)
		{
			fontsize = 12;
		}

		if (hotSegment == true)
		{
			// add segment tags to start and end of display text
			String start = OStrings.TF_CUR_SEGMENT_START;
			String end = OStrings.TF_CUR_SEGMENT_END;
			if (m_segmentTagHasNumber)
			{
				// put entry number in first tag
				int num = m_curEntryNum + 1;
				int ones;
				// do it digit by digit - there's a better way (like sprintf)
				//	but I don't recall the Java method presently
				String disp = "";
				while (num > 0)
				{
					ones = num % 10;
					num /= 10;
					disp = ((int) ones) + disp;
				}
				int zero = start.lastIndexOf('0');
				start = start.substring(0, zero-disp.length()+1) + 
					disp + start.substring(zero+1);
			}
			// insert text into document
			// make sure segment tags display in source font
			MutableAttributeSet segAttr = new SimpleAttributeSet();
			StyleConstants.setFontFamily(segAttr, m_srcFont);
			StyleConstants.setFontSize(segAttr, fontsize);
			StyleConstants.setBold(segAttr, true);

			StyleConstants.setFontSize(attr, fontsize);
			m_xlDoc.insertString(offset, start, segAttr);
			offset += start.length();
			m_xlDoc.insertString(offset, " " + text + " ", attr);
			offset += text.length() + 2;
			m_xlDoc.insertString(offset, end, segAttr);
			offset += end.length();
			len = start.length() + text.length() + end.length();
			m_curTrans = text;
		}
		else
		{
			// insert text into document
			StyleConstants.setFontSize(attr, fontsize);
			m_xlDoc.insertString(offset, text, attr);
			offset += text.length();
			len = text.length();
			m_segmentDisplayLength = len + 2;
		}

		// add a small space between segments for readibility
		attr = new SimpleAttributeSet();
		text = "\n\n";
		StyleConstants.setFontFamily(attr, "Monospaced");
		StyleConstants.setFontSize(attr, 6);
		m_xlDoc.insertString(offset, text, attr);

		len += text.length();

		return len;
	}
	
	// unhilite current entry; remove redundant text
	public synchronized void deactivateEntry()
		throws BadLocationException
	{
//System.out.println("deactivate entry  offset:"+m_segmentStartOffset+" doclen:"+m_xlDoc.getLength()+" dispLen:"+m_segmentDisplayLength+" inset:"+m_segmentEndInset+" segend:"+(m_xlDoc.getLength()-m_segmentEndInset));
		// first cut out old text
		m_xlDoc.remove(m_segmentStartOffset, 
				m_xlDoc.getLength() - m_segmentEndInset - m_segmentStartOffset);

		// insert translated text (if available) otherwise insert source
		int len = insertSegment(m_curEntryNum, m_segmentStartOffset, 
					false, false);

		DocumentSegment docSeg = (DocumentSegment) m_docSegList.get(
				m_curEntryNum-m_xlFirstEntry);
		docSeg.length = len;
	}

	// move document focus to current entry
	// make sure fuzzy info displayed if available and wanted
	public synchronized void activateEntry() 
	{
//System.out.println("activate entry");
		int i;
		DocumentSegment docSeg;

		// recover data about current entry
		m_curEntry = CommandThread.core.getSTE(m_curEntryNum);
		String srcText = m_curEntry.getSrcText();
		String trans = m_curEntry.getTranslation();;
		
		// sum up total character offset to current segment start
		m_segmentStartOffset = 0;
		for (i=m_xlFirstEntry; i<m_curEntryNum; i++)
		{
			docSeg = (DocumentSegment) m_docSegList.get(i-m_xlFirstEntry);
			m_segmentStartOffset += docSeg.length;
		}

		// remove display text; replace w/ source
		docSeg = (DocumentSegment) m_docSegList.get(m_curEntryNum - 
					m_xlFirstEntry);
		try 
		{
			m_xlDoc.remove(m_segmentStartOffset, docSeg.length);
			// display source text
			m_segmentDisplayLength = insertSegment(m_curEntryNum, 
						m_segmentStartOffset, true, false);
			m_segmentEndInset = m_segmentStartOffset + m_segmentDisplayLength;
			m_segmentEndInset += insertSegment(m_curEntryNum, 
						m_segmentEndInset, false, true);
			// -2 to take into account double newlines at end of segment
			m_segmentEndInset = m_xlDoc.getLength() - m_segmentEndInset - 2;

		}
		catch (BadLocationException e)
		{
			// this is bad - for some reason we've got out of sync
			//	w/ the UI
			// possible corruption can occur if we continue, so 
			//	bail out completely to be safe
			displayError(OStrings.TF_BAD_LOCATION_POSSIBLE_CORRUPTION, e);
			fatalError(OStrings.TF_BAD_LOCATION_POSSIBLE_CORRUPTION, null);
		}

		
		// TODO XXX format source text if there is near match
		
//		m_fuzzyProjLabel.setText("");
		if (m_curEntry.getSrcFile().name.compareTo(m_activeFile) != 0)
		{
			m_activeFile = m_curEntry.getSrcFile().name;
			doSetTitle();
		}
		
		// TODO set word counts
		
		// fill context fields.  make text to be translated red so
		//  to stand out in low context field

		doCompareN(0);

		StringEntry se = m_curEntry.getStrEntry();
		if (se.getGlosList().size() > 0)
		{
			// TODO do something with glossary terms
			m_glossaryLength = 0;

			// make glossary entries bold
		}
		else
			m_glossaryLength = 0;

		int nearLength = se.getNearList().size();
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
		
		// with glossary information displayed, we can finally set
		//	the end inset
		m_segmentEndInset += m_glossaryLength;
//System.out.println("activate entry  offset:"+m_segmentStartOffset+" doclen:"+m_xlDoc.getLength()+" dispLen:"+m_segmentDisplayLength+" inset:"+m_segmentEndInset+" segend:"+(m_xlDoc.getLength()-m_segmentEndInset));
//System.out.println(" segment text '"+m_curEntry.getSrcText()+"' -> '"+m_curEntry.getTranslation()+"'");

		// hilite translation area in yellow
		MutableAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setBackground(attr, Color.yellow);
		m_xlDoc.setCharacterAttributes(m_segmentStartOffset, 
				m_xlDoc.getLength() - m_segmentEndInset - 
				1 - m_segmentStartOffset, attr, false);

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
		m_xlPane.setCaretPosition(m_xlDoc.getLength() - m_segmentEndInset 
				+ padding);
//System.out.println("seg: ("+m_segmentStartOffset+", "+(m_xlDoc.getLength()-m_segmentEndInset)+")  setting "+(m_xlDoc.getLength() - m_segmentEndInset + padding));

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
//		m_xlPane.setCaretPosition(m_xlDoc.getLength() + nextLength -
//		m_xlPane.setCaretPosition(m_xlDoc.getLength() -
//					m_segmentEndInset - 
//					OStrings.TF_CUR_SEGMENT_END.length() - 3);
//		checkCaret();
		if (m_docReady == false)
		{
			m_xlPane.setCursor(m_defaultCursor);
			m_docReady = true;
		}
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

		MutableAttributeSet mattr = null;

		// reset color of text to default value
		mattr = new SimpleAttributeSet();
		StyleConstants.setForeground(mattr, textColor);
		doc.setCharacterAttributes(startOffset, length, mattr, false);

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

			mattr = new SimpleAttributeSet();
			if ((attrList[i] & StringData.UNIQ) != 0)
			{
//System.out.println("    uniq");
				StyleConstants.setForeground(mattr, uniqColor);
			}
			else if ((attrList[i] & StringData.PAIR) != 0)
			{
//System.out.println("    near");
				StyleConstants.setForeground(mattr, Color.green);
			}
			doc.setCharacterAttributes(start, end-start, mattr, false);
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
			else if (evtSrc == m_miFileProjWin)
			{
				if (m_projWin != null)
				{
					m_projWin.show();
					m_projWin.toFront();
				}
			}
			else if (evtSrc == m_miEditNext)
			{
				doNextEntry();
			}
			else if (evtSrc == m_miEditPrev)
			{
				doPrevEntry();
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
			setSize(500, 240);

			String[] fontSizes = new String[] 
				{	"8",	"9",	"10",	"11",	
					"12",	"14",	"16",	"18" };
			
			// create UI objects
			JLabel srcFontLabel = new JLabel();
			m_srcFontCB = new JComboBox(StaticUtils.getFontNames());
			m_srcFontCB.setEditable(true);
			if (m_srcFont.equals("") == false)
				m_srcFontCB.setSelectedItem(m_srcFont);

			JLabel srcFontSizeLabel = new JLabel();
			m_srcFontSizeCB = new JComboBox(fontSizes);
			m_srcFontSizeCB.setEditable(true);
			if (m_srcFontSize.equals("") == false)
				m_srcFontSizeCB.setSelectedItem(m_srcFontSize);

			JLabel locFontLabel = new JLabel();
			m_locFontCB = new JComboBox(StaticUtils.getFontNames());
			m_locFontCB.setEditable(true);
			if (m_locFont.equals("") == false)
				m_locFontCB.setSelectedItem(m_locFont);

			JLabel locFontSizeLabel = new JLabel();
			m_locFontSizeCB = new JComboBox(fontSizes);
			m_locFontSizeCB.setEditable(true);
			if (m_locFontSize.equals("") == false)
				m_locFontSizeCB.setSelectedItem(m_locFontSize);

			JButton okButton = new JButton();
			JButton cancelButton = new JButton();
	
			// arrange the UI
			Box srcFontSize = Box.createHorizontalBox();
			srcFontSize.add(srcFontSizeLabel);
			srcFontSize.add(Box.createHorizontalGlue());
			srcFontSize.add(m_srcFontSizeCB);
			
			Box srcFont = Box.createVerticalBox();
			srcFont.add(srcFontLabel);
			srcFont.add(m_srcFontCB);
			srcFont.add(srcFontSize);

			Box locFontSize = Box.createHorizontalBox();
			locFontSize.add(locFontSizeLabel);
			locFontSize.add(Box.createHorizontalGlue());
			locFontSize.add(m_locFontSizeCB);
			
			Box locFont = Box.createVerticalBox();
			locFont.add(locFontLabel);
			locFont.add(m_locFontCB);
			locFont.add(locFontSize);

			Box font = Box.createHorizontalBox();
			font.add(srcFont);
			font.add(Box.createHorizontalStrut(10));
			font.add(locFont);

			Box buttons = Box.createHorizontalBox();
			buttons.add(Box.createHorizontalGlue());
			buttons.add(cancelButton);
			buttons.add(Box.createHorizontalStrut(10));
			buttons.add(okButton);

			Container cont = getContentPane();
			cont.add(font, "Center");
			cont.add(buttons, "South");
			
			// add text
			okButton.setText(OStrings.PP_BUTTON_OK);
			cancelButton.setText(OStrings.PP_BUTTON_CANCEL);

			srcFontLabel.setText(OStrings.TF_SELECT_SOURCE_FONT);
			srcFontSizeLabel.setText(OStrings.TF_SELECT_FONTSIZE);
			locFontLabel.setText(OStrings.TF_SELECT_TARGET_FONT);
			locFontSizeLabel.setText(OStrings.TF_SELECT_FONTSIZE);

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
			str = m_srcFontCB.getSelectedItem().toString();
			if (str.equals(m_srcFont) == false)
			{
				m_isChanged = true;
				m_srcFont = str;
				core.setPreference(OConsts.TF_SRC_FONT_NAME, m_srcFont);
			}
			
			str = m_srcFontSizeCB.getSelectedItem().toString();
			if (str.equals(m_srcFontSize) == false)
			{
				m_isChanged = true;
				m_srcFontSize = str;
				core.setPreference(OConsts.TF_SRC_FONT_SIZE, m_srcFontSize);
			}
			
			str = m_locFontCB.getSelectedItem().toString();
			if (str.equals(m_locFont) == false)
			{
				m_isChanged = true;
				m_locFont = str;
				core.setPreference(OConsts.TF_LOC_FONT_NAME, m_locFont);
			}
			
			str = m_locFontSizeCB.getSelectedItem().toString();
			if (str.equals(m_locFontSize) == false)
			{
				m_isChanged = true;
				m_locFontSize = str;
				core.setPreference(OConsts.TF_LOC_FONT_SIZE, m_locFontSize);
			}

			dispose();
		}

		private void		doCancel()		{ dispose();			}
		protected boolean	isChanged()		{ return m_isChanged;	}
		
		protected JComboBox	m_srcFontCB;
		protected JComboBox	m_srcFontSizeCB;
		protected JComboBox	m_locFontCB;
		protected JComboBox	m_locFontSizeCB;

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
						else if (pos > (m_xlDoc.getLength()-m_segmentEndInset))
						{
							// after current entry
							int inset = (m_xlDoc.getLength()-m_segmentEndInset);
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
			int keyCode = e.getKeyCode();
			char c = e.getKeyChar();

			// ignore keyboard until document is ready
			// don't ignore it too much or jvm 1.4 won't hear ctrl-o
			//	to open a project
			if (m_docReady == false)
			{
				// look for control events and let certain ones of these
				//	pass through 
				if (((e.getModifiers() == m_shortcutKey)		&&
						((e.getID() == KeyEvent.KEY_PRESSED))	||
						(e.getID() == KeyEvent.KEY_TYPED)))
				{
					switch (c)
					{
						case 6:		// ctrl-f (to allow directory searches)
						case 15:	// ctrl-o
						case 17:	// ctrl-q
							super.processKeyEvent(e);
							return;
						default:
					}
				}

				return;
			}
			
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
									m_segmentDisplayLength +
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
						int end = m_xlDoc.getLength() - m_segmentEndInset -
								OStrings.TF_CUR_SEGMENT_END.length() - 3;
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
			int end = m_xlDoc.getLength() - m_segmentEndInset -
				OStrings.TF_CUR_SEGMENT_END.length() - 3;
			if (pos >= end)
				return false;
		}
		else
		{
			// make sure we're not at start of segment
			int start = m_segmentStartOffset + m_segmentDisplayLength +
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
		int start = m_segmentStartOffset + m_segmentDisplayLength +
			OStrings.TF_CUR_SEGMENT_START.length() + 1;
		// -1 for space before tag, -2 for newlines
		int end = m_xlDoc.getLength() - m_segmentEndInset -
			OStrings.TF_CUR_SEGMENT_END.length() - 3;
		boolean reset = false;
		
		if (spos != epos)
		{
			// dealing with a selection here - make sure it's w/in bounds
			if (spos < start)
			{
				reset = true;
				m_xlPane.setSelectionStart(start);
			}
			if (epos > end)
			{
				reset = true;
				m_xlPane.setSelectionEnd(end);
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
	private JMenuItem	m_miFileSave;
	private JMenuItem	m_miFileQuit;
	private JMenu m_mEdit;
	private JMenuItem	m_miEditNext;
	private JMenuItem	m_miEditPrev;
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
	private JCheckBoxMenuItem	m_miDisplayFuzzyMatch;
	private JCheckBoxMenuItem	m_miDisplayGlossaryMatch;
	
	private JMenu		m_mTools;
	private JMenuItem	m_miToolsSpell;
	private JMenuItem	m_miToolsPseudoTrans;
	private JMenuItem	m_miToolsValidateTags;
	private JMenuItem	m_miToolsMergeTMX;

	private JMenu		m_mVersion;
	private JMenuItem	m_miVersionHelp;
//	private JMenuItem	m_miVersionNumber;

	// source and target font display info
	protected String	m_srcFont;
	protected String	m_srcFontSize;
	protected String	m_locFont;
	protected String	m_locFontSize;

	// first and last entry numbers in current file
	protected int		m_xlFirstEntry;
	protected int		m_xlLastEntry;

	// starting offset and length of source lang in current segment
	protected int		m_segmentStartOffset = 0;
	protected int		m_segmentDisplayLength = 0;
	protected int		m_segmentEndInset = 0;
	// text length of glossary, if displayed
	protected int		m_glossaryLength = 0;

	// boolean set after safety check that OStrings.TF_CUR_SEGMENT_START
	//	contains empty "0000" for segment number
	protected boolean	m_segmentTagHasNumber = false;

	// indicates the document is loaded and ready for processing
	protected boolean	m_docReady = false;
	protected Cursor	m_defaultCursor = null;

	// indicates the boundary of editable text in xlPane (char offsets)
	protected int		m_lowTextLock = 0;
	protected int		m_highTextLock = 0;

	// list of text segments in current doc
	protected ArrayList	m_docSegList;

	// make a local copy of this instead of fetching it each time
	protected int	m_shortcutKey;

	private JTextPane	m_oldSrcPane;
	private DefaultStyledDocument	m_oldSrcDoc;
	private JTextPane	m_oldLocPane;
	private DefaultStyledDocument	m_oldLocDoc;
	private XLPane		m_xlPane;
	private DefaultStyledDocument	m_xlDoc;
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

