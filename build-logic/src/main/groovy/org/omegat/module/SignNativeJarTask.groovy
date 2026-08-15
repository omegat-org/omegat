package org.omegat.module

import org.gradle.api.DefaultTask
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

import javax.inject.Inject

abstract class SignNativeJarTask extends DefaultTask {

    @Inject abstract ExecOperations getExecOperations()
    @Inject abstract FileSystemOperations getFileSystemOperations()
    @Inject abstract ArchiveOperations getArchiveOperations()

    @InputFile abstract RegularFileProperty getSourceJar()
    @Input abstract Property<String> getSigningIdentity()
    @InputFile abstract RegularFileProperty getEntitlements()
    @OutputDirectory abstract DirectoryProperty getStagingDir()

    @TaskAction
    void signAndExtract() {
        def staging = stagingDir.get().asFile

        fileSystemOperations.copy {
            from archiveOperations.zipTree(sourceJar)
            into staging
        }

        def nativeLibs = []
        staging.eachFileRecurse { f ->
            if (f.name.endsWith('.dylib') || f.name.endsWith('.jnilib')) {
                nativeLibs << f
            }
        }

        if (nativeLibs) {
            execOperations.exec {
                commandLine(['codesign', '--deep', '--force',
                             '--sign', signingIdentity.get(),
                             '--timestamp',
                             '--options', 'runtime',
                             '--entitlements', entitlements.get().asFile.absolutePath] + nativeLibs)
            }
        }
    }
}