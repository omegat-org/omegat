package org.omegat.gradle

import org.gradle.api.file.FileSystemOperations
import javax.inject.Inject

interface InjectedOps {
    @Inject
    FileSystemOperations getFs()
}
