package org.omegat.documentation

import com.icl.saxon.TransformerFactoryImpl
import org.apache.xerces.jaxp.SAXParserFactoryImpl
import org.apache.xml.resolver.tools.CatalogResolver
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.xml.sax.InputSource
import org.xml.sax.XMLReader

import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.Result
import javax.xml.transform.Source
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

class XIncludeTask extends DefaultTask implements DocConfigurable {

    @InputFile
    final Property<RegularFile> inputFile = project.objects.fileProperty()

    @InputDirectory
    final DirectoryProperty styleDir = project.objects.directoryProperty()

    @InputFile
    Provider<RegularFile> styleSheetFile = project.objects.fileProperty()

    @TaskAction
    void transform() {
        File outputFile = outputs.getFiles().singleFile

        // Configure SAX Parser with XInclude support
        SAXParserFactory factory = new SAXParserFactoryImpl();
        factory.setValidating(false)
        factory.setNamespaceAware(true);
        factory.setXIncludeAware(true);  // Enable XInclude
        factory.setFeature("http://xml.org/sax/features/external-general-entities", true);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", true);

        Result result = new StreamResult(outputFile.getAbsolutePath())
        CatalogResolver resolver = new CatalogResolver(XsltHelper.createCatalogManager())
        InputSource inputSource = new InputSource(inputFile.get().asFile.getAbsolutePath())

        XMLReader reader = factory.newSAXParser().getXMLReader()
        reader.setEntityResolver(resolver)
        TransformerFactory transformerFactory = new TransformerFactoryImpl()
        transformerFactory.setURIResolver(resolver)
        URL url = styleSheetFile.get().asFile.toURI().toURL()
        Source source = new StreamSource(url.openStream(), url.toExternalForm())
        Transformer transformer = transformerFactory.newTransformer(source)
        transformer.transform(new SAXSource(reader, inputSource), result);
    }

    @Override
    void configureWith(DocConfigExtension extension) {
        styleDir.convention(extension.styleDir)
    }
}
