package gov.nih.nci.protegex.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;

import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.event.ModelAdapter;
import edu.stanford.smi.protegex.owl.model.event.PropertyValueAdapter;
import edu.stanford.smi.protegex.owl.model.event.PropertyValueListener;
import edu.stanford.smi.protegex.owl.model.event.ResourceAdapter;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.panel.ConceptChangedListener;

/**
 * @Author: NGIT, Kim Ong; Iris Guo
 */

public class EventListener {
	Logger logger = Log.getLogger(getClass());

	/**
	 * A FrameListener that detects any changes in own slot values of annotation
	 * properties and then updates the table model accordingly.
	 */

	private PropertyValueListener getPropValListener() {
		return new PropertyValueAdapter() {
			public void propertyValueChanged(RDFResource resource,
					RDFProperty property, Collection oldValues) {
				if (resource instanceof OWLNamedClass) {
					OWLNamedClass owl_cls = (OWLNamedClass) resource;
					if (inListenedToClses(owl_cls)) {
                        ArrayList<ConceptChangedListener> copy = new ArrayList<ConceptChangedListener>();
                        for (ConceptChangedListener cl : listenedToClses.get(owl_cls)) {
                            copy.add(cl);
                        }
						for (ConceptChangedListener l : copy) {
							String propertyName = property.getBrowserText();
							String msg = owl_cls.getBrowserText() + " "
									+ propertyName
									+ " property has been modified.";
							l.conceptChanged(owl_cls, msg);
						}
						/**
						 * 
						 */
					}
				}
			}
		};
	}

	private PropertyValueListener valueListener = this.getPropValListener();

	private OWLModel owlModel;

	// private NCIEditTab tab;

	private HashMap<OWLNamedClass, ArrayList<ConceptChangedListener>> listenedToClses = null;

	// ///////////////////////////////////////////////////////////////////////////////////////////////

	// Constructor
	public EventListener(NCIEditTab tab) {
		// this.tab = tab;
		this.owlModel = tab.getOWLModel();

		// WTF!!!!???
		// owlModel.addPropertyValueListener(this.getPropValListener());

		listenedToClses = new HashMap<OWLNamedClass, ArrayList<ConceptChangedListener>>();

		addOWLModelListners();

	}

	public boolean inListenedToClses(OWLNamedClass cls) {

		return listenedToClses.get(cls) != null;

	}

	public void addToListenedToClses(OWLNamedClass cls, ConceptChangedListener l) {
		ArrayList<ConceptChangedListener> ls = listenedToClses.get(cls);
		if (ls == null) {
			ls = new ArrayList<ConceptChangedListener>();
			cls.addPropertyValueListener(this.valueListener);
		}
        if (!ls.contains(l)) {
            ls.add(l);
        }
		listenedToClses.put(cls, ls);
	}

	public void removeFromListenedToClses(OWLNamedClass cls,
			ConceptChangedListener l) {
		if (cls == null || l == null) {
			return;
		}
		ArrayList<ConceptChangedListener> ls = listenedToClses.get(cls);
		if (ls != null) {
			ls.remove(l);
			if (ls.isEmpty()) {
				listenedToClses.remove(cls);
				cls.removePropertyValueListener(this.valueListener);
			}
		}

	}

	public void clearListenedToClses() {
		listenedToClses = new HashMap<OWLNamedClass, ArrayList<ConceptChangedListener>>();
	}

	private void addOWLModelListners() {
		
		owlModel.addModelListener(new ModelAdapter() {
			@Override
			public void classDeleted(RDFSClass cls) {
				if (!(cls instanceof OWLNamedClass)) {
					return;
				}
				logger.info("Class " + cls.getName() + " deleted");
			}
		});
	}

}
