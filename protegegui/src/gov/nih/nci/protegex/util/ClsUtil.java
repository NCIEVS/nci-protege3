package gov.nih.nci.protegex.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import gov.nih.nci.protegex.edit.OWLWrapper;

/**
 * Contains utility methods for concept class.
 * 
 * @author David Yee
 */
public class ClsUtil {
	/**
	 * Returns the concept name from the concept class.
	 * 
	 * @param cls
	 *            The concept class.
	 * @return The concept name.
	 */
	public static String getConceptName(Cls cls) {
		OWLNamedClass owlCls = (OWLNamedClass) cls;
		if (owlCls == null)
			return "null";
		return owlCls.getPrefixedName();
	}

	/**
	 * Returns the concept code from the concept class.
	 * 
	 * @param wrapper
	 *            The OWLWrapper.
	 * @param cls
	 *            The concept class.
	 * @return The concept code.
	 */
	public static String getConceptCode(OWLWrapper wrapper, Cls cls) {
		if (cls == null)
			return "null";
		return wrapper.getCode(cls);
	}

	/**
	 * Returns the numeric value part of the concept code from a concept class.
	 * 
	 * @param wrapper
	 *            The OWLWrapper.
	 * @param cls
	 *            The concept class.
	 * @return the numeric value part of the concept code from a concept class.
	 */
	public static int getNumericCode(OWLWrapper wrapper, Cls cls) {
		return StringUtil.extractFirstInteger(ClsUtil.getConceptCode(wrapper,
				cls));
	}

	/**
	 * Finds the concept class by its name.
	 * 
	 * @param wrapper
	 *            The OWLWrapper.
	 * @param className
	 *            The class name.
	 * @return The corresponding OWLNamedClass.
	 */
	public static OWLNamedClass getClsByName(OWLWrapper wrapper,
			String className) {
		return wrapper.getOWLNamedClass(className);
	}

	// //Note: no one currently calling this method
	// /**
	// * Finds the "old" concept class by its concept code.
	// * @param kb The KnowledgeBase.
	// * @param cls The concept class.
	// * @return The corresponding OWLNamedClass.
	// */
	// public static OWLNamedClass getOldONC(KnowledgeBase kb, OWLNamedClass
	// cls) {
	// // TODO: Dave this is just an ugly kluge to get a protege concept into
	// // the panel
	// RDFProperty code = ((OWLModel) kb).getRDFProperty("code");
	// if (cls.getPropertyValue(code) != null)
	// return getOldONC(kb, cls.getPropertyValue(code));
	// return null;
	// }

	/**
	 * Finds the "old" concept class by its concept code.
	 * 
	 * @param owlModel
	 *            The OWLModel.
	 * @param conceptCode
	 *            The concept code.
	 * @return The corresponding OWLNamedClass.
	 */
	public static OWLNamedClass getConceptByCode(OWLModel owlModel,
			String conceptCode) {
		RDFProperty code = owlModel.getRDFProperty("code");
		Iterator it = owlModel.getRDFResourcesWithPropertyValue(code,
				conceptCode).iterator();
		if (it.hasNext())
			return (OWLNamedClass) it.next();
		return null;
	}

	/**
	 * Deletes the class (or concept) from the server (database) side.
	 * 
	 * @param model
	 *            The OWLModel.
	 * @param cls
	 *            The class.
	 * @return true if successful.
	 */
	public static boolean deleteCls(OWLModel model, OWLNamedClass cls) {
		try {
			model.beginTransaction("Deleting class " + cls.getBrowserText(),
					cls.getName());
			cls.delete();
			model.commitTransaction();
			return true;
		} catch (Exception e) {
			model.rollbackTransaction();
			return false;
		}
	}

	/**
	 * Deletes the proposed class (when its code value is 0) from the server
	 * (database) side.
	 * 
	 * @param wrapper
	 *            The OWLWrapper.
	 * @param model
	 *            The OWLModel.
	 * @param cls
	 *            The class.
	 * @return true if successful.
	 */
	public static boolean deleteProposedCls(OWLWrapper wrapper, OWLModel model,
			OWLNamedClass cls) {
		String code = wrapper.getCode(cls);
		if (code == null || !code.equals("0"))
			return false;
		return deleteCls(model, cls);
	}

	/**
	 * Returns a list of selectable resources (properties).
	 * 
	 * @param owlModel
	 *            The OWL Model.
	 * @return a list of selectable resources (properties).
	 */
	public static Collection getSelectableResources(OWLModel owlModel) {

		Collection properties = owlModel.getOWLAnnotationProperties();

		ArrayList<RDFProperty> result = new ArrayList<RDFProperty>();

		Iterator it = properties.iterator();
		while (it.hasNext()) {
			RDFProperty prop = (RDFProperty) it.next();
			if (!prop.isReadOnly()
					&& !prop.getNamespacePrefix().equalsIgnoreCase("xsp")
					&& !prop.getNamespacePrefix().equalsIgnoreCase("protege")) {
				result.add(prop);
			}

		}
		return result;
	}

	/**
	 * Creates or retrieves a concept.
	 * 
	 * @param model
	 *            The OWLModel
	 * @param name
	 *            The concept name
	 * @param create
	 *            if true, creates a concept
	 * @return OWLNamedClass
	 */
	public static OWLNamedClass createOrRetrieveConcept(OWLModel model,
			String name, boolean create) throws Exception {
		OWLNamedClass cls = model.getOWLNamedClass(name);
		if (create && cls == null) {
			cls = model.createOWLNamedClass(name);
			cls.addPropertyValue(model.getRDFProperty("code"), "0");
		}
		if (cls == null)
			throw new Exception("Concept " + name + " could not be "
					+ (create ? "created." : "retrieved."));
		return cls;
	}

	/**
	 * Returns true if code is a valid concept code.
	 * 
	 * @param code
	 *            The concept code.
	 * @return true if code is a valid concept code.
	 */
	public static boolean isCode(String code) {
		if (code == null || code.length() < 2
				|| !Character.isLetter(code.charAt(0)))
			return false;
		return true;
		/**
		try {
			Integer.parseInt(code.substring(1));
			return true;
		} catch (Exception e) {
			return false;
		}
		**/
	}

	/**
	 * collect all properties in an array at initialize time.
	 */
	private static ArrayList<RDFProperty> _allProps = null;

	/**
	 * Returns the list of properties.
	 * 
	 * @param model
	 *            The OWLModel.
	 * @return the list of properties.
	 */
	public static ArrayList<RDFProperty> getAllProperties(OWLModel model) {
		if (_allProps != null)
			return _allProps;

		ArrayList<RDFProperty> allProps = new ArrayList<RDFProperty>();
		Collection props = model.getRDFProperties();
		Iterator iterator = props.iterator();

		while (iterator.hasNext()) {
			RDFProperty prop = (RDFProperty) iterator.next();
			allProps.add(prop);
		}
		return _allProps = allProps;
	}

	/**
	 * Returns the RDFProperty from the property code.
	 * 
	 * @param model
	 *            The OWLModel.
	 * @param code
	 *            The property code.
	 * @return the RDFProperty from the property code.
	 */
	public static RDFProperty findRDFProperty(OWLModel model, String code) {
		getAllProperties(model); // Initializes _allProps
		RDFProperty codeProp = model.getRDFProperty("code");
		for (RDFProperty prop : _allProps)
			if (code.equalsIgnoreCase((String) prop.getPropertyValue(codeProp)))
				return prop;
		return null;
	}
}
