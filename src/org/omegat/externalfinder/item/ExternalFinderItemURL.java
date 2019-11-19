/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Chihiro Hio, Aaron Madlon-Kay
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

package org.omegat.externalfinder.item;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.omegat.util.OStrings;

/**
 * A data class representing an ExternalFinder "url". Immutable. Optionally use
 * {@link Builder} to construct.
 */
public class ExternalFinderItemURL {

    private final String url;
    private final ExternalFinderItem.TARGET target;
    private final ExternalFinderItem.ENCODING encoding;

    public ExternalFinderItemURL(String url, ExternalFinderItem.TARGET target,
            ExternalFinderItem.ENCODING encoding) {
        this.url = url;
        this.target = target;
        this.encoding = encoding;
    }

    public String getURL() {
        return url;
    }

    public ExternalFinderItem.TARGET getTarget() {
        return target;
    }

    public ExternalFinderItem.ENCODING getEncoding() {
        return encoding;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((encoding == null) ? 0 : encoding.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ExternalFinderItemURL other = (ExternalFinderItemURL) obj;
        if (encoding != other.encoding) {
            return false;
        }
        if (target != other.target) {
            return false;
        }
        if (url == null) {
            if (other.url != null) {
                return false;
            }
        } else if (!url.equals(other.url)) {
            return false;
        }
        return true;
    }

    public URI generateURL(String findingWords) throws UnsupportedEncodingException, URISyntaxException {
        return generateURL(url, encoding, findingWords);
    }

    private static URI generateURL(String url, ExternalFinderItem.ENCODING encoding,
            String findingWords) throws UnsupportedEncodingException, URISyntaxException {
        String encodedWords;
        if (encoding == ExternalFinderItem.ENCODING.NONE) {
            encodedWords = findingWords;
        } else {
            encodedWords = URLEncoder.encode(findingWords, StandardCharsets.UTF_8.name());
            if (encoding == ExternalFinderItem.ENCODING.ESCAPE) {
                encodedWords = encodedWords.replace("+", "%20");
            }
        }

        String replaced = url.replace(ExternalFinderItem.PLACEHOLDER_TARGET, encodedWords);
        return new URI(replaced);
    }

    public static final class Builder {
        private String url;
        private ExternalFinderItem.TARGET target = ExternalFinderItem.TARGET.BOTH;
        private ExternalFinderItem.ENCODING encoding = ExternalFinderItem.ENCODING.DEFAULT;

        public static Builder from(ExternalFinderItemURL item) {
            return new Builder().setURL(item.getURL()).setTarget(item.getTarget())
                    .setEncoding(item.getEncoding());
        }

        public Builder setURL(String url) {
            this.url = url;
            return this;
        }

        public String getURL() {
            return url;
        }

        public Builder setTarget(ExternalFinderItem.TARGET target) {
            this.target = target;
            return this;
        }

        public ExternalFinderItem.TARGET getTarget() {
            return target;
        }

        public Builder setEncoding(ExternalFinderItem.ENCODING encoding) {
            this.encoding = encoding;
            return this;
        }

        public ExternalFinderItem.ENCODING getEncoding() {
            return encoding;
        }

        public ExternalFinderItemURL build() throws ExternalFinderValidationException {
            validate();
            return new ExternalFinderItemURL(url, target, encoding);
        }

        /**
         * Check the current builder parameters to see if they constitute a valid URL.
         *
         * @return A sample URL illustrating what the output will look like
         * @throws ExternalFinderValidationException
         *             If any parameter is not valid
         */
        public URI validate() throws ExternalFinderValidationException {
            if (url == null) {
                throw new ExternalFinderValidationException(
                        OStrings.getString("EXTERNALFINDER_URL_ERROR_NOURL"));
            }
            if (!url.contains(ExternalFinderItem.PLACEHOLDER_TARGET)) {
                throw new ExternalFinderValidationException(OStrings.getString(
                        "EXTERNALFINDER_URL_ERROR_NOTOKEN", ExternalFinderItem.PLACEHOLDER_TARGET));
            }
            if (target == null) {
                throw new ExternalFinderValidationException("EXTERNALFINDER_URL_ERROR_NOTARGET");
            }
            if (encoding == null) {
                throw new ExternalFinderValidationException("EXTERNALFINDER_URL_ERROR_NOENCODING");
            }
            try {
                return generateSampleURL();
            } catch (Throwable e) {
                throw new ExternalFinderValidationException(e);
            }
        }

        public URI generateSampleURL() throws UnsupportedEncodingException, URISyntaxException {
            String findingWords = target == ExternalFinderItem.TARGET.NON_ASCII_ONLY
                    ? "f\u00f8\u00f8 b\u00e5r" : "foo bar";
            return generateURL(url, encoding, findingWords);
        }
    }
}
