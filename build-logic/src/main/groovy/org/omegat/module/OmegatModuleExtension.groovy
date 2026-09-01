package org.omegat.module

import org.gradle.api.GradleException
import org.gradle.api.Project

import javax.inject.Inject

class OmegatModuleExtension {
    static final String PROVIDED_CODE_LIBS_PATH = 'lib/provided/core'
    static final String PROVIDED_MODULE_LIBS_PATH = 'lib/provided/module'
    private final Project project

    @Inject
    OmegatModuleExtension(Project project) {
        this.project = project
    }

    String getProvidedCoreLibsPath() {
        return PROVIDED_CODE_LIBS_PATH
    }

    String getProvidedModuleLibsPath() {
        return PROVIDED_MODULE_LIBS_PATH
    }

    File getProvidedCoreLibsDir() {
        return project.layout.settingsDirectory.dir(providedCoreLibsPath).asFile
    }

    File getProvidedModuleLibsDir() {
        return project.layout.settingsDirectory.dir(providedModuleLibsPath).asFile
    }

    def providedCoreLib(String... artifacts) {
        return providedLib(providedCoreLibsDir, artifacts)
    }

    def providedModuleLib(String... artifacts) {
        return providedLib(providedModuleLibsDir, artifacts)
    }

    private def providedLib(String scope, File libsDir, String... artifacts) {
        // Require every requested artifact to match at least one jar. A single
        // combined FileTree would hide missing jars when another artifact in
        // the same call still matches.
        def patterns = artifacts.collect { "**/${it}-*.jar".toString() }
        patterns.each { pattern ->
            def matches = project.fileTree(dir: libsDir, include: pattern)
            if (matches.isEmpty()) {
                throw new GradleException("No ${scope} provided jar matches ${patterns} in ${libsDir}; "
                        + "the ${scope} provided libraries are incomplete.")
            }
        }
        return project.fileTree(dir: libsDir, includes: patterns)
    }
}
