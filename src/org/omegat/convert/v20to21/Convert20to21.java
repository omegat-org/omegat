/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
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
package org.omegat.convert.v20to21;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import gen.core.filters.Files;
import gen.core.filters.Filter;
import gen.core.filters.Filters;

/**
 * Convert some configs from v2.0 to v2.1
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public final class Convert20to21 {

    private Convert20to21() {
    }

    /**
     * Convert filters config ('filters.conf') into new format.
     *
     * @param fromFile
     *            old config file
     * @param toFile
     *            new config file
     */
    public static void convertFiltersConfig(final File fromFile, final File toFile) throws Exception {
        if (!fromFile.exists()) {
            return;
        }
        String c = read(fromFile);
        org.omegat.convert.v20to21.data.Filters filters;
        try (XMLDecoder xmldec = new XMLDecoder(
                new ByteArrayInputStream(c.getBytes(StandardCharsets.UTF_8)))) {
            filters = (org.omegat.convert.v20to21.data.Filters) xmldec.readObject();
        }

        Filters res = new Filters();
        for (org.omegat.convert.v20to21.data.OneFilter f : filters.getFilter()) {
            Filter fo = new Filter();
            res.getFilters().add(fo);
            fo.setClassName(f.getClassName());
            fo.setEnabled(f.isOn());
            for (org.omegat.convert.v20to21.data.Instance i : f.getInstance()) {
                Files io = new Files();
                fo.getFiles().add(io);
                io.setSourceFilenameMask(i.getSourceFilenameMask());
                io.setTargetFilenamePattern(i.getTargetFilenamePattern());
                io.setSourceEncoding(i.getSourceEncoding());
                io.setTargetEncoding(i.getTargetEncoding());
            }
            Serializable opts = f.getOptions();
            if (opts != null) {
                BeanInfo bi = Introspector.getBeanInfo(opts.getClass());
                for (PropertyDescriptor prop : bi.getPropertyDescriptors()) {
                    if ("class".equals(prop.getName())) {
                        continue;
                    }
                    Object value = prop.getReadMethod().invoke(opts);
                    Filter.Option op = new Filter.Option();
                    op.setName(prop.getName());
                    op.setValue(value.toString());
                    fo.getOption().add(op);
                }
            }
        }

        convertTextFilter(res);
        convertHTMLFilter2(res);

        XmlMapper mapper = new XmlMapper();
        mapper.registerModule(new JaxbAnnotationModule());
        mapper.writeValue(toFile, res);
    }

    /**
     * Read old config file and replace package names.
     *
     * @param f
     *            old config file
     * @return old config file with replaces package names
     * @throws IOException
     */
    private static String read(File f) throws IOException {
        StringBuilder r = new StringBuilder();
        char[] c = new char[8192];
        int len;
        try (InputStreamReader rd = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) {
            while ((len = rd.read(c)) >= 0) {
                r.append(c, 0, len);
            }
        }
        String res = r.toString();
        res = res.replace("org.omegat.filters2.master.Filters", "org.omegat.convert.v20to21.data.Filters");
        res = res.replace("org.omegat.filters2.master.OneFilter",
                "org.omegat.convert.v20to21.data.OneFilter");
        res = res.replace("org.omegat.filters2.Instance", "org.omegat.convert.v20to21.data.Instance");
        res = res.replace("org.omegat.filters2.html2.HTMLOptions",
                "org.omegat.convert.v20to21.data.HTMLOptions");
        res = res.replace("org.omegat.filters2.text.TextOptions",
                "org.omegat.convert.v20to21.data.TextOptions");
        res = res.replace("org.omegat.filters3.xml.opendoc.OpenDocOptions",
                "org.omegat.convert.v20to21.data.OpenDocOptions");
        res = res.replace("org.omegat.filters3.xml.openxml.OpenXMLOptions",
                "org.omegat.convert.v20to21.data.OpenXMLOptions");
        res = res.replace("org.omegat.filters3.xml.xhtml.XHTMLOptions",
                "org.omegat.convert.v20to21.data.XHTMLOptions");
        return res;
    }

    /**
     * Convert TextFilter options from int to string.
     *
     * @param res
     */
    private static void convertTextFilter(Filters res) {
        for (Filter f : res.getFilters()) {
            if (!f.getClassName().equals("org.omegat.filters2.text.TextFilter")) {
                continue;
            }
            for (Filter.Option opt : f.getOption()) {
                if (opt.getName().equals("segmentOn")) {
                    try {
                        switch (Integer.parseInt(opt.getValue())) {
                        case 1:
                            opt.setValue("BREAKS");
                            break;
                        case 2:
                            opt.setValue("EMPTYLINES");
                            break;
                        case 3:
                            opt.setValue("NEVER");
                            break;
                        }
                    } catch (Exception ex) {
                    }
                }
            }
        }
    }

    /**
     * Convert HTMLFilter2 options from int to string.
     *
     * @param res
     */
    private static void convertHTMLFilter2(Filters res) {
        for (Filter f : res.getFilters()) {
            if (!f.getClassName().equals("org.omegat.filters2.html2.HTMLFilter2")) {
                continue;
            }
            for (Filter.Option opt : f.getOption()) {
                if (opt.getName().equals("rewriteEncoding")) {
                    try {
                        switch (Integer.parseInt(opt.getValue())) {
                        case 1:
                            opt.setValue("ALWAYS");
                            break;
                        case 2:
                            opt.setValue("IFHEADER");
                            break;
                        case 3:
                            opt.setValue("IFMETA");
                            break;
                        case 4:
                            opt.setValue("NEVER");
                            break;
                        }
                    } catch (Exception ex) {
                    }
                }
            }
        }
    }
}
