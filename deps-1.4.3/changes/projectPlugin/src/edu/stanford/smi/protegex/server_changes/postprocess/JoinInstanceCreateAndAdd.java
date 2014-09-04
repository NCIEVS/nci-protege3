package edu.stanford.smi.protegex.server_changes.postprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.change.api.ChangeFactory;
import edu.stanford.bmir.protegex.chao.change.api.Composite_Change;
import edu.stanford.bmir.protegex.chao.change.api.Individual_Added;
import edu.stanford.bmir.protegex.chao.change.api.Individual_Created;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protegex.server_changes.PostProcessorManager;

public class JoinInstanceCreateAndAdd implements PostProcessor {

    private PostProcessorManager postProcessorManager;

    private boolean owl;

    private Map<RemoteSession, Individual_Created> lastCreateBySession = new HashMap<RemoteSession, Individual_Created>();

    public void addChange(Change change) {
        if (owl) {
			return;
		}

        RemoteSession session = postProcessorManager.getCurrentSession();
        if (change instanceof Individual_Created) {
            lastCreateBySession.put(session, (Individual_Created) change);
            return;
        }
        if (change instanceof Individual_Added) {
            Individual_Created create_op = lastCreateBySession.remove(session);
            Ontology_Component created = change.getApplyTo();
            if (create_op == null || !created.equals(change.getApplyTo())) {
				return;
			}

            List<Change> subChanges = new ArrayList<Change>();
            subChanges.add(create_op);
            subChanges.add(change);

            Composite_Change transaction = new ChangeFactory(postProcessorManager.getChangesKb()).createComposite_Change(null);
            transaction.setSubChanges(subChanges);
            postProcessorManager.finalizeChange(transaction, create_op.getApplyTo(),
                                      "Created Instance " + created.getCurrentName());
        }
    }

    public void initialize(PostProcessorManager postProcessorManager) {
        this.postProcessorManager = postProcessorManager;
        owl = postProcessorManager.isOwl();
    }

}
