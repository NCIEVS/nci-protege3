package com.clarkparsia.protege.change.listener;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.clarkparsia.protege.change.ChangeLog;
import com.clarkparsia.protege.change.Converter;
import com.clarkparsia.protege.exceptions.ConversionException;

import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.event.ResourceAdapter;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Aug 22, 2007 10:20:21 AM
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class ProtegeChangeResourceListener extends ResourceAdapter {
    private final Logger LOGGER = Log.getLogger(ProtegeChangeResourceListener.class);

    private Converter mConverter;
    private ChangeLog mChangeLog;

    public ProtegeChangeResourceListener(Converter theConverter, ChangeLog theLog) {
        mConverter = theConverter;
        mChangeLog = theLog;
    }

    @Override
	public void typeAdded(RDFResource theResource, RDFSClass theType) {
		LOGGER.fine("ResourceListener: type added: " + theResource + " - " + theType);

		if (theResource instanceof RDFIndividual) {
			try {
				mChangeLog.queueAdd(mConverter.createClassAssertionAxiom(theResource, theType));
			} catch( ConversionException e ) {
				LOGGER
						.log(
								Level.SEVERE,
								"Error affecting reasoner synchronization: failure attempting to queue class assertion axiom for addition",
								e);
			}
		}
	}

	@Override
	public void typeRemoved(RDFResource theResource, RDFSClass theType) {
		LOGGER.fine("ResourceListener: type removed: " + theResource + " - " + theType);

		if (theResource instanceof RDFIndividual) {
			try {
				mChangeLog.queueRemove(mConverter.createClassAssertionAxiom(theResource, theType));
			} catch( ConversionException e ) {
				LOGGER
						.log(
								Level.SEVERE,
								"Error affecting reasoner synchronization: failure attempting to queue class assertion axiom for removal",
								e);
			}
		}
	}
}
