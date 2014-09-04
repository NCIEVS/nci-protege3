/*
 * Author(s): Natasha Noy (noy@smi.stanford.edu)
  * 
*/
package edu.stanford.smi.protegex.prompt.promptDiff;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLAnonymousClass;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLNames;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFSNames;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.event.PromptAdapter;
import edu.stanford.smi.protegex.prompt.event.PromptEvent;
import edu.stanford.smi.protegex.prompt.event.PromptListenerManager;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.ResultTable;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.TableRow;

public class CompareOWLAnonymousClasses implements DiffAlgorithm {
    private static final  Logger  log = Log.getLogger(CompareOWLAnonymousClasses.class);
    
	public boolean usesClassImageInformationInTable () {return true;}
	public boolean usesSlotImageInformationInTable () {return false;}
	public boolean usesFacetImageInformationInTable () {return false;}
	public boolean usesInstanceImageInformationInTable () {return true;}

	public boolean usesClassOperationInformationInTable () {return false;}
	public boolean usesSlotOperationInformationInTable () {return false;}
	public boolean usesFacetOperationInformationInTable () {return false;}
	public boolean usesInstanceOperationInformationInTable () {return false;}

	public boolean modifiesClassImageInformationInTable () {return true;}
	public boolean modifiesSlotImageInformationInTable () {return false;}
	public boolean modifiesFacetImageInformationInTable () {return false;}
	public boolean modifiesInstanceImageInformationInTable () {return true;}

	public boolean modifiesClassOperationInformationInTable () {return false;}
	public boolean modifiesSlotOperationInformationInTable () {return false;}
	public boolean modifiesFacetOperationInformationInTable () {return false;}
	public boolean modifiesInstanceOperationInformationInTable () {return false;}

//-----------------------------------------------------------------------------

	private static ResultTable resultsTable;
	
	private static OWLModel kb1 = null;
	private static OWLModel kb2 = null;
	
	private static Set<OWLNamedClass> unexaminedClasses = null;
	static {
	    PromptListenerManager.addDiffListener(new  PromptAdapter() {
	        @Override
	        public void initializationDone(PromptEvent event) {
	            unexaminedClasses = null;
	        }
	        
	        @Override
	        public void taskComplete(PromptEvent event, boolean interrupted) {
	            unexaminedClasses = null;
	        }
	    });
	}
	
	private static boolean changesMade = false;

	public static boolean run (ResultTable table, PromptDiff promptDiff) {
		if (!sourceKbsInOwl(promptDiff.getKb1(), promptDiff.getKb2())) return false;
		Log.getLogger().info ("*********** start CompareOWLAnonymousClasses ************");
		if (log.isLoggable(Level.FINE)) {
		    log.fine("Starting at: " + new Date());
		}			
		resultsTable = table;
	
		if (unexaminedClasses == null) {
		    unexaminedClasses = new HashSet<OWLNamedClass>();
		    for (TableRow row : resultsTable.values()) {
		        if (row.getF1Value() instanceof OWLNamedClass) {
		            unexaminedClasses.add((OWLNamedClass) row.getF1Value());
		        }
		    }
		}
		
		changesMade = false;
		resultsTable.traceOn(false);

		findMatchingFrames();
		
		if (log.isLoggable(Level.FINE)) {
		    log.fine("Done at: " + new Date());
		}
		return changesMade;
	}
	
	private static boolean sourceKbsInOwl (KnowledgeBase kb1, KnowledgeBase kb2) {
	    CompareOWLAnonymousClasses.kb1 =  (OWLModel) kb1;
	    CompareOWLAnonymousClasses.kb2 =  (OWLModel) kb2;
		return (PromptTab.kbInOWL());
	}

	private static void findMatchingFrames() {
	    Set<OWLNamedClass> examinedClasses  = new HashSet<OWLNamedClass>();
	    for (OWLNamedClass cls1 : unexaminedClasses) {
	        if (cls1.isSystem()) {
	            continue;
	        }
	        boolean classHasMatch = false;
	        for (Frame frame2 : resultsTable.getImages(cls1)) {
	            if (frame2 instanceof OWLNamedClass) {
	                classHasMatch = true;
	                OWLNamedClass cls2 = (OWLNamedClass) frame2;
	                if (findMatchingFrames(cls1, cls2)) {
	                    break;
	                }
	            }
	        }
	        if (classHasMatch) {
	            examinedClasses.add(cls1);
	        }
	    }
	    unexaminedClasses.removeAll(examinedClasses);
	}
	
	private static boolean findMatchingFrames(OWLNamedClass cls1, OWLNamedClass cls2) {
	    boolean found = false;
	    if (findMatchingFrames(cls1, cls2, RDFSNames.Slot.SUB_CLASS_OF)) {
	        found = true;
	    }
	    if (findMatchingFrames(cls1, cls2, OWLNames.Slot.EQUIVALENT_CLASS)) {
	        found = true;
	    }
	    if (found) {
	        changesMade = true;
	    }
	    return found;
	}

	private static boolean findMatchingFrames(OWLNamedClass cls1, OWLNamedClass cls2, String propertyName) {
	    boolean found = false;    
	    Map<String, OWLAnonymousClass> map = makeBrowserTextToClassMap(cls2, propertyName);
	    RDFProperty  p1 = cls1.getOWLModel().getRDFProperty(propertyName);
	    for (Object o : cls1.getPropertyValues(p1)) {
	        OWLAnonymousClass match;
	        if (o instanceof OWLAnonymousClass &&
	                (match = map.get(((OWLAnonymousClass) o).getBrowserText())) != null) {
	            OWLAnonymousClass classExpr = (OWLAnonymousClass) o;
	            AlgorithmUtils.createNewMatch(classExpr, match, "Classes are identical, anonymous and attached to the same class", resultsTable);
	            clearReferencedAnonymousClasses(classExpr);
	            clearReferencedAnonymousClasses(match);
	            found = true;
	        }
	    }
	    return found;
	}
	
	private static Map<String, OWLAnonymousClass> makeBrowserTextToClassMap(OWLNamedClass cls, String propertyName) {
	    RDFProperty  p = cls.getOWLModel().getRDFProperty(propertyName);
	    Map<String, OWLAnonymousClass> map = new HashMap<String, OWLAnonymousClass>();
	       for (Object o : cls.getPropertyValues(p)) {
	            if (o instanceof OWLAnonymousClass) {
	                OWLAnonymousClass classExpr = (OWLAnonymousClass) o;
	                map.put(classExpr.getBrowserText(), classExpr);
	            }
	        }
	    return map;
	}
	
	private static void clearReferencedAnonymousClasses(OWLAnonymousClass classExpr) {
	    for (Object o : classExpr.getDependingClasses()) {
	        if (o instanceof OWLAnonymousClass && !o.equals(classExpr)) {
	            OWLAnonymousClass dependent = (OWLAnonymousClass) o;
	            
	            for (TableRow row : resultsTable.getRows(dependent)) {
	                resultsTable.removeElement(row);
	            }
	        }
	    }
	}
	
}
