package gov.nih.nci.protege.builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import com.hp.hpl.jena.util.FileUtils;

import com.clarkparsia.protege3.storage.database.owl.*;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.KnowledgeBaseFactory;
import edu.stanford.smi.protege.model.KnowledgeBaseFactory2;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.WidgetDescriptor;
import edu.stanford.smi.protege.plugin.ProjectPluginManager;
import edu.stanford.smi.protege.query.LuceneQueryPlugin;
import edu.stanford.smi.protege.query.api.QueryApi;
import edu.stanford.smi.protege.query.api.QueryConfiguration;
import edu.stanford.smi.protege.server.metaproject.GroupOperation;
import edu.stanford.smi.protege.server.metaproject.MetaProject;
import edu.stanford.smi.protege.server.metaproject.MetaProjectConstants;
import edu.stanford.smi.protege.server.metaproject.ProjectInstance;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.PropertyList;
import edu.stanford.smi.protegex.changes.ChangesTab;
import edu.stanford.smi.protegex.owl.database.OWLDatabaseKnowledgeBaseFactory;
import edu.stanford.smi.protegex.owl.jena.JenaKnowledgeBaseFactory;
import edu.stanford.smi.protegex.owl.jena.OWLFilesCreateProjectPlugin;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.ui.cls.LogicClassDefinitionWidgetType;
import edu.stanford.smi.protegex.owl.ui.metadatatab.OWLMetadataTab;
import gov.nih.nci.protegex.edit.NCIEditTab;

public class Main {
	private final static Logger log = Log.getLogger(Main.class);
	
    public final static String ONTOLOGY_PREFIX_PROPERTY="ontology.prefix";
    public final static String DB_HOST_PROPERTY="db.host";
    public final static String DB_DATABASE_PROPERTY="db.database";
    public final static String DB_TABLE_PROPERTY="db.table";
    public final static String DB_USER_PROPERTY="db.user";
    public final static String DB_PASSWORD_PROPERTY="db.password";
    
    
	public final static String DB_URL = "com.mysql.jdbc.Driver";
	
	public final static String META_PROJECT_CLASS = "Project";
	public final static String META_GROUP_OPERATION_CLASS = "GroupOperation";
	public final static String META_NAME_SLOT = "name";
	public final static String META_LOCATION_SLOT = "location";
	public final static String META_ANNOTATING_SLOT = "annotationProject";
	public final static String META_ALLOWED_GROUP_OPERATION_SLOT = "allowedGroupOperation";
	public final static String RELATIVE_SERVER_DIR="examples/";
	
	
	@SuppressWarnings("unchecked")
	public final static Class[] NCI_WIDGETS = {
		OWLMetadataTab.class, NCIEditTab.class, LuceneQueryPlugin.class,
		ChangesTab.class
	};
	
	private String ontPrefix;
	private String hostname = "localhost";
	private String database;
	private String table;
	private String user;
	private String password;
	
	private File owlFile;
	private File projectFile;
	private File dbProjectFile;
	
	public Main() throws SQLException {
		try {
			Class.forName(DB_URL);
		}
		catch (ClassNotFoundException cnfe) {
			SQLException sqle = new SQLException("Could not install database driver");
			sqle.initCause(cnfe);
			throw sqle;
		}
	}

	public void setOntPrefix(String ontPrefix) {
		this.ontPrefix = ontPrefix;
		owlFile = new File(ontPrefix + ".owl");
		projectFile = new File(ontPrefix + ".pprj");
		dbProjectFile = new File(ontPrefix + "-db.pprj");
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		if (hostname.equals("")) {
			this.hostname = "localhost";
		}
		else {
			this.hostname = hostname;
		}
	}


	public void setDatabase(String database) {
		this.database = database;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public void execute() throws OntologyLoadException {
		System.out.println("----------------Creating the OWL File Project");
		OWLModel model = createFileProject();
		System.out.println("----------------OWL File Project Saved");
		System.out.println("----------------Indexing Ontologies");
		indexOntology(model);
		System.out.println("----------------Ontologies Indexed");
		System.out.println("----------------Creating Database Project");
		model = convertToDatabaseProject(model);
		setTabs(model);
		saveDBProject(model);
		System.out.println("----------------Database Project Saved");
		System.out.println("----------------Creating Changes Annotation Project");
		createAnnotationProject(model);
		System.out.println("----------------Changes Annotation Project Saved");
		System.out.println("----------------Creating Meta Project for Server");
		updateMetaProject();
		System.out.println("----------------Meta Project for Server Saved");
	}
	
	@SuppressWarnings("unchecked")
	private OWLModel createFileProject() {
		List errors = new ArrayList();
		OWLFilesCreateProjectPlugin creator = new OWLFilesCreateProjectPlugin();
		creator.setKnowledgeBaseFactory(new JenaKnowledgeBaseFactory());
		creator.setUseExistingSources(true);
		creator.setLanguage(FileUtils.langXMLAbbrev);
		creator.setFile(owlFile.toURI().toString());
		creator.setDefaultClassView(LogicClassDefinitionWidgetType.class);
		Project p = creator.createProject();
		p.setProjectURI(projectFile.toURI());
		p.save(errors);
		displayErrors(errors);
		return (OWLModel) p.getKnowledgeBase();
	}
	

	
	public void indexOntology(OWLModel model) {
		QueryApi api = new QueryApi(model);
		QueryConfiguration config = new QueryConfiguration(model);
		// cleverly set the location that it will use when it is a database project.
		config.setBaseIndexPath("lucene" + File.separator + ontPrefix + "-db");
		api.install();
		api.index(config);
	}
	
	/*
	 * The following was put together by stepping through ProjectManager.changeProjectStorageFormatRequest().
	 * The Streaming database version of this code is much more robust.
	 */
	@SuppressWarnings("unchecked")
	private OWLModel convertToDatabaseProject(OWLModel fileModel)  {
		ProjectPluginManager pluginManager = new ProjectPluginManager();
		List errors = new ArrayList();

		Project fileProject = fileModel.getProject();
		
		OWLDatabaseKnowledgeBaseFactory factory = new CPOWLDatabaseKnowledgeBaseFactory();
		PropertyList sources = fileProject.getSources();
		OWLDatabaseKnowledgeBaseFactory.setDriver(sources, DB_URL);
		OWLDatabaseKnowledgeBaseFactory.setURL(sources, makeURL());
		OWLDatabaseKnowledgeBaseFactory.setTablename(sources, table);
		OWLDatabaseKnowledgeBaseFactory.setUsername(sources, user);
		OWLDatabaseKnowledgeBaseFactory.setPassword(sources, password);
		sources.setString(KnowledgeBaseFactory.FACTORY_CLASS_NAME, factory.getClass().getName());
		fileProject.setProjectURI(dbProjectFile.toURI());
		((KnowledgeBaseFactory2) fileProject.getKnowledgeBaseFactory()).prepareToSaveInFormat(fileModel, factory, errors);
		pluginManager.beforeSave(fileProject);
		fileProject.save(errors);
		pluginManager.afterSave(fileProject);
		fileProject.dispose();
		
		Project dbProject = Project.loadProjectFromURI(dbProjectFile.toURI(), errors);
		pluginManager.afterLoad(dbProject);
		
		displayErrors(errors);
		return (OWLModel) dbProject.getKnowledgeBase();
	}

	
	private String makeURL() {
		return "jdbc:mysql://" + hostname + "/" + database;
	}
	
	@SuppressWarnings("unchecked")
	public void setTabs(OWLModel model) {
		Project p = model.getProject();
		Map<String, WidgetDescriptor> widgetMap = new HashMap<String, WidgetDescriptor>();
		for (Object o  : p.getTabWidgetDescriptors()) {
			if (o instanceof WidgetDescriptor) {
				WidgetDescriptor w = (WidgetDescriptor) o;
				w.setVisible(false);
				widgetMap.put(w.getWidgetClassName(), w);
			}
		}
		List<WidgetDescriptor> widgets = new ArrayList<WidgetDescriptor>();
		for (Class clz : NCI_WIDGETS) {
			WidgetDescriptor w = widgetMap.get(clz.getName());
			if (w == null) {
				System.out.println("Could not find widget for " + clz);
			}
			widgets.add(w);
			w.setVisible(true);
		}
		p.setTabWidgetDescriptorOrder(widgets);
	}
	
	
	@SuppressWarnings("unchecked")
	private void createAnnotationProject(OWLModel model) {
//	    KnowledgeBase changesKb = ChAOKbManager.createRDFFileChAOKb(
//            model, ChAOKbManager.getChAOProjectURI(model));

        //Note: Use the following if you want to create a db version,
        //  in lieu of the previous line
        String db = "jdbc:mysql://" + hostname + "/" + database;
        String annoTable = "annotation_" + ontPrefix;
        KnowledgeBase changesKb = ChAOKbManager.createDbChAOKb(
            model, ChAOKbManager.getChAOProjectURI(model),
            DB_URL, db, annoTable, user, password);
		
		List errors = new ArrayList();
		changesKb.getProject().save(errors);
	    displayErrors(errors);
	}
	

	
	@SuppressWarnings("unchecked")
	private void updateMetaProject() {
		MetaProject metaproject = new MetaProjectImpl(new File("metaproject.pprj").toURI());
		
		if (metaproject.getProject(ontPrefix) != null) {
			log.config("Meta-project already contains this project");
			return;
		}
		
		ProjectInstance project = metaproject.createProject(ontPrefix);
		project.setLocation(RELATIVE_SERVER_DIR + ontPrefix + "-db.pprj");
		project.setAllowedGroupOperations(metaproject.getGroupOperations());

		ProjectInstance annotationProject = metaproject.createProject("Do not edit - " + ontPrefix + " annotations");
		annotationProject.setLocation(RELATIVE_SERVER_DIR + "annotation_" + ontPrefix + "-db.pprj");
		for (GroupOperation groupOp : metaproject.getGroupOperations()) {
			if (!groupOp.getAllowedOperations().contains(MetaProjectConstants.OPERATION_DISPLAY_IN_PROJECT_LIST)) {
				annotationProject.addAllowedGroupOperations(groupOp);
			}
		}
		
		
		project.setAnnotationProject(annotationProject);
		
		List errors = new ArrayList();
		metaproject.save(errors);
		displayErrors(errors);
	}
	
	@SuppressWarnings("unchecked")
	private void saveDBProject(OWLModel model) {
        Project p = model.getProject();
        p.setProjectURI(dbProjectFile.toURI());
        List errors = new ArrayList();
        p.save(errors);
        displayErrors(errors);
	}
	
	@SuppressWarnings("unchecked")
	private static void displayErrors(List errors) {
		if (!errors.isEmpty()) {
			System.out.println("--------------------------------------- Errors Found ----------------");
			for (Object o : errors) {
				if (o instanceof Throwable) {
					System.out.println("Exception caught");
					((Throwable) o).printStackTrace();
				}
				else {
					System.out.println("Error = " + o);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		try {
			Main job = new Main();
			InputStream in = new FileInputStream(args[0]);
            Properties p;
            try {
                p = new Properties();
                p.load(in);
            }
            finally {
                in.close();
            }
            
            job.setOntPrefix(p.getProperty(ONTOLOGY_PREFIX_PROPERTY));
            job.setHostname(p.getProperty(DB_HOST_PROPERTY));
            job.setDatabase(p.getProperty(DB_DATABASE_PROPERTY));
            job.setTable(p.getProperty(DB_TABLE_PROPERTY));
            job.setUser(p.getProperty(DB_USER_PROPERTY));
            job.setPassword(p.getProperty(DB_PASSWORD_PROPERTY));

			job.execute();
		}
		catch (Throwable t) {
			System.out.println("Conversion failed");
			t.printStackTrace();
		}
	}
	
	


}
