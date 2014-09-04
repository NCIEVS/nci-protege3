package com.clarkparsia.protege.change.listener;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.clarkparsia.protege.change.ChangeLog;
import com.clarkparsia.protege.change.Converter;
import com.clarkparsia.protege.exceptions.ConversionException;

import edu.stanford.smi.protegex.owl.model.RDFList;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.event.ModelAdapter;
import edu.stanford.smi.protege.util.Log;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Aug 22, 2007 10:16:42 AM
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class ProtegeChangeModelListener extends ModelAdapter {
    private final Logger LOGGER = Log.getLogger(ProtegeChangeModelListener.class);

    private Converter mConverter;
    private ChangeLog mChangeLog;

    public ProtegeChangeModelListener(Converter theConverter, ChangeLog theLog) {
        mConverter = theConverter;
        mChangeLog = theLog;
    }

    public void classCreated(RDFSClass theClass) {
		LOGGER.fine("ModelListener: class created " + theClass);
		if (!theClass.isAnonymous())
			try {
				mChangeLog.queueAdd(mConverter.createDeclarationAxiom(theClass));
				mChangeLog.recordNewResource(theClass);
			} catch( ConversionException e ) {
				LOGGER
						.log(
								Level.SEVERE,
								"Error affecting reasoner synchronization: failure attempting to queue class declaration axiom for addition",
								e);
			}
	}

	public void classDeleted(RDFSClass theClass) {
		LOGGER.fine("ModelListener: class deleted " + theClass);
		if (!theClass.isAnonymous())
			try {
				mChangeLog.queueRemove(mConverter.createDeclarationAxiom(theClass));
			} catch( ConversionException e ) {
				LOGGER
						.log(
								Level.SEVERE,
								"Error affecting reasoner synchronization: failure attempting to queue class declaration axiom for removal",
								e);
			}
	}

    public void individualCreated(RDFResource theResource) {
        LOGGER.fine("ModelListener: individual created " + theResource);

        if (theResource instanceof RDFList)
        	return;
        
        try {
			mChangeLog.queueAdd(mConverter.createDeclarationAxiom(theResource));
		} catch( ConversionException e ) {
			LOGGER
					.log(
							Level.SEVERE,
							"Error affecting reasoner synchronization: failure attempting to queue individual declaration axiom for addition",
							e);
		}
    }

    public void individualDeleted(RDFResource theResource) {
        LOGGER.fine("ModelListener: individual deleted " + theResource);

        if (theResource instanceof RDFList)
        	return;
        
        try {
			mChangeLog.queueRemove(mConverter.createDeclarationAxiom(theResource));
		} catch( ConversionException e ) {
			LOGGER
					.log(
							Level.SEVERE,
							"Error affecting reasoner synchronization: failure attempting to queue individual declaration axiom for removal",
							e);
		}
    }

    public void propertyCreated(RDFProperty theProperty) {
		LOGGER.fine("ModelListener: property created " + theProperty);
		/*
		 * Tracking is performed at
		 * ProtegeChangePropertyValueListener.handlePropertyTypeChange which
		 * also includes property unary axioms
		 */
	}

    public void propertyDeleted(RDFProperty theProperty) {
        LOGGER.fine("ModelListener: property deleted " + theProperty);
        // Unable to get property name at this point.  Must catch deletion elsewhere.
    }

    public void resourceNameChanged(RDFResource theResource, String theName) {
        LOGGER.fine("ModelListener: resource name changed " + theResource + " - " + theName);
    }
}
