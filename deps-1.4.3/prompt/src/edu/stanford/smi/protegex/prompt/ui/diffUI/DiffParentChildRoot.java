 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui.diffUI;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;


public class DiffParentChildRoot extends LazyTreeRoot  {
	private DiffViewSetUp _viewSetup;
	
    public DiffParentChildRoot(Cls root, DiffViewSetUp vs) {
        super(root);
		_viewSetup = vs;
    }

    public LazyTreeNode createNode(Object o) {
        return new DiffParentChildNode(this, (Cls) o, _viewSetup);
    }
    public Comparator getComparator() {
        return new LazyTreeNodeFrameComparator();
    }

}
