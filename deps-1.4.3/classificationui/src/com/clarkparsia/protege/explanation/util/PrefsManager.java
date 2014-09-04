package com.clarkparsia.protege.explanation.util;

import java.util.prefs.Preferences;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Title: PrefsManager<br>
 * Description: Preferences for the ExplanationTab<br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Apr 23, 2007 8:27:37 AM
 *
 * @author Michael Grove <mhgrove@hotmail.com>
 */
public class PrefsManager {
    public enum PrefsKey {
        PREFS_RENDER_SYNTAX,

        PREFS_CONCEPT_WRAPPING,
        
        PREFS_IRRELEVANT_PARTS;

        public String key() {
        	return toString();
        }
    }

    public enum PrefsValue {
        PREFS_ON,
        PREFS_OFF,
        
        PREFS_MANCHESTER_SYNTAX,
        PREFS_ABSTRACT_SYNTAX,
        
        PREFS_CONCEPT_NO_WRAP,
        PREFS_CONCEPT_WRAP,
        PREFS_CONCEPT_SMART_WRAP,
        
        PREFS_SHOW_IRRELEVANT_PARTS,
        PREFS_COLOR_IRRELEVANT_PARTS,
        PREFS_HIDE_IRRELEVANT_PARTS;

        public String value() {
        	return toString();
        }
    }

    private Preferences mPrefs;

    public PrefsManager(String theVersion) {
        mPrefs = Preferences.userNodeForPackage(PrefsManager.class).node(theVersion);
    }

    private Preferences getPrefs() {
        return mPrefs;
    }

    public String get(PrefsKey theKey) {
        String aValue = getPrefs().get(theKey.key(), "");

        if (aValue.equals(""))
            return null;
        else return aValue;
    }

    public URL getURL(PrefsKey theKey) throws MalformedURLException {
        String aValue = get(theKey);

        if (aValue == null)
            return null;
        else return new URL(aValue);
    }

    public void set(PrefsKey theKey, String theValue) {
        getPrefs().put(theKey.key(), theValue);
    }

    public void set(PrefsKey theKey, PrefsValue theValue) {
        getPrefs().put(theKey.key(), theValue.value());
    }

    public void setConceptWrapping(PrefsValue theValue) {
        if (!theValue.equals(PrefsValue.PREFS_CONCEPT_NO_WRAP) 
        	&& !theValue.equals(PrefsValue.PREFS_CONCEPT_WRAP)
        	&& !theValue.equals(PrefsValue.PREFS_CONCEPT_SMART_WRAP)) {
            throw new IllegalArgumentException();
        }

        set(PrefsKey.PREFS_CONCEPT_WRAPPING, theValue);
    }
    
    
}
