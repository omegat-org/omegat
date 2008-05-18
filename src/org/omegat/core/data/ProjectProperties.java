/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/
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

package org.omegat.core.data;

import java.io.File;

import org.omegat.util.Language;
import org.omegat.util.OConsts;
import org.omegat.util.Preferences;

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
    public ProjectProperties(File projectDir)
    {
        setProjectName(projectDir.getName());
        setProjectRoot(projectDir.getAbsolutePath() + File.separator);
        setSourceRoot(projectRoot + OConsts.DEFAULT_SOURCE + File.separator);
        setTargetRoot(projectRoot + OConsts.DEFAULT_TARGET + File.separator);
        setGlossaryRoot(projectRoot + OConsts.DEFAULT_GLOSSARY + File.separator);
        setTMRoot(projectRoot + OConsts.DEFAULT_TM + File.separator);
        setSourceLanguage("EN-US"); // NOI18N
        setTargetLanguage("EN-GB"); // NOI18N
        setSentenceSegmentingEnabled(true);
        
        String sourceLocale = Preferences.getPreference(Preferences.SOURCE_LOCALE);
        if( !sourceLocale.equals(""))                                                 
            setSourceLanguage(sourceLocale);
        
        String targetLocale = Preferences.getPreference(Preferences.TARGET_LOCALE);
        if( !targetLocale.equals("") )                                                
            setTargetLanguage(targetLocale);
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
        return projectRoot + OConsts.DEFAULT_INTERNAL + File.separator;
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
     * @return true if project OK, false if some directories are missing
     */
    public boolean verifyProject()
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
