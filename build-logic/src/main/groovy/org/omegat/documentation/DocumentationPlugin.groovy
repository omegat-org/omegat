/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.omegat.documentation

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
@SuppressWarnings("unused")
@Slf4j
class DocumentationPlugin implements Plugin<Project> {

    static final String DOCUMENTATION_EXTENSION = 'docConfig'
    static final String DOCUMENTATION_OUTPUT_TYPES = 'docOutputTypes'
    static final String DOCUMENTATION_SET_CONTAINER = 'documentationSets'

    @Override
    void apply(Project project) {
        project.apply(plugin: 'base')

        def extension = project.extensions.create(DOCUMENTATION_EXTENSION, DocConfigExtension, project.objects)
        project.tasks.withType(DocConfigurable).whenTaskAdded { DocConfigurable docConfigurableTask ->
            org.omegat.documentation.DocumentationPlugin.log.debug("Configuring task {}", docConfigurableTask)
            docConfigurableTask.configureWith(extension)
        }
    }

    private static String capitalizeFirst(String value) {
        if (value == null || value.length() == 0) {
            return ''
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1)
    }
}
