/*
 * Author(s): Natasha Noy (noy@smi.stanford.edu)
  * 
*/
package edu.stanford.smi.protegex.prompt.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLAllValuesFrom;
import edu.stanford.smi.protegex.owl.model.OWLAnonymousClass;
import edu.stanford.smi.protegex.owl.model.OWLCardinalityBase;
import edu.stanford.smi.protegex.owl.model.OWLHasValue;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNAryLogicalClass;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLRestriction;
import edu.stanford.smi.protegex.owl.model.OWLSomeValuesFrom;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.impl.AbstractRDFSClass;
import edu.stanford.smi.protegex.owl.model.impl.DefaultRDFList;
import edu.stanford.smi.protegex.owl.model.impl.OWLSystemFrames;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.TraversalDirective;
import edu.stanford.smi.protegex.prompt.operation.KeepClsOperation;
import edu.stanford.smi.protegex.prompt.promptDiff.ChangeAnalyzer;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.FrameDifferenceElement;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.ResultTable;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.TableRow;

public class OWLUtil {

	private static final String OWL_ANONYMOUS_CLS_NAME = ":OWL-ANONYMOUS-ROOT";
	public static boolean isOWLAnonymousClassFrame (Frame frame) {
		return (frame instanceof OWLAnonymousClass);
	}
	
	public static boolean isOWLAnonymousIndividual (Frame frame) {
		return (!(frame instanceof Cls) && frame instanceof RDFResource && ((RDFResource)frame).isAnonymous());
	}
	
	public static boolean isOWLAnonymousFrame (Frame frame) {
		if (!(frame instanceof RDFResource)) return false;
		return ((RDFResource)frame).isAnonymous();
	}

	public static Cls getNamedClsMetaCls (KnowledgeBase kb) {			
		OWLModel owlkb = (OWLModel)kb;
		return owlkb.getOWLNamedClassClass();
	}
	
	public static Cls getPropertyMetaSlot (KnowledgeBase kb) {			
		OWLModel owlkb = (OWLModel)kb;
		return owlkb.getOWLDatatypePropertyClass();
	}
	
	public static Cls getRootCls (KnowledgeBase kb) {
		OWLModel owlkb = (OWLModel)kb;
		return owlkb.getOWLThingClass();
	}

	public static Collection removeAnonymousClasses (Collection c) {
		if (c == null || c.isEmpty()) return c;
		Object first = CollectionUtilities.getFirstItem (c);
		if (first instanceof Frame)
			return removeAnonymousClasses (c, ((Frame)first).getKnowledgeBase());	
		else
			return c;
	}

	private static Collection  removeAnonymousClasses (Collection c, KnowledgeBase kb) {
		if (PromptTab.kbInOWL()) {
			Collection result = new ArrayList (c);
			Iterator i = result.iterator();
			while (i.hasNext()) {
				Object next = i.next();
				if (next instanceof Cls && isOWLAnonymousClassFrame((Cls)next))
					i.remove();
			}
			return result;
		} else
			return c;
	}
	
	public static Collection  removeAnonymousAndEquivalentSubclasses (Collection c, Cls cls, KnowledgeBase kb) {
		if (PromptTab.kbInOWL()) {
			Collection result = new ArrayList (c);
			Iterator i = result.iterator();
			while (i.hasNext()) {
				Object next = i.next();
				if (next instanceof Cls && isOWLAnonymousClassFrame((Cls)next))
					i.remove();
				else if (next instanceof RDFSClass && cls instanceof RDFSClass && ((RDFSClass)cls).hasEquivalentClass((RDFSClass)next))
					i.remove();
				else if (next instanceof Cls && !((Cls)next).isVisible())
					i.remove();
			}
			return result;
		} else
			return c;
	}

	
	private static OWLModel _oldKb = null;
	private static OWLModel _newKb = null;
	private static RDFSClass _oldCls = null;
	private static RDFSClass _newCls = null;

	public static void copyRemainingAnonymousClasses (Cls oldCls, Cls newCls) {
//Log.enter (OWLUtil.class, "createCopyLevelOnlyDirective", oldCls, newCls);
		if (!PromptTab.kbInOWL()) return;
		_oldKb = (OWLModel)oldCls.getKnowledgeBase();
		_newKb = (OWLModel)newCls.getKnowledgeBase();
		_oldCls = (RDFSClass)oldCls;
		_newCls = (RDFSClass)newCls;
		copyRemainingAnonymousSuperclasses	();
		copyEquivalentAnonymousClasses ();	
	}
	
	private static void copyRemainingAnonymousSuperclasses () {
		Collection oldDependingClses = getAnonymousSuperclasses(_oldCls);
		if (oldDependingClses == null || oldDependingClses.isEmpty()) return;
		
		Collection newDependingClses = getAnonymousSuperclasses(_newCls);
		Collection newDependingClsesNames = Util.getBrowserNamesFromCollection (newDependingClses);
//Log.trace ("oldDependingClses: " + oldDependingClses, OWLUtil.class, "copyRemainingAnonymousSuperclasses");
//Log.trace ("newDependingClsesNames: " + newDependingClsesNames, OWLUtil.class, "copyRemainingAnonymousSuperclasses");
		
		Iterator i = oldDependingClses.iterator();
		while (i.hasNext()) {
			Frame next = (Frame)i.next();
			String nextName = next.getBrowserText();
			if (newDependingClsesNames!= null && newDependingClsesNames.contains(nextName)) continue;
			(new KeepClsOperation (next, TraversalDirective.createCopyLevelOnlyDirective (next))).performOperation();
		}
	}
	
	public static Collection getAnonymousSuperclasses (Cls cls) {
		Collection superclasses = cls.getDirectSuperclasses();
		Collection result = new ArrayList();
		Iterator i = superclasses.iterator(); 
		while (i.hasNext()) {
			Cls next = (Cls)i.next();
			if (isOWLAnonymousClassFrame(next))
				result.add (next);
		}
		return result;
	}
	
	private static void copyEquivalentAnonymousClasses () {
		
	}

	public static boolean isEquivalent(RDFSClass cls1, RDFSClass cls2){
		if(cls1.getEquivalentClasses().contains(cls2)){
			return true;
		}
		return false;
	}

	public static String getChangeDescription(KnowledgeBase kb, TableRow row,FrameDifferenceElement diff) {
		if(PromptTab.kbInOWL()){
			String relationship = diff.getRelationshipToFrame();
			if(relationship == FrameDifferenceElement.FACET || 
			   relationship == FrameDifferenceElement.FACET_VALUE){
			   	
				relationship = "Restriction";
			   				   	
			   }
			else if(relationship == FrameDifferenceElement.OWN_SLOT || 
					relationship == FrameDifferenceElement.TEMPLATE_SLOT){
						relationship = "Property";
					}
			else if(relationship == FrameDifferenceElement.OWN_SLOT_VALUE ||
				relationship == FrameDifferenceElement.TEMPLATE_SLOT_VALUE){
					relationship = "Property Value";
				}else if(relationship == FrameDifferenceElement.SUPERCLASS){
					RDFSClass newCls = (RDFSClass)row.getF2Value();
					RDFSClass oldCls = (RDFSClass)row.getF1Value();
					RDFSClass newSuper = (diff.getO2Value() instanceof RDFSClass) ? (RDFSClass)diff.getO2Value() : null;
					RDFSClass oldSuper = (diff.getO1Value() instanceof RDFSClass) ? (RDFSClass)diff.getO1Value() : null;
					
					if(newSuper != null){
						if(isEquivalent(newCls,newSuper)){
							relationship = "Sufficient Condition";
						}else{
							relationship = "Necessary Condition";
						}						
					}else{
						if(isEquivalent(oldCls,oldSuper)){
							relationship = "Sufficient Condition";
						}else{
							relationship = "Necessary Condition";
						}							
					}
				}
				
			return relationship + " " + diff.getOperationName();
		}
		return diff.getChangeDescription();
	}

	private static Set _systemFrames = null;
	public static Set getOWLSystemFrames(KnowledgeBase kb) {
		_systemFrames = new HashSet(((OWLModel)kb).getOWLSystemResources());
		return _systemFrames;
	}

	public static List<OWLIndividual> anonymousIndividualsFromCollection (Collection<Frame> c, KnowledgeBase kb) {
		List<OWLIndividual> result = new LinkedList<OWLIndividual> ();
		for (Frame frame : c) {
			if (frame instanceof OWLIndividual && ((OWLIndividual) frame).isAnonymous()) {
				result.add((OWLIndividual) frame);
			}
		}
		return result; 
	}


	public static LinkedList<OWLAnonymousClass> anonymousClassesFromCollection (Collection<?> c, KnowledgeBase kb) {
		LinkedList<OWLAnonymousClass> result = new LinkedList<OWLAnonymousClass> ();
		Iterator<?> i = c.iterator();
		while (i.hasNext()) {
			Instance next = (Instance)i.next();
			if (OWLUtil.isOWLAnonymousClassFrame(next))
				result.addLast((OWLAnonymousClass) next);
		}
		return result; 
	}

	public static Slot getPropertyForRestriction(Cls cls) {
		if (cls instanceof OWLRestriction)
			return ((OWLRestriction)cls).getOnProperty();
		else
			return null;
	}

	public static boolean sameRestrictionType(Cls cls1, Cls cls2) {
		if (cls1.equals (cls2)) return true;
		if (cls1 instanceof OWLCardinalityBase && cls2 instanceof OWLCardinalityBase) return true;
		if (cls1 instanceof OWLHasValue && cls2 instanceof OWLHasValue) return true;
		if (cls1 instanceof OWLSomeValuesFrom && cls2 instanceof OWLSomeValuesFrom) return true;
		if (cls1 instanceof OWLAllValuesFrom && cls2 instanceof OWLAllValuesFrom) return true;
		return false;
	}

	public static boolean sameProperty(Cls cls1, Cls cls2) {
		if (!(cls1 instanceof OWLRestriction)) return false;
		if (!(cls2 instanceof OWLRestriction)) return false;
		Slot slot1 = ((OWLRestriction)cls1).getOnProperty();
		Slot slot2 = ((OWLRestriction)cls2).getOnProperty();
		if (slot1 == null || slot2 == null) return false;
		return (slot1.equals (slot2));		
	}

	public static boolean isRestrictionImage(Cls sourceCls, Cls imageCls, ResultTable results) {
		if (!(sourceCls instanceof OWLRestriction)) return false;
		if (!(imageCls instanceof OWLRestriction)) return false;
		Slot slot1 = ((OWLRestriction)sourceCls).getOnProperty();
		Slot slot2 = ((OWLRestriction)imageCls).getOnProperty();
		Slot slot1Image = (Slot)results.getSoleImage(slot1);
		if (slot1Image != null && slot1Image.equals (slot2)) return true;
		return false;
	}
	
	
	
	// given two named classes, returns true and adds explanatinos to collection
	public static boolean  compareRestrictions (Cls cls1, Cls cls2) {
		//compare direct types
		if (isOWLAnonymousClassFrame(cls1) || isOWLAnonymousClassFrame (cls2)) return true;
		if (Util.isSystem(cls1)) return true;
		
		//get restrictions, see if they are images of each other. if not, add them
try {		

		RDFSClass owlCls1 = (RDFSClass)cls1;
		RDFSClass owlCls2 = (RDFSClass)cls2;
		
		Collection restr1 = new ArrayList (owlCls1.getSuperclasses(false));
		restr1.removeAll(owlCls1.getNamedSuperclasses());
		Collection restr2 = new ArrayList (owlCls2.getSuperclasses(false));
		restr2.removeAll(owlCls2.getNamedSuperclasses());
		return ChangeAnalyzer.compareCollections  (restr1, restr2, FrameDifferenceElement.RESTRICTION);

} catch (Exception e) {
	Log.getLogger().severe ("exception = " + e);
	Log.getLogger().severe ("cls1 = " + cls1 + ", class = " + cls1.getClass());
	return false;
	
	}	
	}

	public static void acceptRestrictionChange(Cls cls, FrameDifferenceElement diffEl) {
		// XXX Auto-generated method stub
		//probably do nothing....
	}

	public static void rejectRestrictionChange(Cls cls, TableRow row, FrameDifferenceElement diffEl) {
		RDFSClass newCls = (RDFSClass)row.getF2Value();
		RDFSClass oldCls = (RDFSClass)row.getF1Value();
		RDFSClass newSuper = (diffEl.getO2Value() instanceof RDFSClass) ? (RDFSClass)diffEl.getO2Value() : null;
		RDFSClass oldSuper = (diffEl.getO1Value() instanceof RDFSClass) ? (RDFSClass)diffEl.getO1Value() : null;
		switch(diffEl.getOperation()){
			case FrameDifferenceElement.OP_ADDED :
			{
				boolean isEquivalent = OWLUtil.isEquivalent(newSuper,newCls);					
				newCls.removeSuperclass(newSuper);
				if(isEquivalent){
					newSuper.removeSuperclass(newCls);
				}
				break;
			}
				
			case FrameDifferenceElement.OP_CHANGED :
			{
				boolean isEquivalent = OWLUtil.isEquivalent(newSuper,newCls);					
				newCls.removeSuperclass(newSuper);
				if(isEquivalent){
					newSuper.removeSuperclass(newCls);
				}
			}
			case FrameDifferenceElement.OP_DELETED :
			{
				boolean isEquivalent = OWLUtil.isEquivalent(oldSuper,oldCls);					
				String expression = oldSuper.getBrowserText();
				OWLModel okb = newCls.getOWLModel();
				RDFSClass newExpr = (RDFSClass)edu.stanford.smi.protegex.owl.model.impl.OWLUtil.createClone(okb,expression);
				if(newExpr == null)
					return;
						
				newCls.addSuperclass(newExpr);
				if(isEquivalent)
					newExpr.addSuperclass(newCls);
					
				break;
			}
		}
	}

    // TODO: This could be deleted, because after 2.0 any OWL frame also has isSystem() true
	public static boolean isOWLSystemFrame(Frame f) {
		return f.isSystem();
	}

	//slots not to compare in ChangeAnalyzer
	public static Collection<Slot> systemSlotsToIgnore(OWLModel owlModel) {
	    OWLSystemFrames frames = owlModel.getSystemFrames();
		Collection<Slot> result = new ArrayList<Slot>();
		result.add (frames.getRdfsDomainProperty());
		result.add(frames.getRdfsRangeProperty());
		result.add(frames.getRdfsSubClassOfProperty());
		result.add(frames.getRdfTypeProperty());
		result.add(frames.getOwlOntologyPrefixesProperty());
		return result;
	}

	public static boolean isRDFList(Frame f) {
		return (f instanceof DefaultRDFList);
	}

	public static boolean displayFrameInDiffTable(Frame f) {
		return !isRDFList(f);
	}
	
	public static void compareAnonymousClasses (TableRow row) {
		Frame f1 = row.getF1Value();
		Frame f2 = row.getF2Value();
		if (f1.getBrowserText().equals(f2.getBrowserText())  || sameOperandsForOWLNAryLogicalClass (f1, f2)) {
			row.setMappingLevel (TableRow.MAPPING_LEVEL_UNCHANGED);
		} else {
			row.setMappingLevel (TableRow.MAPPING_LEVEL_DIRECTLY_CHANGED);
		}
	}

	public static void compareAnonymousIndividuals (TableRow row) {
		Frame f1 = row.getF1Value();
		Frame f2 = row.getF2Value();
//		if (f1.getBrowserText().equals(f2.getBrowserText())  || sameOperandsForOWLNAryLogicalClass (f1, f2)) {
			row.setMappingLevel (TableRow.MAPPING_LEVEL_UNCHANGED);
//		} else {
//			row.setMappingLevel (TableRow.MAPPING_LEVEL_DIRECTLY_CHANGED);
//		}
	}

	public static boolean compareDefinedAndPrimitive(Cls cls1, Cls cls2) {
		if (! (cls1 instanceof OWLNamedClass && cls2 instanceof OWLNamedClass)) {
			Log.getLogger().warning("Shouldn't be here: cls1 = " + cls1 + ", instanceof " + cls1.getClass() +
					", cls2 = " + cls2 + "instance of " + cls2.getClass());
			return true;
		}
		OWLNamedClass owlCls1 = (OWLNamedClass)cls1;
		OWLNamedClass owlCls2 = (OWLNamedClass)cls2;
		boolean cls1Primitive = (owlCls1.getDefinition() == null);
		boolean cls2Primitive = (owlCls2.getDefinition() == null);
		if (cls1Primitive == cls2Primitive) return true;
		if (cls1Primitive && !cls2Primitive)
				ChangeAnalyzer.addExplanation (FrameDifferenceElement.PRIMITIVE_TO_DEFINED);
		if (!cls1Primitive && cls2Primitive)
			ChangeAnalyzer.addExplanation (FrameDifferenceElement.DEFINED_TO_PRIMITIVE);
		return false;
	}

	public static boolean isDefinedCls(Cls cls) {
		if (cls instanceof AbstractRDFSClass)
			return (((AbstractRDFSClass)cls).getDefinition() != null);
		return false;
	}

	public static boolean isIntersectionOrUnionClass(Frame f) {
		return (f instanceof OWLNAryLogicalClass);
	}

	public static boolean isOWLNamedClass(Cls cls) {
		return (cls instanceof OWLNamedClass);
	}

	public static boolean sameOperandsForOWLNAryLogicalClass(Frame cls1, Frame cls2) {
		if (!(cls1 instanceof OWLNAryLogicalClass) || ! (cls2 instanceof OWLNAryLogicalClass))
			return false;
		OWLNAryLogicalClass naryClass1 = (OWLNAryLogicalClass)cls1;
		OWLNAryLogicalClass naryClass2 = (OWLNAryLogicalClass)cls2;
		
        Set setA = new HashSet();
        for (Iterator it = naryClass1.getOperands().iterator(); it.hasNext();) {
            RDFSClass operand = (RDFSClass) it.next();
            setA.add(operand.getBrowserText());
        }
        Set setB = new HashSet();
        for (Iterator it = naryClass2.getOperands().iterator(); it.hasNext();) {
            RDFSClass operand = (RDFSClass) it.next();
            setB.add(operand.getBrowserText());
        }
        if(setA.size() == setB.size()) {
            setA.removeAll(setB);
            return setA.isEmpty();
        }
        return false;
	}
    
    /*
	
    public static void fixBrowserSlotPatterns(Project project) {
    	Collection customizedClasses = project.getClsesWithDirectBrowserSlots();
    	
    	for (Iterator iter = customizedClasses.iterator(); iter.hasNext();) {
			Cls cls = (Cls) iter.next();
		
			BrowserSlotPattern browserPattern = project.getBrowserSlotPattern(cls);
	        if (browserPattern != null && !(browserPattern instanceof OWLBrowserSlotPattern))
	        	cls.setDirectBrowserSlotPattern(new OWLBrowserSlotPattern(browserPattern.getElements()));
		}
	}
    */
} 
