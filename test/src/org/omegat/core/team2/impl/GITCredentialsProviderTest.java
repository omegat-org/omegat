/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021 Hiroshi Miura
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

package org.omegat.core.team2.impl;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class GITCredentialsProviderTest {

    private final String inputTest;
    private final String expected;

    public GITCredentialsProviderTest(String inputText, String expected) {
        this.inputTest = inputText;
        this.expected = expected;
    }

    @Test
    public void extractFingerprint() {
        String fingerPrint = GITCredentialsProvider.extractFingerprint(inputTest);
        assertEquals(fingerPrint, expected);
    }

    @Parameterized.Parameters
    public static List<String[]> unknownHostKeyMessages() {
        return Arrays.asList(new String[][]{
                {"The authenticity of host 'example.example.com' cannot be established.\n"
                        + "The EC key's fingerprints are:\n"
                        + "MD5:27:eb:84:a1:af:13:be:e6:7d:8a:20:fa:93:87:29:7b\n"
                        + "SHA256:Pv1a78W/c6tlPKyxTuT3Ziw6n8vXLTQiGfgR+NkU6fk\n"
                        + "Accept and store this key, and continue connecting?",
                 "Pv1a78W/c6tlPKyxTuT3Ziw6n8vXLTQiGfgR+NkU6fk"},
                {"The authenticity of host '192.0.2.1' can't be established.\n"
                        + "ECDSA key fingerprint is SHA256:cdDZrkZGXs01lb5r1Q93qGPkNxd+EiMrre5C0o3dSZ1.\n"
                        + "Are you sure you want to continue connecting?",
                 "cdDZrkZGXs01lb5r1Q93qGPkNxd+EiMrre5C0o3dSZ1"},
                {"The authenticity of host '192.0.2.1' can't be established.\n"
                        + "RSA key fingerprint is 27:eb:84:a1:af:13:be:e6:7d:8a:20:fa:93:87:29:7b.\n"
                        + "Are you sure you want to continue connecting?",
                 "27:eb:84:a1:af:13:be:e6:7d:8a:20:fa:93:87:29:7b"}
        });
    }
}
