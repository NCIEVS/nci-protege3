 /*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu,
 *                 Michel Klein michel.klein@cs.vu.nl
 */

package edu.stanford.smi.protegex.prompt.promptDiff.structures;

import java.util.Comparator;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.util.CompareNames;
import edu.stanford.smi.protegex.prompt.util.OWLUtil;
import edu.stanford.smi.protegex.prompt.util.Util;

/*
 * I have some reason to believe that this comparator is somehow broken. -Timothy
 */

public class TableRowComparator implements Comparator<TableRow> {
	private final int O1_LESS_THAN_O2 = -1;
	private final int O1_GREATER_THAN_O2 = 1;
	private final int O1_EQUAL_TO_O2 = 0;
	private static boolean _owl = PromptTab.kbInOWL();


    public int compare (TableRow t1, TableRow t2) {

    	if (t1.getF1Value() == null) {
    	    if (t2.getF1Value() != null)
	           	return O1_LESS_THAN_O2;
            else {
            	if (compareFrameNamesAndType(t1.getF2Value(), t2.getF2Value()) < 0)
     	        	return O1_LESS_THAN_O2;
		else
         	 	return O1_GREATER_THAN_O2;
            }
        }
    	if (t2.getF1Value() == null)
        	return O1_GREATER_THAN_O2;

    	if (t1.getF2Value() == null)
    		if (t2.getF1Value() != null)
	           	return O1_LESS_THAN_O2;
            else
            	if (compareFrameNamesAndType(t1.getF1Value(), t2.getF1Value()) < 0)
     	    		return O1_LESS_THAN_O2;
                else
         		return O1_GREATER_THAN_O2;
    	if (t2.getF2Value() == null)
        	return O1_GREATER_THAN_O2;

        if (t1.getRenameValue() == TableRow.RENAME_PLUS && t2.getRenameValue() != TableRow.RENAME_PLUS)
           	return O1_LESS_THAN_O2 ;
        if (t2.getRenameValue() == TableRow.RENAME_PLUS && t1.getRenameValue() != TableRow.RENAME_PLUS)
           	return O1_GREATER_THAN_O2;

        if (t1.getOperationValue() == null && t2.getOperationValue() != null)
        	return O1_LESS_THAN_O2;
        if (t2.getOperationValue() == null && t1.getOperationValue() != null)
        	return O1_GREATER_THAN_O2;

	if (t1.getOperationValue() != null && t2.getOperationValue() != null) {
	  if (t1.getOperationValue().equals(TableRow.OPERATION_SPLIT) &&
    	    	!t2.getOperationValue().equals(TableRow.OPERATION_SPLIT))
        	    return O1_LESS_THAN_O2;
          if (t2.getOperationValue().equals(TableRow.OPERATION_SPLIT) &&
    	    	!t1.getOperationValue().equals(TableRow.OPERATION_SPLIT))
        	    return O1_GREATER_THAN_O2;

	  if (t1.getOperationValue().equals(TableRow.OPERATION_MERGE) &&
    	    	!t2.getOperationValue().equals(TableRow.OPERATION_MERGE))
        	    return O1_LESS_THAN_O2;
          if (t2.getOperationValue().equals(TableRow.OPERATION_MERGE) &&
    	    	!t1.getOperationValue().equals(TableRow.OPERATION_MERGE))
        	    return O1_GREATER_THAN_O2;

          if (t1.getMappingLevel() != t2.getMappingLevel()) {
           if (t1.getMappingLevel().equals(TableRow.MAPPING_LEVEL_DIRECTLY_CHANGED))
             return O1_LESS_THAN_O2;
            if (t2.getMappingLevel().equals(TableRow.MAPPING_LEVEL_DIRECTLY_CHANGED))
              return O1_GREATER_THAN_O2;

            if (t1.getMappingLevel().equals(TableRow.MAPPING_LEVEL_CHANGED))
              return O1_LESS_THAN_O2;
            if (t2.getMappingLevel().equals(TableRow.MAPPING_LEVEL_CHANGED))
              return O1_GREATER_THAN_O2;

            if (t1.getMappingLevel().equals(TableRow.MAPPING_LEVEL_UNCHANGED))
              return O1_GREATER_THAN_O2;
            if (t2.getMappingLevel().equals(TableRow.MAPPING_LEVEL_UNCHANGED))
              return O1_LESS_THAN_O2;
          }

        }

        if (Util.isSystem(t1.getF1Value()) && !Util.isSystem(t2.getF1Value()))
        	return O1_GREATER_THAN_O2;
        if (Util.isSystem(t2.getF1Value()) && !Util.isSystem(t1.getF1Value()))
        	return O1_LESS_THAN_O2;

        if (compareFrameNamesAndType(t1.getF1Value(), t2.getF1Value()) < 0)
            return O1_LESS_THAN_O2;
        else
           	return O1_GREATER_THAN_O2;
    	}


      private static int compareFrameNamesAndType (Frame f1, Frame f2) {
//Log.enter(TableRowComparator.class, "compareFrameNamesAndType", f1, f2);
        int result;

//        if (f1 instanceof Cls && ((Cls)f1).isMetaCls() &&
//            !(f2 instanceof Cls && ((Cls)f2).isMetaCls())) result = -2;
//        else if (f2 instanceof Cls && ((Cls)f2).isMetaCls() &&
//            !(f1 instanceof Cls && ((Cls)f1).isMetaCls())) result = 2;

        //else 
        	if (f1 instanceof Cls && !(f2 instanceof Cls)) result  = -2;
        else if (f2 instanceof Cls && !(f1 instanceof Cls)) result  = 2;

        else if (f1 instanceof Slot && !(f2 instanceof Slot)) result =  -2;
        else if (f2 instanceof Slot && !(f1 instanceof Slot)) result =  2;

        else if (f1 instanceof Facet && !(f2 instanceof Facet)) result =  -2;
        else if (f2 instanceof Facet && !(f1 instanceof Facet)) result =  2;
        //f1 is an instance
//        else if (f2 instanceof Cls || f2 instanceof Slot || f2 instanceof Facet) result = 2;
        else if (_owl && OWLUtil.isOWLAnonymousClassFrame(f1) && OWLUtil.isOWLAnonymousClassFrame(f2))
        	result = 0;
        else if (_owl && OWLUtil.isRDFList(f1) && OWLUtil.isRDFList(f2))
        	result = 0;
        else 
        	result = CompareNames.compareNames(Util.getLocalBrowserText (f1), Util.getLocalBrowserText(f2));
        return result;
      }

    }


