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

package org.omegat.core.matching;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.omegat.core.StringData;
import org.omegat.core.StringEntry;
import org.omegat.core.threads.CommandThread;
import org.omegat.gui.TransFrame;
import org.omegat.gui.messages.MessageRelay;
import org.omegat.util.StaticUtils;
import org.omegat.util.Token;

/**
 * The class, responsible for building the list of fuzzy matches 
 * between the source text strings.
 *
 * @author  Maxym Mykhalchuk
 */
public class FuzzyMatcher
{
	private String statusTemplate;
	private double nearTrash;
	private TransFrame tf;	
	/** near proj is the project a near (fuzzy matched) string is from */
	private String project;
	private CommandThread core;
	
	private void updateStatus(int index, int total)
	{
		Object[] obj = { new Integer(index), new Integer(total) };
		MessageRelay.uiMessageSetMessageText(tf, MessageFormat.format(statusTemplate, obj));
		Thread.yield();
	}
	
	/** Creates a new instance of FuzzyMatcher */
	public FuzzyMatcher(String statusTemplate, double nearTrash, 
							TransFrame tf, String project, CommandThread core)
	{
		this.statusTemplate = statusTemplate;
		this.nearTrash = nearTrash;
		this.tf = tf;
		this.project = project;
		this.core = core;
	}
	
	/** 
	 * Breaks string into tokens.
	 * Examples:
	 * <ul>
	 * <li> This is a semi-good way. -> "this", "is", "a", "semi-good", "way"
	 * <li> Fine, thanks, and you? -> "fine", "thanks", "and", "you"
	 * </ul>
	 */
	private List breakString(String string)
	{
		ArrayList tokenList = new ArrayList();
        StaticUtils.tokenizeText(string, tokenList);
		return tokenList;
	}
	
	/**
	 * Builds the similarity data for color highlight in match window.
	 */
	private byte[] buildSimilarityData(List sourceTokens, List matchTokens)
	{
		int len = matchTokens.size();
		byte[] result = new byte[len];
		
		boolean leftfound = true;
		for(int i=0; i<len; i++)
		{
			result[i]=0;
			
			Token righttoken = null;
			if( i+1<len )
				righttoken = (Token)matchTokens.get(i+1);
			boolean rightfound = sourceTokens.contains(righttoken);

			Token token;
			token = (Token)matchTokens.get(i);
			boolean found = sourceTokens.contains(token);
			
			if( found && (!leftfound || !rightfound) )
				result[i] = StringData.PAIR;
			else if( !found )
				result[i] = StringData.UNIQ;
			
			leftfound = found;
		}
		return result;
	}
	
	/**
	 * Actually does build the list of fuzzy matches.
	 *
	 * @param strings - the list of the source text strings.
	 */
	public void match(ArrayList strings) throws InterruptedException
	{
		int total = strings.size();
		
		updateStatus(0, total);
		
		List stringTokensList = new ArrayList();
		for(int i=0; i<total; i++)
		{
			StringEntry string = (StringEntry) strings.get(i);
			List stringTokens = breakString(string.getSrcText());
			stringTokensList.add(stringTokens);
		}
		
		for(int i=0; i<total; i++)
		{
			if( i%20==0 )
			{
				if( core.shouldStop() )
					throw new InterruptedException("Stopping on demand");		// NOI18N
				updateStatus(i, total);
			}

			StringEntry strEntry = (StringEntry) strings.get(i);
			List strTokens = (List) stringTokensList.get(i);
			int strTokensSize = strTokens.size();
			
			for(int j=i+1; j<total; j++)
			{
				StringEntry candEntry = (StringEntry) strings.get(j);
				List candTokens = (List) stringTokensList.get(j);
				int candTokensSize = candTokens.size();
				
				int ld = LevenshteinDistance.compute(strTokens, candTokens);
				double similarity = (1.0 * (Math.max(strTokensSize, candTokensSize) - ld)) / 
						Math.max(strTokensSize, candTokensSize);
				
				if( similarity<nearTrash )
					continue;
				
				byte similarityData[] = buildSimilarityData(strTokens, candTokens);
				strEntry.addNearString(candEntry, similarity, similarityData, project);
				
				similarityData = buildSimilarityData(candTokens, strTokens);
				candEntry.addNearString(strEntry, similarity, similarityData, project);
			}
		}
		updateStatus(total, total);
	}
	
}
