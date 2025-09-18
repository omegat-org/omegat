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

        def getUpdateSuffix = { update, mavenStyle ->
            if (!update || update == '0') return ''
            if (update.length() == 1) {
                if (mavenStyle) return "-0${update}"
                return "_0${update}"
            }
            if (mavenStyle) return "-${update}"
            "_${update}"
        }

        def update = getUpdateSuffix(versionPropProvider.getProperty('update'), false)
        def mavenUpdate = getUpdateSuffix(versionPropProvider.getProperty('update'), true)
        project.ext.omtVersion =  [
                version: version + update,
                beta: beta,
                revision: revision,
                mavenStyleVersion: version + mavenUpdate
        ]
    }
}
