/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.omegat.documentation

import groovy.transform.CompileStatic
import org.apache.xml.resolver.CatalogManager

@CompileStatic
class XsltHelper {

    /**
     * Creates a catalog manager with the XML catalogs on the classpath.
     * <p>
     * Includes all catalog.xml and the first docbook/catalog.xml relative to the root of the classpath.
     * </p>
     *
     * @return Catalog manager
     * @throws IllegalStateException if docbook/catalog.xml is not on the classpath
     */
    static CatalogManager createCatalogManager() {
        def manager = new CatalogManager()
        manager.setIgnoreMissingProperties(true)
        def classLoader = XsltHelper.class.getClassLoader()
        def builder = new StringBuilder()
        def docbookCatalogName = "docbook/catalog.xml"
        def docbookCatalog = classLoader.getResource(docbookCatalogName)

        if (docbookCatalog == null) {
            throw new IllegalStateException("Docbook catalog " + docbookCatalogName + " could not be found in " + classLoader)
        }

        builder.append(docbookCatalog.toExternalForm())

        def enumeration = classLoader.getResources("catalog.xml")
        while (enumeration.hasMoreElements()) {
            builder.append(';')
            def resource = enumeration.nextElement()
            builder.append(resource.toExternalForm())
        }
        manager.setCatalogFiles(builder.toString())
        return manager
    }
}
