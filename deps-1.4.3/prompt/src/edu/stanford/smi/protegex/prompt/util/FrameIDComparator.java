 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.util;

import java.util.Comparator;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Slot;


public class FrameIDComparator implements Comparator {
      private Slot _idSlot;

      public FrameIDComparator () {
    	super ();
      }

      public FrameIDComparator (Slot idSlot) {
    	super ();
        _idSlot = idSlot;
      }

      public int compare (Object o1, Object o2) {
        Frame f1 = (Frame)o1;
        Frame f2 = (Frame)o2;

//Log.trace ("slot1 = " + _idSlot + " from " + _idSlot.getKnowledgeBase(), FrameIDComparator.class, "compare");
//Log.trace ("f1 = " + f1 + " from " + f1.getKnowledgeBase() +
//", f2 = " + " from " + f2.getKnowledgeBase() + f2, FrameIDComparator.class, "compare");
//        String id1 = f1.hasOwnSlot(_idSlot) ? (String)f1.getOwnSlotValue(_idSlot) : "";
//        String id2 = f2.hasOwnSlot(_idSlot) ? (String)f2.getOwnSlotValue(_idSlot) : "";
        String id1 = (String)f1.getOwnSlotValue(_idSlot);
        String id2 = (String)f2.getOwnSlotValue(_idSlot);

        if (id1 == null)
          id1 = "";
        if (id2 == null)
          id2 = "";

       return id1.compareTo(id2);
      }
}
