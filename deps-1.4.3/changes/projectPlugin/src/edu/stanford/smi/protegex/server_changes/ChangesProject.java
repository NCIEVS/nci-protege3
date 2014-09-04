package edu.stanford.smi.protegex.server_changes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.smi.protege.Application;
import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.WidgetDescriptor;
import edu.stanford.smi.protege.plugin.ProjectPluginAdapter;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.MessageError;
import edu.stanford.smi.protegex.changes.ChangesTab;
import edu.stanford.smi.protegex.server_changes.listeners.ChangesClsListener;
import edu.stanford.smi.protegex.server_changes.listeners.ChangesFrameListener;
import edu.stanford.smi.protegex.server_changes.listeners.ChangesInstanceListener;
import edu.stanford.smi.protegex.server_changes.listeners.ChangesKBListener;
import edu.stanford.smi.protegex.server_changes.listeners.ChangesSlotListener;
import edu.stanford.smi.protegex.server_changes.listeners.ChangesTransListener;
import edu.stanford.smi.protegex.server_changes.postprocess.AnnotationCombiner;
import edu.stanford.smi.protegex.server_changes.postprocess.JoinCreateAndNameChange;
import edu.stanford.smi.protegex.server_changes.postprocess.JoinInstanceCreateAndAdd;
import edu.stanford.smi.protegex.server_changes.server.ChangeOntStateMachine;
import edu.stanford.smi.protegex.server_changes.util.Util;

public class ChangesProject extends ProjectPluginAdapter {

    private static Map<KnowledgeBase, PostProcessorManager> postProcessorManagerMap = new HashMap<KnowledgeBase, PostProcessorManager>();

    /* ---------------------------- Project Plugin Interfaces ---------------------------- */
    @Override
    public void afterLoad(Project p) {
        if (!isChangeTrackingEnabled(p) || p.isMultiUserClient()) {
            return;
        }
        initialize(p);
    }

    @Override
    public void afterSave(Project p) {
        if (p.isMultiUserClient()) {
            return;
        }
        ChAOKbManager.saveChAOProject(p.getKnowledgeBase());
    }

    @Override
    public void beforeClose(Project p) {
    	//very conservative
    	OntologyComponentCache.clearCache();
    	
        if (p.isMultiUserClient()) {
            return;
        }
        KnowledgeBase kb = p.getKnowledgeBase();
        PostProcessorManager postProcessorManager = getPostProcessorManager(kb);
        if (postProcessorManager != null) {
            if (!p.isMultiUserServer()) {
                //FIXME: don't dispose for now. The ChAOKBManager should handle this.
                //changesDb.getChangesKb().dispose();
            }
            postProcessorManagerMap.remove(kb);
        }

        //TODO: who removes the KB listeners on the client?!
    }

    /* ---------------------------- End of Project Plugin Interfaces ---------------------------- */

    public static boolean isChangeTrackingEnabled(Project p) {
        boolean trackChanges = p.getChangeTrackingActive();

        if (trackChanges) {
            return true;
        }

        //If the update modification slots is not set, try to find the changes tab

        String changesTabClassName = ChangesTab.class.getName();
        for (Object o : p.getTabWidgetDescriptors()) {
            WidgetDescriptor w = (WidgetDescriptor) o;
            if (w.isVisible() && changesTabClassName.equals(w.getWidgetClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInitialized(Project p) {
        return postProcessorManagerMap.get(p.getKnowledgeBase()) != null;
    }

    public static void initialize(Project p) {
        if (p.isMultiUserClient()) {
            return;
        }
        Project currentProj = p;
        KnowledgeBase currentKB = currentProj.getKnowledgeBase();

        KnowledgeBase changesKb = ChAOKbManager.getChAOKb(currentKB);
        if (changesKb == null) {
            Log.getLogger().warning("Could not find the ChAO KB.");
            return;
        }

        installPostProcessors(currentKB);

        if (p.isMultiUserServer()) {
            ServerFrameStore.requestEventDispatch(currentKB);
            ((DefaultKnowledgeBase) changesKb).setCacheMachine(new ChangeOntStateMachine(changesKb));
        }

        // Register listeners
        if ( Util.kbInOwl(currentKB)) {
            ChangesProjectOWL.registerOwlListeners(currentKB);
        } else {
            registerKBListeners(currentKB);
        }
    }

    private static void registerKBListeners(KnowledgeBase currentKB) {
        currentKB.addKnowledgeBaseListener(new ChangesKBListener(currentKB));
        currentKB.addClsListener(new ChangesClsListener(currentKB));
        currentKB.addInstanceListener(new ChangesInstanceListener(currentKB));
        currentKB.addSlotListener(new ChangesSlotListener(currentKB));
        currentKB.addTransactionListener(new ChangesTransListener(currentKB));
        currentKB.addFrameListener(new ChangesFrameListener(currentKB));
    }

    private static void installPostProcessors(KnowledgeBase currentKB) {
        PostProcessorManager ppm = postProcessorManagerMap.get(currentKB);
        if (ppm == null) {
            ppm = new PostProcessorManager(currentKB);
            ppm.addPostProcessor(new AnnotationCombiner());
            ppm.addPostProcessor(new JoinCreateAndNameChange());
            ppm.addPostProcessor(new JoinInstanceCreateAndAdd());
            postProcessorManagerMap.put(currentKB, ppm);
        }
    }

    public static String getUserName(KnowledgeBase currentKB) {
        return currentKB.getUserName();
    }

    public static PostProcessorManager getPostProcessorManager(KnowledgeBase kb) {
        return postProcessorManagerMap.get(kb);
    }

    public static KnowledgeBase getChangesKB(KnowledgeBase kb) {
        return ChAOKbManager.getChAOKb(kb);
    }

    public static Project getChangesProj(KnowledgeBase kb) {
        KnowledgeBase chaoKb = getChangesKB(kb);
        return chaoKb == null ? null : chaoKb.getProject();
    }

    @Override
    public String getName() {
        return "Changes Project Plugin";
    }

    public static void displayErrors(Collection errors) {
        Iterator i = errors.iterator();
        while (i.hasNext()) {
            Object elem = i.next();
            if (elem instanceof Throwable) {
                Log.getLogger().log(Level.WARNING, "Warnings at loading changes project", (Throwable) elem);
            } else if (elem instanceof MessageError) {
                Log.getLogger().log(Level.WARNING, ((MessageError) elem).getMessage(),
                        ((MessageError) elem).getException());
            } else {
                Log.getLogger().warning(elem.toString());
            }
        }
    }

    public static void main(String[] args) {
        Application.main(args);
    }

}
