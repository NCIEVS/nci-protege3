package edu.stanford.smi.protegex.owl.inference.ui.action;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.logging.Level;

import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.inference.protegeowl.ReasonerManager;
import edu.stanford.smi.protegex.owl.inference.protegeowl.task.ReasonerTaskListener;
import edu.stanford.smi.protegex.owl.inference.reasoner.ProtegeReasoner;
import edu.stanford.smi.protegex.owl.inference.reasoner.exception.ProtegeReasonerException;
import edu.stanford.smi.protegex.owl.inference.ui.ReasonerActionRunner;
import edu.stanford.smi.protegex.owl.inference.ui.RunnableReasonerAction;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.ui.actions.AbstractOWLModelAction;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;

/**
 * User: matthewhorridge<br>
 * The Univeristy Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: Jun 18, 2004<br><br>
 * <p/>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class ClassifyTaxonomyAction extends AbstractOWLModelAction implements RunnableReasonerAction {

    private OWLModel owlModel;
    private ProtegeReasoner reasoner = null;
    
    public void executeReasonerActions(ReasonerTaskListener taskListener) throws ProtegeReasonerException {
    	reasoner = ReasonerManager.getInstance().getProtegeReasoner(owlModel);
        reasoner.setReasonerTaskListener(taskListener);
        reasoner.classifyTaxonomy();
    }


    public String getIconFileName() {
        return OWLIcons.CLASSIFY;
    }


    public String getMenubarPath() {
        return REASONING_MENU + PATH_SEPARATOR + ActionConstants.ACTION_GROUP;
    }


    public String getToolbarPath() {
        return ActionConstants.ACTION_GROUP;
    }


    public String getName() {
        return "Classify taxonomy...";
    }


    public void run(OWLModel owlModel) {
        this.owlModel = owlModel;
        ReasonerActionRunner runner = new ReasonerActionRunner(this, true);
        runner.execute();
    }


    public OWLModel getOWLModel() {
        return owlModel;
    }
}

