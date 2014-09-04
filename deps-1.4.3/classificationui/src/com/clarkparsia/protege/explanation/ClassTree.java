package com.clarkparsia.protege.explanation;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JTree;
import javax.swing.Icon;
import javax.swing.Action;
import javax.swing.AbstractAction;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Set;
import java.util.Collections;
import java.util.HashSet;
import java.util.Collection;

import edu.stanford.smi.protege.util.SelectableContainer;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.ui.SubclassPane;
import edu.stanford.smi.protege.ui.HeaderComponent;
import edu.stanford.smi.protege.ui.ClsTreeFinder;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.resource.LocalizedText;
import edu.stanford.smi.protegex.owl.ui.cls.OWLSubclassPane;
import edu.stanford.smi.protegex.owl.ui.cls.OWLClassesTab;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;
import edu.stanford.smi.protegex.owl.ui.subsumption.ChangedClassesPanel;
import edu.stanford.smi.protegex.owl.ui.results.ResultsPanelManager;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Apr 11, 2007 12:09:42 PM
 *
 * @author Michael Grove <mhgrove@hotmail.com>
 */
public class ClassTree extends SelectableContainer {
    private SelectableContainer mSubClassPane;
    private Project mProject;

    private Action displayChangedAction = new AbstractAction("Display changed classes in list",
                                                             OWLIcons.getDisplayChangedClassesIcon()) {
		public void actionPerformed(ActionEvent e) {
			ChangedClassesPanel ccp = ChangedClassesPanel.get(getOWLModel());
			ccp.refresh();
			if (ccp.getChangeCount() > 0) {
				ResultsPanelManager.addResultsPanel(getOWLModel(), ccp, true);
			}
		}
    };

    public ClassTree(Project theProject) {

        mProject = theProject;

        //mSubClassPane = new SubclassPane(null, theProject.getKnowledgeBase().getRootCls(), null, null);
        mSubClassPane = new OWLSubclassPane((OWLModel)theProject.getKnowledgeBase(), null, (RDFSNamedClass) theProject.getKnowledgeBase().getRootCls());

        String subclassesLabel = LocalizedText.getText(ResourceKey.CLASS_BROWSER_HIERARCHY_LABEL);
        LabeledComponent aComp = new LabeledComponent(subclassesLabel, mSubClassPane, true);
		aComp.addHeaderButton(displayChangedAction);
		
        aComp.setBorder(ComponentUtilities.getAlignBorder());

        add(aComp, BorderLayout.CENTER);
        add(createHeaderComponent(), BorderLayout.NORTH);

        setSelectable(mSubClassPane);        
    }

    public HeaderComponent createHeaderComponent() {
        JLabel label = ComponentFactory.createLabel(mProject.getName(), Icons.getProjectIcon(), SwingConstants.LEFT);
        String forProject = LocalizedText.getText(ResourceKey.CLASS_BROWSER_FOR_PROJECT_LABEL);
        String classBrowser = LocalizedText.getText(ResourceKey.CLASS_BROWSER_TITLE);
        return new HeaderComponent(classBrowser, forProject, label);
    }

    public JTree getTree() {
        return (JTree) mSubClassPane.getSelectable();
    }

    public OWLModel getOWLModel() {
        return (OWLModel) mProject.getKnowledgeBase();
    }
}
