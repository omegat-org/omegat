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

import javax.xml.transform.Transformer

// Parts derived from https://github.com/spring-projects/spring-build-gradle

@CompileStatic
class DocbookHtml extends Docbook {

    DocbookHtml() {
    }

    @Override
    protected void preTransform(Transformer transformer, File sourceFile, File outputFile) {
        def rootName = mainOutputFile.map { file ->
            String filename = file.asFile.getName()
            int extensionIndex = filename.lastIndexOf('.')
            if (extensionIndex > 0) {
                filename.substring(0, extensionIndex)
            } else {
                filename
            }
        }.get()
        transformer.setParameter("root.filename", rootName)
        transformer.setParameter("base.dir", mainOutputFile.get().asFile.parent + File.separator)
        transformer.setParameter("use.id.as.filename", 1)
        transformer.setParameter("html.ext", ".html")
        transformer.setParameter("chunk.section.depth", 0)
        transformer.setParameter("chunk.first.sections", 0)
        transformer.setParameter("chunker.output.encoding", "UTF-8")
        transformer.setParameter("chunker.output.indent", "yes")
        transformer.setParameter("use.extensions", 1)
        transformer.setParameter("chapter.autolabel", 0)
        transformer.setParameter("section.autolabel", 0)
        transformer.setParameter("tablecolumns.extension", 0)
        transformer.setParameter("toc.max.depth", 2)
        transformer.setParameter("generate.toc", "book toc,title,figure,table chapter toc appendix toc")
        transformer.setParameter("generate.index", 1)
        transformer.setParameter("html.stylesheet", "omegat.css")
        transformer.setParameter("docbook.css.link", 0)
        transformer.setParameter("saxon.character.representation", "native;decimal")
    }
}
