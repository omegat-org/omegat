//-------------------------------------------------------------------------
//  
//  LFileCopy.java - 
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
//  Build date:  16Sep2003
//  Copyright (C) 2002, Keith Godfrey
//  keithgodfrey@users.sourceforge.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

class LFileCopy 
{
	static void copy(String src, String dest) throws IOException
	{
		File ifp = new File(src);
		File ofp = new File(dest);
		if (ifp.exists() == false)
		{
			throw new IOException("file '" + src + "' does not exist");
		}
		FileInputStream fis = new FileInputStream(ifp);
		FileOutputStream fos = new FileOutputStream(ofp);
		byte [] b = new byte[1024];
		while (fis.read(b) > 0)
			fos.write(b);
	
		fis.close();
		fos. close();
	}

	public static void main(String[] args)
	{
		int len = Array.getLength(args);
		System.out.println("copying '" + args[0] + "' to '" + args[1] + "'");
		try 
		{
			copy(args[0], args[1]);
		}
		catch (IOException e)
		{
			System.out.println(e);
		}
	}
}
