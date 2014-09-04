 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.analysis;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.*;

public class Analysis {
  public static void  considerMergingSlots (Cls cls) {
  	if (PromptTab.merging () || PromptTab.mapping())
   		new ConsiderMergingSlots(cls, false);
  }

  public static void classesMerged (Cls cls1, Cls cls2) {
   	// arguments are classes that have just been merged
    // we analyze the classes that reference cls1 and cls2
    // and we analyze their superclasses
    if (PromptTab.merging () || PromptTab.mapping()) {
   		new TriggerClsesWithTheSameReferences (cls1, cls2);
        new ConsiderMergingSuperclasses (cls1, cls2);
    }
  }

  public static void classCopied (Cls cls) {
  	if (PromptTab.merging () || PromptTab.mapping()) {
     	new ConsiderMergingSlots (cls, true);
    }
  }

  public static void mergeClsesWithTheSameReferences (Cls cls1, Cls cls2) {
   // cls1 and cls2 are in the source ontologies
	if (PromptTab.merging () || PromptTab.mapping())  {
   		if (KnowledgeBaseInMerging.isInTarget(cls1) ||
       		KnowledgeBaseInMerging.isInTarget(cls2))
       		return;

  		new MergeClsesWithTheSameReferences (cls1, cls2);
   	}
  }
}
