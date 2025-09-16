package org.omegat.documentation

import groovy.transform.CompileStatic
import org.apache.xml.resolver.tools.CatalogResolver
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.xml.sax.InputSource
import org.xml.sax.XMLReader

import javax.xml.transform.Transformer
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.stream.StreamResult

@CompileStatic
class TransformationTask extends AbstractTransformationTask {

    private final Provider<RegularFile> styleSheetFile = project.objects.fileProperty()
    private final Provider<RegularFile> outputFile = project.objects.fileProperty()
    private final Property<RegularFile> inputFile = project.objects.fileProperty()

    @InputFile
    Provider<RegularFile> getStyleSheetFile() {
        return styleSheetFile
    }

    @OutputFile
    Provider<RegularFile> getOutputFile() {
        return outputFile
    }

    @InputFile
    Property<RegularFile> getInputFile() {
        return inputFile
    }

    TransformationTask() {
    }

    @Override
    void configureWith(DocConfigExtension extension) {
        super.configureWith(extension)
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

}
