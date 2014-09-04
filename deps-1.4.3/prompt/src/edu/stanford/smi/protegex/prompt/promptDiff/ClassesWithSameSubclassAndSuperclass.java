 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class ClassesWithSameSubclassAndSuperclass implements DiffAlgorithm {
    public boolean usesClassImageInformationInTable () {return true;}
    public boolean usesSlotImageInformationInTable () {return false;}
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

    // if the set of unmatched siblings on the one side is the same as the one on the other side
    // and the only difference is the same suffix (or prefix), match them
	public static boolean run (ResultTable currentResults, PromptDiff promptDiff) {
    	Log.getLogger().info ("*********** start ClassesWithSameSubclassAndSuperclass ************");
        _changesMade = false;
        if (currentResults == null) return false;
		_results = currentResults;
        startAlgorithm ();
        return _changesMade;
    }

    private static void startAlgorithm () {
        ArrayList unmatchedFromO1 = AlgorithmUtils.filterClses (_results.getUnmatchedEntriesFromO1 ());
        ArrayList unmatchedFromO2 = AlgorithmUtils.filterClses (_results.getUnmatchedEntriesFromO2 ());

        if (unmatchedFromO1 == null || unmatchedFromO1.isEmpty()) return;
        int i = 0;
        while (!unmatchedFromO1.isEmpty() && i < unmatchedFromO1.size()) {
         	Cls nextUnmatchedCls = (Cls)unmatchedFromO1.get(i);
            if (_results.getFirstImage(nextUnmatchedCls) == null) // i.e., we haven't found a match for it in
            	considerMatches (nextUnmatchedCls, unmatchedFromO1, unmatchedFromO2); // one of the previous iterations of this loop
			i++;
        }
    }

    private static void considerMatches (Cls unmatchedCls, Collection unmatchedInO1, Collection unmatchedInO2) {
        Collection subclasses = Util.getDirectSubclasses(unmatchedCls);
        if (subclasses == null || subclasses.isEmpty()) return;

        Collection subclassImages = AlgorithmUtils.getImages(subclasses, _results);
        if (subclassImages == null || subclassImages.isEmpty()) return;
        Collection imageSuperclasses = AlgorithmUtils.findCommonSuperclasses (subclassImages);
        if (imageSuperclasses.size() != 1) return;

        Cls potentialMatch = (Cls)CollectionUtilities.getSoleItem(imageSuperclasses);
        if (!unmatchedInO2.contains(potentialMatch)) return;

        Collection potentialMatchSuperclasses = new ArrayList(Util.getDirectSuperclasses(potentialMatch));
     	Collection superclasses = Util.getDirectSuperclasses(unmatchedCls);
        Collection superclassImages = AlgorithmUtils.getImages(superclasses, _results);

        potentialMatchSuperclasses.retainAll(superclassImages);
        if (!potentialMatchSuperclasses.isEmpty()) {
        	AlgorithmUtils.createNewMatch (unmatchedCls, potentialMatch, "Same superclass and subclasses", _results);
            _changesMade = true;
        }

	}

	public String toString () {
   		return "ClassesWithSameSubclassAndSuperclass";
  	}


}

