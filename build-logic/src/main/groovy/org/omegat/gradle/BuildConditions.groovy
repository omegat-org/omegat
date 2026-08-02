package org.omegat.gradle

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class BuildConditions {
    private static final Logger LOGGER = Logging.getLogger(BuildConditions)

    static boolean exePresent(String exe) {
        ["where $exe", "which $exe"].any {
            try {
                def findExe = it.execute()
                findExe.waitForProcessOutput()
                return findExe.exitValue() == 0
            } catch (any) {
                return false
            }
        }
    }

    static boolean conditions(List... items) {
        items.each { val, str ->
            if (!val) {
                LOGGER.warn(str)
            }
        }
        items.every { it[0] }
    }

    static boolean condition(val, str) {
        conditions([val, str])
    }
}
