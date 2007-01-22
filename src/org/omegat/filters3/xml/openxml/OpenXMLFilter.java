/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
           (C) 2007 Didier Briel
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

package org.omegat.filters3.xml.openxml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import java.util.regex.*;
import java.util.Collections; 
import java.util.ArrayList;
import java.util.Comparator;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.filters3.xml.openxml.*;
import org.omegat.util.LFileCopy;
import org.omegat.util.OStrings;


/**
 * Filter for Open XML file format.
 * 
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 */
public class OpenXMLFilter extends AbstractFilter
{
    private static final String DOCUMENTS = 
    // Word
    "(document\\.xml)|(comments\\.xml)|(footnotes\\.xml)|(endnotes\\.xml)" +    // NOI18N                
    "|(header\\d+\\.xml)|(footer\\d+\\.xml)" +                                  // NOI18N
    // Excel
    "|(sharedStrings\\.xml)|(comments\\d+\\.xml)" +                             // NOI18N
    // PowerPoint
    "|(slide\\d+\\.xml)|(notesSlide\\d+\\.xml)"                                 // NOI18N
    ;            
    private static final Pattern TRANSLATABLE = Pattern.compile(DOCUMENTS);
    
    private static final Pattern DIGITS = Pattern.compile("(\\d+)\\.xml");      // NOI18N
    
    /** Creates a new instance of OpenXMLFilter */
    public OpenXMLFilter()
    {
    }

    /** Returns true if it's an Open XML file. */
    public boolean isFileSupported(File inFile, String inEncoding)
    {
        try
        {
            ZipFile file = new ZipFile(inFile);
            Enumeration entries = file.entries();
            while (entries.hasMoreElements())
            {
                ZipEntry entry = (ZipEntry) entries.nextElement();
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
        if (xmlfilter==null)
            xmlfilter = new OpenXMLXMLFilter();
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
        if ( fileName.endsWith(".xml") ) 
            fileName = fileName.substring(0, fileName.lastIndexOf(".xml"));
        return fileName;
    }
   
    /**
     * Processes a single OpenXML file,
     * which is actually a ZIP file consisting of many XML files, 
     * some of which should be translated.
     */
    public List processFile(File inFile, String inEncoding, File outFile, 
                            String outEncoding) 
                            throws IOException, TranslationException
    {
        ZipFile zipfile = new ZipFile(inFile);
        ZipOutputStream zipout = null;
        if (outFile!=null)
            zipout = new ZipOutputStream(new FileOutputStream(outFile));
        Enumeration unsortedZipcontents = zipfile.entries();
        List filelist = Collections.list(unsortedZipcontents);
        Collections.sort(filelist, new Comparator()
        {
         // Sort filenames, because zipfile.entries give a random order
         // We use a simplified natural sort, to have slide1, slide2 ... slide10
         // instead of slide1, slide10, slide 2 
         // We also order files arbitrarily, to have, for instance
         // documents.xml before comments.xml
             public int compare(Object o1, Object o2)
             {
                 ZipEntry z1 = (ZipEntry)o1; 
                 ZipEntry z2 = (ZipEntry)o2;
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
                     // because "contents" is present twice in DOCUMENTS
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
        Enumeration zipcontents = Collections.enumeration(filelist);
 
        while (zipcontents.hasMoreElements())
        {
            ZipEntry zipentry = (ZipEntry) zipcontents.nextElement();
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

    /** Source encoding can not be varied by the user. */
    public boolean isSourceEncodingVariable()
    {
        return false;
    }

    /** Target encoding can not be varied by the user. */
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
    
}
