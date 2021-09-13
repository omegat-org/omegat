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
package org.omegat.core.protocol.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.io.Charsets;


/**
 * The data protocol connection driver.
 * <p>
 * Data protocol Syntax
 *      data:[<mediatype>][;base64],<data>
 * inspired from StackOverflow.
 *
 * @author Hiroshi Miura
 * @see <a href="https://stackoverflow.com/questions/9388264/jeditorpane-with-inline-image/9388757#9388757">
 *     JEditorPane with inline image</a>
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/Data_URIs">Data URLs</a>
 */
public class DataConnection extends URLConnection {

    private static final String DATA_PROTO_RE =  "data:((.*?/.*?)?(?:;(.*?)=(.*?))?)(?:;(base64)?)?,(.*)";
    private static final String DEFAULT_MIME_TYPE = "text/plain;charset=US-ASCII";

    private final Matcher m;

    public DataConnection(final URL u) throws MalformedURLException {
        super(u);
        Pattern re = Pattern.compile(DATA_PROTO_RE);
        m = re.matcher(url.toString());
        connected = m.matches();
        if (!connected) {
            throw new MalformedURLException("Wrong data protocol URL");
        }
    }

    @Override
    public void connect() {
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (!connected) {
            throw new IOException();
        }
        return new ByteArrayInputStream(getData());
    }

    /**
     * Returns the value of the content-type header field.
     * <p>
     * data protocol defined with optional content-type field.
     * If omitted, defaults to text/plain;charset=US-ASCII.
     * @return. the content type of the resource that the URL references.
     */
    @Override
    public String getContentType() {
        if (!connected) {
            return null;
        }
        String contentType = m.group(1);
        if (contentType == null || "".equals(contentType)) {
            return DEFAULT_MIME_TYPE;
        }
        return contentType;
    }

    private byte[] getData() {
        String type = m.group(2);
        String attribute = m.group(3);
        String charset = m.group(4);
        String b64 = m.group(5);
        String data = m.group(6);
        if ("base64".equals(b64)) {
            return Base64.decodeBase64(data);
        }
        try {
            if (!"charset".equals(attribute)) {
                return getText(data, "US-ASCII");
            }
            if (type.startsWith("text/")) {
                return getText(data, charset);
            }
        } catch (DecoderException ignore) {
        }
        return new byte[0];
    }

    private byte[] getText(final String data, final String charset) throws DecoderException {
        URLCodec codec = new URLCodec(charset);
        Charset cs = Charsets.toCharset(charset);
        return codec.decode(data).getBytes(cs);
    }
}
