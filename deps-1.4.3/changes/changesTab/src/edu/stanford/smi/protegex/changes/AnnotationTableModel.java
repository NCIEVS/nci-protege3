package edu.stanford.smi.protegex.changes;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.table.AbstractTableModel;

import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Timestamp;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;

/**
 * The tabel model for the annotation specific table
 *
 */
public class AnnotationTableModel extends AbstractTableModel{

    public enum Column {
        ANNOTATE_COLNAME_TYPE("Type"),
        ANNOTATE_COLNAME_COMMENTS("Description"),
        ANNOTATE_COLNAME_AUTHOR("Author"),
        ANNOTATE_COLNAME_CREATED("Created");

        private String name;

        private Column(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public int getColumn() {
            return ordinal();
        }
    }

    public static final String TYPE_EXPLANATION = "Explanation";
    public static final String TYPE_QUESTION = "Question";
    public static final String TYPE_SEEALSO = "SeeAlso";
    public static final String TYPE_ADVICE = "Advice";
    public static final String TYPE_EXAMPLE = "Example";
    public static final String TYPE_COMMENT = "Comment";

	private String[] colNames;
	private ArrayList<Annotation> data;
	private KnowledgeBase changeKB;

	public AnnotationTableModel(KnowledgeBase changeKB) {
		this.changeKB = changeKB;

		// Setup the table column size/names

        Column[] cols = Column.values();
        colNames = new String[cols.length];
        for (int i = 0; i < cols.length; i++) {
            colNames[i] = cols[i].getName();
        }

		data = new ArrayList<Annotation>();
	}

	public void update() {
		fireTableDataChanged();
	}


	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return data.size();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return colNames.length;
	}


	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
        if (column >= 0 && column <= colNames.length) {
            return colNames[column];
        }
        return "";
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	@Override
	public Class getColumnClass(int c) {
		if (getValueAt(0, c) == null) {
			return String.class;
		}
		return getValueAt(0, c).getClass();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {

		// This is the instance object,
		// get the particular piece of info out of it.
		Annotation aInst = data.get(row);

		if (aInst == null) {
			return "";
		}

        if (col < 0 || col >= Column.values().length) {
            return "";
        }
		switch(Column.values()[col]) {
        case ANNOTATE_COLNAME_TYPE:
            return ((AbstractWrappedInstance)aInst).getWrappedProtegeInstance().getDirectType().getBrowserText();
        case ANNOTATE_COLNAME_COMMENTS:
            return aInst.getBody();
		case ANNOTATE_COLNAME_AUTHOR:
			return aInst.getAuthor();
		case ANNOTATE_COLNAME_CREATED: {
			Timestamp ts = aInst.getCreated();
			return ts == null ? "" : ts.getDate();
		}

		}

		return "";
	}

	public Object getObjInRow(int row) {
		Instance aInst = ((AbstractWrappedInstance)data.get(row)).getWrappedProtegeInstance();
		return aInst;
	}

	/**
	 * @param index
	 * @return Returns the instance name associated with the given index in the table
	 */
	public String getInstanceName(int index) {
		String name;

		Annotation annotation = data.get(index);
		name = ((AbstractWrappedInstance)annotation).getWrappedProtegeInstance().getName();

		return name;
	}

	/**
	 * @param annotate
	 * Add the given annotation to the internal data structure of instances
	 */
	public void addAnnotationData(Annotation annotate) {
		data.add(annotate);
		fireTableRowsInserted(data.size()-1, data.size()-1);
	}

	/**
	 * @param index
	 * Remove the given annotation with the associated index
	 */
	public void removeAnnotationData(int index) {
		data.remove(index);
		fireTableRowsDeleted(data.size()-1, data.size()-1);

	}

	/**
	 * @param indicies
	 * Remove the given annotations associated with the given indicies
	 */
	public void removeAnnotationData(int[] indicies) {
		for (int i = 0; i < indicies.length; i++) {
			data.remove(indicies[i]-i);
		}
		fireTableRowsDeleted(data.size()-1, data.size()-1);
	}

	/**
	 * @param annotate
	 * @param index
	 * Edit the given annotation with the new annotation at the position specified by the index
	 */
	public void editAnnotationData(Instance annotate, int index) {
		data.remove(index);
		data.add(index, (Annotation) annotate);
		fireTableDataChanged();
	}

	public void setAnnotations(Collection<Annotation> annotations) {
		data = new ArrayList<Annotation>(annotations);

		update();
	}
}
