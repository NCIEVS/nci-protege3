package edu.stanford.smi.protegex.changes;

import java.util.ArrayList;
import java.util.Collection;

import edu.stanford.smi.protegex.changes.ui.Filter;


public abstract class AbstractChangeTreeTableNode implements TreeTableNode {

	protected Filter filter;
	
	public AbstractChangeTreeTableNode(Filter filter) {
		this.filter = filter;
	}
	
	public Filter getFilter() {
		return filter;
	}
	
	public void setFilter(Filter filter) {		
		this.filter = filter;
	}
	
	public TreeTableNode[] filter(TreeTableNode[] nodes) {
		if (filter == null) { return nodes; }
		Collection<TreeTableNode> filteredNodes = new ArrayList<TreeTableNode>();
		for (int i = 0; i < nodes.length; i++) {
			if (filter.matches(nodes[i])) {
				filteredNodes.add(nodes[i]);
			}
		}		
		TreeTableNode[] filteredNodesArray = new TreeTableNode[filteredNodes.size()];		
		filteredNodes.toArray(filteredNodesArray);
		return filteredNodesArray;
	}
	
}
