/**
 * 
 */
package gov.nih.nci.protegex.util;

import java.util.*;

/**
 * @author Bob Dionne
 *
 */
public abstract class EditRecorder {
	
	
	private Stack<Object> pastStates = new Stack<Object>();
	
	private Stack<Object> futureStates = new Stack<Object>();	
	
	protected Object currentState = null;
	
	abstract Object clone(Object obj);
	
	public Object backUp() {
		
		futureStates.push(currentState);
		currentState = pastStates.pop();
		return currentState;
		
	}
	
	public Object goForward() {
		
		pastStates.push(currentState);
		currentState = futureStates.pop();
		return currentState;
		
	}
	
	protected void prepare() {
		pastStates.push(clone(currentState));
	}
	
	
}
