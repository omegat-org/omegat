/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Zoltan Bartko
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.editor.autotext;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;
import org.omegat.util.OConsts;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;

/**
 *
 * @author bartkoz
 */
public class Autotext {
    
    private List<AutotextPair> list = new ArrayList<AutotextPair>();
    
    private String name;
    
    public Autotext(String name) {
        this.name = name;
        
        if (name != null)
            load(name);
    }
    
    public List<AutotextPair> getList() {
        return list;
    }

    public void processLine(String thisLine) {
        if (thisLine == null || thisLine.isEmpty() || thisLine.trim().isEmpty())
            return;
        
        String parts[] = thisLine.split("\t");

        if (parts.length == 2) {
            list.add(new AutotextPair(parts[0], parts[1], ""));
        }
        if (parts.length == 3)
            list.add(new AutotextPair(parts[0], parts[1], parts[2]));
    }
    
    public void load(String fileName) {
        list.clear();
        //separator = Core.get
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    fileName), OConsts.UTF8));

            String thisLine;
            while ((thisLine = br.readLine()) != null) {
                processLine(thisLine);
            }
        } catch (FileNotFoundException ex) {
            // there is no default, so load nothing
        } catch (IOException ex) {
            // so now what?
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                // so now what?
            }
        }
    }

    public void save(String filename) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), OConsts.UTF8));

        StringBuilder builder = new StringBuilder();
        for (AutotextPair pair:list) {
            builder.replace(0, builder.length(), "");
            builder.append(pair.source).append("\t");
            builder.append(pair.target).append("\t");
            builder.append(pair.comment).append("\n");
            out.write(builder.toString());
        }
        
        out.close();
    }
    
    public void save() throws IOException {
        save(StaticUtils.getConfigDir() + Preferences.AC_AUTOTEXT_FILE_NAME);
    }
}
