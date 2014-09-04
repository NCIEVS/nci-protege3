/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
   *                 Kyle Bruck kbruck@stanford.edu
*/

package edu.stanford.smi.protegex.prompt.operation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.stanford.smi.protege.model.BrowserSlotPattern;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.prompt.Mappings;
import edu.stanford.smi.protegex.prompt.Preferences;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.TraversalDirective;
import edu.stanford.smi.protegex.prompt.analysis.Analysis;
import edu.stanford.smi.protegex.prompt.conflict.Conflict;
import edu.stanford.smi.protegex.prompt.explanation.AlreadyExists;
import edu.stanford.smi.protegex.prompt.explanation.Explanation;
import edu.stanford.smi.protegex.prompt.ui.Warning;
import edu.stanford.smi.protegex.prompt.util.OWLUtil;
import edu.stanford.smi.protegex.prompt.util.Util;

public class CopyOperation extends Operation {
	static final int COPY_OPERATION_ARITY = 1;
	//when copying a direct type of an instance during a copy instance operation, always
	//consider inherited slots

	protected boolean _localConsiderInheritedSlots = false;

	private boolean _continueCopy = true;
	protected boolean _typeSameAsFrame = false;

	public CopyOperation() {
		super(COPY_OPERATION_ARITY);
	}

	public CopyOperation(TraversalDirective td) {
		super(COPY_OPERATION_ARITY);
		_traversalDirective = td;
	}

	public CopyOperation(Explanation exp) {
		super(COPY_OPERATION_ARITY, exp);
	}

	protected void setDefaultParameters() {
		if (PromptTab.merging() && _traversalDirective.nullDirective()) {
			_traversalDirective = new TraversalDirective(null, false, false);
//    	_copyInstances = false;
//        _copySubclasses = false;
		} else {
			_traversalDirective = new TraversalDirective(null, false, false);
//    	_copyInstances = false;
//        _copySubclasses = false;
		}
	}

	public void actualOperation() {
		Frame f = (Frame) _args.getArg(0);

		KnowledgeBase sourceKb = f.getKnowledgeBase();

		if (sourceKb == _targetKb) {
			return;
		}

		if (Util.isSystem(f)) {
			return;
		}

		// *** change this when imports are handled. in fact, perhaps, it should work for inclusion as well

		if (f.isIncluded()) {
			return;
		}

		// it has been already copied or merged with something

		_operationPerformed = alreadyExists(f, sourceKb);

		if (!_operationPerformed) {
		    if (_targetKb instanceof OWLModel) {
		        ((OWLModel) _targetKb).getFrameStoreManager().setOwlFrameStoresEnabled(false);
		    }
		    try {
		        _newFrame = copyFrame(f, sourceKb);
		    }
		    finally {
		        if (_targetKb instanceof OWLModel) {
		            ((OWLModel) _targetKb).getFrameStoreManager().setOwlFrameStoresEnabled(true);
		        }
		    }

		} else {
			copyDirectSuperclasses((Cls) f, (Cls) _newFrame);
		}

//    if (_newFrame == null) return;
//    
		if (_traversalDirective.copyInstances()) {
			copyInstances((Cls) f);
		}

		if (_traversalDirective.copySubclasses()) {
			copyTree((Cls) f);
		}

		//if (PromptTab.mergingAndMapping())
		//	createMapping();

		if (PromptTab.extracting()) {
			executeTraversalDirective(); //if traversal directives are not null
		}

		if (PromptTab.extracting() || PromptTab.merging() || PromptTab.mapping()) {
			copyBrowserKey();
		}

		if (_newFrame instanceof Cls) {
			Analysis.classCopied((Cls) _newFrame);
		}
	}

	private void copyBrowserKey() {
		if (!(_args.getArg(0) instanceof Cls)) {
			return;
		}
		if (PromptTab.kbInOWL() && OWLUtil.isOWLAnonymousClassFrame((Cls) _args.getArg(0))) {
			return;
		}

		Cls cls = (Cls) _args.getArg(0);
		BrowserSlotPattern pattern = cls.getBrowserSlotPattern();
		if (pattern == null) {
			return;
		}

		Collection elements = pattern.getElements();
		List newElements = new ArrayList();
		boolean slotExists = false;
		Iterator i = elements.iterator();

		while (i.hasNext()) {
			Object next = i.next();
			Object nextElement;
			if (next instanceof Slot) {
				Object newBrowserSlot = Mappings.getWhatBecameOfIt((Slot) next);
				if (newBrowserSlot == null) {
					continue;
				}
				nextElement = newBrowserSlot;
				slotExists = true;
			} else {
				nextElement = next;
			}
			newElements.add(nextElement);
		}

		if (slotExists) {
			((Cls) _newFrame).setDirectBrowserSlotPattern(new BrowserSlotPattern(newElements));
		}
	}

	// create and execute new operations if traversal directive is non-trivial
	protected void executeTraversalDirective() {
		if (_traversalDirective.nullDirective() || _traversalDirective.lastLevel()) {
			return;
		}

		TraversalDirective newTD = _traversalDirective.nextDirective(null);

		Collection directiveSlots = _traversalDirective.getSlotsInDirective();
		Frame oldFrame = (Frame) _args.getArg(0);
		executeTDsForFramesReferencedBySlot(oldFrame, newTD, directiveSlots, false);
		executeTDsForFramesReferencedBySlot(oldFrame, newTD, directiveSlots, true);
	}

	protected void executeTDsForFramesReferencedBySlot(Frame oldFrame, TraversalDirective newTD, Collection directiveSlots, boolean templateOrOwn) {
		if (!(oldFrame instanceof Cls) && templateOrOwn) {
			return;
		}
		Collection slots = new ArrayList(templateOrOwn ? ((Cls) oldFrame).getTemplateSlots() : oldFrame.getOwnSlots());
		slots.retainAll(directiveSlots);
		Iterator i = slots.iterator();
		Set<Slot> ownSlotsToIgnore = getOwnSlotsToIgnore(oldFrame.getKnowledgeBase());
		while (i.hasNext()) {
			Slot nextSlot = (Slot) i.next();
			if (ownSlotsToIgnore.contains(nextSlot)) {
				continue;
			}
			Collection framesReferencedBySlot = getFramesReferencedBySlot(oldFrame, nextSlot, templateOrOwn);
			if (framesReferencedBySlot != null && !framesReferencedBySlot.isEmpty()) {
				executeTDsForValueCollection(framesReferencedBySlot, newTD);
			}
		}
	}

	protected void executeTDsForValueCollection(Collection frames, TraversalDirective newTD) {
		Iterator i = frames.iterator();
		while (i.hasNext()) {
			Frame next = (Frame) i.next();
			TraversalDirective nextTD = newTD.sameDirective(next);
			if (!nextTD.nullDirective()) {
				(KeepFrameOperation.createOperation(next, nextTD, false)).performOperation();
			}
		}
	}

	protected Collection getFramesReferencedBySlot(Frame frame, Slot slot, boolean templateOrOwn) {
		if (!templateOrOwn) {
			return getFramesFromCollection(frame.getOwnSlotValues(slot));
		}
		Cls cls = (Cls) frame;
		Collection referencedValues = new ArrayList();
		referencedValues.addAll(cls.getTemplateSlotValues(slot));
		referencedValues.addAll(cls.getTemplateSlotAllowedClses(slot));
		referencedValues.addAll(cls.getTemplateSlotAllowedParents(slot));
		referencedValues.addAll(cls.getTemplateSlotAllowedValues(slot));
		referencedValues.addAll(cls.getTemplateSlotDefaultValues(slot));
		return getFramesFromCollection(referencedValues);
	}

	protected Collection getFramesFromCollection(Collection c) {
		Collection result = new ArrayList();
		Iterator i = c.iterator();
		while (i.hasNext()) {
			Object next = i.next();
			if (next instanceof Frame) {
				result.add(next);
			}
		}
		return result;
	}

	protected void createMapping() {
		Collection sources = Mappings.getSources(_newFrame);
		if (sources == null || sources.size() != 1) {
			return;
			//KB--Prevents duplicate slot mappings.
			//_mappingKb.createOneSidedMapping ((Frame)CollectionUtilities.getSoleItem(sources));
		}
	}

	protected void copyInstances(Cls cls) {
		Collection instances = cls.getDirectInstances();
		if (instances == null || instances.size() == 0) {
			return;
		}
		Iterator i = instances.iterator();

		if (cls.isSlotMetaCls()) {
			while (i.hasNext()) {
				Frame next = (Frame) i.next();
				(new KeepSlotOperation(next, _traversalDirective.nextDirective(next))).performOperation();
			}
		}
		if (cls.isMetaCls()) {
			while (i.hasNext()) {
				Frame next = (Frame) i.next();
				(new KeepClsOperation(next, _traversalDirective.nextDirective(next))).performOperation();
			}
		} else {
			while (i.hasNext()) {
				Frame next = (Frame) i.next();
				(new KeepInstanceOperation(next, (Cls) _newFrame, _traversalDirective.nextDirective(next))).performOperation();
			}
		}

	}

	protected void copyTree(Cls cls) {
		Collection subclasses = Util.getDirectSubclasses(cls);
		if (subclasses == null || subclasses.size() == 0) {
			return;
		}
		Iterator i = subclasses.iterator();
		while (i.hasNext()) {
//	(new KeepClsOperation ((Cls)i.next(), _copyInstances, true, false)).performOperation();
			Cls next = (Cls) i.next();
			(new KeepClsOperation(next, _traversalDirective.nextDirective(next), false)).performOperation();
		}
	}

	protected Frame copyFrame(Frame oldFrame, KnowledgeBase sourceKb) {
		if (Util.isSystem(oldFrame)) {
			return _targetKb.getFrame(oldFrame.getName());
		}

//    if (PromptTab.justCheckingTheView()) {
//    	Collection rows = PromptDiff.getResultsTable().getRows(oldFrame);
//		if (rows == null || rows.size() != 1) {
//			PromptTab.viewChanged (true);
//			return null;
//		}
//		TableRow row = (TableRow)CollectionUtilities.getSoleItem(rows);
//		if (row.getMappingLevel() != TableRow.OPERATION_MAP || row.getMappingLevel() != TableRow.MAPPING_LEVEL_UNCHANGED) {
//			PromptTab.viewChanged (true);
//			return null;
//		}
//	}
//
		Cls newFrameType = _frameType;

		if (newFrameType == null) {
			newFrameType = copyDirectType(oldFrame, sourceKb, _traversalDirective.lastLevel());
		}

		//it is possible that the frame was created as part of the recursive creation of the type in the statement above;
		// don't create it then -- just get out
		if (alreadyExists(oldFrame, sourceKb)) {
			return Mappings.getWhatBecameOfIt(oldFrame);
		}

		Frame newFrame = createNewFrame(new Frame[] { oldFrame }, oldFrame.getName(), sourceKb, newFrameType);

//     if (firstTime)
		_newFrame = newFrame;

		if (!_traversalDirective.lastLevel() || oldFrame instanceof Slot) {
			copyOwnSlots(oldFrame, newFrame);
		}

		// override direct subclasses and direct superclasses created as own slots
		// ** if slots can be in a hierarchy - fix this
		if (oldFrame instanceof Cls) {
			copyDirectSuperclasses((Cls) oldFrame, (Cls) newFrame);

			if (_typeSameAsFrame) {
				((Cls) newFrame).setDirectType((Cls) newFrame);
			}

			copyDirectSubclasses((Cls) oldFrame, (Cls) newFrame);
			copyTemplateSlots((Cls) oldFrame, (Cls) newFrame, true);
			if (PromptTab.kbInOWL()) {
				OWLUtil.copyRemainingAnonymousClasses((Cls) oldFrame, (Cls) newFrame);
			}
//	   copyTemplateSlots ((Cls)oldFrame, (Cls)newFrame, _traversalDirective.lastLevel());
		}

		restoreBackReferences(oldFrame, newFrame);

		return (newFrame);

	}

	private final static int TOO_MANY_REFERENCES = 1000;

	protected void restoreBackReferences(Frame oldFrame, Frame newFrame) {
		Collection refs = oldFrame.getReferences();
		Iterator i = refs.iterator();
		Set<Slot> toIgnore = getOwnSlotsToIgnore(oldFrame.getKnowledgeBase());
		while (i.hasNext()) {
			Reference nextRef = (Reference) i.next();
			Slot nextSlot = nextRef.getSlot();
			if (!toIgnore.contains(nextSlot)) {
				Frame nextFrame = nextRef.getFrame();
				Frame nextFrameMapping = Mappings.getWhatBecameOfIt(nextFrame);
				Slot nextSlotMapping = (Slot) Mappings.getWhatBecameOfIt(nextSlot);
				if (nextFrameMapping != null && nextSlotMapping != null && nextRef.getFacet() == null && !nextFrameMapping.getDirectOwnSlotValues(nextSlotMapping).contains(newFrame)) {
					nextFrameMapping.addOwnSlotValue(nextSlotMapping, newFrame);
				}
			}
		}
	}

	protected Cls copyDirectType(Frame oldFrame, KnowledgeBase sourceKb, boolean copyLevelOnly) {
		return null;
	}

	protected void copyDirectSuperclasses(Cls oldCls, Cls newCls) {
		Collection superclasses = copyReferencedFrames(Util.getDirectSuperclasses(oldCls), _targetKb, false);
		if (superclasses == null || superclasses.size() == 0) {
			superclasses = new ArrayList();
			findNearestCopiedSuperclass(oldCls, superclasses);
		}

//
//     if ((superclasses == null || superclasses.size() == 0) &&
//          oldCls.isMetaCls())
//        superclasses = CollectionUtilities.createCollection(Util.getStandardMetaclass(newCls.getKnowledgeBase()));
		addDirectSuperclasses(newCls, superclasses);
	}

	private void findNearestCopiedSuperclass(Cls cls, Collection nearestCopiedSuperclasses) {
		Iterator i = Util.getDirectSuperclasses(cls).iterator();
		while (i.hasNext()) {
			Cls nextSuper = (Cls) i.next();
			Cls nextSuperImage = (Cls) Mappings.getWhatBecameOfIt(nextSuper);
			if (nextSuperImage != null) {
				nearestCopiedSuperclasses.add(nextSuperImage);
			} else {
				findNearestCopiedSuperclass(nextSuper, nearestCopiedSuperclasses);
			}
		}
	}

//   protected void  addDirectSuperclasses (Cls cls, Collection superclasses) {
//   	 if (superclasses == null) return;
//   	 Collection oldSuperclasses = Util.getDirectSuperclasses(cls);
//   	 superclasses.removeAll(oldSuperclasses);
//   	 Iterator i = superclasses.iterator();
//   	 while (i.hasNext()){
//   	 	Cls next = (Cls)i.next();
//   	 	cls.addDirectSuperclass((Cls)next);
//   	 }
//
//	Collection newSuperclasses = cls.getDirectSuperclasses();
//Log.trace ("in Copy: cls = " + cls + ", superclasses = " + newSuperclasses, this, "addDirectSuperclasses");
//	if (newSuperclasses.size() > 1 &&
//		newSuperclasses.contains(_targetKb.getRootCls()))
//		cls.removeDirectSuperclass (_targetKb.getRootCls());
//   }

	public void addDirectSubclass(Cls child, Cls parent, KnowledgeBase kb) {
		Iterator i = (new ArrayList(Util.getDirectSuperclasses(child))).iterator();
		child.addDirectSuperclass(parent);
		while (i.hasNext()) {
			Cls nextChildSuper = (Cls) i.next();
			if (parent.hasSuperclass(nextChildSuper) && oldParentShouldBeRemoved(child, nextChildSuper) && !(PromptTab.kbInOWL() && OWLUtil.isOWLAnonymousClassFrame(parent))) {
				child.removeDirectSuperclass(nextChildSuper);
			}
		}
	}

	private boolean oldParentShouldBeRemoved(Cls child, Cls oldParent) {
		Cls childSource = (Cls) CollectionUtilities.getFirstItem(Mappings.getSources(child));
		Cls oldParentSource = (Cls) CollectionUtilities.getFirstItem(Mappings.getSources(oldParent));
		return !childSource.hasDirectSuperclass(oldParentSource);
	}

	private void copyDirectSubclasses(Cls oldCls, Cls newCls) {
		Collection subclasses = copyReferencedFrames(Util.getDirectSubclasses(oldCls), _targetKb, true);

		if (subclasses != null) {
			Iterator i = subclasses.iterator();
			while (i.hasNext()) {
				Cls cls = (Cls) i.next();
				addDirectSubclass(cls, newCls, _targetKb);
			}
		}
	}

	private Collection copyTemplateSlots(Cls oldFrame, Cls newFrame, boolean copyOverrides) {
		return copySlots(oldFrame, newFrame, true, copyOverrides);
	}

	private Collection copyOwnSlots(Frame oldFrame, Frame newFrame) {
		return copySlots(oldFrame, newFrame, false, false);
	}

	private Collection copySlots(Frame oldFrame, Frame newFrame, boolean templateOrOwnSlots, boolean copyOverrides) {
		Collection<Slot> newSlots = new ArrayList<Slot>();
		Collection<Slot> oldSlots = getSlots(oldFrame, templateOrOwnSlots);
		KnowledgeBase sourceKb = oldFrame.getKnowledgeBase();

		if (oldSlots == null) {
			return null;
		}

		Iterator<Slot> i = oldSlots.iterator();
		Slot next, nextCopy;
		Set<Slot> ownSlotsToIgnore = getOwnSlotsToIgnore(oldFrame.getKnowledgeBase());

		while (i.hasNext()) {
			next = i.next();
			// *** this is a hack for the moment
			// *** need to put better handling for included and import
			if (!ownSlotsToIgnore.contains(next) || Util.isSystem(next)) {
				_continueCopy = true;
				if (next.isIncluded()){
					nextCopy = _targetKb.getSlot(next.getName());
					if (nextCopy == null) continue;
				} else {
				nextCopy = checkExistingFrames(next, sourceKb, _targetKb);
				}

				if (_continueCopy) {
					if (nextCopy == null) {
//             nextCopy = (Slot)copyFrame (next, sourceKb, false);
						(new KeepSlotOperation(next, _traversalDirective.sameDirective(next))).performOperation();
						nextCopy = (Slot) Mappings.getWhatBecameOfIt(next);
					}
					// ** check for frames with the same name
//           if (oldFrame instanceof Cls)
					copySlotAttachmentInformation(oldFrame, next, newFrame, nextCopy, templateOrOwnSlots, copyOverrides);
					newSlots.add(nextCopy);
				}
			}
		}
		return newSlots;
	}

	private Slot checkExistingFrames(Slot slot, KnowledgeBase sourceKb, KnowledgeBase targetKb) {
		Slot nextSlot = checkMapping(slot, sourceKb);
		if (!_continueCopy) {
			return null;
		}
		if (nextSlot == null) {
			nextSlot = checkOtherFrames(slot, sourceKb, targetKb);
		}
		return nextSlot;
	}

	private Slot checkMapping(Slot slot, KnowledgeBase kb) {
		Frame mapping = Mappings.getWhatBecameOfIt(slot);
		if (mapping == null) {
			return null;
		}

		// mapping exists
		if (mapping instanceof Slot) {
			return (Slot) mapping;
		}

		//mapping is to a different frame type
		mappedToDifferentFrameType(slot, mapping);
		return null;
	}

	private Slot checkOtherFrames(Slot slot, KnowledgeBase sourceKb, KnowledgeBase targetKb) {
		Frame existing = targetKb.getFrame(slot.getName());
		if (existing == null) {
			return null;
		}
		Collection solution = null;
		if (existing.getClass().equals(slot.getClass()) && _temporary == false) {
			if (Warning.confirmToContinue(Warning.SLOT_EXISTS_AND_WILL_BE_ATTACHED, Warning.ASK_TO_COPY_SLOT, slot, existing)) {
				if (PromptTab.merging()) {
					solution = CollectionUtilities.createCollection(new MergeSlotsOperation(existing, slot, new AlreadyExists(existing)));
					_currentFoundConflicts.add(Conflict.duplicateFrameNames(existing, slot, solution));
				}
				return null;
			} else {
				return (Slot) existing;
			}
		}
		existsDifferentFrameType(slot, existing);
		return null;
	}

	private void mappedToDifferentFrameType(Slot slot, Frame mapping) {
		if (_temporary == false) {
			if (Warning.confirmToContinue(Warning.MAPPING_EXISTS, Warning.ASK_TO_COPY_SLOT, slot, mapping)) {
				if (PromptTab.merging()) {
					_currentFoundConflicts.add(Conflict.duplicateFrameNames(slot, mapping, null));
				} else {
					_continueCopy = false;
				}
			}
		}
	}

	private void existsDifferentFrameType(Slot slot, Frame existing) {
		if (_temporary == false) {
			if (Warning.confirmToContinue(Warning.FRAME_EXISTS, Warning.ASK_TO_COPY_SLOT, slot, existing)) {
				if (PromptTab.merging()) {
					_currentFoundConflicts.add(Conflict.duplicateFrameNames(slot, existing, null));
				}
			} else {
				_continueCopy = false;
			}
		}
	}

	private Collection<Slot> getSlots(Frame frame, boolean templateOrOwnSlots) {
		if (templateOrOwnSlots && frame instanceof Cls) {
			if (Preferences.considerInheritedSlots() || _localConsiderInheritedSlots) {
				return ((Cls) frame).getTemplateSlots();
			} else {
				return getLocalTemplateSlotsOnly((Cls) frame);
			}
		} else {
			return frame.getOwnSlots();
		}
	}

	protected Collection copyReferencedFrames(Collection c, KnowledgeBase targetKb) {
		return copyReferencedFrames(c, targetKb, _newFrame, false, null, false);
	}

	protected Collection copyReferencedFrames(Collection c, KnowledgeBase targetKb, boolean subclassesOrSuperclasses) {
		Collection result = copyReferencedFrames(c, targetKb, _newFrame, false, null, subclassesOrSuperclasses);
		return result;
	}

	protected boolean alreadyExists(Frame f, KnowledgeBase sourceKb) {
		Frame mapping = Mappings.getWhatBecameOfIt(f);
//Log.trace ("f = " + f + ", mapping = " + mapping, this, "doneIfExists");
		if (mapping != null) {
			boolean confirm;
			if (_traversalDirective.copyInstances()) {
				// assuming you would want to copy instances anyway
				confirm = !Warning.confirmToContinue(Warning.MAPPING_EXISTS, Warning.ASK_TO_COPY_INSTANCES, f, mapping);
			} else {
				confirm = Warning.confirmToContinue(Warning.MAPPING_EXISTS, Warning.ASK_TO_CONTINUE, f, mapping);
			}
			if (!confirm) {
				_newFrame = mapping;
			}
			return !confirm;
		} else {
			Frame existing = _targetKb.getFrame(f.getName());
//Log.trace ("existing = " + existing, this, "doneIfExists");
			if (existing != null && _temporary == false) {
				boolean confirm = performMergeOrDeclareConflict(existing, f);
				if (confirm) {
					_newFrame = existing;
				}
				return confirm;
			}
		}
		return false;
	}

	// returns true if it has taken care of everything; false if the caller needs to continue
	private boolean performMergeOrDeclareConflict(Frame existing, Frame f) {
		if (existing.getClass() == f.getClass()) {
			if (Warning.confirmToContinue(Warning.FRAME_EXISTS, Warning.SUGGEST_MERGE, f, existing)) {
				MergeFramesOperation delegate = MergeFramesOperation.selectMergeOperation(existing, f, new AlreadyExists(existing));
				delegate.performOperation();
				return true;
			} else {
				return false;
			}
		} else if (Warning.confirmToContinue(Warning.FRAME_EXISTS, Warning.ASK_TO_CONTINUE, f, existing)) {
			if (PromptTab.merging()) {
				_currentFoundConflicts.add(Conflict.duplicateFrameNames(existing, f, null));
			}
			return false;
		} else {
			return true;
		}
	}

	protected void setLocalConsiderInheritedSlots(boolean b) {
		_localConsiderInheritedSlots = b;
	}

/*
  public JPanel createEditBox () {
  	GetValueWidget [] argWidgets = Dispatcher.createValuesWidgetsImplementation(this);
    Object [] args = new Object [argWidgets.length];
    int length = args.length;
    args[0] = _args[0];
    if (length >= 1)
    	if (this  instanceof DeepCopyFrameOperation)
    		args[1] = new Boolean (true);
        else
        	args[1] = new Boolean (false);
    if (length >= 2)
    	args[2] = new Boolean (_copySubclasses);
    if (length >= 3)
    	args[3] = new Boolean (_copyInstances);
	JPanel panel = Operation.createEditBox(argWidgets, args);
	return panel;
  }
*/
}
