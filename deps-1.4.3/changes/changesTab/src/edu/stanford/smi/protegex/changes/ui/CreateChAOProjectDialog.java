package edu.stanford.smi.protegex.changes.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.net.URI;
import java.util.ArrayList;
import java.util.logging.Level;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.storage.database.DatabaseKnowledgeBaseFactory;
import edu.stanford.smi.protege.storage.database.DatabaseKnowledgeBaseSourcesEditor;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.FileField;
import edu.stanford.smi.protege.util.FileUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.PropertyList;
import edu.stanford.smi.protege.util.URIUtilities;
import edu.stanford.smi.protege.util.ModalDialog.CloseCallback;
import edu.stanford.smi.protegex.storage.rdf.RDFBackend;
import edu.stanford.smi.protegex.storage.rdf.RDFSourcesEditor;

public class CreateChAOProjectDialog {

	private enum ChaoType {FILE, DB, EXISTING};

	private KnowledgeBase kb;
	private KnowledgeBase changesKb;
	private CreateChAOProjectPanel panel;

	public CreateChAOProjectDialog(KnowledgeBase kb) {
		this.kb = kb;
	}

	public void showDialog() {
		panel = new CreateChAOProjectPanel(kb);
		ModalDialog.showDialog(ProjectManager.getProjectManager().getMainPanel(),
				panel, "Choose associated ChAO", ModalDialog.MODE_OK_CANCEL, getCloseCallback());
	}

	protected CloseCallback getCloseCallback() {
		return new CloseCallback() {
			public boolean canClose(int result) {
				if (result == ModalDialog.OPTION_CANCEL) {
					return true;
				}
				switch (panel.getSelection()) {
				case FILE:
					return onFileProjectOption();
				case DB:
					return onDBProjectOption();
				case EXISTING:
					return onExistingProjectOption();
				default:
					break;
				}
				return true;
			}
		};
	}

	public KnowledgeBase getChangesKb() {
		return changesKb;
	}

	protected boolean onFileProjectOption() {
		//new project - no path
		if (kb.getProject().getProjectDirectoryURI() == null) {
			changesKb = ChAOKbManager.createRDFFileChAOKb(kb, ChAOKbManager.getChAOProjectURI(kb), panel.isChangeTrackingEnabled());
			return true;
		}

		//existing project that have a path
		Project tmpPrj = Project.createNewProject(null, new ArrayList());
		PropertyList sources = PropertyList.create(tmpPrj.getInternalProjectKnowledgeBase());
		//URI chaoURI = URIUtilities.createURI(ApplicationProperties.getApplicationDirectory() + File.separator + "annotation.pprj");

		URI chaoURI = ChAOKbManager.getChAOProjectURI(kb);
		String rdfsFileName = FileUtilities.replaceExtension(URIUtilities.getName(chaoURI), ".rdfs");
		String rdfFileName = FileUtilities.replaceExtension(URIUtilities.getName(chaoURI), ".rdf");
		RDFBackend.setSourceFiles(sources, rdfsFileName, rdfFileName, ChAOKbManager.PROTEGE_NAMESPACE);

		RDFSourcesEditor editor = new RDFSourcesEditor(chaoURI.toString(), sources);
		editor.setShowProject(true);

		int ret = ModalDialog.showDialog(panel,	editor, "Create new RDF(S) ChAO", ModalDialog.MODE_OK_CANCEL);

		if (ret == ModalDialog.OPTION_OK) {
			PropertyList newSources = editor.getSources();
			try {
				changesKb = ChAOKbManager.createRDFFileChAOKb(kb, URIUtilities.createURI(editor.getProjectPath()),
						RDFBackend.getClsesFileName(newSources), RDFBackend.getInstancesFileName(newSources),
						RDFBackend.getNamespace(newSources), panel.isChangeTrackingEnabled());
			} catch (Throwable t) {
				Log.getLogger().log(Level.WARNING, "Errors at creating the ChAO KB", t);
			}
		}

		tmpPrj.dispose();

		return ret == ModalDialog.OPTION_OK;
	}

	protected boolean onDBProjectOption() {
		if (kb.getProject().getProjectDirectoryURI() == null) {
			ModalDialog.showMessageDialog(panel, "Please save first the main project.", "New project, please save");
			return false;
		}
		
		//existing projects that have a path
		Project tmpPrj = Project.createNewProject(null, new ArrayList());
		PropertyList sources = PropertyList.create(tmpPrj.getInternalProjectKnowledgeBase());
		//URI chaoURI = URIUtilities.createURI(ApplicationProperties.getApplicationDirectory() + File.separator + "annotation.pprj");

		URI chaoURI = ChAOKbManager.getChAOProjectURI(kb);

		DatabaseKnowledgeBaseSourcesEditor editor = new DatabaseKnowledgeBaseSourcesEditor(chaoURI.toString(), sources);
		editor.setShowProject(kb.getProject().getProjectDirectoryURI() != null);
		editor.setShowIncludedProjects(false);

		int ret = ModalDialog.showDialog(panel,	editor, "Create new DB ChAO", ModalDialog.MODE_OK_CANCEL);

		if (ret == ModalDialog.OPTION_OK) {
			PropertyList newSources = editor.getSources();
			try {
				changesKb = ChAOKbManager.createDbChAOKb(kb, chaoURI,
						DatabaseKnowledgeBaseFactory.getDriver(newSources),
						DatabaseKnowledgeBaseFactory.getURL(newSources),
						DatabaseKnowledgeBaseFactory.getTableName(newSources),
						DatabaseKnowledgeBaseFactory.getUsername(newSources),
						DatabaseKnowledgeBaseFactory.getPassword(newSources), 
						panel.isChangeTrackingEnabled());
			} catch (Throwable t) {
				Log.getLogger().log(Level.WARNING, "Errors at creating the ChAO KB", t);
			}
		}

		tmpPrj.dispose();

		return ret == ModalDialog.OPTION_OK;
	}

	protected boolean onExistingProjectOption() {
		FileField fileField = new FileField("Select the Changes and Annotations project file", null, ".pprj", "ChAO");
		int ret = ModalDialog.showDialog(panel, fileField, "Select the Changes and Annotations file", ModalDialog.MODE_OK_CANCEL);
		URI uri = URIUtilities.createURI(fileField.getPath());

		if (ret == ModalDialog.OPTION_OK) {
			boolean validProject = ChAOKbManager.isValidChAOKb(uri);
			if (!validProject) {
				ModalDialog.showMessageDialog(panel,
						"The provided location does not seem\n" +
						"to contain a valid ChAO project.\n" +
				"Please select another file or another option.", "Invalid ChAO project");
			} else {
				//if valid project
				ChAOKbManager.setChAOProjectURI(kb, uri);
				changesKb = ChAOKbManager.getChAOKb(kb);
			}
			return validProject;
		}
		//other cases
		return false;
	}


	class CreateChAOProjectPanel extends JPanel {
		private static final long serialVersionUID = 5569432370041174566L;
		
		private KnowledgeBase kb;
		private JRadioButton fileRadioButton;
		private JRadioButton dbRadioButton;
		private JRadioButton existingRadioButton;
		private JCheckBox enableChangeTrackingCheckBox;

		public CreateChAOProjectPanel(KnowledgeBase kb) {
			this.kb = kb;
			buildUI();
		}

		protected void buildUI() {
			setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
			add(getTextComponent());
			addRadioBoxOptions();			
		}

		protected JTextArea getTextComponent() {
			JTextArea textArea = new JTextArea();
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);		
			textArea.setEditable(false);
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("Some Protege feature (e.g., the collaboration panel or the Changes Tab)\n");
			Font font = textArea.getFont();
			int width = textArea.getFontMetrics(font).stringWidth(stringBuilder.toString());
			int height = textArea.getFontMetrics(font).getHeight();
			setPreferredSize(new Dimension(width + 20, height * 10));
			stringBuilder.append("needs a Changes and Annotations ontology (ChAO) for its functioning.\n");
			stringBuilder.append("Please choose one of the following options:");
			textArea.setText(stringBuilder.toString());			
			return textArea;
		}

		protected void addRadioBoxOptions() {
			fileRadioButton = new JRadioButton("Create ChAO as RDF(S) files.", true);
			dbRadioButton = new JRadioButton("Create ChAO stored in a database (if new project, please save first).", false);
			existingRadioButton = new JRadioButton("Use existing ChAO from your file system.", false);

			fileRadioButton.setAlignmentX(Component.LEFT_ALIGNMENT);
			dbRadioButton.setAlignmentX(Component.LEFT_ALIGNMENT);
			existingRadioButton.setAlignmentX(Component.LEFT_ALIGNMENT);

			ButtonGroup group = new ButtonGroup();
			group.add(fileRadioButton);
			group.add(dbRadioButton);
			group.add(existingRadioButton);

			JPanel gridPanel = new JPanel(new GridLayout(5,1));
			gridPanel.add(fileRadioButton);
			gridPanel.add(dbRadioButton);
			gridPanel.add(existingRadioButton);
			gridPanel.add(Box.createRigidArea(new Dimension(0, 10)));
			
			enableChangeTrackingCheckBox = new JCheckBox("Enable change tracking");
			enableChangeTrackingCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
			enableChangeTrackingCheckBox.setSelected(true);
			gridPanel.add(enableChangeTrackingCheckBox);
			
			add(Box.createRigidArea(new Dimension(0, 5)));
			
			add(gridPanel);
		}				

		public ChaoType getSelection() {
			if (fileRadioButton.isSelected()) {
				return ChaoType.FILE;
			} else if (dbRadioButton.isSelected()) {
				return ChaoType.DB;
			} else if (existingRadioButton.isSelected()) {
				return ChaoType.EXISTING;
			}
			return ChaoType.FILE;
		}
		
		public boolean isChangeTrackingEnabled() {
			return enableChangeTrackingCheckBox.isSelected();
		}
	}	

}
