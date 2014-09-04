package gov.nih.nci.protegex.edit;

import gov.nih.nci.protegex.panel.*;

import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import edu.stanford.smi.protegex.owl.ui.actions.ResourceAction;

import edu.stanford.smi.protegex.owl.model.OWLModel;

/**
 *@Author: NGIT, Kim Ong; Iris Guo
 */

public class NCIDeletePropertyAction extends ResourceAction {
	
	public static final long serialVersionUID = 938456026L;
	
    NCIEditTab tab;
    EditPanel editPanel = null;

    OWLModel owlModel = null;
    NCIAnnotationsTable table = null;
    NCIFULLSYNTable table_2 = null;
    NCIQualifierTableModel qualifier_model = null;

    int table_type = 0;
    int type;

    NCIAnnotationsTableModel tableModel;

    public NCIDeletePropertyAction(NCIEditTab tab, EditPanel ep, NCIAnnotationsTable table, int type) {
        super("Delete property", OWLIcons.getDeleteIcon(OWLIcons.ANNOTATION));
        this.tab = tab;
        this.editPanel = ep;
        this.owlModel = tab.getOWLModel();

        this.table = table;
        this.table_type = 1;
        this.type=type;

        this.tableModel = table.getTableModel();
    }

    public NCIDeletePropertyAction(NCIEditTab tab, EditPanel ep, NCIAnnotationsTable table,
    		NCIQualifierTableModel qualifier_model, int type) {
        super("Delete property", OWLIcons.getDeleteIcon(OWLIcons.ANNOTATION));
        this.tab = tab;
        this.editPanel = ep;
        this.owlModel = tab.getOWLModel();
        this.qualifier_model = qualifier_model;

        this.table = table;
        this.table_type = 1;
		this.type = type;

        this.tableModel = table.getTableModel();
    }

    public NCIDeletePropertyAction(NCIEditTab tab, EditPanel ep, NCIFULLSYNTable table) {
        super("Delete full synonym", OWLIcons.getDeleteIcon(OWLIcons.ANNOTATION));
        this.tab = tab;
        this.editPanel = ep;
        this.owlModel = tab.getOWLModel();

        this.table_2 = table;
        this.table_type = 2;

        //this.tableModel_fullsyn = (NCIAnnotationsTableModel) table.getTableModel();
    }

    public void actionPerformed(ActionEvent e) {

        if (table_type == 1)
        {
			int rowIndex = table.getSelectedRow();
			if (rowIndex < 0)
			{
				 if (tableModel.getRowCount() > 0)
				 {
					ProtegeUI.getModalDialogFactory().showMessageDialog(owlModel, "No item is selected.");
				 }
				 return;
			}

			String propertyname = table.getTableModel().getPropertyName(rowIndex);
            if (tab.getOWLWrapper().isReadOnlyProperty(propertyname))
            {
				 ProtegeUI.getModalDialogFactory().showMessageDialog(owlModel, propertyname + " is read only.");
				 return;
			}

			//013007
			else if (propertyname.compareTo("rdfs:label") == 0)
			{
				 ProtegeUI.getModalDialogFactory().showMessageDialog(owlModel, "Cannot delete " + propertyname + " property.");
				 return;
			}


			boolean bool_val = confirmDeleteAction("");
			if (!bool_val) return;

        	tableModel.deleteRow(rowIndex);

 			if (tableModel.getRowCount() > 0)
 			{
 				table.setRowSelectionInterval(0, 0);
 				tab.getEditPanel().updateQualifierTable(tableModel.get_qualifier_tblmodel_id());
			}
        	else if (qualifier_model != null)
        	{
				qualifier_model.initialize();
			}

			//062906 annotation
			if (table.getTableModel().getRowCount()==0) {
				//tab.getEditPanel().enableDelEditHB(type, false);
				//tab.getEditPanel().enableAddButtons()
				
			}
				
		}

        else if (table_type == 2)
        {
 			int rowIndex = table_2.getSelectedRow();
			if (rowIndex < 0)
			{
				 if (table_2.getTableModel().getRowCount() > 0)
				 {
					ProtegeUI.getModalDialogFactory().showMessageDialog(owlModel, "No item is selected.");
				 }
				 return;
			}

			boolean bool_val = confirmDeleteAction("");
			if (!bool_val) return;

			table_2.getTableModel().deleteRow(rowIndex);
			if (table_2.getTableModel().getRowCount()==0)
				tab.getEditPanel().enableDelEditHB(1, false);

		}
        editPanel.enableSaveButton(true);
    }

    public boolean confirmDeleteAction(String msg) {
        return ProtegeUI.getModalDialogFactory().showConfirmDialog(owlModel,
                "The selected property will be deleted. Are you sure you want to delete it?",
                "Confirm Delete");
    }

    public boolean isSuitable(Component component, RDFResource resource) {
        return resource instanceof RDFProperty && resource.isEditable() && component instanceof JTree;
    }
}
