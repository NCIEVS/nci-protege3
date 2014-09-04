package com.clarkparsia.protege.reasoner;

import java.net.URL;
import java.net.MalformedURLException;

import edu.stanford.smi.protegex.owl.inference.dig.reasoner.DIGReasoner;
import edu.stanford.smi.protegex.owl.inference.dig.reasoner.DIGReasonerIdentity;
import edu.stanford.smi.protegex.owl.inference.dig.exception.DIGReasonerException;
import org.w3c.dom.Document;
import com.clarkparsia.dig20.client.DigReasoner;

/**
 * Title: A stub implementation of the DIGReasoner interface providing the minimum functionality required by the
 * ProtegeOWLReasoner interface.<br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Sep 10, 2007 11:56:03 AM
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class DefaultDIGReasoner implements DIGReasoner {
    private DigReasoner mReasoner;

    public DefaultDIGReasoner(DigReasoner theReasoner) {
        mReasoner = theReasoner;
    }

    public void setReasonerURL(String theURL) {
        try {
            mReasoner.setServerURL(new URL(theURL));
        }
        catch (MalformedURLException mue) {
            mue.printStackTrace();
        }
    }

    public String getReasonerURL() {
        return mReasoner.getServerURL().toString();
    }

    public DIGReasonerIdentity getIdentity() throws DIGReasonerException {
        return new DefaultDIGReasonerIdentity();
    }

    public String createKnowledgeBase() throws DIGReasonerException {
        // no-op
        return "" + System.currentTimeMillis();
    }

    public void releaseKnowledgeBase(String string) throws DIGReasonerException {
        // no-op
    }

    public void clearKnowledgeBase(String string) throws DIGReasonerException {
        // no-op
    }

    public Document performRequest(Document document) throws DIGReasonerException {
        // TODO: is this ok?
        return null;
    }
}
