package com.clarkparsia.protege.change.listener;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.clarkparsia.protege.change.ChangeList;
import com.clarkparsia.protege.change.ChangeLog;
import com.clarkparsia.protege.change.Converter;
import com.clarkparsia.protege.exceptions.ConversionException;

import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.event.PropertyAdapter;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Aug 22, 2007 10:13:59 AM
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class ProtegeChangePropertyListener extends PropertyAdapter {
    private final Logger LOGGER = Log.getLogger(ProtegeChangePropertyListener.class);

    private Converter mConverter;
    private ChangeLog mChangeLog;

    public ProtegeChangePropertyListener(Converter theConverter, ChangeLog theLog) {
        mConverter = theConverter;
        mChangeLog = theLog;
    }

    @Override
    public void subpropertyAdded(RDFProperty theProperty, RDFProperty theSubProperty) {
    	if (LOGGER.isLoggable(Level.FINE))
    		LOGGER.fine("PropertyListener: sub property added " + theProperty + " - " + theSubProperty);
		/*
		 * Tracking is performed at superpropertyAdded
		 */
    }

    @Override
    public void subpropertyRemoved(RDFProperty theProperty, RDFProperty theSubProperty) {
    	if (LOGGER.isLoggable(Level.FINE))
    		LOGGER.fine("PropertyListener: sub property removed " + theProperty + " - " + theSubProperty);
		/*
		 * Tracking is performed at superpropertyRemoved
		 */
    }

    @Override
    public void superpropertyAdded(RDFProperty theProperty, RDFProperty theSuperProperty) {
    	if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("PropertyListener: super property added " + theProperty + " - " + theSuperProperty);

		try {
			mChangeLog.queueAdd(mConverter.createSubPropertyAxiom(theProperty, theSuperProperty));
		}
		catch (ConversionException e) {
			LOGGER
					.log(
							Level.SEVERE,
							"Error affecting reasoner synchronization: failure attempting to queue subproperty axiom for addition",
							e);
		}
	}

    @Override
    public void superpropertyRemoved(RDFProperty theProperty, RDFProperty theSuperProperty) {
    	if (LOGGER.isLoggable(Level.FINE))
    		LOGGER.fine("PropertyListener: super property removed " + theProperty + " - " + theSuperProperty);

        try {
			mChangeLog
					.queueRemove(mConverter.createSubPropertyAxiom(theProperty, theSuperProperty));
		}
		catch (ConversionException e) {
			LOGGER
					.log(
							Level.SEVERE,
							"Error affecting reasoner synchronization: failure attempting to queue subproperty axiom for removal",
							e);
		}
    }

    private void updateDomain(RDFProperty theProperty, Collection theNewDomain, Set<RDFSClass> theOldDomain) {

    	if (theProperty.isSystem()) {
			final String msg = "System property passed as argument: " + theProperty;
			LOGGER.severe(msg);
			throw new IllegalArgumentException(msg);
		}

    	final OWLNamedClass aThing = theProperty.getOWLModel().getOWLThingClass();
		try {
			ChangeList aChangeList = new ChangeList();
			if (!theOldDomain.isEmpty()
					&& !theOldDomain.equals(Collections.singleton(aThing))) {
				aChangeList.axiomRemoved(mConverter.createDomainAxiom(theOldDomain, theProperty));
			}
			if (!theNewDomain.isEmpty()
					&& (theNewDomain.size() > 1 || !theNewDomain.contains(aThing))) {
				aChangeList.axiomAdded(mConverter.createDomainAxiom(theNewDomain, theProperty));
			}

			mChangeLog.queueChanges(aChangeList);
		}
		catch (ConversionException e) {
			String aPropertyString = (theProperty == null) ? "NULL" : theProperty.toString();
			LOGGER
					.log(
							Level.SEVERE,
							"Error affecting reasoner synchronization: failure attempting to queue property domain axiom for addition ("
									+ aPropertyString + ")", e);
		}

	}
    
    @Override
	public void unionDomainClassAdded(RDFProperty theProperty, RDFSClass theClass) {
		// Ignore domain changes for system slots (e.g., rdfs:label)
		if (theProperty.isSystem()) {
			if (LOGGER.isLoggable(Level.FINER))
				LOGGER.finer("Ignoring domain change for system property: " + theProperty);
			return;
		}

		// Ignore domain changes for annotation properties
		if (theProperty.isAnnotationProperty()) {
			if (LOGGER.isLoggable(Level.FINER))
				LOGGER.finer("Ignoring domain change for annotation property: " + theProperty);
			return;
		}

    	if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("PropertyListener: union domain class added " + theProperty + " - " + theClass);

		final Collection aNewDomain = theProperty.getUnionDomain();
		Set<RDFSClass> aOldDomain = new HashSet<RDFSClass>(aNewDomain);
		aOldDomain.remove(theClass);

		updateDomain(theProperty, aNewDomain, aOldDomain);
	}

	@Override
	public void unionDomainClassRemoved(RDFProperty theProperty, RDFSClass theClass) {
		// Ignore domain changes for system slots (e.g., rdfs:label)
		if (theProperty.isSystem()) {
			if (LOGGER.isLoggable(Level.FINER))
				LOGGER.finer("Ignoring domain change for system property: " + theProperty);
			return;
		}

		// Ignore domain changes for annotation properties
		if (theProperty.isAnnotationProperty()) {
			if (LOGGER.isLoggable(Level.FINER))
				LOGGER.finer("Ignoring domain change for annotation property: " + theProperty);
			return;
		}
		
		if (LOGGER.isLoggable(Level.FINER))
			LOGGER.finer("PropertyListener: union domain class rmeoved " + theProperty + " - "
				+ theClass);

		final Collection aNewDomain = theProperty.getUnionDomain();
		Set<RDFSClass> aOldDomain = new HashSet<RDFSClass>(aNewDomain);
		aOldDomain.add(theClass);

		updateDomain(theProperty, aNewDomain, aOldDomain);
	}
}
