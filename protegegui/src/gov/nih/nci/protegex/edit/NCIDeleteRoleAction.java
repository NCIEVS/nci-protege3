package gov.nih.nci.protegex.edit;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JTree;

import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.actions.ResourceAction;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;

/**
 *@Author: NGIT, Kim Ong; Iris Guo
 */

public class NCIDeleteRoleAction extends ResourceAction {
	
	public static final long serialVersionUID = 937456026L;
	
    NCIEditTab tab;

    OWLModel owlModel = null;
    NCIRoleGroupTable table = null;

    NCIRoleGroupTableModel tableModel;
    NCIRoleGroupEditorPanel rolegroupPanel;

    public NCIDeleteRoleAction(NCIRoleGroupEditorPanel rolegroupPanel, NCIEditTab tab,
    		NCIRoleGroupTable table) {
        super("Delete restriction/group", OWLIcons.getDeleteIcon(OWLIcons.ANNOTATION));
        this.rolegroupPanel = rolegroupPanel;
        this.tab = tab;
        this.owlModel = tab.getOWLModel();
        this.table = table;
        this.tableModel = table.getTableModel();
    }


    public void actionPerformed(ActionEvent e) {

		int rowIndex = table.getSelectedRow();
		if (rowIndex < 0)
		{
			 if (tableModel.getRowCount() > 0)
				ProtegeUI.getModalDialogFactory().showMessageDialog(rolegroupPanel, "No item is selected.");
			 return;
		}

		boolean bool_val = confirmDeleteAction("");
		if (!bool_val) return;


		tableModel.deleteRow(rowIndex);
		if (tableModel.getRowCount() > 0)
			table.setRowSelectionInterval(0, 0);

		tab.getEditPanel().enableSaveButton(true);
    }

    public boolean confirmDeleteAction(String msg) {
        return ProtegeUI.getModalDialogFactory().showConfirmDialog(rolegroupPanel,
                "The selected restriction will be deleted. Are you sure you want to delete it?",
                "Confirm Delete");
    }

    public boolean isSuitable(Component component, RDFResource resource) {
        return resource.isEditable() && component instanceof JTree;
    }
}
