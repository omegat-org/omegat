package org.omegat.documentation

import com.icl.saxon.ExtendedInputSource
import com.icl.saxon.ParameterSet
import com.icl.saxon.StyleSheet
import com.icl.saxon.TransformerFactoryImpl
import com.icl.saxon.expr.StringValue
import com.icl.saxon.om.NamePool
import groovy.transform.CompileStatic
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import javax.xml.transform.Templates
import javax.xml.transform.sax.SAXSource

@CompileStatic
class StylesheetTask extends AbstractStyleSheetTask {

    @InputFile
    Provider<RegularFile> inputFile = project.objects.fileProperty()

    @InputFile
    Provider<RegularFile> styleSheetFile = project.objects.fileProperty()

    @OutputFile
    Provider<RegularFile> outputFile = project.objects.fileProperty()

    StylesheetTask() {
    }

    @TaskAction
    void transform() {
        preTransform()
        outputFile.get().asFile.parentFile.mkdirs()
        execute(inputFile.get().asFile, styleSheetFile.get().asFile, outputFile.get().asFile)
    }

    protected NamePool namePool = NamePool.getDefaultNamePool()
    protected ParameterSet params = new ParameterSet()

    void execute(File sourceFile, File sheetFile, File output) {
        def factory = new TransformerFactoryImpl()

        ExtendedInputSource eis = new ExtendedInputSource(sourceFile)
        def sourceInput = new SAXSource(factory.getSourceParser(), eis)
        eis.setEstimatedLength((int)sourceFile.length())

        ExtendedInputSource eis2 = new ExtendedInputSource(sheetFile)
        def styleSource = new SAXSource(factory.getStyleParser(), eis2)
        Templates sheet = factory.newTemplates(styleSource)

        StyleSheet styleSheet = new StyleSheet()
        styleSheet.processFile(sourceInput, sheet, output, params)
    }

    void preTransform() {}

}