/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.anchorPrompt.analysis;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.anchorPrompt.*;

public class Path {
    EquivalenceGroup _start = null;
    EquivalenceGroup _finish = null;

    Cls _startAnchor = null;
    Cls _finishAnchor = null;

	Collection _path = new ArrayList ();
    EquivalenceGroup _currentClses = new EquivalenceGroup();
    int _length = 0;

    Path (Cls start) {
		_startAnchor = start;
	}

    Path (Path oldPath) {
        if (oldPath._start != null)
        	_start = new EquivalenceGroup (oldPath._start);
        if (oldPath._finish != null)
        	_finish = new EquivalenceGroup (oldPath._finish);
        _length = oldPath._length;
		_currentClses = new EquivalenceGroup (oldPath._currentClses);
        _path = new ArrayList (oldPath._path);
        _startAnchor = oldPath._startAnchor;
        _finishAnchor = oldPath._finishAnchor;
    }

    // while classes are added, accumulate the equivalence group
    // when a slot comes in "finishPath", dump the classes into equivalence group
    public void addNextElement (Slot slot) {
        _path.add (new PathElement (_currentClses));
        if (slot != null)
    		_path.add (new PathElement (slot));
        _length ++;
        finishAddElement ();
    }

    private void finishAddElement () {
        if (_start == null) {
            _start = new EquivalenceGroup (_currentClses);
        }
/*
        if (_finish == null)
        	_finish = new EquivalenceGroup (_currentClses);
        else {
	        _finish.setValue (_currentClses);
        }
*/
        _currentClses.clear();
    }

    public void addNextElement (Cls cls) {
    	_currentClses.add (cls);
    }

    public void finishPath () {
        _finish = new EquivalenceGroup (_currentClses);
        _path.add (new PathElement (_currentClses));
		finishAddElement ();
    }

    public boolean continueUpTheHierarchy () {
//    	if (_start == null) return false;
     	return _currentClses.size() < Parameters.EQUIVALENCE_THRESHOLD;
    }

    public boolean equivalent (Path path) {
     	if (_length != path._length || _length == 1) return false;
        if (_startAnchor != path._startAnchor || _finishAnchor != path._finishAnchor)
        	return false;
        Iterator i = _path.iterator();
        Iterator j = path._path.iterator();
        while (i.hasNext()) {
         	PathElement eq1 = (PathElement)i.next();
         	PathElement eq2 = (PathElement)j.next();
            if (i.hasNext()) {
             	PathElement slot1 = (PathElement)i.next();
                PathElement slot2 = (PathElement)j.next();
                if (slot1.getSlot() != slot2.getSlot())
                	return false;
            }
        }
        return true;
    }

    public int length () {
     	return _length;
    }

    public EquivalenceGroup getStartClses () {return _start;}

    public EquivalenceGroup getFinishClses () {return _finish;}

    public Cls getStartAnchor () {return _startAnchor;}

    public Cls getFinishAnchor () {return _finishAnchor;}

    public void setFinishAnchor (Cls cls) {
    	_finishAnchor = cls;
    }

    public String toString () {
        String result = "";
        Iterator i = _path.iterator();
        while (i.hasNext()) {
         	PathElement next = (PathElement)i.next();
            result += ":" + next;
        }
        return result;
//        return result + ", current classes: " + _currentClses;
    }

    public Collection getPathEquivalenceGroups () {
     	Iterator i = _path.iterator();
        Collection result = new ArrayList();
	    while (i.hasNext()) {
         	PathElement next = (PathElement) i.next();
            result.add (next.getEquivalenceGroup() );
            if (i.hasNext()) i.next();
        }
        return result;
    }

    public class PathElement {
       // an instance of Equivalence class or a slot;
       EquivalenceGroup _equiv = null;
       Slot _slot = null;

       PathElement (Slot slot) {
       		_slot = slot;
       }

       PathElement (EquivalenceGroup eq) {
        	_equiv = new EquivalenceGroup (eq);
       }

       Slot getSlot () { return _slot; }

       EquivalenceGroup getEquivalenceGroup () { return _equiv;}

       public String toString () {
       		if (_slot != null) return _slot.toString();
            if (_equiv != null) return _equiv.toString();
            return "null";
       }
    }

}
