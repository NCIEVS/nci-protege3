 /*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu,
 *                 Michel Klein michel.klein@cs.vu.nl
 */

package edu.stanford.smi.protegex.prompt.promptDiff;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.*;
//import edu.stanford.smi.protege.storage.clips.*;

public class ChangeEnricher {
	private ResultTable _results;

	public ChangeEnricher(ResultTable _result) {
		_results = _result;
	}


	public boolean enrichChanges() {
		boolean _changesMade = false;

    	Iterator rows = _results.getRowsWithOperation(TableRow.OPERATION_MAP).iterator();
        while (rows.hasNext()) {
            TableRow row = (TableRow)rows.next();

            if (findRowBasedChanges (row))
            	_changesMade = true;
        }
        return _changesMade;
	}


	private boolean findRowBasedChanges(TableRow row) {
		boolean changed = false;

		Collection operations = row.getOperationExplanation();
		Iterator i = operations.iterator();
	    while (i.hasNext()) {
	    	FrameDifferenceElement diff = (FrameDifferenceElement)i.next();
	    	if (findRangeChange(row, diff))
	    		changed = true;
	    	if (findSlotRestrictionChange(row, diff))
	    		changed = true;
	    	if (findSuperclassChange(row, diff))
	    		changed = true;
		}
		return changed;
	}

	/* IF   Type = Slot
	 * AND  relation = own_slot_value
	 * AND  slot = :VALUE-TYPE
	 * THEN relation = range 	*/
	public boolean findRangeChange (TableRow row, FrameDifferenceElement diff) {
		if ((row.getF1Value() instanceof Slot) &&
			(diff.getRelationshipToFrame() == FrameDifferenceElement.OWN_SLOT_VALUE) &&
			(diff.getSlotValue() != null) &&
			(diff.getSlotValue().getName().equals(":SLOT-VALUE-TYPE"))) {
				diff.setRelationshipToFrame(FrameDifferenceElement.RANGE);
				diff.setSlotValue(null);
				return true;
		}
		return false;
	}


	/*
	 * IF   Type = Cls
	 * AND  relation = facet OR relation = facet-value
	 * AND  facet = :VALUE-TYPE
	 * THEN allValuesFrom */
	public boolean findSlotRestrictionChange (TableRow row, FrameDifferenceElement diff) {
		if ((row.getF1Value() instanceof Cls) &&
		    //((diff.getRelationshipToFrame() == FrameDifferenceElement.FACET) ||
			 ((diff.getRelationshipToFrame() == FrameDifferenceElement.FACET_VALUE)) &&
			(diff.getFacetValue() != null) &&
			(diff.getFacetValue().getName().equals(":VALUE-TYPE"))) {
				diff.setRelationshipToFrame(FrameDifferenceElement.SLOT_RESTRICTION_VALUE);
				diff.setFacetValue(null);
				return true;
		}
		return false;
	}


	public boolean findSuperclassChange (TableRow row, FrameDifferenceElement diff) {
		boolean changed = false;

		if (row.getF1Value() instanceof Frame) {
        	Object o1 = diff.getO1Value();
        	Object o2 = diff.getO2Value();

        	if (o1 instanceof Cls && o2 instanceof Cls) {
   				Frame o2source = _results.getSoleSource((Frame)o2);
	    		if (o2source != null) {
	        		if (((Cls)o2source).hasSuperclass((Cls)o1)) {
						diff.setOperation(FrameDifferenceElement.OP_CHANGED_SUB);
	        			changed = true;
	        		} else if (((Cls)o1).hasSuperclass((Cls)o2source)) {
						diff.setOperation(FrameDifferenceElement.OP_CHANGED_SUPER);
	        			changed = true;
	        		}
	        	}
	            Frame o1image = _results.getSoleImage((Frame)o1);
	       		if (o1image != null) {
		            if (((Cls)o1image).hasSuperclass((Cls)o2)) {
						diff.setOperation(FrameDifferenceElement.OP_CHANGED_SUPER);
	        			changed = true;
	       			} else if (((Cls)o2).hasSuperclass((Cls)o1image)) {
						diff.setOperation(FrameDifferenceElement.OP_CHANGED_SUB);
	        			changed = true;
	       			}
	            }
        	}
        }
        return changed;
    }

}