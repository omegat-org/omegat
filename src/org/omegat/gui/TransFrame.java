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

package org.omegat.gui;

import org.omegat.core.glossary.GlossaryEntry;
import org.omegat.core.matching.NearString;
import org.omegat.core.matching.SourceTextEntry;
import org.omegat.core.StringEntry;
import org.omegat.gui.dialogs.FontSelectionDialog;
import org.omegat.core.threads.CommandThread;
import org.omegat.core.threads.SearchThread;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.PreferenceManager;
import org.omegat.util.RequestPacket;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.border.EtchedBorder;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 * The main frame of OmegaT application
 *
 * @author Keith Godfrey
 */
public class TransFrame extends JFrame implements ActionListener
{
	// Initialization and display code
	public TransFrame()
	{
        m_curEntryNum = -1;
		m_curNear = null;
        m_activeProj = "";														// NOI18N
		m_activeFile = "";														// NOI18N

		m_shortcutKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

		m_undo = new UndoManager();

		m_docSegList = new ArrayList();

		createMenus();
		createUI();
		
		////////////////////////////////
		updateUIText();
		loadDisplayPrefs();

		enableEvents(0);

		initScreenLayout();

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
		catch (NumberFormatException nfe) {
        }
		m_xlPane.setFont(new Font(m_font, Font.PLAIN, fontSize));
		m_matchViewer.setFont(new Font(m_font, Font.PLAIN, fontSize));

		
		// check this only once as it can be changed only at compile time
		// should be OK, but customization might have messed it up
		String start = OStrings.TF_CUR_SEGMENT_START;
		int zero = start.lastIndexOf('0');
        m_segmentTagHasNumber = (zero > 4) && // 4 to reserve room for 10000 digit
                (start.charAt(zero - 1) == '0') &&
                (start.charAt(zero - 2) == '0') &&
                (start.charAt(zero - 3) == '0');
	}

	private void loadDisplayPrefs()
	{
		String mn = PreferenceManager.pref.getPreference(OConsts.PREF_MNEMONIC);
		if (mn != null && mn.equals("true"))								// NOI18N
		{
			m_miDisplayMnemonic.setSelected(true);
			doSetMnemonics(true);
		}
		String tab = PreferenceManager.pref.getPreference(OConsts.PREF_TAB);
		if (tab != null && tab.equals("true"))								// NOI18N
		{
			m_miDisplayAdvanceKey.setSelected(true);
			m_advancer = KeyEvent.VK_TAB;
		}
		else
			m_advancer = KeyEvent.VK_ENTER;
	}

	private void initScreenLayout()
	{
		// KBG - assume screen size is 800x600 if width less than 900, and
		//		1024x768 if larger.  assume task bar at bottom of screen.
		//		if screen size saved, recover that and use instead
		//	(18may04)
		String dw, dh, dx, dy;
		dw = PreferenceManager.pref.getPreference(OConsts.PREF_DISPLAY_W);
		dh = PreferenceManager.pref.getPreference(OConsts.PREF_DISPLAY_H);
		dx = PreferenceManager.pref.getPreference(OConsts.PREF_DISPLAY_X);
		dy = PreferenceManager.pref.getPreference(OConsts.PREF_DISPLAY_Y);
		int x=0;
		int y=0;
		int w=0;
		int h=0;
		boolean badSize = false;
		if (dw == null || dw.equals("")	|| dh == null			||			// NOI18N
                dh.equals("")	|| dx == null || dx.equals("")	||			// NOI18N
                dy == null || dy.equals(""))								// NOI18N
		{
			badSize = true;
		}
		else
		{
			try 
			{
				x = Integer.parseInt(dx);
				y = Integer.parseInt(dy);
				w = Integer.parseInt(dw);
				h = Integer.parseInt(dh);
			}
			catch (NumberFormatException nfe)
			{
				badSize = true;
			}
		}
		if (badSize)
		{
			// size info missing - put window in default position
			GraphicsEnvironment env = 
					GraphicsEnvironment.getLocalGraphicsEnvironment();
			Rectangle scrSize = env.getMaximumWindowBounds();
			if (scrSize.width < 900)
			{
				// assume 800x600
				setSize(585, 536);
				setLocation(0, 0);
			}
			else
			{
				// assume 1024x768 or larger
				setSize(675, 700);
				setLocation(0, 0);
			}
		}
		else
		{
			setSize(w, h);
			setLocation(x, y);
		}
	}

	private void storeScreenLayout()
	{
		int w = getWidth();
		int h = getHeight();
		int x = getX();
		int y = getY();
		CommandThread.core.setPreference(OConsts.PREF_DISPLAY_W, "" + w);		// NOI18N
		CommandThread.core.setPreference(OConsts.PREF_DISPLAY_H, "" + h);		// NOI18N
		CommandThread.core.setPreference(OConsts.PREF_DISPLAY_X, "" + x);		// NOI18N
		CommandThread.core.setPreference(OConsts.PREF_DISPLAY_Y, "" + y);		// NOI18N
	}

	private void createUI()
	{
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
	}

	private void doSetMnemonics(boolean show)
	{
		if (show)
		{
			m_mFile.setMnemonic(KeyEvent.VK_F);
			m_miFileOpen.setMnemonic(KeyEvent.VK_O);
			m_miFileCreate.setMnemonic(KeyEvent.VK_N);
			m_miFileCompile.setMnemonic(KeyEvent.VK_C);
			m_miFileProjWin.setMnemonic(KeyEvent.VK_L);
			m_miFileMatchWin.setMnemonic(KeyEvent.VK_M);
			m_miFileSave.setMnemonic(KeyEvent.VK_S);
			m_miFileQuit.setMnemonic(KeyEvent.VK_Q);
			m_mEdit.setMnemonic(KeyEvent.VK_E);
			m_miEditUndo.setMnemonic(KeyEvent.VK_Z);
			m_miEditRedo.setMnemonic(KeyEvent.VK_Y);
			m_miEditRecycle.setMnemonic(KeyEvent.VK_R);
			m_miEditInsert.setMnemonic(KeyEvent.VK_I);
			m_miEditFind.setMnemonic(KeyEvent.VK_F);
//			m_miEditUntrans.setMnemonic(KeyEvent.VK_U);
			m_miEditNext.setMnemonic(KeyEvent.VK_N);
			m_miEditPrev.setMnemonic(KeyEvent.VK_P);
			m_miEditNextUntrans.setMnemonic(KeyEvent.VK_U);
			m_miEditCompare1.setMnemonic(KeyEvent.VK_1);
			m_miEditCompare2.setMnemonic(KeyEvent.VK_2);
			m_miEditCompare3.setMnemonic(KeyEvent.VK_3);
			m_miEditCompare4.setMnemonic(KeyEvent.VK_4);
			m_miEditCompare5.setMnemonic(KeyEvent.VK_5);
			m_mDisplay.setMnemonic(KeyEvent.VK_C);
			m_miDisplayFont.setMnemonic(KeyEvent.VK_F);
			m_miDisplayAdvanceKey.setMnemonic(KeyEvent.VK_T);
			m_miDisplayMnemonic.setMnemonic(KeyEvent.VK_M);
			m_mTools.setMnemonic(KeyEvent.VK_T);
			m_miToolsValidateTags.setMnemonic(KeyEvent.VK_T);
			m_mVersion.setMnemonic(KeyEvent.VK_O);
			m_miVersionHelp.setMnemonic(KeyEvent.VK_H);
		}
		else
		{
			m_mFile.setMnemonic(0);
			m_miFileOpen.setMnemonic(0);
			m_miFileCreate.setMnemonic(0);
			m_miFileCompile.setMnemonic(0);
			m_miFileProjWin.setMnemonic(0);
			m_miFileMatchWin.setMnemonic(0);
			m_miFileSave.setMnemonic(0);
			m_miFileQuit.setMnemonic(0);
			m_mEdit.setMnemonic(0);
			m_miEditUndo.setMnemonic(0);
			m_miEditRedo.setMnemonic(0);
			m_miEditRecycle.setMnemonic(0);
			m_miEditInsert.setMnemonic(0);
			m_miEditFind.setMnemonic(0);
	//		m_miEditUntrans.setMnemonic(0);
			m_miEditNext.setMnemonic(0);
			m_miEditPrev.setMnemonic(0);
			m_miEditNextUntrans.setMnemonic(0);
			m_miEditCompare1.setMnemonic(0);
			m_miEditCompare2.setMnemonic(0);
			m_miEditCompare3.setMnemonic(0);
			m_miEditCompare4.setMnemonic(0);
			m_miEditCompare5.setMnemonic(0);
			m_mDisplay.setMnemonic(0);
			m_miDisplayFont.setMnemonic(0);
			m_miDisplayAdvanceKey.setMnemonic(0);
			m_miDisplayMnemonic.setMnemonic(0);
			m_mTools.setMnemonic(0);
			m_miToolsValidateTags.setMnemonic(0);
			m_mVersion.setMnemonic(0);
			m_miVersionHelp.setMnemonic(0);
		}
	}

	private void createMenus()
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
//		m_miFileCreate.setAccelerator(
//					KeyStroke.getKeyStroke(KeyEvent.VK_N, m_shortcutKey));
		m_miFileCreate.addActionListener(this);
		m_mFile.add(m_miFileCreate);

		m_miFileClose = new JMenuItem();
		m_miFileClose.addActionListener(this);
		m_mFile.add(m_miFileClose);

		m_miFileCompile = new JMenuItem();
		m_miFileCompile.addActionListener(this);
		m_mFile.add(m_miFileCompile);
		
		m_mFile.addSeparator();

		m_miFileProjWin = new JMenuItem();
		m_miFileProjWin.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_L,  m_shortcutKey));
		m_miFileProjWin.addActionListener(this);
		m_mFile.add(m_miFileProjWin);
		
		m_miFileMatchWin = new JMenuItem();
		m_miFileMatchWin.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_M,  m_shortcutKey));
		m_miFileMatchWin.addActionListener(this);
		m_mFile.add(m_miFileMatchWin);
		
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
		m_miEditUndo = new JMenuItem();
		m_miEditUndo.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_Z,  m_shortcutKey));
		m_miEditUndo.addActionListener(this);
		m_mEdit.add(m_miEditUndo);
		
		m_miEditRedo = new JMenuItem();
		m_miEditRedo.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_Y,  m_shortcutKey));
		m_miEditRedo.addActionListener(this);
		m_mEdit.add(m_miEditRedo);
		
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

//		m_miEditUntrans = new JMenuItem();
//		m_miEditUntrans.setAccelerator(KeyStroke.getKeyStroke(
//				KeyEvent.VK_U,  m_shortcutKey));
//		m_miEditUntrans.addActionListener(this);
//		m_mEdit.add(m_miEditUntrans);

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
		
		m_miEditNextUntrans = new JMenuItem();
		m_miEditNextUntrans.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, m_shortcutKey));
		m_miEditNextUntrans.addActionListener(this);
		m_mEdit.add(m_miEditNextUntrans);

		m_mEdit.addSeparator();

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

		m_miDisplayFont = new JMenuItem();
		m_miDisplayFont.addActionListener(this);
		m_mDisplay.add(m_miDisplayFont);
		
		m_miDisplayAdvanceKey = new JCheckBoxMenuItem();
		m_miDisplayAdvanceKey.setSelected(false);
		m_miDisplayAdvanceKey.addActionListener(this);
		m_mDisplay.add(m_miDisplayAdvanceKey);

		m_miDisplayMnemonic = new JCheckBoxMenuItem();
		m_miDisplayMnemonic.setSelected(false);
		m_miDisplayMnemonic.addActionListener(this);
		m_mDisplay.add(m_miDisplayMnemonic);

		mb.add(m_mDisplay);
		
		// tools
		m_mTools = new JMenu();
		m_miToolsPseudoTrans = new JMenuItem();
		m_miToolsPseudoTrans.addActionListener(this);
		m_mTools.add(m_miToolsPseudoTrans);

		m_miToolsValidateTags = new JMenuItem();
		m_miToolsValidateTags.addActionListener(this);
		m_mTools.add(m_miToolsValidateTags);

		mb.add(m_mTools);
		
		m_mVersion = new JMenu();
		m_miVersionHelp = new JMenuItem();
		m_miVersionHelp.setAccelerator(
					KeyStroke.getKeyStroke(KeyEvent.VK_F1,0));
		m_miVersionHelp.addActionListener(this);
		m_mVersion.add(m_miVersionHelp);

		mb.add(m_mVersion);

		setJMenuBar(mb);
	}

	private void updateUIText()
	{
		doSetTitle();

		m_matchViewer.setTitle(OStrings.TF_MATCH_VIEWER_TITLE);

        m_mFile.setText(OStrings.TF_MENU_FILE);
		m_miFileOpen.setText(OStrings.TF_MENU_FILE_OPEN);
		m_miFileCreate.setText(OStrings.TF_MENU_FILE_CREATE);
		m_miFileClose.setText(OStrings.TF_MENU_FILE_CLOSE);
		m_miFileCompile.setText(OStrings.TF_MENU_FILE_COMPILE);
		m_miFileProjWin.setText(OStrings.TF_MENU_FILE_PROJWIN);
		m_miFileMatchWin.setText(OStrings.TF_MENU_FILE_MATCHWIN);
		m_miFileSave.setText(OStrings.TF_MENU_FILE_SAVE);
		m_miFileQuit.setText(OStrings.TF_MENU_FILE_QUIT);

		m_mEdit.setText(OStrings.TF_MENU_EDIT);
		m_miEditUndo.setText(OStrings.TF_MENU_EDIT_UNDO);
		m_miEditRedo.setText(OStrings.TF_MENU_EDIT_REDO);
		m_miEditNext.setText(OStrings.TF_MENU_EDIT_NEXT);
		m_miEditPrev.setText(OStrings.TF_MENU_EDIT_PREV);
		m_miEditNextUntrans.setText(OStrings.TF_MENU_EDIT_NEXTUNTRANS);
		m_miEditCompare1.setText(OStrings.TF_MENU_EDIT_COMPARE_1);
		m_miEditCompare2.setText(OStrings.TF_MENU_EDIT_COMPARE_2);
		m_miEditCompare3.setText(OStrings.TF_MENU_EDIT_COMPARE_3);
		m_miEditCompare4.setText(OStrings.TF_MENU_EDIT_COMPARE_4);
		m_miEditCompare5.setText(OStrings.TF_MENU_EDIT_COMPARE_5);
		m_miEditRecycle.setText(OStrings.TF_MENU_EDIT_RECYCLE);
		m_miEditInsert.setText(OStrings.TF_MENU_EDIT_INSERT);
		m_miEditFind.setText(OStrings.TF_MENU_EDIT_FIND);
//		m_miEditUntrans.setText(org.omegat.OStrings.TF_MENU_EDIT_NEXT_UNTRANS);
		
		m_mDisplay.setText(OStrings.TF_MENU_DISPLAY);
		m_miDisplayFont.setText(OStrings.TF_MENU_DISPLAY_FONT);
		m_miDisplayAdvanceKey.setText(OStrings.TF_MENU_DISPLAY_ADVANCE);
		m_miDisplayMnemonic.setText(OStrings.TF_MENU_DISPLAY_MNEMONIC);
		
		m_mTools.setText(OStrings.TF_MENU_TOOLS);
		m_miToolsPseudoTrans.setText(OStrings.TF_MENU_TOOLS_PSEUDO);
		m_miToolsValidateTags.setText(OStrings.TF_MENU_TOOLS_VALIDATE);

		m_mVersion.setText(OStrings.VERSION);
		m_miVersionHelp.setText(OStrings.TF_MENU_VERSION_HELP);
		
		// KBG - the UI looks bad w/ misplaced mnemonics, but this is
		//	better than hard coding their location for localized versions
//		m_miFileProjWin.setDisplayedMnemonicIndex(10);
//		m_miFileMatchWin.setDisplayedMnemonicIndex(5);
//		m_miToolsValidateTags.setDisplayedMnemonicIndex(9);
	}

    ///////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////
	// command handling
	
	private void doQuit()
	{
		// shutdown
		if (m_projectLoaded)
		{
			commitEntry();
			doSave();
		}

		storeScreenLayout();
		m_matchViewer.storeScreenLayout();
		PreferenceManager.pref.save();

		CommandThread.core.signalStop();
		for (int i=0; i<25; i++)
		{
			while (CommandThread.core != null)
			{
				try { Thread.sleep(10); }
				catch (InterruptedException e) {
                }
			}
			if (CommandThread.core == null)
				break;
		}

		System.exit(0);
	}

	public void doPseudoTrans()
	{
		if (!m_projectLoaded)
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

	private void doValidateTags()
	{
		ArrayList suspects = CommandThread.core.validateTags();
		if (suspects.size() > 0)
		{
			// create list of suspect strings - use org.omegat.gui.ContextFrame for now
			ContextFrame cf = new ContextFrame(this);
			cf.setVisible(true);
			cf.displayStringList(suspects);
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
		if (!m_projectLoaded)
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
		if (!m_projectLoaded)
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

	/**
	  * Finds the next untranslated entry in the document.
	  */	
	public void doNextUntranslatedEntry()
	{
		// check if a document is loaded
		if (m_projectLoaded == false)
			return;
		
		// save the current entry
		commitEntry();
	
		// get the current entry number and the total number of entries
		int curEntryNum = m_curEntryNum;
		int numEntries = CommandThread.core.numEntries();
		
		// iterate through the list of entries,
		// starting at the current entry,
		// until an entry with no translation is found
		SourceTextEntry entry = null;
		while (curEntryNum < numEntries)
		{
			// get the next entry
			entry = CommandThread.core.getSTE(curEntryNum);
			
			// check if the entry is not null, and whether it contains a translation
			if (   (entry != null)
			    && (entry.getTranslation().length() == 0))
			{
				// mark the entry
				m_curEntryNum = curEntryNum;
				
				// activate the entry
				activateEntry();
				
				// stop searching
				break;
			}
			
			// next entry
			curEntryNum++;
		}
	}

	// insert current fuzzy match at cursor position
    private void doInsertTrans()
	{
		if (!m_projectLoaded)
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
		if (!m_projectLoaded)
			return;
		
		if (m_curNear == null)
			return;

		StringEntry se = m_curNear.str;
		doReplaceEditText(se.getTrans());
	}

    private void doReplaceEditText(String text)
	{
		if (!m_projectLoaded)
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

	public void doCompareN(int nearNum)
	{
		if (!m_projectLoaded)
			return;
		
		updateFuzzyInfo(nearNum);
	}

	public void doUnloadProject()
	{
		if (m_projectLoaded)
		{
			commitEntry();
			doSave();
		}
		m_projWin.reset();
		m_projectLoaded = false;
		m_xlPane.setText("");													// NOI18N
		m_miFileOpen.setEnabled(true);
		m_miFileCreate.setEnabled(true);
	}

	// display dialog allowing selection of source and target language fonts
    private void doFont()
	{
		FontSelectionDialog dlg = new FontSelectionDialog(this, m_font, m_fontSize);
		dlg.setVisible(true);
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
			catch (NumberFormatException nfe) {
            }
			Font font = new Font(m_font, Font.PLAIN, fontSize);
			m_xlPane.setFont(font);
			m_matchViewer.setFont(font);
			activateEntry();
		}
	}
	
    private void doSave()
	{
		if (!m_projectLoaded)
			return;
		
		RequestPacket pack;
		pack = new RequestPacket(RequestPacket.SAVE, this);
		CommandThread.core.messageBoardPost(pack);
	}

	private void doLoadProject()
	{
		doUnloadProject();
		m_matchViewer.reset();
		
		RequestPacket load;
		load = new RequestPacket(RequestPacket.LOAD, this);
		CommandThread.core.messageBoardPost(load);
	}

	private void doGotoEntry(int entryNum)
	{
		if (!m_projectLoaded)
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
		int num;
		try
		{
			num = Integer.parseInt(str);
			doGotoEntry(num);
		}
		catch (NumberFormatException e) {
        }
	}

	public void finishLoadProject()
	{
		m_activeProj = CommandThread.core.projName();
		m_activeFile = "";														// NOI18N
		m_curEntryNum = 0;
		loadDocument();
		m_projectLoaded = true;
		m_miFileOpen.setEnabled(false);
		m_miFileCreate.setEnabled(false);
	}

	private void doCompileProject()
	{
		if (!m_projectLoaded)
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

	private void doSetTitle()
	{
		String s = OStrings.TF_TITLE;
		if (m_activeProj.compareTo("") != 0)									// NOI18N
		{
			String file = m_activeFile.substring(
					CommandThread.core.sourceRoot().length());
			s += " - " + m_activeProj + " :: " + file;							// NOI18N
		}
		setTitle(s);
	}

	private void doFind()
	{
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

	/* updates status label */
	public void setMessageText(String str)
	{
		if( str.equals("") )													// NOI18N
			str = " ";															// NOI18N
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
		public int	length;		// display length
	}

	// displays all segments in current document
	// displays translation for each segment if it's available (in dark gray)
	// otherwise displays source text (in black)
	// stores length of each displayed segment plus its starting offset
    private void loadDocument()
	{
		m_docReady = false;

		// clear old text
		m_xlPane.setText("");													// NOI18N
		m_docSegList.clear();
		m_curEntry = CommandThread.core.getSTE(m_curEntryNum);
		setMessageText(OStrings.TF_LOADING_FILE + 
								m_curEntry.getSrcFile().name);
		Thread.yield();	// let UI update
		m_xlFirstEntry = m_curEntry.getFirstInFile();
		m_xlLastEntry = m_curEntry.getLastInFile();

		DocumentSegment docSeg;
		StringBuffer textBuf = new StringBuffer();
		
		for (int entryNum=m_xlFirstEntry; entryNum<=m_xlLastEntry; entryNum++)
		{
			docSeg = new DocumentSegment();
			
			SourceTextEntry ste = CommandThread.core.getSTE(entryNum);
			String text = ste.getTranslation();
			// set text and font
			if( text.length()==0 ) 
			{
				// no translation available - use source text
				text = ste.getSrcText(); 
			}
			text += "\n\n";														// NOI18N
			
			textBuf.append(text);

			docSeg.length = text.length();
			m_docSegList.add(docSeg);
		}
		m_xlPane.setText(textBuf.toString());
		
		setMessageText("");														// NOI18N
		Thread.yield();
	}

	///////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////
	// display oriented code

	// display fuzzy matching info if it's available
	// don't call this directly - should only be called through doCompareN
    private void updateFuzzyInfo(int nearNum)
	{
		if (!m_projectLoaded)
			return;
		
		StringEntry curEntry = m_curEntry.getStrEntry();
		List nearList = curEntry.getNearListTranslated();
		// see if there are any matches
		if( nearList.size()<=0 ) 
		{
			m_curNear = null;
			m_matchViewer.updateMatchText();
			return;
		}
		
		m_curNear = (NearString) nearList.get(nearNum);
		String str = null;
		
		NearString ns;
		int ctr = 0;
		int offset;
		int start = -1;
		int end = -1;
		ListIterator li = nearList.listIterator();
		
		while( li.hasNext() ) 
		{
			ns = (NearString) li.next();
			
			String oldStr = ns.str.getSrcText();
			String locStr = ns.str.getTrans();
			String proj = ns.proj;
			offset = m_matchViewer.addMatchTerm(oldStr, locStr,	(int)(ns.score*100), proj);
			
			if( ctr==nearNum ) {
				start = offset;
				str = oldStr;
			} else if( ctr==nearNum + 1 ) {
				end = offset;
			}
			
			ctr++;
		}
		
		m_matchViewer.hiliteRange(start, end);
		m_matchViewer.updateMatchText();
		m_matchViewer.formatNearText(m_curNear.str.getTokenList(), m_curNear.attr);
	}
	
	private void commitEntry()
	{
		if (!m_projectLoaded)
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
		String s;
		if (start == end)
		{
			s = m_curEntry.getSrcText();
			m_xlPane.select(start, end);
			m_xlPane.replaceSelection(s);
			end += s.length();
		}
		else
			s = m_xlPane.getText().substring(start, end);

		m_xlPane.select(start, end);
		MutableAttributeSet mattr;
		mattr = new SimpleAttributeSet();
		StyleConstants.setForeground(mattr, Color.darkGray);
		m_xlPane.setCharacterAttributes(mattr, true);
		
		m_xlPane.select(end, m_xlPane.getText().length() - m_segmentEndInset);
		m_xlPane.replaceSelection("");											// NOI18N
		m_xlPane.select(m_segmentStartOffset, start);
		m_xlPane.replaceSelection("");											// NOI18N
			
		// update memory
		m_curEntry.setTranslation(s);
		
		DocumentSegment docSeg = (DocumentSegment) 
							m_docSegList.get(m_curEntryNum - m_xlFirstEntry);
		docSeg.length = s.length() + "\n\n".length();							// NOI18N	
		
		// update the length parameters of all changed segments
		// update strings in display
		if (!s.equals(m_curTrans))
		{
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
				if (entry >= m_xlFirstEntry && entry <= m_xlLastEntry)
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
					m_xlPane.replaceSelection(s + "\n\n");						// NOI18N
					docSeg.length = s.length() + "\n\n".length();				// NOI18N
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
		if (!m_projectLoaded)
			return;

		int i;
		DocumentSegment docSeg;

		// recover data about current entry
		m_curEntry = CommandThread.core.getSTE(m_curEntryNum);
		String srcText = m_curEntry.getSrcText();

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
		String startStr = "\n" + OStrings.TF_CUR_SEGMENT_START;					// NOI18N
		String endStr = OStrings.TF_CUR_SEGMENT_END;
		if (m_segmentTagHasNumber)
		{
			// put entry number in first tag
			int num = m_curEntryNum + 1;
			int ones;
			
			// do it digit by digit - there's a better way (like sprintf)
			//	but this works just fine
			String disp = "";													// NOI18N
			while (num > 0)
			{
				ones = num % 10;
				num /= 10;
				disp = ones + disp;
			}
			int zero = startStr.lastIndexOf('0');
			startStr = startStr.substring(0, zero-disp.length()+1) + 
				disp + startStr.substring(zero+1);
		}

		MutableAttributeSet mattr;
		// append to end of segment first because this operation is done
		//	by reference to end of file which will change after insert
		m_xlPane.select(paneLen-m_segmentEndInset, paneLen-m_segmentEndInset);
		m_xlPane.replaceSelection(endStr);
		m_xlPane.select(paneLen-m_segmentEndInset + 1, 
				paneLen-m_segmentEndInset + endStr.length());
		mattr = new SimpleAttributeSet();
		StyleConstants.setBold(mattr, true);
		m_xlPane.setCharacterAttributes(mattr, true);

		m_xlPane.select(m_segmentStartOffset, m_segmentStartOffset);
		String insertText = srcText + startStr;
		m_xlPane.replaceSelection(insertText);
		m_xlPane.select(m_segmentStartOffset, m_segmentStartOffset + 
				insertText.length() - 1);
		m_xlPane.setCharacterAttributes(mattr, true);

		// background color
		Color background = new Color(192, 255, 192);
		// other color options
		m_xlPane.select(m_segmentStartOffset, m_segmentStartOffset + 
				insertText.length() - startStr.length());
		mattr = new SimpleAttributeSet();
		StyleConstants.setBackground(mattr, background);
		m_xlPane.setCharacterAttributes(mattr, false);

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
		if (curEntry.getGlossaryEntries().size() > 0)
		{
			// TODO do something with glossary terms
			m_glossaryLength = curEntry.getGlossaryEntries().size();
			ListIterator li = curEntry.getGlossaryEntries().listIterator();
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

		int nearLength = curEntry.getNearListTranslated().size();
		
		if (nearLength > 0 && m_glossaryLength > 0)
		{
			// display text indicating both categories exist
			Object obj[] = { 
					new Integer(nearLength), 
					new Integer(m_glossaryLength) };
			setMessageText(MessageFormat.format(
							OStrings.TF_NUM_NEAR_AND_GLOSSARY, obj));
		}
		else if (nearLength > 0)
		{
			Object obj[] = { new Integer(nearLength) };
			setMessageText(MessageFormat.format(
							OStrings.TF_NUM_NEAR, obj));
		}
		else if (m_glossaryLength > 0)
		{
			Object obj[] = { new Integer(m_glossaryLength) };
			setMessageText(MessageFormat.format(
							OStrings.TF_NUM_GLOSSARY, obj));
		}
		else
			setMessageText("");													// NOI18N

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
			if (j > 2 || padding > 500)
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
		if (!m_docReady)
		{
			m_docReady = true;
		}
		m_undo.die();
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
			else if (evtSrc == m_miFileClose)
			{
				doUnloadProject();
				CommandThread.core.requestUnload();
			}
			else if (evtSrc == m_miFileCompile)
			{
				doCompileProject();
			}
			else if (evtSrc == m_miFileMatchWin)
			{
				if (m_matchViewer.isVisible())
					m_matchViewer.setVisible(false);
				else
				{
					m_matchViewer.setVisible(true);
					toFront();
				}
			}
			else if (evtSrc == m_miFileProjWin)
			{
				if (m_projWin != null)
				{
					m_projWin.setVisible(true);
					m_projWin.toFront();
				}
			}
			else if (evtSrc == m_miEditUndo)
			{
				try 
				{
					m_undo.undo();
				}
				catch (CannotUndoException cue)	{
                }
			}
			else if (evtSrc == m_miEditRedo)
			{
				try 
				{
					m_undo.redo();
				}
				catch (CannotRedoException cue)	{
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
			else if (evtSrc == m_miEditNextUntrans)
			{
				doNextUntranslatedEntry();
			}
			else if (evtSrc == m_miDisplayAdvanceKey)
			{
				if (m_miDisplayAdvanceKey.isSelected())
				{
					m_advancer = KeyEvent.VK_TAB;
					CommandThread.core.setPreference(
							OConsts.PREF_TAB, "true");							// NOI18N
				}
				else
				{	
					m_advancer = KeyEvent.VK_ENTER;
					CommandThread.core.setPreference(
							OConsts.PREF_TAB, "false");							// NOI18N
				}
			}
			else if (evtSrc == m_miDisplayMnemonic)
			{
				if (m_miDisplayMnemonic.isSelected())
				{
					doSetMnemonics(true);
					CommandThread.core.setPreference(
							OConsts.PREF_MNEMONIC, "true");						// NOI18N
				}
				else
				{
					doSetMnemonics(false);
					CommandThread.core.setPreference(
							OConsts.PREF_MNEMONIC, "false");					// NOI18N
				}
			}
//			else if (evtSrc == m_miEditUntrans)
//			{
//				doNextUntrans();
//			}
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
			else if (evtSrc == m_miVersionHelp)
			{
				HelpFrame hf = HelpFrame.getInstance();
				hf.setVisible(true);
				hf.toFront();
			}
		}
	}

	public void displayWarning(String msg, Throwable e)
	{
		setMessageText(msg);
		String str = OStrings.TF_WARNING;
		JOptionPane.showMessageDialog(this, msg + "\n" + e.toString(),			// NOI18N
					str, JOptionPane.WARNING_MESSAGE);
	}

	public void displayError(String msg, Throwable e)
	{
		setMessageText(msg);
		String str = OStrings.TF_ERROR;
		JOptionPane.showMessageDialog(this, msg + "\n" + e.toString(),			// NOI18N 
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
					if (!m_docReady)
						return;

					super.mouseClicked(e);
					if (e.getClickCount() == 2)
					{
						// user double clicked on view pane - goto entry
						// that was clicked
						int pos = getCaretPosition();
						DocumentSegment docSeg;
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
						else if (pos > m_xlPane.getText().length() - m_segmentEndInset)
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
			if (!m_projectLoaded)
			{
				if (e.getModifiers() == m_shortcutKey ||
                        e.getModifiers() == InputEvent.ALT_MASK);
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
			if (e.getID() == KeyEvent.KEY_PRESSED	&&
					(keyCode == KeyEvent.VK_CONTROL	||
                    keyCode == KeyEvent.VK_ALT		||
                    keyCode == KeyEvent.VK_META		||
                    keyCode == KeyEvent.VK_SHIFT))
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
					if (checkCaretForDelete(false))
					{
						super.processKeyEvent(e);
					}
					return;
				case 127:
					// this check shouldn't be necessary, but
					//	make it in case the key handling changes to make
					//	backspace and delete work the same way
					if (checkCaretForDelete(true))
					{
						super.processKeyEvent(e);
					}
					return;
			}

			// for now, force all key presses to reset the cursor to
			//	the editing region unless it's a ctrl-c (copy)
			if (e.getID() == KeyEvent.KEY_PRESSED && e.getModifiers() == m_shortcutKey && (keyCode == 'c' || keyCode == 'C')
					||
                    e.getID() == KeyEvent.KEY_TYPED && e.getModifiers() == m_shortcutKey && c == 3)
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
			// 18may04 KBG - enable user to hit TAB to advance to next 
			//	entry.  some asian IMEs don't swallow enter key resulting
			//	in non-usability
			if (keyCode == KeyEvent.VK_ENTER) 
			{
				if (e.isShiftDown())
				{
					// convert key event to straight enter key
					KeyEvent ke = new KeyEvent(e.getComponent(), e.getID(), 
							e.getWhen(), 0, KeyEvent.VK_ENTER, '\n');
					super.processKeyEvent(ke);
				}
				else if (m_advancer != keyCode)
				{
					return;	// swallow event - hopefully IME still works
				}
			}
			
			if (keyCode == m_advancer)
			{
				if (m_advancer == KeyEvent.VK_ENTER)
				{
					if (e.isControlDown())
					{
						// go backwards on control return
						if (e.getID() == KeyEvent.KEY_PRESSED)
							doPrevEntry();
					}
					else if (!e.isShiftDown())
					{
						// return w/o modifiers - swallow event and move on to 
						//  next segment
						if (e.getID() == KeyEvent.KEY_PRESSED)
							doNextEntry();
					}
				}
				else if (m_advancer == KeyEvent.VK_TAB)
				{
					// ctrl-tab not caught
					if (e.isShiftDown())
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
				}
				return;
			}
			
			// need to over-ride default text hiliting procedures because
			//	we're managing caret placement manually
			if (e.isShiftDown())
			{
				// if navigation control, make sure things are hilited
				if (keyCode == KeyEvent.VK_UP				||
                        keyCode == KeyEvent.VK_LEFT		||
                        keyCode == KeyEvent.VK_KP_UP		||
                        keyCode == KeyEvent.VK_KP_LEFT)
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
				else if (keyCode == KeyEvent.VK_DOWN		||
                        keyCode == KeyEvent.VK_RIGHT		||
                        keyCode == KeyEvent.VK_KP_DOWN	||
                        keyCode == KeyEvent.VK_KP_RIGHT)
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
			if (keyCode == KeyEvent.VK_UP				||
                    keyCode == KeyEvent.VK_LEFT		||
                    keyCode == KeyEvent.VK_KP_UP		||
                    keyCode == KeyEvent.VK_KP_LEFT)
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
			else if (keyCode == KeyEvent.VK_DOWN		||
                    keyCode == KeyEvent.VK_RIGHT		||
                    keyCode == KeyEvent.VK_KP_DOWN	||
                    keyCode == KeyEvent.VK_KP_RIGHT)
			{
				int end = m_xlPane.getSelectionEnd();
				int start = m_xlPane.getSelectionStart();
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
    private boolean checkCaretForDelete(boolean forward)
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
    private void checkCaret()
	{
		//int pos = m_xlPane.getCaretPosition();
		int spos = m_xlPane.getSelectionStart();
		int epos = m_xlPane.getSelectionEnd();
		int start = m_segmentStartOffset + m_sourceDisplayLength +
			OStrings.TF_CUR_SEGMENT_START.length() + 1;
		// -1 for space before tag, -2 for newlines
		int end = m_xlPane.getText().length() - m_segmentEndInset -
			OStrings.TF_CUR_SEGMENT_END.length();

		if (spos != epos)
		{
			// dealing with a selection here - make sure it's w/in bounds
			if (spos < start)
			{
				m_xlPane.setSelectionStart(start);
			}
			else if (spos > end)
			{
				m_xlPane.setSelectionStart(end);
			}
			if (epos > end)
			{
				m_xlPane.setSelectionEnd(end);
			}
			else if (epos < start)
			{
				m_xlPane.setSelectionStart(start);
			}
		}
		else
		{
			// non selected text 
			if (spos < start)
			{
				m_xlPane.setCaretPosition(start);
			}
			else if (spos > end)
			{
				m_xlPane.setCaretPosition(end);
			}
		}
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
				catch (InterruptedException e) {
                }
			}
			if (CommandThread.core == null)
				break;
		}
		System.exit(1);
	}
	
	/**
	 * Overrides parent method to show Match/Glossary viewer 
	 * simultaneously with the main frame.
	 */
	public void setVisible(boolean b)
	{
		super.setVisible(b);
		m_matchViewer.setVisible(b);
		toFront();
	}

	public boolean	isProjectLoaded()	{ return m_projectLoaded;		}

    private JMenu 		m_mFile;
	private JMenuItem	m_miFileOpen;
	private JMenuItem	m_miFileCreate;
	private JMenuItem	m_miFileClose;
	private JMenuItem	m_miFileCompile;
	private JMenuItem	m_miFileProjWin;
	private JMenuItem	m_miFileMatchWin;
	private JMenuItem	m_miFileSave;
	private JMenuItem	m_miFileQuit;
	private JMenu m_mEdit;
	private JMenuItem	m_miEditUndo;
	private JMenuItem	m_miEditRedo;
	private JMenuItem	m_miEditNext;
	private JMenuItem	m_miEditPrev;
	private JMenuItem m_miEditNextUntrans;
//	private JMenuItem	m_miEditUntrans;
//	private JMenuItem	m_miEditGoto;
	private JMenuItem	m_miEditFind;
	private JMenuItem	m_miEditRecycle;
	private JMenuItem	m_miEditInsert;
	private JMenuItem	m_miEditCompare1;
	private JMenuItem	m_miEditCompare2;
	private JMenuItem	m_miEditCompare3;
	private JMenuItem	m_miEditCompare4;
	private JMenuItem	m_miEditCompare5;

	private JMenu		m_mDisplay;
	private JCheckBoxMenuItem	m_miDisplayAdvanceKey;
	private JCheckBoxMenuItem	m_miDisplayMnemonic;
	private JMenuItem	m_miDisplayFont;
	
	private JMenu		m_mTools;
    private JMenuItem	m_miToolsPseudoTrans;
	private JMenuItem	m_miToolsValidateTags;

    private JMenu		m_mVersion;
	private JMenuItem	m_miVersionHelp;
//	private JMenuItem	m_miVersionNumber;

	// source and target font display info
    private String	m_font;
	private String	m_fontSize;

	// first and last entry numbers in current file
    private int		m_xlFirstEntry;
	private int		m_xlLastEntry;

	// starting offset and length of source lang in current segment
    private int		m_segmentStartOffset;
	private int		m_sourceDisplayLength;
	private int		m_segmentEndInset;
	// text length of glossary, if displayed
    private int		m_glossaryLength;

	// boolean set after safety check that org.omegat.OStrings.TF_CUR_SEGMENT_START
	//	contains empty "0000" for segment number
    private boolean	m_segmentTagHasNumber;

	// indicates the document is loaded and ready for processing
    private boolean	m_docReady;

    // list of text segments in current doc
    private ArrayList	m_docSegList;

	// make a local copy of this instead of fetching it each time
    private int	m_shortcutKey;
	private UndoManager	m_undo;
	private char	m_advancer;

	private XLPane		m_xlPane;
	private JScrollPane	m_xlScroller;

	private JLabel		m_statusLabel;
	private SourceTextEntry		m_curEntry;

	private String	m_activeFile;
	private String	m_activeProj;
	private int m_curEntryNum;
	private NearString m_curNear;
    private String	m_curTrans = "";										// NOI18N

	private ProjectFrame	m_projWin;
	private MatchWindow	m_matchViewer;

	private boolean m_projectLoaded;

}

