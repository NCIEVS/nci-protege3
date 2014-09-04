package com.clarkparsia.protege.change;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLAxiomChange;
import org.semanticweb.owl.model.OWLOntologyChange;

import com.clarkparsia.protege.exceptions.ConversionException;
import com.clarkparsia.protege.exceptions.IncompleteInputException;
import com.clarkparsia.protege.reasoner.CustomReasonerProjectPlugin;

import edu.stanford.smi.protege.event.TransactionEvent;
import edu.stanford.smi.protege.event.TransactionListener;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.util.DeletionHook;
import edu.stanford.smi.protege.util.DeletionHookUtil;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.inference.protegeowl.ReasonerManager;
import edu.stanford.smi.protegex.owl.model.OWLAnonymousClass;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNAryLogicalClass;
import edu.stanford.smi.protegex.owl.model.RDFList;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.model.triplestore.Tuple;

/**
 * Title: <br>
 * Description: A ChangeLog for protege. Uses protege listeners to track events
 * of the kb and builds the corresponding OWLAxiom objects and marks them as
 * either adds or deletes from the kb. Encapsulates a protege transaction
 * listener that groups sets of changes (tracked in a ChangeList) in a
 * Transaction. When a transaction end event is caught, we close the Transaction
 * and merge the ChangeList with the master list of changes.<br/> This class is
 * thread-safe <i>only</i> with respect to the change list, which is guarded by
 * this object. So, the following example demonstrates appropriate use
 * 
 * <pre>
 * List&lt;OWLOntologyChange&gt; changes;
 * synchronized( aChangeLog ) {
 * 	changes = aChangeLog.getChanges();
 * 	aChangeLog.clear();
 * }
 * // ... do something with changes ...
 * </pre>
 * 
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Sep 18, 2007 9:34:31 AM
 * 
 * @author Michael Grove <mike@clarkparsia.com>
 * @author Michael Smith <msmith@clarkparsia.com>
 */
public class ChangeLog implements TransactionListener {
    private final Logger LOGGER	= Log.getLogger(ChangeLog.class);

	/**
	 * The master list of changes to the kb.  Never published, concurrent access guarded by {@code this}
	 */
	final private ChangeList			mChangeList;

	/**
	 * The current transaction
	 */
	private Transaction	mCurrentTransaction;

	private Converter			mConverter;

    /**
     * The KB whose changes we are tracking, or null if we're not tracking any changes
     */
    final private OWLModel mOWLModel;

    private List<DeletionHook> mDeletionHooks;
    
    final private Collection<RDFResource> mNewResources;

    public ChangeLog(Converter theConverter, OWLModel theModel) {
        mChangeList = new ChangeList();
        mConverter = theConverter;
        mOWLModel = theModel;
        mDeletionHooks = new ArrayList<DeletionHook>();
        mNewResources = new HashSet<RDFResource>();
        registerDeletionHooks();
    }

    private void maybeSynchReasoner() {
		// if we're not updating the reasoner in real time, just return.
		Object aVal = mOWLModel.getProject().getClientInformation(CustomReasonerProjectPlugin.KEY_SYNCH);

		if (!CustomReasonerProjectPlugin.VALUE_REALTIME.equals(aVal))
			return;

		ReasonerManager.getInstance().getProtegeReasoner(mOWLModel).forceReasonerReSynchronization();
	}

    private void registerDeletionHooks() {
        /*
		 * Setup deletion hook for RDF Resources to cache URIs
		 */
		{
			DeletionHook aDeletionHook = new DeletionHook() {
				public void delete(Frame frame) {
					if (frame instanceof RDFResource) {
						final RDFResource aResource = (RDFResource) frame;
						if (!(aResource instanceof RDFList)) {
							mConverter.cacheURIForResource(aResource);
						}
					}
				}
			};
			DeletionHookUtil.addDeletionHook(mOWLModel, aDeletionHook);
			mDeletionHooks.add(aDeletionHook);
		}
        
        /*
		 * Setup deletion hook for named classes, necessary for deleting
		 * hierarchies together. See ChangeTrackingTests.removeParentClass()
		 */
        {
			DeletionHook aDeletionHook = new DeletionHook() {
				public void delete(Frame frame) {
					if (frame instanceof RDFSNamedClass) {

						RDFSNamedClass aClassBeingDeleted = (RDFSNamedClass) frame;
						Collection<?> aSubs = aClassBeingDeleted.getSubclasses(true);

						Set<RDFSNamedClass> aNamedSubs = new HashSet<RDFSNamedClass>(aSubs.size());
						for (Object aObj : aSubs) {
							if (aObj instanceof RDFSNamedClass) {
								aNamedSubs.add((RDFSNamedClass) aObj);
							}
						}
						for (RDFSNamedClass aNamedSub : aNamedSubs) {
							@SuppressWarnings("cast")
							Set<Object> aDirectSupers = new HashSet<Object>(
									(Collection<?>) aNamedSub.getSuperclasses(false));
							aDirectSupers.remove(aClassBeingDeleted);
							aDirectSupers.removeAll(aNamedSubs);
							if (aDirectSupers.isEmpty()) {
								mConverter.cacheURIForResource(aNamedSub);
							}
						}
					}
				}
			};
			DeletionHookUtil.addDeletionHook(mOWLModel, aDeletionHook);
			mDeletionHooks.add(aDeletionHook);
		}
        
        /*
		 * Setup deletion hook for anonymous classes
		 */
        {
			DeletionHook aDeletionHook = new DeletionHook() {
				public void delete(Frame frame) {
					if (frame instanceof OWLAnonymousClass) {

						OWLAnonymousClass aClassBeingDeleted = (OWLAnonymousClass) frame;
						OWLAnonymousClass aRoot = aClassBeingDeleted.getExpressionRoot();

						LOGGER.fine("Anonymous class deletion hook: " + frame
								+ " attempting to cache root expression: " + aRoot);

						try {
							mConverter.cacheAnonCls(aRoot);
						}
						catch (ConversionException e) {
							if (aRoot instanceof OWLNAryLogicalClass) {
								LOGGER
										.fine("Unable to pre-cache nary logical class being deleted: "
												+ aRoot);
							}
							else LOGGER
									.severe("Conversion exception while trying to save anonymous class expression being deleted"
											+ e);
						}
					}
				}
			};
			DeletionHookUtil.addDeletionHook(mOWLModel, aDeletionHook);
			mDeletionHooks.add(aDeletionHook);
		}
        
        
        /*
		 * Setup deletion hook for rdf:Lists used in class descriptions
		 */
		{
			DeletionHook aDeletionHook = new DeletionHook() {

				private final RDFProperty mOwlUnionOf = mOWLModel.getOWLUnionOfProperty();
				private final RDFProperty mOwlIntersectionOf = mOWLModel
						.getOWLIntersectionOfProperty();

				public void delete(Frame theFrame) {

					if (theFrame instanceof RDFList) {
						final RDFList aList = (RDFList) theFrame;
						try {
							final RDFList aListStart = aList.getStart();
							LOGGER.fine("RDFList being deleted, attempting to cache contents");
							mConverter.cacheList(aListStart);

							@SuppressWarnings("unchecked")
							Iterator<Tuple> aIt = mOWLModel.listReferences(aListStart,
									Integer.MAX_VALUE);
							while( aIt.hasNext() ) {
								Tuple aTuple = aIt.next();
								RDFProperty aProperty = aTuple.getPredicate();
								if (aProperty.equals(mOwlUnionOf) || aProperty.equals(mOwlIntersectionOf)) {
									RDFResource aSubject = aTuple.getSubject();
									if (aSubject instanceof RDFSClass) {
										RDFSClass aCls = (RDFSClass) aSubject;
										mConverter.cacheNaryClass(aCls, mConverter
												.convertClassList(aListStart));
									}
									else LOGGER
											.warning("Object list of " +  aProperty + " deleted with non-class subject");
								}
							}
						}
						catch (IncompleteInputException e) {
							LOGGER.fine("Unable to cache list, already disassembled");
						}
						catch (ConversionException e) {
							LOGGER
									.severe("Conversion exception while trying to save list being deleted: "
											+ e);
						}
					}
				}
			};
			DeletionHookUtil.addDeletionHook(mOWLModel, aDeletionHook);
			mDeletionHooks.add(aDeletionHook);
		}
    }

    /**
	 * Stop logging changes made to the current OWLModel associated with this
	 * ChangeLog
	 */
    private void unregisterDeletionHooks() {
        for (DeletionHook aHook : mDeletionHooks) {
        	DeletionHookUtil.removeDeletionHook(mOWLModel, aHook);
        }
    }

    /**
     * Log an axiom that was added to the KB
     * @param theAxiom the axiom that was added
     */
    public synchronized void queueAdd(OWLAxiom theAxiom) {
        if (mCurrentTransaction == null) {
			Transaction aTransaction = new Transaction();
			aTransaction.startTransaction();
			aTransaction.axiomAdded(theAxiom);
			aTransaction.finalizeTransaction();

            // save the changes we made in this transaction
            mChangeList.add(aTransaction.getTransactionChanges());
            mChangeList.minimize();
            mConverter.reset();

            maybeSynchReasoner();
        }
		else mCurrentTransaction.axiomAdded(theAxiom);
	}

    /**
     * Log an axiom that was removed from the KB.
     * @param theAxiom the axiom that was remove
     */
    public synchronized void queueRemove(OWLAxiom theAxiom) {
		if (mCurrentTransaction == null) {
			Transaction aTransaction = new Transaction();
			aTransaction.startTransaction();
			aTransaction.axiomRemoved(theAxiom);
			aTransaction.finalizeTransaction();

            // save the changes we made in this transaction
            mChangeList.add(aTransaction.getTransactionChanges());
            mChangeList.minimize();
            mConverter.reset();

            maybeSynchReasoner();
        }
		else mCurrentTransaction.axiomRemoved(theAxiom);
	}

    /**
     * Add a list of changes to this change log
     * @param theChangeList the list of changes
     */
    public synchronized void queueChanges(ChangeList theChangeList) {

		Transaction aNewTransaction;
		
		if (mCurrentTransaction == null)
			aNewTransaction = new Transaction();
		else aNewTransaction = new Transaction(mCurrentTransaction);

		aNewTransaction.startTransaction();
		for (OWLAxiomChange aChange : theChangeList.getChanges()) {
			if (ChangeList.isAddition(aChange))
				aNewTransaction.axiomAdded(aChange.getAxiom());
			else aNewTransaction.axiomRemoved(aChange.getAxiom());
		}
		aNewTransaction.finalizeTransaction();

		if (mCurrentTransaction == null) {
            mChangeList.add(aNewTransaction.getTransactionChanges());
            mChangeList.minimize();
            mConverter.reset();
            
            maybeSynchReasoner();
        }
    }

    public void transactionBegin(TransactionEvent theEvent) {
        if (mCurrentTransaction == null) {
            mCurrentTransaction = new Transaction();
        }
        else mCurrentTransaction = new Transaction(mCurrentTransaction);

        mCurrentTransaction.startTransaction();
        
        LOGGER.fine("ChangeLog: transaction started: " + theEvent.getBeginString() );
    }
    
    public void recordNewResource(RDFResource theResource) {
    	mNewResources.add(theResource);
    }
    
    public boolean isNewResource(RDFResource theResource) {
    	return mNewResources.contains(theResource);
    }

    public synchronized void transactionEnded(TransactionEvent theEvent) {
        LOGGER.fine("ChangeLog: transaction ended, finalizing" );
        
        mCurrentTransaction.finalizeTransaction();

        if (mCurrentTransaction.getParent() != null) {
            mCurrentTransaction = mCurrentTransaction.getParent();
        }
        else {
            mChangeList.add(mCurrentTransaction.getTransactionChanges());
            mChangeList.minimize();
            mConverter.reset();
            
            mCurrentTransaction = null;
            mNewResources.clear();

        }
        LOGGER.fine("ChangeLog: transaction finalized, current queue:" + mChangeList);

        // we only want to do the real-time synch when top-level transactions are done
        if ((mCurrentTransaction == null) && !mChangeList.isEmpty()) maybeSynchReasoner();
    }

    /**
     * Clear the list of changes
     */
    public synchronized void clear() {
        mChangeList.clear();
    }
    
    public void dispose() {
    	unregisterDeletionHooks();
    }

    /**
     * Get all the changes
     * @return a ChangeList of all the changes made to the associated KB
     */
    public synchronized ChangeList getChanges() {
        ChangeList aList = new ChangeList();

        aList.add(mChangeList);

        return aList;
    }

    /**
     * Get the ChangeLog as List of OWLOntologyChange objects
     * @return a List of OWLOntologyChanges which corresponds to all the changes found in the ChangeList
     */
    public synchronized List<OWLOntologyChange> asOWLOntologyChangeList() {
    	
        List<OWLOntologyChange> aChanges = new ArrayList<OWLOntologyChange>(mChangeList.getChanges());

        return aChanges;
    }
}
