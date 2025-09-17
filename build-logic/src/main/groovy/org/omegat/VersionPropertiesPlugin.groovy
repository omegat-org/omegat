package org.omegat

import org.gradle.api.Plugin
import org.gradle.api.Project

class VersionPropertiesPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def versionPropertiesFile = project.layout.projectDirectory.file('src/org/omegat/Version.properties')
        def versionPropProvider = project.providers.fileContents(versionPropertiesFile)
                .asText // Get the contents as a String
                .map { content ->
                    def props = new Properties()
                    props.load(new StringReader(content))
                    return props
                }.get()

        def version = versionPropProvider.getProperty('version')
        def beta = versionPropProvider.getProperty('beta')
        def revision = versionPropProvider.getProperty('revision')
        project.ext.omtVersion =  [
                version: version,
                beta: beta,
                revision: revision,
                mavenStyleVersion: version
        ]
    }
}
