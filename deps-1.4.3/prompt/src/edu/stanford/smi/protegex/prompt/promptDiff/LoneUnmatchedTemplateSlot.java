 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.*;

public class LoneUnmatchedTemplateSlot implements DiffAlgorithm {
    public boolean usesClassImageInformationInTable () {return true;}
    public boolean usesSlotImageInformationInTable () {return true;}
    public boolean usesFacetImageInformationInTable () {return false;}
    public boolean usesInstanceImageInformationInTable () {return false;}

    public boolean usesClassOperationInformationInTable () {return false;}
    public boolean usesSlotOperationInformationInTable () {return false;}
    public boolean usesFacetOperationInformationInTable () {return false;}
    public boolean usesInstanceOperationInformationInTable () {return false;}

    public boolean modifiesClassImageInformationInTable () {return false;}
    public boolean modifiesSlotImageInformationInTable () {return true;}
    public boolean modifiesFacetImageInformationInTable () {return false;}
    public boolean modifiesInstanceImageInformationInTable () {return false;}

    public boolean modifiesClassOperationInformationInTable () {return false;}
    public boolean modifiesSlotOperationInformationInTable () {return false;}
    public boolean modifiesFacetOperationInformationInTable () {return false;}
    public boolean modifiesInstanceOperationInformationInTable () {return false;}

//-----------------------------------------------------------------------------

 	private static ResultTable _results;
    private static boolean _changesMade = false;

	public static boolean run (ResultTable currentResults, PromptDiff promptDiff) {
    	Log.getLogger().info ("*********** start LoneUnmatchedTemplateSlot ************");
        _changesMade = false;
        if (currentResults == null) return false;
		_results = currentResults;
        startAlgorithm ();
        return _changesMade;
    }

    private static void startAlgorithm () {
        Collection unmatchedFromO1 = AlgorithmUtils.filterSlots(_results.getUnmatchedEntriesFromO1 ());
        Collection unmatchedFromO2 = AlgorithmUtils.filterSlots(_results.getUnmatchedEntriesFromO2 ());
        Collection loneTemplateSlotsFromO1 = getLoneTemplateSlots (unmatchedFromO1);
        if (loneTemplateSlotsFromO1 == null)
        	return;

        Iterator i = loneTemplateSlotsFromO1.iterator();
        while (i.hasNext()) {
         	Slot nextLoneTemplateSlot = (Slot)i.next();
            findMatch (nextLoneTemplateSlot, unmatchedFromO2);
        }
    }

    private static Collection getLoneTemplateSlots (Collection unmatchesSlots) {
     	Collection result = new ArrayList ();
        Iterator i = unmatchesSlots.iterator();
        while (i.hasNext()) {
            Slot nextSlot = (Slot)i.next();
			if (loneUnmatchedTemplateSlot (nextSlot))
            	result.add(nextSlot);
        }
        if (result.isEmpty()) return null;
        return result;
    }

    // returns true if it's the only slot in its class that doesn't have an image
    private static boolean loneUnmatchedTemplateSlot (Slot slot){
     	Collection attachedToClses = slot.getDirectDomain();
        if (attachedToClses == null || attachedToClses.isEmpty()) return false;
        Iterator clses = attachedToClses.iterator();
        while (clses.hasNext()) {
         	Cls nextCls = (Cls)clses.next();
            // ***** need to deal with multiple images here
			Object nextClsImage = _results.getFirstImage(nextCls);
            if (nextClsImage == null) {
            	return false;
            }
            Collection templateSlots = nextCls.getDirectTemplateSlots();
            Iterator slots = templateSlots.iterator();
            while (slots.hasNext()) {
             	Slot nextSlot = (Slot)slots.next();
                // ***** need to deal with multiple images here
                if (!nextSlot.equals (slot) && _results.getFirstImage(nextSlot) == null ) {
					return false;
                }
            }
        }
        return true;
    }

    private static void findMatch (Slot loneTemplateSlot, Collection unmatchedInO2) {
     	Collection attachedToClses = loneTemplateSlot.getDirectDomain();
        Iterator clses = attachedToClses.iterator();
        while (clses.hasNext()) {
         	Cls nextCls = (Cls)clses.next();
            Collection nextTemplateSlots = nextCls.getDirectTemplateSlots();
            // **** need to deal with multiple images here
            Cls nextClsImage = (Cls)_results.getFirstImage(nextCls);
            Collection nextClsImageTemplateSlots = new ArrayList(nextClsImage.getDirectTemplateSlots());
            Collection nextTemplateSlotsImages = AlgorithmUtils.getImages (nextTemplateSlots, _results);
            nextClsImageTemplateSlots.removeAll(nextTemplateSlotsImages);

            if (nextClsImageTemplateSlots.size() != 0) {
				Slot newMatch = null;
	            if (nextClsImageTemplateSlots.size() == 1)
            		newMatch = (Slot)CollectionUtilities.getSoleItem(nextClsImageTemplateSlots);
            	else
        	    	newMatch = findBestSlotMatch (nextCls, loneTemplateSlot, nextClsImage, nextClsImageTemplateSlots);
    	        if (newMatch != null && unmatchedInO2.remove(newMatch)) {
	                AlgorithmUtils.createNewMatch(loneTemplateSlot, newMatch, "lone unmatched template slot", _results);
	                _changesMade = true;
	            }
            }
        }
    }

    private static Slot findBestSlotMatch (Cls cls1, Slot loneTemplateSlot1,
    									   Cls cls2, Collection remainingSlots) {
    	// **** implement this method, possibly generating a split or a 1-to-many match
	   	return null;
    }

	public String toString () {
   		return "LoneUnmatchedTemplateSlot";
  	}


}

