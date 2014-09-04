 /*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu,
 *                 Michel Klein michel.klein@cs.vu.nl
 */

package edu.stanford.smi.protegex.prompt.promptDiff;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.*;

public class SplitClasses implements DiffAlgorithm {
    public boolean usesClassImageInformationInTable () {return true;}
    public boolean usesSlotImageInformationInTable () {return true;}
    public boolean usesFacetImageInformationInTable () {return true;}
    public boolean usesInstanceImageInformationInTable () {return true;}

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
    	Log.getLogger().info ("*********** start SplitClasses ************");
        _changesMade = false;
        if (currentResults == null) return false;
		_results = currentResults;
        startAlgorithm ();
        return _changesMade;
    }

    private static void startAlgorithm () {
        ArrayList unmatchedFromO2 = AlgorithmUtils.filterClses (_results.getUnmatchedEntriesFromO2 ());

        if (unmatchedFromO2 == null || unmatchedFromO2.isEmpty()) return;
        Iterator i = unmatchedFromO2.iterator();
        while (i.hasNext()) {
            Cls nextCls = (Cls)i.next();
            Collection instances = nextCls.getDirectInstances();
            if (instances.isEmpty()) continue;

            Instance firstInstance = (Instance)CollectionUtilities.getFirstItem(instances);
            Instance firstInstanceSource = (Instance)_results.getSoleSource(firstInstance);
            if (firstInstanceSource == null) continue;
            Cls potentialMatch =  firstInstanceSource.getDirectType();
            // **** need to deal with multiple images here
            Cls currentImage = (Cls)_results.getFirstImage(potentialMatch);
            checkIfRealSplit (potentialMatch, currentImage, nextCls);
        }
    }

    private static void checkIfRealSplit (Cls source, Cls currentMatch, Cls additionalMatch) {
        if (source == null || currentMatch == null || additionalMatch == null) return;
     	if (source.getDirectInstanceCount() !=
        		(currentMatch.getDirectInstanceCount() + additionalMatch.getDirectInstanceCount())) return;

		Collection sourceInstances = source.getDirectInstances();
        Iterator i = sourceInstances.iterator();
        while (i.hasNext()) {
         	Instance nextInstance = (Instance)i.next();
            // **** need to deal with multiple images here
            Instance nextInstanceImage = (Instance)_results.getFirstImage(nextInstance);
            if (nextInstanceImage == null) return;
            if (!nextInstanceImage.hasDirectType(currentMatch) &&
            	!nextInstanceImage.hasDirectType(additionalMatch))
            	return;
        }
        TableRow newRow = AlgorithmUtils.createNewMatch(source, additionalMatch, "instances are split", _results);
		Collection rows = getRowsWithImages(_results.getRows(source));
        i = rows.iterator();
        while (i.hasNext()) {
        	TableRow next = (TableRow)i.next();
        	_results.setOperation (next, TableRow.OPERATION_SPLIT);
//                _results.setMappingLevel (next, TableRow.MAPPING_LEVEL_CHANGED);
        }
 	_changesMade = true;
    }

    private static Collection getRowsWithImages (Collection c) {
    	Collection result = new ArrayList();
     	Iterator i = c.iterator();
        while (i.hasNext()) {
         	TableRow next = (TableRow)i.next();
            if (next.getF2Value() != null)
            	result.add(next);
        }
        return result;
    }


	public String toString () {
   		return "SplitClasses";
  	}


}

