package edu.stanford.smi.protegex.server_changes.postprocess;

import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.smi.protegex.server_changes.PostProcessorManager;

/**
 * This guy is very similar to a listener - maybe these concepts should be merged.
 * @author tredmond
 *
 */
public interface PostProcessor {
    public void initialize(PostProcessorManager changes_db);

    public void addChange(Change change);
}
