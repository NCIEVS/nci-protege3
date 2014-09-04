/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */
package edu.stanford.smi.protegex.promptx.umls;

public interface UMLSSourceWrapper {

	public String getCUIforTerm(String browserText);

	public boolean initialized();

}
