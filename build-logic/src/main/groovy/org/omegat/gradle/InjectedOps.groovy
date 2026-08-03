package org.omegat.gradle

import org.gradle.api.file.FileSystemOperations
import org.gradle.process.ExecOperations

import javax.inject.Inject

interface InjectedOps {
    @Inject
    ExecOperations getExecOps()
    @Inject
    FileSystemOperations getFs()
}
