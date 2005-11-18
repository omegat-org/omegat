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

package org.omegat.tools.tmx.sentseg;

import java.io.*;
import java.util.List;
import org.omegat.core.segmentation.Segmenter;

import org.omegat.filters2.TranslationException;
import org.omegat.util.*;

/**
 * The class that does the resegmenting:
 * <ul>
 * <li>It loads the TMX
 * <li>Iterates through each segment
 * <li>Breaks each segment into sentences
 * <li>Saves the new TMX with sentences as segments
 * </ul>
 *
 * @author Maxym Mykhalchuk
 */
public class TMXResegmenter
{
    private String sourceTMX;
    private String targetTMX;
    
    /** Creates a new instance of TMXResegmenter */
    public TMXResegmenter(String sourceTMX, String targetTMX)
    {
        this.sourceTMX = sourceTMX;
        this.targetTMX = targetTMX;
    }
    
    // post-resegmentation information
    
    private int ssn;
    /** The number of source segments */
    public int getSourceSegmentsNumber()
    {
        return ssn;
    }
   
    private int tsn;
    /** The number of target segments */
    public int getTargetSegmentsNumber()
    {
        return tsn;
    }
    
    private int msn;
    /** The number of missegmented segments */
    public int getMissegmentedSegmentsNumber()
    {
        return msn;
    }    
    
    /**
     * Does the resegmenting.
     */
    public void resegment() throws IOException, TranslationException
    {
        TMXReader tmx = new TMXReader("UTF-8");
        tmx.loadFile(sourceTMX);
        String srclang = tmx.getSourceLanguage();
        String tarlang = tmx.getTargetLanguage();
        Writer writer = createWriter();
        writeHeader(writer, srclang, tarlang);
        ssn = tmx.numSegments();
        if( sourceTMX.equals(targetTMX) )
            LFileCopy.copy(sourceTMX, sourceTMX+".ori");                        // NOI18N
        tsn = 0;
        msn = 0;
        for (int i=0; i<ssn; i++)
        {
            String src = tmx.getSourceSegment(i);
            String tar = tmx.getTargetSegment(i);
            
            List srcSegments = Segmenter.segment(src, null);
            List tarSegments = Segmenter.segment(tar, null);
            
            int n = srcSegments.size();
            if( n==tarSegments.size() )
            {
                tsn += n;
                if( n==1 )
                    writeSegment(writer, srclang, src, tarlang, tar);
                else
                    for(int j=0; j<n; j++)
                    {
                        String srcseg = (String)srcSegments.get(j);
                        String tarseg = (String)tarSegments.get(j);
                        writeSegment(writer, srclang, srcseg, tarlang, tarseg);
                    }
            }
            else
            {
                System.out.println("Not resegmented (number of segments in source and target mismatched):");
                System.out.println(src);
                System.out.println("-");
                System.out.println(tar);
                System.out.println("===");
                writeSegment(writer, srclang, src, tarlang, tar);
                msn++;
            }
        }
        writeFooter(writer);
        writer.close();
    }
    
    private Writer createWriter() throws FileNotFoundException, UnsupportedEncodingException
    {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetTMX), "UTF-8"));
    }
    
    /** Writes TMX file header */
    private void writeHeader(Writer writer, String srclang, String tarlang) throws IOException
    {
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        writer.write("<!DOCTYPE tmx SYSTEM \"tmx11.dtd\">\n");
        writer.write("<tmx version=\"1.1\">\n");
        writer.write("  <header\n");
        writer.write("    creationtool=\"OmegaT\"\n");
        writer.write("    creationtoolversion=\"1.6\"\n");
        writer.write("    segtype=\"sentence\"\n");
        writer.write("    o-tmf=\"OmegaT TMX\"\n");
        writer.write("    adminlang=\"EN-US\"\n");
        writer.write("    srclang=\"" + srclang + "\"\n");
        writer.write("  >\n");
        writer.write("  </header>\n");
        writer.write("  <body>\n");
    }
    
    /** Writes one segment */
    private void writeSegment(Writer writer, String srclang, String srcseg,
            String tarlang, String tarseg) throws IOException
    {
        srcseg = StaticUtils.makeValidXML(srcseg);
        tarseg = StaticUtils.makeValidXML(tarseg);
        writer.write("    <tu>\n");
        writer.write("      <tuv lang=\"" + srclang + "\">\n");
        writer.write("        <seg>" + srcseg + "</seg>\n");
        writer.write("      </tuv>\n");
        writer.write("      <tuv lang=\"" + tarlang + "\">\n");
        writer.write("        <seg>" + tarseg + "</seg>\n");
        writer.write("      </tuv>\n");
        writer.write("    </tu>\n");
    }
    
    /** Writes TMX footer */
    private void writeFooter(Writer writer) throws IOException
    {
        writer.write("  </body>\n");
        writer.write("</tmx>\n");
    }
    
}
