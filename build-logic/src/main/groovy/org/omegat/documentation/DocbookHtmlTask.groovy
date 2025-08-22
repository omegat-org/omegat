package org.omegat.documentation

import groovy.transform.CompileStatic

import javax.xml.transform.Transformer

@CompileStatic
class DocbookHtmlTask extends TransformationTask {

    private static String extractRootName(File file) {
        def fileName = file.getName()
        int extensionIndex = fileName.lastIndexOf('.')
        if (extensionIndex <= 0) {
            return fileName
        }
        return fileName.substring(0, extensionIndex)
    }

    @Override
    protected void preTransform(Transformer transformer, File source, File target) {
        String baseDir = outputFile.get().asFile.parent + File.separator
        transformer.setParameter("root.filename", extractRootName(target))
        transformer.setParameter("base.dir", baseDir)
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
