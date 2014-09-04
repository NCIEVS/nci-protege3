 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.operation.editor;

import java.awt.*;
import java.lang.reflect.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.operation.*;
import edu.stanford.smi.protegex.prompt.ui.*;

public abstract class Editor {
	public final static String DEEP_COPY = "superclasses";
	public final static String DEEP_COPY_SLOTS = "superslots";
	public final static String COPY_SUBCLASSES = "subclasses";
	public final static String COPY_SUBSLOTS = "subslots";
	public final static String COPY_INSTANCES = "instances";
	protected final static String COPY_EVERYTHING_RELATED = "everything related";
	protected final static String COPY_SLOTS = "slots (template)";
	protected final static String SLOT_VALUES = "slot values";
	protected final static String DEPTH_LIMIT_PREFIX = "(";
        protected final static String DEPTH_LIMIT = "depth limit: ";
	protected final static String DEPTH_LIMIT_SUFFIX = ")";
	protected final static String EVERYTHING_REQUIRED = "everything required";


    protected Object [] _args;
  	protected GetValueWidget [] _argumentWidgets;
    protected static final String OPERATIONS_PACKAGE = "edu.stanford.smi.protegex.prompt.operation.";
    private static final String [] DEFINED_OPERATIONS_MERGING =
            {"edu.stanford.smi.protegex.prompt.operation.editor.MergeClsesOperationEditor",
             "edu.stanford.smi.protegex.prompt.operation.editor.MergeInstancesOperationEditor",
             "edu.stanford.smi.protegex.prompt.operation.editor.MergeSlotsOperationEditor",
             "edu.stanford.smi.protegex.prompt.operation.editor.CopyClsOperationEditor",
             "edu.stanford.smi.protegex.prompt.operation.editor.CopyInstanceOperationEditor",
             "edu.stanford.smi.protegex.prompt.operation.editor.CopySlotOperationEditor",
//             "edu.stanford.smi.protegex.prompt.operation.editor.RenameClsOperationEditor",
//             "edu.stanford.smi.protegex.prompt.operation.editor.RenameSlotOperationEditor",
             "edu.stanford.smi.protegex.prompt.operation.editor.RemoveParentOperationEditor",
             };
    private static final String [] DEFINED_OPERATIONS_EXTRACTING =
    {
     "edu.stanford.smi.protegex.prompt.operation.editor.CopyClsOperationEditor",
     "edu.stanford.smi.protegex.prompt.operation.editor.CopyInstanceOperationEditor",
     "edu.stanford.smi.protegex.prompt.operation.editor.CopySlotOperationEditor",
     };
    private static final String [] DEFINED_OPERATIONS_MAPPING =
    {
    	"edu.stanford.smi.protegex.prompt.operation.editor.MergeClsesOperationEditor",
        "edu.stanford.smi.protegex.prompt.operation.editor.MergeInstancesOperationEditor",
        "edu.stanford.smi.protegex.prompt.operation.editor.MergeSlotsOperationEditor"
     };
    private static final String [] DEFINED_OPERATIONS_PARTITIONING =
            {
             "edu.stanford.smi.protegex.prompt.operation.editor.MoveClsUpOperationEditor",
             "edu.stanford.smi.protegex.prompt.operation.editor.MoveClsDownOperationEditor",
             "edu.stanford.smi.protegex.prompt.operation.editor.MoveInstanceUpOperationEditor",
             "edu.stanford.smi.protegex.prompt.operation.editor.MoveInstanceDownOperationEditor",
             "edu.stanford.smi.protegex.prompt.operation.editor.MoveInstancesOfClsDownOperationEditor",
             "edu.stanford.smi.protegex.prompt.operation.editor.MoveSlotUpOperationEditor",
             "edu.stanford.smi.protegex.prompt.operation.editor.MoveSlotDownOperationEditor",
             };
  private static String [] _responsibleFor = new String[] {
  	OPERATIONS_PACKAGE + "Operation"};
  private static Collection _allowedOperations = createAllowedOperations();

  private Operation _originalOperation = null;

  protected static Operation _dispatchOperation = null;
  protected String _name = null;
  protected String _prettyName = null;
  protected Object _arg = null;
  private static final Class [] emptyClassArray = {};
  private static final Object[] emptyObjectArray = {};
  private static HashMap _operationToEditor = null;

  private static Collection createAllowedOperations () {
    Collection result = new ArrayList();
 	try{
    	String [] definedOperations;
        if (PromptTab.merging())
        	definedOperations = DEFINED_OPERATIONS_MERGING;
        else if (PromptTab.mapping())
        	definedOperations = DEFINED_OPERATIONS_MAPPING;
        else if (PromptTab.extracting())
        	definedOperations = DEFINED_OPERATIONS_EXTRACTING;
        else
		   	definedOperations = DEFINED_OPERATIONS_PARTITIONING;


    	for (int i = 0; i < definedOperations.length; i++)
      		result.add (Class.forName(definedOperations[i]));
	} catch (Exception e) {
    	   e.printStackTrace();
	}
    return result;
  }

  public static String getPrettyName (Class c) {
  	try {
  	Method nameMethod = c.getMethod ("getPrettyName", emptyClassArray);
    return (String) nameMethod.invoke(null, emptyObjectArray);
    } catch (Exception e) {
     	e.printStackTrace();
    }
    return null;
  }

  public static Collection getAllowedOperations () {
	return _allowedOperations;
  }

  public static int numberOfOperations () {
   	return _allowedOperations.size();
  }

  public static OperationPanel createActionBox (Class c, boolean chooseOntology, boolean willBeModal) {
   	return createActionBox (c, null, chooseOntology, willBeModal);
  }

  abstract public GetValueWidget [] createValueWidgets ();

  abstract public GetValueWidget [] createValueWidgets (boolean chooseOntology, boolean willBeModal);

  public static OperationPanel createActionBox (Class c, Operation a,
  												boolean chooseOntology, boolean willBeModal) {
    JPanel p1 = new JPanel();
    Editor editor = null;
	try {
    	Constructor defaultConstructor = c.getConstructor(emptyClassArray);
        editor = (Editor)defaultConstructor.newInstance (emptyObjectArray);
        GetValueWidget [] argWidgets = editor.createValueWidgets (chooseOntology, willBeModal);

	    if (a != null) {
        	editor._originalOperation = a;
        	Object [] args = editor.createArgs (a);
	    	for (int i = 0; i < argWidgets.length; i++)
	      		argWidgets[i].setValue(args[i]);
	    }

//	    p1.setLayout (new BoxLayout (p1, BoxLayout.Y_AXIS));

	JPanel frameWidgetsPanel = new JPanel ();
        JPanel simpleWidgetsPanel = new JPanel ();
        JPanel simpleShortWidgetsPanel = new JPanel ();

        int frameWidgetIndex = 0;
        int simpleWidgetIndex = 0;

        CheckBoxWithValueWidget wideWidget = null;

        for (int i = 0; i < argWidgets.length; i++) {
          JComponent next = (JComponent)argWidgets[i];
	  next.setAlignmentX(JComponent.LEFT_ALIGNMENT);
          if (next instanceof SelectFrameWidget) {
            frameWidgetsPanel.add(next);
            frameWidgetIndex++;
          }
          else if (next instanceof CheckBoxWithValueWidget)
            wideWidget = (CheckBoxWithValueWidget)next;
          else {
            simpleShortWidgetsPanel.add(next);
            simpleWidgetIndex++;
          }
        }

        frameWidgetsPanel.setLayout (new GridLayout (1, 0, 3, 0));
        simpleShortWidgetsPanel.setLayout (new GridLayout (1, 0, simpleWidgetIndex-1, 0));

        if (wideWidget != null) {
          simpleWidgetsPanel.setLayout (new BorderLayout());
          if (simpleShortWidgetsPanel.getComponentCount() > 0) {
            simpleWidgetsPanel.add (simpleShortWidgetsPanel, BorderLayout.CENTER);
            simpleWidgetsPanel.add (wideWidget, BorderLayout.EAST);
          } else
            simpleWidgetsPanel.add (wideWidget, BorderLayout.WEST);
        } else
          simpleWidgetsPanel = simpleShortWidgetsPanel;

/*
	for (int i = 0; i < argWidgets.length; i++) {
          JComponent next = (JComponent)argWidgets[i];
	  next.setAlignmentX(JComponent.LEFT_ALIGNMENT);
          if (next instanceof SelectFrameWidget)
            	frameWidgets.add (next);
          else
            	simpleWidgets.add(next);
//			p1.add(next);
        }
*/
        if (frameWidgetsPanel.getComponentCount() == 1)
        	frameWidgetsPanel.add (new JPanel());

        p1.setLayout (new BorderLayout () );
        p1.setBorder(new EmptyBorder (5, 5, 3, 3));
    	p1.add (frameWidgetsPanel, BorderLayout.NORTH);
        p1.add (simpleWidgetsPanel, BorderLayout.SOUTH);
    }  catch (Exception e) {
     	e.printStackTrace();
    }

    return new OperationPanel (editor, p1);
  }

  protected Object [] createArgs (Operation a) {
	_args = a.getArgs ().toArray();

   	return _args;
  }

  public static OperationPanel createEditBox (Operation a, boolean willBeModal) {
  	Class editor = findEditor (a);
    OperationPanel p = createActionBox (editor, a, true, willBeModal);
    return p;
  }

  private static Class findEditor (Operation a) {
	if (_operationToEditor == null)
    	buildOperationToEditorHashMap ();
    return (Class)_operationToEditor.get (a.getClass());
  }

  private static void buildOperationToEditorHashMap () {
  try {
    _operationToEditor = new HashMap ();

    String [] definedOperations;
    if (PromptTab.merging())
       	definedOperations = DEFINED_OPERATIONS_MERGING;
    else if (PromptTab.mapping())
       	definedOperations = DEFINED_OPERATIONS_MAPPING;   
    else if (PromptTab.extracting())
       	definedOperations = DEFINED_OPERATIONS_EXTRACTING;
    else
       	definedOperations = DEFINED_OPERATIONS_PARTITIONING;

   	for (int i = 0; i < definedOperations.length; i++) {
     	Class next = Class.forName (definedOperations[i]);
        Constructor defaultConstructor = next.getConstructor (emptyClassArray);
        String[] operationNames =  ((Editor)defaultConstructor.newInstance(emptyObjectArray)).responsibleFor();
        for (int j = 0; j < operationNames.length; j++)
            	_operationToEditor.put (Class.forName (operationNames[j]), next);
    }
  } catch (Exception e) {
   	e.printStackTrace();
  }
  }

  public void clear () {
    for (int i = 0; i < _argumentWidgets.length; i++)
    	_argumentWidgets[i].clear();
  }

  public Operation collectData () {
	String[] className = responsibleFor();
  	if (className.length > 1)
    	Log.getLogger().severe ("There should be only one class name");
    Object next;
    Object [] args = new Object [_argumentWidgets.length];


    for (int i = 0; i < _argumentWidgets.length; i++) {
      next = _argumentWidgets[i].getValue();
      args[i] = next;
//      _argumentWidgets[i].clear();
    }

    if (Arrays.equals(_args, args)) return _originalOperation;

     try {
        Class classItself = Class.forName(className[0]);
	    Constructor defaultConstructor = classItself.getConstructor (emptyClassArray);
    	Operation a = (Operation)defaultConstructor.newInstance(emptyObjectArray);
      	a.setArgs (args);
        return a;
     } catch (Exception e) {
      	e.printStackTrace();
     }
        return null;
  }

  public  String [] responsibleFor () {
	Log.getLogger().severe ("should never be here");
   	return _responsibleFor;
  }

  public void addArgument (Object o) {
        for (int i = 0; i < _argumentWidgets.length; i++) {
         	if (addValue ((GetValueWidget)_argumentWidgets[i], o))
            	return;
        }
  }

  protected static boolean addValue (GetValueWidget w, Object o) {
  	if (! (w instanceof SelectFrameWidget)) return false;
    if (w.getValue() != null) return false;
    w.setValue(o);
    return true;
  }



}
