//-------------------------------------------------------------------------
//  
//  RequestPacket.java - 
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


// used to wrap a request to the CommandThread 
// requests are queued and executed when CommandThread is not busy
class RequestPacket 
{
	public RequestPacket()
	{
		reset();
	}

	public RequestPacket(int num, Object o)
	{
		type = num;
		obj = o;
	}

	public void set(RequestPacket pack)
	{
		type = pack.type;
		obj = pack.obj;
	}

	public void reset()
	{
		type = NO_OP;
		obj = null;
	}

	public static final int NO_OP		= 1;
	public static final int LOAD		= 2;
	public static final int SAVE		= 3;

	public int type;
	public Object obj;
}
