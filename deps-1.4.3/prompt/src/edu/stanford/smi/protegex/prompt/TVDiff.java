/*
 * Author(s): Natasha Noy (noy@smi.stanford.edu)
  * 
*/
package edu.stanford.smi.protegex.prompt;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.promptDiff.*;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class TVDiff {
	private static KnowledgeBase _kbOld = null;
	private static KnowledgeBase _kbNew = null;
	private static KnowledgeBase _kbTV = null;
	
	private static String _vold = null;
	private static String _vnew = null;
	private static String _tv = null;
	
	private static ResultTable _diffTable = null;
	private static TraversalDirectivesKnowledgeBase _tdKb = null;

	public static boolean viewDirty (String vold, String vnew, String tv){
		boolean result = false;
		openAndCompareKbs (vold, vnew, tv);
		Cls traversalDirectivesCls = _tdKb.getTraversalDirectiveCls();
		Collection tds = new ArrayList (traversalDirectivesCls.getDirectInstances());
		Iterator i = tds.iterator();
		while (i.hasNext()) {
			Instance next = (Instance)i.next();
			if (tdDirty (next)) 
				result = true;
		}
		System.out.print ("View is " + (result ? "dirty." : "clean."));
		if (result) {
			System.out.println (" Save the updated view definitions? [y/n]");
			try {
				int ch = System.in.read();
				if (ch == 'y')
					_tdKb.save();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
			
		System.out.flush();
		return result;
	}
	
	//for each frame in the view, check that it has an image and there is no rename; otherwise dirty
	private static boolean tdDirty (Instance td) {
		boolean result = false;
		Frame starterConcept = _tdKb.getStarterConcept (td);
		Collection imageConcept = new ArrayList ();
		if (frameReferenceDirty (starterConcept, imageConcept)) {
			result = true;
			if (imageConcept.size() == 0) {
				System.out.println ("Starter concept " + starterConcept + " in directive " + td + " was deleted");
				_tdKb.removeTraversalDirective (td);
				return true;
			}
			else {
				System.out.println ("Starter concept " + starterConcept + " in directive " + td + " was changed to " + CollectionUtilities.getSoleItem(imageConcept));
				_tdKb.setStarterConcept (td, ((Frame)CollectionUtilities.getSoleItem(imageConcept)).getName());
			}
		}
		Collection slotDirectives = _tdKb.getSlotDirectives (td);
		Iterator i = slotDirectives.iterator();
		while (i.hasNext()) {
			Instance next = (Instance)i.next();
			Slot nextSlot = _tdKb.getSlotInSlotDirective (next);
			if (frameReferenceDirty (nextSlot, imageConcept)) {
				result = true;
				if (imageConcept.size() == 0) {
					System.out.println ("Slot " + nextSlot + " in directive " + td + " was deleted");
					_tdKb.removeSlotDirective (next, td);
				}
				else {
					System.out.println ("Slot " + nextSlot + " in directive " + td + " was changed to " + CollectionUtilities.getSoleItem(imageConcept));
					_tdKb.setSlotInSlotDirective (next, ((Frame)CollectionUtilities.getSoleItem(imageConcept)).getName());
				}
			}
		}
		return result;	
	}
	
	//for each frame in the view, check that it has an image and there is no rename; otherwise dirty
	// if there is a rename, return the new name in the second argument
	private static boolean frameReferenceDirty (Frame f, Collection newFrame) {
		newFrame.clear();
		Collection rows = _diffTable.getRows(f);
		Iterator i = rows.iterator();
		while (i.hasNext()) {
			TableRow next = (TableRow)i.next();
			if (next.getOperationValue() == TableRow.OPERATION_MAP) {
				if (next.getRenameValue() == TableRow.RENAME_MINUS)
					return false;
				else {
					newFrame.add (next.getF2Value());
				}					
			}
		}
		return true;
	}

	public static boolean viewChanged (String vold, String vnew, String tv){
		openAndCompareKbs (vold, vnew, tv);
		
		return false;
	}
	
	private static void openAndCompareKbs (String vold, String vnew, String tv) {
		if (!vold.equals(_vold)) {
			Collection errors = new ArrayList();
			Project projectOld = new Project(vold, errors);
			Util.displayErrors (errors);
			if (errors.size() == 0) {
				_kbOld = projectOld.getKnowledgeBase();
				_vold = vold;	
				_diffTable = null;		
			}
		}
		if (!vnew.equals(_vnew)){
			Collection errors = new ArrayList();
			Project projectNew = new Project(vnew, errors);
			Util.displayErrors (errors);
			if (errors.size() == 0) {
				_kbNew = projectNew.getKnowledgeBase();
				_vnew = vnew;	
				_diffTable = null;		
			}
		}
		if (!tv.equals(_tv)){
			Collection errors = new ArrayList();
			Project projectTV = new Project(tv, errors);
			Util.displayErrors (errors);
			if (errors.size() == 0) {
				_kbTV = projectTV.getKnowledgeBase();
				_tv = tv;			
				_tdKb = new TraversalDirectivesKnowledgeBase (projectTV, _kbOld);
			}
		}
		PromptDiff promptDiff = new PromptDiff();
		if (_diffTable == null) promptDiff.runDiff (_kbOld, _kbNew);
		_diffTable = promptDiff.getResultsTable();
	}

}
