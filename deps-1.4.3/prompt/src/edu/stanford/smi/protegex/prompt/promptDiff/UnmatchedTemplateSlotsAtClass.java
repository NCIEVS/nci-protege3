 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff;

import java.util.Collection;

import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.ResultTable;

public class UnmatchedTemplateSlotsAtClass implements DiffAlgorithm {
    public boolean usesClassImageInformationInTable () {return true;}
    public boolean usesSlotImageInformationInTable () {return true;}
    public boolean usesFacetImageInformationInTable () {return true;}
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
    	Log.getLogger().info ("*********** start UnmatchedTemplateSlotsAtClass ************");
        _changesMade = false;
        if (currentResults == null) return false;
		_results = currentResults;
        startAlgorithm ();
        return _changesMade;
    }

    private static void startAlgorithm () {
        Collection unmatchedFromO1 = AlgorithmUtils.filterSlots(_results.getUnmatchedEntriesFromO1 ());
        Collection unmatchedFromO2 = AlgorithmUtils.filterSlots(_results.getUnmatchedEntriesFromO2 ());
        if (unmatchedFromO1 == null) return;

		while (unmatchedFromO1.size() >= 2) {
        	 Slot firstSlot = (Slot)CollectionUtilities.getFirstItem(unmatchedFromO1);
             unmatchedFromO1.remove(firstSlot);
             unmatchedFromO1 = findMatches (firstSlot, unmatchedFromO1, unmatchedFromO2);
        }
    }

    private static Collection findMatches (Slot slot, Collection remainingUnmatchedFromO1,
    										Collection unmatchedFromO2) {
//*****stopped here
    	return remainingUnmatchedFromO1;
    }

	public String toString () {
   		return "LoneUnmatchedTemplateSlot";
  	}


}

