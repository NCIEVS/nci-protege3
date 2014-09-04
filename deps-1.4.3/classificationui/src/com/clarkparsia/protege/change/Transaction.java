package com.clarkparsia.protege.change;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLAxiomChange;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;

import edu.stanford.smi.protege.util.Log;

/**
 * Title: <br>
 * Description: A list of changes (adds and removes) that occur between transaction start/end events from protege.  Can
 * have a parent transaction, the parent's list of changes will be a sum of all the changes of its children.<br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Sep 18, 2007 9:11:43 AM
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class Transaction {
	
    private final Logger LOGGER		    = Log.getLogger(ChangeLog.class);
	private OWLDataFactory	mFactory	= OWLManager.createOWLOntologyManager().getOWLDataFactory();
	private Transaction		mParent;
	private ChangeList		mChangeList;

	private OWLEquivalentClassesAxiom equivalent(
			Collection<? extends OWLDescription> theDescriptions) {
		Set<OWLDescription> aSet = new CopyOnWriteArraySet<OWLDescription>(theDescriptions);
		return mFactory.getOWLEquivalentClassesAxiom(aSet);
	}
	
    public Transaction() {
        this(null);
    }

    public Transaction(Transaction theParent) {
        mParent = theParent;

        mChangeList = new ChangeList();
    }
    
    public void startTransaction() {
        mChangeList.clear();
    }

    public void finalizeTransaction() {

    	// Minimize Changelist
    	mChangeList.minimize();
    	
    	// Equivalent class related cleanups
    	{
			Set<OWLAxiom> aSeenEqAdds = new HashSet<OWLAxiom>();
			Set<OWLAxiom> aSeenEqRemoves = new HashSet<OWLAxiom>();

			/*
			 * 1.) Equivalent class axioms are added in both directions A = B , B =
			 * A . This cleanup eliminates the second.
			 */
			{
				ChangeList aNewChangeList = new ChangeList();

				for (OWLAxiomChange aChange : mChangeList.getChanges()) {
					final OWLAxiom aAxiom = aChange.getAxiom();
					final boolean aAdd = ChangeList.isAddition(aChange);

					if (aAxiom instanceof OWLEquivalentClassesAxiom) {
						if (aAdd) {
							if (aSeenEqAdds.add(aAxiom)) aNewChangeList.axiomAdded(aAxiom);
						}
						else {
							if (aSeenEqRemoves.add(aAxiom)) aNewChangeList.axiomRemoved(aAxiom);
						}
					}
					else {
						if (aAdd)
							aNewChangeList.axiomAdded(aAxiom);
						else aNewChangeList.axiomRemoved(aAxiom);
					}
				}
				
				mChangeList = aNewChangeList;
			}
			
			/*
			 * 2.) If an equivalent class axiom is added A = B, the related subclass axioms are not needed A [= B, B [ = A
			 */
			{
				ChangeList aNewChangeList = new ChangeList();

				for (OWLAxiomChange aChange : mChangeList.getChanges()) {
					final OWLAxiom aAxiom = aChange.getAxiom();
					final boolean aAdd = ChangeList.isAddition(aChange);

					if (aAxiom instanceof OWLSubClassAxiom) {
						final OWLSubClassAxiom aSub = (OWLSubClassAxiom)aAxiom;
						if (aAdd) {
							if (!aSeenEqAdds.contains(equivalent(aSub.getDescriptions()))) aNewChangeList.axiomAdded(aAxiom);
						}
						else {
							if (!aSeenEqRemoves.contains(equivalent(aSub.getDescriptions()))) aNewChangeList.axiomRemoved(aAxiom);
						}
					}
					else {
						if (aAdd)
							aNewChangeList.axiomAdded(aAxiom);
						else aNewChangeList.axiomRemoved(aAxiom);
					}
				}
				
				mChangeList = aNewChangeList;				
			}
		}
    	
        if (mParent != null) {
            mParent.mChangeList.add(mChangeList);
        }
    }
    
    public Transaction getParent() {
        return mParent;
    }

    public ChangeList getTransactionChanges() {
        return mChangeList;
    }

    public void axiomAdded(OWLAxiom theAxiom) {
        mChangeList.axiomAdded(theAxiom);
    }

    public void axiomRemoved(OWLAxiom theAxiom) {
        mChangeList.axiomRemoved(theAxiom);
    }
}
