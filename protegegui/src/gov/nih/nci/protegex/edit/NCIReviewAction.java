package gov.nih.nci.protegex.edit;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JTree;

import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.ui.actions.ResourceAction;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;
import gov.nih.nci.protegex.dialog.ReviewClassDialog;

public class NCIReviewAction extends ResourceAction {
    private static final long serialVersionUID = 123456030L;
    private NCIEditTab myTab;
    private OWLNamedClass myCls;

    public NCIReviewAction(NCIEditTab tab) {
        super("Review", OWLIcons.getViewIcon(OWLIcons.ANNOTATION));
        setEnabled(false);
        myTab = tab;
        
    }
    
    private void debugCls(String tag, OWLNamedClass cls) {
//      String value = cls != null ? cls.getLocalName() : "null";
//      System.out.println(tag + " = " + value);
//      System.out.flush();
  }
    
    public void setCls(OWLNamedClass cls) {
        debugCls("setCls", myCls);
        myCls = cls;
        setEnabled(myCls != null);
    }
    
    public OWLNamedClass getCls() {
        return myCls;
    }
    
    public void actionPerformed(ActionEvent e) {
        debugCls("actionPerformed", myCls);
        if (myCls == null)
            return;

        myTab.setSelectedInstance(myCls);
        myTab.getEditPanel().setSelectedInstance(myCls);
        myTab.getEditPanel().updateAll();
        new ReviewClassDialog(myTab, myTab.getEditPanel(), myCls);
    }
    
    public boolean isSuitable(Component component, RDFResource resource) {
        return resource instanceof RDFProperty && resource.isEditable() &&
            component instanceof JTree;
    }
}
