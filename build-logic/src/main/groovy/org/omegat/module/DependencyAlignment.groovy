package org.omegat.module

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ModuleComponentIdentifier

/**
 * Aligns dependency resolution of given configuration to versions application
 * ships on its runtime classpath. Parent-first class loading serves shared
 * libraries from application copy regardless of module-resolved version;
 * alignment makes module classpaths match runtime reality and keeps
 * version-skewed duplicates out of distribution (invariant guarded in CI by
 * tool/check_duplicate_dependencies.py).
 */
final class DependencyAlignment {

    private DependencyAlignment() {
    }

    /**
     * Rewrites requested versions to application-resolved version where same
     * group and name appear on application runtime classpath. Never attach to
     * root runtimeClasspath itself: version map resolves that configuration,
     * rule there would recurse (guarded below). Deadlock-free at execution
     * time because module plugin's afterEvaluate already resolves root
     * runtimeClasspath at configuration time; closure then only reads
     * memoized resolution state.
     */
    static void alignToApplication(Project rootProject, Configuration configuration) {
        def rootRuntime = rootProject.configurations.getByName("runtimeClasspath")
        if (configuration.is(rootRuntime)) {
            throw new IllegalArgumentException(
                    "alignToApplication must not target root runtimeClasspath itself")
        }
        def coreVersions = null
        configuration.resolutionStrategy.eachDependency { details ->
            if (coreVersions == null) {
                coreVersions = [:]
                rootRuntime.incoming.resolutionResult.allComponents.each { component ->
                    def id = component.id
                    if (id instanceof ModuleComponentIdentifier) {
                        coreVersions["${id.group}:${id.module}".toString()] = id.version
                    }
                }
            }
            def coreVersion = coreVersions["${details.requested.group}:${details.requested.name}".toString()]
            if (coreVersion != null && coreVersion != details.requested.version) {
                details.useVersion(coreVersion)
                details.because("aligned with the application-provided version")
            }
        }
    }
}
