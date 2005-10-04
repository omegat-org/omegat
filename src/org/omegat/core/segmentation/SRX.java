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

package org.omegat.core.segmentation;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

import org.omegat.util.Language;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;



/**
 * The class with all the segmentation data possible -- rules, languages, etc.
 * It loads and saves its data from/to SRX file.
 *
 * @author Maxym Mykhalchuk
 */
public class SRX implements Serializable
{
    
    private static SRX srx = null;
    private static File configFile=new File("segmentation.conf");               // NOI18N
            
    /**
     * SRX factory method.
     * <p>
     * For now, just returns the only SRX manager object.
     */
    public static SRX getSRX()
    {
        if( srx==null )
        {
            srx = load();
        }
        return srx;
    }
    
    /**
     * Reloads SRX rules from disk.
     */
    public static void reload()
    {
        srx = load();
    }
    
    /** 
     * Creates an empty SRX, without any rules.
     */
    public SRX() 
    {
    }

    /**
     * Saves segmentation rules.
     */
    public void save()
    {
        try
        {
            setVersion(CURRENT_VERSION);
            XMLEncoder xmlenc = new XMLEncoder(new FileOutputStream(configFile));
            xmlenc.writeObject(this);
            xmlenc.close();
        }
        catch( IOException ioe )
        {
            String message = 
                    MessageFormat.format(OStrings.getString("CORE_SRX_ERROR_SAVING_SEGMENTATION_CONFIG"),
                    new Object[] {ioe} );
            StaticUtils.log(message);
            JOptionPane.showMessageDialog(null, 
                    message,
                    OStrings.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Loads segmentation rules from an XML file.
     * If there's an error loading a file, it calls <code>init</code>.
     */
    private static SRX load()
    {
        SRX res;
        try
        {
            
            MyExceptionListener myel = new MyExceptionListener();
            XMLDecoder xmldec = new XMLDecoder(new FileInputStream(configFile), null, myel);
            res = (SRX)xmldec.readObject();
            xmldec.close();
            
            if( myel.isExceptionOccured() )
            {
                StringBuffer sb = new StringBuffer();
                List exceptions = myel.getExceptionsList();
                for(int i=0; i<exceptions.size(); i++)
                {
                    sb.append("    ");                                          // NOI18N
                    sb.append(exceptions.get(i));
                    sb.append("\n");                                            // NOI18N
                }
                throw new Exception(
                        MessageFormat.format("Exceptions occured while loading segmentation rules:\n{0}",  // NOI18N
                        new Object[] {sb.toString()} ) );
            }
        }
        catch( Exception e )
        {
            // silently ignoring FNF
            if( !(e instanceof FileNotFoundException) )
                StaticUtils.log(e.toString());
            res = new SRX();
            res.init();
        }
        return res;
    }

    /**
     * My Own Class to listen to exceptions, 
     * occured while loading filters configuration.
     */
    static class MyExceptionListener implements ExceptionListener
    {
        private List exceptionsList = new ArrayList();
        private boolean exceptionOccured = false;
        public void exceptionThrown(Exception e)
        {
            exceptionOccured = true;
            exceptionsList.add(e);
        }
        
        /**
         * Returns whether any exceptions occured.
         */
        public boolean isExceptionOccured()
        {
            return exceptionOccured;
        }
        /**
         * Returns the list of occured exceptions.
         */
        public List getExceptionsList()
        {
            return exceptionsList;
        }
    }
    
    /**
     * Initializes default rules.
     */
    private void init()
    {
        List srules = new ArrayList();
        
        // exceptions first
        srules.add(new Rule(false, "Dr\\.", "\\s"));                            // NOI18N
        srules.add(new Rule(false, "U\\.K\\.", "\\s"));                         // NOI18N
        srules.add(new Rule(false, "M\\.", "\\s"));                             // NOI18N
        srules.add(new Rule(false, "Mr\\.", "\\s"));                            // NOI18N
        srules.add(new Rule(false, "Mrs\\.", "\\s"));                           // NOI18N
        srules.add(new Rule(false, "Ms\\.", "\\s"));                            // NOI18N
        srules.add(new Rule(false, "Prof\\.", "\\s"));                          // NOI18N
        
        srules.add(new Rule(false, "e\\.g\\.", "\\s"));                         // NOI18N
        srules.add(new Rule(false, "resp\\.", "\\s"));                          // NOI18N
        srules.add(new Rule(false, "tel\\.", "\\s"));                           // NOI18N

        // here goes the rule
        srules.add(new Rule(true, "[\\.\\?\\!]+", "\\s"));                      // NOI18N
        // special handling for BR tag to segmenent on it
        // idea by Jean-Christophe Helary
        srules.add(new Rule(true, "<br\\d+>", "\\."));                          // NOI18N

        getMappingRules().add(
                new MapRule(
                OStrings.getString("CORE_SRX_DEFAULT_RULES_NAME"), 
                ".*",                                                           // NOI18N
                srules));
    }
        
    /**
     * Finds the rules for a certain language.
     * <p>
     * Usually (if the user didn't skrew up the setup) there're a default
     * segmentation rules, so it's a good idea to rely on this method 
     * always returning at least some rules.
     * <p>
     * Or in case of a completely screwd setup -- an empty list without any rules.
     */
    public List lookupRulesForLanguage(Language srclang)
    {
        for(int i=0; i<getMappingRules().size(); i++)
        {
            MapRule maprule = (MapRule)getMappingRules().get(i);
            if( maprule.getCompiledPattern().matcher(srclang.getLanguage()).matches() )
                return maprule.getRules();
        }
        return new ArrayList();
    }

    /**
     * Holds value of property segmentSubflows.
     */
    private boolean segmentSubflows = true;

    /**
     * Getter for property segmentSubflows.
     * @return Value of property segmentSubflows.
     */
    public boolean isSegmentSubflows()
    {

        return this.segmentSubflows;
    }

    /**
     * Setter for property segmentSubflows.
     * @param segmentSubflows New value of property segmentSubflows.
     */
    public void setSegmentSubflows(boolean segmentSubflows)
    {

        this.segmentSubflows = segmentSubflows;
    }

    /**
     * Holds value of property includeStartingTags.
     */
    private boolean includeStartingTags;

    /**
     * Getter for property includeStartingTags.
     * @return Value of property includeStartingTags.
     */
    public boolean isIncludeStartingTags()
    {

        return this.includeStartingTags;
    }

    /**
     * Setter for property includeStartingTags.
     * @param includeStartingTags New value of property includeStartingTags.
     */
    public void setIncludeStartingTags(boolean includeStartingTags)
    {
        this.includeStartingTags = includeStartingTags;
    }

    /**
     * Holds value of property includeEndingTags.
     */
    private boolean includeEndingTags = true;

    /**
     * Getter for property includeEndingTags.
     * @return Value of property includeEndingTags.
     */
    public boolean isIncludeEndingTags()
    {
        return this.includeEndingTags;
    }

    /**
     * Setter for property includeEndingTags.
     * @param includeEndingTags New value of property includeEndingTags.
     */
    public void setIncludeEndingTags(boolean includeEndingTags)
    {
        this.includeEndingTags = includeEndingTags;
    }

    /**
     * Holds value of property includeIsolatedTags.
     */
    private boolean includeIsolatedTags;

    /**
     * Getter for property includeIsolatedTags.
     * @return Value of property includeIsolatedTags.
     */
    public boolean isIncludeIsolatedTags()
    {

        return this.includeIsolatedTags;
    }

    /**
     * Setter for property includeIsolatedTags.
     * @param includeIsolatedTags New value of property includeIsolatedTags.
     */
    public void setIncludeIsolatedTags(boolean includeIsolatedTags)
    {

        this.includeIsolatedTags = includeIsolatedTags;
    }
    
    public static void main(String args[])
    {
        SRX.getSRX().save();
    }

    /** Correspondences between languages and their segmentation rules. */
    private List mappingRules = new ArrayList();

    /**
     * Returns all mapping rules at once: 
     * correspondences between languages and their segmentation rules.
     */
    public List getMappingRules()
    {
        return mappingRules;
    }

    /**
     * Sets all mapping rules at once: 
     * correspondences between languages and their segmentation rules.
     */
    public void setMappingRules(List rules)
    {
        mappingRules = rules;
    }

    //////////////////////////////////////////////////////////////////
    // Versioning properties to detect version upgrades
    // and possibly do something if required
    
    /** Version of OmegaT 1.4.6 segmentation support. */
    public static String OT146_VERSION = "0.2";                                 // NOI18N
    /** Currently supported segmentation support version. */
    public static String CURRENT_VERSION =OT146_VERSION;
    
    /** Version of OmegaT segmentation support. */
    private String version;

    /** Returns segmentation support version. */
    public String getVersion()
    {
        return version;
    }

    /** Sets segmentation support version. */
    public void setVersion(String value)
    {
        version = value;
    }
    
}
