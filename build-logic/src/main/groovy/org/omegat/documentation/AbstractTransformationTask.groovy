package org.omegat.documentation

import groovy.transform.CompileStatic
import org.apache.xerces.jaxp.SAXParserFactoryImpl
import org.apache.xml.resolver.tools.CatalogResolver
import org.gradle.api.tasks.TaskAction
import org.xml.sax.XMLReader

import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamSource

@CompileStatic
abstract class AbstractTransformationTask extends AbstractDocumentTask implements DocConfigurable {

    AbstractTransformationTask() {
    }

    @TaskAction
    abstract void transform()


    static final String EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities"
    static final String EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities"

    protected XMLReader initializeXmlReader(CatalogResolver resolver) {
        def factory = configureSAXParserFactory()
        def xmlReader = factory.newSAXParser().getXMLReader()
        xmlReader.setEntityResolver(resolver)
        return xmlReader
    }

    protected SAXParserFactory configureSAXParserFactory() {
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
