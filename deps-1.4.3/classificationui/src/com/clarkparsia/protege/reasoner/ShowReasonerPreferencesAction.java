package com.clarkparsia.protege.reasoner;

import edu.stanford.smi.protegex.owl.ui.actions.AbstractOWLModelAction;
import edu.stanford.smi.protegex.owl.ui.actions.OWLModelActionConstants;

import edu.stanford.smi.protegex.owl.model.OWLModel;

import edu.stanford.smi.protegex.owl.inference.ui.action.ActionConstants;

import edu.stanford.smi.protegex.owl.inference.protegeowl.ReasonerManager;

import edu.stanford.smi.protegex.owl.inference.reasoner.ProtegeReasoner;

import edu.stanford.smi.protege.util.ComponentUtilities;

import edu.stanford.smi.protege.ui.ProjectManager;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import java.awt.Component;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Title: ShowReasonerPreferencesAction<br/>
 * Description: OWLModelAction implementation to add a menu item to the reasoning menu which will let us provide a menu
 * item that will show the reasoner details for the C&P reasoner.<br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Apr 15, 2009 11:29:40 AM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class ShowReasonerPreferencesAction extends AbstractOWLModelAction {

	/**
	 * Create a new ShowReasonerPreferencesAction 
	 */
    public ShowReasonerPreferencesAction() {
        super();

        JMenu aMenu = ComponentUtilities.getMenu(ProjectManager.getProjectManager().getCurrentProjectMenuBar(), REASONING_MENU);

        if (aMenu == null) {
            return;
        }

        JMenuItem aReasonerInspectorMenu = null;

        // try and find the reasoner inspector menu item...
        for (int aItemIndex = 0; aItemIndex < aMenu.getMenuComponentCount(); aItemIndex++) {
            Component aComp = aMenu.getMenuComponent(aItemIndex);

            if (aComp instanceof JMenuItem) {
                JMenuItem aItem = (JMenuItem) aComp;

                if (aItem.getText().toLowerCase().indexOf("reasoner inspector") != -1) {
                    aReasonerInspectorMenu = aItem;
                    break;
                }
            }
        }

        // set the initial state of the reasoner inspector item
        if (aReasonerInspectorMenu != null) {
            aReasonerInspectorMenu.setEnabled(!isSuitable((OWLModel) ProjectManager.getProjectManager().getCurrentProject().getKnowledgeBase()));
        }

        // attach some listeners to the reasoner menu items, and only the reasoner menu items
        for (int aItemIndex = 0; aItemIndex < aMenu.getMenuComponentCount(); aItemIndex++) {
            Component aComp = aMenu.getMenuComponent(aItemIndex);

            if (aComp instanceof JMenuItem) {
                JMenuItem aItem = (JMenuItem) aComp;

                aItem.addActionListener(new MenuListener(aReasonerInspectorMenu));

				if (ProjectManager.getProjectManager().getCurrentProject().isMultiUserClient()) {
					aItem.setEnabled(false);
				}
            }

            if (aComp instanceof JSeparator) {
                // this is a little hackish, but the first separator encountered in the menu list is what
                // divides the menu items for the reasoners from the rest of the menu items.  so we know if
                // we've reached this component, we've iterated over all the menu items pertaining to reasoners.
                break;
            }
        }
    }

    /**
     * @inheritDoc
     */
    public String getMenubarPath() {
        return REASONING_MENU + PATH_SEPARATOR + OWLModelActionConstants.PREFERENCES_GROUP;
    }

    /**
     * @inheritDoc
     */
    public String getName() {
        return CustomProtegeOWLReasoner.getReasonerName() + " Details";
    }

    /**
     * Show the C&P reasoner preferences dialog
     * @inheritDoc
     */
    public void run(OWLModel theModel) {
        new ReasonerPreferencesDialog(ProjectManager.getProjectManager().getCurrentProject()).setVisible(true);
    }

    /**
     * This menu item is "suitable" only when the C&P reasoner is the active reasoner
     * @inheritDoc
     */
    @Override
    public boolean isSuitable(OWLModel theModel) {
        ProtegeReasoner aReasoner = ReasonerManager.getInstance().getProtegeReasoner(theModel);

        return super.isSuitable(theModel) && aReasoner != null && aReasoner instanceof CustomProtegeOWLReasoner;
    }

    /**
     * Listener for reasoner menu items.  When a reasoner menu item is clicked on, we update the enabled state of
     * the reasoner inspector item (it should be disabled when our reasoner is the current one, protege has a bug that
     * breaks things for anything but their dig reasoner when this is used) and enables our reasoner preferences menu
     * item when our reasoner is the current one.
     */
    private class MenuListener implements ActionListener {
        private JMenuItem mInspectorItem;

		/**
		 * Create a new MenuListener
		 * @param theInspectorItem the reasoner inspector menu item
		 */
        public MenuListener(JMenuItem theInspectorItem) {
            mInspectorItem = theInspectorItem;
        }

		/**
		 * @inheritDoc
		 */
        public void actionPerformed(ActionEvent theEvent) {
            // defer this for a second, protege still hasn't actually processed the menu click even
            // so trying to do an operation that depends on the result of the menu operation is futile.
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    // this tells the OWLModelActionAction which wraps this class to update itself.
                    notifyPropertyChangeListeners("enabled", true, false);

                    // update the reasoner inspector item based on the current reasoner
                    if (mInspectorItem != null) {
                        mInspectorItem.setEnabled(!isSuitable((OWLModel) ProjectManager.getProjectManager().getCurrentProject().getKnowledgeBase()));
                    }
                }
            });
        }
    }
}
