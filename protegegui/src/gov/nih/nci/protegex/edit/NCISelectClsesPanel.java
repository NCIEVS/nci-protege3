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

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.query.ui.QueryTreeFinderPanel;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.FrameWithBrowserText;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.Validatable;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.ui.ResourceRendererWithBrowserText;
import edu.stanford.smi.protegex.owl.ui.cls.ClassTreeWithBrowserTextRoot;


/**
 *@Author: NGIT, Kim Ong; Iris Guo
 */

public class NCISelectClsesPanel extends JComponent implements Validatable {
	
	private static final long serialVersionUID = 123456078L;
    private JTree _tree;
    private boolean _allowsMultiple;
    

    public NCISelectClsesPanel(KnowledgeBase kb, Collection clses) {
        this(kb, clses, true);
    }

    private NCISelectClsesPanel(KnowledgeBase kb, Collection clses, boolean allowsMultiple) {
        _allowsMultiple = allowsMultiple;        
        
        if (clses.isEmpty()) {    
            clses = kb.getRootClses();
        }        

        _tree = ComponentFactory.createSelectableTree(ModalDialog.getCloseAction(this), new ClassTreeWithBrowserTextRoot(clses, false));        
        _tree.setCellRenderer(new ResourceRendererWithBrowserText((OWLModel) kb) {
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
        _tree.setShowsRootHandles(true);
    
       
        
        _tree.setSelectionRow(0);
        _tree.collapsePath(_tree.getSelectionPath());
        setLayout(new BorderLayout());

        add(new JScrollPane(_tree), BorderLayout.CENTER);

        QueryTreeFinderPanel finder = QueryTreeFinderPanel.getQueryTreeFinderPanel((OWLModel) kb, _tree, true);
        finder.setViewButtonsVisible(false);
        add(finder, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(300, 300));
    }

    public Collection getSelection() {
        Collection sel = ComponentUtilities.getSelection(_tree);
        if (sel == null) { return null;}
        Collection<Frame> clses = new ArrayList<Frame>();
        for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
			FrameWithBrowserText fbt = (FrameWithBrowserText) iterator.next();
			clses.add(fbt.getFrame());
		}        
        return clses;
    }

    public boolean validateContents() {
        boolean isValid = _allowsMultiple || getSelection().size() <= 1;
        if (!isValid) {
            ModalDialog.showMessageDialog(this, "Only 1 class can be selected", ModalDialog.MODE_CLOSE);
        }
        return isValid;
    }

    public void saveContents() { }

}
