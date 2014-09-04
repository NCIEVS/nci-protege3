/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */
package edu.stanford.smi.protegex.promptx.umls;

import java.net.*;
import java.rmi.*;
import java.util.*;

import edu.stanford.smi.protege.util.*;
import gov.nih.nlm.kss.api.*;
import gov.nih.nlm.kss.models.meta.concept.*;
import gov.nih.nlm.kss.util.*;

public class UMLSKSSConnector implements UMLSSourceWrapper {
	
	private KSSRetrieverV5_0 _kssRetriever = null;
	private static String hostName = "//umlsks.nlm.nih.gov/KSSRetriever";
	private static String LAT = null;
	private static Vector SABS = null;
	private static String DBYEAR = "2006AC";

    public UMLSKSSConnector () {
		initialize();
	}
	
	private void initialize () {
        try {
        		_kssRetriever = (KSSRetrieverV5_0) Naming.lookup(hostName);
        } catch (RemoteException e) {
            System.out.println(e.getMessage());
        } catch (NotBoundException e) {
            System.out.println(e.getMessage());
        } catch (MalformedURLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("Knowledge Source Server Can Not Be Connected!");
        }
		
	}
	
	public String getCUIforTerm(String browserText) {
		ArrayList <String> matches = new ArrayList <String>();
		try {
			char[] result;

			result = _kssRetriever.findCUI(DBYEAR, browserText, SABS, LAT,
					KSSRetriever.ExactMatch);
			ConceptIdVector idVector = new ConceptIdVector(new String(result));
			Iterator i = idVector.iterator();
			while (i.hasNext()) {
				ConceptId nextId = (ConceptId) i.next();
				matches.add(nextId.getCUI());
			}

		} catch (DatabaseException e) {
			System.out.println(e.getMessage());
		} catch (RemoteException e) {
			System.out.println(e.getMessage());
		} catch (XMLException e) {
			System.out.println(e.getMessage());
		}

		if (matches == null || matches.isEmpty()) {
			Log.getLogger().info("No results for " + browserText);
			return null;
		}

		if (matches.size() == 1)
			return (String) CollectionUtilities.getSoleItem(matches);

		Log.getLogger().info(
				"Multiple results for " + browserText
						+ ", returning the first one");
		return (String) CollectionUtilities.getFirstItem(matches);

	}

	public boolean initialized() {
		return (_kssRetriever != null);
	}

}
