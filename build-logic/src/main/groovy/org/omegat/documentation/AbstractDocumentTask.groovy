package org.omegat.documentation

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

import javax.inject.Inject

@CompileStatic
abstract class AbstractDocumentTask  extends DefaultTask {

    @Internal
    final LogLevel logLevel = project.gradle.startParameter.logLevel

    @Inject
    abstract ObjectFactory getObjects()

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    final RegularFileProperty inputFile = objects.fileProperty()

    protected void configureLogging() {
        // Redirect spurious task output to INFO unless explicitly configured for debug
        switch (logLevel) {
            case LogLevel.DEBUG:
                break
            default:
                logging.captureStandardOutput(LogLevel.INFO)
                logging.captureStandardError(LogLevel.INFO)
        }
    }
}
