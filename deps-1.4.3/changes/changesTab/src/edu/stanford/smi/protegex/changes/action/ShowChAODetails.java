package edu.stanford.smi.protegex.changes.action;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.bmir.protegex.chao.change.api.ChangeFactory;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.ModalDialog;

public class ShowChAODetails extends AbstractAction {

	private static final String EXTRACT_FROM_CHG_ONT = "Show Changes Ontology Details";
	private KnowledgeBase changesKb;

	public ShowChAODetails(KnowledgeBase changesKb) {
		super(EXTRACT_FROM_CHG_ONT);
		this.changesKb = changesKb;
	}

	public void actionPerformed(ActionEvent arg0) {
		ChAODetailsPanel detailsPane = new ChAODetailsPanel();

		ModalDialog.showDialog(ProjectManager.getProjectManager().getMainPanel(), detailsPane, "Changes ontology details", ModalDialog.MODE_CLOSE);
	}

	class ChAODetailsPanel extends JPanel {

		public ChAODetailsPanel() {
			buildGUI();
		}

		private void buildGUI() {
			setLayout(new GridLayout(4, 1, 5, 5));

			Project chaoPrj = changesKb.getProject();
			String location = chaoPrj.getProjectURI() == null ? "(unknown)" : chaoPrj.getProjectURI().toString();

			add(new JLabel("<html>Location: <b>" + location + "</b></html>"));
			add(new JLabel("<html>Backend: <b>" + (chaoPrj.isMultiUserClient() ? " Remote project (stored on Protege server)" : chaoPrj.getKnowledgeBaseFactory().getDescription()) + "</html>"));
			add(new JLabel("Number of annotations: " + new AnnotationFactory(changesKb).getAnnotationClass().getInstanceCount()));
			add(new JLabel("Number of changes: " + new ChangeFactory(changesKb).getChangeClass().getInstanceCount()));
		}

	}

}
