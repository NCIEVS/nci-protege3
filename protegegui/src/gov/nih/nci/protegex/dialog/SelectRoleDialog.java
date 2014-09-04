package gov.nih.nci.protegex.dialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.ui.DisplayUtilities;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.panel.PartonomyPanel;
import gov.nih.nci.protegex.util.MsgDialog;

/**
 *@Author: NGIT, Kim Ong; Iris Guo
 */

public class SelectRoleDialog extends JDialog implements ActionListener
{
    private static final long serialVersionUID = 123456035L;
    JButton okButton, cancelButton;
	JButton loadButton;
	JTextField tf;
	String separator;

	String role_name;
	String role_value;
	String role_modifier;
	String role_modifier_label;

	int selectedIndex = -1;


	JButton selButton;
	JButton clsButton;
	KnowledgeBase kb;

	OWLModel owlModel;

	NCIEditTab tab;

	JList kindList;
	JList roleList;
	PartonomyPanel partonomyPanel;
	
	Vector<String> transPropsList = null;

    public SelectRoleDialog(NCIEditTab tab, KnowledgeBase kb, Vector<String> tpl)
    {
		super((JFrame)tab.getTopLevelAncestor(), "Select Transitive Properties", true);
		this.kb = kb;
		this.owlModel = (OWLModel) kb;
		this.tab = tab;
		partonomyPanel = tab.getPartonomyPanel();
		selectedIndex = -1;
		transPropsList = tpl;
		init();

	}

    


    
	public void init()
	{
		Container contain = this.getContentPane();

		this.setLocation(360,300);
		
		contain.setLayout(new BorderLayout());


        JPanel clsPanel = new JPanel();
        clsPanel.setLayout(new BorderLayout());


		clsButton = new JButton("Root");
		clsButton.addActionListener(this);
		tf = new JTextField();
		if (tab.getSelectedInstance() != null)
		{
			Cls cls = (Cls) tab.getSelectedInstance();
			String sel_cls_name = tab.getOWLWrapper().getInternalName(cls);

			if (sel_cls_name.compareTo("owl:Thing") != 0)
			{
				tf.setText(sel_cls_name);
		    }
		}
		tf.setEditable(false);

		tf.setPreferredSize(new Dimension (240, 20));

		clsPanel.add(tf, BorderLayout.CENTER);
		clsPanel.add(clsButton, BorderLayout.EAST);

		contain.add(clsPanel, BorderLayout.NORTH);

        roleList = new JList(transPropsList);

        JScrollPane roleScrollPane = new JScrollPane(roleList);
        roleScrollPane.setPreferredSize(new Dimension(300, 150));
        JPanel rolePanel = new JPanel();
        rolePanel.setLayout(new BorderLayout());
        rolePanel.add(BorderLayout.NORTH, new LabeledComponent("Restrictions (Press Ctrl to make multiple selection.)", roleScrollPane));
        rolePanel.add(BorderLayout.CENTER, new JPanel());        

		contain.add(rolePanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		okButton = new JButton("OK");

        if (transPropsList.size() > 0)
        {
			roleList.setSelectedIndex(0);
		}
		else
		{
			okButton.setEnabled(false);
		}

		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		contain.add(buttonPanel, BorderLayout.SOUTH);

		pack();
		this.setVisible(true);
   	}


	public void actionPerformed(ActionEvent event)
	{
		Object action = event.getSource();
		if (action == okButton)
		{
            String cls_name = tf.getText();
			Cls cls = tab.getWrapper().getOWLNamedClass(cls_name);
			if (cls == null)
			{
                MsgDialog.ok(this, "Concept " + cls_name + " not found.");
				return;
			}


			partonomyPanel.draw(tf.getText(), roleList.getSelectedValues());
			dispose();
		}else if (action == cancelButton){
			dispose();
		}else if (action == clsButton)	{
			Collection clses = kb.getRootClses();
			Cls cls = DisplayUtilities.pickConcreteCls(clsButton, kb, clses);
			if (cls == null)
				System.out.println("cls == null");
			else
				tf.setText(tab.getOWLWrapper().getInternalName(cls));
		}
	}

}