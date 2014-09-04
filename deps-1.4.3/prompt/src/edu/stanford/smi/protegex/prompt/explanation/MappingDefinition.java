 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.explanation;

public class MappingDefinition extends Explanation {
  final  static int MAPPING_DEFINITION_ARITY = 0;

  public MappingDefinition () {
	super (MAPPING_DEFINITION_ARITY);
	_name = "mapping definition";
	_prettyName = "corresponding mapping definition";
  }

  public String toString () {
	return "There is a corresponding mapping definition";
  }
}
