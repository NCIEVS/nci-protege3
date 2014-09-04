package gov.nih.nci.protegex.dialog;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.panel.EditPanel;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;

/**
 * @Author: NGIT, Kim Ong; Iris Guo
 */

// public class EditDialog extends JDialog implements ActionListener
public class EditDialog extends JFrame {
    public static final long serialVersionUID = 123456793L;

    JButton okButton, cancelButton;

    NCIEditTab tab;

    EditPanel editpanel;

    Cls cls;

    public EditDialog(NCIEditTab tab, Cls cls) {
        // super((JFrame)tab.getTopLevelAncestor(), cls.getBrowserText(),
        // false);
        this.tab = tab;
        this.cls = cls;
        this.setTitle(cls.getBrowserText());
        init();
    }

    public void init() {
        Container container = this.getContentPane();
        setLocation(250, 150);
        setSize(new Dimension(800, 550));
        container.setLayout(new BorderLayout());

        editpanel = tab.createEditPanel(cls);
        editpanel.setFocusClass((OWLNamedClass) cls);
        if (tab.getProject().isMultiUserClient() && tab.isActionAllowed(tab.MERGE)) {

        } else {
            editpanel.disableAll();
        }
        container.add(editpanel, BorderLayout.NORTH);
        this.toFront();
        this.setVisible(true);

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                if (tab.getEditPanel() != null && tab.getEditPanel().getFocusClass() != null) {
                    OWLNamedClass focusedCls = tab.getEditPanel().getFocusClass();
                    if (focusedCls.getBrowserText().compareTo(cls.getBrowserText()) != 0) {
                        tab.removeFromListenedToClses((OWLNamedClass) cls, editpanel);
                    }
                }
                setVisible(false);
            }
        });

    }
}
