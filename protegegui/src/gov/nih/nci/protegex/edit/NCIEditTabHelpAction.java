package gov.nih.nci.protegex.edit;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;

/**
 * An Action that shows the form editor for the direct type of a given Instance.
 *
 * @author Holger Knublauch  <holger@knublauch.com>
 */
public class NCIEditTabHelpAction extends AbstractAction {
	
	private static final long serialVersionUID = 441936038070346935L;

    private String applicationPathname;
    private String filename;



    public NCIEditTabHelpAction(String applicationPathname, String filename) {
        super("Online Help...", OWLIcons.getImageIcon("CheckConsistency"));
        this.applicationPathname = applicationPathname;
        this.filename = filename;
    }


    public void actionPerformed(ActionEvent e) {
		try {
    	    Runtime.getRuntime().exec(applicationPathname + " " + filename);
	    } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Unable to open User Guide -- please check NCIEditTab.cfg and try again.");
		}
    }

}
