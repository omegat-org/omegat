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

package org.omegat.core;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;

import org.omegat.filters2.TranslationException;
import org.omegat.gui.dialogs.ProjectPropertiesDialog;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.ProjectFileStorage;
import org.omegat.util.StaticUtils;
import org.omegat.util.gui.OmegaTFileChooser;
import org.omegat.util.gui.OpenProjectFileChooser;


/**
 * Storage for project properties.
 * May read and write project from/to disk.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 */
public class ProjectProperties
{
    /** Default constructor to initialize fields (to get no NPEs). */
    public ProjectProperties()
    {
        reset();
    }
    
    /** Returns The Target (Compiled) Files Directory */
    public String getTargetRoot()
    {
        return targetRoot;
    }
    /** Sets The Target (Compiled) Files Directory */
    public void setTargetRoot(String targetRoot)
    {
        this.targetRoot = targetRoot;
    }
    
    /** Returns The Glossary Files Directory */
    public String getGlossaryRoot()
    {
        return glossaryRoot;
    }
    /** Sets The Glossary Files Directory */
    public void setGlossaryRoot(String glossaryRoot)
    {
        this.glossaryRoot = glossaryRoot;
    }
    
    /** Returns The Translation Memory (TMX) Files Directory */
    public String getTMRoot()
    {
        return tmRoot;
    }
    /** Sets The Translation Memory (TMX) Files Directory */
    public void setTMRoot(String tmRoot)
    {
        this.tmRoot = tmRoot;
    }
    
    /** Returns the name of the Project */
    public String getProjectName()
    {
        return projectName;
    }
    /** Sets the name of the Project */
    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }
    
    /** Returns The Project File Name */
    public String getProjectFile()
    {
        return projectFile;
    }
    /** Sets The Project File Name */
    public void setProjectFile(String projectFile)
    {
        this.projectFile = projectFile;
    }
    
    /** Returns The Project Root Directory */
    public String getProjectRoot()
    {
        return projectRoot;
    }
    /** Sets The Project Root Directory */
    public void setProjectRoot(String projectRoot)
    {
        this.projectRoot = projectRoot;
    }
    
    /** Returns The Project's Translation Memory (TMX) File */
    public String getProjectInternal()
    {
        return projectInternal;
    }
    /** Sets The Project's Translation Memory (TMX) File */
    public void setProjectInternal(String projectInternal)
    {
        this.projectInternal = projectInternal;
    }
    
    /** Returns The Source (to be translated) Files Directory */
    public String getSourceRoot()
    {
        return sourceRoot;
    }
    /** Sets The Source (to be translated) Files Directory */
    public void setSourceRoot(String sourceRoot)
    {
        this.sourceRoot = sourceRoot;
    }
    
    /** Returns The Source Language (language of the source files) of the Project */
    public Language getSourceLanguage()
    {
        return sourceLanguage;
    }
    /** Sets The Source Language (language of the source files) of the Project */
    public void setSourceLanguage(Language sourceLanguage)
    {
        this.sourceLanguage = sourceLanguage;
    }
    /** Sets The Source Language (language of the source files) of the Project */
    public void setSourceLanguage(String sourceLanguage)
    {
        this.sourceLanguage = new Language(sourceLanguage);
    }
    
    /** Returns The Target Language (language of the translated files) of the Project */
    public Language getTargetLanguage()
    {
        return targetLanguage;
    }
    /** Sets The Target Language (language of the translated files) of the Project */
    public void setTargetLanguage(Language targetLanguage)
    {
        this.targetLanguage = targetLanguage;
    }
    /** Sets The Target Language (language of the translated files) of the Project */
    public void setTargetLanguage(String targetLanguage)
    {
        this.targetLanguage = new Language(targetLanguage);
    }
    
    /** Returns whether The Sentence Segmenting is Enabled for this Project. Default, Yes. */
    public boolean isSentenceSegmentingEnabled()
    {
        return sentenceSegmentingOn;
    }
    /** Sets whether The Sentence Segmenting is Enabled for this Project */
    public void setSentenceSegmentingEnabled(boolean sentenceSegmentingOn)
    {
        this.sentenceSegmentingOn = sentenceSegmentingOn;
    }
    
    /**
     * Resets all project properties to empty or default values
     */
    public void reset()
    {
        setProjectFile("");                                                     // NOI18N
        setProjectName("");	                                                // NOI18N
        setProjectRoot("");	                                                // NOI18N
        setProjectInternal("");	                                                // NOI18N
        setSourceRoot("");	                                                // NOI18N
        setTargetRoot("");	                                                // NOI18N
        setGlossaryRoot("");	                                                // NOI18N
        setTMRoot("");                                                          // NOI18N
        setSourceLanguage("EN-US");                                             // NOI18N
        setTargetLanguage("EN-GB");                                             // NOI18N
        setSentenceSegmentingEnabled(true);
    }
    
    /**
     * Loads existing project file.
     * Brings up OmegaTFileChooser to open a file and
     * sets global properties.
     *
     * @param projectRoot Project root. If null, brings up the file chooser 
     *                      so that the user chooses the project.
     */
    public boolean loadExisting(Frame parentFrame, String projectRoot) 
            throws IOException, InterruptedIOException
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
        Preferences.setPreference(Preferences.CURRENT_FOLDER, projectRootFolder.getParent());
        try
        {
            ProjectFileStorage pfr = new ProjectFileStorage();
            setProjectName(projectRootFolder.getName());
            setProjectRoot(projectRoot);
            pfr.loadProjectFile(getProjectRoot() + OConsts.FILE_PROJECT);
            
            setSourceRoot(pfr.getSource());
            setTargetRoot(pfr.getTarget());
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
                ProjectPropertiesDialog prj = new ProjectPropertiesDialog(
                        parentFrame, this, getProjectFile(), ProjectPropertiesDialog.RESOLVE_DIRS);
                
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
            Log.log(te);
            throw new IOException(OStrings.getString("PP_ERROR_UNABLE_TO_READ_PROJECT_FILE"));
        }
    }
    
    /**
     * @return true if project OK, false if some directories are missing
     */
    private boolean verifyProject()
    {
        // now see if these directories are where they're suposed to be
        File src = new File(getSourceRoot());
        File loc = new File(getTargetRoot());
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
    
    /**
     * Displays dialogs to create a new project.
     */
    public boolean createNew(Frame parentFrame)
    {
        // new project window; create project file
        ProjectPropertiesDialog newProjDialog = new ProjectPropertiesDialog(
                parentFrame, this, null, ProjectPropertiesDialog.NEW_PROJECT);
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
        ProjectFileStorage pfr = new ProjectFileStorage();
        
        pfr.setTarget(getTargetRoot());
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
    public boolean editProject(Frame parentFrame) throws IOException
    {
        // backing up, as NewProjectDialog changes properties directly
        // it's a hack, but faster than refactoring ;-)
        ProjectProperties backup = createBackup();
        
        // displaying the dialog to change paths and other properties
        ProjectPropertiesDialog prj = new ProjectPropertiesDialog(parentFrame, this, getProjectFile(), ProjectPropertiesDialog.EDIT_PROJECT);
        
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
            setProjectName(backup.projectName);
            setProjectFile(backup.projectFile);
            setProjectRoot(backup.projectRoot);
            setProjectInternal(backup.projectInternal);
            setSourceRoot(backup.sourceRoot);
            setTargetRoot(backup.targetRoot);
            setGlossaryRoot(backup.glossaryRoot);
            setTMRoot(backup.tmRoot);
            setSourceLanguage(backup.sourceLanguage);
            setTargetLanguage(backup.targetLanguage);
            
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
        res.sourceLanguage = new Language(sourceLanguage.getLocaleCode());
        res.targetLanguage = new Language(targetLanguage.getLocaleCode());
        return res;
    }
    
    private String projectName;
    private String projectFile;
    private String projectRoot;
    private String projectInternal;
    private String sourceRoot;
    private String targetRoot;
    private String glossaryRoot;
    private String tmRoot;
    
    private Language sourceLanguage;
    private Language targetLanguage;
    
    private boolean sentenceSegmentingOn;
    private boolean dontInsertSource;
    private boolean insertBestMatch;
    private int minimalSimilarity;
}
