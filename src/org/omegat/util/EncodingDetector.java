/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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

package org.omegat.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.mozilla.universalchardet.UniversalDetector;

public class EncodingDetector {
    
    /**
     * Detect the encoding of the supplied file.
     * Convenience method for {@link #detectEncoding(java.io.InputStream)}.
     */
    public static String detectEncoding(File inFile) throws IOException {
        FileInputStream stream = new FileInputStream(inFile);
        try {
            return detectEncoding(stream);
        } finally {
            stream.close();
        }
    }
    
    /**
     * Detect the encoding of the supplied file.
     * @see Original https://code.google.com/p/juniversalchardet/
     * @see Fork https://github.com/amake/juniversalchardet
     */
    public static String detectEncoding(InputStream stream) throws IOException {
        UniversalDetector detector = new UniversalDetector(null);
        
        byte[] buffer = new byte[4096];
        int read;
        while ((read = stream.read(buffer)) > 0 && !detector.isDone()) {
            detector.handleData(buffer, 0, read);
        }
        
        detector.dataEnd();
        
        String encoding = detector.getDetectedCharset();
        detector.reset();
        
        return encoding;
    }
    
    /**
     * Detect the encoding of the supplied file. If detection fails, return the supplied
     * default encoding.
     */
    public static String detectEncodingDefault(File inFile, String defaultEncoding) {
        String detected = null;
        try {
            detected = detectEncoding(inFile);
        } catch (IOException ex) {
            // Ignore
        }
        return detected == null ? defaultEncoding : detected;
    }
}
