package edu.stanford.smi.protegex.prompt.ui.diffUI;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.util.MutableSelectable;
import edu.stanford.smi.protege.util.SelectionEvent;
import edu.stanford.smi.protege.util.SelectionListener;
import edu.stanford.smi.protegex.changes.ChangeProjectUtil;
import edu.stanford.smi.protegex.prompt.promptDiff.PromptDiff;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.TableRow;
import edu.stanford.smi.protegex.server_changes.prompt.DiffUserView;

public class PromptDiffUserView extends DiffUserView implements MutableSelectable {
    private static final long serialVersionUID = 7266972178961933881L;
    private Map<SelectionListener, SelectionListener> wrappedSelectionListeners
                                    = new HashMap<SelectionListener, SelectionListener>();
    private PromptDiff diff;
    private PromptUserConceptList userConceptList;
    private List<TableRow> results;

    public PromptDiffUserView(PromptDiff diff, List<TableRow> results) {
        super(diff.getKb1(), diff.getKb2());
        this.diff = diff;
        this.results = results;
        setAuthorManagement(diff.getAuthorManagement());
    }

    @Override
    protected PromptUserConceptList getUserConceptList() {
        if (userConceptList == null) {
            userConceptList = new PromptUserConceptList(diff, results);
            userConceptList.initialize();
        }
        return userConceptList;
    }

    public void reset() {
        reloadDiffPanel();
        userTableModel.fireTableDataChanged();
        authorManagement.reinitialize();
    }

    public void clearSelection() {
        getUserConceptList().clearSelection();
    }

    public void notifySelectionListeners() {
        getUserConceptList().notifySelectionListeners();
    }

    public void removeSelectionListener(SelectionListener listener) {
        SelectionListener wrappedSelectionListener = wrappedSelectionListeners.remove(listener);
        getUserConceptList().removeSelectionListener(wrappedSelectionListener);
    }

    public void addSelectionListener(final SelectionListener listener) {
        SelectionListener wrappedListener = new SelectionListener() {

            public void selectionChanged(SelectionEvent event) {
                SelectionEvent outerEvent = new SelectionEvent(PromptDiffUserView.this, event.getEventType());
                listener.selectionChanged(outerEvent);
            }

        };
        wrappedSelectionListeners.put(listener, wrappedListener);
        getUserConceptList().addSelectionListener(wrappedListener);
    }

    @SuppressWarnings("unchecked")
    public void setSelection(Collection objects) {
        Set<Ontology_Component> unconflictedSelection = new HashSet<Ontology_Component>();
        Set<Ontology_Component> conflictedSelection = new HashSet<Ontology_Component>();
        Set<Ontology_Component> components = getComponentsFromFrames(objects);
        Set<String> users = getUsersTouching(components);
        addComponentSelectionsFromUsers(components, unconflictedSelection, conflictedSelection);
        setSelectedUsers(users);
        getUserConceptList().setSelection(unconflictedSelection, conflictedSelection);
    }

    @SuppressWarnings("unchecked")
    private Set<Ontology_Component> getComponentsFromFrames(Collection objects) {
        Set<Ontology_Component> components = new HashSet<Ontology_Component>();
        for (Object o : objects) {
            if (o instanceof Frame) {
                Frame frame = (Frame) o;
                String name = frame.getName();
                Ontology_Component c = null;
                if (frame.getKnowledgeBase().equals(kb2)) {
                	//TODO: Check if here is kb1 or kb2..
                    c = ChangeProjectUtil.getOntologyComponentByFinalName(ChAOKbManager.getChAOKb(kb2), name);
                }
                else if (frame.getKnowledgeBase().equals(kb1)) {
                    c = ChangeProjectUtil.getOntologyComponentByInitialName(ChAOKbManager.getChAOKb(kb2), name);
                }
                if (c != null && c.getChanges() != null && !c.getChanges().isEmpty()) {
                    components.add(c);
                }
            }
        }
        authorManagement.filter(components);
        return components;
    }

    @SuppressWarnings("unchecked")
    private Set<String> getUsersTouching(Set<Ontology_Component> components) {
        Set<String> users = new HashSet<String>();
        for (Ontology_Component component : components) {
            Collection changes = component.getChanges();
            if (changes != null) {
                for (Object o : changes) {
                    if (o instanceof Change) {
                        Change change = (Change) o;
                        String user = change.getAuthor();
                        if (user != null) {
							users.add(user);
						}
                    }
                }
            }
        }
        return users;
    }

    private void addComponentSelectionsFromUsers(Set<Ontology_Component> components,
                                                 Set<Ontology_Component> unconflictedSelection,
                                                 Set<Ontology_Component> conflictedSelection) {
        Set<Ontology_Component> allConflictedComponents = authorManagement.getConflictedFrames();
        Set<Ontology_Component> allUnconflictedComponents = authorManagement.getUnconflictedFrames();
        for (Ontology_Component component : components) {
            if (allConflictedComponents.contains(component)) {
                conflictedSelection.add(component);
            }
            else if (allUnconflictedComponents.contains(component)) {
                unconflictedSelection.add(component);
            }
        }
    }

    public Collection<Frame> getSelection() {
        Collection<Ontology_Component> ocSelection = getUserConceptList().getSelection();
        Collection<Frame> selection = new HashSet<Frame>();
        for (Ontology_Component oc : ocSelection) {
            if (oc.getCurrentName() != null) {
                selection.add(kb2.getFrame(oc.getCurrentName()));
            }
            else {
                selection.add(kb1.getFrame(oc.getInitialName()));
            }
        }
        return selection;
    }

    public void reloadDiffPanel() {
        userConceptList.reloadDiffPanel();
    }
}
