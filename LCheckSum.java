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
//  Build date:  16Apr2003
//  Copyright (C) 2002, Keith Godfrey
//  keithgodfrey@users.sourceforge.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------

import java.util.zip.*;
import java.util.Date;

class LCheckSum
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

/////////////////////////////////////////////////////////
	
	public static void main(String[] args)
	{
		int i;
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

		Date start;
		Date end;
		start = new Date();
		for (i=0; i<20000; i++)
		{
			LCheckSum.compute(new String("a    "));
			LCheckSum.compute(new String("b    "));
			LCheckSum.compute(new String("moose"));
			LCheckSum.compute(new String("mouse"));
			LCheckSum.compute(new String("house"));
			LCheckSum.compute(new String("louse"));
			LCheckSum.compute(new String("lousy"));
			LCheckSum.compute(new String("pink floyd"));
		}
		end = new Date();
		System.out.println("new mode - elapsed time: " + 
				(end.getTime() - start.getTime()));

		System.out.println("a:          " + LCheckSum.compute2(new 
				String("a")));
		System.out.println("b:          " + LCheckSum.compute2(new 
				String("b")));
		System.out.println("moose:      " + LCheckSum.compute2(new 
				String("moose")));
		System.out.println("mouse:      " + LCheckSum.compute2(new 
				String("mouse")));
		System.out.println("house:      " + LCheckSum.compute2(new 
				String("house")));
		System.out.println("louse:      " + LCheckSum.compute2(new 
				String("louse")));
		System.out.println("lousy:      " + LCheckSum.compute2(new 
				String("lousy")));
		System.out.println("pink floyd: " + LCheckSum.compute2(new 
				String("pink floyd")));
		start = new Date();
		for (i=0; i<20000; i++)
		{
			LCheckSum.compute2(new String("a    "));
			LCheckSum.compute2(new String("b    "));
			LCheckSum.compute2(new String("moose"));
			LCheckSum.compute2(new String("mouse"));
			LCheckSum.compute2(new String("house"));
			LCheckSum.compute2(new String("louse"));
			LCheckSum.compute2(new String("lousy"));
			LCheckSum.compute2(new String("pink floyd"));
		}
		end = new Date();
		System.out.println("old mode - elapsed time: " + 
				(end.getTime() - start.getTime()));
	}
}
