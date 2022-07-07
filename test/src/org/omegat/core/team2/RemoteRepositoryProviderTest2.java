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

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

public class RemoteRepositoryProviderTest2 {


    @Test
    public void testRelativeRemoteToAbsoluteLocal() {
        String base = System.getProperty("java.io.tmpdir") + File.separator;
        assertEquals(new File(base + "file.txt"),
                RemoteRepositoryProvider.relativeRemoteToAbsoluteLocal("file.txt", new File(base), "/", "/"));
        assertEquals(new File(base + "file.txt"),
                RemoteRepositoryProvider.relativeRemoteToAbsoluteLocal("file.txt", new File(base), "", ""));
        assertEquals(new File(base + "file.txt"),
                RemoteRepositoryProvider.relativeRemoteToAbsoluteLocal("file.txt", new File(base), "", "/"));
        assertEquals(new File(base + "file.txt"),
                RemoteRepositoryProvider.relativeRemoteToAbsoluteLocal("file.txt", new File(base), "/", ""));

        assertEquals(new File(base + "source/file.txt"),
                RemoteRepositoryProvider.relativeRemoteToAbsoluteLocal("somedir/file.txt", new File(base), "somedir", "source"));
        assertEquals(new File(base + "source/file.txt"),
                RemoteRepositoryProvider.relativeRemoteToAbsoluteLocal("somedir/file.txt", new File(base), "somedir", "source/"));
        assertEquals(new File(base + "source/file.txt"),
                RemoteRepositoryProvider.relativeRemoteToAbsoluteLocal("somedir/file.txt", new File(base), "somedir/", "source"));
        assertEquals(new File(base + "source/file.txt"),
                RemoteRepositoryProvider.relativeRemoteToAbsoluteLocal("somedir/file.txt", new File(base), "/somedir/", "source"));

        assertEquals(new File(base + "source/somedir/file.txt"),
                RemoteRepositoryProvider.relativeRemoteToAbsoluteLocal("somedir/file.txt", new File(base), "/", "/source"));

    }

    @Test
    public void testWithoutSlashes() {
        assertEquals("aa", RemoteRepositoryProvider.withoutSlashes("/aa/"));
        assertEquals("aa", RemoteRepositoryProvider.withoutSlashes("aa"));
        assertEquals("aa", RemoteRepositoryProvider.withoutSlashes("aa/"));
        assertEquals("aa", RemoteRepositoryProvider.withoutSlashes("/aa"));
        assertEquals("a/b/c/d", RemoteRepositoryProvider.withoutSlashes("/a/b/c/d/"));
    }

    @Test
    public void testWithSlashes() {
        assertEquals("/aa/", RemoteRepositoryProvider.withSlashes("/aa/"));
        assertEquals("/aa/", RemoteRepositoryProvider.withSlashes("aa"));
        assertEquals("/aa/", RemoteRepositoryProvider.withSlashes("aa/"));
        assertEquals("/aa/", RemoteRepositoryProvider.withSlashes("/aa"));
        assertEquals("/a/b/c/d/", RemoteRepositoryProvider.withSlashes("a/b/c/d"));
    }

    @Test
    public void testWithLeadingSlash() {
        assertEquals("/aa/", RemoteRepositoryProvider.withLeadingSlash("/aa/"));
        assertEquals("/aa", RemoteRepositoryProvider.withLeadingSlash("aa"));
        assertEquals("/aa/", RemoteRepositoryProvider.withLeadingSlash("aa/"));
        assertEquals("/aa", RemoteRepositoryProvider.withLeadingSlash("/aa"));
        assertEquals("/a/b/c/d", RemoteRepositoryProvider.withLeadingSlash("a/b/c/d"));
    }
}
