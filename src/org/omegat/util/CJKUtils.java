/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2004  Keith Godfrey et al
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

import java.util.HashSet;
import java.util.Set;


/**
 * Class to test if smth is Chinese/Japanese/Korean.
 * CJK language family is different because it has no spaces between words.
 *
 * @author  Maxym Mykhalchuk
 */
class CJKUtils {
	
	/** the list of unicode block where Japanese symbols fall to */
    private final static Set cjkBlocks = new HashSet();
    static 
	{
        cjkBlocks.add(Character.UnicodeBlock.KATAKANA);
        cjkBlocks.add(Character.UnicodeBlock.HIRAGANA);
        cjkBlocks.add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
        cjkBlocks.add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A);
		// need more:
		// Chinese Traditional
		// Chinese Simplified
		// Korean
    }

	/**
	 * @param ch character to test
	 * @return if the character is CJK
	 */
	public static boolean isCJK(char ch) 
	{
		Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
		return cjkBlocks.contains(block);
	}
}