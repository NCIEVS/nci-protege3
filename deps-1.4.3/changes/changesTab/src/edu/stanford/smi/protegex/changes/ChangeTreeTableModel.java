package edu.stanford.smi.protegex.changes;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.change.api.ChangeFactory;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.changes.ui.AbstractTreeTableModel;
import edu.stanford.smi.protegex.changes.ui.Filter;
import edu.stanford.smi.protegex.changes.ui.TreeTableModel;

public class ChangeTreeTableModel extends AbstractTreeTableModel implements TreeTableModel{
    private static final transient Logger log = Log.getLogger(ChangeTreeTableModel.class);

	private String[] colNames;
    private RootTreeTableNode root;
    private Slot partOfCompositeChangeSlot;
    private Slot subChangesSlot;
    private Filter filter;


	public ChangeTreeTableModel(KnowledgeBase changesKb) {
		super(new RootTreeTableNode());
        root = (RootTreeTableNode) super.root;
        ChangeFactory factory = new ChangeFactory(changesKb);
        partOfCompositeChangeSlot = factory.getPartOfCompositeChangeSlot();
        subChangesSlot = factory.getSubChangesSlot();
		init();
	}


	// init the column names, data structures
	private void init() {
        ChangeTableColumn[] cols = ChangeTableColumn.values();
		colNames = new String[cols.length];
        for (int i = 0; i < cols.length; i++) {
            colNames[i] = cols[i].getName();
        }
	}


	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return colNames.length;
	}

	@Override
	public Class getColumnClass(int column) {
		return column == 0 ? TreeTableModel.class : Object.class;
    }

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	public String getColumnName(int column) {
        if (column < 0 || column >= colNames.length) {
            return "";
        }
        return colNames[column];
	}



	public int getChildCount(Object node) {
	    return ((TreeTableNode) node).getChildCount();
	}

	public Object getChild(Object node, int i) {
	    return ((TreeTableNode) node).getChildAt(i);
	}


	public Object getValueAt(Object node, int col) {
	    return ((TreeTableNode) node).getValueAt(col);
	}


   @Override
public void setValueAt(Object aValue, Object node, int column) {
   	((TreeTableNode) node).setValueAt(aValue, column);
   }


   public void addChangeData(Change changeInst) {	   
       addChangeData(changeInst, true);
   }

   private void addChangeData(Change changeInst,  boolean completeUpdate) {
       ChangeProjectUtil.logAnnotatableThing("adding change to ui model", log, Level.FINE, changeInst);
       if (ChangeProjectUtil.isRoot(changeInst)) {
		return;
       }

       ChangeTreeTableNode node = new ChangeTreeTableNode(root, changeInst, filter);
       
       if (filter != null && !filter.matches(node)) {return; }
       
       TreeTableNode parent = node.getParent();
       if (parent.isRoot()) {
           int index = root.addChild(node);
           if (index >= 0) {
               int childIndices[] = { index };
               Object children[] = { node };
               fireTreeNodesInserted(root, makePath(root), childIndices, children);
           }
       }
       else {
           int index = 0;
           for (Object o : parent.getChildren()) {
               if (node.equals(o)) {
                   int childIndices[] = { index };
                   Object  children[] = { node };
                   fireTreeNodesInserted(parent, makePath(parent), childIndices, children);
               }
               index++;
           }
       }
   }

   public void update(Change changeInst, Slot slot, List oldValues) {
       ChangeProjectUtil.logAnnotatableThing("Updating ui model with slot = " + slot, log, Level.FINE, changeInst);
       if (slot.equals(partOfCompositeChangeSlot)) {
           removeFromParent(changeInst, oldValues);
       }
       else if (slot.equals(subChangesSlot)) {
           updateChildren(new ChangeTreeTableNode(root, changeInst, filter));
       }
   }

   private void removeFromParent(Change change, List oldValues) {
       TreeTableNode parent;
       if (oldValues == null || oldValues.isEmpty()) {
           parent = root;
           ChangeTreeTableNode node = new ChangeTreeTableNode(root, change, filter);
           int index = root.removeChild(node);
           if (index >= 0) {
               int childIndices[] = { index };
               Object children[] = { node };
               fireTreeNodesRemoved(root, makePath(root), childIndices, children);
           }
       }
       else {
           Change parentChange = (Change) oldValues.get(0);
           if (parentChange == null || parentChange.getApplyTo() == null) {
               return;
           }
           parent = new ChangeTreeTableNode(root, parentChange, filter);
           updateChildren(parent);
       }
   }

   private void updateChildren(TreeTableNode parent) {
       Object[] children = parent.getChildren();
       int childIndices[] = new int[children.length];
       for (int i = 0; i < children.length; i++) {
           childIndices[i] = i;
       }
       fireTreeStructureChanged(parent, makePath(parent), childIndices, children);
   }


	public void cancelQuery() {
		setFilter(null);
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}


	public Object getObjInRow(int row) {
        // this is extremely massively suspect...
		Change aInst = ((ChangeTreeTableNode) root.getChildAt(row)).getChange();
		return aInst;
	}
	
    private Object[] makePath(TreeTableNode node) {
        int len = 1;
        for (TreeTableNode climber = node; !climber.isRoot(); climber = climber.getParent()) {
            len++;
        }
        Object[] path = new Object[len];
        TreeTableNode climber = node;
        for(int i = len - 1; i >=0; i--) {
            path[i] = climber;
            climber = climber.getParent();
        }
        return path;
    }


	public void clear() {
		// TODO Auto-generated method stub
		
	}

}
