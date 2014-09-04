 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.*;
import edu.stanford.smi.protegex.prompt.util.*;

// Looks through unmatched anonymous superclasses of a class
// and if there is only one on each side of a particular type (e.g., cardinality restriction)
// matches them.

public class LoneUnmatchedRestrictionOfSameType implements DiffAlgorithm {
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
    private static boolean _localAnythingChanged = false;

	private static KnowledgeBase _kb1 = null;
	private static KnowledgeBase _kb2 = null;

	public static boolean run (ResultTable currentResults, PromptDiff promptDiff) {
		if (!sourceKbsInOwl(promptDiff.getKb1(), promptDiff.getKb2())) return false;
	   	Log.getLogger().info ("*********** start LoneUnmatchedRestrictionOfSameType ************");
        _changesMade = false;
        if (currentResults == null) return false;
		_results = currentResults;
        startAlgorithm ();
        return _changesMade;
    }

	private static boolean sourceKbsInOwl (KnowledgeBase kb1, KnowledgeBase kb2) {
		_kb1 =  kb1;
		_kb2 =  kb2;
		return (PromptTab.kbInOWL () );
	}

   private static void startAlgorithm () {
        Collection unmatchedAnonymousFromO1 = OWLUtil.anonymousClassesFromCollection (_results.getUnmatchedEntriesFromO1 (), _kb1);
        Collection unmatchedAnonymousFromO2 = OWLUtil.anonymousClassesFromCollection (_results.getUnmatchedEntriesFromO2 (), _kb2);
        Iterator i = unmatchedAnonymousFromO1.iterator();
        while (i.hasNext()) {
         	Cls nextAnonymousCls = (Cls)i.next();
            findMatch (nextAnonymousCls, unmatchedAnonymousFromO2);
        }
    }

    private static void findMatch (Cls cls, Collection unmatchedInO2) {
    	String typeName = cls.getDirectType().getName();
     	Collection subclasses = cls.getDirectSubclasses();
        if (subclasses == null || subclasses.size() != 1) return;
        Cls sub = (Cls) CollectionUtilities.getSoleItem(subclasses);
        
        if (!soleSuperOfThisType (sub, typeName)) return;
        
        Cls subImage = (Cls)_results.getFirstImage(sub);
        if (subImage == null) return;
        
        Collection imageSuperclasses = subImage.getDirectSuperclasses();
		Iterator i = imageSuperclasses.iterator();
		Collection unmatchedImages = new ArrayList();
		while (i.hasNext() && unmatchedImages.size() < 2) {
			Cls nextSuperclass = (Cls)i.next();
			if (OWLUtil.isOWLAnonymousClassFrame(nextSuperclass) &&
				nextSuperclass.getDirectType().getName().equals(typeName) &&
				_results.getSoleSource(nextSuperclass) == null)
					unmatchedImages.add(nextSuperclass);
		}
		if (unmatchedImages.size() == 1) {
			AlgorithmUtils.createNewMatch(cls, (Cls)CollectionUtilities.getSoleItem(unmatchedImages),
											"single unmatched anonynous superclasses of the same type", _results);
			_changesMade = true;
		}
    }
    
    private static boolean soleSuperOfThisType (Cls sub, String typeName) {
    	Collection supers = sub.getDirectSuperclasses();
    	Iterator i = supers.iterator();
    	int supersFound = 0;
    	while (i.hasNext() && supersFound < 2) {
    		Cls nextSuper = (Cls)i.next();
    		if (nextSuper.getDirectType().getName().equals(typeName))
				supersFound++;
    	}
    	return (supersFound == 1);
    }

	public String toString () {
   		return "LoneUnmatchedRestrictionOfSameType";
  	}


}

