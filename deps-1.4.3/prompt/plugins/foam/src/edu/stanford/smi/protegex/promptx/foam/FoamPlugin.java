package edu.stanford.smi.protegex.promptx.foam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JOptionPane;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLOntology;
import edu.stanford.smi.protegex.prompt.plugin.AlgorithmProgressMonitor;
import edu.stanford.smi.protegex.prompt.plugin.model.ComparisonAlgorithmPlugin;
import edu.stanford.smi.protegex.prompt.plugin.ui.ComparisonAlgorithmPluginConfigurationPanel;
import edu.unika.aifb.foam.input.ExplicitRelation;
import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.main.Align;
import edu.unika.aifb.foam.main.Parameter;

public class FoamPlugin implements ComparisonAlgorithmPlugin {
	private FoamConfigPanel configPanel = new FoamConfigPanel();
	
	private static final String CLASSIFIERFILE = ApplicationProperties.getApplicationDirectory().getAbsolutePath()
			+ "/plugins/edu.stanford.smi.protegex.prompt/plugins/edu.stanford.smi.protegex.promptx.foam/config/tree.obj";
	private static final String RULESFILE = ApplicationProperties.getApplicationDirectory().getAbsolutePath()
			+ "/plugins/edu.stanford.smi.protegex.prompt/plugins/edu.stanford.smi.protegex.promptx.foam/config/rules.obj";
	
	private static final boolean SEMI = Parameter.FULLAUTOMATIC;
	private static final int NUMBERQUESTIONS = 5;
	private static final boolean REMOVEDOUBLES = Parameter.REMOVEDOUBLES;		

	private static final String MANUALMAPPINGSFILE = "";		
	
	public static void main(String[] args) {
		edu.stanford.smi.protegex.prompt.PromptTab.main(args);
	}

	public String getPluginName() {
		return "FOAM Plugin for Prompt";
	}

	public void invokePlugin() {
		//  Auto-generated method stub
	}
	
	private String getURI(KnowledgeBase kb) {
		OWLModel model = (OWLModel)kb;
		Collection c = model.getOWLOntologies();
		for(Iterator iter = c.iterator(); iter.hasNext(); ) {
			OWLOntology o1 = (OWLOntology)iter.next();
			if (o1.getURI().endsWith(".owl")) {
				String uri = o1.getURI().replace("file:", "");
				if (uri.startsWith("//")) {
					uri = uri.substring(1);
		}
				return uri;
			}
		}
		
		return "";
	}
	
	public boolean validateConfigSettings() {
		return configPanel.hasValidConfiguration();
	}
	
	public Collection performAlignment(AlgorithmProgressMonitor progress, KnowledgeBase kb1, KnowledgeBase kb2) {
		String[] ontologyFiles = new String[2];
		
		progress.setProgressText("Starting FOAM algorithm");
		
		if(kb1 instanceof OWLModel && kb2 instanceof OWLModel) {
			ontologyFiles[0] = getURI(kb1);
			ontologyFiles[1] = getURI(kb2);
		} else {
			JOptionPane.showMessageDialog(null, "Sorry, but this plugin only supports OWL ontologies.", "Invalid ontology", JOptionPane.OK_OPTION);
			return null;
		}
		
		try {
			// creating the new alignment method
			Align align = new Align();				
			
			// assigning the ontologies
			MyOntology ontologies = new MyOntology(ontologyFiles);	
			
			// check that the parsing succeeded
			if(!ontologies.ok) {
				JOptionPane.showMessageDialog(null, "An unexpected error occurred while parsing the OWL files.\nEither the file URIs were invalid or the OWL format is unsupported.",
						"Unexpected error", JOptionPane.OK_OPTION);
				return null;
			}
	
			// check that the parsing completed without problems
			if (ontologies.ok == false) {
				return null;
			}
			
			// assigning pre-known alignments
			ExplicitRelation explicit = new ExplicitRelation("", ontologies);	
			
			// get the configuration parameters
			int maxIterations = configPanel.getMaxIterations();
			double maxError = configPanel.getMaxError();
			double cutOff = configPanel.getCutOff();
			int strategy = configPanel.getStrategy();
			boolean internalToo = configPanel.isExternal();
			boolean efficientAgenda = configPanel.isEfficient();
			
			// assigning the parameters
			Parameter parameter = new Parameter(maxIterations,strategy,internalToo,efficientAgenda,CLASSIFIERFILE,RULESFILE,SEMI,maxError,NUMBERQUESTIONS,REMOVEDOUBLES,cutOff,ontologyFiles);	
			parameter.manualmappingsFile = MANUALMAPPINGSFILE;
			
			align.name = "Application";
			align.ontology = ontologies;	
			align.p = parameter;
			align.explicit = explicit;
			
			progress.setProgressText("Computing alignment");
			
			// perform alignment 
			align.align();	
			
			progress.setProgressText("Alignment complete");
	
			ArrayList convertedResults = convertResults(align.cutoff, (OWLModel)kb1, (OWLModel)kb2);
			
			progress.setCompleted(true);
			
			return convertedResults;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "An unexpected error occurred.", "Unexpected error", JOptionPane.OK_OPTION);
			e.printStackTrace();
		}
		
		return null;
	}
	
	private ArrayList convertResults(Vector results, OWLModel kb1, OWLModel kb2) {
		Iterator iter = results.iterator();
		ArrayList promptResults = new ArrayList();
		while (iter.hasNext()) {
			String[] element = (String[]) iter.next();

			Frame f1 = findFrame(kb1, element[0]);
			Frame f2 = findFrame(kb2, element[1]);
			
			if(f1 != null && f2 != null) {
				promptResults.add(new CandidateMapping (f1, f2, new FoamExplanation(element[2])));
			}
		}
		
		return promptResults;
	}
	
	private Frame findFrame(OWLModel kb, String name) {
		String namespace = kb.getNamespaceForURI(name);
		String prefix = kb.getNamespaceManager().getPrefix(namespace);
		if (prefix != null && prefix.length() > 0) {
			prefix += ":";
		} else {
			prefix = "";
		}

		name = prefix + name.replace(namespace, "");

		return kb.getFrame(name);
	}

	public ComparisonAlgorithmPluginConfigurationPanel getConfigurationComponent() {
		return configPanel;
	}
}
