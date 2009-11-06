/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
           (C) 2007-2008 Didier Briel
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

package org.omegat.filters3.xml.openxml;

import java.awt.Dialog;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.util.LFileCopy;
import org.omegat.util.Log;
import org.omegat.util.OStrings;

/**
 * Filter for Open XML file format.
 * 
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 */
public class OpenXMLFilter extends AbstractFilter
{
    private String DOCUMENTS = "(document\\.xml)";                              // NOI18N 
    private Pattern TRANSLATABLE; 
    private static final Pattern DIGITS = Pattern.compile("(\\d+)\\.xml");      // NOI18N

    
    private boolean optionsAlreadyRead = false;
    /**
     * Defines the documents to read according to options
     */
    private void defineDOCUMENTSOptions()
    {
/*    
    // Complete string when all options are enabled
    // Word
    "(document\\.xml)|(comments\\.xml)|(footnotes\\.xml)|(endnotes\\.xml)" +    
    "|(header\\d+\\.xml)|(footer\\d+\\.xml)" +                                  
    // Excel
    "|(sharedStrings\\.xml)|(comments\\d+\\.xml)" +                             
    // PowerPoint
    "|(slide\\d+\\.xml)|(slideMaster\\d+\\.xml)|(notesSlide\\d+\\.xml)"                                 
*/          
        if (!optionsAlreadyRead){
            OpenXMLOptions options = (OpenXMLOptions) this.getOptions();
            if (options == null)
                options = new OpenXMLOptions();

            if (options.getTranslateComments())
                DOCUMENTS += "|(comments\\.xml)";                               // NOI18N
            if (options.getTranslateFootnotes())
                DOCUMENTS += "|(footnotes\\.xml)";                              // NOI18N
            if (options.getTranslateEndnotes())
                DOCUMENTS += "|(endnotes\\.xml)";                               // NOI18N
            if (options.getTranslateHeaders())
                DOCUMENTS += "|(header\\d+\\.xml)";                             // NOI18N
            if (options.getTranslateFooters())
                DOCUMENTS += "|(footer\\d+\\.xml)";                             // NOI18N
            DOCUMENTS += "|(sharedStrings\\.xml)";                              // NOI18N    
            if (options.getTranslateExcelComments())
                DOCUMENTS += "|(comments\\d+\\.xml)";                           // NOI18N
            DOCUMENTS += "|(slide\\d+\\.xml)";                                  // NOI18N
            if (options.getTranslateSlideMasters())          
                DOCUMENTS += "|(slideMaster\\d+\\.xml)";                        // NOI18N
            if (options.getTranslateSlideComments())
                DOCUMENTS += "|(notesSlide\\d+\\.xml)";                         // NOI18N
            TRANSLATABLE = Pattern.compile(DOCUMENTS);
            optionsAlreadyRead = true;
        }
    }
        
    
    /** Creates a new instance of OpenXMLFilter */
    public OpenXMLFilter()
    {
    }

    /** Returns true if it's an Open XML file. */
    public boolean isFileSupported(File inFile, String inEncoding)
    {
        try
        {
            defineDOCUMENTSOptions(); // Define the documents to read
            
            ZipFile file = new ZipFile(inFile);
            Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                String shortname = entry.getName();
                shortname = removePath(shortname);
                Matcher filematch = TRANSLATABLE.matcher(shortname);
                if (filematch.matches())
                    return true;
            }
        } catch (IOException e) {}
        return false;
    }

    OpenXMLXMLFilter xmlfilter = null;
    private OpenXMLXMLFilter getXMLFilter()
    {
        if (xmlfilter==null) {
            xmlfilter = new OpenXMLXMLFilter();
            xmlfilter.setParseCallback(entryProcessingCallback);
        }
        // Defining the actual dialect, because at this step 
        // we have the options
        OpenXMLDialect dialect = (OpenXMLDialect) xmlfilter.getDialect();
        dialect.defineDialect((OpenXMLOptions) this.getOptions());
        
        return xmlfilter;
    }
    
    /** Returns a temporary file for Open XML. A nasty hack, to say polite way. */
    private File tmp() throws IOException
    {
        return File.createTempFile("o-xml-temp", ".xml");                       // NOI18N
    }
    
    /**
     * Receives a filename with a path
     * Returns a string without the path
     */
    private String removePath(String fileName)
    {
        if (fileName.lastIndexOf('/')>=0)
            fileName = fileName.substring(fileName.lastIndexOf('/')+1);                     
        return fileName;
    }

    /**
     * Receives a filename
     * Removes an .xml extension if found    
     */
    private String removeXML(String fileName)
    {
        if ( fileName.endsWith(".xml") )                                        // NOI18N
            fileName = fileName.substring(0, fileName.lastIndexOf(".xml"));     // NOI18N
        return fileName;
    }
   
    /**
     * Processes a single OpenXML file,
     * which is actually a ZIP file consisting of many XML files, 
     * some of which should be translated.
     */
    public List<File> processFile(File inFile, String inEncoding, File outFile, 
                            String outEncoding) 
                            throws IOException, TranslationException
    {
	defineDOCUMENTSOptions(); // Define the documents to read
	 
	ZipFile zipfile = new ZipFile(inFile);
        ZipOutputStream zipout = null;
        if (outFile!=null)
            zipout = new ZipOutputStream(new FileOutputStream(outFile));
        Enumeration<? extends ZipEntry> unsortedZipcontents = zipfile.entries();
        List<? extends ZipEntry> filelist = Collections.list(unsortedZipcontents);
        Collections.sort(filelist, new Comparator<ZipEntry>()
        {
         // Sort filenames, because zipfile.entries give a random order
         // We use a simplified natural sort, to have slide1, slide2 ... slide10
         // instead of slide1, slide10, slide 2 
         // We also order files arbitrarily, to have, for instance
         // documents.xml before comments.xml
             public int compare(ZipEntry z1, ZipEntry z2)
             {
                 String s1 = z1.getName();
                 String s2 = z2.getName();
                 String[] words1 = s1.split("\\d+\\.");                         // NOI18N
                 String[] words2 = s2.split("\\d+\\.");                         // NOI18N
                 // Digits at the end and same text
                 if ( ( words1.length > 1 && words2.length > 1 ) && // Digits
                      ( words1[0].equals(words2[0]) ) )  // Same text
                 {
                     int number1 = 0;
                     int number2 = 0;
                     Matcher getDigits = DIGITS.matcher(s1); 
                     if ( getDigits.find() )
                         number1 = Integer.parseInt(getDigits.group(1));
                     getDigits = DIGITS.matcher(s2); 
                     if ( getDigits.find() )
                         number2 = Integer.parseInt(getDigits.group(1));
                     if ( number1 > number2 )
                         return 1;                               
                     else if ( number1 < number2 )
                         return -1;
                     else
                        return 0;
                 }
                 else
                 {
                     String shortname1 = removePath(words1[0]);
                     shortname1 = removeXML(shortname1);
                     String shortname2 = removePath(words2[0]);
                     shortname2 = removeXML(shortname2);
                                       
                     // Specific case for Excel
                     // because "comments" is present twice in DOCUMENTS
                     if ( shortname1.indexOf("sharedStrings")>=0 ||             // NOI18N
                          shortname2.indexOf("sharedStrings")>=0 )              // NOI18N
                     {
                         if ( shortname2.indexOf("sharedStrings") >=0 )         // NOI18N
                             return 1; // sharedStrings must be first
                         else
                             return -1;                                    
                     }
                                         
                     int index1 = DOCUMENTS.indexOf(shortname1);                    
                     int index2 = DOCUMENTS.indexOf(shortname2);

                     if ( index1 > index2 )
                         return 1;
                     else if ( index1 < index2 )
                         return -1;
                     else
                        return 0;
                 }
             }
        });
        Enumeration<? extends ZipEntry> zipcontents = Collections.enumeration(filelist);
 
        while (zipcontents.hasMoreElements())
        {
            ZipEntry zipentry = zipcontents.nextElement();
            String shortname = zipentry.getName();
            shortname = removePath(shortname);
            Matcher filematch = TRANSLATABLE.matcher(shortname);
            boolean first = true;
            if (filematch.matches())
            {
                File tmpin = tmp();
                LFileCopy.copy(zipfile.getInputStream(zipentry), tmpin);
                File tmpout = null;
                if (zipout!=null)
                    tmpout = tmp();
             
                try
                {
                    getXMLFilter().processFile(tmpin, null, tmpout, null);
                }
                catch (Exception e)
                {
                    throw new TranslationException(e.getLocalizedMessage() +
                            "\n" +                                              // NOI18N
                            OStrings.getString("OpenXML_ERROR_IN_FILE")+inFile);
                }
                
                if (zipout!=null)
                {
                    ZipEntry outEntry = new ZipEntry (zipentry.getName());
                    zipout.putNextEntry(outEntry);
                    LFileCopy.copy(tmpout, zipout);
                    zipout.closeEntry();
                    first = false;
                }
                if (!tmpin.delete())
                    tmpin.deleteOnExit();
                if (tmpout!=null)
                {
                    if (!tmpout.delete())
                        tmpout.deleteOnExit();
                }
            }
            else
            {
                if (zipout!=null)
                {
                    ZipEntry outEntry = new ZipEntry (zipentry.getName());
                    zipout.putNextEntry(outEntry);

                    LFileCopy.copy(zipfile.getInputStream(zipentry), zipout);
                    zipout.closeEntry();
                }
            }
        }
        if (zipout!=null)
            zipout.close();
        return null;
    }

    /** Human-readable Open XML filter name. */
    public String getFileFormatName()
    {
        return OStrings.getString("OpenXML_FILTER_NAME");
    }

    /** Extensions... */
    public Instance[] getDefaultInstances()
    {
        return new Instance[] 
        {
                new Instance("*.doc?"),                                         // NOI18N
                new Instance("*.xls?"),                                         // NOI18N
                new Instance("*.ppt?"),                                         // NOI18N
        };
    }

    /** Source encoding cannot be varied by the user. */
    public boolean isSourceEncodingVariable()
    {
        return false;
    }

    /** Target encoding cannot be varied by the user. */
    public boolean isTargetEncodingVariable()
    {
        return false;
    }

    /** Not implemented. */
    protected void processFile(BufferedReader inFile, BufferedWriter outFile) 
                   throws IOException, TranslationException
    {
        throw new IOException("Not Implemented!");                              // NOI18N
    }

    /**
     * Returns true to indicate that the OpenXML filter has options.
     * @return True, because the OpenXML filter has options.
     */
    public boolean hasOptions()
    {
        return true;
    }
    
    public Class getOptionsClass() {
        return OpenXMLOptions.class;
    }

    /**
     * OpenXML Filter shows a <b>modal</b> dialog to edit its own options.
     * 
     * @param currentOptions Current options to edit.
     * @return Updated filter options if user confirmed the changes, 
     * and current options otherwise.
     */
    public Serializable changeOptions(Dialog parent, Serializable currentOptions)
    {
        try
        {
            OpenXMLOptions options = (OpenXMLOptions) currentOptions;
            EditOpenXMLOptionsDialog dialog = 
                    new EditOpenXMLOptionsDialog(parent, options);
            dialog.setVisible(true);
            if( EditOpenXMLOptionsDialog.RET_OK==dialog.getReturnStatus() )
                return dialog.getOptions();
            else
                return currentOptions;
        }
        catch( Exception e )
        {
            Log.logErrorRB("HTML_EXC_EDIT_OPTIONS");
            Log.log(e);
            return currentOptions;
        }
    }
}
