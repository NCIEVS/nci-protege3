package gov.nih.nci.protegex.panel;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.*;
import gov.nih.nci.protegex.dialog.SelectRoleDialog;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.edit.OWLWrapper;
import gov.nih.nci.protegex.util.MsgDialog;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;


/**
 *@Author: NGIT, Kim Ong; Iris Guo
 */

public class PartonomyPanel extends JPanel implements ActionListener, PanelDirty
{
	public static final long serialVersionUID = 123456790L;
	
	private Logger logger = Log.getLogger(getClass());
	
	NCIEditTab tab;
	Instance selectedInstance;
	int selected_tab;

    JTree edit_tree;
    JScrollPane treeView;
    DefaultTreeModel model;

    DefaultMutableTreeNode root_node;
    OWLModel kb;

    Vector<Object> role_vec;
    OWLWrapper wrapper;

	int total=0;

	HashSet<String> visitedNodes;
	
	private JButton treeButton_Partonomy, saveButton_Partonomy,
	clearButton_Partonomy;
	
	public boolean isDirty() {
		return this.clearButton_Partonomy.isEnabled();
	}
	
	public void reset() {
		// currently noop
	}

    public PartonomyPanel(NCIEditTab tab, OWLModel kb, OWLWrapper wrapper)
    {
    	super(new BorderLayout());
		this.tab = tab;
		this.kb = kb;
		this.wrapper = wrapper;

		role_vec = new Vector<Object>();

		initialize();
		this.kb = tab.getOWLModel();
		
		init();
	}
    
    private void init() {
    	
		JPanel buttonPanel = new JPanel();

		treeButton_Partonomy = new JButton("Tree");
		treeButton_Partonomy.addActionListener(this);

		saveButton_Partonomy = new JButton("Save");
		saveButton_Partonomy.addActionListener(this);

		clearButton_Partonomy = new JButton("Clear");
		clearButton_Partonomy.addActionListener(this);

		buttonPanel.add(treeButton_Partonomy);
		buttonPanel.add(saveButton_Partonomy);
		buttonPanel.add(clearButton_Partonomy);

		treeButton_Partonomy.setEnabled(true);
		saveButton_Partonomy.setEnabled(false);
		clearButton_Partonomy.setEnabled(false);

		add(buttonPanel, BorderLayout.SOUTH);

		
    }
    
    public void actionPerformed(ActionEvent e) {
    	
    	if (e.getSource() == treeButton_Partonomy) {
			

			try {
				new SelectRoleDialog(tab, kb, getTransPropertiesList());
			} catch (Exception ex) {
				Log.getLogger().log(Level.WARNING, "Exception caught", ex);
			}
		}

		else if (e.getSource() == saveButton_Partonomy) {

			try {

				JFileChooser fc = new JFileChooser();
				int returnVal = fc.showOpenDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fc.getSelectedFile();

					if (selectedFile != null) {
						String outfile = selectedFile.toString();
						logger.info("Selected file: " + outfile);
						PrintWriter pw = new PrintWriter(new BufferedWriter(
								new FileWriter(outfile)));
						boolean retval = outputTree(pw);

						if (retval) {
							// write partonomy tree data to a file
							MsgDialog.ok(this, "Output file " + outfile
									+ " generated.");
						}
						pw.close();
					} else {
						logger.info("File not found.");
					}
				}
			} catch (Exception ex) {
				logger.log(Level.WARNING, "Exception caught", ex);
			}
		}

		else if (e.getSource() == clearButton_Partonomy) {
			clear();
		}
    	
    	
    }

   	public void initialize()
   	{
		

		edit_tree = new JTree(root_node);
		edit_tree.setEditable(true);

		edit_tree.getSelectionModel().setSelectionMode
				(TreeSelectionModel.SINGLE_TREE_SELECTION);

		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setLeafIcon(new ImageIcon("images/middle.gif"));
		edit_tree.setCellRenderer(renderer);

        treeView = new JScrollPane(edit_tree);
		setLayout(new BorderLayout());
		add(treeView, BorderLayout.CENTER);

		visitedNodes = new HashSet<String>();
    }
   	
   	public void clear()
   	{
   		root_node = null;
		selectedInstance = null;
		visitedNodes = new HashSet<String>();
		
		edit_tree = new JTree(root_node);
		edit_tree.getSelectionModel().setSelectionMode
				(TreeSelectionModel.SINGLE_TREE_SELECTION);

		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setLeafIcon(new ImageIcon("images/middle.gif"));
		edit_tree.setCellRenderer(renderer);

		treeView.setViewportView(edit_tree);
		
		enableButton("saveButton_Partonomy", false);
		enableButton("clearButton_Partonomy", false);
		

		

		
    }

    


    public void draw(String cls_name, Object[] roles)
    {
		total=0;
		role_vec = new Vector<Object>();
		for (int i=0; i<roles.length; i++)
		{
			role_vec.add(roles[i]);
		}

		Cls cls = wrapper.getOWLNamedClass(cls_name);
		if (cls == null)
		{
            MsgDialog.ok(this, "Class " + cls_name + " not found.");
			return;
		}

		createTree(wrapper.getOWLNamedClass(cls_name));
    }


    private void createTree(Cls cls)
    {
		selectedInstance = cls;
		String name = tab.getWrapper().getInternalName(cls);//selectedInstance.getBrowserText();
		root_node = new DefaultMutableTreeNode(name);

		edit_tree = new JTree(root_node);
		edit_tree.setEditable(true);

		model = new DefaultTreeModel(root_node);

		visitedNodes.clear();

		for (int i=0; i<role_vec.size(); i++)
		{
			String arole = (String)role_vec.elementAt(i);
			traverseUp(root_node, arole, (OWLNamedClass) cls, 1);
		}

		if (total == 0)
            MsgDialog.warning(null, "No partonomy data to display for "+ name);
		else{
			edit_tree = new JTree(root_node);
			edit_tree.getSelectionModel().setSelectionMode
					(TreeSelectionModel.SINGLE_TREE_SELECTION);

			DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
			renderer.setLeafIcon(new ImageIcon("images/middle.gif"));
			edit_tree.setCellRenderer(renderer);

			treeView.setViewportView(edit_tree);

			enableButton("saveButton_Partonomy", true);
			enableButton("clearButton_Partonomy", true);

		}
    }
    
    public void enableButton(String buttonLabel, boolean state) {
		

		if (buttonLabel.compareTo("treeButton_Partonomy") == 0)
			treeButton_Partonomy.setEnabled(state);
		else if (buttonLabel.compareTo("saveButton_Partonomy") == 0)
			saveButton_Partonomy.setEnabled(state);
		else if (buttonLabel.compareTo("clearButton_Partonomy") == 0)
			clearButton_Partonomy.setEnabled(state);

		
	}


   void traverseUp(DefaultMutableTreeNode parNode, String roleName, OWLNamedClass cls, int level)
   {
		int nextLevel = level + 1;
		String conName = tab.getWrapper().getInternalName(cls);
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();

		Vector referring_clses = wrapper.getRestrictionSources(conName, roleName);
		if (referring_clses!=null && referring_clses.size()!=0)
		{
			for (int i=0; i<referring_clses.size(); i++){
				Cls referring_cls = (Cls) referring_clses.elementAt(i);
				String name = tab.getWrapper().getInternalName(referring_cls);//.getBrowserText();

				if (!visitedNodes.contains(name))
				{
					total++;
					visitedNodes.add(name);
					DefaultMutableTreeNode childNode;
					if (name.equalsIgnoreCase(root.toString()))
						childNode = new DefaultMutableTreeNode(name+" (inverse "+roleName+")");
					else
						childNode = new DefaultMutableTreeNode(name+" (inverse "+roleName+")");
					parNode.add(childNode);

					traverseUp(childNode, roleName, (OWLNamedClass) referring_cls, nextLevel);
			    }
			}
		}
		
	}


	public boolean outputTree(PrintWriter pw)
	{
		if (pw == null) return false;
		if (root_node == null) return false;
        int level = 0;
		outputNode(pw, root_node, level);

		return true;
	}

	private void outputNode(PrintWriter pw, DefaultMutableTreeNode node, int level)
	{
		if (node == null) return;
		outputNodeLabel(pw, node, level);
		int n = node.getChildCount();
		if (n == 0) return;
		level++;
		Enumeration e = node.children();
		while (e.hasMoreElements())
		{
			DefaultMutableTreeNode childnode = (DefaultMutableTreeNode) e.nextElement();
			outputNode(pw, childnode, level);
		}
	}


	private void outputNodeLabel(PrintWriter pw, DefaultMutableTreeNode node, int level)
	{
		if (node == null) return;
		String indent = "";
		for (int i=0; i<level; i++)
		{
			indent = indent + "\t";
		}
		String s = (String)node.getUserObject().toString();
		pw.println(indent + s);
	}
	
	private Vector<String> transPropList = null;
	
	private Vector<String> getTransPropertiesList()
    {
		if (transPropList == null) {
		transPropList = new Vector<String>();
		try {
			//for (Iterator it = owlModel.listRDFProperties(); it.hasNext();) {
			Collection allProperties = kb.getVisibleUserDefinedRDFProperties();
			for (Iterator it = allProperties.iterator(); it.hasNext();) {
				RDFProperty rdfproperty = (RDFProperty)it.next();

				if (!rdfproperty.isAnnotationProperty()){
					if (rdfproperty instanceof OWLObjectProperty){
						OWLObjectProperty property = (OWLObjectProperty) rdfproperty;
						if (property.isTransitive())
							//v.add(property.getBrowserText());
							transPropList.add(property.getPrefixedName());
					}
				}
			}
			if (transPropList.size() == 0)
				System.out.println("WARNING: No applicable part of relationship available.");
	    } catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Exception caught", e);
		}
		}
		return transPropList;
	    
        
    }

}