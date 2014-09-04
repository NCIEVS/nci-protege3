/*
 * Author(s): Natasha Noy (noy@smi.stanford.edu)
  * 
*/
package edu.stanford.smi.protegex.prompt.explanation;

public class AnchorPromptResultExplanation extends Explanation {
	final  static int ANCHOR_PROMPT_RESULT_ARITY = 0;

	public AnchorPromptResultExplanation() {
		super(ANCHOR_PROMPT_RESULT_ARITY);
		_name = "anchor-prompt";
		_prettyName = "AnchorPrompt result";
	}

	public String toString () {
		return "AnchorPrompt result";
	}

}
