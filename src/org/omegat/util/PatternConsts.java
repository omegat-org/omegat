/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2005  Keith Godfrey et al
                          keithgodfrey@users.sourceforge.net
                          907.223.2039

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**************************************************************************/

package org.omegat.util;

import java.util.regex.Pattern;

/**
 * Constant patterns, used in dirrerent other classes.
 *
 * @author Maxym Mykhalchuk
 */
class PatternConsts
{
	/** compiled pattern to extract the encoding from XML file, if any */
	public static final Pattern XML_ENCODING = Pattern.compile(
		"<\\?xml.*?encoding\\s*=\\s*\"(\\S+?)\".*?\\?>");                       // NOI18N
	
	/** compiled pattern to extract the encoding from XML file, if any */
	public static final Pattern XML_HEADER = Pattern.compile(
		"(<\\?xml.*?\\?>)");                                                    // NOI18N
	
	/** compiled pattern to extract the encoding from HTML file, if any */
	public static final Pattern HTML_ENCODING = Pattern.compile(
		"<meta.*?content\\s*=\\s*[\"']\\s*text/html\\s*;\\s*charset\\s*=\\s*(\\S+?)[\"'].*?/?\\s*>",  // NOI18N
		Pattern.CASE_INSENSITIVE);
	
	/** compiled pattern to look for HTML file HEAD declaration */
	public static final Pattern HTML_HEAD = Pattern.compile(
		"<head.*?>",                                                            // NOI18N
		Pattern.CASE_INSENSITIVE);
	
	/** compiled pattern to look for HTML file HTML declaration */
	public static final Pattern HTML_HTML = Pattern.compile(
		"<html.*?>",                                                            // NOI18N
		Pattern.CASE_INSENSITIVE);

}
