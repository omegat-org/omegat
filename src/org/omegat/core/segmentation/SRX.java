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

package org.omegat.core.segmentation;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class SRX implements Serializable, Cloneable
{
    
    private static SRX srx = null;
    private static final String CONF_SENTSEG = "segmentation.conf";             // NOI18N
    private static final File configFile=new File(
            StaticUtils.getConfigDir()+CONF_SENTSEG);
            
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
     * Initializes SRX rules to defaults.
     */
    public static void init()
    {
        srx = new SRX();
        srx.initDefaults();
    }
    
    /** 
     * Creates an empty SRX, without any rules.
     * <p>
     * Please do not call directly unless you know what you are doing.
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
            String message = StaticUtils.format(
                "CORE_SRX_ERROR_SAVING_SEGMENTATION_CONFIG",
                new Object[] {ioe});
            StaticUtils.logError("CORE_SRX_ERROR_SAVING_SEGMENTATION_CONFIG", message);
            ioe.printStackTrace(StaticUtils.getLogStream());
            JOptionPane.showMessageDialog(null,
                    message,
                    OStrings.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Loads segmentation rules from an XML file.
     * If there's an error loading a file, it calls <code>initDefaults</code>.
     * <p>
     * Since 1.6.0 RC8 it also checks if the version of segmentation rules
     * saved is older than that of the current OmegaT, and tries to merge
     * the two sets of rules.
     */
    private static SRX load()
    {
        SRX res;
        try
        {
            
            MyExceptionListener myel = new MyExceptionListener();
            XMLDecoder xmldec = new XMLDecoder(
                    new FileInputStream(configFile), null, myel);
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
                StaticUtils.logErrorRB(
                    "CORE_SRX_EXC_LOADING_SEG_RULES",
                    new Object[] {sb.toString()});
                res = new SRX();
                res.initDefaults();
                return res;
            }
            
            // checking the version
            if (CURRENT_VERSION.compareTo(res.getVersion())>0)
            {
                // yeap, the segmentation config file is of the older version
                
                // initing defaults
                SRX defaults = new SRX();
                defaults.initDefaults();
                // and merging them into loaded rules
                res = merge(res, defaults);
            }
        }
        catch( Exception e )
        {
            // silently ignoring FNF
            if( !(e instanceof FileNotFoundException) )
            {
                StaticUtils.log(e.getMessage());
                e.printStackTrace(StaticUtils.getLogStream());
            }
            res = new SRX();
            res.initDefaults();
        }
        return res;
    }
    
    /** Merges two sets of segmnetation rules together. */
    private static SRX merge(SRX current, SRX defaults)
    {
        current = upgrade(current, defaults);
        
        int defaultMapRulesN = defaults.getMappingRules().size();
        for (int i=0; i<defaultMapRulesN; i++)
        {
            MapRule dmaprule = (MapRule)defaults.getMappingRules().get(i);
            String dcode = dmaprule.getLanguageCode();
            // trying to find
            boolean found = false;
            int currentMapRulesN = current.getMappingRules().size();
            MapRule cmaprule = null;
            for (int j=0; j<currentMapRulesN; j++)
            {
                cmaprule = (MapRule)current.getMappingRules().get(j);
                String ccode = cmaprule.getLanguageCode();
                if (dcode.equals(ccode)) 
                {
                    found = true;
                    break;
                }
            }
                
            if (found)
            {
                // merging -- adding those rules not there in current list
                List crules = cmaprule.getRules();
                List drules = dmaprule.getRules();
                for (int j=0; j<drules.size(); j++)
                {
                    Rule drule = (Rule)drules.get(j);
                    if (!crules.contains(drule))
                    {
                        if (drule.isBreakRule())
                        {
                            // breaks go to the end
                            crules.add(drule);
                        }
                        else
                        {
                            // exceptions go before the first break rule
                            int currentRulesN = crules.size();
                            int firstBreakRuleN = currentRulesN;
                            for (int k=0; k<currentRulesN; k++)
                            {
                                Rule crule = (Rule)crules.get(k);
                                if (crule.isBreakRule())
                                {
                                    firstBreakRuleN = k;
                                    break;
                                }
                            }
                            crules.add(firstBreakRuleN, drule);
                        }
                    }
                }
            }
            else
            {
                // just adding before the default rules
                int englishN = currentMapRulesN;
                for (int j=0; j<currentMapRulesN; j++)
                {
                    cmaprule = (MapRule)current.getMappingRules().get(j);
                    String cpattern = cmaprule.getPattern();
                    if (DEFAULT_RULES_PATTERN.equals(cpattern))
                    {
                        englishN = j;
                        break;
                    }
                }
                current.getMappingRules().add(englishN, dmaprule);
            }
        }
        return current;
    }
    
    /** Implements some upgrade heuristics. */
    private static SRX upgrade(SRX current, SRX defaults)
    {
        // renaming "Default (English)" to "Default"
        // and removing English/Text/HTML-specific rules from there
        if( OT160RC9_VERSION.equals(CURRENT_VERSION) )
        {
            String DEF = "Default (English)";                                   // NOI18N
            for(int i=0; i<current.getMappingRules().size(); i++)
            {
                MapRule maprule = (MapRule)current.getMappingRules().get(i);
                if( DEF.equals(maprule.getLanguageCode()) )
                {
                    maprule.setLanguage(LanguageCodes.DEFAULT_CODE);
                    maprule.getRules().removeAll(DefaultRules.english());
                    maprule.getRules().removeAll(DefaultRules.textFormat());
                    maprule.getRules().removeAll(DefaultRules.htmlFormat());
                }
            }
        }
        return current;
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

    // Patterns
    private static final String DEFAULT_RULES_PATTERN = ".*";                   // NOI18N
    private static final String ENGLISH_RULES_PATTERN = "EN.*";                 // NOI18N
    private static final String JAPANESE_RULES_PATTERN = "JA.*";                // NOI18N
    private static final String RUSSIAN_RULES_PATTERN = "RU.*";                 // NOI18N
    private static final String GERMAN_RULES_PATTERN = "DE.*";                  // NOI18N
    
    /**
     * Initializes default rules.
     */
    private void initDefaults()
    {
        List srules;

        // Extensive set of German exceptions
        getMappingRules().add(new MapRule(
                LanguageCodes.GERMAN_CODE,
                GERMAN_RULES_PATTERN, 
                DefaultRules.german()));
        
        // Russian as an example
        getMappingRules().add(new MapRule(
                LanguageCodes.RUSSIAN_CODE,
                RUSSIAN_RULES_PATTERN, 
                DefaultRules.russian()));
        
        // now Japanese
        getMappingRules().add(new MapRule(
                LanguageCodes.JAPANESE_CODE,
                JAPANESE_RULES_PATTERN, 
                DefaultRules.japanese()));

        // now English
        getMappingRules().add(new MapRule(
                LanguageCodes.ENGLISH_CODE,
                ENGLISH_RULES_PATTERN, 
                DefaultRules.english()));

        // default lingual rules
        getMappingRules().add(new MapRule(
                LanguageCodes.DEFAULT_CODE,
                DEFAULT_RULES_PATTERN, 
                DefaultRules.defaultLingual()));

        // segmentation for text files
        getMappingRules().add(new MapRule(
                LanguageCodes.F_TEXT_CODE,
                DEFAULT_RULES_PATTERN, 
                DefaultRules.textFormat()));

        // segmentation for (X)HTML files
        getMappingRules().add(new MapRule(
                LanguageCodes.F_HTML_CODE,
                DEFAULT_RULES_PATTERN, 
                DefaultRules.htmlFormat()));
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
        List rules = new ArrayList();
        for(int i=0; i<getMappingRules().size(); i++)
        {
            MapRule maprule = (MapRule)getMappingRules().get(i);
            if( maprule.getCompiledPattern().matcher(srclang.getLanguage()).matches() )
                rules.addAll(maprule.getRules());
        }
        return rules;
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
    
    /** 
     * Correspondences between languages and their segmentation rules. 
     * Each element is of class {@link MapRule}. 
     */
    private List mappingRules = new ArrayList();

    /**
     * Returns all mapping rules (of class {@link MapRule}) at once: 
     * correspondences between languages and their segmentation rules.
     */
    public List getMappingRules()
    {
        return mappingRules;
    }

    /**
     * Sets all mapping rules (of class {@link MapRule}) at once: 
     * correspondences between languages and their segmentation rules.
     */
    public void setMappingRules(List rules)
    {
        mappingRules = rules;
    }

    //////////////////////////////////////////////////////////////////
    // Versioning properties to detect version upgrades
    // and possibly do something if required
    
    /** Initial version of segmentation support (1.4.6 beta 4 -- 1.6.0 RC7). */
    public static String INITIAL_VERSION = "0.2";                               // NOI18N
    /** Segmentation support of 1.6.0 RC8 (a bit more rules added). */
    public static String OT160RC8_VERSION = "0.2.1";                            // NOI18N
    /** Segmentation support of 1.6.0 RC9 (rules separated). */
    public static String OT160RC9_VERSION = "0.2.2";                            // NOI18N
    /** Currently supported segmentation support version. */
    public static String CURRENT_VERSION = OT160RC9_VERSION;
    
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
