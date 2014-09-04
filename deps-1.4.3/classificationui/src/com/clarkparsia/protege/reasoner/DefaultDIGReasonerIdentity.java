package com.clarkparsia.protege.reasoner;

import edu.stanford.smi.protegex.owl.inference.dig.reasoner.DIGReasonerIdentity;

/**
 * Title: Extends the default DIGReasonerIdentity class to provide information about the reasoner such as version
 * and a brief description.<br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Sep 10, 2007 11:56:16 AM
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class DefaultDIGReasonerIdentity extends DIGReasonerIdentity {
    @Override
    public String getName() {
        return "Pellet";
    }

    @Override
    public String getVersion() {
        return "1.5.0";
    }

    @Override
    public String getMessage() {
        return "Pellet over DIG";
    }
}
