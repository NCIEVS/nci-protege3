 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.actionLists;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

public class HashMapForCollections extends HashMap {
 private int operationsInQueue = 0;
 private static final int DEFAULT_SIZE = 100;

 public HashMapForCollections (int capacity) {
   super ((capacity > DEFAULT_SIZE) ? DEFAULT_SIZE : capacity);
 }

  public HashMapForCollections () {
   	super (DEFAULT_SIZE);
  }


 public Object put(Object key, Object value) {
   Object oldValue = get (key);
   Stack temp;
   operationsInQueue++;
   if (oldValue == null) {
     temp = new Stack();
     temp.push (value);
     super.put (key, temp);
     return null;
   }
   else {
   	 if (!(((Stack)oldValue).contains(value))) {
     	((Stack)oldValue).push (value);
     	return super.put (key, oldValue);
   	 } else
   	 	return null;
   }
 }

 public void updateKey (Object newKey, Object oldKey) {
  	Object oldValue = remove (oldKey);
    if (oldValue == null) return;

    super.put (newKey, oldValue);
 }

/*
 public Object remove (Object value) {
	Collection allStacks = values();
    if (allStacks == null) return;
    Iterator i = allStacks.iterator();
    while (i.hasNext()) {
     	Stack next = new Stack ((Stack)i.next());
        Iterator j = next.iterator();
        while (j.hasNext()) {
         	value.equlas (j.next())
        }
    }

 }
*/
 public Collection getValues (Object key) {
  	Object result = super.get(key);
    if (result == null)
    	return null;
    else
    	return (Collection)result;
 }

 public boolean remove (Object key, Object value) {
    Object obj = super.get (key);
    if (obj != null) {
      Stack coll = (Stack)obj;
      boolean removedValue = coll.remove (value);

      if (removedValue)  {
        operationsInQueue--;
        if (coll.isEmpty())
        	remove (key);
        return true;
      }
      else
        return false;
    }
    else
      return false;
  }

  public ArrayList listInPriorityOrder () {
//Log.enter (this, "listInPriorityOrder");
    Iterator i = values().iterator();
//Log.trace ("values: " + values (), this, "listInPriorityOrder");
    ArrayList actionsOrdered = new ArrayList (operationsInQueue);
    Object  next;
    Iterator inside;

    while (i.hasNext()) {
      next = i.next();
      if (next instanceof Collection) {
        inside = ((Collection)next).iterator();
        while (inside.hasNext())
          actionsOrdered.add (inside.next());
      }
      else
          actionsOrdered.add (i.next());
    }
//Log.trace ("actionsOrdered: " + actionsOrdered , this, "listInPriorityOrder");
    return actionsOrdered;
  }

/*
  public Map displayNumbered () {
    return DisplayUtilities.displayNumbered (listInPriorityOrder ());
  }
*/
  public int getNumberOfActionsInList () {return operationsInQueue; }

}

