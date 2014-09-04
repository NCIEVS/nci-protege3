package edu.stanford.smi.protegex.changes.ui;

import edu.stanford.smi.protege.util.SimpleStringMatcher;
import edu.stanford.smi.protegex.changes.ChangeTableColumn;
import edu.stanford.smi.protegex.changes.TreeTableNode;

public class Filter {

	private ChangeTableColumn column;
	private String filterValue;
	private SimpleStringMatcher matcher;
	
	public Filter(ChangeTableColumn column, String filterValue) {	
		this.column = column;
		this.filterValue = filterValue;
		this.matcher = new SimpleStringMatcher(filterValue);
	}

	public boolean matches(String value) {
		return matcher.isMatch(value);
	}	

	public boolean matches(TreeTableNode node) {		
		Object value = node.getValueAt(column.ordinal());
		if (value == null) { return false; }
		return matcher.isMatch(value.toString());
	}
}
