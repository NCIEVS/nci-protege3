package com.clarkparsia.protege.change;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLAxiomChange;
import org.semanticweb.owl.model.OWLOntologyChangeVisitor;
import org.semanticweb.owl.model.RemoveAxiom;
import org.semanticweb.owl.model.SetOntologyURI;

import edu.stanford.smi.protege.util.Log;

/**
 * Title: <br>
 * Description: A list of axioms that are marked as either added or removed from
 * a kb. This is used by Transaction and ChangeLog to keep a list of things that
 * have been added or removed.<br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Aug 22, 2007 2:37:35 PM
 * 
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class ChangeList {

	private static class AdditionTester implements OWLOntologyChangeVisitor {

		private boolean mAdd;

		public synchronized boolean isAddition(OWLAxiomChange theChange) {
			theChange.accept(this);
			return mAdd;
		}

		public void visit(AddAxiom change) {
			mAdd = true;
		}

		public void visit(RemoveAxiom change) {
			mAdd = false;
		}

		public void visit(SetOntologyURI change) {
			throw new UnsupportedOperationException();
		}
	};

	private static class Addition extends AddAxiom {

		public Addition(OWLAxiom a) {
			super(null, a);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof AddAxiom) {
				AddAxiom other = (AddAxiom) obj;
				return getAxiom().equals(other.getAxiom()) && other.getOntology() == null;
			}
			return false;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			return prime * getAxiom().hashCode() + Boolean.valueOf(true).hashCode();
		}

		@Override
		public String toString() {
			return "+" + getAxiom().toString();
		}
	}

	private static class Removal extends RemoveAxiom {

		public Removal(OWLAxiom a) {
			super(null, a);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof RemoveAxiom) {
				RemoveAxiom other = (RemoveAxiom) obj;
				return getAxiom().equals(other.getAxiom()) && other.getOntology() == null;
			}
			return false;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			return prime * getAxiom().hashCode() + Boolean.valueOf(false).hashCode();
		}

		@Override
		public String toString() {
			return "-" + getAxiom().toString();
		}
	}

	public static boolean isAddition(OWLAxiomChange theChange) {
		return additionTester.isAddition(theChange);
	}

	public static boolean offset(OWLAxiomChange theFirst, OWLAxiomChange theSecond) {
		if (theFirst.getOntology() == null) {
			if (theSecond.getOntology() != null) return false;
		}
		else {
			if (!theFirst.getOntology().equals(theSecond.getOntology())) return false;
		}

		if (isAddition(theFirst)) {
			if (!isAddition(theSecond)) {
				return theFirst.getAxiom().equals(theSecond.getAxiom());
			}
		}
		else {
			if (isAddition(theSecond)) {
				return theFirst.getAxiom().equals(theSecond.getAxiom());
			}
		}

		return false;
	}

	private static AdditionTester additionTester = new AdditionTester();
	private static Logger LOGGER = Log.getLogger(ChangeLog.class);

	private List<OWLAxiomChange> mChanges;

	public ChangeList() {
		mChanges = new ArrayList<OWLAxiomChange>();
	}

	public void add(ChangeList theList) {
		for (OWLAxiomChange aChange : theList.getChanges()) {
			mChanges.add(aChange);
		}
	}

	public List<OWLAxiomChange> getChanges() {
		return Collections.unmodifiableList(mChanges);
	}

	public void axiomAdded(OWLAxiom... theAxioms) {
		for (OWLAxiom a : theAxioms) {
			LOGGER.fine("Queuing axiom addition: " + a);
			mChanges.add(new Addition(a));
		}
	}

	public void axiomRemoved(OWLAxiom... theAxioms) {
		for (OWLAxiom a : theAxioms) {
			LOGGER.fine("Queuing axiom removal: " + a);
			mChanges.add(new Removal(a));
		}
	}

	public void clear() {
		mChanges.clear();
	}

	@Override
	public int hashCode() {
		// FIXME: problem with OWLAxiomChange objects
		return mChanges.hashCode();
	}
	
	public boolean isEmpty() {
		return mChanges.isEmpty();
	}

	/**
	 * Reduce the number of axioms in the changeset by removing any pairs that
	 * offset
	 */
	public void minimize() {

		for (int i = mChanges.size() - 1; i > 0; i--) {
			final OWLAxiomChange aFirstAxiom = mChanges.get(i);
			for (int j = i - 1; j >= 0; j--) {
				final OWLAxiomChange aSecondAxiom = mChanges.get(j);
				if (offset(aFirstAxiom, aSecondAxiom)) {
					mChanges.remove(i);
					mChanges.remove(j);
					i--;
					break;
				}
			}
		}
	}

	@Override
	public boolean equals(Object theObj) {
		if (theObj instanceof ChangeList) {
			ChangeList other = (ChangeList) theObj;
			// Necessary b/c OWLAxiomChange objects do no override equals
			if (mChanges.size() == other.mChanges.size()) {
				Iterator<OWLAxiomChange> i = mChanges.iterator();
				Iterator<OWLAxiomChange> j = other.mChanges.iterator();
				while( i.hasNext() ) {
					OWLAxiomChange aChangeThis = i.next();
					OWLAxiomChange aChangeOther = j.next();
					if (isAddition(aChangeThis) != isAddition(aChangeOther)) return false;
					if (!aChangeThis.getAxiom().equals(aChangeOther.getAxiom())) return false;
				}
				return true;
			}
			else return false;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return mChanges.toString();
	}

}
