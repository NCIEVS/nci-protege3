package com.clarkparsia.explanation.io.html.utils;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.semanticweb.owl.model.OWLNamedObject;
import org.semanticweb.owl.model.OWLObject;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 *
 * @author Evren Sirin
 */
public class DescriptionSorter {

	/**
	 * Sorts a set of OWLObjects alphabetically based on toString values.
	 * Named objects always come before unnamed objects.
	 * 
	 * @param set the set to sort
	 * @return the sorted version of the set
	 */
	public static <N extends OWLObject> Set<N> toSortedSet(Set<N> set) {
	    Set<N> sorted = new TreeSet<N>(new Comparator<N>() {
	        public int compare(N o1, N o2) {
	        	boolean named1 = o1 instanceof OWLNamedObject;
	        	boolean named2 = o2 instanceof OWLNamedObject;
	        	if (named1 && !named2)
	        		return -1;
	        	else if (!named1 && named2)
	        		return 1;
	        	else
	        		return o1.toString().compareTo(o2.toString());
	        }
	    });
	    sorted.addAll(set);
	    return sorted;
	}

}
