package edu.stanford.smi.protegex.changes.ui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.WidgetDescriptor;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.MessageError;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.ProtegeJob;
import edu.stanford.smi.protege.widget.TabWidget;
import edu.stanford.smi.protegex.changes.ChangeProjectUtil;
import edu.stanford.smi.protegex.changes.ChangesTab;
import edu.stanford.smi.protegex.changes.action.ChangeChAOAction;
import edu.stanford.smi.protegex.changes.action.CleanUpChangesOntAction;
import edu.stanford.smi.protegex.changes.action.ShowChAODetails;
import edu.stanford.smi.protegex.changes.changesKBViewTab.ChangesKBViewTab;
import edu.stanford.smi.protegex.server_changes.ChangesProject;
import edu.stanford.smi.protegex.server_changes.prompt.UsersTab;

public class ChangeMenu extends JMenu {

	public static final String MENU_TITLE = "Change";
	public static final String MENU_ITEM_ANNOTATE_LAST = "Annotate Last Change";
	public static final String MENU_ITEM_CHANGE_INFO = "Selected Change Info";
	public static final String MENU_ITEM_SAVE_CHANGE_PRJ = "Save Changes Ontology";
	public static final String MENU_ITEM_BROWSE_CHANGE_PRJ = "Browse Changes Ontology";
	public static final String MENU_ITEM_STATS_CONFL_PRJ = "View Change Statistics and Conflicts";

	protected AnnotateLastChange lastChange = new AnnotateLastChange();
	protected SelectedChangeInfo selChangeInfo = new SelectedChangeInfo();
	protected Action saveChangesPrjAction;
	protected Change lastInst;

    private KnowledgeBase kb;
	private KnowledgeBase change_kb;
	private Project change_project;

	public ChangeMenu(KnowledgeBase kb, KnowledgeBase changeKb) {
		super(MENU_TITLE);
		setMnemonic(KeyEvent.VK_C);

        this.kb = kb;
		this.change_kb = changeKb;
		this.change_project = change_kb.getProject();

		JMenuItem annotate = new JMenuItem(MENU_ITEM_ANNOTATE_LAST);
		JMenuItem changeInfo = new JMenuItem(MENU_ITEM_CHANGE_INFO);
		JMenuItem saveChangesPrj = new JMenuItem(MENU_ITEM_SAVE_CHANGE_PRJ);
		JMenuItem browseChangesPrj = new JMenuItem(MENU_ITEM_BROWSE_CHANGE_PRJ);
		JMenuItem statsPrj = new JMenuItem(MENU_ITEM_STATS_CONFL_PRJ);
		JMenuItem extract = new JMenuItem(new CleanUpChangesOntAction(change_kb));
		JMenuItem details = new JMenuItem(new ShowChAODetails(changeKb));
		JMenuItem change = new JMenuItem(new ChangeChAOAction(kb));

		change.setEnabled(!kb.getProject().isMultiUserClient());

		annotate.setAction(lastChange);
		annotate.setMnemonic(KeyEvent.VK_A);
		annotate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,ActionEvent.CTRL_MASK));

		changeInfo.setAction(selChangeInfo);
		changeInfo.setMnemonic(KeyEvent.VK_K);
		changeInfo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K,ActionEvent.CTRL_MASK));

		saveChangesPrj.setAction(getSaveChangesPrjAction());
		browseChangesPrj.setAction(getBrowseChangePrjAction());
		statsPrj.setAction(getViewStatsAndConflictsPrjAction());

		add(annotate);
		add(changeInfo);
		addSeparator();
		add(details);
		add(browseChangesPrj);
		add(statsPrj);
		addSeparator();
		add(change);
		add(extract);
		add(saveChangesPrj);

		lastChange.setEnabled(false);
		selChangeInfo.setEnabled(true);
		//saveChangesPrj.setEnabled(true);
	}

	protected Action getViewStatsAndConflictsPrjAction() {
		return new AbstractAction(MENU_ITEM_STATS_CONFL_PRJ) {
			public void actionPerformed(ActionEvent arg0) {
				try {
					String changesKbViewTabClassName = UsersTab.class.getName();

					ProjectView prjView = ProjectManager.getProjectManager().getCurrentProjectView();
					TabWidget tabWidget = prjView.getTabByClassName(changesKbViewTabClassName);

					if (tabWidget != null) {
						prjView.setSelectedTab(tabWidget);
						return;
					}

					WidgetDescriptor d = kb.getProject().getTabWidgetDescriptor(changesKbViewTabClassName);
					d.setVisible(true);

					prjView.addTab(d);
					tabWidget = prjView.getTabByClassName(changesKbViewTabClassName);
					prjView.setSelectedTab(tabWidget);

				} catch (Exception e) {
					Log.getLogger().log(Level.WARNING, "Cannot enable the Users Tab", e);
					ModalDialog.showMessageDialog(ProjectManager.getProjectManager().getCurrentProjectView(),
							"Cannot enable the Users Tab to show the\n" +
							"change statistics and conflicts.\n" +
							"See console for more details.", "Warning");
				}
			}
		};
	}


	protected Action getBrowseChangePrjAction() {
		return new AbstractAction(MENU_ITEM_BROWSE_CHANGE_PRJ) {
			public void actionPerformed(ActionEvent arg0) {
				try {
					String changesKbViewTabClassName = ChangesKBViewTab.class.getName();

					ProjectView prjView = ProjectManager.getProjectManager().getCurrentProjectView();
					TabWidget tabWidget = prjView.getTabByClassName(changesKbViewTabClassName);

					if (tabWidget != null) {
						prjView.setSelectedTab(tabWidget);
						return;
					}

					WidgetDescriptor d = kb.getProject().getTabWidgetDescriptor(changesKbViewTabClassName);
					d.setVisible(true);

					prjView.addTab(d);
					tabWidget = prjView.getTabByClassName(changesKbViewTabClassName);
					prjView.setSelectedTab(tabWidget);

				} catch (Exception e) {
					Log.getLogger().log(Level.WARNING, "Cannot enable the ChangesKBView Tab", e);
					ModalDialog.showMessageDialog(ProjectManager.getProjectManager().getCurrentProjectView(),
							"Cannot enable the ChangesKbView Tab.\nSee console for more details.", "Warning");
				}
			}
		};
	}

	public class AnnotateLastChange extends AbstractAction {

		public AnnotateLastChange() {
			super(MENU_ITEM_ANNOTATE_LAST);
		}
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent arg0) {

			Collection changeInsts = new ArrayList();
			changeInsts.add(lastInst);
			final Annotation annotateInst = ChangeProjectUtil.createAnnotation(change_kb, "Comment", changeInsts);

			JFrame aEdit = change_project.show(((AbstractWrappedInstance)annotateInst).getWrappedProtegeInstance());
			aEdit.addWindowListener(new WindowAdapter() {

				@Override
				public void windowClosed(WindowEvent arg0) {
					//try to get the ChangesTab
					ChangesTab changesTab = (ChangesTab) ProjectManager.getProjectManager().getCurrentProjectView().getTabByClassName("edu.stanford.smi.protegex.changes.ChangesTab");

					if (changesTab != null) {
						changesTab.createAnnotationItemInTable(annotateInst);
					}

					setEnabledLastChange(false);
				}
			});
			aEdit.setVisible(true);
		}
	}

	public class SelectedChangeInfo extends AbstractAction {

		public SelectedChangeInfo() {
			super(MENU_ITEM_CHANGE_INFO);
		}

		public void actionPerformed(ActionEvent arg0) {
			ProjectView pvw = ProjectManager.getProjectManager().getCurrentProjectView();
			TabWidget tWidget = pvw.getSelectedTab();
			Collection selection = tWidget.getSelection();

			if (selection == null || selection.size() == 0) {
				return;
			}

			ArrayList selArray = new ArrayList(selection);
			Object elem = selArray.get(selArray.size()-1);
			String className = null;
			if (elem instanceof Frame) {
				className = ((Frame)elem).getName();
				ChangeAnnotateWindow cmWindow = new ChangeAnnotateWindow(change_kb, className, true);
				cmWindow.show();
			}
		}
	}

	public void setEnabledLastChange(boolean val) {
		lastChange.setEnabled(val);
	}

	public void setChange(Change lastInst) {
		this.lastInst = lastInst;
	}

	public Action getSaveChangesPrjAction() {
		return new AbstractAction(MENU_ITEM_SAVE_CHANGE_PRJ) {

			public void actionPerformed(ActionEvent arg0) {
				if (change_project == null) {
					Log.getLogger().warning("Cannot save Changes project. Changes project is null.");
					return;
				}

				Collection errors = new ArrayList();
				try {
					errors = saveChangeProject();
				} catch (Exception e) {
					Log.getLogger().log(Level.WARNING, "Errors saving changes project", e);
					errors.add(new MessageError(e));
				}

				if (errors.size() == 0) {
					Log.getLogger().info("Changes project saved successfully.");
					return;
				}

				for (Object error : errors) {
					//Errors are either Strings or MessageErrors. We can treat these cases differently, if we need more error information.
					Log.getLogger().warning(error.toString());
				}

				JOptionPane.showMessageDialog(ChangeMenu.this, "There were errors at saving the changes project.\n" +
						"See console for more details");
			}
		};
	}

	protected Collection saveChangeProject() {
	    return (Collection) new SaveChangeProjectJob(kb).execute();
	}

    public static class SaveChangeProjectJob extends ProtegeJob {
        public SaveChangeProjectJob(KnowledgeBase kb) {
            super(kb);
        }

        @Override
		public Object run() throws ProtegeException {
            KnowledgeBase kb = getKnowledgeBase();
            Project changes_project = ChangesProject.getChangesProj(kb);
            List errors = new ArrayList();
            changes_project.save(errors);
            return errors;
        }
    }
}
