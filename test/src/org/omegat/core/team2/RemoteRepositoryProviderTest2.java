/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay, Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: https://omegat.org/support

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

package org.omegat.core.team2;

import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class RemoteRepositoryProviderTest2 {


    @Test
    public void testRelativeRemoteToAbsoluteLocal() {
        assertEquals(new File("/home/project/file.txt"), RemoteRepositoryProvider.relativeRemoteToAbsoluteLocal("file.txt", new File("/home/project"), "/", "/"));
        assertEquals(new File("/home/project/file.txt"), RemoteRepositoryProvider.relativeRemoteToAbsoluteLocal("file.txt", new File("/home/project"), "", ""));
        assertEquals(new File("/home/project/file.txt"), RemoteRepositoryProvider.relativeRemoteToAbsoluteLocal("file.txt", new File("/home/project"), "", "/"));
        assertEquals(new File("/home/project/file.txt"), RemoteRepositoryProvider.relativeRemoteToAbsoluteLocal("file.txt", new File("/home/project"), "/", ""));

        assertEquals(new File("/home/project/source/file.txt"), RemoteRepositoryProvider.relativeRemoteToAbsoluteLocal("somedir/file.txt", new File("/home/project"), "somedir", "source"));
        assertEquals(new File("/home/project/source/file.txt"), RemoteRepositoryProvider.relativeRemoteToAbsoluteLocal("somedir/file.txt", new File("/home/project"), "somedir", "source/"));
        assertEquals(new File("/home/project/source/file.txt"), RemoteRepositoryProvider.relativeRemoteToAbsoluteLocal("somedir/file.txt", new File("/home/project"), "somedir/", "source"));
        assertEquals(new File("/home/project/source/file.txt"), RemoteRepositoryProvider.relativeRemoteToAbsoluteLocal("somedir/file.txt", new File("/home/project"), "/somedir/", "source"));

        assertEquals(new File("/home/project/source/somedir/file.txt"), RemoteRepositoryProvider.relativeRemoteToAbsoluteLocal("somedir/file.txt", new File("/home/project"), "/", "/source"));

    }

    @Test
    public void testWithoutslashes() {
        assertEquals("aa", RemoteRepositoryProvider.withoutSlashes("/aa/"));
        assertEquals("aa", RemoteRepositoryProvider.withoutSlashes("aa"));
        assertEquals("aa", RemoteRepositoryProvider.withoutSlashes("aa/"));
    }
}
