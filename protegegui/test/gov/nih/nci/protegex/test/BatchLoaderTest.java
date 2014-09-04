package gov.nih.nci.protegex.test;

public class BatchLoaderTest extends ProtegeTestAppl {
    private static final String HOSTNAME = "localhost";
    private static final String USERNAME = "";
    private static final String PASSWORD = "";
    private static final String PROJNAME = "NCIThesaurus";
    
    public BatchLoaderTest() {
        super(HOSTNAME, USERNAME, PASSWORD, PROJNAME);
    }

    protected void run() {
        try {
            String parentClsName = "BatchLoaderBug";
            batchLoaderBug(parentClsName);
            printConceptsFrom(parentClsName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void batchLoaderBug(String parentClsName) throws Exception {
        deleteConcept(parentClsName);
        _model.createOWLNamedClass(parentClsName);
        
        int max = 2;
        for (int i=0; i<max; ++i) {
            String conceptName = parentClsName + "_Name_" + i;
            String preferredName = parentClsName + "_Preferred_Name_" + i;
            _wrapper.createCls(conceptName, preferredName, parentClsName);
            //Thread.sleep(5000);
        }       
    }

    public static void main(String[] args) {
        new BatchLoaderTest();
    }
}
