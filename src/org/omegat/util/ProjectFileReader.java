/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2005  Keith Godfrey et al
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

package org.omegat.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.ArrayList;

import org.omegat.filters2.TranslationException;
import org.omegat.filters2.xml.XMLBlock;
import org.omegat.filters2.xml.XMLStreamReader;

/**
 * Class that reads project definition file and returns project's properties.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 */
public class ProjectFileReader
{
	public ProjectFileReader()
	{
		m_reader = new XMLStreamReader();
		m_reader.killEmptyBlocks();
		reset();
	}

	public void loadProjectFile(String filename)
		throws IOException, TranslationException
	{
		m_reader.setStream(filename, "UTF-8");									// NOI18N
		
		// verify valid project file
		XMLBlock blk;
		ArrayList lst;

		// advance to omegat tag
		if (m_reader.advanceToTag("omegat") == null)					// NOI18N
			return;
		
		// advance to project tag
		if ((blk=m_reader.advanceToTag("project")) == null)						// NOI18N
			return;

		String ver = blk.getAttribute("version");								// NOI18N
		if (ver != null && !ver.equals(OConsts.PROJ_CUR_VERSION))
		{
			throw new TranslationException(
                    MessageFormat.format(OStrings.getString("PFR_ERROR_UNSUPPORTED_PROJECT_VERSION"), 
                    new Object[]{ver}));
		}
		
		// if folder is in default locations, name stored as __DEFAULT__
		m_root = filename.substring(0, 
					filename.lastIndexOf(File.separator)) + File.separator;

		lst = m_reader.closeBlock(blk);
		if (lst == null)
			return;

		for (int i=0; i<lst.size(); i++)
		{
			blk = (XMLBlock) lst.get(i);
			if (blk.isClose())
				continue;

			if (blk.getTagName().equals("target_dir"))							// NOI18N
			{
				if (++i >= lst.size())
					break;
				blk = (XMLBlock) lst.get(i);
				m_target = translateBlock(blk, OConsts.DEFAULT_LOC);
			}
			else if (blk.getTagName().equals("source_dir"))						// NOI18N
			{
				if (++i >= lst.size())
					break;
				blk = (XMLBlock) lst.get(i);
				m_source = translateBlock(blk, OConsts.DEFAULT_SRC);
			}
			else if (blk.getTagName().equals("tm_dir"))							// NOI18N
			{
				if (++i >= lst.size())
					break;
				blk = (XMLBlock) lst.get(i);
				m_tm = translateBlock(blk, OConsts.DEFAULT_TM);
			}
			else if (blk.getTagName().equals("glossary_dir"))					// NOI18N
			{
				if (++i >= lst.size())
					break;
				blk = (XMLBlock) lst.get(i);
				m_glossary = translateBlock(blk, OConsts.DEFAULT_GLOS);
			}
			else if (blk.getTagName().equals("source_lang"))					// NOI18N
			{
				if (++i >= lst.size())
					break;
				blk = (XMLBlock) lst.get(i);
				if (blk != null)
					m_sourceLocale = blk.getText();
			}
			else if (blk.getTagName().equals("target_lang"))					// NOI18N
			{
				if (++i >= lst.size())
					break;
				blk = (XMLBlock) lst.get(i);
				if (blk != null)
					m_targetLocale = blk.getText();
			}
			else if (blk.getTagName().equals("sentence_seg"))					// NOI18N
			{
				if (++i >= lst.size())
					break;
				blk = (XMLBlock) lst.get(i);
				if (blk != null)
					m_sentenceSeg = blk.getText();
			}
		}
    }

	private String translateBlock(XMLBlock blk, String def)
	{
		if (blk == null)
			return "";															// NOI18N
		else if (blk.getText().equals(OConsts.DEFAULT_FOLDER_MARKER))
			return m_root + def + File.separator;
		else
			return blk.getText();
	}

	public void writeProjectFile(String filename) throws IOException
	{
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filename), "UTF-8"));                      // NOI18N
		String str;

		// if folders are in default locations, store name as 
		//  __DEFAULT__
		String root = filename.substring(0, 
					filename.lastIndexOf(File.separator));
		root += File.separator;

		String source;														// NOI18N
		String target;														// NOI18N
		String tm;															// NOI18N
		String glossary;													// NOI18N
		if (m_source.equals(root + OConsts.DEFAULT_SRC + File.separator))
			source = OConsts.DEFAULT_FOLDER_MARKER;
		else
			source = m_source;
		
		if (m_target.equals(root + OConsts.DEFAULT_LOC + File.separator))
			target = OConsts.DEFAULT_FOLDER_MARKER;
		else
			target = m_target;
		
		if (m_glossary.equals(root + OConsts.DEFAULT_GLOS + File.separator))
			glossary = OConsts.DEFAULT_FOLDER_MARKER;
		else
			glossary = m_glossary;
		
		if (m_tm.equals(root + OConsts.DEFAULT_TM + File.separator))
			tm = OConsts.DEFAULT_FOLDER_MARKER;
		else
			tm = m_tm;
		
		str = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";					// NOI18N
		str += "<omegat>\n";													// NOI18N
		str += "  <project version=\"1.0\">\n";									// NOI18N
		str += "    <source_dir>" + source + "</source_dir>\n";					// NOI18N
		str += "    <target_dir>" + target + "</target_dir>\n";					// NOI18N
		str += "    <tm_dir>" + tm + "</tm_dir>\n";								// NOI18N
		str += "    <glossary_dir>" + glossary + "</glossary_dir>\n";			// NOI18N
		str += "    <source_lang>" + m_sourceLocale + "</source_lang>\n";			// NOI18N
		str += "    <target_lang>" + m_targetLocale + "</target_lang>\n";			// NOI18N
		str += "    <sentence_seg>" + m_sentenceSeg + "</sentence_seg>\n";			// NOI18N
		str += "  </project>\n";												// NOI18N
		str += "</omegat>\n";													// NOI18N

		out.write(str, 0, str.length());
		out.flush();
	}
	
	private void reset()
	{
		m_target = "";															// NOI18N
		m_source = "";															// NOI18N
		m_tm = "";																// NOI18N
		m_glossary = "";														// NOI18N
		m_sourceLocale = "";															// NOI18N
		m_targetLocale = "";															// NOI18N
		m_root = "";															// NOI18N
	}
	
	public void	setTarget(String x)		{ m_target = x;		}
	public void	setSource(String x)		{ m_source = x;		}
	public void	setTM(String x)			{ m_tm = x;			}
	public void	setGlossary(String x)	{ m_glossary = x;	}

	public void setSourceLang(String x)	{ m_sourceLocale = x;	}
	public void setTargetLang(String x)	{ m_targetLocale = x;	}
    

	public String getTarget()		{ return m_target;		}
	public String getSource()		{ return m_source;		}
	public String getTM()			{ return m_tm;			}
	public String getGlossary()		{ return m_glossary;	}
	public String getSourceLang()	{ return m_sourceLocale;		}
	public String getTargetLang()	{ return m_targetLocale;		}

    public String getSentenceSeg(){ return m_sentenceSeg; }
    public void setSentenceSeg(String x){ m_sentenceSeg = x; }
    
	private XMLStreamReader		m_reader;
	private String		m_target;
	private String		m_source;
	private String		m_tm;
	private String		m_glossary;

    private String		m_targetLocale;
	private String		m_sourceLocale;
    
	private String		m_sentenceSeg;

	private String		m_root;

}


