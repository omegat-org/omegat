/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
               Home page: https://www.omegat.org/
               Support center: https://omegat.org/support

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/
package org.omegat.core;

/**
 * JUnit category marker for smoke tests that run against the installed
 * distribution in build/install/OmegaT rather than against the test
 * classpath.
 *
 * These tests need a previously built distribution and take noticeably
 * longer than unit tests, so they must not run as part of the default test
 * task; run them explicitly with the dedicated Gradle task:
 *
 * <pre>
 * ./gradlew distributionSmokeTest
 * </pre>
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public interface DistributionSmokeTests {
}
