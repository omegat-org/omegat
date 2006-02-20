/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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
 * <p>
 * Our thanks go to Jean-Christophe Helary for bringing this issue up
 * and finding out what Unicode Blocks are used for CJK languages.
 *
 * @author Maxym Mykhalchuk
 */
class CJKUtils {
	
	/** the list of unicode block where Japanese symbols fall to */
    private final static Set cjkBlocks = new HashSet();
    static 
	{
        // For Japanese:
        cjkBlocks.add(Character.UnicodeBlock.HIRAGANA);
        cjkBlocks.add(Character.UnicodeBlock.KATAKANA);
        cjkBlocks.add(Character.UnicodeBlock.KANBUN);

        // For Chinese:
        cjkBlocks.add(Character.UnicodeBlock.BOPOMOFO);
        cjkBlocks.add(Character.UnicodeBlock.BOPOMOFO_EXTENDED);
        cjkBlocks.add(Character.UnicodeBlock.KANGXI_RADICALS);

        // For Korean:
        cjkBlocks.add(Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO);
        cjkBlocks.add(Character.UnicodeBlock.HANGUL_JAMO);
        cjkBlocks.add(Character.UnicodeBlock.HANGUL_SYLLABLES);

        // For the CJK set:
        cjkBlocks.add(Character.UnicodeBlock.CJK_COMPATIBILITY);
        cjkBlocks.add(Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS);
        cjkBlocks.add(Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS);
        cjkBlocks.add(Character.UnicodeBlock.CJK_RADICALS_SUPPLEMENT);
        cjkBlocks.add(Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION);
        cjkBlocks.add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
        cjkBlocks.add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A);
        cjkBlocks.add(Character.UnicodeBlock.ENCLOSED_CJK_LETTERS_AND_MONTHS);
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