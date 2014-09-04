 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
    *                 Kyle Bruck kbruck@stanford.edu
*/

package edu.stanford.smi.protegex.prompt.operation;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.actionLists.*;
import edu.stanford.smi.protegex.prompt.explanation.*;
import edu.stanford.smi.protegex.prompt.ui.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class MergeSlotsOperation extends MergeFramesOperation {
//  static private GetValueWidget [] _argumentWidgets = new GetValueWidget [MERGE_OPERATION_ARITY];
  private static final String _selectSlotAttachmentPrompt = "Choose facets for the new slot at class ";
  private static final String _selectSlotTopLevelPrompt = "Choose facets for the new slot ";
  private static final String _selectSlotAttachmentTitle = "Choose facets";
  private static final String _chooseOwnSlotValue = "CONFLICT: different own slot values";


  public MergeSlotsOperation () {
    super ();
    initialize ();
  }

  public MergeSlotsOperation (Frame f1, Frame f2) {
    super (f1, f2);
    initialize ();
  }

  public MergeSlotsOperation (Frame f1, Frame f2, Explanation exp) {
    super (f1, f2, exp);
    initialize ();
  }

  private void initialize () {
	String mergeOrMap = Util.mergeOrMapString ();
    _name = mergeOrMap + "-slots";
    _prettyName = mergeOrMap + " slots";
    _shortName = mergeOrMap;
  }

  public void mergeSlots () {
	mergeTopLevelSlotInformation ();
  	mergeTemplateSlotAttachments ();
//    mergeOwnSlotValues ();
  }

/*
  public void performOperation () {
    super.performOperation();
  }
*/
  private void mergeTopLevelSlotInformation () {
  	Slot s1 = (Slot)_args.getArg(0);
  	Slot s2 = (Slot)_args.getArg(1);
    if (_preferredFrame != null) {
     	Util.copyTopLevelSlotValues((Slot)_preferredFrame, (Slot)_newFrame);
        mergeOwnSlotValues (s1, s2);
    } else if (!sameFacets (s1, s2)) {
      Slot [] slots = new Slot [] {s1, s2};

      int choice = DisplayWarning.selectSlotFacets (_selectSlotAttachmentTitle,
                                                        "Choose facets for slot " + getNewName(),
                                                        slots);

      if (choice == 0 || choice == 1)
          Util.copyTopLevelSlotValues ((Slot)slots[choice], (Slot)_newFrame);
    } else {
          Util.copyTopLevelSlotValues (s1, (Slot)_newFrame);
          mergeOwnSlotValues (s1, s2);
    }

    suggestMergeForAllowedClasses (s1, s2);
  }

  private void mergeOwnSlotValues (Slot slot1, Slot slot2) {
  	Slot newSlot = (Slot)_newFrame;
    Collection clsesWithSlot1 = slot1.getDirectDomain();
    Collection clsesWithSlot2 = slot2.getDirectDomain();

    Collection clsesWithBothSlots = new ArrayList(clsesWithSlot1);
    clsesWithBothSlots.retainAll (clsesWithSlot2);

    Collection instancesWithBothSlots = getAllInstancesOfClasses (clsesWithBothSlots);

    if (instancesWithBothSlots == null || instancesWithBothSlots.size() == 0) return;
    Iterator i = instancesWithBothSlots.iterator();
    while (i.hasNext ()) {
    	Instance next = (Instance) i.next();
	    FrameSlotCombination oldCombination1 =  new FrameSlotCombination (next, slot1);
    	FrameSlotCombination oldCombination2 =  new FrameSlotCombination (next, slot2);

        // if there is a preferred frame (slot), just take the value of this slot
        // This means that if preferred slot is empty and non-preferred has a value
        // the new slot will be empty.
        if (_preferredFrame != null)
        	next.setOwnSlotValues(newSlot, next.getOwnSlotValues ((Slot)_preferredFrame));
        else //there is no preferred slot - need to figure out what to do...
        	if (!sameValues (oldCombination1, oldCombination2)) {
      			FrameSlotCombination [] uniqueCombinations =
            				new FrameSlotCombination [] {oldCombination1, oldCombination2};
                // the right thing to do is still to ask the user what to do:
                // pick one set of values, another set of values, or merge the values
                // right now we just merge the values
                // ***** fix this later
				if (slot1.getMaximumCardinality() > 1) {
                	next.setOwnSlotValues(newSlot, mergeSlotValues (next, slot1, next, slot2));
            	} else {
                        // right now, if one of the values of a single-valued slot
                        // is empty, set it to the other one
            		if (next.getOwnSlotValue (slot1) == null ||
                		next.getOwnSlotValue (slot1).equals (""))
                   		next.setOwnSlotValue(newSlot, next.getOwnSlotValue(slot2));
               		else if (next.getOwnSlotValue (slot2) == null ||
                			next.getOwnSlotValue (slot2).equals (""))
                   		next.setOwnSlotValue(newSlot, next.getOwnSlotValue(slot1));
                	else {
   						Object choice = DisplayWarning.selectValue (_chooseOwnSlotValue,
	            		"Choose value for the new slot for " + next.getBrowserText(),
    	  	 			getValueAlternatives (uniqueCombinations), true);

	       				next.setOwnSlotValue (newSlot, choice);
                    }
            	}
        	}
    	}
  }

  private Object getSlotValueFromCombination (FrameSlotCombination c) {
     Frame frame = c.getFrame();
     Slot slot = c.getSlot();
     return frame.getOwnSlotValue(slot);
  }


  private Collection getAllInstancesOfClasses (Collection clses) {
   	if (clses == null || clses.size() == 0) return null;

    Collection result = new ArrayList();
    Iterator i = clses.iterator();
    while (i.hasNext()) {
		result.addAll (((Cls)i.next()).getInstances());
    }

    return result;
  }

  public void mergeTemplateSlotAttachments () {
  	Slot s1 = (Slot)_args.getArg(0);
  	Slot s2 = (Slot)_args.getArg(1);
    Collection classesWithSlot1 = s1.getDirectDomain();

    if (classesWithSlot1 != null && classesWithSlot1.size() > 0) {
    	Collection allClassesWithSlot1 = new ArrayList (classesWithSlot1);
        Iterator i =  classesWithSlot1.iterator();
        while (i.hasNext ()) {
        	Cls next = (Cls)i.next();
            allClassesWithSlot1.addAll(next.getSubclasses());
            next.addDirectTemplateSlot((Slot)_newFrame);
        }
 
    	i = allClassesWithSlot1.iterator();
        while (i.hasNext ()) {
        	Cls next = (Cls)i.next();
  //          if (next.hasDirectlyOverriddenTemplateSlot(s1) && next.hasDirectlyOverriddenTemplateSlot(s2))
            	mergeFacetsForTemplateSlot (next);
        }
    }
  }

  private void mergeFacetsForTemplateSlot (Cls cls) {
  	Slot newSlot = (Slot)_newFrame;
    Slot oldSlot1 = (Slot)_args.getArg(0);
    Slot oldSlot2 = (Slot)_args.getArg(1);

    cls.addDirectTemplateSlot(newSlot);
    FrameSlotCombination oldCombination1 =  new FrameSlotCombination (cls, oldSlot1);
    FrameSlotCombination oldCombination2 =  new FrameSlotCombination (cls, oldSlot2);

    if (!sameFacets (oldCombination1, oldCombination2)) {
      FrameSlotCombination [] uniqueCombinations = new FrameSlotCombination [] {oldCombination1, oldCombination2};

      int choice = DisplayWarning.selectSlotFacets (_selectSlotAttachmentTitle,
                                                        "Choose attachment for slot " + getNewName() +
                                                        " for class " + cls.getName(),
                                                        uniqueCombinations);

      if (choice == 0 || choice == 1)
          Util.copyTemplateFacetValues (cls, uniqueCombinations[choice].getSlot(), newSlot);
    } else {
          Util.copyTemplateFacetValues(cls, oldSlot1, newSlot);
    }

    suggestMergeForAllowedClasses (cls, oldSlot1, oldSlot2, newSlot);

    cls.removeDirectTemplateSlot(oldSlot1);
    cls.removeDirectTemplateSlot(oldSlot2);
  }

  private void suggestMergeForAllowedClasses (Cls cls, Slot oldSlot1, Slot oldSlot2, Slot newSlot) {
    if (cls.getTemplateSlotValueType(oldSlot1) != cls.getTemplateSlotValueType(oldSlot1)) return;

    Collection values1 = null;
    Collection values2 = null;
    if (cls.getTemplateSlotValueType(oldSlot1) == ValueType.CLS) {
        	values1 = cls.getTemplateSlotAllowedParents(oldSlot1);
        	values2 = cls.getTemplateSlotAllowedParents(oldSlot2);
    }
    if (cls.getTemplateSlotValueType(oldSlot1) == ValueType.INSTANCE) {
        	values1 = cls.getTemplateSlotAllowedClses(oldSlot1);
        	values2 = cls.getTemplateSlotAllowedClses(oldSlot2);
    }
    if (values1 == null || values2 == null) return;

    makeSuggestion (newSlot, values1, values2);
  }

  private void suggestMergeForAllowedClasses (Slot s1, Slot s2) {
    if (s1.getValueType () != s2.getValueType()) return;

    Collection values1 = null;
    Collection values2 = null;
    if (s1.getValueType() == ValueType.CLS) {
        	values1 = s1.getAllowedParents ();
        	values2 = s2.getAllowedParents ();
    }
    if (s1.getValueType() == ValueType.INSTANCE) {
        	values1 = s1.getAllowedClses ();
        	values2 = s2.getAllowedClses ();
    }
    if (values1 == null || values2 == null) return;

    makeSuggestion ((Slot)_newFrame, values1, values2);
  }

  private void makeSuggestion (Slot slot, Collection values1, Collection values2) {

    if (values1.size () == 1 && values2.size() == 1) {
      // ** hack for now - assuming there is only one allowed class for each of the slots
      Cls v1 =  (Cls) CollectionUtilities.getFirstItem(values1);
      Cls v2 = (Cls) CollectionUtilities.getFirstItem(values2);
      if (Util.isSystem(v1) && Util.isSystem(v2)) return;
      
     

      Cls s1, s2;
      if (DummyFrame.isDummyFrame(v1))
      	s1 = (Cls)CollectionUtilities.getFirstItem (Mappings.getSources(v1));
      else
      	s1 = v1;
      if (DummyFrame.isDummyFrame(v2))
      	s2 = (Cls)CollectionUtilities.getFirstItem (Mappings.getSources(v2));
      else
        s2 = v2;

//      _currentSuggestedOperations.add
      Operation newOp =
           new MergeClsesOperation (s1 == null ? v1 : s1,
                                     s2 == null ? v2 : s2,
                                     new ValueTypeForSameSlot (slot));
      SuggestionsAndConflicts.addSuggestions (CollectionUtilities.createCollection (newOp));
	}
  }
  
  protected void createMapping () {
	Collection sources = getSourcesForMappingKb ();
	if (sources == null || sources.size() < 2) return;
	Iterator i = sources.iterator();
	Slot source = (Slot)i.next();
	Slot target = (Slot)i.next();
	if (_mappingStoragePlugins == null) return;
	for (int m = 0; m < _mappingStoragePlugins.length; m++) {
		_mappingStoragePlugins[m].createSlotToSlotMapping (source, target);
	}
  }



/*
  static public void createValuesWidgetsImplementation () {
    _argumentWidgets[0] = new SelectSlotWidget ("Ontology for the first slot", 0);
    _argumentWidgets[1] = new SelectSlotWidget ("Ontology for the second slot", 1);

  }
*/
}
