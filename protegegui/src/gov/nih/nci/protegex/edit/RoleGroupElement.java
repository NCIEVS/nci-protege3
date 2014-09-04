package gov.nih.nci.protegex.edit;

import java.io.Serializable;

import edu.stanford.smi.protegex.owl.model.OWLRestriction;

public class RoleGroupElement implements Serializable {
	
	public static final long serialVersionUID = 933456026L;

	int rolegroup_number;
	OWLRestriction retriction;


	public RoleGroupElement(int rolegroup_number, OWLRestriction retriction) {
		this.rolegroup_number = rolegroup_number;
		this.retriction = retriction;
	}

	public void setRoleGroupNumber(int rolegroup_number) {
		this.rolegroup_number = rolegroup_number;
	}

	public void setRestriction(OWLRestriction retriction) {
		this.retriction = retriction;
	}

	public int getRoleGroupNumber() {
		return rolegroup_number;
	}

	public OWLRestriction getRestriction() {
		return retriction;
	}
}