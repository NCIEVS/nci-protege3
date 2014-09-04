 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.*;

public class UnmatchedSuperclass implements DiffAlgorithm {
    public boolean usesClassImageInformationInTable () {return true;}
    public boolean usesSlotImageInformationInTable () {return true;}
    public boolean usesFacetImageInformationInTable () {return false;}
    public boolean usesInstanceImageInformationInTable () {return false;}

    public boolean usesClassOperationInformationInTable () {return false;}
    public boolean usesSlotOperationInformationInTable () {return false;}
    public boolean usesFacetOperationInformationInTable () {return false;}
    public boolean usesInstanceOperationInformationInTable () {return false;}

    public boolean modifiesClassImageInformationInTable () {return true;}
    public boolean modifiesSlotImageInformationInTable () {return false;}
    public boolean modifiesFacetImageInformationInTable () {return false;}
    public boolean modifiesInstanceImageInformationInTable () {return false;}

    public boolean modifiesClassOperationInformationInTable () {return false;}
    public boolean modifiesSlotOperationInformationInTable () {return false;}
    public boolean modifiesFacetOperationInformationInTable () {return false;}
    public boolean modifiesInstanceOperationInformationInTable () {return false;}

//-----------------------------------------------------------------------------

 	private static ResultTable _results;
    private static boolean _changesMade = false;
    private static boolean _localAnythingChanged = false;

	public static boolean run (ResultTable currentResults, PromptDiff promptDiff) {
    	Log.getLogger().info ("*********** start UnmatchedSuperclass ************");
        _changesMade = false;
        if (currentResults == null) return false;
		_results = currentResults;
        startAlgorithm ();
        return _changesMade;
    }

    private static void startAlgorithm () {
        Collection unmatchedFromO1 = AlgorithmUtils.filterClses (_results.getUnmatchedEntriesFromO1 ());
        Collection unmatchedFromO2 = AlgorithmUtils.filterClses (_results.getUnmatchedEntriesFromO2 ());

        Iterator i = unmatchedFromO1.iterator();
        while (i.hasNext()) {
         	Cls nextCls = (Cls)i.next();
            findMatch (nextCls, unmatchedFromO2);
        }
    }

    private static void findMatch (Cls cls, Collection unmatchedInO2) {
     	Collection subclasses = cls.getDirectSubclasses();
        if (subclasses == null || subclasses.size() == 0) return;
        Collection subsImages = new ArrayList();
        Iterator subs = subclasses.iterator();
        while (subs.hasNext()) {
			Cls nextSub = (Cls)subs.next();
            // **** need to deal with multiple images here
            Cls nextSubImage = (Cls)_results.getFirstImage(nextSub);
            if (nextSubImage == null) return;
            subsImages.add(nextSubImage);
       }
       Collection commonSuperclassesOfImages = AlgorithmUtils.findCommonSuperclasses (subsImages);
       if (commonSuperclassesOfImages.size() == 1) {
        	Cls newMatch = (Cls)CollectionUtilities.getSoleItem(commonSuperclassesOfImages);
            if (unmatchedInO2.remove(newMatch) && AlgorithmUtils.sameSlots (cls, newMatch, true, _results)) {
                AlgorithmUtils.createNewMatch(cls, newMatch, "unmatched superclass", _results);
                _changesMade = true;
            }
       }
    }

	public String toString () {
   		return "UnmatchedSuperclass";
  	}


}

