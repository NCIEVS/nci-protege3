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

public class ConsiderMergingSlots {
  public ConsiderMergingSlots (Cls cls, boolean fromClassCopy) {
   	ArrayList slots = new ArrayList (cls.getTemplateSlots());
    if (slots == null || slots.size() == 0) return;

    if (!fromClassCopy ||
    	 fromClassCopy && slots.size() != cls.getDirectTemplateSlots().size())
	//compare the slots of the class itself
    	compareSlotsInTwoCollections (slots, slots, cls);

    Collection subclasses = cls.getSubclasses();
    if (subclasses == null || subclasses.size() == 0) return;

    Iterator i = subclasses.iterator();
    while (i.hasNext()) {
     	Cls next = (Cls)i.next();
        ArrayList directTemplateSlots = new ArrayList (next.getDirectTemplateSlots());
        if (directTemplateSlots != null && directTemplateSlots.size() != 0)
            compareSlotsInTwoCollections (slots, directTemplateSlots, next);
    }
  }

/*
  public ConsiderMergingSlots (Cls cls) {
    ArrayList slots = new ArrayList(cls.getTemplateSlots());
    if (slots == null || slots.size() < 2) return;

	if (slots.size() == cls.getDirectTemplateSlots().size()) return; //
    compareSlotsInTwoCollections (slots, slots, cls);
  }
*/
  private void compareSlotsInTwoCollections (ArrayList c1, ArrayList c2, Cls cls) {
    Iterator i = c1.iterator();
    ListIterator j;

    Slot next1;
    Slot next2;
    Operation newOp;

     while (i.hasNext()) {
        next1 = (Slot)i.next();
        j = c2.listIterator(c2.size());
        while (j.hasPrevious()) {
        	next2 = (Slot)j.previous();
        	    if (next1.isIncluded() || next2.isIncluded()) continue;
            if (!next1.equals (next2)) {
        	    String name1 = Mappings.getRealName(next1);
    	        String name2 = Mappings.getRealName(next2);
	            if (name1.equals (name2) ||
            	// assume lazy evaluation and not include !name1.equals (name2)
        	    	CompareNames.closeEnough(name1, name2) &&
    	        	comeFromDifferentKbs (next1, next2) &&
	                // avoid extra suggestions when a class has, for example, dose and dose_units
                	noSameNameFromDifferentKb (next1, c2) &&
            	    noSameNameFromDifferentKb (next2, c1)) {
				        newOp = new MergeSlotsOperation (next1, next2, new SimilarNames());
    	    			newOp.addExplanation(new SameRole("slots", cls));
	            		SuggestionsAndConflicts.addSuggestions (CollectionUtilities.createCollection (newOp));
//        	_currentSuggestedOperations.add (newOp);
				}
            }
        }
    }
  }

  private boolean noSameNameFromDifferentKb (Frame f, Collection c) {
  	String realName = Mappings.getRealName(f);
   	Iterator i = c.iterator();
    while (i.hasNext()) {
     	Frame next = (Frame)i.next();
        if (!next.equals (f) && Mappings.getRealName(next).equals(realName))
        	return false;
    }
    return true;
  }

  private boolean comeFromDifferentKbs (Frame f1, Frame f2) {
    KnowledgeBase kb1 = getKb (f1);
    KnowledgeBase kb2 = getKb (f2);
   	if (kb1 != null && kb2 != null) {
    	return !kb1.equals (kb2);
    } else
    	return false;
  }

  private KnowledgeBase getKb (Frame f) {
  	if (KnowledgeBaseInMerging.isInTarget(f)) {
    	Collection sources = Mappings.getSources(f);
        if (sources == null) return null;
        else return getKb((Frame)CollectionUtilities.getFirstItem(sources));
    } else
    	return f.getKnowledgeBase();

  }

/*
  public ConsiderMergingSlots (Cls cls) {
    ArrayList slots = new ArrayList(cls.getTemplateSlots());
    if (slots == null || slots.size() < 2) return;

    ArrayList slotNames = Mappings.getRealNames (slots);
    Collections.sort(slotNames, new NameComparator());

    Iterator i = slotNames.iterator();
    Iterator j = slotNames.iterator();

    String next1;
    String next2 = (String)j.next(); // just move the pointer forward
    Operation newOp;


     while (j.hasNext()) {
        next1 = (String)i.next();
        next2 = (String)j.next();
        if (next1.equalsIgnoreCase(next2)) {
        	Slot [] slotsToMerge = Mappings.findPrototypes (next1, slots);
	        newOp = new MergeSlotsOperation (slotsToMerge [0], slotsToMerge [1],
                                          	new IdenticalNames());
        	newOp.addExplanation(new SameRole("slots", cls));
            SuggestionsAndConflicts.addSuggestions (CollectionUtilities.createCollection (newOp));
//        	_currentSuggestedOperations.add (newOp);
        }
    }
  }
*/
  private static class NameComparator implements Comparator {
    public int compare (Object o1, Object o2) {
      return CompareNames.compareNames ((String)o1, (String)o2);
    }
  }
}
