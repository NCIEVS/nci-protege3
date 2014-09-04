package com.clarkparsia.dig20.client.admin;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.clarkparsia.dig20.server.xml.AdminVocabulary;

public class AdminResponseXMLHandler extends DefaultHandler {
	private Locator mLocator = null;

    private boolean mSeenTopLevelElem = false;
    private boolean mSeenInfoValues = false;
    private boolean mWithinPropertyElem = false;
    
    private StringBuffer mRecentlyReadCharacters;
    
    private String mPropertyName;
    private String mPropertyValue;
    
    private Properties mProperties;
    
    private String documentTag = "AdminResponse";

    public void setDocumentLocator(Locator locator) {
        mLocator = locator;
    }

    @Override
    public void startElement(String theURI, String theLocalName, String theQName, Attributes theAttributes)
            throws SAXException {

        super.startElement( theURI, theLocalName, theQName, theAttributes );
        resetRecentlyReadCharacters();

        String aFullURI = theURI + theLocalName;

        if (!mSeenTopLevelElem && !aFullURI.equals(AdminVocabulary.ADMIN_NS + documentTag)) {
            throw new IllegalArgumentException("Improperly formatted admin XML response, <" + documentTag + "> MUST be the top level element!");
        }
        else if (!mSeenTopLevelElem && aFullURI.equals(AdminVocabulary.ADMIN_NS + documentTag)) {
            // yay, admin response was the first tag we saw, now lets continue on so we can see
            mSeenTopLevelElem = true;
        }
        else if (mSeenTopLevelElem) {
        	if (!mSeenInfoValues && aFullURI.equals(AdminVocabulary.INFO_VALUES.getURI().toString())) {
        		mSeenInfoValues = true;
        		mProperties = new Properties();
        	} else if (mSeenInfoValues) {
        		if (!mWithinPropertyElem && aFullURI.equals(AdminVocabulary.PROPERTY.getURI().toString())) {
        			mWithinPropertyElem = true;
        			mPropertyName = null;
        			mPropertyValue = null;
        		} else if (mWithinPropertyElem) {
        			if (!(aFullURI.equals(AdminVocabulary.PROPERTY_NAME.getURI().toString()) || aFullURI.equals(AdminVocabulary.PROPERTY_VALUE.getURI().toString()))) {
        				throw new SAXParseException( "Unhandled element: " + theURI + theLocalName, mLocator );
        			}        				
        		} else throw new SAXParseException( "Unhandled element: " + theURI + theLocalName, mLocator );
        	} else throw new SAXParseException( "Unhandled element: " + theURI + theLocalName, mLocator );
        } else throw new SAXParseException( "Unhandled element: " + theURI + theLocalName, mLocator );               
    }

    @Override
    public void endElement(String theURI, String theLocalName, String theQName) throws SAXException {
        super.endElement( theURI, theLocalName, theQName );
    	
        String aFullURI = theURI + theLocalName;
        
    	if (mWithinPropertyElem) {
    		if (aFullURI.equals(AdminVocabulary.PROPERTY_NAME.getURI().toString())) {
    			mPropertyName = getRecentlyReadCharacters();
    		} else if (aFullURI.equals(AdminVocabulary.PROPERTY_VALUE.getURI().toString())) {
    			mPropertyValue = getRecentlyReadCharacters();
    		} else if (aFullURI.equals(AdminVocabulary.PROPERTY.getURI().toString())) {
    			mWithinPropertyElem = false;
    			
    			if (mPropertyName == null) {
    				throw new IllegalArgumentException("Improperly formatted admin XML response: there is a <" 
    						+ AdminVocabulary.PROPERTY.getShortName() + "> without <" 
    						+ AdminVocabulary.PROPERTY_NAME.getShortName() + ">");
    			}
    			
    			if (mPropertyValue == null) {
    				throw new IllegalArgumentException("Improperly formatted admin XML response: there is a <" 
    						+ AdminVocabulary.PROPERTY.getShortName() + "> without <" 
    						+ AdminVocabulary.PROPERTY_VALUE.getShortName() + ">");
    			}

    			mProperties.setProperty(mPropertyName, mPropertyValue);
    		}
    	}
    }

    @Override
    public void characters(char[] ch, int start, int length) {
    	if (null == mRecentlyReadCharacters) {
            mRecentlyReadCharacters = new StringBuffer();
        }       

        mRecentlyReadCharacters.append(ch, start, length);
    }
    
    private void resetRecentlyReadCharacters() {
    	mRecentlyReadCharacters = null;
    }
    
    private String getRecentlyReadCharacters() {
    	if (null != mRecentlyReadCharacters) {
    		return mRecentlyReadCharacters.toString();
    	}
    	
    	return "";
    }
    
    public Properties getReadProperties() {
    	return mProperties;
    }
}
