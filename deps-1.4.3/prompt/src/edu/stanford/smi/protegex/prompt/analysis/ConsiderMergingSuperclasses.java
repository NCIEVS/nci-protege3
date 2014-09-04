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

public class ConsiderMergingSuperclasses {
  public ConsiderMergingSuperclasses (Cls cls1, Cls cls2) {
    Collection superclses1 = Util.getDirectSuperclasses(cls1);
    Collection superclses2 = Util.getDirectSuperclasses(cls2);

    Iterator i1 = superclses1.iterator();
    Iterator i2 = superclses2.iterator();

    while (i1.hasNext()) {
     	Cls nextSupercls1 = (Cls)i1.next();
        while (i2.hasNext()) {
        	Cls nextSupercls2 = (Cls)i2.next();
			if (!Util.isSystem(nextSupercls1) && !Util.isSystem(nextSupercls2) && !nextSupercls1.isIncluded() && ! nextSupercls2.isIncluded() &&
            	CompareNames.closeEnough (Util.getLocalBrowserText(nextSupercls1), Util.getLocalBrowserText (nextSupercls2))) {
	        	Operation newOp = new MergeClsesOperation (nextSupercls1, nextSupercls2,
                                          	new SimilarNames());
        		newOp.addExplanation(new SameRole("superclasses", Mappings.getWhatBecameOfIt(cls1)));
            	SuggestionsAndConflicts.addSuggestions (CollectionUtilities.createCollection (newOp));
            }
        }
    }
  }

}
