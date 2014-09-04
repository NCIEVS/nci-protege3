package edu.stanford.smi.protegex.changes.action;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.util.ArchiveManager;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protegex.changes.ChangeProjectUtil;
import edu.stanford.smi.protegex.changes.ChangesTab;

public class CleanUpChangesOntAction extends AbstractAction {

	private static final String EXTRACT_FROM_CHG_ONT = "Clean Up Changes Ontology ...";
	
	private KnowledgeBase changesKb;
	
	public CleanUpChangesOntAction(KnowledgeBase changesKb) {
		super(EXTRACT_FROM_CHG_ONT);
		this.changesKb = changesKb;
	}
	
	public void actionPerformed(ActionEvent arg0) {

		CleanupPanel cleanupPanel = new CleanupPanel();

		int result = ModalDialog.showDialog(ProjectManager.getProjectManager().getMainPanel(), cleanupPanel, EXTRACT_FROM_CHG_ONT, ModalDialog.MODE_OK_CANCEL);

		if (result == ModalDialog.OPTION_OK) {
			if (cleanupPanel.isArchiveCurrentVersion()) {				
				ArchiveManager manager = ArchiveManager.getArchiveManager();
				try {
					manager.archive(changesKb.getProject(), new Date().toString() + ": Archiving changes ontology before clean-up");
				} catch (Exception e) {
					Log.getLogger().log(Level.WARNING, "Archive of changes ontology failed. ", e);
					ModalDialog.showMessageDialog(ProjectManager.getProjectManager().getMainPanel(), 
					"Archiving of changes ontology failed. Operation aborted.\nSee console and logs for more details.");
					return;
				}
			}
			
			/*
			 * Disable events, so that the ChangesTab does not get updated
			 * (this would slow things down) 
			 */
			
			boolean eventsEnabled = changesKb.getGenerateEventsEnabled();
			changesKb.setGenerateEventsEnabled(false);
			
			boolean success = true;			
			
			if (cleanupPanel.isExtractAnnotations()) {
				try {
					ChangeProjectUtil.deleteAllAnnotations(changesKb);
				} catch (Exception e) {
					Log.getLogger().log(Level.WARNING, "Delete of annotation instances failed. ", e);
					success = false;
					
					ModalDialog.showMessageDialog(ProjectManager.getProjectManager().getMainPanel(), 
					"Delete of annotation instances failed.\nSee console and logs for more details.");					
				}
			}

			if (cleanupPanel.isExtractChanges()) {
				try {
					ChangeProjectUtil.deleteAllChanges(changesKb);
				} catch (Exception e) {
					Log.getLogger().log(Level.WARNING, "Delete of change instances failed. ", e);
					success = false;
					
					ModalDialog.showMessageDialog(ProjectManager.getProjectManager().getMainPanel(), 
					"Delete of change instances failed.\nSee console and logs for more details.");
				}		    	
			}
			
			/*
			 * Reinitialize ChangesTab to refresh the display of changes
			 */
			
			try	{
				ProjectView projectView = ProjectManager.getProjectManager().getCurrentProjectView();
				ChangesTab changesTab = (ChangesTab) projectView.getTabByClassName(ChangesTab.class.getName());
				
				if (changesTab != null) {
					changesTab.refreshTables(null);
				}
				
			} catch (Exception e) {
				Log.getLogger().log(Level.WARNING, "Errors at reinitializing ChangesTab after ontology clean-up", e);			
			} 
			
			// re-enable events generation
			changesKb.setGenerateEventsEnabled(eventsEnabled);
			
			if (success == true) {
				ModalDialog.showMessageDialog(ProjectManager.getProjectManager().getMainPanel(), 
				"Clean-up of changes ontology successful!");
			}
			
		}
	}
	
	@Override
	public boolean isEnabled() {
		return !changesKb.getProject().isMultiUserClient();
	}
	
	
	
	protected class CleanupPanel extends JPanel {
		
		private static final String EXTRACT_CHANGES = "Delete changes";
		private static final String EXTRACT_ANNOTATIONS = "Delete annotations";
		private static final String ARCHIVE = "Archive changes ontology before deleting";
		
		private JCheckBox extractChangesCheckBox;
		private JCheckBox extractAnnotationsCheckBox;
		private JCheckBox archiveCheckBox;
		
		
		public CleanupPanel() {
			buildGUI();
		}
		
		public void buildGUI() {
			extractChangesCheckBox = new JCheckBox(EXTRACT_CHANGES, true);
			extractAnnotationsCheckBox = new JCheckBox(EXTRACT_ANNOTATIONS, false);
			archiveCheckBox = new JCheckBox(ARCHIVE, true);
			
			
			setLayout(new GridLayout(3,1));
			
			add(extractAnnotationsCheckBox);
			add(extractChangesCheckBox);			
			add(archiveCheckBox);
		}
		
		public boolean isExtractChanges() {
			return extractChangesCheckBox.isSelected();
		}

		public boolean isExtractAnnotations() {
			return extractAnnotationsCheckBox.isSelected();
		}

		public boolean isArchiveCurrentVersion(){
			return archiveCheckBox.isSelected();
		}
	
	}
	
	
}
