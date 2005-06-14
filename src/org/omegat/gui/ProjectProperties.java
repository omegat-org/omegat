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

package org.omegat.gui;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import javax.swing.JFrame;

import org.omegat.filters2.TranslationException;
import org.omegat.util.Language;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.PreferenceManager;
import org.omegat.util.ProjectFileReader;

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
    public String getProjectName() {        return m_projName;    }
    public String getProjectFile() {        return m_projFile;    }
    public String getProjectRoot() {        return m_projRoot;    }
    public String getProjectInternal() {    return m_projInternal;    }
    public String getSourceRoot() {         return m_srcRoot;    }
    
	public Language getSourceLanguage()		{ return m_sourceLang;	}
    public void setSourceLanguage(Language m_srcLoc) {      this.m_sourceLang = m_srcLoc;    }
    public void setSourceLanguage(String srcLocale) {       this.m_sourceLang = new Language(srcLocale);  }
    
	public Language getTargetLanguage()		{ return m_targetLang;	}
    public void setTargetLanguage(Language m_trgLoc) {      this.m_targetLang = m_trgLoc;    }
    public void setTargetLanguage(String trgLocale) {       this.m_targetLang = new Language(trgLocale);  }
    
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
		setSourceLanguage("EN-US");  // NOI18N
		setTargetLanguage("EN-GB");  // NOI18N
	}

    class OTFileFilter extends FileFilter
	{
		public String getDescription()
		{
			return OStrings.getString("PP_PROJECT_FILES_DESC");
		}
		
		public boolean accept(File f)
		{
			if (f.getName().endsWith(OConsts.PROJ_EXTENSION))
				return true;
			else
                return f.isDirectory();
		}
	}

    /**
     * Loads existing project file.
     * Brings up OmegaTFileChooser to open a file and 
     * sets global properties.
     */
	public boolean loadExisting() throws IOException, InterruptedIOException
	{
		reset();

        // select existing project file - open it
        OmegaTFileChooser pfc=new OpenProjectFileChooser();
        if( OmegaTFileChooser.APPROVE_OPTION!=pfc.showOpenDialog(this) )
            return false;

        File projectRootFolder = pfc.getSelectedFile();
        String projectRoot = projectRootFolder.getAbsolutePath() + File.separator;

		PreferenceManager.pref.setPreference(
                OConsts.PREF_CUR_DIR, projectRootFolder.getParent());
		try 
		{
			ProjectFileReader pfr = new ProjectFileReader();
			setProjectName(projectRootFolder.getName());
			setProjectRoot(projectRoot);
			pfr.loadProjectFile(getProjectRoot() + OConsts.PROJ_FILENAME);

			setSourceRoot(pfr.getSource());
			setLocRoot(pfr.getTarget());
			setGlossaryRoot(pfr.getGlossary());
			setTMRoot(pfr.getTM());
			setProjectInternal(getProjectRoot() + OConsts.DEFAULT_INTERNAL
						+ File.separator);
			setSourceLanguage(pfr.getSourceLang());
			setTargetLanguage(pfr.getTargetLang());
			PreferenceManager.pref.setPreference(
                    OConsts.PREF_SOURCELOCALE, getSourceLanguage().toString());
			PreferenceManager.pref.setPreference(
                    OConsts.PREF_TARGETLOCALE, getTargetLanguage().toString());
			setProjectFile(getProjectRoot() + OConsts.PROJ_FILENAME);

			if( !verifyProject() )
			{
				// something wrong with the project - display open dialog
				//  to fix it
				NewProjectDialog prj = new NewProjectDialog(this, this, getProjectFile(), true);

				// continue  until user fixes problem or cancels
				boolean abort = false;
				while( true )
				{
					prj.setVisible(true);
					if( prj.dialogCancelled() )
					{
						abort = true;
						break;
					}
                    else if( verifyProject() )
                    {
						buildProjFile();
						break;
					}
				}
				prj.dispose();
				if (abort)
				{
					reset();
					return false;
				}
			}
			return true;
		}
		catch( TranslationException te )
		{
			reset();
			throw new IOException(OStrings.getString("PP_ERROR_UNABLE_TO_READ_PROJECT_FILE") + te);
		}
	}

    /**
     * @return true if project OK, false if some directories are missing
     */
    private boolean verifyProject() {
		// now see if these directories are where they're suposed to be
		File src = new File(getSourceRoot());
		File loc = new File(getLocRoot());
		File gls = new File(getGlossaryRoot());
		File tmx = new File(getTMRoot());

		if (src.exists() && loc.exists() && gls.exists() && tmx.exists())
			return true;

		return false;
	}

    /**
     * Verifies whether the language code is OK.
     */
	public static boolean verifySingleLangCode(String code)
	{
		if( code.length()==2 )
		{
			// make sure both values are characters
			if( Character.isLetter(code.charAt(0)) &&
                    Character.isLetter(code.charAt(1)) &&
                    new Language(code).getDisplayName().length()>0 )
			{
				return true;
			}
		}
		else if (code.length() == 5)
		{
			// make sure both values are characters
			if( Character.isLetter(code.charAt(0)) &&
                    Character.isLetter(code.charAt(1)) &&
                    Character.isLetter(code.charAt(3)) &&
                    Character.isLetter(code.charAt(4)) &&
                    (code.charAt(2)=='-' || code.charAt(2)=='_') &&
                    new Language(code).getDisplayName().length()>0 )
			{
                return true;
			}
		}
		return false;
	}

	public boolean createNew()
	{
		// new project window; create project file
		NewProjectDialog newProjDialog = new NewProjectDialog(this, this, null, false);
        if( !newProjDialog.dialogCancelled() )
        {
            newProjDialog.setVisible(true);
            m_dialogOK = ! newProjDialog.dialogCancelled();
            newProjDialog.dispose();
        }
		return m_dialogOK;
	}

	public void buildProjFile() throws IOException
	{
		ProjectFileReader pfr = new ProjectFileReader();
		
		pfr.setTarget(getLocRoot());
		pfr.setSource(getSourceRoot());
		pfr.setTM(getTMRoot());
		pfr.setGlossary(getGlossaryRoot());
		pfr.setSourceLang(getSourceLanguage().toString());
		pfr.setTargetLang(getTargetLanguage().toString());
		
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

	private Language	m_sourceLang;
	private Language	m_targetLang;
	
	boolean m_dialogOK;

}
