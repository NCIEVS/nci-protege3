 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

public class Queue {
    LinkedList _list;

    public Queue () {
     	_list = new LinkedList();
    }

    public void put (Object o) {
     	_list.addLast(o);
    }

    public void putAll (Collection c) {
      if (c==null) return;
      Iterator i = c.iterator();
      while (i.hasNext())
        _list.addLast(i.next());
    }

    public Object get () {
     	return _list.removeFirst();
    }

    public void removeAllCopies (Object o) {
   	while (_list.remove(o));
    }

    public void clear () {
      _list.clear();
    }

    public boolean isEmpty () {
     	return _list.isEmpty();
    }

    public int size () {
        return _list.size();
    }

    public String toString () {
      if (_list == null) return "NULL";
     	return _list.toString();
    }

    public Collection toCollection () {
     	return _list;
    }

    public void reverse () {
     	if (_list==null) return;
        Collections.reverse(_list);
    }
}
