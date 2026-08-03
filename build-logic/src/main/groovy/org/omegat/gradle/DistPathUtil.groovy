package org.omegat.gradle

import org.gradle.api.file.FileCopyDetails
import org.gradle.api.file.RelativePath

class DistPathUtil {
    static void replaceRelativePathSegment(FileCopyDetails deets, pattern, replacement) {
        def segs = deets.relativePath.segments.collect {
            it =~ pattern ? replacement : it
        }
        deets.relativePath = new RelativePath(!deets.directory, segs as String[])
    }
}
