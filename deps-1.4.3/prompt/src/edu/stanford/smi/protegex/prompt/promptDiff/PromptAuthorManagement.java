package edu.stanford.smi.protegex.prompt.promptDiff;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.ResultTable;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.TableRow;
import edu.stanford.smi.protegex.server_changes.ChangesProject;
import edu.stanford.smi.protegex.server_changes.prompt.AuthorManagement;

public class PromptAuthorManagement extends AuthorManagement {
    private PromptDiff promptDiff;
    private KnowledgeBase kb1, kb2;
    private Map<Ontology_Component, Boolean> promptSaysDidntChangeCache = new HashMap<Ontology_Component, Boolean>();


    public PromptAuthorManagement(KnowledgeBase kb1, KnowledgeBase kb2, PromptDiff diff) {
        super(kb1, kb2);
        promptDiff = diff;
        this.kb1 = kb1;
        this.kb2 = kb2;
    }

    public static AuthorManagement getAuthorManagement(KnowledgeBase kb1, KnowledgeBase kb2, PromptDiff diff) {
        if (ChangesProject.getPostProcessorManager(kb2) != null) {
            return new PromptAuthorManagement(kb1, kb2, diff);
        }
        else {
            return null;
        }
    }

    @Override
    public Set<Ontology_Component> getFilteredConflictedFrames(String user) {
        Set<Ontology_Component> components = super.getFilteredConflictedFrames(user);
        promptFilter(components);
        return components;
    }

    @Override
    public Set<Ontology_Component> getFilteredUnConflictedFrames(String user) {
        Set<Ontology_Component> components = super.getFilteredUnConflictedFrames(user);
        promptFilter(components);
        return components;
    }

    private void promptFilter(Set<Ontology_Component> components) {
        Set<Ontology_Component> toRemove = new HashSet<Ontology_Component>();
        for (Ontology_Component component : components) {
            if (promptSaysDidntChange(component)) {
                toRemove.add(component);
            }
        }
        components.removeAll(toRemove);
    }

    private boolean promptSaysDidntChange(Ontology_Component component) {
        if (promptSaysDidntChangeCache.containsKey(component)) {
            return promptSaysDidntChangeCache.get(component);
        }

        ResultTable results = promptDiff.getResultsTable();
        boolean changed = false;

        String initialName = component.getInitialName();
        String currentName = component.getCurrentName();
        boolean existsInitially = initialName != null && kb1.getFrame(initialName) != null;
        boolean existsTerminally = currentName != null && kb2.getFrame(currentName) != null;
        if (existsInitially && !existsTerminally) {
            changed = true;
        }
        else if (!existsInitially && existsTerminally) {
            changed = true;
        }
        else if (!existsInitially && !existsTerminally) {
            changed = false;
        }
        else {
            Frame frame = kb1.getFrame(initialName);
            for (TableRow row : results.getRows(frame)) {
                if (!filteredMappingLevels(row.getMappingLevel())) {
                    changed = true;
                    break;
                }
            }
        }
        promptSaysDidntChangeCache.put(component, !changed);
        return !changed;
    }

    /*
     * It might be simpler to use make this the negation of something like
     *  mappingLevel.equals(MAPPING_LEVEL_DiRECTLY_CHANGED)
     * but the code  below is safer - who knows what mapping levels the plugins use.
     */
    private boolean filteredMappingLevels(String mappingLevel) {
        return mappingLevel == null
                || mappingLevel.equals(TableRow.MAPPING_LEVEL_NOT_SET)
                || mappingLevel.equals(TableRow.MAPPING_LEVEL_UNCHANGED)
                || mappingLevel.equals(TableRow.MAPPING_LEVEL_ISOMORPHIC)
                || mappingLevel.equals(TableRow.MAPPING_LEVEL_WEAK_ISOMORPHIC)
                || mappingLevel.equals(TableRow.MAPPING_LEVEL_STRONG_ISOMORPHIC);
    }

    @Override
    public void reinitialize() {
        promptSaysDidntChangeCache.clear(); // no super - the change model didn't change but the filter did
    }

}
