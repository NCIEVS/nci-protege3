
/*
 * Contributor(s): Abhita Chugh abhita@stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui.diffUI;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class AcceptChangesKnowledgeBase {
	private Cls _acceptChangesCls = null;
	private Slot _classOneSlot = null;
	private Slot _classTwoSlot = null;
	private Slot _acceptSlotSlot = null;
	private Slot _acceptInstanceSlot = null;
	private Slot _typeOfCallSlot = null;
	private Slot _objectOneSlot = null;
	private Slot _objectTwoSlot = null;
	private Slot _acceptFacetSlot = null;
	private Slot _refersFrameSlot = null;
	
	
	private KnowledgeBase _acceptChangesKB = null;
	private Project _project = null;
	
	private static KnowledgeBase _Kb1 = null;
	private static KnowledgeBase _Kb2 = null;	
	
	public static final int ACCEPT_CLS = 1;
	public static final int ACCEPT_CHANGE_IN_CLS = 2;
	public static final int ACCEPT_TREE = 3;
	public static final int ACCEPT_SLOT = 4;
	public static final int ACCEPT_INSTANCE = 5;
	public static final int ACCEPT_CHANGE_IN_INSTANCE = 6;
	
	public AcceptChangesKnowledgeBase () {
		_Kb1 = ProjectsAndKnowledgeBases.getKnowledgeBase(ProjectsAndKnowledgeBases.OLD_VERSION_INDEX);
		_Kb2 = ProjectsAndKnowledgeBases.getKnowledgeBase(ProjectsAndKnowledgeBases.NEW_VERSION_INDEX);
		
		_project = Util.createNewClipsProject ("-accepts");
		
		Collection errors = new ArrayList();
		Log.getLogger().info("url: " + Util.getProjectURI ("accepts.pprj"));
		_project.includeProject(Util.getProjectURI ("accepts.pprj"), errors);
		_project.mergeIncludedProjects();
		
		_acceptChangesKB = _project.getKnowledgeBase();
		initializeVariables ();
	}
	
	public AcceptChangesKnowledgeBase (Project project) {
		
		_Kb1 = ProjectsAndKnowledgeBases.getKnowledgeBase(ProjectsAndKnowledgeBases.OLD_VERSION_INDEX);
		_Kb2 = ProjectsAndKnowledgeBases.getKnowledgeBase(ProjectsAndKnowledgeBases.NEW_VERSION_INDEX);
		
		_project = project;
		_acceptChangesKB = project.getKnowledgeBase();
		initializeVariables ();
	}
	
	public void save () {
		Collection errors = new ArrayList();
		_project.save(errors);
		Util.displayErrors(errors);
	}
	
	private static final String ACCEPT_CHANGES_CLS_NAME = "AcceptChanges";
	
	private static final String CLASS_ONE_SLOT_NAME = "class_one";
	private static final String CLASS_TWO_SLOT_NAME = "class_two";
	private static final String ACCEPT_SLOT_SLOT_NAME = "accept_slot";
	private static final String ACCEPT_INSTANCE_SLOT_NAME = "accept_instance";
	private static final String TYPE_OF_CALL_SLOT_NAME = "type_of_call";
	private static final String OBJECT_ONE_SLOT_NAME = "object_one";
	private static final String OBJECT_TWO_SLOT_NAME = "object_two";
	private static final String ACCEPT_FACET_SLOT_NAME = "accept_facet";
	private static final String REFERS_FRAME_SLOT_NAME = "refers_frame";
	
	private void initializeVariables () {
		_acceptChangesCls = _acceptChangesKB.getCls(ACCEPT_CHANGES_CLS_NAME);
		
		_classOneSlot = _acceptChangesKB.getSlot(CLASS_ONE_SLOT_NAME);
		_classTwoSlot = _acceptChangesKB.getSlot(CLASS_TWO_SLOT_NAME);
		_acceptSlotSlot = _acceptChangesKB.getSlot(ACCEPT_SLOT_SLOT_NAME);
		_acceptInstanceSlot = _acceptChangesKB.getSlot(ACCEPT_INSTANCE_SLOT_NAME);
		_typeOfCallSlot = _acceptChangesKB.getSlot(TYPE_OF_CALL_SLOT_NAME);
		
		_objectOneSlot = _acceptChangesKB.getSlot(OBJECT_ONE_SLOT_NAME);
		_objectTwoSlot = _acceptChangesKB.getSlot(OBJECT_TWO_SLOT_NAME);
		_acceptFacetSlot = _acceptChangesKB.getSlot(ACCEPT_FACET_SLOT_NAME);
		_refersFrameSlot = _acceptChangesKB.getSlot(REFERS_FRAME_SLOT_NAME);
		setInstancesToNotEditable ();
	}
	
	private void setInstancesToNotEditable () {
		Collection instances = _acceptChangesCls.getInstances();
		
		Iterator i = instances.iterator();
		while (i.hasNext()) {
			Instance next = (Instance)i.next();
			next.setEditable(false); 
		}
	}
	
	public KnowledgeBase getKnowledgeBase() {
		return _acceptChangesKB;
	}
	
	public Project getProject() {
		return _project;
	}
	
	public Instance addToKB (int type, String cls1, String cls2, String slot, String instance) {
		
		Instance acInstance = _acceptChangesKB.createInstance(null, _acceptChangesCls);
		acInstance.setEditable(false);
		
		acInstance.setOwnSlotValue (_classOneSlot, cls1);
		acInstance.setOwnSlotValue(_classTwoSlot, cls2);
		acInstance.setOwnSlotValue(_acceptSlotSlot, slot);
		acInstance.setOwnSlotValue(_acceptInstanceSlot, instance);
		acInstance.setOwnSlotValue(_typeOfCallSlot, new Integer (type));
		
		//acInstance.setOwnSlotValue (_objectOneSlot, null);
		//acInstance.setOwnSlotValue (_objectTwoSlot, null);
		//acInstance.setOwnSlotValue (_acceptFacetSlot, null);
		//acInstance.setOwnSlotValue (_refersFrameSlot, null);
		
		return acInstance;
	}
	
	
	public Instance addChangeToKB (int type, String cls1, String instance, String slot, String facet, Object o1, Object o2, boolean refersToFrame) {
		
		Instance acInstance = _acceptChangesKB.createInstance(null, _acceptChangesCls);
		acInstance.setEditable(false);
		
		acInstance.setOwnSlotValue (_classOneSlot, cls1);
		
		acInstance.setOwnSlotValue(_objectOneSlot, o1);
		
		acInstance.setOwnSlotValue(_objectTwoSlot, o2);
		acInstance.setOwnSlotValue(_acceptSlotSlot, slot);
		acInstance.setOwnSlotValue(_acceptFacetSlot, facet);
		acInstance.setOwnSlotValue(_acceptInstanceSlot, instance);
		acInstance.setOwnSlotValue(_typeOfCallSlot, new Integer (type));
		acInstance.setOwnSlotValue(_refersFrameSlot,new Boolean (refersToFrame));
		
		
		return acInstance;
	}
	
	
	public void executeAcceptedChanges (DiffTreeView treeView) {
		Cls cls1 = null;
		Cls cls2 = null;
		Slot slot = null;
		Instance instance = null;
		AcceptorRejector _acceptorRejector = PromptTab.getPromptDiff().getAcceptorRejector();
		Collection changes = _acceptChangesCls.getDirectInstances();
		Iterator i = changes.iterator();
		while (i.hasNext()) {
			Instance nextChange = (Instance)i.next();
			int typeOfCall = ((Integer)nextChange.getOwnSlotValue(_typeOfCallSlot)).intValue();
			if(typeOfCall == ACCEPT_CLS){
				cls1 = _Kb2.getCls((String)nextChange.getOwnSlotValue (_classOneSlot));
				//if(cls1 == null)
				//cls1 = _Kb1.getCls((String)nextChange.getOwnSlotValue (_classOneSlot));
				cls2 = _Kb2.getCls((String)nextChange.getOwnSlotValue (_classTwoSlot));
				//if(cls2 == null)
				//cls2 = _Kb1.getCls((String)nextChange.getOwnSlotValue (_classTwoSlot));
				_acceptorRejector.acceptChange(cls1,cls2);
				
			}
			if(typeOfCall == ACCEPT_TREE){
				cls1 = _Kb2.getCls((String)nextChange.getOwnSlotValue (_classOneSlot));
				//if(cls1 == null)
				//cls1 = _Kb1.getCls((String)nextChange.getOwnSlotValue (_classOneSlot));
				cls2 = _Kb2.getCls((String)nextChange.getOwnSlotValue (_classTwoSlot));
				//if(cls2 == null)
				//cls2 = _Kb1.getCls((String)nextChange.getOwnSlotValue (_classTwoSlot));
				_acceptorRejector.acceptTreeChange(cls1,cls2);
			}
			if(typeOfCall == ACCEPT_SLOT){
				cls1 = _Kb2.getCls((String)nextChange.getOwnSlotValue (_classOneSlot));
				//if(cls1 == null)
				//cls1 = _Kb1.getCls((String)nextChange.getOwnSlotValue (_classOneSlot));
				slot = _Kb2.getSlot((String)nextChange.getOwnSlotValue (_acceptSlotSlot));
				TableRow tableRow = _acceptorRejector.getSoleRow(cls1);
				FrameDifferenceElement diffElement = tableRow.getOperationExplanation(slot);
				if(diffElement == null)/*Slot has been deleted from the target KB, so need to get it from sourceKb*/
					slot = _Kb1.getSlot((String)nextChange.getOwnSlotValue (_acceptSlotSlot));	
				//if(slot == null)
				//slot = _Kb1.getSlot((String)nextChange.getOwnSlotValue (_acceptSlotSlot));
				_acceptorRejector.acceptSlotChange(cls1,slot);
			}
			if(typeOfCall == ACCEPT_INSTANCE){
				cls1 = _Kb2.getCls((String)nextChange.getOwnSlotValue (_classOneSlot));
				//if(cls1 == null)
				//cls1 = _Kb1.getCls((String)nextChange.getOwnSlotValue (_classOneSlot));
				instance = _Kb2.getInstance((String)nextChange.getOwnSlotValue (_acceptInstanceSlot));
				//if(instance == null)
				//instance = _Kb1.getInstance((String)nextChange.getOwnSlotValue (_acceptInstanceSlot));
				_acceptorRejector.acceptInstance(cls1,instance);
			}
			if(typeOfCall == ACCEPT_CHANGE_IN_CLS){
				cls1 = _Kb2.getCls((String)nextChange.getOwnSlotValue (_classOneSlot));
				
				Object o1 = nextChange.getOwnSlotValue (_objectOneSlot);
				Object o2 = nextChange.getOwnSlotValue (_objectTwoSlot);
				String dslot = (String)nextChange.getOwnSlotValue (_acceptSlotSlot);
				String dfacet = (String)nextChange.getOwnSlotValue (_acceptFacetSlot);
				boolean r = ((Boolean)nextChange.getOwnSlotValue(_refersFrameSlot)).booleanValue();
				
				TableRow tableRow = _acceptorRejector.getSoleRow(cls1);
				FrameDifferenceElement diffElement = tableRow.getDifferenceElement(o1,o2,dslot,dfacet,r);
				
				if(diffElement!=null)
					_acceptorRejector.acceptChange(cls1, diffElement);
				
				
			}
			if(typeOfCall == ACCEPT_CHANGE_IN_INSTANCE){
				cls1 = _Kb2.getCls((String)nextChange.getOwnSlotValue (_classOneSlot));
				instance = _Kb2.getInstance((String)nextChange.getOwnSlotValue (_acceptInstanceSlot));
				
				Object o1 = nextChange.getOwnSlotValue (_objectOneSlot);
				Object o2 = nextChange.getOwnSlotValue (_objectTwoSlot);
				String dslot = (String)nextChange.getOwnSlotValue (_acceptSlotSlot);
				String dfacet = (String)nextChange.getOwnSlotValue (_acceptFacetSlot);
				boolean r = ((Boolean)nextChange.getOwnSlotValue(_refersFrameSlot)).booleanValue();
				
				TableRow tableRow = _acceptorRejector.getSoleRow(instance);
				FrameDifferenceElement diffElement = tableRow.getDifferenceElement(o1,o2,dslot,dfacet,r);
				
				if(diffElement!=null)
					_acceptorRejector.acceptChangeInInstance(cls1,instance,diffElement);
				
			}
			
			
			
		}
		
		
	}
	
	
	
}

