package org.omegat.documentation

import groovy.transform.CompileStatic
import net.sf.saxon.lib.ResourceResolverWrappingURIResolver
import net.sf.saxon.s9api.*
import org.apache.xerces.jaxp.SAXParserFactoryImpl
import org.gradle.api.file.FileTree
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.xml.sax.InputSource
import org.xml.sax.XMLReader
import org.xmlresolver.Resolver
import org.xmlresolver.ResolverConfiguration
import org.xmlresolver.ResolverFeature
import org.xmlresolver.XMLResolverConfiguration

import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.stream.StreamSource

@CompileStatic
@CacheableTask
class TransformationTask extends AbstractDocumentTask {

    private static final String EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities"
    private static final String EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities"

    private static final String CATALOG = "classpath:/org/xmlresolver/catalog.xml"

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    @Optional
    final Provider<FileTree> contentFiles = project.objects.property(FileTree)

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    Provider<RegularFile> styleSheetFile = project.objects.fileProperty()

    @OutputFile
    Provider<RegularFile> outputFile = project.objects.fileProperty()

    @Input
    @Optional
    Provider<String> debug = project.objects.property(String)

    @Input
    @Optional
    final Property<Boolean> xIncludeAware = project.objects.property(Boolean).convention(true)

    @TaskAction
    void transform() {
        configureLogging()

        File input = inputFile.get().asFile
        File output = outputFile.get().asFile
        File xslFile = styleSheetFile.get().asFile

        def transformer = initializeTransformer(input, output, xslFile)

        preTransform(transformer, input, output)

        transformer.transform()

        postTransform(output)
    }

    protected void preTransform(XsltTransformer transformer, File source, File target) {
    }

    protected void postTransform(File output) {
    }

    protected void configProcessor(Processor processor) {
    }

    private XMLReader initializeXmlReader() {
        def factory = configureSAXParserFactory()
        def xmlReader = factory.newSAXParser().getXMLReader()
        return xmlReader
    }

    private XsltTransformer initializeTransformer(File input, File output, File xslFile) {
        def xmlReader = initializeXmlReader()

        // Set up Saxon Processor
        Processor processor = new Processor(false)
        configProcessor(processor)
        XsltCompiler compiler = processor.newXsltCompiler()
        compiler.setResourceResolver(initializeResourceResolver())
        if (debug.present) {
            compiler.setParameter(new QName("debug"), new XdmAtomicValue(debug.get()))
        }

        // Compile the XSLT stylesheet
        XsltExecutable executable = compiler.compile(new StreamSource(xslFile))

        // Prepare the input document
        def inputSource = new InputSource(input.getAbsolutePath())
        def saxSource = new SAXSource(xmlReader, inputSource)
        def source = processor.newDocumentBuilder().build(saxSource)

        // Set up output
        Serializer serializer = processor.newSerializer(output)
        serializer.setOutputProperty(Serializer.Property.METHOD, "xhtml")
        serializer.setOutputProperty(Serializer.Property.INDENT, "yes")

        // Create a transformer and set parameters if needed
        XsltTransformer transformer = executable.load()
        transformer.setInitialContextNode(source)
        transformer.setDestination(serializer)

        return transformer
    }

    private SAXParserFactory configureSAXParserFactory() {
        SAXParserFactory factory = new SAXParserFactoryImpl()
        factory.setValidating(false)
        factory.setNamespaceAware(true)
        factory.setXIncludeAware(xIncludeAware.get())
        factory.setFeature(EXTERNAL_GENERAL_ENTITIES, true)
        factory.setFeature(EXTERNAL_PARAMETER_ENTITIES, true)
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)  // Allow DOCTYPE
        return factory
    }

    private static ResourceResolverWrappingURIResolver initializeResourceResolver() {
        // Use the Catalog Resolver for URI resolution
        ResolverConfiguration resolverConfig = new XMLResolverConfiguration()
        resolverConfig.addCatalog(CATALOG)
        resolverConfig.setFeature(ResolverFeature.CLASSPATH_CATALOGS, true)
        def resolver = new Resolver(resolverConfig)
        return new ResourceResolverWrappingURIResolver(resolver)
    }
}
