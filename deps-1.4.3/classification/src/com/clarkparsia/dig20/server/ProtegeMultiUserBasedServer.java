package com.clarkparsia.dig20.server;

import java.util.Map;
import java.util.logging.Level;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.RemoteProjectManager;

/**
 * <p>
 * Title: ProtegeMultiUserBasedServer
 * </p>
 * <p>
 * Description: Server implementation that loads data from Protege-OWL projects
 * via a Protege MultiUser Server
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
public class ProtegeMultiUserBasedServer extends AbstractProtegeBasedServer {

	public static final String	PROTEGE_PASSWORD_PARAMETER;
	public static final String	PROTEGE_PROJECT_PARAMETER;
	public static final String	PROTEGE_SERVER_HOST_PARAMETER;
	public static final String	PROTEGE_USER_PARAMETER;

	public static final String	DEFAULT_PROTEGE_USER;
	public static final String	DEFAULT_PROTEGE_PASSWORD;
	public static final String	DEFAULT_PROTEGE_SERVER_HOST;

	public ProtegeMultiUserBasedServer() {
		super();
		host = DEFAULT_PROTEGE_SERVER_HOST;
		project = null;
		user = DEFAULT_PROTEGE_USER;
		password = DEFAULT_PROTEGE_PASSWORD;
	}

	public ProtegeMultiUserBasedServer(String host, String project, String user, String password) {
		this.host = host;
		this.project = project;
		this.user = user;
		this.password = password;
	}

	private String								host;
	private String								project;
	private String								user;
	private String								password;
	private static final RemoteProjectManager	rpm;

	static {
		rpm = RemoteProjectManager.getInstance();

		PROTEGE_PASSWORD_PARAMETER = "password";
		PROTEGE_PROJECT_PARAMETER = "project";
		PROTEGE_SERVER_HOST_PARAMETER = "host";
		PROTEGE_USER_PARAMETER = "user";

		DEFAULT_PROTEGE_USER = "Guest Administrator";
		DEFAULT_PROTEGE_PASSWORD = "guest";
		DEFAULT_PROTEGE_SERVER_HOST = "localhost";
	}

	@Override
	protected Project getProject() {
		if( project == null )
			return null;

		if( log.isLoggable( Level.FINE ) ) {
			log.fine( String.format(
					"Attempting to get remote project using parameters ('%s','%s','%s','%s')",
					host, user, password, project ) );
		}
		Project p = rpm.getProject( host, user, password, project, false );
		return p;
	}

	@Override
	protected void load(Map<String, String> parameters) {
		String s = parameters.get( PROTEGE_SERVER_HOST_PARAMETER );
		host = (s == null)
			? DEFAULT_PROTEGE_SERVER_HOST
			: s;

		s = parameters.get( PROTEGE_PASSWORD_PARAMETER );
		password = (s == null)
			? DEFAULT_PROTEGE_PASSWORD
			: s;

		s = parameters.get( PROTEGE_USER_PARAMETER );
		user = (s == null)
			? DEFAULT_PROTEGE_USER
			: s;

		project = parameters.get( PROTEGE_PROJECT_PARAMETER );

		reload();
	}
}
