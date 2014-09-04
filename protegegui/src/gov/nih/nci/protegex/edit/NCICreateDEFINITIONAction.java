package gov.nih.nci.protegex.edit;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;

import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.actions.ResourceAction;
import edu.stanford.smi.protegex.owl.ui.dialogs.ModalDialogFactory;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;
import gov.nih.nci.protegex.dialog.CustomizedAnnotationDialog;

/**
 *@Author: NGIT, Kim Ong; Iris Guo
 */

public class NCICreateDEFINITIONAction extends ResourceAction {
	
	private static final long serialVersionUID = 441936038070346945L;
	//060106
	JTextArea desc_ta;
	JComboBox syn_group_combo, source_combo, def_group_combo;
	JTextField name_field, code_field, attr_field, reviewdate_field, reviewer_field;

    OWLModel kb;
    NCIEditTab tab;
    Collection properties;
    Collection excludedProperties = null;
    String title = "Select properties to add";
    NCIAnnotationsTableModel tableModel = null;

    String property_name = null;
    String property_value = null;
    boolean cancelBtnPressed = false;

    int type;

    

    
    public NCICreateDEFINITIONAction(NCIEditTab tab, NCIAnnotationsTableModel tableModel,
           Collection properties, int type)
    {
        super("Add Definition", OWLIcons.getCreateIcon(OWLIcons.ANNOTATION));
        this.tab = tab;
        this.type = type;
        this.kb = tab.getOWLModel();
        this.properties = properties;
        if (properties == null)
        {
			this.properties = kb.getVisibleUserDefinedRDFProperties();
		}
        this.tableModel = tableModel;
    }
    

    


    public void actionPerformed(ActionEvent e) {
        String name = "";
        String prop_value = "";
        RDFProperty property = null;
        if (this.properties != null && this.properties.size() == 1)
        {
			Object[] objs =	this.properties.toArray();
			property = (RDFProperty) objs[0];
			name = property.getPrefixedName();
		}
		else
		{
			Collection allowedSlots = properties;
			if (allowedSlots == null)
			{
				allowedSlots = new ArrayList(kb.getSlots());
			}
			RDFProperty slot = ProtegeUI.getSelectionDialogFactory().selectProperty(tab, kb, allowedSlots);
			//Slot slot = DisplayUtilities.pickSlot(tab, allowedSlots, "Select Property");
			if (slot == null) return;

			name = slot.getPrefixedName();
			property = (RDFProperty) slot;
		}

		if (name.compareTo("") == 0) return;
		if (name.compareTo(NCIEditTab.DEFINITION) != 0) return;

		CustomizedAnnotationDialog dialog
			= new CustomizedAnnotationDialog(tab.getEditPanel(), name, "", -1, tableModel);

		prop_value = dialog.getValue();
		property_value = prop_value;

		
		if (tableModel.alreadyHas(name, property_value))
		{
			Component warning_label = new JLabel("Property already exists.");
			LabeledComponent lc2 = new LabeledComponent("", warning_label);
			ProtegeUI.getModalDialogFactory().showDialog(tab, lc2, "WARNING", ModalDialogFactory.MODE_CLOSE);
			//property_name = "";
			property_value = "";
			cancelBtnPressed = true;
			return;
		}


		if (tableModel != null && prop_value != null && prop_value.compareTo("") != 0)
		{
			//062706
			tableModel.addRow(property, prop_value);
			JTable table = tableModel.getTable();
			if (table != null){
				int newrow = tableModel.getRowCount()-1;
				table.setRowSelectionInterval(newrow, newrow);

			}else
			{
				System.out.println("table is null");
			}

			tab.getEditPanel().enableSaveButton(true);

			//062906
			if (tableModel.getRowCount()==1)
				tab.getEditPanel().enableDelEditHB(type, true);

	    }
	}

	public String getPropertyName()
	{
		return property_name;
	}

	public String getPropertyValue()
	{
		return property_value;
	}

    public boolean isSuitable(Component component, RDFResource resource) {
        return resource instanceof RDFProperty && resource.isEditable() && component instanceof JTree;
    }
}

