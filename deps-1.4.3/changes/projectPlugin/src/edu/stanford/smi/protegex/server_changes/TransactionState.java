package edu.stanford.smi.protegex.server_changes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.change.api.Composite_Change;
import edu.stanford.bmir.protegex.chao.change.api.Deleted_Change;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Transaction;
import edu.stanford.smi.protegex.owl.model.RDFResource;


public class TransactionState {
	private PostProcessorManager changesDb;
	private Stack<List<Change>> changeStack = new Stack<List<Change>>();
	private Stack<String> transactionNameStack = new Stack<String>();


	public TransactionState(PostProcessorManager changesDb) {
		this.changesDb = changesDb;
	}

	public int getTransactionDepth() {
		return changeStack.size();
	}

	public boolean inTransaction() {
		return changeStack.size() != 0;
	}

	public void addToTransaction(Change change) {
		changeStack.peek().add(change);
	}

	public void beginTransaction(String name) {
		changeStack.push(new ArrayList<Change>());
		transactionNameStack.push(name);
	}

	public void commitTransaction() {
		List<Change> changes = changeStack.pop();
		String name = transactionNameStack.pop();
		createTransactionChange(changes, name);
	}

	public void rollbackTransaction() {
		changeStack.pop();
		transactionNameStack.pop();
	}


	public void createTransactionChange(List<Change> changes, String context) {
		if (changes.size() == 0) {
			return;
		}

		Ontology_Component applyTo = getApplyToFromContext(context);

		if (applyTo == null) {
			Representative r;
			try {
				r = guessRepresentative(changes, context);
			} catch (Exception e) {
				e.printStackTrace();
				r = null;
			}
			if (r != null) {
				//if (r.getAction() != null) action = r.getAction();
				if (r.getApplyTo() != null && applyTo == null) {
					applyTo = r.getApplyTo();
				}
			}
		}
		ServerChangesUtil.createTransactionChange(changesDb, applyTo, context, changes);
	}

	public Ontology_Component getApplyToFromContext(String context) {
		if (context == null) {
			return null;
		}
		String frame_name = Transaction.getApplyTo(context);
		if (frame_name == null) {
			return null;
		}
		Frame frame = changesDb.getKb().getFrame(frame_name);
		if (frame == null) {
			return null;
		}
		return changesDb.getOntologyComponent(frame, true);
	}


	private Representative guessRepresentative(List<Change> actions, String name) {
		boolean owl = changesDb.isOwl();
		Ontology_Component first = null;
		Ontology_Component named = null;
		for (Change change : actions) {
			if (first == null) {
				first = change.getApplyTo();
				if (!owl) {
					break;
				}
			}
			if (owl && named == null) {
				Ontology_Component current = change.getApplyTo();
				if (current.isAnonymous()) {
					named = current;
					break;
				}
			}
		}
		Representative r = new Representative(name, named != null ? named : first);
		return r;
	}
	/*
	{
        boolean isOwl = changesDb.isOwl();
        Representative r = null;
		if (isOwl) {
            if ((r = guessFirstNonAnonymousAction(actions, name)) != null) return r;
            if ((r = guessFirstOrDelete(actions, name)) != null) return r;
        }
        else {
            if ((r = guessFirstOrDelete(actions, name)) != null) return r;
        }
        return null;
	}
	 */

	private Representative guessFirstNonAnonymousAction(List<Change> actions, String name) {
		for (Change change : actions) {
			if (!((RDFResource)((AbstractWrappedInstance)change.getApplyTo()).getWrappedProtegeInstance()).isAnonymous()) {
				return new Representative(change.getAction(),
						change.getApplyTo());
			}
		}
		return null;
	}


	private Representative guessFirstOrDelete(Collection<Change> transActions, String name) {
		boolean firstInfoInst = false;
		boolean firstDeleted = false;
		Change firstInst = null;

		for (Change change : transActions) {
			if (!(change instanceof Composite_Change) && !firstInfoInst) {
				firstInst = change;
				firstInfoInst = true;
			}

			if (change instanceof Deleted_Change && !firstDeleted) {
				firstInst = change;
				firstDeleted = true;
			}
		}
		return new Representative(firstInst.getAction(),
				firstInst.getApplyTo());
	}

	private Frame findNamedClass(RDFResource resource) {
		return resource;
	}



	/*
	 * This class provides values that the user wants to see.  If we use a poor heuristic for finding these
	 * values then it is hard for the user to understand what the change tab is showing.
	 */
	class Representative {
		private String action;
		private Ontology_Component applyTo;

		public String getAction() {
			return action;
		}

		public Ontology_Component getApplyTo() {
			return applyTo;
		}

		public Representative(String action, Ontology_Component applyTo) {
			this.action = action;
			this.applyTo = applyTo;
		}
	}
}
