 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff.structures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class StackSet {
	private ArrayList _stack;

    public StackSet () {
     	_stack = new ArrayList();
    }

    public void push (Object o) {
      if (!_stack.contains(o))
	        _stack.add(0, o);
    }

    public void pushAll (Collection c) {
    	if (c == null) return;
        Iterator i = c.iterator();
        while (i.hasNext())
        	push (i.next());
    }

    public Object pop () {
     	if (_stack.isEmpty()) return null;
        return _stack.remove(0);
    }

    public boolean isEmpty () {return _stack.isEmpty();};

}

