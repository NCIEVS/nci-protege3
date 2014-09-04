package gov.nih.nci.protegex.edit;

import javax.swing.Icon;

import edu.stanford.smi.protegex.owl.model.OWLIntersectionClass;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.ui.conditions.ConditionsTableItem;

/**
 * @Author: NGIT, Kim Ong; Iris Guo
 */

public class NCIConditionsTableItem extends ConditionsTableItem {

	private boolean isDefining;

	private NCIConditionsTableItem(RDFSClass aClass, int type,
			OWLNamedClass originCls, OWLIntersectionClass definition,
			boolean isNew, boolean isDefining) {
		super(aClass, type, originCls, definition, isNew);
		this.isDefining = isDefining;
	}

	private NCIConditionsTableItem(RDFSClass aClass, int type,
			OWLNamedClass originCls, OWLIntersectionClass definition,
			boolean isNew) {
		super(aClass, type, originCls, definition, isNew);
		isDefining = false;

	}

	public boolean isDefining() {
		return isDefining;
	}

	public void setIsDefining(boolean isDefining) {
		this.isDefining = isDefining;
	}

	public OWLIntersectionClass getDefinition() {
		return super.getDefinition();
	}

	public boolean getIsDefining() {
		return isDefining;
	}

	public int getType() {
		return super.getType();
	}

	public Icon getIcon(int rowHeight) {
		return super.getIcon(rowHeight);
	}

	public static NCIConditionsTableItem create(RDFSClass aClass, int type) {
		return new NCIConditionsTableItem(aClass, type, null, null, false);
	}

	public static NCIConditionsTableItem createInherited(RDFSClass aClass,
			OWLNamedClass originCls) {
		return new NCIConditionsTableItem(aClass, TYPE_INHERITED, originCls,
				null, false);
	}

	public static NCIConditionsTableItem createNew(int type) {
		return new NCIConditionsTableItem(null, type, null, null, true);
	}

	public static NCIConditionsTableItem createSufficient(RDFSClass aClass,
			int type, OWLIntersectionClass definition) {
		return new NCIConditionsTableItem(aClass, type, null, definition, false);
	}

	public static NCIConditionsTableItem createSeparator(int type) {
		return new NCIConditionsTableItem(null, type, null, null, false);
	}

	public RDFSClass getCls() {
        
		return aClass;
	}

	public OWLNamedClass getOriginCls() {
		return super.getOriginCls();
	}

	public boolean isDefinition() {
		if (isDefining) {
			return true;
		} else {
			return super.isDefinition();
		}

	}

	public boolean isInherited() {
		return super.isInherited();
	}

	public boolean isNew() {
		return super.isNew();
	}

	public boolean isSeparator() {
		return aClass == null && !isNew();
	}

	void setLocalIndex(int value) {
	}

	

	public void setCls(RDFSClass cls) {
		this.aClass = cls;
	}

	public boolean getIsNew() {
		return isNew();
	}

	public NCIConditionsTableItem createClone() {
		return new NCIConditionsTableItem(this.getCls(), this.getType(), this
				.getOriginCls(), // inheritance
				this.getDefinition(), this.isNew(), this.isDefining());
	}

	
	public String getDisplayText() {
		return aClass.getBrowserText();
	}

}
