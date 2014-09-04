 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.actionLists;


public class PriorityList extends HashMapForCollections {
  int currentPriority = 0;

  PriorityList (int capacity) {
    super (capacity);
  }

  public int getCurrentPriority () {return currentPriority; }

  public void incrementCurrentPriority () { currentPriority++; }

  public Object changePriorityToCurrent (Action a) {
    setPriority (a, currentPriority);
    return a;
  }

  public void setPriority (Action a, int value) {
    boolean removed = super.remove (new Integer (a.getPriority()), a);
    if (!removed) return ;
    a.setPriority (value);
    super.put (new Integer (value), a);
  }


}