package org.omegat.documentation

import groovy.transform.CompileStatic
import net.sf.saxon.s9api.Processor
import net.sf.saxon.s9api.QName
import net.sf.saxon.s9api.XdmAtomicValue
import net.sf.saxon.s9api.XsltTransformer
import org.docbook.xsltng.extensions.Register
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input


@CompileStatic
@CacheableTask
class DocbookHtmlTask extends TransformationTask {

    private final Provider<String> css = project.objects.property(String)

    @Input
    Provider<String> getCss() {
        return css
    }

    @Override
    protected void configProcessor(Processor processor) {
        Register register = new Register()
        register.initialize(processor.getUnderlyingConfiguration())
    }

    @Override
    protected void preTransform(XsltTransformer transformer, File source, File target) {
        def outputChunkName = outputFile.get().asFile.name
        transformer.setParameter(new QName("chunk"), new XdmAtomicValue(outputChunkName))
        // Both BaseDirs should be the same form of the URI expressions.
        // Otherwise, produced src URL of the img tags become weired.
        def inputBaseDir = inputFile.get().asFile.parentFile.toURI().toString()
        def outputBaseDir = outputFile.get().asFile.parentFile.toURI().toString()
        transformer.setParameter(new QName("chunk-output-base-uri"), new XdmAtomicValue(outputBaseDir))
        transformer.setParameter(new QName("mediaobject-input-base-uri"), new XdmAtomicValue(inputBaseDir))
        transformer.setParameter(new QName("persistent-toc"), new XdmAtomicValue(false))
        transformer.setParameter(new QName("persistent-toc-search"), new XdmAtomicValue(false))
        transformer.setParameter(new QName("chunk-section-depth"), new XdmAtomicValue(0))
        transformer.setParameter(new QName("section-toc-depth"), new XdmAtomicValue(1))
        transformer.setParameter(new QName("html-extension"), new XdmAtomicValue(".html"))
        transformer.setParameter(new QName("output-media"), new XdmAtomicValue("screen"))
        transformer.setParameter(new QName("page-style"), new XdmAtomicValue("book"))
        transformer.setParameter(new QName("pagetoc-dynamic"), new XdmAtomicValue(false))
        transformer.setParameter(new QName("persistent-toc"), new XdmAtomicValue(false))
        transformer.setParameter(new QName("use-id-as-filename"), new XdmAtomicValue("true"))
        transformer.setParameter(new QName("use-docbook-css"), new XdmAtomicValue("false"))
        // html stylesheet link is constructed as "${resource-base-uri}${user-css-links}"
        // so "resource-base-uri" should be a plain file path that ends with "/"
        transformer.setParameter(new QName("resource-base-uri"), new XdmAtomicValue("./"))
        transformer.setParameter(new QName("user-css-links"), new XdmAtomicValue(css.get()))
        transformer.setParameter(new QName("lists-of-examples"), new XdmAtomicValue("false"))
        transformer.setParameter(new QName("lists-of-figures"), new XdmAtomicValue("false"))
        transformer.setParameter(new QName("lists-of-tables"), new XdmAtomicValue("false"))
    }
}
