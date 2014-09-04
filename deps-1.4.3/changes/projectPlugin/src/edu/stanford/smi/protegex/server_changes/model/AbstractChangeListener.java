package edu.stanford.smi.protegex.server_changes.model;

import java.util.List;

import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.change.api.ChangeFactory;
import edu.stanford.smi.protege.code.generator.wrapping.OntologyJavaMappingUtil;
import edu.stanford.smi.protege.event.FrameAdapter;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;

public abstract class AbstractChangeListener extends FrameAdapter {
    private Slot applyToSlot;
    private Slot annotatesSlot;
    private Cls changeCls;

    public AbstractChangeListener(KnowledgeBase changesKb) {
        applyToSlot = new ChangeFactory(changesKb).getApplyToSlot();
        annotatesSlot = new AnnotationFactory(changesKb).getAnnotatesSlot();
        changeCls = new ChangeFactory(changesKb).getChangeClass();
    }

    @Override
	public void ownSlotValueChanged(FrameEvent event) {
        Frame frame = event.getFrame();
        Slot slot = event.getSlot();
        if (frame instanceof Instance && ((Instance)frame).hasType(changeCls)) {
            if (slot.equals(applyToSlot)) {
            	Change change = OntologyJavaMappingUtil.getSpecificObject(changeCls.getKnowledgeBase(),(Instance) frame, Change.class);
                addChange(change);
            }
            else if (frame.getOwnSlotValues(applyToSlot) != null) {
            	Change change = OntologyJavaMappingUtil.getSpecificObject(changeCls.getKnowledgeBase(),(Instance) frame, Change.class);
                List oldValues = event.getOldValues();
                modifyChange(change, slot, oldValues);
            }
        }
        else if (frame instanceof Annotation && slot.equals(annotatesSlot)) {
            addAnnotation((Annotation) frame);
        }
    }

    public abstract void addChange(Change change);

    public abstract void modifyChange(Change change, Slot slot, List oldValues);

    public abstract void addAnnotation(Annotation annotation);

}
