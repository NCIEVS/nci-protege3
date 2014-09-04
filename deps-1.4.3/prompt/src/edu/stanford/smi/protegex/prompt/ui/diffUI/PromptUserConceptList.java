package edu.stanford.smi.protegex.prompt.ui.diffUI;

import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protegex.prompt.promptDiff.PromptDiff;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.TableRow;
import edu.stanford.smi.protegex.server_changes.prompt.UserConceptList;

public class PromptUserConceptList extends UserConceptList {
    private static final long serialVersionUID = 6850283796994144074L;

    private PromptDiff diff;
    private DiffTablePanel diffTablePanel;
    Collection<TableRow> rows;

    public PromptUserConceptList(PromptDiff diff, List<TableRow> results) {
        super(diff.getKb1(), diff.getKb2());
        this.diff = diff;
        rows = results;
    }


    @Override
	protected JComponent createChangesTable() {
    	JTabbedPane tabbedPane = new JTabbedPane();
        diffTablePanel = new DiffTablePanel(null, null, diff.getResultsTable());
        tabbedPane.addTab("Changes with Accept/Reject", diffTablePanel);
        tabbedPane.addTab("Changes by user", super.createChangesTable());
        return tabbedPane;
    }

    @Override
	public void updateChangeTable(Ontology_Component ontologyComponent) {
    	super.updateChangeTable(ontologyComponent);
        Frame frame = null;
        if (ontologyComponent != null) {
            String name = ontologyComponent.getCurrentName();
            if (name != null) {
                frame = diff.getKb2().getFrame(name);
            }
            else {
                name = ontologyComponent.getInitialName();
                if (name != null) {
                    frame = diff.getKb1().getFrame(name);
                }
            }
        }
        for (TableRow row : rows) {
            Frame f1 = row.getF1Value();
            Frame f2 = row.getF2Value();
            if (f1 != null && f1.equals(frame)) {
                diffTablePanel.setRow(row, false);
            }
            else if (f2 != null && f2.equals(frame)) {
                diffTablePanel.setRow(row, false);
            }

        }
    }

    public void reloadDiffPanel() {
        diffTablePanel.reload();
    }

}
