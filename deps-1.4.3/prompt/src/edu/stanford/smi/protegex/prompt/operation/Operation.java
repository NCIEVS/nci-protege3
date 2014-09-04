 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation;

import java.util.*;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.actionLists.*;
import edu.stanford.smi.protegex.prompt.conflict.*;
import edu.stanford.smi.protegex.prompt.explanation.*;
import edu.stanford.smi.protegex.prompt.plugin.model.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class Operation extends Action {
  private Logger log = Log.getLogger(getClass());  
  
  static private FrameID [] _ownSlotsToIgnoreIDsArray =
       {Model.SlotID.DIRECT_SUBCLASSES,
       	Model.SlotID.DIRECT_SUPERCLASSES,
        Model.SlotID.DIRECT_INSTANCES,
        Model.SlotID.DIRECT_TEMPLATE_SLOTS,
        Model.SlotID.NAME,
//        Model.Slot.ID.DIRECT_DOMAIN,
//        Model.Slot.ID.CONSTRAINTS,
        Model.SlotID.DIRECT_TYPES,
//		Model.SlotID.DIRECT_TYPE
		};
        
  static private Set<FrameID> _ownSlotsToIgnoreIDs = new HashSet<FrameID> (Arrays.asList(_ownSlotsToIgnoreIDsArray));
  static private Map<KnowledgeBase, Set<Slot>> _kbsToOwnSlotsToIgnore = new HashMap<KnowledgeBase, Set<Slot>> (); // <kb to HashSet of own slots to ignore

  String [] _argLabels;
  protected boolean _canView = true;  //is there a create operation dialog? (with all teh widgets)
  protected Cls _frameType = null;

//  protected Collection _currentSuggestedOperations;
  protected Collection _currentFoundConflicts = null;
  protected boolean _defaultConstructorSuccessful = true;
  protected boolean _operationPerformed = true;  // will be set to false if Operation is found unnecessary
  protected Collection <Conflict> _conflictsItSolves = new ArrayList<Conflict>();
  protected boolean _temporary = false; // true if operation was called from another one and there is no need to check for name conflicts.

//  protected boolean _copyInstances = false;
//  protected boolean _copySubclasses = false;
//  protected boolean _copyEverythingRelated = false;
//  protected boolean _copySeveralLevels = false;
//  protected boolean _copyingLevelOnly = false; // if we are extracting several levels around an argument class, we want only top-level
//                                           // slot information for these classes (i.e., do not look at facets at class or own slot values
//
 
  protected TraversalDirective _traversalDirective = TraversalDirective.getNullDirective();

  protected boolean _copyEverythingRequired = false;
  protected boolean _copySlots = false;
  protected boolean _includeSuperclassesOfRelatedFrames = false;

  protected Frame _newFrame = null;
  private Collection _reason = new ArrayList ();
//  private Explanation _reason = new Explanation ();
  protected static KnowledgeBase _targetKb = ProjectsAndKnowledgeBases.getTargetKnowledgeBase();
  protected static MappingStoragePlugin [] _mappingStoragePlugins = ProjectsAndKnowledgeBases.getMappingStoragePlugins();

  public Operation () {
    super (MAX_ARITY);
    initialize (MAX_ARITY);
  }

  public Operation (int ar) {
    super (ar);
    initialize (ar);
  }

  public Operation (int ar, Explanation exp) {
    super (ar);
    addExplanation (exp);
    initialize (ar);
  }

  Operation (Explanation exp) {
    super (MAX_ARITY);
    addExplanation (exp);
    initialize (MAX_ARITY);
  }

  private void initialize(int ar) {
    _argLabels = new String [ar];
    for (int i = 0; i < ar; i++)
        _argLabels[i] = "argument " + i;
    _targetKb = ProjectsAndKnowledgeBases.getTargetKnowledgeBase();
    _priority = SuggestionsAndConflicts.getCurrentTodoPriority();
  }

  public void performOperation () {
    //do the actual thing
// 	if (getPriority() % 50 == 0)
 	   Log.getLogger().info ("Performing operation " + new Date() + " : " + this);
//    Log.stack("Performing operation", this, "performOperation");

//****** TD: check back later
//    PromptTab.setKeepInQueue (_copyEverythingRelated);
	  _newFrame = null;
      Statistics.addToLogStream(this.toString());

      PromptTab.startOperation(this);

      if (!_temporary)
  		    SuggestionsAndConflicts.incrementCurrentPriority();
//      _currentSuggestedOperations = new ArrayList();
      
      if (PromptTab.merging() || PromptTab.moving())
    	  	_currentFoundConflicts = new ArrayList();

      SuggestionsAndConflicts.removeSuggestionFromList (this, true);
      removeAlternativeSuggestionsFromList ();

      actualOperation ();

      if (PromptTab.merging()) {
	      if (_newFrame != null && _newFrame instanceof Cls) {
	        Cls parent = checkForCyclesForCls ((Cls)_newFrame);
	        if (parent != null)
	           _currentFoundConflicts.add (Conflict.cycle ((Cls)_newFrame, (Cls)parent));
	      }
	
	      SuggestionsAndConflicts.addConflicts (_currentFoundConflicts);
	     moveAlternativeOperationsToTop ();
      }
//      Statistics.printStatistics();
      PromptTab.completeOperation(this);
  }

  public void removeAlternativeSuggestionsFromList () {

  }

  public void actualOperation () {

  }

  public boolean allArgumentsDefined () {
    for (int i = 0; i < _arity; i++)
      if (_args.getArg(i) == null || _args.getArg(i).equals ("")) return false;

    return true;
  }

// use this method to disallow the move of a frame to an included project if it depends on other
// included projects. Non-trivial only in the moving() mode (MoveUpOperation)
  public String allArgumentsValid () {
    return null;
  }

   private boolean warned = false;
   protected Frame createNewFrame (Frame [] oldFrames, String newName, KnowledgeBase sourceKb, Cls frameType) {
   	 if (!warned  && PromptTab.kbInOWL() && (PromptTab.merging()  || PromptTab.mapping())) {
   	     log.warning("Names in owl not figured out yet");
   	     warned = true;
   	 }
     if (!PromptTab.kbInOWL() && (PromptTab.merging()  || PromptTab.mapping())) {
   	 	newName = Mappings.createNameWithSource (newName, sourceKb);
   	 }
     return createNewFrame (oldFrames, newName, frameType);
   }

   protected Frame createNewFrame (Frame [] oldFrames, String newName, Cls frameType) {
	Frame newFrame = null;
   try {
	if (oldFrames[0] instanceof Cls) {
       newFrame = _targetKb.createCls (newName,
                           CollectionUtilities.createCollection (_targetKb.getRootCls()),
                           frameType);
    }
	else if (oldFrames[0] instanceof Slot)
       newFrame = _targetKb.createSlot (newName, frameType);
    else if (oldFrames[0] instanceof Facet)
    	newFrame = _targetKb.createFacet (newName, frameType);
    else
       newFrame = _targetKb.createInstance(newName, frameType);

     finishUpCreateFrame (newFrame, oldFrames);
//     if (newFrame instanceof DefaultCls)
//      ((Cls)newFrame).addClsListener (new PromptClsListener ());

   } catch (Exception e) { e.printStackTrace();   }
     return newFrame;
   }

   protected void finishUpCreateFrame (Frame newFrame, Frame [] oldFrames) {
     for (int i = 0; i < oldFrames.length; i++) {
      Frame oldDummy = Mappings.getMappingToDummyFrame(oldFrames[i]);
      if (oldDummy != null)
        DummyFrame.replaceReferencesWithRealFrame(oldDummy, newFrame);

      Mappings.updateWhatBecameOfItMap (oldFrames[i], newFrame);
     }
   }

   protected Collection copyReferencedFrames (Collection c, KnowledgeBase targetKb,
                                           Frame newFrame,
                                           boolean createDummy,
                                           Slot newSlot) {
    return copyReferencedFrames (c, targetKb, newFrame, createDummy, newSlot, false);
  }

   protected Collection copyReferencedFrames (Collection c, KnowledgeBase targetKb,
                                           Frame newFrame,
                                           boolean createDummy,
                                           Slot newSlot,
                                           boolean subclassesOrSuperclasses) {
     if (c == null) return null;

     Collection newF = new HashSet();
     Iterator i = c.iterator();

     Frame nextFrame;
     Frame mapping;
     while (i.hasNext())  {
       Object next = i.next();
		if (next instanceof Frame) {
       		nextFrame = (Frame)next;
			// *** this is a hack for the moment
			// *** need to put better handling for included and import
			if (Util.isSystem(nextFrame)) {
				Frame targetFrame = targetKb.getFrame(nextFrame.getName()); //system frames
				if (targetFrame != null) newF.add(targetFrame);
			} else if (PromptTab.kbInOWL() && OWLUtil.isOWLAnonymousClassFrame(nextFrame)) {
				continue;
			} else {
	       		mapping = Mappings.getWhatBecameOfIt (nextFrame);
	       		if (mapping == null) {
	           		if (createDummy && !(PromptTab.kbInOWL() && OWLUtil.isOWLAnonymousClassFrame(nextFrame)))
	             		  newF.add (createDummyFrame (nextFrame, newFrame, targetKb, newSlot));
    	       		else {
        	   		  boolean moved =  moveRelatedOperationsToTop (nextFrame, newFrame, true, null, subclassesOrSuperclasses);
            			if (!moved)
            			  createCopyOperations(nextFrame, newFrame, null, subclassesOrSuperclasses);
//           	 		moveRelatedOperationsToTopOrCreateCopyOperation
// 	            		(next, newFrame, true, null, subclassesOrSuperclasses);
    	        	}
       			} else
           		newF.add (mapping);
			}
		} else
        	newF.add (next);
      }
      return newF;
   }

   public boolean hasDeletedFrames () {
    	for (int i = 0; i < _args.size(); i++) {
         	if (_args.getArg(i) instanceof Frame &&
                ((Frame)_args.getArg(i)).getName () == null)
            return true;
        }
        return false;
   }

   private boolean moveRelatedOperationsToTop (Frame f, Frame newFrame, boolean addExplanation,
                                                 Slot newSlot) {
     return moveRelatedOperationsToTop (f, newFrame, addExplanation, newSlot, false);
   }

// returns true if something was moved (placed on the toMove list
   private boolean moveRelatedOperationsToTop (Frame f, Frame newFrame, boolean addExplanation,
                                                 Slot newSlot, boolean subclassesOrSuperclasses) {
     Collection currentRelatedOperations = Mappings.getCurrentOperations (f);
     if (currentRelatedOperations != null && currentRelatedOperations.size() != 0)    {
       changePriorityToCurrentForRelatedOperations
                           (currentRelatedOperations, f, newFrame, addExplanation, newSlot, subclassesOrSuperclasses);
       return true;
     }
     return false;
   }

   private Collection createCopyOperations (Frame f, Frame newFrame, Slot newSlot, boolean subclassesOrSuperclasses) {
      if (!(PromptTab.merging ()  || PromptTab.mapping())&& subclassesOrSuperclasses) return null;
      Collection result = null;
      Operation newOp;
      if (_includeSuperclassesOfRelatedFrames && f instanceof Cls)
        newOp = DeepCopyFrameOperation.createOperation(f, new TraversalDirective (f, false, false), false);
      else
        newOp = KeepFrameOperation.createOperation (f, ReferencedBy.selectExplanation (f, newSlot, newFrame, subclassesOrSuperclasses));
      if (newOp != null) {
//        newOp.setCopySeveralLevels(_copySeveralLevels);
//        newOp.setCopyLevelOnly(_copyingLevelOnly);
		newOp.setTraversalDirective (_traversalDirective);
        newOp.setIncludeSuperclassesOfRelatedFrames (_includeSuperclassesOfRelatedFrames);
      	result = CollectionUtilities.createCollection (newOp);
//      	_currentSuggestedOperations.add (newOp);
        if (PromptTab.merging())
        		SuggestionsAndConflicts.addSuggestions (result);
      }
      return result;
   }

   private void changePriorityToCurrentForRelatedOperations
                        (Collection currentRelatedOperations, Frame f, Frame newFrame, boolean addExplanation,
                        Slot newSlot, boolean subclassesOrSuperclasses){
     Collection currentOperations = new ArrayList (currentRelatedOperations);
     Collection result = new ArrayList();
     Iterator i = currentOperations.iterator();
     Action next;
     while (i.hasNext())  {
       next = (Action) i.next();
       result.add (next);
       if (addExplanation && next instanceof Operation)
	   	((Operation)next).addExplanation (ReferencedBy.selectExplanation(f, newSlot, newFrame, subclassesOrSuperclasses));
         
       SuggestionsAndConflicts.addToPendingMoves (next);
     }
   }


/*
   private Collection moveRelatedOperationsToTop (Frame f, Frame newFrame, boolean addExplanation,
                                                 Slot newSlot, boolean subclassesOrSuperclasses) {
     // if there are no related operations, a KEEP operation is suggested
     Collection result = null;
     Collection currentRelatedOperations = Mappings.getCurrentOperations (f);

     if (currentRelatedOperations != null && currentRelatedOperations.size() != 0)    {
       result = changePriorityToCurrentForRelatedOperations
                           (currentRelatedOperations, f, newFrame, addExplanation, newSlot, subclassesOrSuperclasses);
     }
     else  {
      KeepFrameOperation newOp = KeepFrameOperation.createOperation
      			(f, ReferencedBy.selectExplanation (f, newSlot, newFrame, subclassesOrSuperclasses));
      if (newOp != null) {
      	result = CollectionUtilities.createCollection (newOp);
//      	_currentSuggestedOperations.add (newOp);
		SuggestionsAndConflicts.addSuggestions (result, true);
      }
     }
   return result;
   }
*/

   private Frame createDummyFrame (Frame originalFrame, Frame newFrame, KnowledgeBase targetKb, Slot newSlot) {
     Frame dummy = DummyFrame.createDummyFrame (originalFrame, targetKb);
     if (dummy != originalFrame && PromptTab.merging())
       _currentFoundConflicts.add
            (Conflict.danglingReference (newFrame, newSlot, originalFrame,
                                         createSolutions (originalFrame, newFrame, true, newSlot)));
     return dummy;
   }

   public Collection createSolutions (Frame f, Frame newFrame, boolean addExplanation, Slot newSlot) {
	 boolean solutionsExist = moveRelatedOperationsToTop (f, newFrame, addExplanation, newSlot);
     Collection relatedOperations = Mappings.getCurrentOperations (f);
     if (!solutionsExist)
     	relatedOperations = createCopyOperations (f, newFrame, newSlot, false);
     Collection result = new ArrayList();

     if (relatedOperations == null)
       return result;

     Iterator i = relatedOperations.iterator();
     Action next;
     while (i.hasNext()) {
       next = (Action)i.next();
       if (next instanceof Operation)
         result.add (next);
     }
     return result;
   }

   public void moveAlternativeOperationsToTop () {
     for (int i = 0; i < _arity; i++) {
       Object next = _args.getArg(i);
       if (next instanceof Frame) {
         Collection currentRelatedOperations = Mappings.getCurrentOperations ((Frame)next);
//         mapping = Mappings.getWhatBecameOfIt((Frame) next);
         if (currentRelatedOperations != null && currentRelatedOperations.size() != 0) {
            Iterator j = currentRelatedOperations.iterator();
            while (j.hasNext())
              SuggestionsAndConflicts.changePriorityToCurrent ((Action)j.next());
         }
       }
     }
   }

   public void addDirectSubclass (Cls child, Cls parent, KnowledgeBase kb) {
     if (!child.getDirectSuperclasses().contains(parent))
       addDirectSuperclasses (child, CollectionUtilities.createCollection(parent));
     Cls rootCls = kb.getRootCls();
     if (!parent.equals (rootCls) && child.getDirectSuperclasses().contains(rootCls))
       child.removeDirectSuperclass (rootCls);
   }

/*
   static public JPanel createActionBox (GetValueWidget[] argWidgets) {

    JPanel p1 = new JPanel();
    p1.setLayout (new BoxLayout (p1, BoxLayout.Y_AXIS));

    for (int i = 0; i < argWidgets.length; i++) {
      	JComponent next = (JComponent)argWidgets[i];
//Log.trace ("next value = " + argWidgets[i].getValues() , Operation.class, "createActionBox");
        next.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		p1.add(next);
    }

    return p1;
  }

  public JPanel createEditBox (GetValueWidget[] argWidgets) {
    JPanel p = createActionBox (argWidgets);

    for (int i = 0; i < argWidgets.length; i++) {
      argWidgets[i].setValue(_args[i]);
    }
    return p;
  }

  public static JPanel createEditBox (GetValueWidget[] argWidgets, Object[] args) {
    for (int i = 0; i < argWidgets.length; i++) {
		Object next = args[i];
		argWidgets[i].setValue (args[i]);
    }

    JPanel p = createActionBox (argWidgets);
    return p;
  }

  public JPanel createEditBox () {
    return null;
  }

  public void collectData (GetValueWidget[] argWidgets) {
    Object next;

    for (int i = 0; i < argWidgets.length; i++) {
      next = argWidgets[i].getValues();

      if (next instanceof Collection)
        _args[i] = CollectionUtilities.getFirstItem ((Collection)next);
      else
        _args[i] = next;

      argWidgets[i].clear();
    }
  }
*/
   protected void  copySlotAttachmentInformation (Frame oldFrame, Slot oldSlot,
                                                Frame newFrame, Slot newSlot,
                                                boolean templateOrOwnSlots) {
      copySlotAttachmentInformation (oldFrame, oldSlot, newFrame, newSlot, templateOrOwnSlots, true);
   }

   protected void  copySlotAttachmentInformation (Frame oldFrame, Slot oldSlot,
                                                Frame newFrame, Slot newSlot,
                                                boolean templateOrOwnSlots, boolean copyOverrides) {
     if (oldFrame instanceof Cls && newFrame instanceof Cls) {
     	if (templateOrOwnSlots) {
       		addDirectTemplateSlot ((Cls)newFrame, newSlot);
       		if (copyOverrides 
       				//&& ((Cls)oldFrame).hasDirectlyOverriddenTemplateSlot (oldSlot)
       				)
       				copyTemplateSlotAttributes ((Cls)oldFrame, oldSlot, (Cls)newFrame, newSlot);
        } else {
			copyOwnSlotValue (oldFrame, oldSlot, newFrame, newSlot);
        }
     } else if (oldFrame instanceof Slot && newFrame instanceof Slot) {
		copyTopLevelSlotAttributes ((Slot)oldFrame, (Slot)newFrame, newSlot, oldSlot);
     } else if (oldFrame instanceof Instance && newFrame instanceof Instance ) {
			copyOwnSlotValue (oldFrame, oldSlot, newFrame, newSlot);
     }
   }

   protected void copyOwnSlotValue(Frame oldFrame, Slot oldSlot,
			Frame newFrame, Slot newSlot) {
		Collection values = oldFrame.getOwnSlotValues(oldSlot);
		boolean refersToFrames = refersToFrames(oldFrame, oldSlot);
		if (refersToFrames) {
			// values = copyReferencedFrames (values, _targetKb, newFrame,
			// !(_copyEverythingRelated || _copySeveralLevels), newSlot);
			values = copyReferencedFrames(values, _targetKb, newFrame, _traversalDirective.lastLevel(), newSlot);
		}
		// Log.trace ("oldSlot = " + oldSlot + ", newSlot = " + newSlot, this,
		// "copyOwnSlotValue");
		
		if(!newSlot.getBrowserText().equals(":NAME")) {
			newFrame.setOwnSlotValues (newSlot, values);
		}
	}

   // guard against bad overriding (e.g., overriding a string valued slot with an instance valued one
   protected boolean refersToFrames (Frame frame, Slot slot) {
//   	 Cls type = ((Instance)frame).getDirectType();
  //   if (type.hasDirectlyOverriddenTemplateSlot(slot))
//     	return (type.getTemplateSlotValueType(slot) == ValueType.INSTANCE ||
  //              type.getTemplateSlotValueType(slot) == ValueType.CLS);
    // else
     	return (slot.getValueType() == ValueType.INSTANCE ||
                slot.getValueType() == ValueType.CLS);
   }

   protected void copyTopLevelSlotAttributes (Slot oldSlotFrame, Slot newSlotFrame, Slot newSlot, Slot oldSlot) {
	 Collection values = oldSlotFrame.getOwnSlotValues (oldSlot);
     if (values == null) return;

     Iterator i = values.iterator();
     Collection newValues = new ArrayList();
     while (i.hasNext ()) {
      	Object next = i.next();
        if (next instanceof Frame) {
        	Collection nextReferencedFrame = copyReferencedFrames (CollectionUtilities.createCollection(next), newSlotFrame.getKnowledgeBase(),
			 														null, true, newSlotFrame);
			if (nextReferencedFrame.isEmpty())
				next = null;
			else
        		next = (Frame) CollectionUtilities.getFirstItem (nextReferencedFrame);
        }
        if (newSlot.getValueType () == ValueType.FLOAT && next instanceof Integer) {
         	Integer nextInt = (Integer)next;
            next = new Float (nextInt.floatValue());
        }
        if (next != null)
        	newValues.add (next);
     }
//	Log.trace ("old values = " + values, this, "copyTopLevelSlotAttributes");
//	Log.trace ("newSlotFrame = " + newSlotFrame + ", newSlot = " + newSlot + ", newValues = " + newValues, 
//	this, "copyTopLevelSlotAttributes");
   //  Log.trace ("oldSlot = " + oldSlot + ", newSlot = " + newSlot, this, "copyTopLevelSlotAttributes");

     if(!newSlot.getBrowserText().equals(":NAME")) {
    	 newSlotFrame.setOwnSlotValues (newSlot, newValues);
     }
   }

   protected void copyTemplateSlotAttributes (Cls oldCls, Slot oldSlot, Cls newCls, Slot newSlot) {
     if (newCls.equals (oldCls)) return;
     copyTemplateSlotSimpleAttributes (oldCls, oldSlot, newCls, newSlot);
     copyTemplateSlotComplexAttributes (oldCls, oldSlot, newCls, newSlot);
     copyUserDefinedTemplateFacets (oldCls, oldSlot, newCls, newSlot);
   }

   protected void copyUserDefinedTemplateFacets (Cls oldCls, Slot oldSlot, Cls newCls, Slot newSlot) {
		Collection facets = oldCls.getTemplateFacets(oldSlot);
		KnowledgeBase targetKb = newCls.getKnowledgeBase();
		Iterator i = facets.iterator();
		while (i.hasNext()) {
			Facet nextFacet = (Facet)i.next();
			if (nextFacet.isSystem()) continue;
			if (oldCls.hasDirectlyOverriddenTemplateFacet(oldSlot, nextFacet)) {
				Collection oldValues = oldCls.getTemplateFacetValues(oldSlot, nextFacet);
				Facet newFacet = (Facet)Mappings.getWhatBecameOfIt(nextFacet);
				if (newFacet == null)
					Log.getLogger().severe("No mapping for facet " + newFacet);
				Collection newValues = copyReferencedFrames (oldValues, targetKb, newCls, true, newSlot);
				newCls.setTemplateFacetValues(newSlot, newFacet, newValues);
			}
		}
   }

   protected void copyTemplateSlotComplexAttributes (Cls oldCls, Slot oldSlot, Cls newCls, Slot newSlot) {
     ValueType type = newCls.getTemplateSlotValueType (newSlot);
     KnowledgeBase targetKb = newCls.getKnowledgeBase();

	Collection oldValues = oldCls.getTemplateSlotValues (oldSlot);
    if (oldValues != null) {
		Collection newValues = copyReferencedFrames (oldValues, targetKb, newCls, true, newSlot);
//		newCls.setTemplateSlotValues (newSlot, newValues);
		newCls.setTemplateFacetValues(newSlot, newCls.getKnowledgeBase().getFacet(Model.Facet.VALUES), newValues);	
	}

	Collection defaultValues = oldCls.getTemplateSlotDefaultValues (oldSlot);
    if (defaultValues != null)
      newCls.setTemplateSlotDefaultValues (newSlot, copyReferencedFrames (defaultValues, targetKb, newCls, true, newSlot));

     if (type == ValueType.INSTANCE)
       newCls.setTemplateSlotAllowedClses
           (newSlot, copyReferencedFrames (oldCls.getTemplateSlotAllowedClses(oldSlot), targetKb, newCls, true, newSlot));
     else if (type == ValueType.CLS)
       newCls.setTemplateSlotAllowedParents
           (newSlot, copyReferencedFrames (oldCls.getTemplateSlotAllowedClses(oldSlot), targetKb, newCls, true, newSlot));
     else if (type == ValueType.SYMBOL)
       newCls.setTemplateSlotAllowedValues
           (newSlot, oldCls.getTemplateSlotAllowedValues (oldSlot));
   }

   protected void copyTemplateSlotSimpleAttributes (Cls oldCls, Slot oldSlot, Cls newCls, Slot newSlot) {
     newCls.setTemplateSlotValueType
            (newSlot, oldCls.getTemplateSlotValueType (oldSlot));

     newCls.setTemplateSlotMinimumCardinality
            (newSlot, oldCls.getTemplateSlotMinimumCardinality (oldSlot));

     newCls.setTemplateSlotMaximumCardinality
            (newSlot, oldCls.getTemplateSlotMaximumCardinality (oldSlot));

     newCls.setTemplateSlotDocumentation
            (newSlot, oldCls.getTemplateSlotDocumentation (oldSlot));

     newCls.setTemplateSlotMaximumValue
            (newSlot, oldCls.getTemplateSlotMaximumValue (oldSlot));
     newCls.setTemplateSlotMinimumValue
            (newSlot, oldCls.getTemplateSlotMinimumValue (oldSlot));

   }


  public String  toString (){
    String result;
//    result = getPriority() + ": ";
    result = PromptTab.getOperationsCount() + ": ";

    if (_prettyName != null)
       result += _prettyName.toUpperCase();
    else
       result += _name.toUpperCase();
    result += " ";
    for (int i = 0; i < _arity; i++) {
      if (_args.getArg(i) instanceof Frame)
//        result = result + DisplayUtilities.displayFrameWithAffiliation ((Frame)_args.getArg(i)) + " ";
		result = result +  ((Frame)_args.getArg(i)).getBrowserText() + " ";
      else
        result = result + _args.getArg(i) + " ";

    }

//    if (_reason != null && _reason.size () > 0)
//    	result = result + ", reasons: " + _reason;
//    result = result + ", _copyInstances = " + new Boolean (_copyInstances) +
//             ", _copySubclasses = " + new Boolean (_copySubclasses);

/*
    if (Prompt.displayExplanations())
      result += " " + _reason;
*/
    result += showParameters ();
    return result;
  }

  private String showParameters () {
    String params = "PARAMS: ";
//    if (this instanceof DeepCopyFrameOperation)
//      params += " supers";

//    if (_copySubclasses)
//      params += " subs";
//
//    if (_copyInstances)
//      params += " inst";
//
//    if (_copySlots)
//      params += " slots";
//
//    if (_traversalDirective.copyEverythingRelated())
//      params += " related";
//
//    if (_traversalDirective.lastLevel())
//      params += " level only";
//
//    if (_copyEverythingRequired)
//      params += " required";
//
//    if (_copySeveralLevels)
//      params += " " + PromptTab.getCopyingDepth() + " level(s)";
	if (_traversalDirective != null)
		params += _traversalDirective.printDirectives();

    return params;
  }

  public  int getArity () {return _arity; }

  public Collection getReason () {
    return _reason;
  }

  public void addExplanation (Explanation exp) {
    if (exp != null) {
    	_reason.add (exp);
    exp.setOperation (this);
    exp.addToFrameActionsMap();
    }
  }

  public void removeExplanation (Explanation exp) {
   	if (_reason.contains(exp)) {
    	_reason.remove(exp);
        if (_reason.size () == 0)
        	SuggestionsAndConflicts.removeSuggestionFromList (this);
    }
  }

  public  String getName () {return _name; }

  public void setName (String name) {_name = name;}

  public  String getLabelForArg (int i) {return (String)_argLabels[i];}

  public Collection findOperationsWithCurrentFrames () {
    Set result = new HashSet();
    Frame next;
    Collection operationMapForArg;

    for (int i = 0; i < _arity; i++) {
      if (_args.getArg(i) instanceof Frame) {
        next = (Frame)_args.getArg(i);
        operationMapForArg = (Collection)Mappings.getFrameActionsMap(next.getKnowledgeBase()).getValues(next);
        if (operationMapForArg != null)
          result.addAll (operationMapForArg);
      }
    }
    return result;
  }

  public boolean wasDefaultConstructorSuccessful () {return _defaultConstructorSuccessful; }

   public void addConflictItSolvesToOperation (Conflict conflict) {
     _conflictsItSolves.add (conflict);
   }

   public void addConflictsItSolvesToOperation (Collection conflicts) {
   	if (conflicts == null || conflicts.size() == 0) return;
     _conflictsItSolves.addAll (conflicts);
   }

   public void removeConflictItSolvesToOperation (Conflict conflict) {
     _conflictsItSolves.remove (conflict);
   }

   public Collection getConflictsItSolves () {
     return _conflictsItSolves;
   }

   static public Collection<Slot> getLocalTemplateSlotsOnly (Cls cls) {
     Collection<Slot> allTemplateSlots = cls.getTemplateSlots();
     Collection<Slot> result = new ArrayList<Slot>();
     Iterator<Slot> i = allTemplateSlots.iterator();
     Slot next;
     while (i.hasNext())  {
       next = i.next();
      if (cls.hasDirectTemplateSlot (next)  || cls.hasDirectlyOverriddenTemplateSlot(next))
         result.add (next);
     }
    return result;

   }

   protected void  addDirectSuperclasses (Cls cls, Collection superclasses) {
     if (superclasses == null || superclasses.size() == 0)
        return;
     else {
        superclasses.removeAll (Util.getDirectSuperclasses(cls));
//        superclasses.remove(_targetKb.getRootCls());
        Iterator i = superclasses.iterator();
        while (i.hasNext()) {
          Cls next = (Cls)i.next();
          if (!next.hasSuperclass(cls)) {
			cls.addDirectSuperclass (next);
          }
        }
        Collection newSuperclasses = Util.getDirectSuperclasses(cls);

        if (newSuperclasses.size() > 1 &&
        	newSuperclasses.contains(_targetKb.getRootCls()))
	        cls.removeDirectSuperclass (_targetKb.getRootCls());
     }
//     reorderSubclassesForSuperclasses (cls);
   }

/*
   private void reorderSubclassesForSuperclasses (Cls cls) {
   // ** never finished......
    	Collection superClses = new ArrayList (cls.getDirectSuperclasses());
        Iterator i = superClses.iterator();
        while (i.hasNext()) {
         	Cls next = (Cls)i.next();
            reorderSubclasses (next, cls);
        }
   }

   private void reorderSubclasses (Cls superCls, Cls subCls) {
   		 boolean BEFORE = true;
         boolean AFTER = false;
		Collection allSubclasses = superCls.getDirectSubclasses();
        if (allSubclasses.size() <= 1) return;

        KnowledgeBase kb1 = null;
        KnowledgeBase kb2 = null;
        Collection clsSources = Mappings.getSources(subCls);
        if (clsSources != null) {
         	kb1 = ((Frame)CollectionUtilities.getFirstItem(clsSources)).getKnowledgeBase();
            if (clsSources.size() > 1)
            	kb2 = findSecondSource (clsSources, kb1);
        }
        KnowledgeBase kb = null; //if there is only one source, store it here
        if (kb1 == null) kb = kb2;
        if (kb2 == null) kb = kb1;

        Iterator i = allSubclasses.iterator();
        boolean beforeOrAfter = BEFORE;
		while (i.hasNext()) {
         	Cls nextSub = (Cls)i.next();
        }
   }

   private KnowledgeBase findSecondSource (Collection sources, KnowledgeBase firstSource) {
    	Iterator i = sources.iterator();
        i.next();
        while (i.hasNext()) {
         	Frame next = (Frame)i.next();
            KnowledgeBase kb = next.getKnowledgeBase();
            if (! kb.equals (firstSource))
            	return kb;
        }
        return null;
   }
*/
  public Cls checkForCyclesForCls (Cls cls) {
//    Collection result = new ArrayList();
    LinkedList parentsOfCls = new LinkedList (Util.getDirectSuperclasses(cls));
    if (parentsOfCls.size() <= 1) return null;

    int count = 0;
    Cls root = _targetKb.getRootCls();
    Collection visited = new ArrayList ();
    LinkedList current = new LinkedList ();
    current.add (cls);

    while (!current.isEmpty()) {
      Cls next = (Cls)current.removeFirst();
      if (next != root && visited.contains (next))
        return next;

      Collection parents = Util.getDirectSuperclasses (next);
      Iterator i = parents.iterator();
      while (i.hasNext()) {
          Cls nextParent = (Cls)i.next();
          current.addLast(nextParent);
      }
      visited.add(next);
    }
    return null;
  }

   static public void addDirectTemplateSlot (Cls cls, Slot s) {
     if (!cls.hasTemplateSlot(s)) {
		cls.addDirectTemplateSlot(s);
     }
   }

   static protected boolean member (String value, String[] array) {
     for (int i = 0; i < array.length; i++)
       if (value.equals (array[i]))
         return true;
     return false;
   }

  public static void removeClsToClsReferences (Cls from, Cls to) {

  }

  public boolean canView () {return _canView;}
  
 

	public static Set<Slot> getOwnSlotsToIgnore(KnowledgeBase kb) {
		Set<Slot> result = _kbsToOwnSlotsToIgnore.get(kb);
		if (result != null) return result;
		
		result = new HashSet<Slot>();
		Collection allSlots = kb.getSlots(); // need to do this since I cannot get a slot given FrameID
		Iterator i = allSlots.iterator();
		while (i.hasNext()) {
			Slot next = (Slot)i.next();
			FrameID nextFrameID = next.getFrameID();
			if (_ownSlotsToIgnoreIDs.contains(nextFrameID))
				result.add(next);
//			else if (PromptTab.kbInOWL() && OWLUtil.ownSlotToIgnoreID (nextFrameID))
//				result.add (next);
				
		}
		_kbsToOwnSlotsToIgnore.put(kb, result);
		return result;
	}


  public boolean copySubclasses () { return _traversalDirective.copySubclasses(); }
  public boolean copyInstances () {return _traversalDirective.copyInstances(); }
  public boolean copyEverythingRelated () { return _traversalDirective.copyEverythingRelated(); }
  public boolean copySlots () { return _copySlots; }

//  public void setCopyEverythingRelated (boolean value) { _copyEverythingRelated = value; }
//  public void setCopySeveralLevels (boolean value) { _copySeveralLevels = value; }
//  public void setCopyLevelOnly (boolean value) { _copyingLevelOnly = value; }

 
  public void setCopyEverythingRelated (boolean value) { _traversalDirective.setCopyEverythingRelated (value); }
//  public void setCopySeveralLevels (boolean value) { _traversalDirective.setCopySeveralLevels(value); }
  public void setCopyLevelOnly (boolean value) { _traversalDirective.setLastLevel (value); }
  public void setNumberOfLevels (int levels) {_traversalDirective.setNumberOfLevels (levels); }
  public void setCopySubclasses (boolean value) {_traversalDirective.setCopySubclasses(value); }
  public void setCopyInstances (boolean value) {_traversalDirective.setCopyInstances(value); }
 
  public void setIncludeSuperclassesOfRelatedFrames (boolean value) {_includeSuperclassesOfRelatedFrames = value;}

  protected Operation _dispatchOperation = null;
  public Operation getDispatchOperation () {return _dispatchOperation;}
  
  protected void createMapping () {
  }
  
  public void setTraversalDirective (TraversalDirective td) {
  	_traversalDirective = td;
  }

  public TraversalDirective getTraversalDirective () {
  	return _traversalDirective;
  }

}

