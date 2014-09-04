package gov.nih.nci.protegex.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protegex.owl.model.NamespaceManager;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.dialogs.ModalDialogFactory;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.util.UIUtil;

/**
 * This dialog allows the user to create an assignment.
 * 
 * @author Bob Dionne
 */
public class RefactorNamespaceDialog {

	private JTextField tf;

	private JFrame fr = null;

	private JComboBox nameSpacePrefixes = null;

	private NamespaceManager nameSpaceMgr = null;
	private String defaultNameSpace = null;
	private OWLNamedClass owlClass = null;
	
	private NCIEditTab tab = null;

	/**
	 * Constructs this class.
	 * 
	 */
	public RefactorNamespaceDialog(NCIEditTab tab, OWLNamedClass owlCls, NamespaceManager mgr,
			String defNameSpace) {

		owlClass = owlCls;
		nameSpaceMgr = mgr;
		defaultNameSpace = defNameSpace;
		this.tab = tab;
		init();

	}

	/**
	 * Creates the main panel that contains the GUI components for this dialog.
	 */
	private void init() {
		
		

		JPanel panel = new JPanel(new BorderLayout());
		panel.setLocation(450, 300);

		tf = new JTextField();
		tf.setPreferredSize(new Dimension(300, 20));
		tf.setText(owlClass.getLocalName());
		//tf.setEditable(false);
		LabeledComponent ltf = new LabeledComponent("Concept Name:", tf);

		nameSpacePrefixes = new JComboBox();

		Collection<String> list = nameSpaceMgr.getPrefixes();

		ArrayList<Object> sortedList = new ArrayList<Object>(list);
		sortedList = UIUtil.sort(sortedList);
		Iterator<Object> iterator2 = sortedList.iterator();
		while (iterator2.hasNext()) {
			String pf = (String) iterator2.next();
			if (!(pf.length() == 0)) {

				nameSpacePrefixes.addItem(pf);
			} else {
				nameSpacePrefixes.addItem(defaultNameSpace);
			}
		}
		nameSpacePrefixes.setSelectedItem(owlClass.getNamespacePrefix());

		LabeledComponent las = new LabeledComponent("Namespaces:",
				nameSpacePrefixes);
		panel.add(ltf, BorderLayout.NORTH);
		panel.add(las, BorderLayout.CENTER);

		int r = ProtegeUI.getModalDialogFactory().showDialog(fr, panel,
				"Change Namespace", ModalDialogFactory.MODE_OK_CANCEL,
				nameSpacePrefixes);
		if (r == ModalDialogFactory.OPTION_OK) {
			apply();
		}

	}

	/**
	 * Creates an assignment.
	 */
	private boolean apply() {

		String prefix = (String) nameSpacePrefixes.getSelectedItem();
		String ns = nameSpaceMgr.getNamespaceForPrefix(prefix);
		
	
		Cls cls = null;

		if (ns == null) {
			cls = owlClass.rename(tf.getText());

		} else {
			cls = owlClass.rename(ns + tf.getText());
		}
		
		tab.setClassSelectedNew(cls);
		// TODO: Bob, note that this changes the actual underlying frame so
		// some fix up might be required in the various listeners

		return true;
	}
}
