package org.omegat.documentation

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal

@CompileStatic
abstract class AbstractDocumentTask  extends DefaultTask {

    @InputFile
    final Provider<RegularFile> inputFile = project.objects.fileProperty()

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
