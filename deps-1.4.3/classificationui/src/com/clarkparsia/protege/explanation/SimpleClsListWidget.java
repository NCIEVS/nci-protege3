package com.clarkparsia.protege.explanation;

import javax.swing.Action;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.widget.ClsListWidget;
import edu.stanford.smi.protege.util.AllowableAction;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.DoubleClickListener;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLModel;

/**
 * Title: A extension of the ClsListWidget that simplies the UI and overrides un-wanted default functionality, such as
 * the double click action, and the buttons for editing the class list.<br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Apr 12, 2007 10:04:28 AM
 *
 * @author Michael Grove <mhgrove@hotmail.com>
 * @see edu.stanford.smi.protege.widget.ClsListWidget
 */
public class SimpleClsListWidget extends ClsListWidget {
    private ResourceKey mResourceKey;
    private boolean mFilterAnonymous;
    private String mMethodName;

    public SimpleClsListWidget(ResourceKey theKey) {
        this(theKey, true, null);
    }

    public SimpleClsListWidget(ResourceKey theKey, boolean theFilterAnonymous, String theMethodName) {
        mResourceKey = theKey;

        mFilterAnonymous = theFilterAnonymous;

        mMethodName = theMethodName;

        setDoubleClickListener(new DoubleClickListener() {
            public void onDoubleClick(Object object) {
                // do nothing, we dont want anything to happen when the user double clicks
            }
        });
    }

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        return ClsListWidget.isSuitable(cls, slot, facet);
    }

    @Override
    public String getLabel() {
        return localizeStandardLabel(super.getLabel(), null, mResourceKey);
    }

    @Override
    public void clearSelection() {
        super.clearSelection();

        getList().clearSelection();
    }

    @Override
    public void setEditable(boolean theEdit) {
        super.setEditable(theEdit);
        setAllowed((AllowableAction)getViewInstanceAction(), false);
    }

    @Override
    public void setInstance(Instance instance) {
        super.setInstance(instance);

        // trying to avoid that invalid border crap
        getList().setCellRenderer(createRenderer());
    }

    @Override
    public void updateBorder(Collection theValues) {
        super.updateBorder(theValues);

        // trying to avoid the invalid border crap
        setNormalBorder();
    }

    @Override
    protected void addButtons(LabeledComponent theComponent, Action theViewAction) {
        // do nothing, we dont want any buttons

//         or maybe can we do it like this instead??
//        super.addButtons(theComponent, theViewAction);
//        addButtonConfiguration(theViewAction, false);
    }

    @Override
    public void setValues(Collection theValues) {
        Collection aNewValues = new ArrayList(theValues);

        if (mMethodName != null && getInstance() instanceof OWLNamedClass) {
            OWLNamedClass aClass = (OWLNamedClass) getInstance();

            try {
                aNewValues = new ArrayList((Collection) aClass.getClass().getMethod(mMethodName).invoke(aClass));
            }
            catch (Exception ex) {
                System.err.println("no method found for cls list widget");
            }
        }
        
        OWLNamedClass owlNothing = null;
        boolean showingInferredSubClasses = false;
        if (getKnowledgeBase() instanceof OWLModel) {
        	OWLModel model = (OWLModel) getKnowledgeBase();
	        owlNothing = model.getOWLNothing();
	        showingInferredSubClasses = getSlot().equals(model.getProtegeInferredSubclassesProperty());
        }

        // pull out any values we're not interested in seeing in the list
        // 1) anon values and we want to filter anonymous values
        // 2) owl:thing
        // 3) owl:Nothing when the slot is inferred subclasses
        Iterator aIter = aNewValues.iterator();
        while (aIter.hasNext()) {
            Instance aObj = (Instance) aIter.next();

            if ((isAnon(aObj) && mFilterAnonymous) ||
                getKnowledgeBase().getRootCls().equals(aObj) ||
                (showingInferredSubClasses && owlNothing.equals(aObj))) {

                // we will ignore (remove) it from the list of values for this slot
                aIter.remove();
            }
        }

        super.setValues(aNewValues);
    }

    private boolean isAnon(Instance theInst) {
        if (theInst instanceof RDFSClass) {
            RDFSClass aClass = (RDFSClass) theInst;
            return aClass.isAnonymous();
        }
        else {
            // this is sort of hackish, but if we're not using the owl stuff for protege, this might be the only
            // easy way to distinguish between what is named and what isnt
            return theInst.getName().startsWith("@");
        }
    }

//    public void setWidgetValues() {
//        Collection values;
//        if (getAssociatedCls() == null) {
//            values = new ArrayList(getInstance().getOwnSlotValues(getSlot()));
//            boolean editable = getInstance().isEditable();
//            editable &= getSlot().getValueType() == ValueType.CLS || getInstance().getOwnSlotAllowsMultipleValues(getSlot())
//                    || getInstance().getDirectType().getTemplateSlotValues(getSlot()).isEmpty();
//            setEditable(editable);
//        } else {
//            Slot instanceSlot = (Slot) getInstance();
//            Facet facet = getSlot().getAssociatedFacet();
//            if (facet == null) {
//                values = Collections.EMPTY_LIST;
//                setEditable(false);
//            } else {
//                values = getAssociatedCls().getTemplateFacetValues(instanceSlot, facet);
//                boolean editable = getAssociatedCls().isEditable();
//                setEditable(editable);
//            }
//        }
//        try {
//            setValues(values);
//            updateBorder(values);
//        } catch (Exception e) {
//            Log.getLogger().warning(e.toString());
//            setValues(Collections.EMPTY_LIST);
//        }
//    }
}
