/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.anchorPrompt.analysis;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.anchorPrompt.ui.*;

public class EquivalenceGroup {
	// simply a collection of classes  that are in a subclass-superclass relationship
    Collection _clses = null;

    EquivalenceGroup (Collection c) {
		if (c != null) _clses = new ArrayList(c);
    }

    EquivalenceGroup (EquivalenceGroup eq) {
		if (eq._clses != null) _clses = new ArrayList(eq._clses);
    }

    EquivalenceGroup () {
    	_clses = new ArrayList();
    }

    public void add (Cls cls) {
     	_clses.add(cls);
    }

    public void clear () {
     	_clses.clear();
    }

    public boolean isMember (Cls cls) {
     	return _clses.contains(cls);
    }

    public int size () {
     	return _clses.size();
    }

    public Collection getValues () {
     	return _clses;
    }

    public void setValue (EquivalenceGroup eq) {
    	if (eq._clses == null ) _clses = null;
        else if (_clses == null) _clses = new ArrayList (eq._clses);
        else {
	     	_clses.clear();
    	    _clses.addAll(eq._clses);
        }
    }

    public boolean contains (Object o) {
     	return _clses.contains(o);
    }

    public String toString () {
    	if (_clses == null)
        	return "null";
        String result = "[";
        Iterator i = _clses.iterator();
        while (i.hasNext()) {
         	Cls next = (Cls)i.next();
            result += " ";
            result += DisplayUtilities.frameWithKb(next);
        }
     	return result + "]";
    }
}
