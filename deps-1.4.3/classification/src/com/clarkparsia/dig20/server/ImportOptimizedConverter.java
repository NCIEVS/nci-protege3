// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package com.clarkparsia.dig20.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.mindswap.pellet.utils.DurationFormat;
import org.mindswap.pellet.utils.Timer;
import org.mindswap.pellet.utils.progress.ConsoleProgressMonitor;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

import com.clarkparsia.protege.change.OWLAPIConverter;
import com.clarkparsia.protege.exceptions.ConversionException;

import edu.stanford.smi.protege.model.framestore.MergingNarrowFrameStore;
import edu.stanford.smi.protege.model.framestore.NarrowFrameStore;
import edu.stanford.smi.protege.storage.database.ValueCachingNarrowFrameStore;
import edu.stanford.smi.protegex.owl.inference.util.ReasonerUtil;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 * 
 * @author Evren Sirin
 */
public class ImportOptimizedConverter extends OWLAPIConverter {	
	private OWLOntology ontology;
	private OWLModel protegeModel;
	private boolean loadImportsTogether = false;
		
	public ImportOptimizedConverter(OWLModel owlModel, OWLOntologyManager manager) throws ConversionException {
		super( owlModel, manager );
		
		protegeModel = owlModel;
		ontology = super.createTopOntology();
		
		setConvertAnnotations( false );
	}
	
	public void setLoadImportsSeparately(boolean loadImportsSeparately) {
		loadImportsTogether = loadImportsSeparately;
	}
	
	public boolean isLoadImportsSeparately() {
		return loadImportsTogether;
	}	
	
	@Override
	public OWLOntology convert() throws ConversionException {
		Timer t = new Timer();
		t.start();
		
		if( loadImportsTogether ) {
			super.convert();		
		}
		else {
			MergingNarrowFrameStore mnfs = MergingNarrowFrameStore.get( protegeModel );
			NarrowFrameStore systemStore = mnfs.getSystemFrameStore();
			
			List<NarrowFrameStore> frameStores = new ArrayList<NarrowFrameStore>();
	
			for( NarrowFrameStore nfs : mnfs.getAvailableFrameStores() ) {
				if( !nfs.equals( systemStore ) ) {
					mnfs.removeFrameStore( nfs );
					frameStores.add( nfs );
				}
			}
			
			int count = 0;
			for( NarrowFrameStore nfs : frameStores ) {
				System.err.format( "%nLoad ontology (%d of %d): %s%n", ++count, frameStores.size(), nfs.getName() );
				
				// get rid of the ValueCahingFrameStore to avoid unnecessary caching
				if( nfs instanceof ValueCachingNarrowFrameStore )
					nfs = nfs.getDelegate();
				
				mnfs.addActiveFrameStore( nfs );
				mnfs.setTopFrameStore( nfs.getName() );
				
				super.convert();
				
				mnfs.removeFrameStore( nfs );
				
				// we need to clear the cache in ReasonerUtil because we are changing
				// the contents of the KB behind the scenes but ReasonerUtil would use
				// the cache created at the previous iteration of the loop
				ReasonerUtil.getInstance().dispose();
			}
		}

		// free the memory used far caching values
		ReasonerUtil.getInstance().dispose();
		
		t.stop();
		System.err.format( "%nLoading all ontologies finished in %s%n%n", DurationFormat.SHORT.format( t.getTotal() ) );
		
		return ontology;
	}



	@Override
	protected OWLOntology createTopOntology() throws ConversionException {
		return ontology;		
	}
	
	@Override
	protected void convertClasses(OWLOntology ont) throws ConversionException {
		Collection<?> classes = ReasonerUtil.getInstance().getNamedClses(protegeModel);
		
		ConsoleProgressMonitor monitor = new ConsoleProgressMonitor();
		monitor.setProgressTitle( "Loading classes" );
		monitor.setProgressLength( classes.size() );
		monitor.taskStarted();
		for (Iterator<?> iterator = classes.iterator(); iterator.hasNext();) {
			OWLNamedClass owlClass = (OWLNamedClass) iterator.next();

			super.convertClass(ont, owlClass);
			
			monitor.incrementProgress();
		}	
		monitor.taskFinished();
	}
	
	@Override
	protected void convertProperties(OWLOntology ont) throws ConversionException {
		System.err.print( "Loading properties " );
		Timer t = new Timer();
		t.start();
		super.convertProperties(ont);
		t.stop();
		System.err.println( "finished in " + DurationFormat.SHORT.format( t.getTotal() ) );
	}
				
	@Override
	protected void convertIndividuals(OWLOntology ont) throws ConversionException {
		// TODO add a CLI argument to enable conversion of individuals
		
		// Conversion of individuals disabled by default because we know NCI
		// ontologies do not contain any individuals but some OBO annotations
		// will be recognized as individuals causing problems. For example, in
		// CHEBI ontology more than 1M bogus individual assertions are created
		// due to annotations.
		System.err.println( "Loading individuals disabled" );
	}
}