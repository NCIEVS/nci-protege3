package com.clarkparsia.protege.change.listener;

import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protegex.owl.model.event.ClassAdapter;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protege.util.Log;

import com.clarkparsia.protege.change.Converter;
import com.clarkparsia.protege.change.ChangeLog;

import com.clarkparsia.protege.exceptions.ConversionException;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Aug 22, 2007 10:13:16 AM
 * 
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class ProtegeChangeClassListener extends ClassAdapter {
	private final Logger LOGGER = Log.getLogger(ProtegeChangeClassListener.class);

	private Converter mConverter;
	private ChangeLog mChangeLog;

	public ProtegeChangeClassListener(Converter theConverter, ChangeLog theLog) {
		mConverter = theConverter;
		mChangeLog = theLog;
	}

	@Override
	public void addedToUnionDomainOf(RDFSClass theClass, RDFProperty theProperty) {
		if (LOGGER.isLoggable(Level.FINER))
			LOGGER.finer("ClassListener: added to union domain of: " + theClass + " - " + theProperty);
	}

	@Override
	public void instanceAdded(RDFSClass theClass, RDFResource theInstance) {
		if (LOGGER.isLoggable(Level.FINER))
			LOGGER.finer("ClassListener: instance added: " + theClass + " - " + theInstance);
	}

	@Override
	public void instanceRemoved(RDFSClass theClass, RDFResource theInstance) {
		if (LOGGER.isLoggable(Level.FINER))
			LOGGER.finer("ClassListener: instance removed: " + theClass + " - " + theInstance);
	}

	@Override
	public void removedFromUnionDomainOf(RDFSClass theClass, RDFProperty theProperty) {
		if (LOGGER.isLoggable(Level.FINER))
			LOGGER.finer("ClassListener: removed from union domain of: " + theClass + " - " + theProperty);
	}

	@Override
	public void subclassAdded(RDFSClass theSuperClass, RDFSClass theSubClass) {
		if (LOGGER.isLoggable(Level.FINER))
			LOGGER.finer("ClassListener: subclass added: " + theSuperClass + " - " + theSubClass);
		/*
		 * Tracking is performed at ProtegeChangePropertyValueListener.handleSubClassOfChange
		 */
	}

	@Override
	public void subclassRemoved(RDFSClass theSuperClass, RDFSClass theSubClass) {
		if (LOGGER.isLoggable(Level.FINER))
			LOGGER.finer("ClassListener: subclass removed: " + theSuperClass + " - " + theSubClass);
		/*
		 * Tracking is performed at ProtegeChangePropertyValueListener.handleSubClassOfChange
		 */
	}

	@Override
	public void superclassAdded(RDFSClass theSubClass, RDFSClass theSuperClass) {
		if (LOGGER.isLoggable(Level.FINER))
			LOGGER.finer("ClassListener: superclass added: " + theSubClass + " - " + theSuperClass);
		/*
		 * Tracking is performed at ProtegeChangePropertyValueListener.handleSubClassOfChange
		 */
	}

	@Override
	public void superclassRemoved(RDFSClass theSubClass, RDFSClass theSuperClass) {
		if (LOGGER.isLoggable(Level.FINER))
			LOGGER.finer("ClassListener: superclass removed: " + theSubClass + " - " + theSuperClass);
		/*
		 * Tracking is performed at ProtegeChangePropertyValueListener.handleSubClassOfChange
		 */
	}
}
