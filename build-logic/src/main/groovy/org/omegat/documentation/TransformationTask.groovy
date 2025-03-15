package org.omegat.documentation

import groovy.transform.CompileStatic
import org.apache.xerces.jaxp.SAXParserFactoryImpl
import org.apache.xml.resolver.tools.CatalogResolver
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.xml.sax.InputSource
import org.xml.sax.XMLReader

import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

@CompileStatic
class TransformationTask extends AbstractDocumentTask {

    @InputFile
    Provider<RegularFile> styleSheetFile = project.objects.fileProperty()

    @OutputFile
    Provider<RegularFile> outputFile = project.objects.fileProperty()

    TransformationTask() {
    }

    @TaskAction
    final void transform() {
        configureLogging()

        File input = inputFile.get().asFile
        File output = outputFile.get().asFile

        InputSource inputSource = new InputSource(input.getAbsolutePath())
        StreamResult outputResult = new StreamResult(output)

        CatalogResolver resolver = new CatalogResolver(XsltHelper.createCatalogManager())
        XMLReader xmlReader = initializeXmlReader(resolver)
        Transformer transformer = initializeTransformer(resolver, styleSheetFile.get().asFile)

        preTransform(transformer, input, output)
        transformer.transform(new SAXSource(xmlReader, inputSource), outputResult)
        postTransform(output)
    }

    protected void preTransform(Transformer transformer, File source, File target) {
    }

    protected void postTransform(File output) {
    }

    static final String EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities"
    static final String EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities"

    protected static XMLReader initializeXmlReader(CatalogResolver resolver) {
        def factory = configureSAXParserFactory()
        def xmlReader = factory.newSAXParser().getXMLReader()
        xmlReader.setEntityResolver(resolver)
        return xmlReader
    }

    protected static SAXParserFactory configureSAXParserFactory() {
        SAXParserFactory factory = new SAXParserFactoryImpl()
        factory.setValidating(false)
        factory.setNamespaceAware(true)
        factory.setXIncludeAware(true)
        factory.setFeature(EXTERNAL_GENERAL_ENTITIES, true)
        factory.setFeature(EXTERNAL_PARAMETER_ENTITIES, true)
        return factory
    }

    protected static Transformer initializeTransformer(CatalogResolver resolver, File styleSheetFile) {
        def transformerFactory = TransformerFactory.newInstance()
        transformerFactory.setURIResolver(resolver)

        def styleSheetUrl = styleSheetFile.toURI().toURL()
        def styleSource = new StreamSource(styleSheetUrl.openStream(), styleSheetUrl.toExternalForm())
        return transformerFactory.newTransformer(styleSource)
    }

}
