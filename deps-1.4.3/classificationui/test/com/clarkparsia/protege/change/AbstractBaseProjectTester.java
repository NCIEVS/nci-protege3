package com.clarkparsia.protege.change;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.clarkparsia.protege.reasoner.CustomReasonerProjectPlugin;

import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protegex.owl.database.creator.OwlDatabaseCreator;
import edu.stanford.smi.protegex.owl.jena.creator.NewOwlProjectCreator;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.factory.AbstractOwlProjectCreator;

/**
 * <p>
 * Title: AbstractBaseProjectTester
 * </p>
 * <p>
 * Description: Base class for parameterized JUnit 4 tests that are to be run on
 * both file and database projects
 * </p>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 * 
 * @author Mike Smith
 */
@RunWith(Parameterized.class)
public abstract class AbstractBaseProjectTester {

	private final static String DB_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	private final static String DB_PASS = "";
	private final static String DB_TABLE = "TEST_TABLE";
	private final static String DB_USER = "";

	@Parameters
	public static Collection<Object[]> getCases() {
		Collection<Object[]> cases = new ArrayList<Object[]>();
		cases.add(new Object[] { false });
		cases.add(new Object[] { true });
		return cases;
	}

	protected String baseURI;
	private String dbUrl;
	protected OWLModel owlModel;
	private CustomReasonerProjectPlugin plugin;
	private Project project;

	private boolean useDatabase;

	public AbstractBaseProjectTester(String dbName, boolean useDatabase) {
		this.useDatabase = useDatabase;
		this.dbUrl = "jdbc:derby:" + dbName + ";create=true";
	}

	@After
	public void cleanupProject() {
		plugin.beforeClose(project);
		project.dispose();
		project = null;
		owlModel = null;
		plugin.dispose();
		plugin = null;
		if (useDatabase) {
			// Shut down derby
			try {
				DriverManager.getConnection("jdbc:derby:;shutdown=true");
			}
			catch (SQLException e) {
				if (!e.getSQLState().equals("XJ015"))
					System.err.println("Unexpected exception shutting down derby: "
							+ e.getSQLState() + ":" + e.getLocalizedMessage());
			}
		}
	}

	private static Project createProject(AbstractOwlProjectCreator creator) {
		Collection<Object> errors = new ArrayList<Object>();

		try {
			creator.create(errors);
		}
		catch (OntologyLoadException e) {
			throw new RuntimeException(e);
		}

		if (!errors.isEmpty()) throw new RuntimeException("Errors: " + errors);

		return creator.getProject();
	}

	private Project createDatabaseProject() {

		OwlDatabaseCreator creator = new OwlDatabaseCreator(/* wipe = */true);
		creator.setDriver(DB_DRIVER);
		creator.setURL(dbUrl);
		creator.setTable(DB_TABLE);
		creator.setUsername(DB_USER);
		creator.setPassword(DB_PASS);

		return createProject(creator);
	}

	private Project createJenaProject() {
		return createProject(new NewOwlProjectCreator());
	}
	
	@Before
	public void initializeProject() {
		plugin = new CustomReasonerProjectPlugin();

		if (useDatabase) {
			// Start up derby
			try {
				Class.forName(DB_DRIVER).newInstance();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}

			project = createDatabaseProject();
		}
		else {
			project = createJenaProject();
		}

		owlModel = (OWLModel) project.getKnowledgeBase();

		plugin.afterLoad(project);

		baseURI = owlModel.getDefaultOWLOntology().getURI() + "#";
	}
}
