package org.omegat.documentation

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
@SuppressWarnings("unused")
@Slf4j
class DocumentationPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.apply(plugin: 'base')
    }

    private static String capitalizeFirst(String value) {
        if (value == null || value.length() == 0) {
            return ''
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1)
    }
}
