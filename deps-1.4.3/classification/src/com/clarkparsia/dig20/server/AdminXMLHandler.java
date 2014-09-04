package com.clarkparsia.dig20.server;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.clarkparsia.dig20.server.xml.AdminVocabulary;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Aug 1, 2007 7:58:53 AM
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class AdminXMLHandler extends DefaultHandler {
    private enum AdminCommand {
        SHUTDOWN, RELOAD, LOAD, INFO, PERSIST
    }

    private Locator mLocator = null;

    private boolean mSeenTopLevelElem = false;
    private AdminCommand mAdminCommand = null;

    private Map<String, String> mArgs = new HashMap<String, String>();

    public void setDocumentLocator(Locator locator) {
        mLocator = locator;
    }

    public void startElement(String theURI, String theLocalName, String theQName, Attributes theAttributes)
            throws SAXException {

        super.startElement( theURI, theLocalName, theQName, theAttributes );

        String aFullURI = theURI + theLocalName;

        if (!mSeenTopLevelElem && !aFullURI.equals(AdminVocabulary.ADMIN.getURI().toString())) {
            throw new IllegalArgumentException("Improperly formatted admin XML request, <admin> MUST be the top level element!");
        }
        else if (!mSeenTopLevelElem && aFullURI.equals(AdminVocabulary.ADMIN.getURI().toString())) {
            // yay, admin was the first tag we saw, now lets continue on so we can see
            mSeenTopLevelElem = true;
        }
        else if (mSeenTopLevelElem) {
            if (mAdminCommand != null) {
                throw new IllegalArgumentException("Cannot have more than one admin command per request!");
            }

            if (aFullURI.equals(AdminVocabulary.SHUTDOWN.getURI().toString())) {
                mAdminCommand = AdminCommand.SHUTDOWN;
            }
            else if (aFullURI.equals(AdminVocabulary.RELOAD.getURI().toString())) {
                mAdminCommand = AdminCommand.RELOAD;
            }
            else if (aFullURI.equals(AdminVocabulary.LOAD.getURI().toString())) {
                mAdminCommand = AdminCommand.LOAD;

                mArgs.clear();
                // TODO: maybe some checks to see if they are using valid arguments?
                for (int i = 0; i < theAttributes.getLength(); i++) {
                    mArgs.put(theAttributes.getLocalName(i), theAttributes.getValue(i));
                }
            } else if (aFullURI.equals(AdminVocabulary.INFO.getURI().toString())) {
            	mAdminCommand = AdminCommand.INFO;
            }
            else throw new SAXParseException( "Unhandled element: " + theURI + theLocalName, mLocator );
        }
    }

    public Map<String, String> getCommandArguments() {
        return mArgs;
    }

    public boolean isShutdownCommand() {
        return AdminCommand.SHUTDOWN.equals(mAdminCommand);
    }

    public boolean isReloadCommand() {
        return AdminCommand.RELOAD.equals(mAdminCommand);
    }

    public boolean isLoadCommand() {
        return AdminCommand.LOAD.equals(mAdminCommand);
    }
    
    public boolean isInfoCommand() {
    	return AdminCommand.INFO.equals(mAdminCommand);
    }
    
    public boolean isPersistCommand() {
    	return AdminCommand.PERSIST.equals(mAdminCommand);
    }
}
