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

package org.omegat.core;

import java.util.zip.Adler32;
import java.util.zip.CRC32;

public class LCheckSum
{
	// new method - takes about 40% of the time as the old
	public static long compute2(String str)
	{
		// try a new way to see if it's faster
		int len = str.length();
		long dg1 = 0;
		long dg2 = 0;
		Adler32 adler = new Adler32();
		// strings less than 4 characters should be unique under adler,
		//	so just do a single pass, leaving high bytes zero
		adler.update(str.getBytes());
		dg1 = adler.getValue();
		if (len < 4)
		{
			return dg1;
		}
		// if there is a conflict between two adler values, there 
		//	shouldn't be a conflict between substrings of the original
		//	text
		String sub = str.substring(len/2);
		adler.reset();
		adler.update(sub.getBytes());
		dg2 = adler.getValue();

		return (dg2 << 32) | (dg1);
	}

	// old method - keep it around for posterity (and because it gives
	//	a better pseudorandom sequence)
	public static long compute(String str)
	{
		CRC32 crc = new CRC32();
		Adler32 adler = new Adler32();
		crc.update(str.getBytes());
		adler.update(str.getBytes());
		long dg1 = crc.getValue();
		long dg2 = adler.getValue();
		crc.update(String.valueOf(dg2).getBytes());
		adler.update(String.valueOf(dg1).getBytes());
		long d3 = crc.getValue();
		long d4 = adler.getValue();
		dg1 ^= d4;
		dg2 ^= d3;
		return (dg2 ^ ((dg1 >>> 32) | (dg1 << 32)));
		// convert this to first 8 bytes of SHA later
	}

}
