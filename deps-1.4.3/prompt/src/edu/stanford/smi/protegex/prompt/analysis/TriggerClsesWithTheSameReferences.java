 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.analysis;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;

public class TriggerClsesWithTheSameReferences {
  public TriggerClsesWithTheSameReferences (Cls cls1, Cls cls2) {
  	Collection cls1Sources = findFrameInSource (cls1);
  	Collection cls2Sources = findFrameInSource (cls2);

    if (cls1Sources == null || cls1Sources.size() == 0 ||
        cls2Sources == null || cls2Sources.size() == 0)
    	return;

    // in reality, both collections will almost always be of size 1 and each of
    // the loops will execute only once
	Iterator i = cls1Sources.iterator();
    while (i.hasNext()) {
     	Object next1 = (Cls)i.next();
        if (next1 instanceof Cls) {
         	Cls nextCls1 = (Cls)next1;
            Iterator j = cls2Sources.iterator();
            while (j.hasNext()) {
             	Object next2 = j.next();
                if (next2 instanceof Cls) {
                	Cls nextCls2 = (Cls)next2;
                    findRefsForClses (nextCls1, nextCls2);
                }
            }
        }
    }
  }

  private void findRefsForClses (Cls cls1, Cls cls2) {
  	// now we are in the sources
    Collection refs1 = findRefsThroughFacets (cls1);
    Collection refs2 = findRefsThroughFacets (cls2);

	Iterator i = refs1.iterator();
    while (i.hasNext()) {
     	Cls next1 = (Cls)i.next();
       	Iterator j = refs2.iterator();
        while (j.hasNext()) {
         	Cls next2 = (Cls)j.next();
            Analysis.mergeClsesWithTheSameReferences(next1, next2);
        }
    }
  }

  private Collection findRefsThroughFacets (Cls cls) {
   	Collection refs = cls.getReferences();
    if (refs == null) return null;

    Collection result = new HashSet();
    Iterator i = refs.iterator();
    while (i.hasNext()) {
     	Reference next = (Reference)i.next();
        if (next.getFrame() instanceof Slot) {
        	Slot nextSlot = (Slot)next.getFrame() ;
        	Collection attachedTo = nextSlot.getDirectDomain();
         	result.addAll(attachedTo);
        }
    }
    return result;
  }

  private Collection findFrameInSource (Cls cls) {
   	KnowledgeBaseInMerging kbInMerging = ProjectsAndKnowledgeBases.getKnowledgeBaseInMerging(cls.getKnowledgeBase());
    if (kbInMerging.isTarget()) {
		Collection sources = Mappings.getSources(cls);
		return sources;
    } else {
     	return CollectionUtilities.createCollection (cls);
    }
  }
}
