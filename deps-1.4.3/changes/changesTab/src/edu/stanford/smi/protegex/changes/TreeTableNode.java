package edu.stanford.smi.protegex.changes;

import edu.stanford.smi.protegex.changes.ui.Filter;

public interface TreeTableNode {

    boolean isRoot();
    
    TreeTableNode getParent();
    
    int getChildCount();
    
    TreeTableNode[] getChildren();
    
    TreeTableNode getChildAt(int i);

    Object getValueAt(int col);
    
    void setValueAt(Object v, int col);
    
    void setFilter(Filter filter);
}
