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

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;

import org.omegat.filters2.TranslationException;
import org.omegat.gui.dialogs.ProjectPropertiesDialog;
import org.omegat.util.Language;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.ProjectFileReader;
import org.omegat.gui.main.MainInterface;

/**
 * Storage for project properties.
 * May read and write project from/to disk.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 */
public class ProjectProperties
{
    public String getLocRoot()
    { 
        return m_locRoot;		
    }
    public void setLocRoot(String m_locRoot)
    {        
        this.m_locRoot = m_locRoot;    
    }
    
    public String getGlossaryRoot()
    { 
        return m_glosRoot;	
    }
    public void setGlossaryRoot(String m_glosRoot)
    {  
        this.m_glosRoot = m_glosRoot;  
    }
    
    public String getTMRoot()
    { 
        return m_tmRoot;		
    }
    public void setTMRoot(String m_tmRoot)
    {          
        this.m_tmRoot = m_tmRoot;      
    }
    
    public String getProjectName()
    {        
        return m_projName;    
    }
    public void setProjectName(String m_projName)
    {   
        this.m_projName = m_projName;  
    }
    
    public String getProjectFile()
    {        
        return m_projFile;    
    }
    public void setProjectFile(String m_projFile)
    {   
        this.m_projFile = m_projFile;  
    }
    
    public String getProjectRoot()
    {        
        return m_projRoot;    
    }
    public void setProjectRoot(String m_projRoot)
    {   
        this.m_projRoot = m_projRoot;  
    }
    
    public String getProjectInternal()
    {    
        return m_projInternal;    
    }
    public void setProjectInternal(String m_projInternal)
    {    
        this.m_projInternal = m_projInternal;   
    }
    
    public String getSourceRoot()
    {         
        return m_srcRoot;    
    }
    public void setSourceRoot(String m_srcRoot)
    {     
        this.m_srcRoot = m_srcRoot;    
    }
    
    public Language getSourceLanguage()
    { 
        return m_sourceLang;	
    }
    public void setSourceLanguage(Language m_sourceLang)
    {      
        this.m_sourceLang = m_sourceLang;    
    }
    public void setSourceLanguage(String m_sourceLang)
    {       
        this.m_sourceLang = new Language(m_sourceLang);  
    }
    
    public Language getTargetLanguage()
    { 
        return m_targetLang;	
    }
    public void setTargetLanguage(Language m_targetLang)
    {      
        this.m_targetLang = m_targetLang;    
    }
    public void setTargetLanguage(String m_targetLang)
    {       
        this.m_targetLang = new Language(m_targetLang);  
    }
    
    public boolean isSentenceSegmentingEnabled()
    { 
        return m_sentenceSegmenting; 
    }
    public void setSentenceSegmentingEnabled(boolean m_sentenceSegmenting)
    { 
        this.m_sentenceSegmenting = m_sentenceSegmenting; 
    }

    /**
     * Resets all project properties to empty or default values
     */
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
        setSentenceSegmentingEnabled(true);
	}

    private MainInterface mainframe;
    
    /**
     * Loads existing project file.
     * Brings up OmegaTFileChooser to open a file and 
     * sets global properties.
     *
     * @param projectRoot Project root. If null, brings up the file chooser so that the user chooses the project.
     */
	public boolean loadExisting(String projectRoot) throws IOException, InterruptedIOException
	{
		reset();

        File projectRootFolder = null;
        try
        {
            projectRootFolder = new File(projectRoot);
            if( !projectRootFolder.exists() )
                projectRootFolder = null;
        }
        catch( Exception e )
        {
            projectRootFolder = null;
        }
        
        if( projectRootFolder==null )
        {
            // select existing project file - open it
            OmegaTFileChooser pfc=new OpenProjectFileChooser();
            if( OmegaTFileChooser.APPROVE_OPTION!=pfc.showOpenDialog(null) )
                return false;

            projectRootFolder = pfc.getSelectedFile();
        }
        
        projectRoot = projectRootFolder.getAbsolutePath() + File.separator;
		Preferences.setPreference(
                Preferences.CURRENT_FOLDER, projectRootFolder.getParent());
		try 
		{
			ProjectFileReader pfr = new ProjectFileReader();
			setProjectName(projectRootFolder.getName());
			setProjectRoot(projectRoot);
			pfr.loadProjectFile(getProjectRoot() + OConsts.FILE_PROJECT);

			setSourceRoot(pfr.getSource());
			setLocRoot(pfr.getTarget());
			setGlossaryRoot(pfr.getGlossary());
			setTMRoot(pfr.getTM());
			setProjectInternal(getProjectRoot() + OConsts.DEFAULT_INTERNAL
						+ File.separator);
			setSourceLanguage(pfr.getSourceLang());
			setTargetLanguage(pfr.getTargetLang());
			Preferences.setPreference(
                    Preferences.SOURCE_LOCALE, getSourceLanguage().toString());
			Preferences.setPreference(
                    Preferences.TARGET_LOCALE, getTargetLanguage().toString());
			setProjectFile(getProjectRoot() + OConsts.FILE_PROJECT);
            
            setSentenceSegmentingEnabled((pfr.getSentenceSeg()!=null) && pfr.getSentenceSeg().equals("true"));  // NOI18N

			if( !verifyProject() )
			{
				// something wrong with the project - display open dialog
				//  to fix it
				ProjectPropertiesDialog prj = new ProjectPropertiesDialog(this, getProjectFile(), ProjectPropertiesDialog.RESOLVE_DIRS);

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
		ProjectPropertiesDialog newProjDialog = new ProjectPropertiesDialog(this, null, ProjectPropertiesDialog.NEW_PROJECT);
        if( !newProjDialog.dialogCancelled() )
        {
            newProjDialog.setVisible(true);
            if( !newProjDialog.dialogCancelled() )
                return true;
        }
		return false;
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
        pfr.setSentenceSeg(isSentenceSegmentingEnabled() ? "true" : "false");   // NOI18N
		
		pfr.writeProjectFile(getProjectFile());
	}
    
    /**
     * Edits the properties of opened project.
     * Changes global properties accordingly.
     *
     * @return returns true if the project was edited by the user.
     */
	public boolean editProject() throws IOException
	{
        // backing up, as NewProjectDialog changes properties directly
        // it's a hack, but faster than refactoring ;-)
        ProjectProperties backup = createBackup();
        
        // displaying the dialog to change paths and other properties
        ProjectPropertiesDialog prj = new ProjectPropertiesDialog(this, getProjectFile(), ProjectPropertiesDialog.EDIT_PROJECT);

        // continue until user changes correctly or cancels
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
            // backing up, as NewProjectDialog changes properties directly
            // it's a hack, but faster than refactoring ;-)
            restoreBackup(backup);
            return false;
        }
        else
            return true;
	}

    /** Backs up all the properties */
    private ProjectProperties createBackup()
    {
        try
        {
            return (ProjectProperties)clone();
        }
        catch( CloneNotSupportedException cnse )
        {
            return null;
        }
    }
    /** Restores the backup */
    private void restoreBackup(ProjectProperties backup)
    {
        if( backup!=null )
        {
            setProjectName(backup.m_projName);
            setProjectFile(backup.m_projFile);
            setProjectRoot(backup.m_projRoot);
            setProjectInternal(backup.m_projInternal);
            setSourceRoot(backup.m_srcRoot);
            setLocRoot(backup.m_locRoot);
            setGlossaryRoot(backup.m_glosRoot);
            setTMRoot(backup.m_tmRoot);
            setSourceLanguage(backup.m_sourceLang);
            setTargetLanguage(backup.m_targetLang);
            
            // also updating some global OmegaT preferences
			Preferences.setPreference(
                    Preferences.SOURCE_LOCALE, getSourceLanguage().toString());
			Preferences.setPreference(
                    Preferences.TARGET_LOCALE, getTargetLanguage().toString());
        }
    }
    
    protected Object clone() throws CloneNotSupportedException
    {
        ProjectProperties res = (ProjectProperties)super.clone();
        res.m_sourceLang = new Language(m_sourceLang.getLocale());
        res.m_targetLang = new Language(m_targetLang.getLocale());
        return res;
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
	
    private boolean m_sentenceSegmenting;
    private boolean dontInsertSource;
    private boolean insertBestMatch;
    private int minimalSimilarity;
}
