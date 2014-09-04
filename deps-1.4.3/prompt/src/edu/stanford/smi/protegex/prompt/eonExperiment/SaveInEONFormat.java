/*
 * Author(s): Natasha Noy (noy@smi.stanford.edu)
  * 
*/
package edu.stanford.smi.protegex.prompt.eonExperiment;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLAnonymousClass;
import edu.stanford.smi.protegex.prompt.PromptTab;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.TableRow;
import edu.stanford.smi.protegex.prompt.util.Util;

public class SaveInEONFormat {
	static private int _matchedProperties = 0;
	static private int _matchedClasses = 0;
	static private int _unmatchedProperties = 0;
	static private int _unmatchedClasses = 0;
	

	public static void saveInEONFormat (String fileName, String oldNS, String newNS) {
		try {
		  URI oldNSURL = new URI (oldNS);
	      URI newNSURL = new URI (newNS);
		  PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(fileName)));
		  
		  printPreamble (out);
		  printAlignmentObject (out, oldNS, newNS);
		  
		  Collection results = PromptTab.getPromptDiff().getResultsTable().sort();
		  Iterator i = results.iterator();

		  while (i.hasNext()) {
			TableRow nextRow = (TableRow)i.next();
			saveRowInEONFormat (nextRow, out, oldNS, newNS);
		  }
			closeFile(out);
			printStatistics ();
		} catch (Exception e) {
		  e.printStackTrace();
		}

		
	}
	
	private static void printStatistics () {
		Log.getLogger().info ("Matched classes = " + _matchedClasses);
		Log.getLogger().info ("Matched properties = " + _matchedProperties);
		Log.getLogger().info ("Unmatched classes = " + _unmatchedClasses);
		Log.getLogger().info ("Unmatched properties = " + _unmatchedProperties);
	}
	
	private static void printAlignmentObject(PrintStream out, String oldNS, String newNS) {
		out.println("<Alignment>");
		out.println("<xml>yes</xml>");
		out.println("<level>0</level>");
		out.println("<type>11</type>");
		out.println("<onto1>" + oldNS + "</onto1>");
		out.println("<onto2>" + newNS + "</onto2>");
		out.println("<uri1>" + oldNS + "</uri1>");
		out.println("<uri2>" + newNS + "</uri2>");
		out.println ("<map>");
	}

	private static void printPreamble (PrintStream out) {
		out.println("<?xml version='1.0' encoding='UTF-8'?>");
		out.println("<!DOCTYPE rdf:RDF [");
		out.println("\t <!ENTITY rdf 'http://www.w3.org/1999/02/22-rdf-syntax-ns#'>");
		out.println("\t <!ENTITY alignment 'http://knowledgeweb.semanticweb.org/heterogeneity/alignment#'>");
		out.println("\t <!ENTITY rdfs 'http://www.w3.org/TR/1999/PR-rdf-schema-19990303#'>");
		out.println("]>");
		out.println("<rdf:RDF xmlns:rdf=\"&rdf;\"");
		out.println("\t xmlns:rdfs=\"&rdfs;\"");
		out.println("\t xmlns=\"&alignment;\">");
		    
	}
	
	private static void closeFile (PrintStream out) {
		out.println("</map>");
		out.println("</Alignment>");
		out.println("</rdf:RDF>");
		out.flush();		
	}

	private static void saveRowInEONFormat(TableRow nextRow, PrintStream out, String oldNS, String newNS) {
		Frame f1 = nextRow.getF1Value();
		Frame f2 = nextRow.getF2Value();
		if (f1 != null && f2 == null && f1 instanceof Cls && ! (f1 instanceof OWLAnonymousClass) && f1.isVisible())
			_unmatchedClasses++;
		if (f1 != null && f2 == null && f1 instanceof Slot && f1.isVisible())
			_unmatchedProperties++;
		if (f1 == null || f2 == null) return;
		if (Util.isSystem(f1)) return;
		String name1 = f1.getBrowserText();
		String name2 = f2.getBrowserText();
		if (name1.indexOf(':') >0 && name2.indexOf(':') > 0) return;
		if (f1 instanceof Cls || f1 instanceof Slot) {
			if (f1 instanceof OWLAnonymousClass) return;
			out.println ("<Cell>");		

			out.println ("<entity1 rdf:resource = '" + oldNS + "#" + f1.getBrowserText() + "'/>");
			out.println ("<entity2 rdf:resource = '" + newNS + "#" + f2.getBrowserText() + "'/>");
		
			out.println ("<measure>1.0</measure>");
			out.println ("<relation>=</relation>");
			out.println ("</Cell>");		
			if (f1 instanceof Cls) 
				_matchedClasses++;
			else
				_matchedProperties++;
		}
		
	}

}
