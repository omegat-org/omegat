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

import java.util.ArrayList;
import java.util.List;

/**
 * Class with default segmentation rules and exceptions.
 *
 * @author Maxym Mykhalchuk
 */
public class DefaultRules
{
    /** Text files format-related rule. */
    public static List textFormat()
    {
        List srules = new ArrayList();
        
        // special handling for Text files to break on empty indented lines
        // idea by Jean-Christophe Helary
        srules.add(new Rule(true, "\\n", " +"));                                // NOI18N
        
        return srules;
    }

    /** HTML format-related rule. */
    public static List htmlFormat()
    {
        List srules = new ArrayList();
        
        // special handling for BR tag to segmenent on it
        // idea by Jean-Christophe Helary
        srules.add(new Rule(true, "<br\\d+/?>", "."));                          // NOI18N
        
        return srules;
    }

    /** Default language-related segmentation rules. */
    public static List defaultLingual()
    {
        List srules = new ArrayList();
        // ... exception
        srules.add(new Rule(false, "\\.\\.\\.", "\\s+\\P{Lu}"));                // NOI18N
        // .?! break rule
        srules.add(new Rule(true, "[\\.\\?\\!]+", "\\s"));                      // NOI18N
        
        return srules;
    }

    /** English exceptions. */
    public static List english()
    {
        List srules = new ArrayList();
        
        srules.add(new Rule(false, "etc\\.", "\\s+\\P{Lu}"));                   // NOI18N
        
        srules.add(new Rule(false, "Dr\\.", "\\s"));                            // NOI18N
        srules.add(new Rule(false, "U\\.K\\.", "\\s"));                         // NOI18N
        srules.add(new Rule(false, "M\\.", "\\s"));                             // NOI18N
        srules.add(new Rule(false, "Mr\\.", "\\s"));                            // NOI18N
        srules.add(new Rule(false, "Mrs\\.", "\\s"));                           // NOI18N
        srules.add(new Rule(false, "Ms\\.", "\\s"));                            // NOI18N
        srules.add(new Rule(false, "Prof\\.", "\\s"));                          // NOI18N
        
        srules.add(new Rule(false, "(?i)e\\.g\\.", "\\s"));                         // NOI18N
        srules.add(new Rule(false, "(?i)i\\.e\\.", "\\s"));                         // NOI18N
        srules.add(new Rule(false, "resp\\.", "\\s"));                          // NOI18N
        srules.add(new Rule(false, "tel\\.", "\\s"));                           // NOI18N

        return srules;
    }

    /** A bit of Japanese segmentation. */
    public static List japanese()
    {
        List srules = new ArrayList();
        srules.add(new Rule(true, "\u3002", "."));                              // NOI18N
        return srules;
    }
    
    /** Some Russian examples. */
    public static List russian()
    {
        List srules = new ArrayList();
        srules.add(new Rule(false, "(?i)\u0442\\.\u0435\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "(?i)\u0442\\.\u043A\\.", "\\s"));           // NOI18N
        return srules;
    }

    /** Extensive set of exceptions for segmenting German language. */
    public static List german()
    {
        List srules = new ArrayList();

        srules.add(new Rule(false, "www\\.", ".*"));           // NOI18N
        srules.add(new Rule(false, ".*", "\\.at"));           // NOI18N
        srules.add(new Rule(false, ".*", "\\.de"));           // NOI18N
        
        srules.add(new Rule(false, "a\\.a\\.O\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Abb\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Abf\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Abk\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Abo\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Abs\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Abt\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "abzgl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "a\\.D\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Adr\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "a\\.M\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "am\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "amtl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Anh\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Ank\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Anl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Anm\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "a\\.Rh\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "A\\.T\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Aufl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "b\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Bd\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "beil\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "bes\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Best\\.-Nr\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Betr\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Bez\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Bhf\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "b\\.w\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "bzgl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "bzw\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "ca\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Chr\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "d\\.\u00c4\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "dgl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "d\\.h\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Dipl\\.-Ing\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Dipl\\.-Kfm\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Dir\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "d\\.J\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Dr\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Dr\\.\\smed\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Dr\\.\\sphil\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "dt\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Dtzd\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "e\\.h\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "ehem\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "eigtl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "einschl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "entspr\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "erb\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "erw\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Erw\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "ev\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "e\\.V\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "evtl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "e\\.Wz\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "exkl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "f\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Fa\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Fam\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "ff\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "F\\.f\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Ffm\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Forts\\.\\sf\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Fr\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Frl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "frz\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "geb\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Gebr\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "gedr\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "gegr\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "gek\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Ges\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "gesch\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "gest\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "gez\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "ggf\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "ggfs\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Hbf\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "hpts\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Hptst\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Hr\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Hrn\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Hrsg\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "i\\.A\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "i\\.b\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "i\\.B\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "i\\.H\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "i\\.J\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Ing\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Inh\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "inkl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "i\\.R\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "i\\.V\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "jew\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Jh\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "jhrl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Kap\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "kath\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Kfm\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "kfm\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "kgl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Kl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "k\\.o\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "k\\.u\\.k\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "l\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "led\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "m\\.E\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Mio\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "m\u00f6bl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Mrd\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "m\\.W\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "MwSt\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "n\u00e4ml\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "n\\.Chr\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Nr\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "n\\.u\\.Z\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "o\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "o\\.B\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Obb\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "od\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "\u00f6sterr\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "p\\.Adr\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Pfd\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Pl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "r\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Reg\\.-Bez\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "r\\.k\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "r\\.-k\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "r\u00f6m\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "r\u00f6m\\.-kath\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "S\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "s\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "s\\.a\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Sa\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "schles\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "schw\u00e4b\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "schweiz\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "s\\.o\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "So\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "sog\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "St\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Str\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "StR\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "str\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "s\\.u\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "s\u00fcdd\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "t\u00e4gl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "u\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "u\\.a\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "u\\.\u00e4\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "u\\.\u00c4\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "u\\.a\\.m\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "u\\.A\\.w\\.g\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "usw\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "u\\.v\\.a\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "u\\.v\\.a\\.m\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "u\\.U\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "V\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "v\\.Chr\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Verf\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "verh\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "verw\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "vgl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "v\\.H\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "vorm\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "v\\.R\\.w\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "v\\.T\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "v\\.u\\.Z\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "wg\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "z\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "z\\.B\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "z\\.Hd\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Zi\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "zur\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "zus\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "z\\.T\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Ztr\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "zzgl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "z\\.Z\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Elekt\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Stck\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "mind\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "min\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "max\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "sep\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "spez\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Mio\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "\\(s\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "e\\.V\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "\\sempf\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "engl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Fa\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Co\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Ca\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "ca\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "engl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "etc\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "gem\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "insg\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "i\\.d\\.r\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "\\slt\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "\\sa\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Std\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "\\su\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "p\\.\\sa\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Pos\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Prof\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "glw\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "\\ssec\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Stellv\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "stv\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Tab\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "u\\.\\sa\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Red\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "z\\.\\sT\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "d\\.\\sh\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "\\p{Lu}\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Rel\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "iqpr\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "\\sa\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Sek\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "\\ss\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "\\srd\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "\\sp\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "\\sB\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "[0-9]\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Zi\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Altb\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Ausst\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "App\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Blk\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Bj\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "bezugsf\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Hzg\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "erl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "freist\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Ant\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Ben\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Mitben\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "geh\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "gehob\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Grdst\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Ges\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Hdl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "incl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Mo\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Di\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Mi\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Do\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Fr\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "\u00e4\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Elektr\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Stck\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "pl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "sing\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Inv\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "j\u00e4hrl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Kaut\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Einstpl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Ki\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "kinderfreundl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "kl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Kochgel\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "kpl.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "m\u00f6bl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "lux\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "mod\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "mtl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "neuw\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Nfl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "off\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "n\\.V\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Prov\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "ren\\.-bed\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "selbst\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "sep\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Stud\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Tel\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Terr\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Umgeb\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Verk\\.-Anb\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Verk\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Wohnfl\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Zim\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "verm\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "Jg\\.", "\\s"));           // NOI18N
        srules.add(new Rule(false, "einschl\\.", "\\s"));           // NOI18N
        
        return srules;
    }
    
}
