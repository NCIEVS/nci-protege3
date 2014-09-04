/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License");  you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 *
 * The Original Code is Protege-2000.
 *
 * The Initial Developer of the Original Code is Stanford University. Portions
 * created by Stanford University are Copyright (C) 2005.  All Rights Reserved.
 *
 * Protege was developed by Stanford Medical Informatics
 * (http://www.smi.stanford.edu) at the Stanford University School of Medicine
 * with support from the National Library of Medicine, the National Science
 * Foundation, and the Defense Advanced Research Projects Agency.  Current
 * information about Protege can be obtained at http://protege.stanford.edu.
 *
 */

package gov.nih.nci.protegex.edit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import edu.stanford.smi.protege.query.ui.QueryTreeFinderPanel;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.FrameWithBrowserText;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.Validatable;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.ui.ResourceRendererWithBrowserText;
import edu.stanford.smi.protegex.owl.ui.cls.ClassTreeWithBrowserTextRoot;



/**
 *@Author: NGIT, Kim Ong; Iris Guo
 */

public class NCISelectSuperClsPanel extends JComponent implements Validatable {
	
	public static final long serialVersionUID = 923456029L;
    private JTree _tree;
    private boolean _allowsMultiple;
    JCheckBox definingCheckBox;
    

    

    

    public NCISelectSuperClsPanel(OWLModel kb) {
        this(kb, true);
    }

    public NCISelectSuperClsPanel(OWLModel kb, boolean allowsMultiple) {

		

        _allowsMultiple = allowsMultiple;
        Collection clses = OWLWrapper.getInstance().getSelectableRoots();
        
        _tree = ComponentFactory.createSelectableTree(ModalDialog.getCloseAction(this), new ClassTreeWithBrowserTextRoot(clses, false));
        //_tree = ComponentFactory.createSelectableTree(ModalDialog.getCloseAction(this), new ParentChildRoot(clses));
        _tree.setShowsRootHandles(true); 
        //_tree.setCellRenderer(FrameRenderer.createInstance());
        
        _tree.setCellRenderer(new ResourceRendererWithBrowserText(kb) {
            
            
    	public void load(Object value) {
         	if (value instanceof FrameWithBrowserText) {
         		FrameWithBrowserText fbt = (FrameWithBrowserText) value;
         		setMainText(fbt.getBrowserText());
         		if (fbt.getFrame() != null) {             
         			setMainIcon(Icons.getClsIcon());
         		}
         	} else {
         		super.load(value);
         	}
    	 }
         
         
    	 
    });
        
        //_tree = ComponentFactory.createSelectableTree(ModalDialog.getCloseAction(this), new ParentChildRoot(clses));
        //_tree.setShowsRootHandles(true);        
        //_tree.setCellRenderer(FrameRenderer.createInstance());
        
        _tree.expandRow(0);
        
        /**
        
        int rows = _tree.getRowCount();
        int diff = rows - clses.size();
        for (int i = rows - 1; i > diff; --i) {
            _tree.expandRow(i);
        }
        
        **/
        
        
        
        _tree.setSelectionRow(0);
        setLayout(new BorderLayout());

        JPanel definingPanel = new JPanel();
        definingPanel.setLayout(new BorderLayout());
        JLabel definingLabel = new JLabel("  Defining");
        definingCheckBox = new JCheckBox();
        definingPanel.add(definingCheckBox, BorderLayout.WEST);
        definingPanel.add(definingLabel, BorderLayout.CENTER);

        add(definingPanel, BorderLayout.NORTH);
        add(new JScrollPane(_tree), BorderLayout.CENTER);

//120606
        //ClsTreeFinder finder = new ClsTreeFinder(kb, _tree);
        QueryTreeFinderPanel finder = QueryTreeFinderPanel.getQueryTreeFinderPanel((OWLModel) kb, _tree, true);
        finder.setViewButtonsVisible(false);

        add(finder, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(300, 300));

    }

    public Collection<OWLNamedClass> getSelection() {
    	
    	Collection coll = ComponentUtilities.getSelection(_tree);
    	Collection<OWLNamedClass> res = new ArrayList<OWLNamedClass>();
    	Iterator it = coll.iterator();
    	
    	while (it.hasNext()) {
    		FrameWithBrowserText fbt = (FrameWithBrowserText) it.next();
    		OWLNamedClass cls = (OWLNamedClass) fbt.getFrame();
    		res.add(cls);    		
    	}
    	
    	return res;
    	
    }

    public boolean validateContents() {
        boolean isValid = _allowsMultiple || getSelection().size() <= 1;
        if (!isValid) {
            ModalDialog.showMessageDialog(this, "Only 1 class can be selected", ModalDialog.MODE_CLOSE);
        }
        return isValid;
    }

    public void saveContents() {
        // do nothing
    }

    public boolean isDefining()
    {
		return definingCheckBox.isSelected();
	}
}
