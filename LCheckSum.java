//-------------------------------------------------------------------------
//  
//  LCheckSum.java - 
//  
//  Copyright (C) 2002, Keith Godfrey
//  
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//  
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//  
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//  
//  Build date:  23Feb2002
//  Copyright (C) 2002, Keith Godfrey
//  aurora@coastside.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------

import java.util.zip.*;

class LCheckSum
{
	public static long compute(String str)
	{
		// since SHA is "in progress" at Sun, create a new system
		// if crc32 is 4 bytes, why the 'long' return value???
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

/////////////////////////////////////////////////////////
	
	public static void main(String[] args)
	{
		System.out.println("a:          " + LCheckSum.compute(new 
				String("a")));
		System.out.println("b:          " + LCheckSum.compute(new 
				String("b")));
		System.out.println("moose:      " + LCheckSum.compute(new 
				String("moose")));
		System.out.println("mouse:      " + LCheckSum.compute(new 
				String("mouse")));
		System.out.println("house:      " + LCheckSum.compute(new 
				String("house")));
		System.out.println("louse:      " + LCheckSum.compute(new 
				String("louse")));
		System.out.println("lousy:      " + LCheckSum.compute(new 
				String("lousy")));
		System.out.println("pink floyd: " + LCheckSum.compute(new 
				String("pink floyd")));
	}
}
