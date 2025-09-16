package org.omegat.documentation

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal

@CompileStatic
abstract class AbstractDocumentTask  extends DefaultTask implements DocConfigurable {

    private final DirectoryProperty styleDir = project.objects.directoryProperty()
    private final DirectoryProperty docRoot = project.objects.directoryProperty()
    private final DirectoryProperty outputRoot = project.objects.directoryProperty()
    private final Property<LogLevel> logLevel = project.objects.property(LogLevel)

    @InputDirectory
    DirectoryProperty getStyleDir() {
        return styleDir
    }

    /**
     * Sources root for the documentation
     */
    @Internal
    DirectoryProperty getDocRoot() {
        return docRoot
    }

    @Internal
    DirectoryProperty getOutputRoot() {
        return outputRoot
    }

    @Internal
    Property<LogLevel> getLogLevel() {
        return logLevel
    }

    AbstractDocumentTask() {
        logLevel.convention(LogLevel.INFO)
        logLevel.set(project.gradle.startParameter.logLevel)
    }

    @Override
    void configureWith(DocConfigExtension extension) {
        docRoot.convention(extension.docRoot)
        styleDir.convention(extension.styleDir)
        outputRoot.convention(extension.outputRoot)
    }

    protected void configureLogging() {
        // Redirect spurious task output to INFO unless explicitly configured for debug
        switch (project.gradle.startParameter.logLevel) {
            case LogLevel.DEBUG:
                break
            default:
                logging.captureStandardOutput(LogLevel.INFO)
                logging.captureStandardError(LogLevel.INFO)
        }
    }
}
