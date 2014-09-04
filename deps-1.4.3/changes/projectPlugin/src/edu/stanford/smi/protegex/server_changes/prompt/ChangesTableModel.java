package edu.stanford.smi.protegex.server_changes.prompt;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Timestamp;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.impl.DefaultOntology_Component;
import edu.stanford.smi.protege.model.Instance;

public class ChangesTableModel extends DefaultTableModel {
    private static final long serialVersionUID = -2032588342562373978L;

    public enum Column {
	    CONTEXT("Context"), APPLIED_TO("Applied to"), AUTHOR("Author"), TIMESTAMP("Timestamp");

	    private String name;
	    private Column(String name) {
	        this.name = name;
	    }
	    public String getName() {
	        return name;
	    }
	}

	private Ontology_Component ontologyComp;

	private List<Change> changesList;

	private boolean showAllChanges = false;


	public ChangesTableModel(Ontology_Component ontologyComp) {
		super();
		this.ontologyComp = ontologyComp;
		//this.changesList = (ontologyComp == null ? new ArrayList<Instance>() : (ontologyComp).getSortedTopLevelChanges());
		refillTableValues();

		for (Column col : Column.values()) {
		    addColumn(col.getName());
		}
	}

	@Override
	public Class getColumnClass(int columnIndex) {
	    if (columnIndex >=0 && columnIndex < Column.values().length) {
	        switch (Column.values()[columnIndex]) {
	        case CONTEXT:
	            return String.class;
	        case APPLIED_TO:
	            return Instance.class;
	        case AUTHOR:
	            return String.class;
	        case TIMESTAMP:
	            return Timestamp.class;
	        default:
	            throw new UnsupportedOperationException("Programmer missed a case");
	        }
	    }
	    return String.class;
	}

	@Override
	public String getColumnName(int column) {
	    return Column.values()[column].getName();
	}

	@Override
	public int getColumnCount() {
		return Column.values().length;
	}

	@Override
	public int getRowCount() {
		return changesList == null ? 0 : changesList.size();
	}

	public void setOntologyComponent(Ontology_Component ontComp) {
		this.ontologyComp = ontComp;

		refillTableValues();
		fireTableDataChanged();
	}

	@Override
	public Object getValueAt(int row, int column) {
	    if (column >=0 && column < Column.values().length) {
	        switch (Column.values()[column]) {
	        case CONTEXT:
	            return changesList.get(row).getContext();
	        case APPLIED_TO:
	            return changesList.get(row).getApplyTo();
	        case AUTHOR:
	            return changesList.get(row).getAuthor();
	        case TIMESTAMP:
	            return changesList.get(row).getTimestamp();
	        default:
	            throw new UnsupportedOperationException("Programmer missed a case");
	        }
	    }
	    return null;
	}


	private void refillTableValues() {
		//fishy
		changesList = ontologyComp == null ? new ArrayList<Change>() :
			showAllChanges ? ((DefaultOntology_Component)ontologyComp).getSortedChanges() :
				((DefaultOntology_Component)ontologyComp).getSortedTopLevelChanges();
	}

	public Change getChange(int row) {
		if (row >= 0 && row < changesList.size()) {
			return changesList.get(row);
		}

		return null;
	}

	public void setShowAllChanges(boolean showAllChanges) {
		this.showAllChanges = showAllChanges;

		refillTableValues();
		fireTableDataChanged();
	}

}
