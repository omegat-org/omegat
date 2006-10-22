/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/omegat/omegat.html
               Support center: http://groups.yahoo.com/group/OmegaT/

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
import org.omegat.util.xml.XMLBlock;
import org.omegat.util.xml.XMLStreamReader;

/**
 * Class that reads project definition file, returns project's properties,
 * and can save projects.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 */
public class ProjectFileStorage
{
	public ProjectFileStorage()
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
				m_target = computeAbsolutePath(blk.getText(), OConsts.DEFAULT_TARGET);
			}
			else if (blk.getTagName().equals("source_dir"))						// NOI18N
			{
				if (++i >= lst.size())
					break;
				blk = (XMLBlock) lst.get(i);
				m_source = computeAbsolutePath(blk.getText(), OConsts.DEFAULT_SOURCE);
			}
			else if (blk.getTagName().equals("tm_dir"))							// NOI18N
			{
				if (++i >= lst.size())
					break;
				blk = (XMLBlock) lst.get(i);
				m_tm = computeAbsolutePath(blk.getText(), OConsts.DEFAULT_TM);
			}
			else if (blk.getTagName().equals("glossary_dir"))					// NOI18N
			{
				if (++i >= lst.size())
					break;
				blk = (XMLBlock) lst.get(i);
				m_glossary = computeAbsolutePath(blk.getText(), OConsts.DEFAULT_GLOSSARY);
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

    /**
     * Returns absolute path for any project's folder.
     * Since 1.6.0 supports relative paths (RFE 1111956).
     *
     * @param relativePath relative path from project file.
     * @param defaultName  default name for such a project's folder, if relativePath is "__DEFAULT__".
     */
	private String computeAbsolutePath(String relativePath, String defaultName)
	{
		if (OConsts.DEFAULT_FOLDER_MARKER.equals(relativePath))
			return m_root + defaultName + File.separator;
		else
        {
            try
            {
                // check if path starts with a system root
                File[] roots = File.listRoots();
                for (int i = 0; i < roots.length; i++) {
                    if (relativePath.startsWith(roots[i].getCanonicalPath()))
                        // path starts with a root --> path is already absolute
                        return new File(relativePath).getCanonicalPath() + File.separator;
                }

                // path does not start with a system root --> relative to project root
                return new File(m_root, relativePath).getCanonicalPath() + File.separator;
            }
            catch (IOException e)
            {
                return relativePath;
            }
        }
	}

    /**
     * Returns relative path for any project's folder.
     * If absolutePath has default location, returns "__DEFAULT__".
     *
     * @param absolutePath absolute path to project folder.
     * @param defaultName  default name for such a project's folder.
     * @since 1.6.0
     */
	private String computeRelativePath(String absolutePath, String defaultName)
	{
        if (absolutePath.equals(m_root + defaultName + File.separator))
            return OConsts.DEFAULT_FOLDER_MARKER;
        
        try
        {
            // trying to look two folders up
            String res = absolutePath;
            File abs = new File(absolutePath).getCanonicalFile();
            File root = new File(m_root).getCanonicalFile();
            String prefix = new String();
            for (int i=0; i<2; i++)
            {
                if (abs.getPath().startsWith(root.getPath()))
                {
                    res = prefix + abs.getPath().substring(root.getPath().length());
                    if (res.startsWith(File.separator))
                        res = res.substring(1);
                    break;
                }
                else
                {
                    root = root.getParentFile();
                    prefix+= File.separator + "..";                             // NOI18N
                }
            }
            return res.replace(File.separatorChar, '/');
        }
        catch (IOException e)
        {
            return absolutePath.replace(File.separatorChar, '/');
        }
    }

    /**
     * Saves project file to disk.
     */
	public void writeProjectFile(String filename) throws IOException
	{
		m_root = filename.substring(0, filename.lastIndexOf(File.separator));
		m_root += File.separator;

		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filename), OConsts.UTF8));
		out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");             // NOI18N
		out.write("<omegat>\n");                                                // NOI18N
		out.write("  <project version=\"1.0\">\n");                             // NOI18N
		out.write("    <source_dir>" +                                          // NOI18N
                computeRelativePath(m_source, OConsts.DEFAULT_SOURCE) + 
                "</source_dir>\n");                                             // NOI18N
		out.write("    <target_dir>" +                                          // NOI18N
                computeRelativePath(m_target, OConsts.DEFAULT_TARGET) + 
                "</target_dir>\n");                                             // NOI18N
		out.write("    <tm_dir>" +                                              // NOI18N
                computeRelativePath(m_tm, OConsts.DEFAULT_TM) + 
                "</tm_dir>\n");                                                 // NOI18N
		out.write("    <glossary_dir>" +                                        // NOI18N
                computeRelativePath(m_glossary, OConsts.DEFAULT_GLOSSARY) + 
                "</glossary_dir>\n");                                           // NOI18N
		out.write("    <source_lang>" + m_sourceLocale + "</source_lang>\n");   // NOI18N
		out.write("    <target_lang>" + m_targetLocale + "</target_lang>\n");   // NOI18N
		out.write("    <sentence_seg>" + m_sentenceSeg + "</sentence_seg>\n");  // NOI18N
		out.write("  </project>\n");                                            // NOI18N
		out.write("</omegat>\n");                                               // NOI18N
		out.close();
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


