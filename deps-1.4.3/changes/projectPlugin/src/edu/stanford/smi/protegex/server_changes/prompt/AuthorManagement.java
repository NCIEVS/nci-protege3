package edu.stanford.smi.protegex.server_changes.prompt;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protegex.changes.ChangeProjectUtil;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.server_changes.prompt.FilterPanel.ComponentFilter;

public class AuthorManagement {
    private KnowledgeBase kb;

    private Map<String, Set<String>> userConflictsMap = new HashMap<String, Set<String>>();
    private Map<String, Set<Ontology_Component>> conflictingFrameMap = new HashMap<String, Set<Ontology_Component>>();
    private Set<Ontology_Component> conflictedFrames = new HashSet<Ontology_Component>();
    private Map<String, Set<Ontology_Component>> unconflictedFrameMap = new HashMap<String, Set<Ontology_Component>>();
    private Set<Ontology_Component> unconflictedFrames = new HashSet<Ontology_Component>();

    private Set<String> active_users  = new HashSet<String>();

    private Set<ComponentFilter> filters;
    private boolean filter_anonymous_guys;
    private boolean nothing_to_filter;
    public final static Set<FilterPanel.ComponentFilter> DEFAULT_FILTERS = Collections.unmodifiableSet(EnumSet.of(FilterPanel.ComponentFilter.CLASS));

    protected AuthorManagement(KnowledgeBase kb1, KnowledgeBase kb2) {
        this.kb = kb2;
        evaluateConflicts();
        setFilters(AuthorManagement.DEFAULT_FILTERS);
    }

    public static AuthorManagement getAuthorManagement(KnowledgeBase kb1, KnowledgeBase kb2) {
    	if (ChAOKbManager.getChAOKb(kb2) == null) {
    		return null;
    	}

    	return new AuthorManagement(kb1, kb2);
    }

    public void reinitialize() {
        userConflictsMap.clear();
        conflictingFrameMap.clear();
        conflictedFrames.clear();
        unconflictedFrameMap.clear();
        unconflictedFrames.clear();
        active_users.clear();
        evaluateConflicts();
    }

    private void evaluateConflicts() {

        Map<Ontology_Component, Set<String>> whoChangedMeMap = new HashMap<Ontology_Component, Set<String>>();

        for (Object o : ChangeProjectUtil.getSortedChanges(ChAOKbManager.getChAOKb(kb))) {
            Change change = (Change) o;
            Ontology_Component frame = change.getApplyTo();
            Set<String> users = whoChangedMeMap.get(frame);
            if (users == null) {
                users = new HashSet<String>();
                whoChangedMeMap.put(frame, users);
            }
            String user = change.getAuthor();
            users.add(user);
            active_users.add(user);
        }
        for (Entry<Ontology_Component, Set<String>> entry : whoChangedMeMap.entrySet()) {
            Ontology_Component frame = entry.getKey();
            Set<String> users = entry.getValue();
            if (users.size() > 1) {
                for (String user : users) {
                    Set<Ontology_Component> frames = getConflictedFrames(user);
                    frames.add(frame);
                    conflictedFrames.add(frame);

                    Set<String> conflictingUsers = getUsersInConflictWith(user);
                    conflictingUsers.addAll(users);
                }
            }
            else {
                for (String user : users) {
                    Set<Ontology_Component> frames = getUnConlictedFrames(user);
                    frames.add(frame);
                    unconflictedFrames.add(frame);
                }
            }
        }
        for (Entry<String, Set<String>> entry : userConflictsMap.entrySet()) {
            String user = entry.getKey();
            Set<String> conflictingUsers = entry.getValue();
            conflictingUsers.remove(user);
        }
    }

    public Set<String> getUsersInConflictWith(String user) {
        Set<String> conflictingUsers = userConflictsMap.get(user);
        if (conflictingUsers == null) {
            conflictingUsers = new HashSet<String>();
            userConflictsMap.put(user, conflictingUsers);
        }
        return conflictingUsers;
    }

    public Set<Ontology_Component> getConflictedFrames(String user) {
        Set<Ontology_Component> myConflictingFrames = conflictingFrameMap.get(user);
        if (myConflictingFrames == null) {
            myConflictingFrames = new HashSet<Ontology_Component>();
            conflictingFrameMap.put(user, myConflictingFrames);
        }

        return myConflictingFrames;
    }

    public Set<Ontology_Component> getConflictedFrames() {
        return Collections.unmodifiableSet(conflictedFrames);
    }


	public Set<Ontology_Component> getUnConlictedFrames(String user) {
        Set<Ontology_Component> myUnconflictedFrames = unconflictedFrameMap.get(user);
        if (myUnconflictedFrames == null) {
            myUnconflictedFrames = new HashSet<Ontology_Component>();
            unconflictedFrameMap.put(user, myUnconflictedFrames);
        }

        return myUnconflictedFrames;
    }

	public Set<Ontology_Component> getUnconflictedFrames() {
	    return Collections.unmodifiableSet(unconflictedFrames);
	}


	public Set<Ontology_Component> getFilteredConflictedFrames(String user) {
	    Set<Ontology_Component> myConflictingFrames = new HashSet<Ontology_Component>(getConflictedFrames(user));
	    filter(myConflictingFrames);
	    return myConflictingFrames;
	}

	public Set<Ontology_Component> getFilteredUnConflictedFrames(String user) {
	    Set<Ontology_Component> myUnConflictingFrames = new HashSet<Ontology_Component>(getUnConlictedFrames(user));
	    filter(myUnConflictingFrames);
	    return myUnConflictingFrames;
	}

    public Set<String> getUsers() {
        return active_users;
    }

    public Set<String> getEditorsByFrameName(String name, boolean oldName) {
    	KnowledgeBase changesKb = ChAOKbManager.getChAOKb(kb);
        Ontology_Component component = oldName ?
        		ChangeProjectUtil.getOntologyComponentByInitialName(changesKb, name) :
                ChangeProjectUtil.getOntologyComponentByFinalName(changesKb, name);
        Set<String> editors  = new HashSet<String>();
        if (component == null) {
			return editors;
		}
        Collection changes = component.getChanges();
        if (changes != null) {
            for (Object o : changes) {
                if (o instanceof Change) {
                    Change change = (Change) o;
                    editors.add(change.getAuthor());
                }
            }
        }
        return editors;
    }

    public void filter(Set<Ontology_Component> frames) {
        if (nothing_to_filter) {
			return;
		}
        Set<Ontology_Component> remove = new HashSet<Ontology_Component>();
        for (Ontology_Component frame : frames) {
            if (doFilter(frame)) {
                remove.add(frame);
            }
        }
        frames.removeAll(remove);
    }

    private boolean doFilter(Ontology_Component frame) {
        if (filter_anonymous_guys && frame.isAnonymous()) {
            return true;
        }
        for (ComponentFilter filter : filters) {
            if (filter != ComponentFilter.ANONYMOUS && filter.allow(frame)) {
				return false;
			}
        }
        return true;
    }


    public Set<ComponentFilter> getFilters() {
        return Collections.unmodifiableSet(filters);
    }


    public void setFilters(Set<ComponentFilter> filters) {
        this.filters = filters;
        filter_anonymous_guys = kb instanceof OWLModel && !filters.contains(ComponentFilter.ANONYMOUS);
        nothing_to_filter = nothingToFilter();
    }

    private boolean nothingToFilter() {
        for (ComponentFilter filter : ComponentFilter.values()) {
            if (!filters.contains(filter)) {
                return false;
            }
        }
        return true;
    }

}
