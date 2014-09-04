package gov.nih.nci.protegex.util;

import java.util.Vector;

/**
 *@Author: NGIT, Kim Ong; Iris Guo
 */

public class QuickSortVecStrings
{
	public void quickSort(Vector a, int lo0, int hi0) throws Exception
	{
		int lo = lo0;
		int hi = hi0;
		String mid;

		if ( hi0 > lo0)
		{
			mid = (String) a.elementAt( ( lo0 + hi0 ) / 2 );
			while( lo <= hi )
			{
				//while( ( lo < hi0 ) && ( (String) a.elementAt(lo).compareTo(mid) < 0))
				//	++lo;

				while(lo < hi0)
				{
				    String s_lo = (String) a.elementAt(lo);
					if (s_lo.compareTo(mid) < 0)
					{
					    ++lo;
					}
					else
					{
						break;
					}
				}


				//while( ( hi > lo0 ) && ( (String) a.elementAt(hi).compareTo(mid) > 0))
				//	--hi;

				while(hi > lo0)
				{
				    String s_hi = (String) a.elementAt(hi);
					if (s_hi.compareTo(mid) > 0)
					{
					    --hi;
					}
					else
					{
						break;
					}
				}


				// if the indexes have not crossed, swap
				if( lo <= hi )
				{
					swap(a, lo, hi);
					++lo;
					--hi;
				}
			}
			if( lo0 < hi )
				quickSort( a, lo0, hi );

			if( lo < hi0 )
				quickSort( a, lo, hi0 );

		}
	}

	private void swap(Vector a, int i, int j)
	{
		String T;
		T = (String) a.elementAt(i);
		a.setElementAt(a.elementAt(j), i);
		a.setElementAt(T, j);
	}

	public void sort(Vector a) throws Exception
	{
		quickSort(a, 0, a.size() - 1);
	}
}
