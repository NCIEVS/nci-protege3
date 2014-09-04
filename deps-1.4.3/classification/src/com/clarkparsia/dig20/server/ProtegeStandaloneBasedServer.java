package com.clarkparsia.dig20.server;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.MessageError;
import edu.stanford.smi.protege.util.URIUtilities;

/**
 * <p>
 * Title: ProtegeStandaloneBasedServer
 * </p>
 * <p>
 * Description: Server implementation that loads data from Protege-OWL projects
 * </p>
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 * 
 * @author Mike Smith
 */
public class ProtegeStandaloneBasedServer extends AbstractProtegeBasedServer {

	URI	projectFileURI;

	public ProtegeStandaloneBasedServer() {
		this.projectFileURI = null;
	}

	public ProtegeStandaloneBasedServer(String projectFile) {
		super();
		if( projectFile == null )
			throw new NullPointerException();

		this.projectFileURI = URIUtilities.createURI( projectFile );
	}

	@Override
	protected void load(Map<String, String> parameters) {
		String projectFile = parameters.get( "project-file" );
		if( projectFile != null )
			projectFileURI = URIUtilities.createURI( projectFile );

		reload();
	}

	@Override
	protected Project getProject() {
		List<Object> errors = new ArrayList<Object>();
		Project p = Project.loadProjectFromURI( projectFileURI, errors );
		if( !errors.isEmpty() ) {
			StringBuilder msg = new StringBuilder( "Errors loading project:\n" );
			for (Object error : errors) {
				if(error instanceof MessageError) {
					msg.append( ((MessageError) error).getMessage() + "\n" );
					log.log(Level.SEVERE, ((MessageError) error).getMessage(), ((MessageError) error).getException());
				}
				else {
					msg.append( error );
				}
			}
			log.severe( msg.toString() );
			throw new RuntimeException( msg.toString() );
		}
		return p;
	}
}
