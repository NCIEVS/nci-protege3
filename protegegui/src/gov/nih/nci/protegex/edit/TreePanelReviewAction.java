package gov.nih.nci.protegex.edit;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JTree;

import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.ui.actions.ResourceAction;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;
import gov.nih.nci.protegex.dialog.TreePanelReviewDialog;
import gov.nih.nci.protegex.tree.TreePanel;

public class TreePanelReviewAction extends ResourceAction {
    private static final long serialVersionUID = 123456030L;
    
    private TreePanel _tree = null;
    //private JFrame frame = null;
    

    public TreePanelReviewAction(TreePanel tree) {
        super("Review", OWLIcons.getViewIcon(OWLIcons.ANNOTATION));
        //frame = fr;
        _tree = tree;
        setEnabled(false);        
    }  
    
    
    public void setTreePanel(TreePanel p) {
        _tree = p;
        setEnabled(p != null);
    }
    
    
    
    public void actionPerformed(ActionEvent e) {
        if (_tree == null)
            return;
        
        new TreePanelReviewDialog(_tree);
    }
    
    public boolean isSuitable(Component component, RDFResource resource) {
        return resource instanceof RDFProperty && resource.isEditable() &&
            component instanceof JTree;
    }
}

