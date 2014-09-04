package edu.stanford.smi.protegex.changes.action;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.WidgetDescriptor;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.util.FileField;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.URIUtilities;
import edu.stanford.smi.protege.widget.TabWidget;
import edu.stanford.smi.protegex.changes.ChangesTab;

public class ChangeChAOAction extends AbstractAction {

	private static final String CHANGE_CHG_ONT = "Change Associated Changes Ontology...";
	private KnowledgeBase kb;

	public ChangeChAOAction(KnowledgeBase kb) {
		super(CHANGE_CHG_ONT);
		this.kb = kb;
	}

	public void actionPerformed(ActionEvent arg0) {
		ChangeChaoPanel panel = new ChangeChaoPanel();

		int ret = ModalDialog.showDialog(ProjectManager.getProjectManager().getMainPanel(),
				panel, "Select the Changes and Annotations file", ModalDialog.MODE_OK_CANCEL);

		if (ret == ModalDialog.OPTION_OK) {
			URI uri = panel.getURI();
			if (uri == null) {
				ChAOKbManager.detachChAO(kb);
				hideChangesTab();
				ProjectManager.getProjectManager().reloadUI(true);
				return;
			}

			boolean	validProject = ChAOKbManager.isValidChAOKb(uri);
			if (!validProject) {
				ModalDialog.showMessageDialog(panel,
						"The provided location does not seem\n" +
						"to contain a valid ChAO project.\n" +
				"Please select another file.", "Invalid ChAO project");
			} else {
				//if valid project
				ChAOKbManager.setChAOProjectURI(kb, uri);
				ChAOKbManager.getChAOKb(kb);
				ProjectManager.getProjectManager().reloadUI(true);
			}
		}
	}

	private void hideChangesTab() {
		String changesKbClassName = ChangesTab.class.getName();

		ProjectView prjView = ProjectManager.getProjectManager().getCurrentProjectView();
		TabWidget tabWidget = prjView.getTabByClassName(changesKbClassName);

		if (tabWidget == null) {
			return;
		}

		WidgetDescriptor d = kb.getProject().getTabWidgetDescriptor(changesKbClassName);
		d.setVisible(false);
	}


	class ChangeChaoPanel extends JPanel {
		FileField fileField;

		public ChangeChaoPanel() {
			buildGUI();
		}

		private void buildGUI() {
			setLayout(new GridLayout(2,1,5,5));
			fileField = new FileField("Select the new Changes and Annotations project file", null, ".pprj", "ChAO");
			add(fileField);
			add(new JLabel(
					"<html><i>Leave the field <b>blank</b>, if you want to remove the associated<br>" +
					"Changes and Annotations Ontology.</i><br><br>" +
					"The Changes and Annotations Ontology and the user interface will be reloaded.<br>" +
					"<b>To make these changes permanent, please save the domain ontology (File menu -> Save).</b>" +
					"</html>"));
		}

		URI getURI() {
			return URIUtilities.createURI(fileField.getPath());
		}

	}

}
