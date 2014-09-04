package edu.stanford.smi.protegex.server_changes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.impl.DefaultTimestamp;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.server_changes.postprocess.PostProcessor;
import edu.stanford.smi.protegex.server_changes.util.Util;

public class PostProcessorManager {
    private Logger log = Log.getLogger(PostProcessorManager.class);

    private KnowledgeBase kb;
    private KnowledgeBase changes_kb;
    
    private ExecutorService sequentialExecutor;

    /*
     * This map allows the ChangesDb manage transactions.  The map maintains transaction information
     * on a per session basis.  The transaction state class is responsible for tracking whether a transaction
     * is in progress and for closing out transactions.
     */
    private Map<RemoteSession, TransactionState> transactionMap = new HashMap<RemoteSession, TransactionState>();

    /*
     * a list of postprocessing jobs.
     */
    private List<PostProcessor> post_processors = new ArrayList<PostProcessor>();

    public PostProcessorManager(KnowledgeBase kb) {
        this.kb = kb;
        this.changes_kb = ChAOKbManager.getChAOKb(kb);
        if (kb.getProject().isMultiUserServer()) {
            sequentialExecutor = Executors.newSingleThreadExecutor();
        }
        else {
            sequentialExecutor = null;
        }
        DefaultTimestamp.initialize();
    }


    /* -------------------------------- PostProcessing -------------------------------- */
    /* ToDo - put these in separate classes - there are getting to be too many of them */

    private void postProcessChange(Change aChange) {
        checkForTransaction(aChange);
        for (PostProcessor p : post_processors) {
            p.addChange(aChange);
        }
    }

    /*
     * If I am in a transaction, add the change to the transaction.
     */
    private void checkForTransaction(Change aChange) {
        TransactionState tstate = getTransactionState();
        if (tstate.inTransaction()) {
            tstate.addToTransaction(aChange);
        }
    }


    /* -------------------------------------Interfaces ------------------------------*/


    public KnowledgeBase getKb() {
        return kb;
    }

    public KnowledgeBase getChangesKb() {
        return changes_kb;
    }

    public Project getChangesProject() {
        return changes_kb.getProject();
    }
    
    public void submitChangeListenerJob(final Runnable r) {
        if (sequentialExecutor == null) {
            r.run();
        }
        else {
            final RemoteSession session = ServerFrameStore.getCurrentSession();
            sequentialExecutor.submit(new Runnable() {
                    public void run() {
                        try {
                            ServerFrameStore.setCurrentSession(session);
                            r.run();
                        }
                        catch (Throwable t) {
                            log.log(Level.WARNING, "Exception caught in change management listener", t);
                        }
                    }
                });
        }
    }

    public String getCurrentUser() {
        return ChangesProject.getUserName(kb);
    }

    public boolean isOwl() {
        return Util.kbInOwl(kb);
    }

    public RemoteSession getCurrentSession() {
        RemoteSession session = ServerFrameStore.getCurrentSession();
        if (session != null) {
			return session;
		} else {
			return StandaloneSession.getInstance();
		}
    }

    public void addPostProcessor(PostProcessor p) {
        p.initialize(this);
        post_processors.add(p);
    }

    public PostProcessor getPostProcessor(Class<? extends PostProcessor> clazz) {
        for (PostProcessor p : post_processors) {
            if (clazz.isAssignableFrom(p.getClass())) {
                return p;
            }
        }
        return null;
    }


    public Ontology_Component getOntologyComponent(Frame frame) {
    	return getOntologyComponent(frame, false);
    }

    //TODO: do we need to know the OC also for deleted frames?
    public Ontology_Component getOntologyComponent(Frame frame, boolean create) {
    	return ServerChangesUtil.getOntologyComponent(frame, create);
    }


    public TransactionState getTransactionState() {
        TransactionState state = transactionMap.get(getCurrentSession());
        if (state == null) {
            state = new TransactionState(this);
            transactionMap.put(getCurrentSession(), state);
        }
        return state;
    }


    public void finalizeChange(Change change,
                               Ontology_Component applyTo,
                               String context) {
        change.setAuthor(getCurrentUser());
        change.setContext(context);
        change.setTimestamp(DefaultTimestamp.getTimestamp(changes_kb));
        change.setApplyTo(applyTo);  // this is what passes the change to the change tab
                                     // so it  must happen last.  see AbstractChangeListener
        postProcessChange(change);
    }


}
