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

import org.omegat.gui.messages.MessageRelay;
import org.omegat.util.OStrings;
import org.omegat.core.SourceTextEntry;
import org.omegat.core.StringEntry;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.*;

public class ContextFrame extends JFrame
{
	public ContextFrame(TransFrame parent, boolean srcLang, boolean grabFocus)
	{
		String str;
		m_parent = parent;
		m_srcLang = srcLang;

		Container cp = getContentPane();
		m_editorPane = new JEditorPane();
		m_editorPane.setEditable(false);
		JScrollPane scroller = new JScrollPane(m_editorPane);
		cp.add(scroller, "Center");	// NOI18N

		m_closeButton = new JButton();
		m_closeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				doClose();
			}
		});
		Box bbut = Box.createHorizontalBox();
		bbut.add(Box.createHorizontalGlue());
		bbut.add(m_closeButton);
		bbut.add(Box.createHorizontalGlue());
		cp.add(bbut, "South");	// NOI18N

		setSize(500, 400);
		m_editorPane.addHyperlinkListener(new HListener(m_parent, grabFocus));

		// this only seems to work in 1.4, but at least it works there
		// throws exceptions in 1.2
		{
			KeyStroke escKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,
						0, false);
			Action escAction = new AbstractAction()
			{
				public void actionPerformed(ActionEvent e)
				{
					doClose();
				}
			};
			getRootPane().getInputMap(JComponent.WHEN_FOCUSED).put(
					escKeyStroke, "ESCAPE");						// NOI18N
			getRootPane().getActionMap().put("ESCAPE", escAction);	// NOI18N
		}
		
		updateUIText();
	}

	public void updateUIText()
	{
		m_closeButton.setText(OStrings.CF_BUTTON_CLOSE);
		if (m_srcLang)
			m_searchResults = OStrings.CF_SEARCH_RESULTS_SRC;
		else
			m_searchResults = OStrings.CF_SEARCH_RESULTS_LOC;
	}

	public void doClose()
	{
		dispose();
	}

	class QueryComparator implements Comparator
	{
		public int compare(Object o1, Object o2)
		{
			int q1 = ((QueryData) o1).entryNum;
			int q2 = ((QueryData) o2).entryNum;
			return (q1 - q2);
		}
	}

	class QueryData 
	{
		public String src;
		public String xl;
		public int entryNum;
	}
	
	public void displayStringList(ArrayList stringList, String searchTerms)
	{
		Object obj;
		setTitle(m_searchResults + " " + searchTerms);			// NOI18N
		String out;
		String src;
		String trans;
		SourceTextEntry ste;
		StringEntry se;

		out = "<table BORDER COLS=3 WIDTH=\"100%\" NOSAVE>";		// NOI18N
		for (int i=0; i<stringList.size(); i++)
		{
			ste = (SourceTextEntry) stringList.get(i);
			se = ste.getStrEntry();
			src = se.getSrcText();
			trans = se.getTrans();
			if ((src.equals("") == false) && (trans.equals("") == false))		// NOI18N
			{
				out += "<tr>";													// NOI18N
				out += "<td><a href=\"" + (ste.entryNum()+ 1) + "\">";			// NOI18N
				out += (ste.entryNum() + 1) + " </a></td>";						// NOI18N
				out += "<td>" + src + "</td>";									// NOI18N
				out += "<td>" + trans + "</td>";								// NOI18N
				out += "</tr>";													// NOI18N
			}
		}
		out += "</table>";														// NOI18N
		m_editorPane.setContentType("text/html");								// NOI18N
		m_editorPane.setText(out);
	}

	public void displayStringList(TreeMap stringList, String searchTerms)
	{
		
		Object obj;
		setTitle(m_searchResults + " " + searchTerms);							// NOI18N
		String out;
		String s;
		int entryNum;
		int ps;
		ListIterator it;
		String xl;
		TreeMap list = (TreeMap) stringList.clone();
		TreeMap queryMap = new TreeMap(new QueryComparator());
		LinkedList parentList;
		StringEntry strEntry;
		SourceTextEntry srcTextEntry;
		QueryData qd;
		while (list.size() > 0)
		{
			obj = list.firstKey();
			strEntry = (StringEntry) list.remove(obj);
			if (strEntry == null)
			{
				continue;
			}
			parentList = strEntry.getParentList();
			if (parentList == null)
			{
				continue;
			}

			it = parentList.listIterator();
			s = strEntry.getSrcText();
			while (it.hasNext())
			{
				srcTextEntry = (SourceTextEntry) it.next();
				qd = new QueryData();
				qd.src = strEntry.getSrcText();
				qd.xl = strEntry.getTrans();
				qd.entryNum = srcTextEntry.entryNum() + 1;
				queryMap.put(qd, qd);
			}
		}

		out = "<table BORDER COLS=3 WIDTH=\"100%\" NOSAVE>";					// NOI18N
		while (queryMap.size() > 0)
		{
			obj = queryMap.firstKey();
			queryMap.remove(obj);
			qd = (QueryData) obj;
			{
				out += "<tr>";													// NOI18N
				out += "<td><a href=\"" + qd.entryNum + "\">";					// NOI18N
				out += qd.entryNum + "</a></td>";								// NOI18N
				out += "<td>" + qd.src + " </td>";								// NOI18N
				if (qd.xl == null)
					s = "";														// NOI18N
				else 
					s = qd.xl;
				out += "<td>" + s + "</td>";									// NOI18N
				out += "</tr>";													// NOI18N
			}
		}
		out += "</table>";														// NOI18N
		m_editorPane.setContentType("text/html");								// NOI18N
		m_editorPane.setText(out);
	}
	
	private JEditorPane m_editorPane;
	private JButton		m_closeButton;
	private String m_searchResults;
	private boolean	m_srcLang;

	private TransFrame m_parent;

}

