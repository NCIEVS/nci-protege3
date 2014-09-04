 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.analysis;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.actionLists.*;
import edu.stanford.smi.protegex.prompt.explanation.*;
import edu.stanford.smi.protegex.prompt.operation.*;
import edu.stanford.smi.protegex.prompt.util.*;


public class MergeClsesWithTheSameReferences {
  private static final int NUMBER_OR_REFERENCES_THRESHOLD = 2;
  public MergeClsesWithTheSameReferences (Cls cls1, Cls cls2) {
   // cls1 and cls2 are in the sources;
   // consider if cls1 and cls2 refer to the same (or similar) set of classes
   // or if any of their subclasses do
  	Collection referencedFromCls1 = getReferencedClses (cls1);
  	Collection referencedFromCls2 = getReferencedClses (cls2);

    if (referencedFromCls1.size() < NUMBER_OR_REFERENCES_THRESHOLD ||
        referencedFromCls2.size() < NUMBER_OR_REFERENCES_THRESHOLD)
        return;

    Collection mappedReferencedFromCls1 = getMappedClses (referencedFromCls1);
    Collection mappedReferencedFromCls2 = getMappedClses (referencedFromCls2);

    Collection difference12 = new HashSet (mappedReferencedFromCls1);
    difference12.removeAll(mappedReferencedFromCls2);    // 1 - 2

    Collection difference21 = new HashSet (mappedReferencedFromCls2);
    difference21.removeAll(mappedReferencedFromCls1);    // 2 - 1

    if (difference12.size() == 0 && difference21.size() == 0) {
    	// the sets are identical
        Frame whatBecameOfcls1 = Mappings.getWhatBecameOfIt(cls1);
        Frame whatBecameOfcls2 = Mappings.getWhatBecameOfIt(cls2);

     	SuggestionsAndConflicts.addSuggestions
        				(CollectionUtilities.createCollection
                        	(new MergeClsesOperation
                            	((whatBecameOfcls1 != null && whatBecameOfcls1 instanceof Cls) ? (Cls)whatBecameOfcls1 : cls1,
                                (whatBecameOfcls2 != null && whatBecameOfcls2 instanceof Cls) ? (Cls)whatBecameOfcls2 : cls2,
                                new SameReferencedClses())));
    }

  }

  private Collection getReferencedClses (Cls cls) {
   	Collection result = new HashSet();
    Collection slots = cls.getTemplateSlots();
    if (slots == null) return result;

    Iterator i = slots.iterator();
    while (i.hasNext()) {
     	Slot next = (Slot)i.next();
		if (!Util.isSystem(next) && next.getValueType() == ValueType.INSTANCE)
        	result.addAll(cls.getTemplateSlotAllowedClses (next));
    }
    return result;
  }

  private Collection getMappedClses (Collection clses) {
   	//replaces clses in the collection that have already been mapped
    // with their maps ; keeps the other members intact;
    Collection result = new HashSet ();

    Iterator i = clses.iterator();
    while (i.hasNext()){
        Cls next = (Cls)i.next();
        Frame mapping = Mappings.getWhatBecameOfIt(next);
        if (mapping != null && mapping instanceof Cls)
        	result.add(mapping);
        else
        	result.add(next);
    }
    return result;
  }
}
