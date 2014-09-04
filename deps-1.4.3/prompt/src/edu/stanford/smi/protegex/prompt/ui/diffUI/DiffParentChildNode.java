 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
  *                 Sandhya Kunnatur kunnatur@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui.diffUI;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.*;
import edu.stanford.smi.protegex.prompt.util.*;



public class DiffParentChildNode extends LazyTreeNode  {
    private static ResultTable _diffTable;
	private DiffViewSetUp _viewSetup;
	private FrameStatus _frameStatuses;

	DiffClsListener _diffClsListener = new DiffClsListener(){
		public void clsUpdated(Cls cls) {
			clsModified(cls);
		}
		public void childRemoved(Cls cls) {
			notifyChildRemoved(cls);
		}
		
		public void childAdded(Cls cls) {
			notifyChildAdded(cls);
		}
	};
	
    public Comparator getComparator() {
        return new LazyTreeNodeFrameComparator();
    }

    public DiffParentChildNode(LazyTreeNode parentNode, Cls parentCls, DiffViewSetUp vs) {
      super (parentNode, parentCls);
	  _viewSetup = vs;
	  _frameStatuses = vs.getFrameStatuses();
	  _frameStatuses.addDiffClsListener(parentCls,_diffClsListener);
     }

    protected LazyTreeNode createNode(Object o) {
    	if (!(o instanceof Cls))
    		Log.getLogger().warning ("o = " + o + ", class = " + o.getClass());
    	String className = ((Cls)o).getName();
        return new DiffParentChildNode(this, (Cls) o, _viewSetup);
    }

   protected int getChildObjectCount() {
        return getChildObjects().size();
    }

    protected Cls getCls() {
        return (Cls) getUserObject();
    }

    // return a collection of nodes representing the children
    protected Collection getChildObjects() {
      return Util.getDiffChildObjects(getCls(), _viewSetup.getResultsTable());
    }

    public String toString() {
        return "DiffParentChildNode(" + getCls() + ")";
    }
    
    private void clsModified(Cls cls)
    {
    	LazyTreeNode parent = getLazyTreeNodeParent();
//    	_viewSetup.getFrameStatuses().clearStatus(cls,(Cls)parent.getUserObject());
    	notifyNodeChanged();
    }
    public void notifyNodeChanged()
    {
    	notifyNodeChanged(this);
    }
    
    public void notifyChildRemoved(Cls child)
    {
//    	Log.getLogger().info("Notification : child" + child +"removed for " + getUserObject());
    	super.childRemoved(child);
    }
    
    public void notifyChildAdded(Cls child)
    {
//		Log.getLogger().info("Notification : child" + child +"added for " + getUserObject());
		super.childAdded(child);
    }
	public void notifyNodeChanged(LazyTreeNode node) {
		LazyTreeNode parent = getLazyTreeNodeParent();
		Object o2 = parent.getUserObject();
		Cls parentCls = (o2 instanceof Cls) ? (Cls)o2 : null;
//		_viewSetup.getFrameStatuses().clearStatus((Cls)getUserObject(),(Cls)parentCls);
		if (parent == null) {
			Log.getLogger().severe("Notification message lost");
		} else {
			parent.notifyNodeChanged(node);
		}
	}
	
    public void dispose()
    {
    	_frameStatuses.removeDiffClsListener((Cls)getUserObject(),_diffClsListener);
    	super.dispose();
    }
}
