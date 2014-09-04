package gov.nih.nci.protegex.dialog;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.ui.DisplayUtilities;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protegex.owl.model.*;

import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFSNames;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.dialogs.ModalDialogFactory;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.edit.NCISelectClsesPanel;
import gov.nih.nci.protegex.edit.OWLWrapper;
import gov.nih.nci.protegex.util.MsgDialog;

/**
 *@Author: NGIT, Kim Ong; Iris Guo
 */

public class CreateObjPropertyDialog extends JDialog
{
    private static final long serialVersionUID = 5037254126870264720L;
    private static final String TITLE = "Add an Object-Valued Property";
    
	private KnowledgeBase kb;
	private OWLModel owlModel;
	private NCIEditTab tab;
	private OWLWrapper wrapper = OWLWrapper.getInstance();

    private String property_name;
    private String property_value;

	private RDFProperty selected_property;
    private Cls selectedCls = null;

	private JTextField name_field;
	private JTextField value_field;
    private boolean cancelBtnPressed;

    public CreateObjPropertyDialog(NCIEditTab tab, String title, 
        RDFProperty property, Cls cls)
    {
        super((JFrame)tab.getTopLevelAncestor(), TITLE, true);
        selected_property = property;
        selectedCls = cls;
        property_name = selected_property == null ? "" : selected_property.getBrowserText();
        property_value = selectedCls == null ? "" : wrapper.getInternalName(cls);
        initialize(tab, title);
    }

    private void initialize(NCIEditTab tab, String title) {
        this.tab = tab;
        this.kb = tab.getOWLModel();
        this.owlModel = (OWLModel) kb;

        cancelBtnPressed = false;

        JPanel panel = createPanel();
        int r = ProtegeUI.getModalDialogFactory().showDialog(tab, panel, title, 
            ModalDialogFactory.MODE_OK_CANCEL);

        if (r != ModalDialogFactory.OPTION_OK) {
            cancelBtnPressed = true;
            return;
        }
            
        property_name = name_field.getText().trim();
        property_value = value_field.getText().trim();

        if (property_name.equals("") || property_value.equals("")) {
            MsgDialog.ok(this, "Incomplete data entry.");
            cancelBtnPressed = true;
            return;
        }
    }
    
    public RDFProperty getSelectedProperty()
    {
        return selected_property;
    }

    public Cls getSelectedCls()
    {
        return selectedCls;
    }

    public boolean isCancelled()
    {
		return cancelBtnPressed;
	}

    private ArrayList<RDFProperty> getExcludedProperties() {
        ArrayList<RDFProperty> allProperties = 
            new ArrayList<RDFProperty>(owlModel.getVisibleUserDefinedRDFProperties());
        ArrayList<RDFProperty> properties = new ArrayList<RDFProperty>();
		for (Iterator<RDFProperty> it = allProperties.iterator(); it.hasNext();) {
			RDFProperty property = (RDFProperty) it.next();
			if (!property.isAnnotationProperty()) {
				properties.add(property);
			}
		}
		return properties;
    }

    private ArrayList<RDFProperty> getSelectableResources() {
        ArrayList<RDFProperty> properties = new ArrayList<RDFProperty>();
        ArrayList<RDFProperty> allowedProperties = 
            new ArrayList<RDFProperty>(owlModel.getRDFProperties());
        ArrayList<RDFProperty> disallowedProperties = getExcludedProperties();
        for (Iterator<RDFProperty> it = allowedProperties.iterator(); it.hasNext();) {
            RDFProperty property = (RDFProperty) it.next();
            if (property.isVisible() && property.hasObjectRange() && !property.isSystem()
                && !disallowedProperties.contains(property)) {
                properties.add(property);
            }
        }
        properties.add(owlModel.getOWLDisjointWithProperty());
        properties.add(owlModel.getOWLDifferentFromProperty());
        properties.add(owlModel.getOWLEquivalentPropertyProperty());
        properties.add(owlModel.getOWLSameAsProperty());
        properties.add(owlModel.getRDFProperty(RDFSNames.Slot.IS_DEFINED_BY));
        properties.add(owlModel.getRDFProperty(RDFSNames.Slot.SEE_ALSO));
        return properties;
    }


	public JPanel createPanel()
	{
		JPanel panel = new JPanel();
		panel.setLocation(450,300);
		panel.setLayout(new GridLayout(2, 1));

        LabeledComponent lc = null;

		name_field = new JTextField(property_name);
		name_field.setText(property_name);
		name_field.setEditable(false);
		name_field.setPreferredSize(new Dimension (300, 20));
		lc = new LabeledComponent("Select a property", name_field);

		Action SelectPropertyAction =
			new AbstractAction("Select a property ...", 
			    OWLIcons.getCreatePropertyIcon(OWLIcons.OWL_OBJECT_ANNOTATION_PROPERTY)) {
                private static final long serialVersionUID = -5655782185286152307L;

                public void actionPerformed(ActionEvent e)
				{
					ArrayList<RDFProperty> allowedSlots = getSelectableResources();
					Slot slot = DisplayUtilities.pickSlot(
					    new TextField(), allowedSlots, "Select a property");
					RDFProperty property = (RDFProperty) slot;

					name_field.setText(property.getBrowserText());
					selected_property = property;
				}

			};


	    lc.addHeaderButton(SelectPropertyAction);
		panel.add(lc);


		value_field = new JTextField(property_value);
		value_field.setText(property_value);
		value_field.setEditable(false);
		value_field.setPreferredSize(new Dimension (300, 20));
		lc = new LabeledComponent("Select a property value", value_field);

		Action SelectPropertyValueAction =
			new AbstractAction("Select a property value...", OWLIcons.getAddIcon("PrimitiveClass")) {
                private static final long serialVersionUID = -4209569203166709382L;

                public void actionPerformed(ActionEvent e)
				{
					String label = "Select a property value";

					Collection clses = wrapper.getSelectableRoots();
					
					final NCISelectClsesPanel p = new NCISelectClsesPanel((OWLModel) kb, clses);
					int result = ModalDialog.showDialog(tab, p, label, ModalDialog.MODE_OK_CANCEL);
					if (result == ModalDialogFactory.OPTION_OK)
					{
						Collection c = p.getSelection();
						if (c != null && c.size() > 0)
						{
							Iterator it = c.iterator();
							Object obj = it.next();
							selectedCls = (Cls) obj;
							
							//String cls_name = tab.getWrapper().getInternalName(selectedCls);
							String cls_name = ((OWLNamedClass) selectedCls).getBrowserText();
							//if (cls.getBrowserText() == null)
							if (cls_name == null)
							{
								 ProtegeUI.getModalDialogFactory().showErrorMessageDialog(tab, "Invalid selection -- property value is missing.");
								 return;
							}
							//value_field.setText(cls.getBrowserText());
							value_field.setText(cls_name);
						}
					}

				}

			};

	    lc.addHeaderButton(SelectPropertyValueAction);
		panel.add(lc);
		return panel;
   	}
}

