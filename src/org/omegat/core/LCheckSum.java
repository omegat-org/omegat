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
