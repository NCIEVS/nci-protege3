/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 *                 Sandhya Kunnatur kunnatur@stanford.edu
 *                 Abhita Chugh abhita@stanford.edu
 */

package edu.stanford.smi.protegex.prompt.promptDiff.structures;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.ui.diffUI.*;
import edu.stanford.smi.protegex.prompt.util.*;
//This class is used to display a diff tree. Thus, for example, all frames and trees from kb1 are either deleted or moved
//The class itself contains a hash table of objects for storing status of individual tree elements
public class FrameStatus {
	private boolean _variablesInitialized = false;
	private boolean _initializeFrameStatus = false;
	private boolean _initializeTreeStatus = false;
	private boolean _initializeChildrenStatus = false;
	private HashMap _frameStatuses;
	private HashMap _treeStatuses;
	private HashMap _childrenChangedStatuses; // frame --> collection of direct children that have changed
//	private HashMap _frameParentMap;
	private HashMap _diffClsListeners;
	
	private ResultTable _diffTable;
	
	
	public static final int NO_STATUS = 0;
	
	public static final int FRAME_DELETED = 11;
	public static final int FRAME_ADDED = 12;
	public static final int FRAME_CHANGED = 13;
	public static final int FRAME_UNCHANGED = 14;
	public static final int FRAME_MOVED_FROM = 15;
	public static final int FRAME_MOVED_TO = 16;
	public static final int FRAME_COPIED_TO_HERE = 17;  // a superclass was added
	public static final int SUPERCLASS_REMOVED_FROM_FRAME = 18;
	
	public static final int TREE_DELETED = 21;
	public static final int TREE_ADDED = 22;
	public static final int TREE_CHANGED = 23;
	public static final int TREE_UNCHANGED = 24;
	public static final int TREE_MOVED_FROM = 25;
	public static final int TREE_MOVED_TO = 26;
	public static final int TREE_NO_CHILDREN = 27;
	
	private KnowledgeBase _kb1;
	private KnowledgeBase _kb2;
	
	public FrameStatus (KnowledgeBase kb1, KnowledgeBase kb2, ResultTable table){
		_kb1 = kb1;
		_kb2 = kb2;
		_diffTable = table;
		
		initializeStaticVariables();
		initializeFrameStatus();
		initializeTreeStatus();
		initializeChildrenStatus();
	}
	
	private void initializeFrameStatus(){
		if(_initializeFrameStatus)return;
		Frame _frame = null;
		int status = FRAME_UNCHANGED;
		int set = 0;
		Iterator i = _diffTable.values().iterator();
		while (i.hasNext()) {
			TableRow next = (TableRow)i.next();
			
			if((next.getF1Value()!=null && next.getF1Value()instanceof Cls)||(next.getF2Value()!=null && next.getF2Value()instanceof Cls) && (next.getMappingLevel()!= TableRow.MAPPING_LEVEL_UNCHANGED)){
				
				if (next.getOperationValue() == TableRow.OPERATION_DELETE && next.getF1Value()instanceof Cls){
					_frame = next.getF1Value();
					status = FRAME_DELETED; 
					set = 1;
					
				}
				else if (next.getOperationValue() == TableRow.OPERATION_ADD && next.getF2Value()instanceof Cls){
					_frame = next.getF2Value();
					status = FRAME_ADDED;
					set = 1;
					
				}
				else if(next.getMappingLevel()== TableRow.MAPPING_LEVEL_DIRECTLY_CHANGED || next.getRenameValue()==TableRow.RENAME_PLUS && next.getF2Value()instanceof Cls){
					_frame = next.getF2Value();
					status = FRAME_CHANGED;
					set = 1;
				}
				
			} // end if - _frame & status set by now.
			
			Collection frameDiffElements =  next.getOperationExplanation();
			if (frameDiffElements != null) {
				Iterator d = frameDiffElements.iterator();
				while (d.hasNext()) {
					FrameDifferenceElement nextDiffElement = (FrameDifferenceElement)d.next();
					if (nextDiffElement.getRelationshipToFrame().equals(FrameDifferenceElement.SUPERCLASS)) {
						if (nextDiffElement.getOperation() == FrameDifferenceElement.OP_CHANGED) {
							_frameStatuses.put(getHashString (_frame, (Frame)nextDiffElement.getO1Value()), new Integer (FRAME_MOVED_FROM)); 
							_frameStatuses.put(getHashString (_frame, (Frame)nextDiffElement.getO2Value()), new Integer (FRAME_MOVED_TO)); 
						} else if (nextDiffElement.getOperation() == FrameDifferenceElement.OP_ADDED) {
							_frameStatuses.put(getHashString (_frame, (Frame)nextDiffElement.getO2Value()), new Integer (FRAME_COPIED_TO_HERE));
						} else if (nextDiffElement.getOperation() == FrameDifferenceElement.OP_DELETED) {
							_frameStatuses.put(getHashString (_frame, (Frame)nextDiffElement.getO1Value()), new Integer (SUPERCLASS_REMOVED_FROM_FRAME));
						}
					}
				}
			}
			
			
			//****** need to put in moved-from and to part
			if(set == 1){	
				Collection parents = Util.getDirectSuperclasses((Cls)_frame);
				if (parents != null && !parents.isEmpty()) {
					Iterator j = parents.iterator();
					while (j.hasNext()) {
						Cls nextParent = (Cls)j.next();
						if (_frameStatuses.get(getHashString (_frame, nextParent)) == null) // don't replace if there is a moved_to or moved_from there already
							_frameStatuses.put(getHashString(_frame, nextParent), new Integer (status));
//						addFrameParentAssociation(_frame,nextParent);
					}
				}  
				
			}
			
		}// end while
		
		_initializeFrameStatus = true;
	}
	
	
	private void initializeTreeStatus(){
		if(_initializeTreeStatus) return;
		Frame _frame = null;
		
		int set = 0;
		Iterator i = _diffTable.values().iterator();
		while (i.hasNext()) {
			TableRow next = (TableRow)i.next();
			
			if((next.getF1Value()!=null && next.getF1Value()instanceof Cls)||(next.getF2Value()!=null && next.getF2Value()instanceof Cls) && (next.getMappingLevel()!= TableRow.MAPPING_LEVEL_UNCHANGED)){
				
				if (next.getOperationValue() == TableRow.OPERATION_DELETE && next.getF1Value()instanceof Cls){
					_frame = next.getF1Value();
					
					set = 1;
					
				}
				else if (next.getOperationValue() == TableRow.OPERATION_ADD && next.getF2Value()instanceof Cls){
					_frame = next.getF2Value();
					
					set = 1;
					
				}
				else if(next.getMappingLevel()== TableRow.MAPPING_LEVEL_DIRECTLY_CHANGED || next.getRenameValue()==TableRow.RENAME_PLUS && next.getF2Value()instanceof Cls){
					_frame = next.getF2Value();
					
					set = 1;
				}
				
			} // end if - _frame set by now.
			
			if(set == 1){	
				Collection parents = Util.getDirectSuperclasses((Cls)_frame);
				if (parents != null && !parents.isEmpty()) {
					Iterator j = parents.iterator();
					while (j.hasNext()) {
						Cls nextParent = (Cls)j.next();
						setInitTreeStatus(_frame,nextParent);
						
					}
				}  
				
			}
			
		}// end while
		
		_initializeTreeStatus = true;
	}
	
	
	private void initializeChildrenStatus(){
		if(_initializeChildrenStatus) return;
		Frame _frame = null;
		
		int set = 0;
		Iterator i = _diffTable.values().iterator();
		while (i.hasNext()) {
			TableRow next = (TableRow)i.next();
			
			if((next.getF1Value()!=null && next.getF1Value()instanceof Cls)||(next.getF2Value()!=null && next.getF2Value()instanceof Cls) && (next.getMappingLevel()!= TableRow.MAPPING_LEVEL_UNCHANGED)){
				
				if (next.getOperationValue() == TableRow.OPERATION_DELETE && next.getF1Value()instanceof Cls){
					_frame = next.getF1Value();
					
					set = 1;
					
				}
				else if (next.getOperationValue() == TableRow.OPERATION_ADD && next.getF2Value()instanceof Cls){
					_frame = next.getF2Value();
					
					set = 1;
					
				}
				else if(next.getMappingLevel()== TableRow.MAPPING_LEVEL_DIRECTLY_CHANGED || next.getRenameValue()==TableRow.RENAME_PLUS && next.getF2Value()instanceof Cls){
					_frame = next.getF2Value();
					set = 1;
				}
				
			} // end if - _frame set by now.
			
			if(set == 1){	
				Collection parents = Util.getDirectSuperclasses((Cls)_frame);
				if (parents != null && !parents.isEmpty()) {
					Iterator j = parents.iterator();
					while (j.hasNext()) {
						Cls nextParent = (Cls)j.next();
						setInitChildrenStatus(nextParent, _frame);
						
					}
				}  
				
			}
			
		}// end while
		
		_initializeChildrenStatus = true;
	}
	
	
	
	private void setInitChildrenStatus(Frame parent, Frame child){
		addChildToChildrenChangedStatus (parent, child);
		
		Collection parents = Util.getDirectSuperclasses((Cls)parent);
		if (parents != null && !parents.isEmpty()) {
			Iterator j = parents.iterator();
			while (j.hasNext()) {
				Cls nextParent = (Cls)j.next();
//				if (getChildrenChangedStatus (nextParent) == true) return;
//				addFrameParentAssociation(frame,nextParent);
				setInitChildrenStatus(nextParent, parent);
				
			}
		}  
	}
	
	private int getInitTreeStatus (Frame frame, Frame parent) {
		
		Object status = _treeStatuses.get(getHashString(frame, parent));
		
		if (status != null) return ((Integer)status).intValue();
		
		return setInitTreeStatus (frame, parent);
		
	}
	
	
	private int setInitTreeStatus (Frame frame, Frame parent) {
		int status = NO_STATUS;
		
		int rootStatus = getFrameStatus (frame, parent);
		int treeVersionOfRootStatus = convertFrameToTreeStatus(rootStatus);
		
		
		
		if (!(frame instanceof Cls)) {
			status = NO_STATUS;
		} else {
			status = rootStatus;
			Collection childObjects = Util.getDiffChildObjects ((Cls)frame, _diffTable);
			
			if (childObjects != null && !childObjects.isEmpty()) {
				Iterator i = childObjects.iterator();
				boolean heterogeneityFound = false;
				
				while (i.hasNext() && !heterogeneityFound) {
					Frame nextChild = (Frame) i.next();
					int nextFrameStatus = getFrameStatus(nextChild,frame);
					if (nextFrameStatus == FRAME_UNCHANGED || nextFrameStatus != rootStatus) {
						status = NO_STATUS;
						heterogeneityFound = true;
					} else {
						int nextStatus = getInitTreeStatus (nextChild, frame);
						if (nextStatus == NO_STATUS || (nextStatus != TREE_NO_CHILDREN &&nextStatus != treeVersionOfRootStatus)) {
							status = NO_STATUS;
							heterogeneityFound = true; 
						}
					}
				}
			} else { //no children
				status = TREE_NO_CHILDREN;
			}
		}
		status = convertFrameToTreeStatus(status);
		_treeStatuses.put(getHashString(frame, parent), new Integer (status));
//		addFrameParentAssociation(frame,parent);
		return status;
	}
	
	
	
	public int getFrameStatus (Frame frame, Frame parent) {
		initializeStaticVariables();
		Object status = _frameStatuses.get(getHashString(frame, parent));
		if (status != null) return ((Integer)status).intValue();
		
		//return setFrameStatus (frame, parent);
		return FRAME_UNCHANGED;
	}
	
	public int getTreeStatus (Frame frame, Frame parent) {
		initializeStaticVariables();
		Object status = _treeStatuses.get(getHashString(frame, parent));
		
		if (status != null) return ((Integer)status).intValue();
		
		//return setTreeStatus (frame, parent);
		return NO_STATUS;
	}
	
	public boolean getChildrenChangedStatus (Frame frame) {
		Object status = _childrenChangedStatuses.get(frame);
		if (status != null) return true;
		
		//return setChildrenChangedStatus (frame, parent);
		return false;
	}
	
	private void addChildToChildrenChangedStatus (Frame parent, Frame child) {
		Object status = _childrenChangedStatuses.get (parent);
		Collection children;
		if (status != null)
			children = (Collection)status;
		else
			children = new HashSet();
		children.add(child);
		_childrenChangedStatuses.put(parent, children);
	}
	
	private void removeChildChanged (Frame parent, Frame child) {
		Object status = _childrenChangedStatuses.get (parent);
		if (status == null) {
			Log.getLogger().warning("Shouldn't be here");
			return;
		}
		Collection children = (Collection)status;
		if (getFrameStatus(child, parent) == FRAME_UNCHANGED)
			children.remove(child);
		if (children.isEmpty()) {
			_childrenChangedStatuses.remove(parent);
			clearStatusDeep (parent);
		}
		
	}
	
	public String toString () {
		return "FrameStatus";
	}
	
	public Frame movedFromWhere (Frame frame, Frame parent) {
		if (getFrameStatus (frame, parent)!=FRAME_MOVED_TO) return null;
		
		Collection frameSources = _diffTable.getSources(frame);
		if (frameSources == null) {
			Log.getLogger().severe ("Shouldn't be here...");
			return null;
		}
		
		Iterator i = frameSources.iterator();
		Frame currentDeletedParent = null;
		while (i.hasNext()) {
			Object nextSource = i.next();
			if (nextSource instanceof Cls) {
				Collection sourceParents = Util.getDirectSuperclasses((Cls)nextSource);
				Iterator j = sourceParents.iterator();
				while (j.hasNext()) {
					Frame nextParent = (Frame)j.next();
					Collection nextParentImages = _diffTable.getImages(nextParent);
					if (nextParentImages == null || nextParentImages.isEmpty())
						currentDeletedParent = nextParent;
					else
						return (Frame)CollectionUtilities.getFirstItem(nextParentImages);
				}
			}
		}
		return currentDeletedParent;
		
	}
	
//	private boolean frameAndItsMappingIncluded (Frame frame) {
//	if (!frame.isIncluded()) return false;
//	if (frame.getKnowledgeBase()==_kb1 && frame.isIncluded()) return true;
//	Collection rowsWithFrame = _diffTable.getRows(frame);
//	TableRow row = (TableRow)CollectionUtilities.getFirstItem(rowsWithFrame);
//	Frame source = row.getF1Value();
//	return source.isIncluded();
//	}
//	
//	private int setFrameStatus(Frame frame, Frame parent) {
//	int status = FRAME_UNCHANGED;
//	
//	counter1 = counter1 + 1;
//	
//	Log.getLogger().info("FRAME STATUS" + counter1);
//	if (!Util.isSystem(frame) && ! (!PromptDiff.showIncluded() && frameAndItsMappingIncluded(frame))) {
//	if (frame.getKnowledgeBase()==_kb1) {
//	Collection rowsWithFrame = _diffTable.getRows(frame);
//	
//	TableRow row = (TableRow)CollectionUtilities.getFirstItem(rowsWithFrame);
//	if(!row.isChangeAccepted()){     	
//	if (row.getOperationValue()==TableRow.OPERATION_DELETE)
//	status = FRAME_DELETED;
//	else { //frame moved to a different position
//	status = FRAME_MOVED_FROM;
//	}
//	}else
//	checkAllAccepted(rowsWithFrame);
//	} else { // frame in kb2
//	Collection rowsWithFrame = _diffTable.getRows(frame);
//	TableRow firstRow = (TableRow)CollectionUtilities.getFirstItem(rowsWithFrame);
//	if (rowsWithFrame == null || rowsWithFrame.isEmpty()) {
//	Log.getLogger().warning("Frame " + frame + " doesn't have a row in the table");
//	status = NO_STATUS;
//	
//	
//	}else if(firstRow.isChangeAccepted()){
//	checkAllAccepted(rowsWithFrame);
//	}else if (firstRow.getOperationValue()==TableRow.OPERATION_ADD)
//	status = FRAME_ADDED;
//	else {  // changed, or unchanged? **** later, add "move"
//	Iterator i = rowsWithFrame.iterator();
//	while (i.hasNext()) {
//	TableRow next = (TableRow)i.next();
//	String level = next.getMappingLevel();
//	String renamed = next.getRenameValue();
//	if (level == TableRow.MAPPING_LEVEL_DIRECTLY_CHANGED || renamed == TableRow.RENAME_PLUS)
//	status = FRAME_CHANGED;
//	if (frame instanceof Cls && parent != null) {
//	if (! ((Cls)frame).hasDirectSuperclass((Cls)parent))
//	status = FRAME_MOVED_FROM;
//	else {
//	Collection frameSources = _diffTable.getSources(frame);
//	Collection parentSources = _diffTable.getSources(parent);
//	Iterator j = frameSources.iterator();
//	Collection sourceSupers = new ArrayList();
//	while (j.hasNext()) {
//	Object nextSource = j.next();
//	if (nextSource instanceof Cls)
//	sourceSupers.addAll(Util.getDirectSuperclasses((Cls)nextSource));
//	}
//	if (!sourceSupers.removeAll(parentSources))
//	status = FRAME_MOVED_TO;
//	}
//	}
//	}
//	}
//	}
//	}
//	_frameStatuses.put(getHashString(frame, parent), new Integer (status));
//	addFrameParentAssociation(frame,parent);
//	return status;
//	}
	
	public String getOldName (Frame frame, Frame parent) {
		if (getFrameStatus (frame, parent)!=FRAME_CHANGED) return null;
		
		Collection rowsWithFrame = _diffTable.getRows(frame);
		Iterator i = rowsWithFrame.iterator();
		while (i.hasNext()) {
			TableRow nextRow = (TableRow)i.next();
			String renamed = nextRow.getRenameValue();
			if (renamed == TableRow.RENAME_PLUS && nextRow.getF1Value() != null)
				return nextRow.getF1Value().getName();
		}
		return null;
	}
	
//	private boolean setChildrenChangedStatus (Frame frame, Frame parent) {
//	boolean status = false;
//	if (!(frame instanceof Cls))  return false;
//	counter3 = counter3 + 1;
//	
//	Log.getLogger().info("CHILDREN STATUS" + counter3); 
//	// break up the infinite loop in case of moved-form moved-to loop.
//	_childrenChangedStatuses.put(getHashString(frame, parent), new Boolean (true));
//	
//	Collection childObjects = Util.getDiffChildObjects ((Cls)frame, _diffTable);
//	
//	if (childObjects != null && !childObjects.isEmpty()) {
//	Iterator i = childObjects.iterator();
//	while (i.hasNext()) {
//	Cls next = (Cls)i.next();
//	boolean nextStatus = getChildrenChangedStatus (next, frame);
//	if (nextStatus == true) {
//	status = true;
//	break;
//	}
//	//if (next.getDirectSubclassCount() == 0) {
//	int leafFrameStatus = getFrameStatus (next, frame);
//	if (leafFrameStatus != NO_STATUS && leafFrameStatus != FRAME_UNCHANGED) {
//	status = true;
//	break;
//	//}
//	}
//	}
//	}
//	_childrenChangedStatuses.put(getHashString(frame, parent), new Boolean (status));
//	addFrameParentAssociation(frame,parent);
//	return status;
//	}
	
//	private int setTreeStatus (Frame frame, Frame parent) {
//	int status = NO_STATUS;
//	counter2 = counter2 + 1;
//	//if(counter2 % 1000 == 1)
//	Log.getLogger().info("TREE STATUS " +counter2);
//	int rootStatus = getFrameStatus (frame, parent);
//	int treeVersionOfRootStatus = convertFrameToTreeStatus(rootStatus);
//	
//	// put something in to avoid an infinite loop. if we do have a loop, it's always with moved from/moved to
//	// and, hence the status is NO_STATUS anyway
//	_treeStatuses.put(getHashString(frame, parent), new Integer (NO_STATUS));
//	
//	if (!(frame instanceof Cls)) {
//	status = NO_STATUS;
//	} else {
//	status = rootStatus;
//	Collection childObjects = Util.getDiffChildObjects ((Cls)frame, _diffTable);
//	
//	if (childObjects != null && !childObjects.isEmpty()) {
//	Iterator i = childObjects.iterator();
//	
//	while (i.hasNext()) {
//	Frame next = (Frame) i.next();
//	int nextStatus = getTreeStatus (next, frame);
//	if (nextStatus == TREE_CHANGED)
//	status = TREE_CHANGED;
//	if (nextStatus == NO_STATUS || nextStatus != treeVersionOfRootStatus) {
//	_treeStatuses.put(getHashString(frame, parent), new Integer (NO_STATUS));
//	return NO_STATUS;
//	}
//	}
//	}
//	}
//	status = convertFrameToTreeStatus(status);
//	_treeStatuses.put(getHashString(frame, parent), new Integer (status));
//	addFrameParentAssociation(frame,parent);
//	return status;
//	}
	
	
	private int convertFrameToTreeStatus (int frameStatus) {
		switch(frameStatus) {
		case FRAME_ADDED: 
			return TREE_ADDED;
		case FRAME_DELETED: return TREE_DELETED;
		case FRAME_CHANGED: return TREE_CHANGED;
		case FRAME_MOVED_FROM: return TREE_MOVED_FROM;
		case FRAME_MOVED_TO: return TREE_MOVED_TO;
		case FRAME_COPIED_TO_HERE: return TREE_MOVED_TO;
		case FRAME_UNCHANGED: return TREE_UNCHANGED;
		case TREE_NO_CHILDREN: return TREE_NO_CHILDREN;
		}
		return NO_STATUS;
	}
	
	private Object getHashString (Frame frame, Frame parent) {
		if (frame == null) return "null";
		return frame.getName() + ":parent:" + ((parent != null) ? parent.getName() : "null");
		
	}
	
	private void initializeStaticVariables() {
		if (_variablesInitialized) return;
		//Log.getLogger().info("NO OF FRAMES: "+ _kb2.getFrameCount());
		_frameStatuses = new HashMap (_kb2.getFrameCount());
		_treeStatuses = new HashMap (_kb2.getFrameCount());
		_childrenChangedStatuses = new HashMap (_kb2.getFrameCount());
//		_frameParentMap = new HashMap(_kb2.getFrameCount());
		_diffClsListeners = new HashMap(_kb2.getFrameCount());
		_variablesInitialized = true;
	}
	
//	private void addFrameParentAssociation(Frame frame,Frame parent)
//	{
//	Collection parentList = (Collection)_frameParentMap.get(frame);
//	if(parentList == null){
//	parentList = new Vector();
//	_frameParentMap.put(frame,parentList);
//	}
//	if(!parentList.contains(parent)){
//	parentList.add(parent);
//	}
//	}
//	
//	private void removeFrameParentAssociation(Frame frame,Frame parent)
//	{
//	Collection parentList = (Collection)_frameParentMap.get(frame);
//	if(parentList != null){
//	parentList.remove(parent);
//	}
//	}
	
//	private boolean checkAllAccepted(Collection c)
//	{
//	Iterator i = c.iterator();
//	
//	while(i.hasNext()){
//	TableRow row = (TableRow)i.next();
//	if(!row.isChangeAccepted()){
//	Log.getLogger().info("CheckAllAccepted Failed!! for " + 
//	row.getF1Value() + " " + row.getF2Value());
//	
//	return false;
//	}
//	}	
//	return true;
//	}
	
	private void clearStatusDeep(Frame frame)
	{
		if (!(frame instanceof Cls)) return;
		Collection parentList = Util.getDirectSuperclasses((Cls)frame);
		if(parentList == null) {
			return;
		}
		
		Iterator i = parentList.iterator();
		while (i.hasNext()) {
			Cls nextParent = (Cls)i.next();
			clearTreeStatus(frame,nextParent);
//			clearStatusDeep(nextParent);
		}
		
	}
	
	public void addDiffClsListener(Cls cls,DiffClsListener listener)
	{
		initializeStaticVariables();
		Collection listenerList = (Collection)_diffClsListeners.get(cls);
		if(listenerList == null) {
			listenerList = new Vector();
			_diffClsListeners.put(cls,listenerList);
		}
		listenerList.add(listener);
		//Log.getLogger().info("Added listener" + listenerList.size()+" for Cls " + cls);
	}
	
	public void removeDiffClsListener(Cls cls,DiffClsListener listener)
	{
		initializeStaticVariables();
		Collection listenerList = (Collection)_diffClsListeners.get(cls);
		if(listenerList != null)
			listenerList.remove(listener);
		//Log.getLogger().info("Removed listener" + listenerList.size()+" for Cls " + cls);
	}
	
	public void notifyClsUpdated(Cls cls)
	{
//		clearStatusDeep(cls);
		Collection listenerList = (Collection)_diffClsListeners.get(cls);
		if(listenerList == null)
			return;
		for(Iterator i = listenerList.iterator();i.hasNext(); ) {
			DiffClsListener listener = (DiffClsListener)i.next();
			listener.clsUpdated(cls);
		}
	}
	
	public void notifyChildRemoved(Cls cls,Cls child)
	{	 	
//		clearStatus(child,cls);	
//		clearStatusDeep(cls);
		
		Collection listenerList = (Collection)_diffClsListeners.get(cls);
		if(listenerList != null) {
			for(Iterator j = listenerList.iterator();j.hasNext(); ) {
				DiffClsListener listener = (DiffClsListener)j.next();
				listener.childRemoved(child);
			}
		}
	}
	
	public void notifyChildAdded(Cls cls,Cls child)
	{	 		
//		clearStatusDeep(cls);
		
		Collection listenerList = (Collection)_diffClsListeners.get(cls);
		if(listenerList != null) {
			for(Iterator j = listenerList.iterator();j.hasNext(); ) {
				DiffClsListener listener = (DiffClsListener)j.next();
				listener.childAdded(child);
			}
		}			
	}
	
//	public void notifyRenamed(Cls cls,String oldName,String newName)
//	{
//	clearAllStatuses();
//	notifyClsUpdated(cls);
//	}
	
	public void clearStatus(Frame frame,Frame parent)
	{
		Object hashString = getHashString(frame,parent);
		_frameStatuses.remove(hashString);
		clearTreeStatus (frame, parent);
	}
	
	private void clearTreeStatus (Frame frame, Frame parent) {
		Object hashString = getHashString(frame,parent);
		int currentTreeStatus = getTreeStatus (frame, parent);
		if (currentTreeStatus != TREE_NO_CHILDREN) 
			_treeStatuses.remove(hashString); //status must change here
//		_treeStatuses.remove(hashString);
		removeChildChanged (parent, frame);
		removeParentTreeStatus (parent);
//		removeFrameParentAssociation(frame,parent);
	}
	
	private void removeParentTreeStatus (Frame f) {
		Collection parents = Util.getDirectSuperclasses((Cls)f);
		Iterator i = parents.iterator();
		while (i.hasNext()) {
			Frame nextParent = (Frame)i.next();
			_treeStatuses.remove(getHashString (f, nextParent));
		}
	}
	
//	private void clearAllStatuses()
//	{
//	_frameStatuses.clear();
//	_treeStatuses.clear();
//	_childrenChangedStatuses.clear();
////	_frameParentMap.clear();
//	}
}

