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

import org.omegat.gui.threads.CommandThread;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.ProjectFileReader;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.text.ParseException;

/**
 * Creates a dialog where project properties are entered and/or modified
 *
 * @author Keith Godfrey
 */
public class ProjectProperties extends JFrame
{
	public String getLocRoot()		{ return m_locRoot;		}
	public String getGlossaryRoot()	{ return m_glosRoot;	}
	public String getTMRoot()		{ return m_tmRoot;		}
	public String getSrcLang()		{ return m_srcLang;	}
	public String getLocLang()		{ return m_locLang;	}
    public String getProjectName() {        return m_projName;    }
    public String getProjectFile() {        return m_projFile;    }
    public String getProjectRoot() {        return m_projRoot;    }
    public String getProjectInternal() {    return m_projInternal;    }
    public String getSourceRoot() {         return m_srcRoot;    }
    public void setSrcLang(String m_srcLang) {        this.m_srcLang = m_srcLang;    }
    public void setLocLang(String m_locLang) {        this.m_locLang = m_locLang;    }
    public void setProjectName(String m_projName) {   this.m_projName = m_projName;  }
    public void setProjectFile(String m_projFile) {   this.m_projFile = m_projFile;  }
    public void setProjectRoot(String m_projRoot) {   this.m_projRoot = m_projRoot;  }
    public void setProjectInternal(String m_projInternal) {    this.m_projInternal = m_projInternal;   }
    public void setSourceRoot(String m_srcRoot) {     this.m_srcRoot = m_srcRoot;    }
    public void setLocRoot(String m_locRoot) {        this.m_locRoot = m_locRoot;    }
    public void setGlossaryRoot(String m_glosRoot) {  this.m_glosRoot = m_glosRoot;  }
    public void setTMRoot(String m_tmRoot) {          this.m_tmRoot = m_tmRoot;      }

	public void reset()
	{
		setProjectFile("");	// NOI18N
		setProjectName("");	// NOI18N
		setProjectRoot("");	// NOI18N
		setProjectInternal("");	// NOI18N
		setSourceRoot("");	// NOI18N
		setLocRoot("");	// NOI18N
		setGlossaryRoot("");	// NOI18N
		setTMRoot("");	// NOI18N
		setSrcLang("");	// NOI18N
		setLocLang("");	// NOI18N
	}

    class OTFileFilter extends javax.swing.filechooser.FileFilter
	{
		public String getDescription()
		{
			return OStrings.getString("PP_PROJECT_FILES_DESC");
		}
		
		public boolean accept(File f)
		{
			if (f.getName().endsWith(OConsts.PROJ_EXTENSION) == true)
				return true;
			else if (f.isDirectory() == true)
				return true;
			else
				return false;
		}
	}

	public boolean loadExisting() throws IOException, InterruptedIOException
	{
		reset();

		// select existing project file - open it
		String curDir = CommandThread.core.getPreference(OConsts.PREF_CUR_DIR);
		//JFileChooser pfc = new org.omegat.gui.ProjectFileChooser(curDir);
		JFileChooser pfc = new JFileChooser(curDir);
		pfc.setFileFilter(new OTFileFilter());
		pfc.setFileView(new ProjectFileView());
		int res = pfc.showOpenDialog(this);
		if (res == JFileChooser.CANCEL_OPTION)
			throw new InterruptedIOException();

		CommandThread.core.setPreference(OConsts.PREF_CUR_DIR, 
							pfc.getSelectedFile().getParent());
		try 
		{
			ProjectFileReader pfr = new ProjectFileReader();
			setProjectName(pfc.getCurrentDirectory().getName());
			setProjectRoot(pfc.getCurrentDirectory().getAbsolutePath()
						+ File.separator);
			pfr.loadProjectFile(getProjectRoot() + OConsts.PROJ_FILENAME);

			setSourceRoot(pfr.getSource());
			setLocRoot(pfr.getTarget());
			setGlossaryRoot(pfr.getGlossary());
			setTMRoot(pfr.getTM());
			setProjectInternal(getProjectRoot() + OConsts.DEFAULT_INTERNAL
						+ File.separator);
			setSrcLang(pfr.getSourceLang());
			setLocLang(pfr.getTargetLang());
			CommandThread.core.setPreference(OConsts.PREF_SRCLANG, getSrcLang());
			CommandThread.core.setPreference(OConsts.PREF_LOCLANG, getLocLang());
			setProjectFile(getProjectRoot() + OConsts.PROJ_FILENAME);

			res = verifyProject();
			if (res != 0)
			{
				// something wrong with the project - display open dialog
				//  to fix it
				NewProjectDialog prj = new NewProjectDialog(this, this, getProjectFile(), res);

				// continue  until user fixes problem or cancels
				boolean abort = false;
				while (true)
				{
					prj.setVisible(true);
					if (m_dialogOK == false)
					{
						abort = true;
						break;
					}
					if ((res = verifyProject()) != 0)
					{
						prj.setMessageCode(res);
					}
					else
					{
						buildProjFile();
						break;
					}
				}
				prj.dispose();
				if (abort == true)
				{
					reset();
					return false;
				}
			}
			return true;
		}
		catch (ParseException e)
		{
			reset();
			throw new IOException(OStrings.getString("PP_ERROR_UNABLE_TO_READ_PROJECT_FILE") + e);
		}
	}

	// returns 0 if project OK, 1 if language codes off, 2 if directories off
	protected int verifyProject() throws IOException
	{
		// now see if these directories are where they're suposed to be
		File src = new File(getSourceRoot());
		File loc = new File(getLocRoot());
		File gls = new File(getGlossaryRoot());
		File tmx = new File(getTMRoot());

		if (verifyLangCodes() == false)
			return 1;
		
		if (src.exists() && loc.exists() && gls.exists() && tmx.exists())
			return 0;

		return 2;
	}

	// make sure language codes are of the variety XX_XX, XX-XX or XX
	protected boolean verifyLangCodes()
	{
		if (verifySingleLangCode(getSrcLang()) == false)
			return false;
		if (verifySingleLangCode(getLocLang()) == false)
			return false;

		return true;
	}

	public static boolean verifySingleLangCode(String code)
	{
		if (code.length() == 2)
		{
			// make sure both values are characters
			if (Character.isLetter(code.charAt(0)) && 
					Character.isLetter(code.charAt(1)))
			{
				// looks good
				return true;
			}
		}
		else if (code.length() == 5)
		{
			// make sure both values are characters
			if (Character.isLetter(code.charAt(0))	&& 
				Character.isLetter(code.charAt(1))  &&
				Character.isLetter(code.charAt(3))  &&
				Character.isLetter(code.charAt(4)))
			{
				char c = code.charAt(2);
				if ((c == '-') || (c == '_'))
				{
					// good enough
					return true;
				}
			}
		}

		return false;
	}

	public boolean createNew()
	{
		// new project window; create project file
		NewProjectDialog newProjDialog = new NewProjectDialog(this, this, null, 0);
		newProjDialog.setVisible(true);
		boolean m_dialogOK = ! newProjDialog.dialogCancelled();
		newProjDialog.dispose();
		return m_dialogOK;
	}

	public void buildProjFile() throws IOException
	{
		ProjectFileReader pfr = new ProjectFileReader();
		
		pfr.setTarget(getLocRoot());
		pfr.setSource(getSourceRoot());
		pfr.setTM(getTMRoot());
		pfr.setGlossary(getGlossaryRoot());
		pfr.setSourceLang(getSrcLang());
		pfr.setTargetLang(getLocLang());
		
		pfr.writeProjectFile(getProjectFile());
	}

    private String	m_projName;
	private String	m_projFile;
	private String	m_projRoot;
	private String	m_projInternal;
	private String	m_srcRoot;
	private String	m_locRoot;
	private String	m_glosRoot;
	private String	m_tmRoot;

	private String	m_srcLang;
	private String	m_locLang;
	
	protected boolean m_dialogOK;

}
