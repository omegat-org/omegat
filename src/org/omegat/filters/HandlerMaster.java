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

import org.omegat.filters.html.HTMLFileHandler;
import org.omegat.filters.text.TabFileHandler;
import org.omegat.filters.text.TextFileHandler;
import org.omegat.filters.text.bundles.ResourceBundleFileHandler;
import org.omegat.filters.xml.openoffice.OOFileHandler;
import org.omegat.filters.xml.xhtml.XHTMLFileHandler;

import java.util.ArrayList;

/**
 * A master class that registers and handles all the filters.
 * Singleton - there can be only one instance of this class.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 */
public class HandlerMaster
{
	private static HandlerMaster instance = null;
	public static HandlerMaster getInstance()
	{
		if( instance==null )
			instance = new HandlerMaster();
		return instance;				
	}
	
	private HandlerMaster()
	{
		m_handlerList = new ArrayList();
		setupDefaultHandlers();
	}

	private void addHandler(FileHandler hand)
	{
		m_handlerList.add(hand);
	}

    public FileHandler findPreferredHandler(String ext)
	{
		FileHandler fh = null;
		int i;
		if (ext != null)
		{
			for (i=0; i<m_handlerList.size(); i++)
			{
				fh = (FileHandler) m_handlerList.get(i);
				if (ext.compareToIgnoreCase(
						fh.preferredExtension()) == 0)
				{
					break;
				}
			}
			if (i >= m_handlerList.size())
			{
				fh = null;
			}
		}
		return fh;
	}

	private void setupDefaultHandlers()
	{
		addHandler(new TabFileHandler());
		addHandler(new TextFileHandler(TextFileHandler.TYPE_LATIN1, "txt1"));	// NOI18N
		addHandler(new TextFileHandler(TextFileHandler.TYPE_LATIN2, "txt2"));	// NOI18N
		addHandler(new TextFileHandler(TextFileHandler.TYPE_UTF8, "utf8"));		// NOI18N
		addHandler(new HTMLFileHandler());
		addHandler(new XHTMLFileHandler());
		addHandler(new OOFileHandler());
		addHandler(new ResourceBundleFileHandler());
	}

    private ArrayList	m_handlerList;

////////////////////////////////////////////////////////////

}
