package org.omegat.documentation

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

@CompileStatic
abstract class AbstractStyleSheetTask extends DefaultTask implements DocConfigurable {

    /**
     * Language of the documentation (eg 'de', 'ru').
     */
    @Input
    @Optional
    @Option(option = 'language', description = 'The two letter language code for output')
    final Property<String> language = project.objects.property(String)

    @InputDirectory
    final DirectoryProperty styleDir = project.objects.directoryProperty()
    /**
     * Sources root for the documentation
     */
    @Internal
    final DirectoryProperty docRoot = project.objects.directoryProperty()

    @Internal
    final DirectoryProperty outputRoot = project.objects.directoryProperty()

    @Override
    void configureWith(DocConfigExtension extension) {
        docRoot.convention(extension.docRoot)
        outputRoot.convention(extension.outputRoot)
        styleDir.convention(extension.styleDir)
    }

    @TaskAction
    abstract void transform()

}
