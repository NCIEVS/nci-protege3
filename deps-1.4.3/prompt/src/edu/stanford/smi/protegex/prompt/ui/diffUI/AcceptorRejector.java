
/*
 * Contributor(s): Sandhya Kunnatur kunnatur@smi.stanford.edu
 * 		           Abhita Chugh abhita@stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui.diffUI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.tree.TreePath;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.SystemFrames;
import edu.stanford.smi.protege.util.Assert;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.LazyTreeNode;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.FrameDifferenceElement;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.FrameStatus;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.ResultTable;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.TableRow;
import edu.stanford.smi.protegex.prompt.util.OWLUtil;
import edu.stanford.smi.protegex.prompt.util.Util;

public class AcceptorRejector
{
	
	private FrameStatus _frameStatuses = null;
	private boolean _isOWL;
	
	public AcceptorRejector()
	{
		_frameStatuses = PromptTab.getPromptDiff().getViewSetUp().getFrameStatuses();
		_isOWL = PromptTab.kbInOWL();
	}
	
	public void acceptChange(Cls cls,Cls parent)
	{
		Collection rowsWithFrame = PromptTab.getPromptDiff().getResultsTable().getRows(cls);
		
		FrameStatus frameStatuses = PromptTab.getPromptDiff().getViewSetUp().getFrameStatuses();
		
		TableRow firstRow = (TableRow)CollectionUtilities.getSoleItem(rowsWithFrame);
		
		if(firstRow.isChangeAccepted())
			return;
		
		firstRow.setChangeAccepted(true);
		
		if(firstRow.getOperationValue() == TableRow.OPERATION_ADD) {
			acceptAddedCls(cls,firstRow);
		}else if(firstRow.getOperationValue() == TableRow.OPERATION_DELETE) {
			acceptDeletedCls(cls,firstRow);
		}else if(firstRow.getOperationValue() == TableRow.OPERATION_MAP) {
			
//			FrameStatus frameStatuses = PromptTab.getPromptDiff().getViewSetUp().getFrameStatuses();
			int frameStatus = frameStatuses.getFrameStatus(cls,parent);
			//Log.getLogger().info(frameStatus);
			if(frameStatus == FrameStatus.FRAME_MOVED_FROM || 
					frameStatus == FrameStatus.FRAME_MOVED_TO){
				acceptMovedCls(cls,parent,firstRow);		    
			}
			
			Object[] diffElements = firstRow.getOperationExplanation().toArray();
			for(int diffNum =0; diffNum <diffElements.length; diffNum++){
				FrameDifferenceElement diffEl = (FrameDifferenceElement)diffElements[diffNum];
				if(diffEl.getChangeLevel() == FrameDifferenceElement.LEVEL_DIRECT)
					acceptChange(cls,diffEl);
			}
			
		}
		//Log.getLogger().info(frameStatuses.getFrameStatus(cls,parent));
	}
	
	public void acceptTreeChange(Cls cls,Cls parent)
	{
		Collection rowsWithFrame = PromptTab.getPromptDiff().getResultsTable().getRows(cls);
//		Iterator i = rowsWithFrame.iterator();
//		while(i.hasNext()) {
//		Log.getLogger().info(i.next());	
//		}
		TableRow firstRow = (TableRow)CollectionUtilities.getSoleItem(rowsWithFrame);
		
		FrameStatus frameStatuses = PromptTab.getPromptDiff().getViewSetUp().getFrameStatuses();
		int treeStatus = frameStatuses.getTreeStatus(cls,parent);
		switch(treeStatus){
		case FrameStatus.TREE_ADDED :
			acceptAddedSubtree(cls);
			break;  
			
		case FrameStatus.TREE_DELETED :
			acceptTreeDeletion(cls);
			break;
			
		case FrameStatus.TREE_MOVED_FROM:
		case FrameStatus.TREE_MOVED_TO:
			acceptTreeMove(cls,parent);
			break;
			
		case FrameStatus.TREE_UNCHANGED:
//			Log.getLogger().info("Tree Unchanged");
			return;
			
		default :
			acceptHeterogeneousTreeChange(cls,parent);
		return;
		}
		
	}
	
	private void acceptDeletedCls(Cls cls,TableRow firstRow)
	{
		//note--can change getDiffChildObjects so that deleted classes that are accepted are not added
		//firstRow.setChangeAccepted(true);
		
		//move subclasses to parent
		//delete this class
		//
		//update statuses
//		Log.getLogger().info("Accepting delete " + firstRow);
		firstRow.setChangeAccepted(true);
		
		//accept all instances
		Collection instances = cls.getDirectInstances();
		for(Iterator iter=instances.iterator();iter.hasNext();){
			Instance instance = (Instance)iter.next();
//			Log.getLogger().info("Accepting deleted instance" + instance);
			TableRow row = getSoleRow(instance);
			acceptReferenceChanges(instance);
			row.setChangeAccepted(true);
		}
		
		//for each subclass move it to parent
		Object subClasses[] = Util.getDirectSubclasses(cls).toArray();
		Object superClasses[] = Util.getDirectSuperclasses(cls).toArray();
		Cls superClassMap[] = new Cls[superClasses.length];
		
		for(int i = 0;i<superClasses.length;i++) {
			superClassMap[i] = (Cls)Util.getMap((Cls)superClasses[i],PromptTab.getPromptDiff().getResultsTable());
		}
		for(int i = 0;i<subClasses.length;i++) {
			Cls child = (Cls)subClasses[i];
			for(int j = 0;j<superClasses.length;j++) {
				Cls parent = (Cls)superClasses[j];	
				child.addDirectSuperclass(parent);
				_frameStatuses.notifyChildAdded((superClassMap[j] != null)?superClassMap[j]:parent,child);
			}
			child.removeDirectSuperclass(cls);
//			Log.getLogger().info("Moved subclass " + child);
		}
		
		Collection slots = cls.getDirectTemplateSlots();
		for(Iterator iter = slots.iterator();iter.hasNext();) {
			Slot slot = (Slot)iter.next();
			TableRow row = (TableRow)CollectionUtilities.getSoleItem(PromptTab.getPromptDiff().getResultsTable().getRows(slot));
			FrameDifferenceElement diffElement = row.getOperationExplanation(cls);
			if(diffElement != null){
				row.removeOperationExplanation(diffElement);
			}
		}
		
		firstRow.removeAddOrMoveOrDelete();
//		for(int i = 0;i<superClasses.length;i++){
//		if(superClassMap[i] != null)
//		_frameStatuses.notifyChildRemoved(superClassMap[i],cls);
//		else
//		_frameStatuses.notifyChildRemoved((Cls)superClasses[i],cls);
//		}
	}
	
	private void acceptMovedCls(Cls cls,Cls parent,TableRow firstRow)
	{
//		Log.getLogger().info("Accepting move");
		firstRow.setChangeAccepted(true);
		
		Collection slots = cls.getDirectTemplateSlots();
		Iterator slotIterator = slots.iterator();
		while(slotIterator.hasNext()){
			Slot slot = (Slot)slotIterator.next();
//			Log.getLogger().info(slot);
			acceptSlotAttachment(slot,cls);
		}
		
		Object[] opExpln = firstRow.getOperationExplanation().toArray();
		for(int i = 0;i<opExpln.length;i++) {
			
			FrameDifferenceElement diffEl = (FrameDifferenceElement)opExpln[i];
			if(diffEl.getRelationshipToFrame() == FrameDifferenceElement.SUPERCLASS) {
				firstRow.removeOperationExplanation(diffEl, parent);
				switch(diffEl.getOperation()) {
				
				case FrameDifferenceElement.OP_DELETED :
				case FrameDifferenceElement.OP_CHANGED :
					Cls oldCls = (Cls)diffEl.getO1Value();
					Cls oldParent = (Cls)Util.getMap(oldCls,PromptTab.getPromptDiff().getResultsTable());
					if(oldParent == null)
						oldParent = oldCls;
					_frameStatuses.notifyChildRemoved(oldParent,cls);
					break;
				}
			}
		}
//		updateParentsOnAcceptReject(cls);
		firstRow.removeAddOrMoveOrDelete();
		_frameStatuses.notifyClsUpdated(cls);
	}
//	private void updateParentsOnAcceptReject(Cls cls)
//	{
//	//for each parent...get row,remove opexpln and update status
//	Collection superClasses = Util.getDirectSuperclasses(cls);
//	for(Iterator parentIter = superClasses.iterator();parentIter.hasNext();) {
//	Cls parent = (Cls)parentIter.next();
//	TableRow parentRow = getSoleRow(parent);
//	FrameDifferenceElement diffElement = parentRow.getOperationExplanation(cls);
//	parentRow.removeOperationExplanation(diffElement);
//	
//	if(parentRow.getOperationValue() == TableRow.OPERATION_MAP && 
//	parentRow.getOperationExplanation().isEmpty()) {
//	for(Iterator grandparentIter = Util.getDirectSuperclasses(parent).iterator();
//	grandparentIter.hasNext();) {
//	Cls grandparent = (Cls)grandparentIter.next();
//	TableRow grandparentRow = (TableRow)CollectionUtilities.getSoleItem(PromptTab.getPromptDiff().getResultsTable().getRows(grandparent));
//	FrameDifferenceElement diffEl = grandparentRow.getOperationExplanation(parent);
//	grandparentRow.removeOperationExplanation(diffEl);
//	}
//	}
//	}
//	
//	}
	
	private void acceptAddedCls(Cls cls,TableRow firstRow)
	{
//		Log.getLogger().info("Accepting add");
		firstRow.setChangeAccepted(true);
		
		Collection slots = cls.getDirectTemplateSlots();
		Iterator slotIterator = slots.iterator();
		while(slotIterator.hasNext()){
			Slot slot = (Slot)slotIterator.next();
//			Log.getLogger().info(slot);
			acceptSlotAttachment(slot,cls);
		}
		
		firstRow.removeAddOrMoveOrDelete();
		_frameStatuses.notifyClsUpdated(cls);
	}
	
	private void acceptAddedSubtree(Cls cls)
	{
		acceptAddedCls (cls, getSoleRow(cls));
		Collection subClses = Util.getDirectSubclasses(cls);
		for(Iterator subClsIter = subClses.iterator();subClsIter.hasNext();){
			Cls subCls = (Cls)subClsIter.next();
			acceptAddedSubtree(subCls);
		}
	}
	
	private void acceptTreeDeletion(Cls cls)
	{
//		Log.getLogger().info("Accepting delete");
		
		acceptDeletedSubtree(cls);
//		updateParentsOnAcceptReject(cls);
	}
	private void acceptDeletedSubtree(Cls cls)
	{	
//		Log.getLogger().info("Accepting delete ");
		
		//accept all instances
		Collection instances = cls.getDirectInstances();
		for(Iterator iter=instances.iterator();iter.hasNext();){
			Instance instance = (Instance)iter.next();
//			Log.getLogger().info("Accepting deleted instance " + instance);
			TableRow row = getSoleRow(instance);
			row.setChangeAccepted(true);
		}
		
		Collection slots = cls.getDirectTemplateSlots();
		for(Iterator iter = slots.iterator();iter.hasNext();) {
			Slot slot = (Slot)iter.next();
			TableRow row = (TableRow)CollectionUtilities.getSoleItem(PromptTab.getPromptDiff().getResultsTable().getRows(slot));
			FrameDifferenceElement diffElement = row.getOperationExplanation(cls);
			if(diffElement != null){
				row.removeOperationExplanation(diffElement);
			}
		}
		
		getSoleRow(cls).setChangeAccepted(true);
		Collection subClses = Util.getDirectSubclasses(cls);
		for(Iterator subClsIter = subClses.iterator();subClsIter.hasNext();){
			Cls subCls = (Cls)subClsIter.next();
			acceptDeletedSubtree(subCls);
		}
		
		Collection superClses = Util.getDirectSuperclasses(cls);
		for(Iterator superClsIter = superClses.iterator();superClsIter.hasNext();){
			Cls superCls = (Cls)superClsIter.next();
			Cls parent = (Cls)Util.getMap(superCls,PromptTab.getPromptDiff().getResultsTable());
			if(parent == null)
				parent = superCls;
			_frameStatuses.notifyChildRemoved(parent,cls);
		}
	} 
	
	private void acceptTreeMove(Cls cls,Cls parent) 
	{
//		Log.getLogger().info("Accepting Tree move");
		
		acceptMovedSubtree(cls,parent);
//		updateParentsOnAcceptReject(cls);
	}
	
	private void acceptMovedSubtree(Cls cls,Cls parent)
	{
		TableRow firstRow = getSoleRow(cls);
		if(firstRow.isChangeAccepted())
			return;
		firstRow.setChangeAccepted(true);
		
		acceptMovedCls(cls,parent,firstRow);
		Collection children = Util.getDiffChildObjects(cls,PromptTab.getPromptDiff().getResultsTable());
		for(Iterator iterator = children.iterator();iterator.hasNext(); ) {
			Cls child = (Cls)iterator.next();
			acceptMovedSubtree(child,cls);
		}
	}
	
	private void acceptHeterogeneousTreeChange(Cls cls,Cls parent)
	{
		if(_frameStatuses.getTreeStatus(cls,parent) == FrameStatus.TREE_UNCHANGED)
			return;
		
		Collection diffChildren = Util.getDiffChildObjects(cls,PromptTab.getPromptDiff().getResultsTable());
		for(Iterator iter = diffChildren.iterator();iter.hasNext(); ) {
			Cls child = (Cls)iter.next();
			acceptTreeChange(child,cls);
		}
		
		acceptChange(cls,parent);
	}
	
	private void acceptSlotAttachment(Slot slot,Cls cls)
	{
		Collection slotRows = PromptTab.getPromptDiff().getResultsTable().getRows(slot);
		TableRow slotRow = (TableRow)CollectionUtilities.getSoleItem(slotRows);
		FrameDifferenceElement diffElement = slotRow.getOperationExplanation(cls);	
		slotRow.removeOperationExplanation(diffElement);  
	}
	
	public TableRow getSoleRow(Frame frame){
		return (TableRow) CollectionUtilities.getSoleItem(PromptTab.getPromptDiff().getResultsTable().getRows(frame));
	}
	
	public void rejectChange(Cls cls,Cls parent)
	{
		Collection rowsWithFrame = PromptTab.getPromptDiff().getResultsTable().getRows(cls);
		TableRow firstRow = (TableRow)CollectionUtilities.getSoleItem(rowsWithFrame);
		
		if(firstRow.isChangeAccepted())
			return;
		
//		firstRow.setChangeAccepted(true);
		
		if(firstRow.getOperationValue() == TableRow.OPERATION_ADD){
			rejectAddClass(cls,parent,firstRow);
		}else if(firstRow.getOperationValue() == TableRow.OPERATION_DELETE){
			rejectDeleteClass(cls,firstRow);
		}else if(firstRow.getOperationValue() == TableRow.OPERATION_MAP) {
			
			FrameStatus frameStatuses = PromptTab.getPromptDiff().getViewSetUp().getFrameStatuses();
			int frameStatus = frameStatuses.getFrameStatus(cls,parent);
			
			if(frameStatus == FrameStatus.FRAME_MOVED_TO ||
					frameStatus == FrameStatus.FRAME_MOVED_FROM) {
				rejectMove(cls,firstRow);
			}
			
			Collection diffEls = new ArrayList(firstRow.getOperationExplanation());
			for(Iterator iter = diffEls.iterator(); iter.hasNext(); ){
				FrameDifferenceElement diffEl = (FrameDifferenceElement)iter.next();
				if(diffEl.getChangeLevel() == FrameDifferenceElement.LEVEL_DIRECT){
					rejectChange(cls,diffEl);
				}
			}
			
		}
		
	}
	
	private Collection movedToWhere(Cls cls,TableRow row)
	{
		Collection to = new Vector();
		
		Collection diffElements = row.getOperationExplanation();
		for(Iterator diffIter = diffElements.iterator();diffIter.hasNext();) {
			FrameDifferenceElement diffElement = (FrameDifferenceElement)diffIter.next();
			if(diffElement.getRelationshipToFrame() == FrameDifferenceElement.SUPERCLASS 
					&& (diffElement.getOperation() == FrameDifferenceElement.OP_ADDED 
							|| diffElement.getOperation() == FrameDifferenceElement.OP_CHANGED)) {
				to.add(diffElement.getO2Value());
			}
		}
		return to;
	}
	
	private void rejectMove(Cls cls,TableRow firstRow)
	{
		firstRow.setChangeAccepted(true);
		Collection opExpln = firstRow.getOperationExplanation();
		Collection delExplns = new Vector();
		
		for(Iterator diffIter = opExpln.iterator();diffIter.hasNext();) {
			FrameDifferenceElement diffEl = (FrameDifferenceElement)diffIter.next();
			if(diffEl.getRelationshipToFrame() == FrameDifferenceElement.SUPERCLASS) {
				int operation = diffEl.getOperation();
				if(operation == FrameDifferenceElement.OP_ADDED ||
						operation == FrameDifferenceElement.OP_CHANGED){
					Cls newCls = (Cls)diffEl.getO2Value();
					cls.removeDirectSuperclass(newCls);
					_frameStatuses.notifyChildRemoved(newCls,cls);
				}
				
				if(operation == FrameDifferenceElement.OP_DELETED ||
						operation == FrameDifferenceElement.OP_CHANGED){
					Cls oldCls = (Cls)diffEl.getO1Value();
					Cls parentMap = (Cls)Util.getMap(oldCls,PromptTab.getPromptDiff().getResultsTable());
					if(parentMap == null){
						Log.getLogger().warning ("Warning: Could not map " + oldCls);
						continue;
					}
					cls.addDirectSuperclass(parentMap);
				}
				
				delExplns.add(diffEl);
			}
			
		}
		
		for(Iterator i = delExplns.iterator();i.hasNext(); ) {
			FrameDifferenceElement diffEl = (FrameDifferenceElement)i.next();
			
			firstRow.removeOperationExplanation(diffEl);
		}
		
		_frameStatuses.notifyClsUpdated(cls);
		
		
	}
	private void rejectAddClass(Cls cls,Cls parentCls,TableRow firstRow)	
	{	
		DiffSubclassPane subClsPane = ((DiffTabComponent)PromptTab.getTabComponent()).getTreeView().getSubclassPane();
//		Log.getLogger().info("Rejecting add " + firstRow);
		firstRow.setChangeAccepted(true);
		TreePath selectionPath = subClsPane.getPathToSelection();
		TreePath parentPath = (selectionPath == null) ? null : subClsPane.getPathToSelection().getParentPath();
		
		KnowledgeBase kb = cls.getKnowledgeBase();
		//remove all instances
		Collection instances = cls.getDirectInstances();
		for(Iterator iter=instances.iterator();iter.hasNext();){
			Instance instance = (Instance)iter.next();
//			Log.getLogger().info("Deleting instance " + instance);
			kb.deleteFrame(instance);
			TableRow row = getSoleRow(instance);
			PromptTab.getPromptDiff().getResultsTable().removeElement(row);
		}
		
		//for each subclass move it to parent
		Object subClasses[] = Util.getDirectSubclasses(cls).toArray();
		Object superClasses[] = Util.getDirectSuperclasses(cls).toArray();
		
		for(int i = 0;i<subClasses.length;i++) {
			Cls child = (Cls)subClasses[i];
			for(int j = 0;j<superClasses.length;j++) {
				Cls parent = (Cls)superClasses[j];	
				child.addDirectSuperclass(parent);
				_frameStatuses.notifyChildAdded(parent,child);
			}
			child.removeDirectSuperclass(cls);
//			Log.getLogger().info("Moved subclass " + child);
		}
		
		Collection slots = cls.getDirectTemplateSlots();
		for(Iterator iter = slots.iterator();iter.hasNext();) {
			Slot slot = (Slot)iter.next();
			TableRow row = (TableRow)CollectionUtilities.getSoleItem(PromptTab.getPromptDiff().getResultsTable().getRows(slot));
			FrameDifferenceElement diffElement = row.getOperationExplanation(cls);
			if(diffElement != null){
				row.removeOperationExplanation(diffElement, parentCls);
			}
		}
		
//		updateParentsOnAcceptReject(cls);
		TableRow row = (TableRow)CollectionUtilities.getSoleItem(PromptTab.getPromptDiff().getResultsTable().getRows(cls));
		PromptTab.getPromptDiff().getResultsTable().removeElement(row);
		
		for(int i = 0;i<superClasses.length;i++) {
			cls.removeDirectSuperclass((Cls)superClasses[i]);
			_frameStatuses.notifyChildRemoved((Cls)superClasses[i],cls);
		}
		
		if (parentPath != null)
			subClsPane.setSelectedCls(parentCls,parentPath);
		
		kb.deleteFrame(cls);
		
	}
	
	private Cls CopyCls(KnowledgeBase destKb,Cls cls)
	{
		Cls newCls = null;
		try {
//			Log.getLogger().info("Copying class " + cls);
			
			Collection superClasses = Util.getDirectSuperclasses(cls);
			Collection parents = new Vector();
			
			for(Iterator superClassIter = superClasses.iterator();superClassIter.hasNext();) {
				Cls superClass = (Cls)superClassIter.next();
				TableRow row = getSoleRow(superClass);
				if(row == null || row.getF2Value() == null){
					Log.getLogger().severe("Aborting -- couldnot map superclass " + superClass);
					throw new CopyFailureException();
				}else{
					parents.add(row.getF2Value());			
				}
			}
			
			newCls = destKb.createCls(cls.getName(),parents);
			
			Collection slots = cls.getDirectTemplateSlots();
			for(Iterator slotIterator = slots.iterator();slotIterator.hasNext();){
				Slot slot = (Slot)slotIterator.next();
				TableRow row = getSoleRow(slot);
				
				if(row == null || row.getF2Value() == null){
					Log.getLogger().warning("Could not map slot " + slot);
				}else {
					newCls.addDirectTemplateSlot((Slot)row.getF2Value());
				}
			}
			
			/*newCls.addDirectType();
			 newCls.addOwnFacetValue();
			 newCls.addOwnSlotValue();
			 newCls.addTemplateFacetValue();
			 newCls.addTemplateSlotValue();
			 */
			return newCls;
		} catch (CopyFailureException e) {
			if(newCls != null) destKb.deleteCls(newCls);
			return null;
		}
	}
	
	private void rejectDeleteClass(Cls cls,TableRow firstRow)
	{
//		Log.getLogger().info("Rejecting delete " + firstRow);
		KnowledgeBase kb = cls.getKnowledgeBase();
		KnowledgeBase newKb = PromptTab.getPromptDiff().getResultsTable().getKb2();
		
		//copy class
		Cls newCls = CopyCls(newKb,cls);
		if(newCls == null){
			Log.getLogger().warning("Couldnot replicate cls" + cls);
			return;
		}
		
		Collection slots = cls.getDirectTemplateSlots();
		for(Iterator iter = slots.iterator();iter.hasNext();) {
			Slot slot = (Slot)iter.next();
			TableRow row = getSoleRow(slot);
			FrameDifferenceElement diffElement = row.getOperationExplanation(cls);
			if(diffElement != null){
				row.removeOperationExplanation(diffElement);
			}
		}	
		
		TableRow newRow = new TableRow(cls,newCls);
		PromptTab.getPromptDiff().getResultsTable().removeElement(firstRow);
		PromptTab.getPromptDiff().getResultsTable().addElement(newRow);
		newRow.setChangeAccepted(true);
		newRow.removeAddOrMoveOrDelete();
//		_frameStatuses.clearStatusDeep(cls);
		
		Collection superClasses = Util.getDirectSuperclasses(cls);
		for(Iterator superClassIter = superClasses.iterator(); superClassIter.hasNext();) {
			Cls parent = (Cls)superClassIter.next();
			TableRow row = getSoleRow(parent);
			FrameDifferenceElement diffElement = row.getOperationExplanation(cls);
			if(diffElement != null){
//				Log.getLogger().info("Removing diff element " + diffElement );
				row.removeOperationExplanation(diffElement);
			}
			Cls parentMap = (Cls)Util.getMap(parent,PromptTab.getPromptDiff().getResultsTable());
			if(parentMap == null)
				parentMap = parent;
			
			_frameStatuses.notifyChildAdded(parentMap,newCls);
			_frameStatuses.notifyChildRemoved(parentMap,cls);
			
		}
		
		
		
//		todo:add instances ?
		
	}
	
	private Cls getCls(TreePath path,int componentNum)
	{
		if(componentNum < 0)
			return null;
		
		LazyTreeNode node = (LazyTreeNode)path.getPathComponent(componentNum);
		Object o = node.getUserObject();
		if(o == null || o instanceof Cls)
			return (Cls)o;
		
		return null;
	}
	
	public void rejectTreeChange(Cls cls,Cls parent)
	{
		DiffSubclassPane subClsPane = ((DiffTabComponent)PromptTab.getTabComponent()).getTreeView().getSubclassPane();
		TreePath path = subClsPane.getPathToSelection();
		int pathCount = path.getPathCount();
		//Cls parent = getCls(path,pathCount-2);
		Collection rowsWithFrame = PromptTab.getPromptDiff().getResultsTable().getRows(cls);
//		Iterator i = rowsWithFrame.iterator();
//		while(i.hasNext()) {
//		Log.getLogger().info(i.next());	
//		}
		TableRow firstRow = (TableRow)CollectionUtilities.getSoleItem(rowsWithFrame);
		
		FrameStatus frameStatuses = PromptTab.getPromptDiff().getViewSetUp().getFrameStatuses();
		int treeStatus = frameStatuses.getTreeStatus(cls,parent);
		TreePath parentPath;
		Cls parentCls;
		switch(treeStatus){
		case FrameStatus.TREE_ADDED :
			parentPath = subClsPane.getPathToSelection().getParentPath();
			parentCls = getCls(parentPath,parentPath.getPathCount()-1);
			Collection deletedClasses = new Vector();
			rejectTreeAddition(cls,deletedClasses);
			subClsPane.setSelectedCls(parentCls,parentPath);				
			KnowledgeBase kb = cls.getKnowledgeBase();
			for(Iterator clsIter = deletedClasses.iterator();clsIter.hasNext();){
				Cls delCls = (Cls)clsIter.next();
//				Log.getLogger().info("Purging "+delCls);
				kb.deleteCls(delCls);
			}
			break;
			
		case FrameStatus.TREE_DELETED :
			rejectTreeDeletion(cls);
			break;				
			
			
		default :
			rejectGenericTreeChange (cls, parent);
		//*** perhaps come back to this later
//		Log.warning("Invalid action", this, "rejectTreeChange");
		return;
		}
		
	}
	
	private void rejectGenericTreeChange (Cls cls, Cls parent) {
//		Log.trace ("cls = " + cls.getBrowserText() + ", parent = " + parent + ", tree status = " + _frameStatuses.getTreeStatus(cls,parent), this, "rejectGenericTreeChange");
		if(_frameStatuses.getTreeStatus(cls,parent) == FrameStatus.TREE_UNCHANGED)
			return;
		
		Collection diffChildren = Util.getDiffChildObjects(cls,PromptTab.getPromptDiff().getResultsTable());
		for(Iterator iter = diffChildren.iterator();iter.hasNext(); ) {
			Cls child = (Cls)iter.next();
//			Log.trace ("child = " + child.getBrowserText(), this, "rejectGenericTreeChange");
			rejectTreeChange(child,cls);
		}
		rejectChange(cls, parent);
	}
	
	private void rejectTreeAddition(Cls cls,Collection deletedClses)
	{
//		Log.getLogger().info("Rejecting Subtree add");
		
		//for each parent...get row,remove opexpln and update status
		Collection superClasses = Util.getDirectSuperclasses(cls);
		rejectAddedSubtree(cls,deletedClses);
	}
	
	private void rejectAddedSubtree(Cls cls,Collection deletedClses)
	{
		TableRow row = getSoleRow(cls);
		KnowledgeBase kb = cls.getKnowledgeBase();
		
		row.setChangeAccepted(true);
		Object[] subClses = Util.getDirectSubclasses(cls).toArray();
		for(int i =0;i < subClses.length;i++){
			rejectAddedSubtree((Cls)subClses[i],deletedClses);
		}
		
		Collection slots = cls.getDirectTemplateSlots();
		Iterator slotIterator = slots.iterator();
		while(slotIterator.hasNext()){
			Slot slot = (Slot)slotIterator.next();
			TableRow slotRow = getSoleRow(slot);
			row.removeOperationExplanation(slotRow.getOperationExplanation(slot));
		}
		
		Collection instances = cls.getDirectInstances();
		for(Iterator iter=instances.iterator();iter.hasNext();){
			Instance instance = (Instance)iter.next();
//			Log.getLogger().info("Deleting instance " + instance);
			kb.deleteFrame(instance);
			TableRow instanceRow = getSoleRow(instance);
			PromptTab.getPromptDiff().getResultsTable().removeElement(instanceRow);
		}
//		updateParentsOnAcceptReject(cls);
		row.removeAddOrMoveOrDelete();
		PromptTab.getPromptDiff().getResultsTable().removeElement(row);
		
		Object[] superClses = Util.getDirectSuperclasses(cls).toArray();
		for(int i = 0;i<superClses.length;i++) {
			Cls superCls = (Cls)superClses[i];
			cls.removeDirectSuperclass(superCls);
			_frameStatuses.notifyChildRemoved(superCls,cls);
		}
		
//		Log.getLogger().info("Deleting class " + cls);
		deletedClses.add(cls);
		
	}
	
	private void rejectTreeDeletion(Cls cls)
	{
//		Log.getLogger().info("Rejecting delete");
		
		try{
			ClsDependenceGraph depGraph = new ClsDependenceGraph(cls,PromptTab.getPromptDiff().getResultsTable());
//			depGraph.printTable();
			Vector clsList = depGraph.getPartiallyOrderedSet();
//			Log.getLogger().info(clsList);
			
			KnowledgeBase newKb = PromptTab.getPromptDiff().getResultsTable().getKb2(); 
			for(Iterator i = clsList.iterator();i.hasNext();){
				
				Cls targetCls = (Cls)i.next();
				Cls newCls = CopyCls(newKb,targetCls);
//				Log.getLogger().info("Replicating class " + targetCls);
				if(newCls == null){
					Log.getLogger().warning ("Could not replicate cls" + targetCls);
					continue;
				}
				
//				updateParentsOnAcceptReject(targetCls);
				
				TableRow newRow = new TableRow(targetCls,newCls);
				newRow.setChangeAccepted(true);
				newRow.removeAddOrMoveOrDelete();
				PromptTab.getPromptDiff().getResultsTable().removeElement(getSoleRow(targetCls));
				PromptTab.getPromptDiff().getResultsTable().addElement(newRow);
				Collection slots = targetCls.getDirectTemplateSlots();
				for(Iterator iter = slots.iterator();iter.hasNext();) {
					Slot slot = (Slot)iter.next();
					TableRow row = getSoleRow(slot);
					FrameDifferenceElement diffElement = row.getOperationExplanation(targetCls);
					if(diffElement != null){
						row.removeOperationExplanation(diffElement);
					}
				}
				
				for(Iterator iter = Util.getDirectSuperclasses(newCls).iterator();iter.hasNext();) {
					Cls superCls = (Cls)iter.next();
					_frameStatuses.notifyChildRemoved(superCls,targetCls);
					_frameStatuses.notifyChildAdded(superCls,newCls);
				}
			}
		}catch(AlgoFailedException e){
			Log.getLogger().severe("Reject failed");
			return;
		}
	}
	
	public static boolean isSlotAdded(ResultTable diffTable,Cls cls, Slot slot){
		
		TableRow tableRow = (TableRow)CollectionUtilities.getSoleItem(diffTable.getRows(cls));
		FrameDifferenceElement diffElement = tableRow.getOperationExplanation(slot);
		if(diffElement != null && 
				diffElement.getChangeLevel() == FrameDifferenceElement.LEVEL_DIRECT &&
				diffElement.getOperation() == FrameDifferenceElement.OP_ADDED){
			
			return true;
		}
		
		return false;
		
	}
	
	public static boolean slotChanged(ResultTable diffTable,Cls cls,Slot slot){
		TableRow tableRow = (TableRow)CollectionUtilities.getFirstItem(diffTable.getRows(cls));
		return tableRow.slotChangedAtFrame (slot);
	}
	
	public static int getSlotStatus(ResultTable diffTable,Cls cls,Slot slot){
		
		TableRow tableRow = (TableRow)CollectionUtilities.getFirstItem(diffTable.getRows(cls));
		FrameDifferenceElement diffElement = tableRow.getOperationExplanation(slot);
		
		if(diffElement != null && 
				diffElement.getChangeLevel() == FrameDifferenceElement.LEVEL_DIRECT){
			
			return diffElement.getOperation();
			
		}	
		
		return 0;
	}
	
	public void acceptSlotChange(Cls cls,Slot slot){
		
		TableRow tableRow = getSoleRow(cls);
		FrameDifferenceElement diffElement = tableRow.getOperationExplanation(slot);
		
		if(diffElement != null){
			tableRow.removeOperationExplanation(diffElement);
			switch(diffElement.getOperation()){
			case FrameDifferenceElement.OP_DELETED :
				//cls in new kb, slot in oldkb
				acceptSlotDetachment(cls,slot);
				break;
				
			default:
				acceptSlotAttachment(slot,cls);
			break;
			}
			_frameStatuses.notifyClsUpdated(cls);
		}
	}
	
	
	private void rejectRestrictionChange(Cls cls, TableRow row, FrameDifferenceElement diffEl) {
		OWLUtil.rejectRestrictionChange(cls, row, diffEl);
		row.removeOperationExplanation(diffEl);
		_frameStatuses.notifyClsUpdated(cls);
	}
	
	private  void acceptRestrictionChange(Cls cls, FrameDifferenceElement diffEl) {
		OWLUtil.acceptRestrictionChange(cls, diffEl);	
		TableRow row = getSoleRow(cls);
		row.removeOperationExplanation(diffEl);
		_frameStatuses.notifyClsUpdated(cls);
	}
	
	public void rejectSlotChange(Cls cls,Slot slot){
		
		TableRow tableRow = getSoleRow(cls);
		FrameDifferenceElement diffElement = tableRow.getOperationExplanation(slot);
		
		if(diffElement != null){
			tableRow.removeOperationExplanation(diffElement);
			switch(diffElement.getOperation()){
			case FrameDifferenceElement.OP_DELETED :
				//cls in new kb, slot in oldkb
				Cls oldCls = (Cls)Util.getMap(cls,PromptTab.getPromptDiff().getResultsTable());
				if(oldCls != null && oldCls.hasDirectTemplateSlot(slot)){
					Slot newSlot = (Slot)Util.getMap(slot,PromptTab.getPromptDiff().getResultsTable());
					if(newSlot != null){
						cls.addDirectTemplateSlot(newSlot);
						TableRow clsRow = getSoleRow(cls);
						clsRow.removeOperationExplanation(clsRow.getOperationExplanation(slot));
						TableRow slotRow = getSoleRow(newSlot);
						slotRow.removeOperationExplanation(slotRow.getOperationExplanation(oldCls));
						
						copyFacets(cls,newSlot,oldCls,slot);
					}else{
						//slot not present in new knowledge base. copy???
						break;
					}
					copySlotValuesForInstances(cls,oldCls,newSlot,slot);
				}
				break;
				
			case FrameDifferenceElement.OP_ADDED :
				
				if(cls.hasDirectTemplateSlot(slot)){
					cls.removeDirectTemplateSlot(slot);
					TableRow clsRow = getSoleRow(cls);
					clsRow.removeOperationExplanation(clsRow.getOperationExplanation(slot));
					TableRow slotRow = getSoleRow(slot);
					slotRow.removeOperationExplanation(slotRow.getOperationExplanation(cls));
				}		
				break;
				
			default:
				Log.getLogger().info("todo");
			break;
			}
			_frameStatuses.notifyClsUpdated(cls);
		}
	}
	
	private void copySlotValuesForInstances(Cls newCls,Cls oldCls,Slot newSlot,Slot oldSlot){
		Iterator instanceIter = newCls.getDirectInstances().iterator();
		while(instanceIter.hasNext()){
			Instance newInstance = (Instance)instanceIter.next();
			Instance oldInstance = (Instance)Util.getMap(newInstance,PromptTab.getPromptDiff().getResultsTable());
			if(oldInstance == null)
				continue;
			
			Collection oldVals = oldInstance.getDirectOwnSlotValues(oldSlot);
			Collection newVals = new Vector();
			for(Iterator valIter = oldVals.iterator(); valIter.hasNext();){
				Object oldVal = valIter.next();
				Object valCopy = copyOwnSlotValue(newInstance,newSlot,oldSlot,oldVal);
				if(valCopy != null && newInstance.isValidOwnSlotValue(newSlot,valCopy)){
					newVals.add(valCopy);
				}
			}
			if(!newVals.isEmpty()){
				newInstance.setOwnSlotValues(newSlot,newVals);
			}
		}
	}
	
	public void acceptChange(Cls cls,FrameDifferenceElement diffEl){
		TableRow row = getSoleRow(cls);
		
		String relationship = diffEl.getRelationshipToFrame();
		if(relationship == FrameDifferenceElement.OWN_SLOT_VALUE){
			row.removeOperationExplanation(diffEl);
			SystemFrames systemFrames = cls.getKnowledgeBase().getSystemFrames();
			Slot nameSlot = systemFrames.getNameSlot();
			
			if(diffEl.getSlotValue().getName() == nameSlot.getName()){
				row.setRenameExplanation(null);
				PromptTab.getPromptDiff().getResultsTable().setRenameValue(row,TableRow.RENAME_MINUS);
				//Log.getLogger().info("do i come here?");
			}
		}else if(relationship == FrameDifferenceElement.FACET 
				|| relationship == FrameDifferenceElement.FACET_VALUE){
			row.removeOperationExplanation(diffEl);
		}else if(relationship == FrameDifferenceElement.TEMPLATE_SLOT){
			Slot slot = (Slot)diffEl.getO2Value();
			if(slot == null) 
				slot = (Slot)diffEl.getO1Value();
			acceptSlotChange(cls,slot);
		}else if(relationship == FrameDifferenceElement.SUPERCLASS){
			row.removeOperationExplanation(diffEl);
		} else if (relationship == FrameDifferenceElement.RESTRICTION) {
			acceptRestrictionChange (cls, diffEl);
		} else if (relationship == FrameDifferenceElement.TYPE || relationship == FrameDifferenceElement.META_CLASS || relationship == FrameDifferenceElement.META_SLOT) {
			row.removeOperationExplanation(diffEl);
		}
		else{
			//todo--what else could come here??
		}
		_frameStatuses.notifyClsUpdated(cls);
	}
	
	public void rejectChange(Cls cls,FrameDifferenceElement diffEl){
		
		TableRow row = getSoleRow(cls);
		
		String relationship = diffEl.getRelationshipToFrame();
		SystemFrames systemFrames = cls.getKnowledgeBase().getSystemFrames();
		
		Slot slot = diffEl.getSlotValue();
		Slot oldSlot = null,newSlot = null;
		
		if(slot != null){			
			if(slot.getKnowledgeBase() != cls.getKnowledgeBase()){
				oldSlot = slot;
				newSlot = (Slot)Util.getMap(slot,PromptTab.getPromptDiff().getResultsTable());
			}else{
				oldSlot = (Slot)Util.getMap(slot,PromptTab.getPromptDiff().getResultsTable());
				newSlot = slot;
			}
		}
		
		Facet facet = diffEl.getFacetValue();
		Facet oldFacet = null,newFacet = null;
		if(facet != null){			
			if(facet.getKnowledgeBase() != cls.getKnowledgeBase()){
				oldFacet = facet;
				newFacet = (Facet)Util.getMap(facet,PromptTab.getPromptDiff().getResultsTable());
			}else{
				oldFacet = (Facet)Util.getMap(facet,PromptTab.getPromptDiff().getResultsTable());
				newFacet = facet;
			}
		}
		
		if(relationship == FrameDifferenceElement.OWN_SLOT_VALUE){
			
			switch(diffEl.getOperation()){
			case FrameDifferenceElement.OP_ADDED :
			{
				Object value = diffEl.getO2Value();
				cls.removeOwnSlotValue(newSlot,value);
				break;
			}
			case FrameDifferenceElement.OP_DELETED :
			{
				Object value = diffEl.getO1Value();
				Object valueCopy = copyOwnSlotValue(cls,newSlot,oldSlot,value);
//				if (valueCopy == null) { // the old value is a frame that was deleted
//				AcceptorRejector.
//				}
//				Log.trace ("cls = " + cls + ", newSlot = " + newSlot + ", oldSlot = " + oldSlot + ", value = " + value + ", valueCopy = " + valueCopy,
//				AcceptorRejector.class, "rejectChange");
//				Log.trace ("cls kb = " + cls.getKnowledgeBase() + ", newSlot kb = " + newSlot.getKnowledgeBase() ,
//				AcceptorRejector.class, "rejectChange");
				cls.addOwnSlotValue(newSlot,valueCopy);
				break;
			}
			case FrameDifferenceElement.OP_CHANGED :
			{	
				if(newSlot.getName() == systemFrames.getNameSlot().getName()){
					Collection rows = PromptTab.getPromptDiff().getResultsTable().getRows(cls);
					PromptTab.getPromptDiff().getResultsTable().removeElements(rows);
					
					cls.rename((String)diffEl.getO1Value());
					PromptTab.getPromptDiff().getResultsTable().addElements(rows);
					row.setRenameExplanation(null);
					PromptTab.getPromptDiff().getResultsTable().setRenameValue(row,TableRow.RENAME_MINUS);
					//Log.getLogger().info("and here??"); 
//					_frameStatuses.notifyRenamed(cls,(String)diffEl.getO2Value(),(String)diffEl.getO1Value());						
					
				}else{
					Object oldValue = diffEl.getO1Value();
					Object newValue = diffEl.getO2Value();
					Object valueCopy = copyOwnSlotValue(cls,newSlot,oldSlot,oldValue);
					
					Collection slotValues = new ArrayList(cls.getDirectOwnSlotValues(newSlot));
					slotValues.remove(newValue);
					slotValues.add(valueCopy);
					cls.setOwnSlotValues(newSlot,slotValues);
				}	
				break;
			}
			}
			row.removeOperationExplanation(diffEl);
		}else if(relationship == FrameDifferenceElement.FACET
				|| relationship == FrameDifferenceElement.FACET_VALUE){
			
			switch(diffEl.getOperation()){
			case FrameDifferenceElement.OP_ADDED:{
				Collection c = new ArrayList(cls.getTemplateFacetValues(newSlot,newFacet));
				c.remove(diffEl.getO2Value());
				cls.setTemplateFacetValues(newSlot,newFacet,c);
				break;
			}
			case FrameDifferenceElement.OP_DELETED:{
				Collection c = new ArrayList(cls.getTemplateFacetValues(newSlot,newFacet));
				Object newVal = copyFacetValue(newFacet,oldFacet,diffEl.getO1Value());
				c.add(newVal);
				cls.setTemplateFacetValues(newSlot,newFacet,c);
				break;
			}
			case FrameDifferenceElement.OP_CHANGED:{
				Collection c = new ArrayList(cls.getTemplateFacetValues(newSlot,newFacet));
				c.remove(diffEl.getO2Value());
				Object newVal = copyFacetValue(newFacet,oldFacet,diffEl.getO1Value());
				c.add(newVal);
				cls.setTemplateFacetValues(newSlot,newFacet,c);
				break;
			}
			}
		}else if(relationship == FrameDifferenceElement.TEMPLATE_SLOT){
			Slot changedSlot = (Slot)diffEl.getO2Value();
			if(changedSlot == null) 
				changedSlot = (Slot)diffEl.getO1Value();
			rejectSlotChange(cls,changedSlot);			
		}else if (relationship == FrameDifferenceElement.RESTRICTION) {
			rejectRestrictionChange (cls, row, diffEl);			
		} else if (relationship == FrameDifferenceElement.TYPE || relationship == FrameDifferenceElement.META_CLASS || relationship == FrameDifferenceElement.META_SLOT) {
			rejectTypeChange (cls, row, diffEl);
		}
		else if (relationship == FrameDifferenceElement.SUPERCLASS) {
		    rejectSuperClassChange(cls, diffEl);
		}
		else{
			//todo
		}
		row.removeOperationExplanation(diffEl);
		_frameStatuses.notifyClsUpdated(cls);
		
	}
	
	private void rejectSuperClassChange(Cls cls, FrameDifferenceElement diffEl) {
	    Object o1 = diffEl.getO1Value();
	    Object o2 = diffEl.getO2Value();
        if (diffEl.getOperation() == FrameDifferenceElement.OP_ADDED 
                && o2 != null && o2 instanceof Cls) {
            Cls superCls = (Cls) o2;
            cls.removeDirectSuperclass(superCls);
            _frameStatuses.notifyChildRemoved(superCls, cls);
        }
        else if (diffEl.getOperation() == FrameDifferenceElement.OP_CHANGED &&
                    o1 != null && o1 instanceof Cls && 
                    o2 != null && o2  instanceof Cls) {
            Cls oldSuperCls = (Cls) o1;
            Cls newSuperCls = (Cls) o2;
            Cls oldSuperClsKb2 = (Cls) Util.getMap(oldSuperCls, PromptTab.getPromptDiff().getResultsTable());
            cls.removeDirectSuperclass(newSuperCls);
            cls.addDirectSuperclass(oldSuperClsKb2);
            _frameStatuses.notifyChildAdded(oldSuperCls, cls);
            _frameStatuses.notifyChildRemoved(newSuperCls, cls);
        }
	}
	
	private void rejectTypeChange (Cls cls, TableRow row, FrameDifferenceElement diffEl) {
		switch (diffEl.getOperation()) {
		case FrameDifferenceElement.OP_ADDED : {
			Object value = diffEl.getO2Value();
			cls.removeDirectType((Cls)value);
			break;
		}
		case FrameDifferenceElement.OP_DELETED : {
			Object value = diffEl.getO1Value();
			Object newType = PromptTab.getPromptDiff().getResultsTable().getFirstImage((Frame)value);
			if (newType != null)
				cls.addDirectType((Cls)newType);
			break;
		}
		case FrameDifferenceElement.OP_CHANGED :	{	
			Object value = diffEl.getO1Value();
			Object newType = PromptTab.getPromptDiff().getResultsTable().getFirstImage((Frame)value);
			if (newType != null)
				cls.addDirectType((Cls)newType);
			value = diffEl.getO2Value();
			cls.removeDirectType((Cls)value);
		}
		}
		
	}
	
	
	private void copyFacets(Cls newCls,Slot newSlot,Cls oldCls,Slot oldSlot){
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
	private Object copyOwnSlotValue(Instance newInstance,Slot newSlot,Slot oldSlot,Object value){
		if(value instanceof Frame){
			Frame mapping = Util.getMap((Frame)value,PromptTab.getPromptDiff().getResultsTable());
			return mapping;
		}
		return value;
	}
	
	private Object copyFacetValue(Facet newFacet,Facet oldFacet,Object value){
		if(value instanceof Frame){
			Frame mapping = Util.getMap((Frame)value,PromptTab.getPromptDiff().getResultsTable());
			return mapping;
		}
		
		return value;
	}
	//Cls in new kb, slot in old kb
	private void acceptSlotDetachment(Cls cls,Slot slot){
		Cls oldCls = (Cls)Util.getMap(cls,PromptTab.getPromptDiff().getResultsTable());
		Slot newSlot = (Slot)Util.getMap(slot,PromptTab.getPromptDiff().getResultsTable());
		if(oldCls != null && newSlot != null){
			TableRow row = getSoleRow(newSlot);
			FrameDifferenceElement diffEl = row.getOperationExplanation(oldCls);
			row.removeOperationExplanation(diffEl);
		}
	}
	
	public void acceptInstance(Cls cls,Instance instance){
		TableRow row = getSoleRow (instance);
		if(row.isChangeAccepted()){
			return;
		}
		String opval = row.getOperationValue();
		if(opval == TableRow.OPERATION_ADD){
			acceptAddedInstance(cls,instance);
		}else if(opval == TableRow.OPERATION_DELETE){
			acceptDeletedInstance(cls,instance);
		}else{
			acceptMappedInstance(cls,instance);
		}
	}
	private void acceptAddedInstance(Cls cls,Instance instance){
		TableRow row = getSoleRow(instance);
		row.setChangeAccepted(true);
		acceptReferenceChanges(instance);	
	}
	
	private void acceptDeletedInstance(Cls cls,Instance instance){
		TableRow row = getSoleRow(instance);
		row.setChangeAccepted(true);	
		
		acceptReferenceChanges(instance);
	}
	
	private void acceptReferenceChanges(Instance instance){
		deleteReferenceChangeElements(instance);
	}
	
	private void deleteReferenceChangeElements(Instance instance){
		
		Collection references = instance.getReferences();
		
		for(Iterator refIter = references.iterator();refIter.hasNext();){
			Reference ref = (Reference)refIter.next();
			Frame frame = ref.getFrame();
			Slot slot = ref.getSlot();
			
			deleteReferenceChange(frame,slot,instance);
		}
	}
	
	private void deleteReferenceChange(Frame frame,Slot slot,Instance instance){
		Slot slotMap = (Slot)Util.getMap(slot,PromptTab.getPromptDiff().getResultsTable());
		
		TableRow row = getSoleRow(frame);
		for(Iterator diffElIter = row.getOperationExplanation().iterator();diffElIter.hasNext();){
			FrameDifferenceElement diffEl = (FrameDifferenceElement)diffElIter.next();
			if((diffEl.getSlotValue() == slot  || diffEl.getSlotValue() == slotMap) && 
					(diffEl.getO1Value() == instance || diffEl.getO2Value() == instance)){
				
				row.removeSlotOperationExplanation(diffEl);
				break;		
			}
		}
		
	} 	
	
	private void acceptMappedInstance(Cls cls,Instance instance){
		TableRow row = getSoleRow(instance);
		Object[] diffElements = row.getOperationExplanation().toArray();
		for(int diffNum =0; diffNum <diffElements.length; diffNum++){
			FrameDifferenceElement diffEl = (FrameDifferenceElement)diffElements[diffNum];
			if(diffEl.getChangeLevel() == FrameDifferenceElement.LEVEL_DIRECT)
				acceptChangeInInstance(cls,instance,diffEl);
		}
		row.setChangeAccepted(true);	
	}
	
	public void acceptChangeInInstance(Cls cls,Instance instance,FrameDifferenceElement diffEl){
		TableRow row = getSoleRow(instance);
		row.removeInstanceOperationExplanation(diffEl);
		
	}
	
	public void rejectInstance(Cls cls,Instance instance){
		TableRow row = getSoleRow (instance);
		if(row.isChangeAccepted()){
			return;
		}
		String opval = row.getOperationValue();
		if(opval == TableRow.OPERATION_ADD){
			rejectAddedInstance(cls,instance);
		}else if(opval == TableRow.OPERATION_DELETE){
			rejectDeletedInstance(cls,instance);
		}else{
			rejectMappedInstance(cls,instance);
		}
	}
	
	private void rejectAddedInstance(Cls cls,Instance instance){
		TableRow row = getSoleRow(instance);
		deleteReferenceChangeElements(instance);
		instance.getKnowledgeBase().deleteFrame(instance);
	}
	
	private void rejectDeletedInstance(Cls cls,Instance instance){
		TableRow row = getSoleRow(instance);
		Instance newInstance = CopyInstance(PromptTab.getPromptDiff().getResultsTable().getKb2(),instance);
		if(newInstance == null){
			Log.getLogger().info("Reject failed : Couldnot copy instance " + instance.getName());
		}
		TableRow newRow = new TableRow(instance,newInstance);
		PromptTab.getPromptDiff().getResultsTable().removeElement(row);
		PromptTab.getPromptDiff().getResultsTable().addElement(newRow);
		newRow.setChangeAccepted(true);
		reinstateReferences(instance,newInstance);	
	}
	
	private void reinstateReferences(Instance oldInstance,Instance newInstance){
		Iterator refIter = oldInstance.getReferences().iterator();
		
		while(refIter.hasNext()){
			Reference ref = (Reference)refIter.next();
			Frame frame = ref.getFrame();
			Slot slot = ref.getSlot();
			Facet facet = ref.getFacet();
			
			Frame newFrame = (Frame)Util.getMap(frame,PromptTab.getPromptDiff().getResultsTable());
			Slot newSlot = (Slot)Util.getMap(slot,PromptTab.getPromptDiff().getResultsTable());
			if(newFrame == null || newSlot == null){
				return;
			}
			if(facet == null){
				if(newFrame.getDirectOwnSlotValues(newSlot).isEmpty() || newFrame.getOwnSlotAllowsMultipleValues(newSlot)){
					newFrame.addOwnSlotValue(newSlot,newInstance);
				}
				deleteReferenceChange(frame,slot,oldInstance);
			}
		}
	}
	
	private Instance CopyInstance(KnowledgeBase destKb,Instance instance)
	{
		Instance newInstance = null;
		
		Cls oldCls = instance.getDirectType();
		Cls newCls = (Cls)Util.getMap(oldCls,PromptTab.getPromptDiff().getResultsTable());
		if(newCls == null){
			return null;
		}
		newInstance = destKb.createInstance(instance.getName(),newCls);
		
		Collection slots = newCls.getTemplateSlots();
		Iterator slotIter = slots.iterator();
		while(slotIter.hasNext()){
			Slot newSlot = (Slot)slotIter.next();
			Slot oldSlot = (Slot)Util.getMap(newSlot,PromptTab.getPromptDiff().getResultsTable());
			
			if(oldSlot == null){
				continue;
			}
			
			Collection vals = instance.getDirectOwnSlotValues(oldSlot);
			for(Iterator valIter = vals.iterator();valIter.hasNext(); ){
				Object oldVal = valIter.next();
				Object newVal = copyOwnSlotValue(newInstance,newSlot,oldSlot,oldVal);
				if(newVal != null){
					newInstance.addOwnSlotValue(newSlot,newVal);
				}
			}
		}
		Collection doc = new ArrayList(instance.getDocumentation());
		newInstance.setDocumentation(doc);
		
		return newInstance;
		
		
	}
	
	private void rejectMappedInstance(Cls cls,Instance instance){
		TableRow row = getSoleRow(instance);
		Object[] diffElements = row.getOperationExplanation().toArray();
		for(int diffNum =0; diffNum <diffElements.length; diffNum++){
			FrameDifferenceElement diffEl = (FrameDifferenceElement)diffElements[diffNum];
			if(diffEl.getChangeLevel() == FrameDifferenceElement.LEVEL_DIRECT)
				rejectChangeInInstance(cls,instance,diffEl);
		}
		
	}
	
//	public void checkChangedClassesCount(TableRow row)
//	{
//	if(row.getF1Value() instanceof Cls && row.isChecked()==false && (row.getMappingLevel()== TableRow.MAPPING_LEVEL_UNCHANGED || row.getMappingLevel()== TableRow.MAPPING_LEVEL_ISOMORPHIC))
//	{
//	//DiffClsesPanel clsesPanel = DiffClsesPanel.getInstance(PromptTab.getProject(PromptDiff.getKb2()),new DiffViewSetUp (PromptTab.getProject(PromptDiff.getKb1()), PromptTab.getProject(PromptDiff.getKb2()),PromptDiff.getResultsTable() ));
//	//Log.getLogger().info("Before decrement:" +clsesPanel._totalChanges);
//	DiffClsesPanel clsesPanel = DiffClsesPanel.getInstance(PromptTab.getProject(PromptDiff.getKb2()),PromptTab.getPromptDiff().getViewSetUp());
//	clsesPanel._totalChanges = clsesPanel._totalChanges - 1;
//	//Log.getLogger().info("After decrement:" +clsesPanel._totalChanges);
//	row.setChecked(true);
//	clsesPanel._numberOfChanges.setText("    " + Integer.toString(clsesPanel._totalChanges));
//	}
//	}
	
	public void rejectChangeInInstance(Cls cls,Instance instance,FrameDifferenceElement diffEl){
		TableRow row = getSoleRow(instance);
		String relationship = diffEl.getRelationshipToFrame();
		if(relationship != FrameDifferenceElement.OWN_SLOT_VALUE){
			Log.getLogger().info("Yet to handle Relationship  " + relationship);
			return;
		}
		
		Slot slot = diffEl.getSlotValue();
		Slot oldSlot = null,newSlot = null;
		
		if(slot != null){			
			if(slot.getKnowledgeBase() != cls.getKnowledgeBase()){
				oldSlot = slot;
				newSlot = (Slot)Util.getMap(slot,PromptTab.getPromptDiff().getResultsTable());
			}else{
				oldSlot = (Slot)Util.getMap(slot,PromptTab.getPromptDiff().getResultsTable());
				newSlot = slot;
			}
		}		
		
		switch(diffEl.getOperation()){
		case FrameDifferenceElement.OP_ADDED:{
			Object value = diffEl.getO2Value();
			instance.removeOwnSlotValue(newSlot,value);
			break;
		}
		
		case FrameDifferenceElement.OP_DELETED:{
			Object oldVal = diffEl.getO1Value();
			Object valCopy = copyOwnSlotValue(instance,newSlot,oldSlot,oldVal);
			instance.addOwnSlotValue(newSlot,valCopy);
			break;
		}
		
		case FrameDifferenceElement.OP_CHANGED:{
			Object oldVal = diffEl.getO1Value();
			Object newVal = diffEl.getO2Value();
			ArrayList slotVals = new ArrayList(instance.getDirectOwnSlotValues(newSlot));
			slotVals.remove(oldVal);
			slotVals.add(newVal);
			
			break;
		}
		}
		row.removeInstanceOperationExplanation(diffEl);
	}
}

class ClsDependenceGraph
{
	public ClsDependenceGraph(Cls root,ResultTable diffTable)
	throws AlgoFailedException
	{
		BuildDependenceGraph(root);
	}
	
	public Vector getPartiallyOrderedSet()
	throws AlgoFailedException
	{
		Vector resultSet = new Vector(_nodeList.size());
		Vector candidateRoots = new Vector(_rootList);
		
		for(Iterator i = _dependenceMap.values().iterator();i.hasNext();)
		{
			ClsDepElement depEl = (ClsDepElement)i.next();
			depEl.tmpNumDependees = depEl.numDependees;
		}
		
		while(!candidateRoots.isEmpty()) {
			
			Cls root = (Cls)candidateRoots.remove(0);
			ClsDepElement depEl = (ClsDepElement)_dependenceMap.get(root);
			
			Assert.assertTrue("Root should not have any dependees",(depEl.tmpNumDependees == 0));
			if(_nodeList.contains(root))
				resultSet.add(root);
			
			for(Iterator i = depEl.dependentList.iterator();i.hasNext();){
				Cls dependent = (Cls)i.next(); 
				ClsDepElement clsDepEl = (ClsDepElement)_dependenceMap.get(dependent);
				clsDepEl.tmpNumDependees--;
				if(clsDepEl.tmpNumDependees == 0)
					candidateRoots.add(dependent);
			}
		}
		if(resultSet.size() != _nodeList.size()) {
			Log.getLogger().severe("Class dependence graph could not be formed");
			throw new  AlgoFailedException();
		}
		return resultSet;
	}
	private void BuildDependenceGraph(Cls root)
	throws AlgoFailedException
	{
		if(_nodeList.contains(root))
			return;
		
		Collection superClses = Util.getDirectSuperclasses(root);
		for(Iterator superClsIter = superClses.iterator();superClsIter.hasNext();){
			Cls superCls = (Cls)superClsIter.next();
			TableRow row = getSoleRow(superCls);
			if(row.getOperationValue() == TableRow.OPERATION_DELETE){
				addDependence(superCls,root);
			}else if(row.getF2Value() == null){
				Log.getLogger().severe("For Cls " + root + "Failed to map SuperClass " + superCls);
				throw new AlgoFailedException();
			}
		}
		_nodeList.add(root);
		ClsDepElement depEl = (ClsDepElement)_dependenceMap.get(root);
		if(depEl == null )
		{
			depEl = new ClsDepElement(root);
			_dependenceMap.put(root,depEl); 
		}
		
		if(depEl.numDependees == 0){
			_rootList.add(root);
		}
		
		Collection subClses = Util.getDirectSubclasses(root);
		for(Iterator subClsIter = subClses.iterator();subClsIter.hasNext();){
			Cls subCls = (Cls)subClsIter.next();
			BuildDependenceGraph(subCls);
		}
	}
	
	private void addDependence(Cls cls,Cls dependent)
	{
		ClsDepElement clsEl = (ClsDepElement)_dependenceMap.get(cls);
		if(clsEl == null){
			clsEl = new ClsDepElement(cls);
			_dependenceMap.put(cls,clsEl);
		}
		clsEl.addDependent(dependent);
		
		ClsDepElement depEl = (ClsDepElement)_dependenceMap.get(dependent);
		if(depEl == null){
			depEl = new ClsDepElement(dependent);
			_dependenceMap.put(dependent,depEl);
		}
		depEl.numDependees++;		
	}
	
	public void printTable()
	{
		for(Iterator i = (_dependenceMap.values()).iterator();i.hasNext();)
		{
			ClsDepElement depEl = (ClsDepElement)i.next();
			Log.getLogger().info(depEl.node + "(" + depEl.numDependees+")"+"->" + depEl.dependentList);
		}
	}
	
	private TableRow getSoleRow(Cls cls)
	{
		return (TableRow)CollectionUtilities.getSoleItem(PromptTab.getPromptDiff().getResultsTable().getRows(cls));
	}
	HashMap _dependenceMap = new HashMap();
	Collection _rootList = new Vector();
	Collection _nodeList = new Vector();
}

class ClsDepElement
{
	public ClsDepElement(Cls cls)
	{
		node = cls;
	}
	public void addDependent(Cls cls)
	{
		dependentList.add(cls);
	}
	Cls node;
	Collection dependentList = new Vector();
	int numDependees = 0; //number of nodes this element is dependent on
	int tmpNumDependees = -1;  //for scratch work
}
class CopyFailureException extends Exception
{
	
}

class AlgoFailedException extends Exception
{
}

