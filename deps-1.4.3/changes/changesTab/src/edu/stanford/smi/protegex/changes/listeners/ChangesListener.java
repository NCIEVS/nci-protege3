package edu.stanford.smi.protegex.changes.listeners;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.changes.ChangeProjectUtil;
import edu.stanford.smi.protegex.changes.ChangesTab;
import edu.stanford.smi.protegex.server_changes.model.AbstractChangeListener;


public class ChangesListener extends AbstractChangeListener {
    private final static Logger log = Log.getLogger(ChangesListener.class);

    private ChangesTab changesTab;

    public ChangesListener(KnowledgeBase changes_kb, ChangesTab changesTab) {
        super(changes_kb);

        this.changesTab = changesTab;
    }

    @Override
	public void addChange(Change change) {
        ChangeProjectUtil.logAnnotatableThing("ChangeTab listener received change", log, Level.FINE, change);
        changesTab.createChange(change);
    }

    @Override
    public void modifyChange(Change change, Slot slot, List oldValues) {
    	ChangeProjectUtil.logAnnotatableThing("ChangeTab listener received modification to change", log, Level.FINE, change);
        changesTab.modifyChange(change, slot, oldValues);
    }

    @Override
	public void addAnnotation(Annotation a) { }

}

