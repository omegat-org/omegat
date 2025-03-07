package org.omegat.documentation

import com.xmlmind.util.StringUtil
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.file.RegularFile
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import com.xmlmind.whc.Compiler
import org.gradle.api.tasks.options.Option

@CompileStatic
class WhcTask extends DefaultTask implements DocConfigurable {

    @InputDirectory
    final DirectoryProperty styleDir = project.objects.directoryProperty()

    @Internal
    final DirectoryProperty docRoot = project.objects.directoryProperty()

    @Internal
    final DirectoryProperty outputRoot = project.objects.directoryProperty()

    @InputFile
    final Provider<RegularFile> inputFile = project.objects.fileProperty()

    @InputFiles
    final Provider<FileTree> contentFiles = project.objects.property(FileTree)

    @InputFile
    final Provider<RegularFile> tocFile = project.objects.fileProperty()

    @OutputDirectory
    final Provider<Directory> outputDirectory = project.objects.directoryProperty()

    @Option
    @Input
    ListProperty<String> parameterList = project.objects.listProperty(String)

    @TaskAction
    void transform() {
        switch (project.gradle.startParameter.logLevel) {
            case LogLevel.DEBUG:
                break
            default:
                logging.captureStandardOutput(LogLevel.INFO)
                logging.captureStandardError(LogLevel.INFO)
        }
        Compiler compiler = new Compiler(null)
        compiler.setVerbose(true)
        File[] contents = contentFiles.get().getFiles().toArray(new File[0])
        if (parameterList.get().size() > 1) {
            compiler.parseParameters((String[])parameterList.get().toArray(StringUtil.EMPTY_LIST))
        }
        compiler.compile(contents, tocFile.get().asFile, inputFile.get().asFile, outputDirectory.get().asFile)
    }

    @Override
    void configureWith(DocConfigExtension extension) {
        docRoot.convention(extension.docRoot)
        outputRoot.convention(extension.outputRoot)
        styleDir.convention(extension.styleDir)
    }
}
