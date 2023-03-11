/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2012 Didier Briel
               2015 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/
package org.omegat.util;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.After;
import org.junit.Test;

import java.awt.ComponentOrientation;
import java.util.Locale;

import org.omegat.core.Core;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectProperties;

public class BiDiUtilsTest {
	final Language LTR_LANGUAGE = new Language("pl");
	final Language RTL_LANGUAGE = new Language("ar");
	final Locale LTR_LOCALE = LTR_LANGUAGE.getLocale();
	final Locale RTL_LOCALE = RTL_LANGUAGE.getLocale();
	final Locale INITIAL_LOCALE = Locale.getDefault();

	@After
	public void resetInitialLocale() {
		Locale.setDefault(INITIAL_LOCALE);
	}

	@Test
	public void testGetOrientationType_noProjectLocaleLtr_allLtr() {
		Locale.setDefault(LTR_LOCALE);
		setupProjectNotLoaded();
		assertEquals(BiDiUtils.ORIENTATION.ALL_LTR, BiDiUtils.getOrientationType());
	}

	@Test
	public void testGetOrientationType_noProjectLocaleRtl_allRtl() {
		Locale.setDefault(RTL_LOCALE);
		setupProjectNotLoaded();
		assertEquals(BiDiUtils.ORIENTATION.ALL_RTL, BiDiUtils.getOrientationType());
	}

	@Test
	public void testGetOrientationType_allLtrProjectAndRtlLocale_differ() {
		Locale.setDefault(RTL_LOCALE);
		setupAllLtrProject();
		assertEquals(BiDiUtils.ORIENTATION.DIFFER, BiDiUtils.getOrientationType());
	}

	@Test
	public void testGetOrientationType_allRtlProjectAndLtrLocale_differ() {
		Locale.setDefault(Locale.ENGLISH);
		setupAllRtlProject();
		assertEquals(BiDiUtils.ORIENTATION.DIFFER, BiDiUtils.getOrientationType());
	}

	@Test
	public void testGetOrientationType_allLtrProjectAndLtrLocale_allLtr() {
		Locale.setDefault(LTR_LOCALE);
		setupAllLtrProject();
		assertEquals(BiDiUtils.ORIENTATION.ALL_LTR, BiDiUtils.getOrientationType());
	}

	@Test
	public void testGetOrientationType_allRtlProjectAndRtlLocale_allRtl() {
		Locale.setDefault(RTL_LOCALE);
		setupAllRtlProject();
		assertEquals(BiDiUtils.ORIENTATION.ALL_RTL, BiDiUtils.getOrientationType());
	}

	@Test
	public void testGetOrientationType_ltrToRtlProjectAndLtrLocale_differ() {
		Locale.setDefault(LTR_LOCALE);
		setupLtrToRtlProject();
		assertEquals(BiDiUtils.ORIENTATION.DIFFER, BiDiUtils.getOrientationType());
	}

	@Test
	public void testGetOrientationType_ltrToRtlProjectAndRtlLocale_differ() {
		Locale.setDefault(RTL_LOCALE);
		setupLtrToRtlProject();
		assertEquals(BiDiUtils.ORIENTATION.DIFFER, BiDiUtils.getOrientationType());
	}

	@Test
	public void testGetOrientationType_rtlToLtrProjectAndLtrLocale_differ() {
		Locale.setDefault(LTR_LOCALE);
		setupRtlToLtrProject();
		assertEquals(BiDiUtils.ORIENTATION.DIFFER, BiDiUtils.getOrientationType());
	}

	@Test
	public void testGetOrientationType_rtlToLtrProjectAndRtlLocale_differ() {
		Locale.setDefault(RTL_LOCALE);
		setupRtlToLtrProject();
		assertEquals(BiDiUtils.ORIENTATION.DIFFER, BiDiUtils.getOrientationType());
	}

	@Test
	public void testGetOrientation_nullParam_notNull() {
		Locale.setDefault(RTL_LOCALE);
		setupRtlToLtrProject();
		assertNotNull(BiDiUtils.getOrientation(null));
	}

	@Test
	public void testGetOrientation_allLtrTargetIsLtr_Ltr() {
		setupAllLtrProject();
		ComponentOrientation orientation = BiDiUtils.getOrientation(BiDiUtils.ORIENTATION.ALL_LTR);
		assertEquals(ComponentOrientation.LEFT_TO_RIGHT, orientation);
	}

	@Test
	public void testGetOrientation_allRtlTargetIsRtl_Rtl() {
		setupAllRtlProject();
		ComponentOrientation orientation = BiDiUtils.getOrientation(BiDiUtils.ORIENTATION.ALL_RTL);
		assertEquals(ComponentOrientation.RIGHT_TO_LEFT, orientation);
	}

	@Test
	public void testAddRtlBidiAround() {
		String input = "everything";
		String output = BiDiUtils.addRtlBidiAround(input);
		assertEquals("\u202beverything\u202c", output);
	}

	@Test
	public void testAddLtrBidiAround() {
		String input = "everything";
		String output = BiDiUtils.addLtrBidiAround(input);
		assertEquals("\u202aeverything\u202c", output);
	}

	private void setupAllLtrProject() {
		setupProject(LTR_LANGUAGE, LTR_LANGUAGE);
	}

	private void setupAllRtlProject() {
		setupProject(RTL_LANGUAGE, RTL_LANGUAGE);
	}

	private void setupRtlToLtrProject() {
		setupProject(RTL_LANGUAGE, LTR_LANGUAGE);
	}

	private void setupLtrToRtlProject() {
		setupProject(LTR_LANGUAGE, RTL_LANGUAGE);
	}

	private void setupProject(Language sourceLanguage, Language targetLanguage) {
		Core.setProject(new NotLoadedProject() {
			@Override
			public boolean isProjectLoaded() {
				return true;
			}

			@Override
			public ProjectProperties getProjectProperties() {
				return new ProjectProperties() {
					@Override
					public Language getSourceLanguage() {
						return sourceLanguage;
					}

					@Override
					public Language getTargetLanguage() {
						return targetLanguage;
					}
				};
			}
		});
	}

	private void setupProjectNotLoaded() {
		Core.setProject(new NotLoadedProject() {
			@Override
			public boolean isProjectLoaded() {
				return false;
			}
		});
	}

}
