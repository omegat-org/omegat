/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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

import gen.core.tmx14.Tu;
import gen.core.tmx14.Tuv;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.omegat.core.segmentation.Segmenter;
import org.omegat.util.StringUtil;
import org.omegat.util.TMXReader2;

/**
 * Class for store data from TMX from /tm/ folder. They are used only for fuzzy matches.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ExternalTMX {

    private final String name;

    private final List<TMXEntry> entries;

    public ExternalTMX(String name, List<TMXEntry> entries) {
        this.name = name;
        this.entries = entries;
    }

    public ExternalTMX(final ProjectProperties props, final File file) throws Exception {
        this.name = file.getName();
        entries = new ArrayList<TMXEntry>();

        TMXReader2.LoadCallback loader = new TMXReader2.LoadCallback() {
            public void onTu(Tu tu, Tuv tuvSource, Tuv tuvTarget, boolean isParagraphSegtype) {
                String changer = StringUtil.nvl(tuvTarget.getChangeid(), tuvTarget.getCreationid(),
                        tu.getChangeid(), tu.getCreationid());
                String dt = StringUtil.nvl(tuvTarget.getChangedate(), tuvTarget.getCreationdate(),
                        tu.getChangedate(), tu.getCreationdate());

                List<String> sources = new ArrayList<String>();
                List<String> targets = new ArrayList<String>();
                Segmenter.segmentEntries(props.isSentenceSegmentingEnabled() && isParagraphSegtype,
                        props.getSourceLanguage(), tuvSource.getSeg(), props.getTargetLanguage(),
                        tuvTarget.getSeg(), sources, targets);

                for (int i = 0; i < sources.size(); i++) {
                    TMXEntry te = new TMXEntry(sources.get(i), targets.get(i), changer,
                            TMXReader2.parseISO8601date(dt));
                    entries.add(te);
                }
            }
        };

        TMXReader2.readTMX(file, props.getSourceLanguage(), props.getTargetLanguage(),
                props.isSentenceSegmentingEnabled(), false, loader);
    }

    public String getName() {
        return name;
    }

    public List<TMXEntry> getEntries() {
        return entries;
    }
}
