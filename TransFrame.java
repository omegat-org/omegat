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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.lang.*;

class TransFrame extends JFrame implements ActionListener
{
	public TransFrame()
	{
		super();
		String str;
		m_curEntry = -1;
		m_curNear = null;
		m_numEntries = -1;
		m_activeProj = "";
		m_activeFile = "";
		m_nearListNum = -1;

		m_langMap = new TreeMap();
		createMenus();
		createUI();
		
		////////////////////////////////
		updateUIText();

		enableEvents(0);

		setSize(900, 680);
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
	}

	protected void createUI()
	{
		int i;

		// create glos html view
		m_matchPane = new JTextPane();
		m_matchPane.setEditable(false);
		m_matchPane.setContentType("text/html");
		JScrollPane matchScroller = new JScrollPane(m_matchPane);
		matchScroller.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		// create 'old' src html field
		m_oldSrcPane = new JTextPane();
		m_oldSrcPane.setEditable(false);
		m_oldSrcPane.setContentType("text/html");
		JScrollPane oldSrcScroller = new JScrollPane(m_oldSrcPane);
		oldSrcScroller.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

		// create 'old' loc html field
		m_oldLocPane = new JTextPane();
		m_oldLocPane.setEditable(false);
		m_oldLocPane.setContentType("text/html");
		JScrollPane oldLocScroller = new JScrollPane(m_oldLocPane);
		oldLocScroller.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

		// create context view - entry n-1
		m_context1Pane = new MTField(this, 0);
		m_context1Pane .setEditable(false);
		m_context1Pane .setContentType("text/html");
		m_context1Pane.addHyperlinkListener(new HListener(this));
		JScrollPane context1Scroller = new JScrollPane(m_context1Pane);

		// create context view - entry n+1
		m_context2Pane = new MTField(this, 1);
		m_context2Pane .setEditable(false);
		m_context2Pane .setContentType("text/html");
		m_context2Pane.addHyperlinkListener(new HListener(this));
		JScrollPane context2Scroller = new JScrollPane(m_context2Pane);


		// create translation edit field
		//m_xlPane = new JTextPane();
		m_xlPane = new XLPane();
		m_xlPane.setContentType("text/plain");

		m_fuzzyProjLabel = new JLabel();

		/////////////////////////////////////////
		// create stat container
		// 4 rows are goto entry, word count, find label, find field
		Container statContainer = new Container();
		statContainer.setLayout(new GridLayout(5,1));

		// entry info
		m_statusLabel = new JLabel();
		m_wordcountLabel = new JLabel();
		m_entryNumPosition = new JLabel();
		m_gotoEntry = new JTextField();
		m_gotoEntry.setEnabled(false);
		m_gotoEntryContainer = new Container();
		m_gotoEntryContainer.setLayout(new GridLayout(1,2));
		m_gotoEntryContainer.add(m_entryNumPosition);
		m_gotoEntryContainer.add(m_gotoEntry);

		// create find fields
		m_findPane = new JTextField();
		m_findPane.setEnabled(false);
		m_findLabel = new JLabel();

		statContainer.add(m_gotoEntryContainer);
		statContainer.add(m_wordcountLabel);
		statContainer.add(m_findLabel);
		statContainer.add(m_findPane);
		statContainer.add(m_fuzzyProjLabel);
		
		///////////////////////////////////////////
		// create stat+oldSrc container
		Container statSrc = new Container();
		GridBagLayout gb = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		statSrc.setLayout(gb);

		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(3, 3, 3, 3);
		gb.setConstraints(statContainer, c);
		statSrc.add(statContainer);
		
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		gb.setConstraints(m_oldSrcPane, c);
		statSrc.add(m_oldSrcPane);
		
		///////////////////////////////////////////
		// create display
		Container cp = getContentPane();
		GridBagLayout gridbag = new GridBagLayout();
		c = new GridBagConstraints();
		cp.setLayout(gridbag);

		c.insets = new Insets(3, 3, 3, 3);
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 0.1;
		c.weighty = 0.4;
		c.fill = GridBagConstraints.BOTH;

		// stat container
		gridbag.setConstraints(statSrc, c);
		cp.add(statSrc);

		// context n-1
		c.weighty = 0.2;
		c.weightx = 1.9;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(context1Scroller, c);
		cp.add(context1Scroller);
		
		// old loc
		c.gridwidth = 1;
		c.weightx = 0.1;
		gridbag.setConstraints(oldLocScroller, c);
		cp.add(oldLocScroller);
		
		// new loc
		c.weightx = 1.9;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(m_xlPane, c);
		cp.add(m_xlPane);
		
		// glossary list
		c.weightx = 0.1;
		c.gridwidth = 1;
		c.weighty = 0.4;
		gridbag.setConstraints(matchScroller, c);
		cp.add(matchScroller);
	
		// context n+1 and n+2
		c.weightx = 1.9;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(context2Scroller, c);
		cp.add(context2Scroller);

		// project message label
		c.weightx = 1.0;
		c.weighty = 0.0;
		gridbag.setConstraints(m_statusLabel, c);
		cp.add(m_statusLabel);

		///////////////////////////////////////////////
		// preset some strings in the UI to describe what's what
		m_context1Pane.setText("Context segments (pre)");
		m_context2Pane.setText("Context segments (post)");
		m_oldSrcPane.setText("'Fuzzy' matched source text");
		m_oldLocPane.setText("Corresponding translation to 'fuzzy' match");
		m_xlPane.setText("Translation field");
		m_matchPane.setText("Glossary matches");

		m_projWin = new ProjectFrame(this);
	}

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

	public void fatalError(String msg, RuntimeException re)
	{
		System.out.println(msg);
		re.printStackTrace();
		System.exit(1);
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
				KeyEvent.VK_O, 
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
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
		m_miFileProjWin.addActionListener(this);
		m_mFile.add(m_miFileProjWin);
		
		m_mFile.addSeparator();

		m_miFileSave = new JMenuItem();
		m_miFileSave.addActionListener(this);
		m_miFileSave.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_S,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		m_mFile.add(m_miFileSave);

		m_miFileQuit = new JMenuItem();
		m_miFileQuit.addActionListener(this);
		m_mFile.addSeparator();
		m_miFileQuit.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_Q, 
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		m_mFile.add(m_miFileQuit);
		mb.add(m_mFile);

		// edit
		m_mEdit = new JMenu();
		m_miEditNext = new JMenuItem();
		m_miEditNext.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_N, 
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		m_miEditNext.addActionListener(this);
		m_mEdit.add(m_miEditNext);

		m_miEditPrev = new JMenuItem();
		m_miEditPrev.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_P, 
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		m_miEditPrev.addActionListener(this);
		m_mEdit.add(m_miEditPrev);

		m_mEdit.addSeparator();

		m_miEditRecycle = new JMenuItem();
		m_miEditRecycle.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_R, 
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		m_miEditRecycle.addActionListener(this);
		m_mEdit.add(m_miEditRecycle);

		m_mEdit.addSeparator();

		m_miEditFind = new JMenuItem();
		m_miEditFind.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_F, 
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		m_miEditFind.addActionListener(this);
		m_mEdit.add(m_miEditFind);

		m_miEditGoto = new JMenuItem();
		m_miEditGoto.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_G, 
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		m_miEditGoto.addActionListener(this);
		m_mEdit.add(m_miEditGoto);
		
		m_mEdit.addSeparator();

		m_miEditCompare1 = new JMenuItem();
		m_miEditCompare1.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_1, 
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		m_miEditCompare1.addActionListener(this);
		m_mEdit.add(m_miEditCompare1);

		m_miEditCompare2 = new JMenuItem();
		m_miEditCompare2.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_2, 
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		m_miEditCompare2.addActionListener(this);
		m_mEdit.add(m_miEditCompare2);

		m_miEditCompare3 = new JMenuItem();
		m_miEditCompare3.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_3, 
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		m_miEditCompare3.addActionListener(this);
		m_mEdit.add(m_miEditCompare3);

		m_miEditCompare4 = new JMenuItem();
		m_miEditCompare4.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_4, 
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		m_miEditCompare4.addActionListener(this);
		m_mEdit.add(m_miEditCompare4);

		m_miEditCompare5 = new JMenuItem();
		m_miEditCompare5.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_5, 
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		m_miEditCompare5.addActionListener(this);
		m_mEdit.add(m_miEditCompare5);

		mb.add(m_mEdit);

		// tools
		m_mTools = new JMenu();
		m_miToolsPseudoTrans = new JMenuItem();
		m_miToolsPseudoTrans.addActionListener(this);
		m_mTools.add(m_miToolsPseudoTrans);

		mb.add(m_mTools);
		
		m_mVersion = new JMenu();
		m_miVersionNumber = new JMenuItem();
		m_mVersion.add(m_miVersionNumber);

		mb.add(m_mVersion);

		setJMenuBar(mb);
	}

	protected void doPseudoTrans() {
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
		displayEntry(true);
	}


	public void doNextEntry()
	{
		if (m_projectLoaded == false)
			return;
		
		checkEntry();
		if (m_curEntry < 0)
			return;
		m_curEntry++;
		if (m_curEntry >= CommandThread.core.numEntries())
			m_curEntry = 0;
		displayEntry(true);
	}

	public void doPrevEntry()
	{
		if (m_projectLoaded == false)
			return;
		
		checkEntry();
		if (m_curEntry < 0)
			return;
		m_curEntry--;
		if (m_curEntry < 0)
			m_curEntry = CommandThread.core.numEntries() - 1;
		displayEntry(true);
	}

	public void doRecycleTrans()
	{
		if (m_projectLoaded == false)
			return;
		
		if (m_curNear == null)
			return;
		StringEntry se = m_curNear.str;
		String s = se.getTrans();
		if (s != null)
			m_xlPane.setText(s);
	}

	protected void doCopySourceText()
	{
		if (m_projectLoaded == false)
			return;
		
		m_xlPane.setText(m_eData.srcText);
	}

	protected void doCompareN(int n)
	{
		if (m_projectLoaded == false)
			return;
		
		if ((m_nearList != null) && (m_nearList.size() > n))
		{
			NearString ns = (NearString) m_nearList.get(n);
			m_nearListNum = n;
			m_curNear = ns;
			displayOldSrc(ns.score);
			displayOldLoc();
		}
	}

	protected void displayOldSrc(double score)
	{
		if (m_projectLoaded == false)
			return;
		
		String newStr = m_eData.srcText;
		String oldStr = m_curNear.str.getSrcText();
		byte[] newAttr = m_curNear.parAttr;
		byte[] oldAttr = m_curNear.attr;
		String proj = m_curNear.proj;

		LBuffer buf = new LBuffer(1024);
		Double sc = new Double(score * 100);

		buildFormattedString(oldStr, oldAttr, RED, buf);
		//buildFormattedString(oldStr, oldAttr, BLUE, buf);
		m_oldSrcPane.setText( (m_nearListNum+1) + ") " + buf.string() + 
				"<p><font color=\"red\">" + sc.intValue() + "% match</font>");
		m_oldSrcPane.setCaretPosition(0);

		if (proj == null)
			proj = OStrings.TF_FUZZY_CURRENT_PROJECT;
		m_fuzzyProjLabel.setText(proj);
	}

	protected void displayOldLoc()
	{
		if (m_projectLoaded == false)
			return;
		
		String oldStr = m_curNear.str.getTrans();
		m_oldLocPane.setText(controlifyHTML(oldStr));
		m_oldLocPane.setCaretPosition(0);
	}

	protected void buildFormattedString(String src, byte[] attrList,
						String uniqColor, LBuffer buf)
	{
		StringTokenizer st;
		String tok;
		byte attr;
		int ctr = 0;
		boolean uniq = false;	// red is new, blue is old
		boolean near = false;	// green
		boolean pair = false;	// underline
		boolean ulOverride = false;
		int pos = 0;
		int end = 0;

		st = new StringTokenizer(src);
		while (st.hasMoreTokens())
		{
			tok = st.nextToken();
			pos = src.indexOf(tok, end);
			buf.append(src.substring(end, pos));
			end = pos + tok.length();
			if (CommandThread.stripString(tok) == null)
			{
				buf.append(controlifyHTML(tok));
				continue;
			}
			attr = attrList[ctr++];

			if (((attr & StringData.UNIQ) != 0) && (uniq == false))
			{
				// uniq string segment starts here
				if (pair == true)
				{
					// imbedded underline - close
					buf.append("</u>");
					ulOverride = true;
				}
				if (near == true)
				{
					buf.append("</font>");
					near = false;
				}
				buf.append("<font color=\"#" +uniqColor +"\">");
				uniq = true;
			}
			else if ((uniq == true) && 
					((attr & StringData.UNIQ) == 0))
			{
				// uniq segment over - close it
				if (pair == true)
				{
					// imbedded underline - close
					buf.append("</u>");
					ulOverride = true;
				}
				buf.append("</font>");
				uniq = false;
			}

			if (((attr & StringData.NEAR) != 0) && (near == false))
			{
				// uniq string segment starts here
				if (pair == true)
				{
					// imbedded underline - close
					buf.append("</u>");
					ulOverride = true;
				}
				if (uniq == true)
				{
					buf.append("</font>");
					uniq = false;
				}
				buf.append("<font color=\"#" + GREEN + "\">");
				near = true;
			}
			else if ((near == true) && 
					((attr & StringData.NEAR) == 0))
			{
				// pair segment over - close it
				if (pair == true)
				{
					// imbedded underline - close
					buf.append("</u>");
					ulOverride = true;
				}
				buf.append("</font>");
				near = false;
			}

			if ((attr & StringData.PAIR) != 0)
			{
				if (pair == false)
				{
					buf.append("<u>");
					pair = true;
					ulOverride = false;
				}
				else if ((pair == true) && (ulOverride == true))
				{
					buf.append("<u>");
					ulOverride = false;
				}
			}
			else if (pair == true)
			{
				buf.append("</u>");
				pair = false;
			}

			buf.append(controlifyHTML(tok));
		}
		if (pair == true)
			buf.append("</u>");
		if ((uniq == true) || (near == true))
			buf.append("</font>");
		buf.append(src.substring(end, src.length()));
	}

	// if hilited text in m_xlPane, launch a find on that
	// otherwise, simply move the cursor
	protected void doStartFind()
	{
		int start = m_xlPane.getSelectionStart();
		int end = m_xlPane.getSelectionEnd();
		if ((end - start) >= 2)
		{
			String selection = m_xlPane.getSelectedText();
			selection.trim();
			if (selection.length() >= 2)
			{
				m_findPane.setText(selection);
				doFind(selection);
			}
		}
		else
		{
			// reset cursor to m_findPane
			m_findPane.requestFocus();
		}
	}
	
	protected void doFind(String str)
	{
		if (m_projectLoaded == false)
			return;
		
		TreeMap foundList = CommandThread.core.findAll(str);
		if (foundList == null)
		{
			String msg = "search for terms '" + str + 
					"' returned nothing";
			setMessageText(msg);
		}
		else
		{
			ContextFrame cf = new ContextFrame(this);
			cf.show();
			cf.displayStringList(foundList, str);
		}
	}

	// move focus to goto-entry component
	protected void doGoto()
	{
		m_gotoEntry.requestFocus();
	}

	public void doGotoEntry(String str)
	{
		if (m_projectLoaded == false)
			return;
		
		int num = -1;
		checkEntry();
		try
		{
			num = Integer.parseInt(str);
		}
		catch (NumberFormatException e)
		{
			num = -2;
		}
		if ((num < 1) || (num > m_numEntries))
		{
			m_gotoEntry.setText(String.valueOf(m_curEntry + 1));
		}
		else
		{
			m_curEntry = num - 1;
			displayEntry(true);
		}
	}

	protected void doSave()
	{
		if (m_projectLoaded == false)
			return;
		
		RequestPacket pack;
		pack = new RequestPacket(RequestPacket.SAVE, this);
		CommandThread.core.messageBoardPost(pack);
	}

	private void checkEntry()
	{
		// see if previous entry changed - if so, update core
		String s = m_xlPane.getText();
		
		// convert hard return to soft
		s = s.replace((char) 0x0a, (char) 0x8d);
		
		// only record translation if the field is not blank
		if (s.equals("") == false)
			CommandThread.core.setTranslation(m_curEntry, s);
	}

	protected void doLoadProject()
	{
		if (m_projectLoaded == false)
		{
			// TODO - unload current project
		}
		
		RequestPacket pack;
		pack = new RequestPacket(RequestPacket.LOAD, this);
		CommandThread.core.messageBoardPost(pack);
	}

	public void finishLoadProject()
	{
		m_numEntries = CommandThread.core.numEntries();
		m_activeProj = CommandThread.core.projName();
		m_activeFile = "";
		m_curEntry = 0;
		m_gotoEntry.setEnabled(true);
		m_findPane.setEnabled(true);
		m_projectLoaded = true;

		// this is called by another thread so try to avoid modifying
		//  the UI
		// force the displayEntry call seperately
		//displayEntry(true);
	}

	protected void updateMatchPane()
	{
		// prepare html table
		LBuffer buf = new LBuffer(1024);
		String s = "";
		ListIterator it;
		StringEntry strEntry;
		LinkedList ll;
	
		if (m_eData.getNearTermsSize() > 0)
		{
			NearString near;
			m_nearListNum = 0;
			m_nearList = m_eData.getNearTermsClone();
			int sz = m_eData.getNearTermsSize();
			if (sz > 0)
			{
				// at least one near term - display message
				Object[] obj = { new Integer(sz) };
				if (sz == 1)
					buf.append(MessageFormat.format(
							OStrings.TF_NUM_FUZZY_MATCH, obj));
				else
					buf.append(MessageFormat.format(
							OStrings.TF_NUM_FUZZY_MATCHES, obj));
				doCompareN(0);
			}
		}
		if (m_eData.glosTerms != null)
		{
			it = m_eData.glosTerms.listIterator(0);
			buf.append("<caption>");
			buf.append(OStrings.TF_GLOSSARY);
			buf.append("</caption>");
			buf.append("<table BORDER COLS=3 ");
			buf.append("WIDTH=\"100%\" NOSAVE>");
			while (it.hasNext())
			{
				strEntry = (StringEntry) it.next();
				buf.append("<tr><td><font size=-1>");

				s = strEntry.getSrcText();
				if (s != null)
				{
					s = controlifyHTML(s);
					buf.append(s);
				}
				buf.append("</font></td><td><font size=-1>");

				s = strEntry.getTrans();
				if (s != null)
				{
					s = controlifyHTML(s);
					buf.append(s);
				}
				buf.append("</font></td></tr>");
			}
			buf.append("</table>");
		}
		m_matchPane.setText(buf.string());
		m_matchPane.setCaretPosition(0);
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

	public synchronized void displayEntry(boolean updateXL)
	{
		JTextPane contextPane;
		m_nearList = null;
		m_nearListNum = -1;
		String s;
		m_curNear = null;
		m_eData = CommandThread.core.getEntry(m_curEntry);
		s = controlifyHTML(m_eData.srcText);
		m_oldSrcPane.setText("");
		m_oldLocPane.setText("");
		m_fuzzyProjLabel.setText("");
		if (m_eData.file.compareTo(m_activeFile) != 0)
		{
			m_activeFile = m_eData.file;
			doSetTitle();
		}
		// set word counts
		// don't display word counts for now - will be inaccurate when
		//  projects opened subsequent times.   TODO - fix this
		//Object[] obj1 = { new Integer(m_eData.currentWords), 
		//		new Integer(m_eData.partialWords),
		//		new Integer(m_eData.totalWords) };
		//s = OStrings.TF_NUM_WORDS;
		//s = MessageFormat.format(s, obj1);
		//m_wordcountLabel.setText(s);
		if (updateXL == true)
		{
			if ((m_eData.trans == null) || m_eData.trans.equals(""))
			{
				s = m_eData.srcText;
			}
			else
			{
				s = m_eData.trans;
			}
			// convert soft returns to hard for display
			s = s.replace((char) 0x8d, (char) 0x0a);
			m_xlPane.setText(s);
		}
		m_xlPane.setCaretPosition(0);
		if (m_numEntries > 0)
		{
			Object[] obj = { new Integer(m_curEntry + 1), 
					new Integer(m_numEntries) };
			String str = MessageFormat.format(m_curString, obj);
			m_entryNumPosition.setText(str);
		}

		// fill context fields.  make text to be translated red so
		//  to stand out in low context field
		// use hyperlinks to enable entry jumps
		ArrayList contextList = CommandThread.core.getContext(
			m_curEntry, m_contextLow, m_contextHigh);
		int i;
		int j;
		String context = "";
		int num;

		if ((m_eData.getNearTermsSize() > 0) || 
				(m_eData.glosTerms != null))
		{
			updateMatchPane();
		}
		else
			m_matchPane.setText("");
		
		context = "<table BORDER COLS=2 WODTH=\"100%\" NOSAVE>";
		for (i=0, j=-m_contextLow; i<m_contextLow; i++, j++)
		{
			s = (String) contextList.get(i);
			if ((s != null) && (s.equals("") == false))
			{
				num = m_curEntry + 1 + j;
				context += "<tr><td><a href=\"" + num + "\">" 
										+ num + "</a></td>";
				context += "<td>" + controlifyHTML(s) + "</td></tr>";
			}
		}
		num = m_curEntry + 1;
		context += "<tr><td><font color=\"blue\"><bold>" + num + 
						"</bold></font></td>";
		context += "<td><font color=\"blue\">";
		LBuffer buf = new LBuffer(256);
		if (m_curNear == null)
		{
			context += controlifyHTML(m_eData.srcText);
		}
		else
		{
			buildFormattedString(m_eData.srcText, m_curNear.parAttr, RED, buf);
			context += buf.string();
		}
		context += "</font></td></tr></table>";
		m_context1Pane.setText(context);

		context = "<table BORDER COLS=2 WODTH=\"100%\" NOSAVE>";
		for (j++; i<m_contextHigh+m_contextLow; i++, j++)
		{
			s = (String) contextList.get(i);
			if (s != null)
			{
				num = m_curEntry + 1 + j;
				context += "<tr><td><a href=\"" + num + "\">" 
										+ num + "</a></td>";
				context += "<td>" + controlifyHTML(s) + "</td></tr>";
			}
		}
		context += "</table>";
		m_context2Pane.setText(context);
		m_context2Pane.setCaretPosition(0);

	}

	// convert & and < to HTML codes
	protected static String controlifyHTML(String text)
	{
		int endPos = 0;
		int startPos = 0;
		String s = text;
		String s1 = "";
		while ((endPos = s.indexOf('&', startPos)) >= 0)
		{
			s1 += s.substring(startPos, endPos);
			s1 += "&amp;";
			startPos = endPos + 1;
		}
		s1 += s.substring(startPos, s.length());
		s = s1;
		s1 = "";
		startPos = 0;
		while ((endPos = s.indexOf('<', startPos)) >= 0)
		{
			s1 += s.substring(startPos, endPos);
			s1 += "&lt;";
			startPos = endPos + 1;
		}
		s1 += s.substring(startPos, s.length());
		return s1;
	}

//	protected void buildNearHTML(NearString nearString, 
//							int num, LBuffer buf)
//	{
//		String s;
//		StringEntry strEntry = nearString.str;
//		double score = nearString.score;
//
//		s = strEntry.getTrans();
//		if (s == null)
//			s = strEntry.getSrcText();
//		s = controlifyHTML(s);
//
//		buf.append("<tr><td><font size=-1>");
//		buf.appendInt(num);
//		buf.append("</font></td><td><font size=-1>");
//		buf.append(s);
//		buf.append("</font></td><td><font size=-1>");
//		buf.appendInt((int) (score * 100.0));
//		buf.append("%</font></td></tr>");
//	}

	public void setMessageText(String str)
	{
		m_statusLabel.setText(str);
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
			else if (evtSrc == m_miEditGoto)
			{
				doGoto();
			}
			else if (evtSrc == m_miEditFind)
			{
				// doFind initiates the search - the menu option
				//  will reset the cursor to find field or copy hilited 
				//  text to find window then launch find
				doStartFind();
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
			else if (evtSrc == m_miToolsPseudoTrans)
			{
				doPseudoTrans();
			}
		}
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

		m_findLabel.setText(OStrings.TF_SEARCH);
		int blockHeight = m_findLabel.getPreferredSize().height;
		m_statusLabel.setMinimumSize(new Dimension(m_leftX, blockHeight));
		m_curString = OStrings.TF_CUR_STRING;
		str = OStrings.TF_GOTO_ENTRY;

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
		m_miEditGoto.setText(OStrings.TF_MENU_EDIT_GOTO);
		m_miEditRecycle.setText(OStrings.TF_MENU_EDIT_RECYCLE);
		m_miEditFind.setText(OStrings.TF_MENU_EDIT_FIND);

		m_mTools.setText(OStrings.TF_MENU_TOOLS);
		m_miToolsPseudoTrans.setText(OStrings.TF_MENU_TOOLS_PSEUDO);

		m_mVersion.setText(OStrings.TF_MENU_VERSION);
		m_miVersionNumber.setText(OmegaTVersion.name());
	}

	protected void processKeyEvent(KeyEvent e)
	{
		super.processKeyEvent(e);
		if (e.getKeyCode() == KeyEvent.VK_ENTER)
		{
			if (m_gotoEntry.hasFocus() == true)
			{
				doGotoEntry(m_gotoEntry.getText());
				m_xlPane.requestFocus();
			}
			else if (m_findPane.hasFocus() == true)
			{
				doFind(m_findPane.getText());
				m_xlPane.requestFocus();
			}
		}
	}

	class MSelectLanguage extends JDialog 
	{
		public MSelectLanguage(JFrame par, LinkedList langs)
		{
			super(par, true);
			setSize(400, 240);

			// create the list
			String[] arr = new String[langs.size()];
			ListIterator it = langs.listIterator();
			int ctr = 0;
			while(it.hasNext())
				arr[ctr++] = (String) it.next();
			m_list = new JList(arr);
			JScrollPane sp = new JScrollPane(m_list);

			m_selectLangLabel = new JLabel();
			m_okButton = new JButton();
			m_cancelButton = new JButton();

			getContentPane().add(m_selectLangLabel, "East");
			getContentPane().add(sp, "West");
			Box b2 = Box.createHorizontalBox();
			b2.add(Box.createHorizontalGlue());
			b2.add(m_cancelButton);
			b2.add(Box.createHorizontalStrut(5));
			b2.add(m_okButton);
			getContentPane().add(b2, "South");

			m_okButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					doOK();
				}
			});

			m_cancelButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					doCancel();
				}
			});

			String str;
			setTitle(OStrings.TF_SELECT_LANGUAGE_TITLE);
	
			m_selectLangLabel.setText(OStrings.TF_SELECT_LANGUAGE);
	
			m_okButton.setText(OStrings.PP_BUTTON_OK);

			m_cancelButton.setText(OStrings.PP_BUTTON_CANCEL);
		}

		private void doOK()
		{
			dispose();
		}

		private void doCancel()
		{
			dispose();
		}
				
		public JLabel		m_selectLangLabel;
		public JButton		m_okButton;
		public JButton		m_cancelButton;
		public JList		m_list;
	}

	class MTField extends JTextPane
//	class MTField extends JTextArea
//	class MTField extends JTextField
	{
		public MTField(TransFrame tf, int x)
		{
			super();
			m_tf = tf;
			m_pos = x;
		}
	
		public TransFrame getTransFrame()	{ return m_tf;	}
		public int getPos()			{ return m_pos;	}
	
		private TransFrame	m_tf;
		private int 		m_pos;
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
		protected void processKeyEvent(KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_ENTER)  
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

			}
			else
			{
				super.processKeyEvent(e);
			}
		}
	}
	
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
	private JMenuItem	m_miEditGoto;
	private JMenuItem	m_miEditFind;
	private JMenuItem	m_miEditRecycle;
	private JMenuItem	m_miEditCompare1;
	private JMenuItem	m_miEditCompare2;
	private JMenuItem	m_miEditCompare3;
	private JMenuItem	m_miEditCompare4;
	private JMenuItem	m_miEditCompare5;
	private JMenu		m_mTools;
	private JMenuItem	m_miToolsPseudoTrans;

	private JMenu		m_mVersion;
	private JMenuItem	m_miVersionNumber;

	private JTextPane	m_matchPane;
	private JTextPane	m_oldSrcPane;
	private JTextPane	m_oldLocPane;
	private XLPane		m_xlPane;
	private JTextPane	m_context1Pane;
	private JTextPane	m_context2Pane;
	private int		m_contextLow = 4;
	private int		m_contextHigh = 4;
//	private ArrayList	m_contextArray;	// stores JTextPanes
//	private ArrayList	m_contextArrayStrings;
	private LinkedList	m_nearList;
	private int		m_nearListNum;

	private JTextField	m_findPane;
	private JLabel		m_findLabel;
	private JLabel		m_statusLabel;
	private JLabel		m_fuzzyProjLabel;
	private JLabel		m_wordcountLabel;
	private JLabel		m_entryNumPosition;
	private String		m_curString;
	private JTextField	m_gotoEntry;
	private Container	m_gotoEntryContainer;

	private String	m_activeFile;
	private String	m_activeProj;
	private int m_curEntry;
	private NearString m_curNear;
	private int m_numEntries;

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

	private TreeMap m_langMap; // user interface languages
	private EntryData m_eData;

	private int	m_leftX = 350;

///////////////////////////////////////////////////////////////

	public static void main(String[] args)
	{
		JFrame f = new TransFrame();
		f.show();
	}
}

