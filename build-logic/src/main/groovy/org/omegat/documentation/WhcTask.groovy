package org.omegat.documentation

import com.xmlmind.util.StringUtil
import com.xmlmind.whc.Compiler
import groovy.transform.CompileStatic
import org.gradle.api.file.Directory
import org.gradle.api.file.FileTree
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.api.tasks.options.Option

@CompileStatic
class WhcTask extends AbstractDocumentTask {

    @InputFile
    final Provider<RegularFile> inputFile = project.objects.fileProperty()

    @InputFiles
    final Provider<FileTree> contentFiles = project.objects.property(FileTree)

    @InputFile
    final Provider<RegularFile> tocFile = project.objects.fileProperty()

    @InputFile
    final Provider<RegularFile> headerFile = project.objects.fileProperty()

    @OutputDirectory
    final Provider<Directory> outputDirectory = project.objects.directoryProperty()

    @Input
    @Option
    final ListProperty<String> parameterList = project.objects.listProperty(String)

    @Input
    @Option
    final Property<String> documentLayout = project.objects.property(String)

    @Input
    @Option
    final Property<Boolean> localJQuery = project.objects.property(Boolean)


    @TaskAction
    void transform() {
        configureLogging()

        // get task options
        def hasParameter = parameterList.get().size() > 1
        def toc = tocFile.get().asFile
        def input = inputFile.get().asFile
        def output = outputDirectory.get().asFile
        File[] contents = contentFiles.get().getFiles().toArray(new File[0])

        // configure WHC compiler
        Compiler compiler = new Compiler(null)
        compiler.setVerbose(true)
        if (headerFile.present) {
            def headerFileUrl = headerFile.get().asFile.toPath().toUri().toURL()
            compiler.setUserHeader(headerFileUrl)
        }
        if (documentLayout.present) {
            compiler.setLayout(documentLayout.get())
        }
        if (localJQuery.present) {
            compiler.setLocalJQuery(localJQuery.get())
        }
        if (hasParameter) {
            compiler.parseParameters((String[])parameterList.get().toArray(StringUtil.EMPTY_LIST))
        }

        // do compile
        compiler.compile(contents, toc, input, output)
    }
}
