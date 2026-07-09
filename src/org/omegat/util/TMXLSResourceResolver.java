/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
               Home page: https://www.omegat.org/
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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/
package org.omegat.util;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

class TMXLSResourceResolver implements LSResourceResolver {

    private String encoding;
    private String publicId;
    private String systemId;
    private String baseURI;

    TMXLSResourceResolver() {
        encoding = StandardCharsets.UTF_8.name();
    }

    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId,
                                   String baseURI) {
        this.publicId = publicId;
        this.systemId = systemId;
        this.baseURI = baseURI;
        if (systemId.endsWith("tmx11.dtd")) {
            return getLsInput("/schemas/tmx11.dtd");
        } else if (systemId.endsWith("tmx14.dtd")) {
            return getLsInput("/schemas/tmx14.dtd");
        } else {
            return null;
        }
    }

    private @NotNull LSInput getLsInput(String dtd) {
        return new LSInput() {
            @Override
            public Reader getCharacterStream() {
                InputStream is = TMXReader2.class.getResourceAsStream(dtd);
                if (is == null) {
                    return new StringReader("");
                }
                return new InputStreamReader(is, StandardCharsets.UTF_8);
            }

            @Override
            public void setCharacterStream(Reader characterStream) {
            }

            @Override
            public InputStream getByteStream() {
                return TMXReader2.class.getResourceAsStream(dtd);
            }

            @Override
            public void setByteStream(InputStream byteStream) {
            }

            @Override
            public String getStringData() {
                try (InputStream is = TMXReader2.class.getResourceAsStream(dtd)) {
                    if (is == null) {
                        return "";
                    }
                    return new String(is.readAllBytes(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    return "";
                }
            }

            @Override
            public void setStringData(String stringData) {
            }

            @Override
            public String getSystemId() {
                return systemId;
            }

            @Override
            public void setSystemId(String newsystemId) {
                systemId = newsystemId;
            }

            @Override
            public String getPublicId() {
                return publicId;
            }

            @Override
            public void setPublicId(String newPublicId) {
                publicId = newPublicId;
            }

            @Override
            public String getBaseURI() {
                return baseURI;
            }

            @Override
            public void setBaseURI(String newBaseURI) {
                baseURI = newBaseURI;
            }

            @Override
            public String getEncoding() {
                return encoding;
            }

            @Override
            public void setEncoding(String newEncoding) {
                encoding = newEncoding;
            }

            @Override
            public boolean getCertifiedText() {
                return false;
            }

            @Override
            public void setCertifiedText(boolean certifiedText) {
            }
        };
    }
}
