package gov.nih.nci.protegex.edit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.ui.FrameComparator;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLAllValuesFrom;
import edu.stanford.smi.protegex.owl.model.OWLAnonymousClass;
import edu.stanford.smi.protegex.owl.model.OWLCardinality;
import edu.stanford.smi.protegex.owl.model.OWLDataRange;
import edu.stanford.smi.protegex.owl.model.OWLHasValue;
import edu.stanford.smi.protegex.owl.model.OWLIntersectionClass;
import edu.stanford.smi.protegex.owl.model.OWLMaxCardinality;
import edu.stanford.smi.protegex.owl.model.OWLMinCardinality;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLNames;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.OWLRestriction;
import edu.stanford.smi.protegex.owl.model.OWLSomeValuesFrom;
import edu.stanford.smi.protegex.owl.model.OWLUnionClass;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSLiteral;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFSNames;
import edu.stanford.smi.protegex.owl.model.classparser.OWLClassParseException;
import edu.stanford.smi.protegex.owl.model.classparser.OWLClassParser;
import edu.stanford.smi.protegex.owl.model.impl.DefaultOWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.impl.DefaultRDFSDatatype;
import edu.stanford.smi.protegex.owl.model.impl.DefaultRDFSLiteral;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.profiles.ProfilesManager;
import edu.stanford.smi.protegex.owl.ui.search.FindUsage;
import edu.stanford.smi.protegex.owl.ui.search.FindUsageTableItem;
import gov.nih.nci.protegex.codegen.RemoteCodeGen;
import gov.nih.nci.protegex.dialog.CustomizedAnnotationData;
import gov.nih.nci.protegex.util.Config;
import gov.nih.nci.protegex.util.QuickSortVecStrings;

//import gov.nih.nci.protegex.edit.RemoteOWL.RemoteOWLWrapper;

/**
 * @Author: NGIT, Kim Ong; Iris Guo
 */
public class OWLWrapper {

	private Logger logger = Log.getLogger(getClass());

	private static OWLWrapper wrapper = null;

	private final String NOCODE = "NOCODE";
	
	private Config config = null;
	
	public void setConfig(Config cfg) {config = cfg;}

	private OWLWrapper(OWLModel kb) {

		owlModel = kb;
		default_kb = (DefaultKnowledgeBase) owlModel;
		sort = new QuickSortVecStrings();
		

	}

	public static OWLWrapper createInstance(OWLModel m) {
		if (wrapper != null) {

		} else {
			wrapper = new OWLWrapper(m);

		}

		return getInstance();
	}

	public static OWLWrapper getInstance() {

		return wrapper;
	}

	/**
	 * If protege is closed and reopened, new singleton is needed
	 * 
	 */
	public void close() {
		wrapper = null;

	}

	private static QuickSortVecStrings sort = null;

	String referenced_code = null;

	private OWLModel owlModel;

	private DefaultKnowledgeBase default_kb;

	Vector noneditable_vec = null;

	String username = null;

	Vector requiredProperties = null;

	Vector readOnlyProperties = null;

	
	public boolean codeSlotExists = false;
	
	public boolean byCode() {
		return !codeSlotExists;
	}

	String codeSlotName = null;

	public void setUserName(String name) {
		username = name;
	}

	public String getCodeSlotName() {
		return codeSlotName;
	}
	
	public void setCodeSlotName(String s) {
		codeSlotName = s;
	}

	public String[] getSupportedLanguages() {
		return owlModel.getUsedLanguages();
	}

	public void init(Vector noneditable_vec, Vector readOnlyProperties,
			Vector requiredProperties) {
		this.noneditable_vec = noneditable_vec;
		this.readOnlyProperties = readOnlyProperties;
		this.requiredProperties = requiredProperties;		
	}

	public String getPlainString(String s) {
		String[] languages = owlModel.getUsedLanguages();
		if (s.indexOf("~#") == -1)
			return s;

		for (int i = 0; i < languages.length; i++) {
			if (s.startsWith("~#" + languages[i] + " "))
				return s.substring(5);
		}

		return s.substring(2);
	}

	private boolean removeRestriction(OWLNamedClass owl_cls, String name,
			String value, String modifier) {
		if (owl_cls == null)
			return false;
		boolean includingSuperclassRestrictions = false;
		java.util.Collection col = owl_cls
				.getRestrictions(includingSuperclassRestrictions);
		int i = 0;
		for (Iterator it = col.iterator(); it.hasNext();) {
			OWLRestriction r = (OWLRestriction) it.next();			

			RDFProperty p = r.getOnProperty();
			String prop_name = p.getPrefixedName();
			
			//Collection pvals = owl_cls.getPropertyValues(p);
			// TODO: filler text can have single quotes
			String prop_value = null;
			if (r instanceof OWLSomeValuesFrom) {
				prop_value = ((OWLNamedClass) ((OWLSomeValuesFrom) r).getFiller()).getPrefixedName();
			} else if (r instanceof OWLAllValuesFrom) {
				prop_value = ((OWLNamedClass) ((OWLAllValuesFrom) r).getFiller()).getPrefixedName();			
			}
			//r.getFillerText(); 
			String prop_modifier = getRestrictionType(r);

			i++;

			String value_with_brackets = "(" + prop_value + ")";

			if (name.compareTo(prop_name) == 0
					&& value.compareTo(prop_value) == 0
					&& modifier.compareToIgnoreCase(prop_modifier) == 0) {
				owl_cls.removeSuperclass((RDFSClass) r);
				r.delete();

				return true;
			} else if (name.compareTo(prop_name) == 0
					&& value.compareTo(value_with_brackets) == 0
					&& modifier.compareToIgnoreCase(prop_modifier) == 0) {
				owl_cls.removeSuperclass((RDFSClass) r);
				r.delete();

				return true;
			}
		}
		return false;
	}

	public String getRestrictionType(OWLRestriction restriction) {
		if (restriction instanceof OWLCardinality) {
			return "exactly";
		} else if (restriction instanceof OWLMinCardinality) {
			return "min";
		} else if (restriction instanceof OWLMaxCardinality) {
			return "max";
		} else if (restriction instanceof OWLAllValuesFrom) {
			return "only";
		} else if (restriction instanceof OWLSomeValuesFrom) {
			return "some";
		} else if (restriction instanceof OWLHasValue) {
			return "has";
		}
		return "undefined";
	}

	public String getRestrictionType(String restriction) {
		if (restriction.compareToIgnoreCase("owl:CardinalityRestriction") == 0) {
			return "exactly";
		} else if (restriction
				.compareToIgnoreCase("owl:MinCardinalityRestriction") == 0) {
			return "min";
		} else if (restriction
				.compareToIgnoreCase("owl:MaxCardinalityRestriction") == 0) {
			return "max";
		} else if (restriction
				.compareToIgnoreCase("owl:AllValuesFromRestriction") == 0) {
			return "only";
		} else if (restriction
				.compareToIgnoreCase("owl:SomeValuesFromRestriction") == 0) {
			return "some";
		} else if (restriction.compareToIgnoreCase("owl:HasValueRestriction") == 0) {
			return "has";
		}
		return restriction;
	}

	public String restrictionType2Label(String type) {
		if (type.compareToIgnoreCase("exactly") == 0) {
			return "owl:CardinalityRestriction";
		} else if (type.compareToIgnoreCase("min") == 0) {
			return "owl:MinCardinalityRestriction";
		} else if (type.compareToIgnoreCase("max") == 0) {
			return "owl:MaxCardinalityRestriction";
		} else if (type.compareToIgnoreCase("all") == 0
				|| type.compareToIgnoreCase("only") == 0) {
			return "owl:AllValuesFromRestriction";
		} else if (type.compareToIgnoreCase("some") == 0) {
			return "owl:SomeValuesFromRestriction";
		} else if (type.compareToIgnoreCase("has") == 0) {
			return "owl:HasValueRestriction";
		}

		return type;
	}

	private boolean conceptExists(String name) {
		try {
			OWLNamedClass cls = owlModel.getOWLNamedClass(name);
			if (cls == null)
				return false;
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	
	private String getSerializedCustomizedAnnotationData(String annotation_name, ArrayList<String> values) {
		CustomizedAnnotationData cad = config.getCustomizedAnnotationData(annotation_name);
		return cad.formatValues(values);
	}
	

	// //////////////////////////////////////////////////////////
	// batch load
	// //////////////////////////////////////////////////////////
	private String formatFULL_SYN(String name, String term_group, String source) {
		ArrayList<String> vals = new ArrayList<String>();
		vals.add(name);
		vals.add(term_group);
		vals.add(source);
		vals.add("");
		vals.add("en");
		return getSerializedCustomizedAnnotationData(NCIEditTab.ALTLABEL,
				vals);
		
		// return name + "|" + term_group + "|" + source;
	}

	// 101906, KLO, Tracker #3119

	public String formatDEFINITION(String source, String text) {
		// SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd");

		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		String mydate = sdf.format(new Date());
		
		ArrayList<String> vals = new ArrayList<String>();
		
		vals.add(text);
		vals.add("en");		
		vals.add(mydate);
		vals.add(source);
		vals.add("");
		vals.add(username);
		
		return getSerializedCustomizedAnnotationData(NCIEditTab.DEFINITION, vals);

		
	}

	// //////////////////////////////////////////////////////////
	// batch editing
	// //////////////////////////////////////////////////////////

	public boolean modifyAnnotationProperty(String name, String propertyname,
			String propertyvalue, String newpropertyvalue) {
		boolean conceptexists = conceptExists(name);
		if (!conceptexists)
			return false;
		OWLNamedClass cls = owlModel.getOWLNamedClass(name);
		boolean retval = removeAnnotationProperty(cls, propertyname,
				propertyvalue);
		if (!retval)
			return retval;
		return createAnnotationProperty(cls, propertyname, newpropertyvalue);
	}

	public boolean removeAnnotationProperty(String name, String propertyname,
			String propertyvalue) {
		boolean conceptexists = conceptExists(name);
		if (!conceptexists)
			return false;

		OWLNamedClass owlCls = (OWLNamedClass) getCls(name);
		return removeAnnotationProperty(owlCls, propertyname, propertyvalue);
	}

	public boolean addRestriction(String clsName, String name, String value,
			String modifier)

	{
		OWLNamedClass owl_cls = getOWLNamedClass(clsName);
		if (owl_cls == null)
			return false;
		return addRestriction(owl_cls, name, value, modifier);
	}

	public boolean removeRestriction(String clsName, String name, String value,
			String modifier) {
		OWLNamedClass owl_cls = getOWLNamedClass(clsName);
		if (owl_cls == null)
			return false;
		return removeRestriction(owl_cls, name, value, modifier);
	}

	public boolean modifyRestriction(String clsName, String name, String value,
			String modifier, String newvalue, String newmodifier) {
		OWLNamedClass owl_cls = getOWLNamedClass(clsName);
		if (owl_cls == null)
			return false;

		boolean retval = removeRestriction(owl_cls, name, value, modifier);
		if (retval == false)
			return retval;
		return addRestriction(owl_cls, name, newvalue, newmodifier);

	}

	public boolean hasAnnotationProperty(OWLNamedClass cls, String slotname) {
		if (cls == null) {
			return false;
		}

		RDFProperty rp = owlModel.getRDFProperty(slotname);

		if (rp == null) {
			logger.warning("Annotation Property" + slotname + " not defined.");
			return false;

		} else {
			return cls.getPropertyValueCount(rp) > 0;
		}

	}
	
	public boolean isValidValue(String[] values, String value) {
		for (int i = 0; i < values.length; i++) {
			if (values[i].equals(value)) {
				return true;
			}
		}
		return false;
	}

	public String[] getAllowableValues(RDFProperty property) {
		boolean rangeDefined = property.isRangeDefined();
		if (!rangeDefined)
			return null;
		boolean includingSuperproperties = false;
		Vector<String> v = new Vector<String>();
		Collection c = property.getRanges(includingSuperproperties);
		if (c == null)
			return null;
		for (Iterator iterator = c.iterator(); iterator.hasNext();) {
			Object value = (Object) iterator.next();

			if (value instanceof DefaultRDFSDatatype) {
				return null;
			} else {
				Collection values = ((OWLDataRange) value).getOneOf()
						.getValues();
				if (values == null)
					return null;
				if (values.size() > 0) {
					for (Iterator iterator2 = values.iterator(); iterator2
							.hasNext();) {
						Object value2 = (Object) iterator2.next();
						v.add(value2.toString());
					}
				}
			}
		}
		Object[] objs = v.toArray();
		String[] allowedvalues = new String[objs.length];

		for (int i = 0; i < objs.length; i++) {
			allowedvalues[i] = (String) objs[i];
		}
		return allowedvalues;
	}

	public boolean isRetired(OWLNamedClass retcls) {
		
		
		return retcls.isDeprecated();
		
	}

	public boolean isPreretired(OWLNamedClass retcls) {
		if (retcls.isSubclassOf(preretiredCls)) {
			return true;
		} else if (getInternalName(retcls).compareTo(
				getInternalName(preretiredCls)) == 0) {
			return true;
		}
		return false;
	}

	public boolean isPremerged(OWLNamedClass cls) {

		if (cls.isSubclassOf(this.premergedCls)) {
			return true;
		} else if (getInternalName(cls)
				.compareTo(getInternalName(premergedCls)) == 0) {
			return true;
		}

		return false;
	}

	// 041306
	public Vector getRestrictionSources(String con_name, String role_name) {
		if (con_name == null || role_name == null)
			return null;
		Vector<OWLNamedClass> v = new Vector<OWLNamedClass>();
		Cls targetCls = owlModel.getOWLNamedClass(con_name);
		RDFResource cls = (RDFResource) targetCls;
		Collection referenceCollection = cls.getReferringAnonymousClasses();
		if (referenceCollection != null) {
			Object[] obs = referenceCollection.toArray();
			for (int i = 0; i < obs.length; i++) {
				OWLAnonymousClass owlcls = (OWLAnonymousClass) obs[i];
				if (owlcls instanceof OWLRestriction) {
					
					String restrication_name = ((OWLRestriction) owlcls)
							.getOnProperty().getBrowserText();

					if (restrication_name.equals(role_name)) {
						Collection subs = owlcls.getSubclasses(false);
						Object[] obs_2 = subs.toArray();
						for (int j = 0; j < obs_2.length; j++) {
							OWLNamedClass owlcls_2 = (OWLNamedClass) obs_2[j];

							if (getInternalName(targetCls).compareTo(
									getInternalName(owlcls_2)) != 0)
								v.add(owlcls_2);
						}
					}
				}
			}
		}
		return v;
	}

	
	
	

	// 041306

	public Vector getRestrictionSources(OWLNamedClass con) {
		if (con == null)
			return null;
		Vector<String> v = new Vector<String>();
		//Cls targetCls = owlModel.getOWLNamedClass(con_name);
		RDFResource cls = (RDFResource) con;
		
		Collection coll = FindUsage.getItems(cls);   //cls.getReferringAnonymousClasses();
		
		Iterator it = coll.iterator();
		while (it.hasNext()) {
			FindUsageTableItem i = (FindUsageTableItem) it.next();
            // ODO: enhance to also support associations
			OWLNamedClass onc = (OWLNamedClass) i.host;
			if (i.usage instanceof OWLRestriction) {
				OWLRestriction r = (OWLRestriction) i.usage;
				RDFProperty p = r.getOnProperty();
				if (p != null) {
					
					String s = r.getFillerText();
					if (s.compareTo(con.getBrowserText()) == 0) {
					String res = p.getPrefixedName() + "|" + onc.getPrefixedName()
					+ "|" + getRestrictionType(r);
			        v.add(res);
					}
				}
			} else if (i.usage instanceof OWLIntersectionClass) {
            	OWLIntersectionClass ointc = (OWLIntersectionClass) i.usage;
            	Collection ops = ointc.getOperands();
            	Iterator itc = ops.iterator();
            	while (itc.hasNext()) {
            		RDFSClass rc = (RDFSClass) itc.next();
            		if (rc instanceof OWLRestriction) {
            			OWLRestriction r = (OWLRestriction) rc;
            			RDFProperty p = r.getOnProperty();
            			Object rv = r.getPropertyValue(p);
        				if (p != null) {
        					String s = r.getFillerText();
        					if (s != null) {
        						if (s.compareTo(con.getBrowserText()) == 0) {
        						String res = p.getPrefixedName() + "|" + onc.getPrefixedName()
        						+ "|" + getRestrictionType(r);
        				        v.add(res);
        						}
        						
        					}
        				}
            		}
            	}
                
            }
		}

		
		/**
		if (referenceCollection != null) {
			Object[] obs = referenceCollection.toArray();
			for (int i = 0; i < obs.length; i++) {
				OWLAnonymousClass owlcls = (OWLAnonymousClass) obs[i];
				if (owlcls instanceof OWLRestriction) {
					OWLRestriction r = (OWLRestriction) owlcls;
					String prop_modifier = getRestrictionType(r);
					RDFProperty p = r.getOnProperty();
					String prop_name = p.getPrefixedName();
					String prop_value = r.getFillerText();
					
					if (prop_value.compareTo(con_name) == 0) {
						Collection subs = owlcls.getSubclasses(false);
						Object[] obs_2 = subs.toArray();
						for (int j = 0; j < obs_2.length; j++) {
							OWLNamedClass owlcls_2 = (OWLNamedClass) obs_2[j];
							if (getInternalName(targetCls).compareTo(
									getInternalName(owlcls_2)) != 0) {
								String s = prop_name + "|" + getInternalName(owlcls_2)
								+ "|" + prop_modifier;
						        v.add(s);
							}
								
						}
						
					}
					
				}
			}
		}
		
		**/
		return v;
	}
    
    public Vector getInverseAssocStrings(OWLNamedClass cls) {
        if (cls == null)
            return null;
        Vector<String> v = new Vector<String>();
        String con_name = getInternalName(cls);

        Collection referringClasses = getReferringClasses(cls);
        Iterator itr = referringClasses.iterator();
        while (itr.hasNext()) {
            OWLNamedClass referringClass = (OWLNamedClass) itr.next();
            String referringClassName = getInternalName(referringClass);
            Collection referenceCollection = referringClass.getRDFProperties();
            if (referenceCollection != null) {
                Object[] obs = referenceCollection.toArray();
                for (int i = 0; i < obs.length; i++) {
                    if (obs[i] instanceof OWLObjectProperty) {
                        OWLObjectProperty op = (OWLObjectProperty) obs[i];
                        if (getInternalName((OWLNamedClass) referringClass.getPropertyValue(op)).equals(con_name)) {
                            String s = op.getPrefixedName() + "|" + referringClassName;
                            v.add(s);
                        }
                    }
                }
            }
        }
        return v;
    }


	// 041306
	public OWLNamedClass getOWLNamedClass(String name) {
		return owlModel.getOWLNamedClass(name);
	}
	
	

	// 041306
	public Vector getRestrictionTargetStrings(OWLNamedClass owl_cls) {
		if (owl_cls == null)
			return null;
		Vector<String> v = new Vector<String>();
		boolean includingSuperclassRestrictions = false;
		java.util.Collection col = owl_cls
				.getRestrictions(includingSuperclassRestrictions);

		if (col != null) {
			final Object[] obs = col.toArray();
			if (obs.length > 0) {
				for (int i = 0; i < obs.length; i++) {
					OWLRestriction r = (OWLRestriction) obs[i];
					if (!(r instanceof OWLNamedClass)) {
						RDFProperty p = r.getOnProperty();
						String prop_name = p.getName();
						String prop_value = r.getFillerText();
						v.add(prop_name + "$" + prop_value);
					}
				}
			}
		}
		return v;
	}

	public boolean addDirectSubclasses(RDFSClass fromCls, RDFSClass toCls) {
		if (fromCls == null) {
			logger.warning("fromCls is null.");
			return false;
		}
		if (toCls == null) {
			logger.warning("toCls is null.");
			return false;
		}
		try {
			Collection subs = fromCls.getSubclasses(false);
			for (Iterator iterator = subs.iterator(); iterator.hasNext();) {
				RDFSClass sub = (RDFSClass) iterator.next();
				addDirectSubclass(toCls, sub);
			}
			return true;
		} catch (Exception e) {
			logger.severe("failed to add subclass " + toCls.getBrowserText());
			logger.severe(e.getLocalizedMessage());
			return false;
		}
	}

	public boolean addDirectSuperclass(RDFSClass hostClass, RDFSClass superCls) {

		if (hostClass == null) {
			logger.warning("hostClass is null.");
			return false;
		}
		if (superCls == null) {
			logger.warning("superCls is null.");
			return false;
		}

		try {
			Collection sups = hostClass.getSuperclasses(false);
			boolean toAdd = true;
			for (Iterator iterator = sups.iterator(); iterator.hasNext();) {
				RDFSClass sup = (RDFSClass) iterator.next();
				if (sup instanceof OWLNamedClass) {
					if (getInternalName(sup).compareTo(
							getInternalName((Cls) superCls)) == 0) {
						toAdd = false;
						break;
					}
				}
			}
			if (toAdd)
				hostClass.addSuperclass(superCls);

			if (hostClass.isSubclassOf(owlModel.getOWLThingClass())) {
				hostClass.removeSuperclass(owlModel.getOWLThingClass());
			}

			return true;
		} catch (Exception e) {
			logger.severe("failed to add parent");
			logger.severe(e.getLocalizedMessage());
			return false;
		}

	}

	public boolean addDirectSubclass(RDFSClass hostClass, RDFSClass subCls) {

		if (hostClass == null) {
			logger.warning("hostClass is null.");
			return false;
		}
		if (subCls == null) {
			logger.warning("subCls is null.");
			return false;
		}

		Collection subs = hostClass.getSubclasses(false);
		boolean toAdd = true;
		for (Iterator iterator = subs.iterator(); iterator.hasNext();) {
			RDFSClass sub = (RDFSClass) iterator.next();
			if (sub instanceof OWLNamedClass) {
				if (getInternalName(sub).compareTo(
						getInternalName((Cls) subCls)) == 0) {
					toAdd = false;
					break;
				}
			}
		}
		if (toAdd) {
			subCls.addSuperclass(hostClass);
		}

		if (subCls.isSubclassOf(owlModel.getOWLThingClass())) {
			subCls.removeSuperclass(owlModel.getOWLThingClass());
		}
		return true;
	}

	public void removeDirectSubclasses(RDFSClass retireCls) {
		Collection subclasses = retireCls.getSubclasses(false);
		if (subclasses != null) {
			final Object[] obs = subclasses.toArray();
			if (obs.length > 0) {
				for (int i = 0; i < obs.length; i++) {
					Cls c = (Cls) obs[i];
					if (c instanceof OWLNamedClass) {
						c.removeDirectSuperclass(retireCls);
					}
				}
			}
		}
	}

	public void redirectReferringClasses(Cls fromCls, Cls toCls)
			throws Exception {

		Collection coll = FindUsage.getItems((RDFResource) fromCls);
		Iterator it = coll.iterator();
		while (it.hasNext()) {
			FindUsageTableItem i = (FindUsageTableItem) it.next();
            // ODO: enhance to also support associations
			OWLNamedClass onc = (OWLNamedClass) i.host;
			if (i.usage instanceof OWLRestriction) {
				OWLRestriction r = (OWLRestriction) i.usage;
				

				RDFProperty p = r.getOnProperty();
				if (p != null) {
					String s = r.getFillerText();
					if (s != null) {
						if (s.compareTo(fromCls.getBrowserText()) == 0) {
							if (this.hasRole(onc, p.getPrefixedName(), ((RDFResource) fromCls).getPrefixedName())
									&& this.hasRole(onc, p.getPrefixedName(), ((RDFResource) toCls)
											.getPrefixedName())) {
								this.removeRestriction(onc, r);
							} else {
								r.setFillerText(getInternalName(toCls));
							}
						}
					}
				}
			} else if (i.usage instanceof OWLObjectProperty) {
                OWLObjectProperty op = (OWLObjectProperty) i.usage;
                                
                OWLNamedClass tcls = (OWLNamedClass) onc.getPropertyValue(op);
                if (tcls.getBrowserText().equals(fromCls.getBrowserText())) {
                    onc.removePropertyValue(op, tcls);
                    onc.addPropertyValue(op, toCls);                    
                }               
               
            } else if (i.usage instanceof OWLIntersectionClass) {
            	OWLIntersectionClass ointc = (OWLIntersectionClass) i.usage;
            	Collection ops = ointc.getOperands();
            	Iterator itc = ops.iterator();
            	while (itc.hasNext()) {
            		RDFSClass rc = (RDFSClass) itc.next();
            		if (rc instanceof OWLRestriction) {
            			OWLRestriction r = (OWLRestriction) rc;
            			RDFProperty p = r.getOnProperty();
        				if (p != null) {
        					String s = r.getFillerText();
        					if (s != null) {
        						// TODO: pick up here tomorrow
        						if (s.compareTo(fromCls.getBrowserText()) == 0) {
        							if (this.hasRole(onc, p.getPrefixedName(), ((RDFResource) fromCls).getPrefixedName())
        									&& this.hasRole(onc, p.getPrefixedName(), ((RDFResource)toCls
        											).getPrefixedName())) {
        								this.removeEquivalentDefinition(onc, r);
        							} else {
        								r.setFillerText(getInternalName(toCls));
        							}
        						}
        					}
        				}
            		}
            	}
                
            }
		}
	}

	

	public boolean isNonEditable(OWLNamedClass owl_cls) {
		if (owl_cls == null)
			return false;

		// String name = owl_cls.getBrowserText();
		String name = getInternalName(owl_cls);
		return noneditable_vec.contains(name);
	}

	public boolean removeObjectProperty(OWLNamedClass owl_cls, String name,
			String value) {
		if (owl_cls == null)
			return false;

		if (name.compareTo(RDFSNames.Slot.IS_DEFINED_BY) == 0) {
			java.util.Collection col = owl_cls.getIsDefinedBy();
			if (col != null) {
				final Object[] obs = col.toArray();
				if (obs.length > 0) {
					for (int i = 0; i < obs.length; i++) {
						RDFResource r = (RDFResource) obs[i];
						if (r instanceof OWLNamedClass) {
							// if (r.getBrowserText().compareTo(value) == 0)
							if (getInternalName((Cls) r).compareTo(value) == 0) {
								owl_cls.removeIsDefinedBy(r);
								return true;
							}

						}
					}
				}
			}
			return false;
		}

		else if (name.compareTo(OWLNames.Slot.SAME_AS) == 0) {
			java.util.Collection col = owl_cls.getSameAs();
			if (col != null) {
				final Object[] obs = col.toArray();
				if (obs.length > 0) {
					for (int i = 0; i < obs.length; i++) {
						RDFResource r = (RDFResource) obs[i];
						if (r instanceof OWLNamedClass) {
							// if (r.getBrowserText().compareTo(value) == 0)
							if (getInternalName((Cls) r).compareTo(value) == 0) {
								owl_cls.removeSameAs(r);
								return true;
							}

						}
					}
				}
			}
			return false;
		}

		else if (name.compareTo(RDFSNames.Slot.SEE_ALSO) == 0) {
			OWLNamedClass slotvalue = owlModel.getOWLNamedClass(value);
			if (slotvalue == null) {
				return false;
			}
			
			OWLObjectProperty slot = owlModel.getOWLObjectProperty(name);
			if (slot != null) {
				owl_cls.removePropertyValue(slot, slotvalue);
				return true;
			}
			return false;
		}

		//
		else if (name.compareTo(OWLNames.Slot.DIFFERENT_FROM) == 0) {
			OWLNamedClass slotvalue = owlModel.getOWLNamedClass(value);
			if (slotvalue == null) {
				return false;
			}
			
			RDFProperty property = owlModel.getOWLDifferentFromProperty();

			
			if (property != null) {
				owl_cls.removePropertyValue(property, slotvalue);
				return true;
			}
			return false;
		}
		
		else if (name.compareTo(owlModel.getOWLEquivalentPropertyProperty()
				.getPrefixedName()) == 0
				|| name.compareTo(owlModel.getOWLDisjointWithProperty()
						.getPrefixedName()) == 0)

		{
			OWLNamedClass slotvalue = owlModel.getOWLNamedClass(value);
			if (slotvalue == null) {
				return false;
			}

			RDFProperty slot = (RDFProperty) owlModel.getRDFProperty(name);
			if (slot != null) {
				owl_cls.removePropertyValue(slot, slotvalue);
				return true;
			}
			return false;
		}

		else {
			OWLNamedClass slotvalue = owlModel.getOWLNamedClass(value);
			if (slotvalue == null) {
				return false;
			}

			RDFProperty slot = null;

			Collection allProperties = owlModel
					.getVisibleUserDefinedRDFProperties();
			
			for (Iterator it = allProperties.iterator(); it.hasNext();) {
				RDFProperty property = (RDFProperty) it.next();
				if (property.isAnnotationProperty()
						&& property.hasObjectRange()
						&& property instanceof DefaultOWLObjectProperty) {
					if (property.getPrefixedName().compareTo(name) == 0) {
						slot = property;
					}
				}
			}
			if (slot == null) {
				logger.warning("Protege.Slot: " + name + " not found.");
				return false;
			} else {
				logger.info("removeObjectProperty slotvalue: "
						+ slotvalue.getBrowserText());
			}
			logger.info("Calling owlModel.removeOwnSlotValue slot "
					+ slot.getBrowserText());
			owl_cls.removePropertyValue(slot, slotvalue);
			logger.info("Exit owlModel.removeOwnSlotValue slot "
					+ slot.getBrowserText());

		}
		return true;
	}

	public edu.stanford.smi.protege.model.Cls getMetaClsByName(String name) {
		edu.stanford.smi.protege.model.Cls[] metaClses = ProfilesManager
				.getSupportedRestrictionMetaClses(owlModel);
		for (int i = 0; i < metaClses.length; i++) {
			edu.stanford.smi.protege.model.Cls cls = metaClses[i];
			if (cls.getBrowserText().compareTo(name) == 0)
				return cls;
		}
		return null;
	}

	private Vector getPropertiesList() {
		Vector<RDFProperty> v = new Vector<RDFProperty>();
		try {
			if (owlModel == null) {
				logger.warning("WARNING: owlModel == null");
				return v;
			}

			Collection allProperties = owlModel
					.getVisibleUserDefinedRDFProperties();
			for (Iterator it = allProperties.iterator(); it.hasNext();) {
				RDFProperty property = (RDFProperty) it.next();
				if (!property.isAnnotationProperty()) {
					v.add(property);
				}
			}
		} catch (Exception e) {
			logger.warning("Exception caught" + e.getLocalizedMessage());
		}

		return v;
	}

	public boolean addRestriction(OWLNamedClass owl_cls, String name,
			String value, String modifier) {
		if (getCls(value) == null) {
			try {
				return createOWLRestriction(owl_cls, modifier, name, value);
			} catch (Exception e) {
				System.out
						.println("Exception thrown in OWLWrapper addRestriction.");
				return false;
			}
		}

		Collection allProperties = owlModel
				.getVisibleUserDefinedRDFProperties();
		RDFProperty property = null;
		for (Iterator it = allProperties.iterator(); it.hasNext();) {
			RDFProperty prop = (RDFProperty) it.next();
			if (!prop.isAnnotationProperty()) {
				// if (name.compareTo(property.getBrowserText()) == 0)
				if (name.compareTo(prop.getPrefixedName()) == 0) {
					property = prop;
					break;
				}
			}
		}

		if (property == null) {
			System.out
					.println("WARNING: addRestriction -- cannot find property");
			return false;

		}

		OWLNamedClass target_cls = (OWLNamedClass) getCls(value);
		if (target_cls == null) {
			logger.warning("WARNING: class " + value + " not found,");
			return false;
		}

		if (modifier.compareToIgnoreCase("all") == 0
				|| modifier.compareToIgnoreCase("only") == 0) {
			// OWLNamedClass target_cls = owlModel.getOWLNamedClass(value);

			owl_cls.addSuperclass(owlModel.createOWLAllValuesFrom(property,
					target_cls));
			return true;
		} else if (modifier.compareToIgnoreCase("some") == 0) {
			// OWLNamedClass target_cls = owlModel.getOWLNamedClass(value);

			owl_cls.addSuperclass(owlModel.createOWLSomeValuesFrom(property,
					target_cls));
			return true;
		} else if (modifier.compareToIgnoreCase("has") == 0) {
			// OWLNamedClass target_cls = owlModel.getOWLNamedClass(value);

			owl_cls.addSuperclass(owlModel.createOWLHasValue(property,
					target_cls));
			return true;
		} else if (modifier.compareToIgnoreCase("exactly") == 0) {
			int intValue = Integer.parseInt(value);
			owl_cls.addSuperclass(owlModel.createOWLCardinality(property,
					intValue));
			return true;
		} else if (modifier.compareToIgnoreCase("min") == 0) {
			int intValue = Integer.parseInt(value);
			owl_cls.addSuperclass(owlModel.createOWLMinCardinality(property,
					intValue));
			return true;
		} else if (modifier.compareToIgnoreCase("max") == 0) {
			int intValue = Integer.parseInt(value);
			owl_cls.addSuperclass(owlModel.createOWLMaxCardinality(property,
					intValue));
			return true;
		}
		return false;
	}

	public OWLRestriction createOWLRestriction(String metaClsName, // modifier
			// name
			String propertyName, // role name
			String text) throws Exception // filler
	{
		edu.stanford.smi.protege.model.Cls[] metaClses = ProfilesManager
				.getSupportedRestrictionMetaClses(owlModel);

		edu.stanford.smi.protege.model.Cls metaCls = null;

		String metaCls_name = restrictionType2Label(metaClsName);
		for (int i = 0; i < metaClses.length; i++) {
			if (metaClses[i].getBrowserText().compareTo(metaCls_name) == 0) {
				metaCls = metaClses[i];
				break;
			}
		}

		if (metaCls == null) {
			return null;
		}

		RDFProperty property = null;
		Vector properties = getPropertiesList();
		for (int i = 0; i < properties.size(); i++) {
			RDFProperty p = (RDFProperty) properties.elementAt(i);
			// if (p.getBrowserText().compareTo(propertyName) == 0)
			if (p.getName().compareTo(propertyName) == 0) {
				property = p;
				break;
			}
		}
		if (property == null) {
			logger.warning("property " + propertyName
					+ " not found -- return null");
			return null;
		}

		Collection parents = CollectionUtilities
				.createCollection(((KnowledgeBase) owlModel)
						.getCls(OWLNames.Cls.ANONYMOUS_ROOT));
		KnowledgeBase kb = owlModel;
		OWLRestriction restriction = (OWLRestriction) kb.createCls(null,
				parents, metaCls);
		restriction.setOnProperty(property);
		restriction.setFillerText(text);

		return restriction;
	}

	public boolean createOWLRestriction(OWLNamedClass owl_cls,
			String metaClsName, // abbrev modifier
			String propertyName, // role name
			String text) throws Exception // filler
	{
		edu.stanford.smi.protege.model.Cls[] metaClses = ProfilesManager
				.getSupportedRestrictionMetaClses(owlModel);

		edu.stanford.smi.protege.model.Cls metaCls = null;

		String s = restrictionType2Label(metaClsName);

		for (int i = 0; i < metaClses.length; i++) {
			if (metaClses[i].getBrowserText().compareTo(s) == 0) {
				metaCls = metaClses[i];
				break;
			}
		}

		if (metaCls == null) {
			return false;
		}

		RDFProperty property = null;
		Vector properties = getPropertiesList();
		for (int i = 0; i < properties.size(); i++) {
			RDFProperty p = (RDFProperty) properties.elementAt(i);
			// if (p.getBrowserText().compareTo(propertyName) == 0)
			if (p.getPrefixedName().compareTo(propertyName) == 0) {
				property = p;
				break;
			}
		}
		if (property == null) {
			return false;
		}

		Collection parents = CollectionUtilities
				.createCollection(((KnowledgeBase) owlModel)
						.getCls(OWLNames.Cls.ANONYMOUS_ROOT));
		KnowledgeBase kb = owlModel;
		OWLRestriction restriction = (OWLRestriction) kb.createCls(null,
				parents, metaCls);
		restriction.setOnProperty(property);
		restriction.setFillerText(text);

		owl_cls.addSuperclass(restriction);

		return true;
	}

	public String getPropertyValue(Cls cls, String propname) {
		if (cls == null)
			return null;
		
		//String pn = this.byCode() ? name2Code.get(propname) : propname;
		RDFProperty slot = owlModel.getRDFProperty(propname);

		if (slot == null)
			return null;

		Object res = ((OWLNamedClass) cls).getPropertyValue(slot);

		return (String) res;

	}
	
	public ArrayList<String> getPropertyValues(OWLNamedClass cls, String id) {
		//String pn = this.byCode() ? name2Code.get(name) : name;
	    RDFProperty p = owlModel.getRDFProperty(id);
	    Collection coll = cls.getPropertyValues(p);
	    ArrayList<String> res = new ArrayList<String>(coll.size());
	    Iterator it = coll.iterator();
	    while (it.hasNext()) {
	        res.add((String) it.next());
	    }
	    return res;
	}

	public String getInternalName(Cls cls) {

		if (cls == null) {
			return null;
		} else {
			return ((OWLNamedClass) cls).getPrefixedName();
			// return cls.getBrowserText();
		}

	}
	
	public OWLNamedClass createClsByName(String name, String pt, Cls supCls,
			String def) {
		return createClsNEW(name, pt, supCls, def);
		
	}
	
	public OWLNamedClass createClsByCode(String pt, Cls supCls,
			String def) {
		
		return createClsNEWCode(pt, supCls, def);
		
	}

	public OWLNamedClass createCls(String name, String pt, Cls supCls,
			String def) {
		
		return createCls(name, pt, supCls, def, true);
	}

	public OWLNamedClass createCls(String name, String pt, String sup) {
		Cls supCls = getCls(sup);
		if (supCls == null)
			return null;
		return createCls(name, pt, supCls, null);

	}

	public OWLNamedClass createCls(String name, boolean generatecode) {
		return createCls(name, null, null, null, generatecode);
		
	}

	public String getCode() {

		RemoteCodeGen rcg = new RemoteCodeGen(this.default_kb);

		// try to get a code three times, to allow for network glitches
		for (int i = 0; i < 3; i++) {
			try {
				return (String) rcg.execute();
			} catch (Exception e) {
				logger.warning("Unable to get code frome server because "
						+ e.getLocalizedMessage());

			}
		}

		// if unsuccessful after three times return nocode
		return NOCODE;

	}

	private OWLNamedClass createCls(String name, String pt, Cls supCls,
			String def, boolean generatecode) {

		//System.out.println("creating class: " + name + " " + pt);

		OWLNamedClass cls = null;
		String code = "";

		if (generatecode) {

			code = getCode();

		}

		if (byCode()) {
			if (code == null || code.compareTo("0") == 0) {
				System.out
						.println("Unable to create class -- code generator not available.");
				return null;
			}
		}

		try {
			owlModel.beginTransaction("Create class " + name, name);

			if (supCls != null) {
				cls = owlModel.createOWLNamedSubclass(name,
						(OWLNamedClass) supCls);
				//System.out.println("created subclass");
			} else {
				cls = owlModel.createOWLNamedClass(name);
			}

			// cls = owlModel.createOWLNamedClass(name);
			if (generatecode) {
				addAnnotationProperty(cls, codeSlotName, code);
			}

			if (generatecode) {
				
				if (pt == null) {
					addAnnotationProperty(cls, "rdfs:label", name);
				} else {
					addAnnotationProperty(cls, "rdfs:label", pt);
				}
			}

			String term_group = "PT";
			String source = "NCI";

			if (pt != null && pt.compareTo("") != 0) {
				addAnnotationProperty(cls, NCIEditTab.PREFLABEL, pt);
				
				addAnnotationProperty(cls, NCIEditTab.ALTLABEL, owlModel.createRDFSLiteral(formatFULL_SYN(
						pt, term_group, source), owlModel.getSystemFrames().getXmlLiteralType()));
			}
			if (def != null && def.compareTo("") != 0) {
				addAnnotationProperty(cls, NCIEditTab.DEFINITION,
						owlModel.createRDFSLiteral(formatDEFINITION(source, def),
									owlModel.getSystemFrames().getXmlLiteralType()));
			}
			owlModel.commitTransaction();
		} catch (Exception e) {
			owlModel.rollbackTransaction();
			System.out.println("create failed");
			e.printStackTrace();
			
			return null;
		}
		return cls;
	}
	
	private OWLNamedClass createClsNEW(String name, String pt, Cls supCls,
			String def) {

		logger.fine("creating class: " + name + " " + pt);

		OWLNamedClass cls = null;
		
		String code = getCode();

		
		
			if (code == null || code.compareTo("0") == 0) {
				System.out
						.println("Unable to create class -- code generator not available.");
				return null;
			}
		

		try {
			owlModel.beginTransaction("Create class " + name, name);

			if (supCls != null) {
				cls = owlModel.createOWLNamedSubclass(name,
						(OWLNamedClass) supCls);
			} else {
				cls = owlModel.createOWLNamedClass(name);
			}

			
				addAnnotationProperty(cls, codeSlotName, code);
			

			
				
				if (pt == null) {
					addAnnotationProperty(cls, "rdfs:label", name);
				} else {
					addAnnotationProperty(cls, "rdfs:label", pt);
				}
			

			String term_group = "PT";
			String source = "NCI";

			if (pt != null && pt.compareTo("") != 0) {
				addAnnotationProperty(cls, NCIEditTab.PREFLABEL, pt);
				
				addAnnotationProperty(cls, NCIEditTab.ALTLABEL, owlModel.createRDFSLiteral(formatFULL_SYN(
						pt, term_group, source), owlModel.getSystemFrames().getXmlLiteralType()));
			}
			if (def != null && def.compareTo("") != 0) {
				addAnnotationProperty(cls, NCIEditTab.DEFINITION,
						owlModel.createRDFSLiteral(formatDEFINITION(source, def),
									owlModel.getSystemFrames().getXmlLiteralType()));
			}
			owlModel.commitTransaction();
		} catch (Exception e) {
			owlModel.rollbackTransaction();
			
			return null;
		}
		return cls;
	}
	
	private OWLNamedClass createClsNEWCode(String pt, Cls supCls,
			String def) {

		OWLNamedClass cls = null;
		
		String code = getCode();

		
		
			if (code == null || code.compareTo("0") == 0) {
				System.out
						.println("Unable to create class -- code generator not available.");
				return null;
			}
		

		try {
			owlModel.beginTransaction("Create class " + code, code);

			if (supCls != null) {
				cls = owlModel.createOWLNamedSubclass(code,
						(OWLNamedClass) supCls);
			} else {
				cls = owlModel.createOWLNamedClass(code);
			}

			
				
			

			
				
				if (pt == null) {
					addAnnotationProperty(cls, "rdfs:label", code);
				} else {
					addAnnotationProperty(cls, "rdfs:label", pt);
				}
			

			String term_group = "PT";
			String source = "NCI";

			if (pt != null && pt.compareTo("") != 0) {
				addAnnotationProperty(cls, NCIEditTab.PREFLABEL, pt);
				addAnnotationProperty(cls, NCIEditTab.ALTLABEL, owlModel.createRDFSLiteral(formatFULL_SYN(
						pt, term_group, source),
						owlModel.getSystemFrames().getXmlLiteralType()));
			}
			if (def != null && def.compareTo("") != 0) {
				addAnnotationProperty(cls, NCIEditTab.DEFINITION,
						owlModel.createRDFSLiteral(formatDEFINITION(source, def),
								owlModel.getSystemFrames().getXmlLiteralType()));
			}
			owlModel.commitTransaction();
		} catch (Exception e) {
			owlModel.rollbackTransaction();
			// OWLUI.handleError(owlModel, e);
			// throw new RuntimeException(e);
			return null;
		}
		return cls;
	}

	
	public void deleteOWLNamedClass(OWLNamedClass c) {
		c.delete();
	}

	public boolean printable(String line) {
		if (line.contains(":DIRECT-SUPERCLASSES"))
			return false;
		if (line.contains(":DIRECT-TYPE"))
			return false;
		if (line.contains("@_"))
			return false;
		if (line.contains("rdf:type"))
			return false;

		return true;
	}

	public OWLNamedClass getClsByLabel(String label) {
		Slot slot = owlModel.getRDFProperty("rdfs:label");
		Collection c = owlModel.getMatchingResources((RDFProperty) slot, label,
				1);
		if (c.isEmpty())
			return null;

		Iterator it = c.iterator();
		while (it.hasNext()) {
			OWLNamedClass cls = (OWLNamedClass) it.next();
			return cls;
		}
		return null;
	}

	public Cls getCls(String name) {
		try {
			return owlModel.getRDFSNamedClass(name);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public RDFProperty getRDFProperty(String propname) {
		try {
			//String pn = byCode() ? name2Code.get(propname) : propname;
			RDFProperty prop = owlModel.getRDFProperty(propname);

			return prop;
		} catch (Exception e) {
			logger.warning("RDFProperty " + propname + " does not exist.");
			return null;
		}

	}
	
	public RDFProperty getRDFPropertyKluge(String propname) {
		try {
			
			RDFProperty prop = owlModel.getRDFProperty(propname);

			return prop;
		} catch (Exception e) {
			logger.warning("RDFProperty " + propname + " does not exist.");
			return null;
		}

	}


	public void flushEvents() {

		default_kb.flushEvents();
	}

	public String getCode(Cls cls) {
		if (cls == null)
			return null;
		RDFProperty codeSlot = getRDFProperty(codeSlotName);
		if (codeSlot != null) {

			return this.getPropertyValue(cls, codeSlotName);

		} else {
			return ((OWLNamedClass) cls).getPrefixedName();
		}

		
	}

	public String getObjectValue(Object obj) {
		if (obj == null)
			return null;
		Object newValue;
		String str = obj.toString();
		if (obj instanceof RDFSLiteral) {
			RDFSLiteral oldLiteral = (RDFSLiteral) obj;
			newValue = owlModel
					.createRDFSLiteral(str, oldLiteral.getDatatype());
		} else if (obj instanceof Boolean) {
			newValue = Boolean.valueOf(str.equals("true"));
		} else if (obj instanceof Float) {
			newValue = Float.valueOf(str);
		} else if (obj instanceof Integer) {
			newValue = Integer.valueOf(str);
		} else {
			newValue = str;
		}
		return newValue.toString();
	}

	public boolean isReadOnlyProperty(String name) {
		if (readOnlyProperties.contains(name)) {
			return true;
		}
		RDFResource p = owlModel.getRDFResource(name);
		if (p instanceof RDFProperty && ((RDFProperty) p).isReadOnly()) {
			return true;
		}
		return false;
	}

	public Vector getReadOnlyProperties() {
		return readOnlyProperties;
	}

	// ///////////////////////////////
	public Vector getNamedSubclassNames(Cls cls) {
		Vector<String> v = new Vector<String>();
		if (cls == null)
			return v;
		Collection subClassCollection = cls.getDirectSubclasses();
		if (subClassCollection != null) {
			final Object[] obs = subClassCollection.toArray();
			for (int i = 0; i < obs.length; i++) {
				Cls c = (Cls) obs[i];
				if (c instanceof OWLNamedClass) {
					// v.add(c.getBrowserText());
					v.add(getInternalName(c));
				}
			}
		}
		return v;
	}

	public Collection getDirectSubclasses(Cls superCls) {
		return superCls.getDirectSubclasses();
	}

	public QuickSortVecStrings getSortUtility() {
		return OWLWrapper.sort;
	}

	public String convertObjecttoString(Object obj, ValueType type) {
		if (type.equals(ValueType.STRING)) {
			String s = (String) obj;
			if (s.startsWith(DefaultRDFSLiteral.DATATYPE_PREFIX)) {
				DefaultRDFSLiteral literal = new DefaultRDFSLiteral(owlModel, s);
		        return literal.getString();
			} else {
				return obj.toString();
			}
		} else if (type.equals(ValueType.FLOAT))
			return obj.toString();
		else if (type.equals(ValueType.INTEGER))
			return obj.toString();
		else if (type.equals(ValueType.ANY))
			return obj.toString();
		else if (type.equals(ValueType.BOOLEAN))
			return obj.toString();
		else if (type.equals(ValueType.CLS)) {
			Cls cls = (Cls) obj;
			return cls.getName();
		} else if (type.equals(ValueType.INSTANCE)) {
			Instance inst = (Instance) obj;
			return inst.getName();
			// return inst.getName()+"("+inst.getDirectType().getName()+")";
		} else if (type.equals(ValueType.SYMBOL))
			return obj.toString();
		else
			return "Unknown";
	}

	public ValueType getObjectValueType(Object obj) {
		if (obj instanceof Boolean)
			return ValueType.BOOLEAN;
		else if (obj instanceof Cls)
			return ValueType.CLS;
		else if (obj instanceof Float)
			return ValueType.FLOAT;
		else if (obj instanceof Instance)
			return ValueType.INSTANCE;
		else if (obj instanceof Integer)
			return ValueType.INTEGER;
		else if (obj instanceof String)
			return ValueType.STRING;
		else
			return ValueType.ANY;
	}

	public Collection getNamedSubclasses(OWLNamedClass cls) {
		Set<OWLNamedClass> hashset = new HashSet<OWLNamedClass>();
		if (cls == null)
			return hashset;
		Collection subClassCollection = cls.getSubclasses(false);
		if (subClassCollection != null) {
			final Object[] obs = subClassCollection.toArray();
			for (int i = 0; i < obs.length; i++) {
				Cls c = (Cls) obs[i];
				if (c instanceof OWLNamedClass) {
					hashset.add((OWLNamedClass) c);
				}
			}
		}
		return hashset;
	}

	public Collection getNamedSuperclasses(Cls cls) {
		Set<OWLNamedClass> hashset = new HashSet<OWLNamedClass>();
		if (cls == null)
			return hashset;
		Collection subClassCollection = cls.getDirectSuperclasses();
		if (subClassCollection != null) {
			final Object[] obs = subClassCollection.toArray();
			for (int i = 0; i < obs.length; i++) {
				Cls c = (Cls) obs[i];
				if (c instanceof OWLNamedClass) {
					hashset.add((OWLNamedClass) c);
				}
			}
		}
		return hashset;
	}

	public Collection getReferringClasses(Cls cls) {
		Set hashset = new HashSet();
		if (cls == null)
			return hashset;

		Collection coll = FindUsage.getItems((RDFResource) cls);
		Iterator it = coll.iterator();
		while (it.hasNext()) {
			FindUsageTableItem i = (FindUsageTableItem) it.next();

			hashset.add((OWLNamedClass) (i.host));
		}
		return hashset;

	}

	public boolean addAnnotationProperty(OWLNamedClass cls,
			String propertyname, String propertyvalue) {
		
		if (config.getComplexProperties().contains(propertyname)) {
			return this.addAnnotationProperty(cls, propertyname, owlModel.createRDFSLiteral(propertyvalue,
					owlModel.getSystemFrames().getXmlLiteralType()));
		}
		
		if (cls == null) {
			return false;
		}

		RDFProperty prop = owlModel.getRDFProperty(propertyname);

		if (prop == null) {

			logger.warning("Unable to add property -- " + propertyname
					+ " not defined.");
			return false;

		} else {
			cls.addPropertyValue(prop, propertyvalue);
		}

		return true;

	}
	
	public boolean addAnnotationProperty(OWLNamedClass cls,
			String propertyname, Object propertyvalue) {
		if (cls == null) {
			return false;
		}

		RDFProperty prop = owlModel.getRDFProperty(propertyname);

		if (prop == null) {

			logger.warning("Unable to add property -- " + propertyname
					+ " not defined.");
			return false;

		} else {
			cls.addPropertyValue(prop, propertyvalue);
		}

		return true;

	}

	// for association (object valued annotation property
	public boolean addAnnotationProperty(OWLNamedClass cls,
			String propertyname, OWLNamedClass propertyvalue) {
		if (cls == null)
			return false;

		Collection allProperties = owlModel
				.getVisibleUserDefinedRDFProperties();

		allProperties.add(owlModel.getOWLDifferentFromProperty());
		allProperties.add(owlModel.getOWLDisjointWithProperty());
		allProperties.add(owlModel.getOWLEquivalentPropertyProperty());
		allProperties.add(owlModel.getOWLSameAsProperty());
		allProperties.add(owlModel.getRDFSIsDefinedByProperty());
		allProperties.add(owlModel.getRDFProperty(RDFSNames.Slot.SEE_ALSO));

		RDFProperty property = null;

		for (Iterator it = allProperties.iterator(); it.hasNext();) {
			property = (RDFProperty) it.next();
			if (property.isAnnotationProperty() && property.hasObjectRange()
					&& property instanceof DefaultOWLObjectProperty) {

				// if (propertyname.compareTo(property.getBrowserText()) == 0)
				if (propertyname.compareTo(property.getName()) == 0) {
					break;
				}
			}
		}

		if (property == null) {
			System.out
					.println("WARNING: addRestriction -- cannot find property");
			return false;

		}

		cls.addPropertyValue(property, propertyvalue);
		return true;
	}

	public boolean isEditable(String cls_name) {
		if (noneditable_vec.contains(cls_name))
			return false;
		return true;
	}

	private OWLNamedClass retiredCls = null;

	private OWLNamedClass preretiredCls = null;

	private OWLNamedClass premergedCls = null;

	private OWLNamedClass thing = null;

	private ArrayList<Cls> selectableRoots = null;

	public Collection getSelectableRoots() {
		if (selectableRoots == null) {
			Collection roots = thing.getSubclasses(false);
			
			selectableRoots = new ArrayList<Cls>();
			selectableRoots.addAll(roots);

			selectableRoots.remove(retiredCls);
			selectableRoots.remove(premergedCls);
			selectableRoots.remove(preretiredCls);
			Cls[] owlcs = new Cls[selectableRoots.size()];
			Arrays.sort(selectableRoots.toArray(owlcs), new FrameComparator());
			
			selectableRoots = new ArrayList<Cls>();
			for (int i = 0; i < owlcs.length; i++) {
				selectableRoots.add(owlcs[i]);
			}
			
		}
		return selectableRoots;
	}

	public void initRoots() {

		retiredCls = (OWLNamedClass) owlModel
				.getOWLNamedClass(NCIEditTab.RETIRED_CONCEPTS);

		preretiredCls = (OWLNamedClass) owlModel
				.getOWLNamedClass(NCIEditTab.PRERETIRED_CONCEPTS);

		premergedCls = (OWLNamedClass) owlModel
				.getOWLNamedClass(NCIEditTab.PREMERGED_CONCEPTS);

		thing = owlModel.getOWLThingClass();

	}

	public static long OWLCNT = 0;

	public boolean isEditable(OWLNamedClass cls) {
		if (cls.equals(thing) || cls.equals(retiredCls)
				|| cls.equals(preretiredCls) || cls.equals(premergedCls)) {
			return false;
		} else {
			return true;
		}
	}


	public Cls getFrameNameMatches(String name) {
		Collection c = default_kb.getFrameNameMatches(name, 1);

		for (Iterator it = c.iterator(); it.hasNext();) {
			RDFSClass cls = (RDFSClass) it.next();
			return (Cls) cls;
		}

		return null;
	}

	public RDFSClass parsableText2RDFSClass(String parsableText) {
		RDFSClass newRestriction = null;
		try {
			OWLClassParser parser = owlModel.getOWLClassDisplay().getParser();
			newRestriction = parser.parseClass(owlModel, parsableText);
			return newRestriction;
		} catch (OWLClassParseException e) {
			ProtegeUI.getModalDialogFactory().showErrorMessageDialog(owlModel,
					"Error at parsing restriction: " + parsableText,
					"Parse Error");
			return null;
		}
	}

	public RDFSClass getEquivalentClass(OWLNamedClass hostClass) {

		return hostClass.getDefinition();
	}

	public boolean containsDefinition(OWLNamedClass hostClass, RDFSClass aClass) {
		if (aClass == null)
			return false;

		RDFSClass definition = null;
		Collection c = hostClass.getEquivalentClasses();

		if (c.size() == 0)
			return false;

		Iterator it = c.iterator();
		while (it.hasNext()) {
			definition = (RDFSClass) it.next();
			if (definition.getBrowserText().compareTo(aClass.getBrowserText()) == 0) {
				return true;
			}
		}

		if (definition instanceof OWLIntersectionClass) {
			OWLIntersectionClass intersectionCls = (OWLIntersectionClass) definition;
			if (intersectionCls.hasOperandWithBrowserText(aClass
					.getBrowserText()))
				return true;
		}
		return false;
	}

	public boolean removeEquivalentClass(OWLNamedClass hostClass,
			RDFSClass aClass) {
		try {
			if (!containsDefinition(hostClass, aClass))
				return true; // Nothing done

			hostClass.removeEquivalentClass(aClass);

			// added by tt
			// hanldes the case that the class does not have any parent any more
			if (hostClass.getNamedSuperclasses(false).size() == 0)
				hostClass.addSuperclass(owlModel.getOWLThingClass());
			// end added by tt

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean addEquivalentClass(OWLNamedClass hostClass, RDFSClass aClass) {
		try {
			if (containsDefinition(hostClass, aClass))
				return true; // Nothing done
			hostClass.addEquivalentClass(aClass);

			// Note (GF13060 Incorrect assignment of owl:thing as a parent):
			// When the "defining" flag is checked in the parent concept,
			// the following property is changed in the TreePanel:
			// * "rdfs:subClassOf: [PARENT_CONCEPT]"
			// to * "owl:equivalentClass: [PARENT_CONCEPT]".
			// Since the subClassOf item is no longer display while
			// copying (or cloning) a concept, the addDirectSuperclass
			// method is not called (which handles removing the owl:Thing
			// parent) instead this method (addEquivalentClass) is called.
			if (hostClass.isSubclassOf(owlModel.getOWLThingClass()))
				hostClass.removeSuperclass(owlModel.getOWLThingClass());
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean addDirectSuperclass(Cls subCls, Cls supCls) {
		return addDirectSuperclass((RDFSClass) subCls, (RDFSClass) supCls);
	}

	public boolean removeDirectSuperclass(Cls sub, Cls sup) {
		long beg = System.currentTimeMillis();
		String browserText = sup.getBrowserText();
		String owlThing = owlModel.getOWLThingClass().getBrowserText();

		if (browserText.compareTo(owlThing) == 0) {
			logger.warning("Cannot delete owlThing");
			return false;
		}

		OWLNamedClass subCls = (OWLNamedClass) sub;
		if (subCls == null) {
			logger.warning("Unable to find " + sub);
			return false;
		}
		OWLNamedClass supCls = (OWLNamedClass) sup;
		if (supCls == null) {
			logger.warning("Unable to find " + sup);
			return false;
		}

		subCls.removeDirectSuperclass(supCls);
		logger.fine("local call took " + (System.currentTimeMillis() - beg));

		return true;
	}

	/**
	 * public boolean removeDirectSuperclassRemote(Cls sub, Cls sup) { long beg =
	 * System.currentTimeMillis(); RemoteOWLWrapper rowl = new RemoteOWLWrapper(
	 * (KnowledgeBase) default_kb, (OWLNamedClass) sub, (OWLNamedClass) sup);
	 * Boolean res = (Boolean) rowl.run(); System.out.println("remot call took " +
	 * (System.currentTimeMillis() - beg)); return res.booleanValue(); }
	 */
	public boolean addRestriction(RDFSClass hostClass, RDFSClass superCls) {

		if (hostClass == null) {
			logger.warning("hostClass is null.");
			return false;
		}
		if (superCls == null) {
			logger.warning("superCls is null.");
			return false;
		}

		try {

			hostClass.addSuperclass(superCls);			
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	public boolean removeRestriction(OWLNamedClass hostClass, RDFSClass r) {
		if (hostClass == null)
			return false;
		try {
			hostClass.removeSuperclass(r);
			r.delete();

			return true;
		} catch (Exception e) {
			// 102206, KLO ???
			// Log.getLogger().log(Level.WARNING, "Exception caught", e);;
			logger.warning("RDFSClass delete method throws exception.");
		}
		return false;
	}

	public boolean canRemoveEquivalentClass(OWLNamedClass hostClass,
			RDFSClass aClass) {
		if (aClass instanceof OWLNamedClass)
			return true;
		RDFSClass definition = getEquivalentClass(hostClass);
		if (!(definition instanceof OWLIntersectionClass)) {
			return true;
		}
		OWLIntersectionClass intersectionClass = (OWLIntersectionClass) definition;
		if (!(intersectionClass.hasOperandWithBrowserText(aClass
				.getBrowserText()))) {
			return false;
		}

		return true;
	}

	public Object createNewValue(RDFProperty property, String text,
			String language) {
		Object newValue = null;
		if (language == null || language.trim().length() == 0) {
			newValue = text;
		} else {
			newValue = property.getOWLModel().createRDFSLiteralOrString(text,
					language);
		}
		return newValue;
	}

	public Object createNewValue(String text, String language) {
		Object newValue = null;
		if (language == null || language.trim().length() == 0) {
			newValue = text;
		} else {
			newValue = owlModel.createRDFSLiteralOrString(text, language);
		}
		return newValue;
	}

	public boolean resetLanguage(OWLNamedClass hostClass, String propertyName,
			String propertyvalue, String newLanguage) {
		if (hostClass == null)
			return false;
		if (propertyName == null)
			return false;

		try {
			RDFResource subject = (RDFResource) hostClass;
			RDFProperty property = getRDFProperty(propertyName);
			if (property == null)
				return false;

			Collection c = subject.getPropertyValues(property);
			if (c == null || c.size() == 0) {
				return false;
			}

			Iterator it = c.iterator();
			while (it.hasNext()) {
				Object value = it.next();
				if (value instanceof RDFSLiteral) {
					RDFSLiteral oldValue = (RDFSLiteral) value;
					if (oldValue.getString().compareTo(propertyvalue) == 0) {
						Object newValue = createNewValue(property,
								propertyvalue, newLanguage);
						subject.removePropertyValue(property, oldValue);

						// 122006
						/*
						 * RDFSLiteral newLiteral = (RDFSLiteral) newValue;
						 * subject.addPropertyValue(property, newLiteral);
						 */
						subject.addPropertyValue(property, newValue);

						break;
					}
				} else { // String type
					String oldValue = (String) value;
					if (oldValue.compareTo(propertyvalue) == 0) {
						subject.removePropertyValue(property, oldValue);
						Object newValue = createNewValue(property,
								propertyvalue, newLanguage);

						subject.addPropertyValue(property, newValue);
						break;
					}
				}
			}

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void countProperties(OWLNamedClass subject, String propertyname,
			String label) {
		if (subject == null)
			return;
		logger.info("\n====" + label + " ====");
		logger.info("propertyname " + propertyname);

		RDFProperty property = getRDFProperty(propertyname);
		Collection c = subject.getPropertyValues(property);
		logger.info("Number of " + property.getBrowserText() + " : " + c.size()
				+ "\n");
		logger.info("========\n");
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Annotation Property

	private boolean createAnnotationProperty(OWLNamedClass cls,
			String propertyname, String propertyvalue) {
		if (cls == null)
			return false;
		RDFProperty slot = owlModel.getRDFProperty(propertyname);
		if (slot != null) {
			cls.addPropertyValue(slot, propertyvalue);
		}
		return true;
	}

	public boolean addAnnotationProperty(String name, String slotname,
			String slotvalue) {
		boolean conceptexists = conceptExists(name);
		if (!conceptexists)
			return false;
		OWLNamedClass cls = owlModel.getOWLNamedClass(name);
		RDFProperty slot = owlModel.getRDFProperty(slotname);
		if (slot == null)
			return false;

		cls.addPropertyValue(slot, slotvalue);
		return true;
	}

	/**
	 * public boolean addAnnotationProperty(Cls cls, String slotname, String
	 * slotvalue) { return this.addAnnotationProperty(((OWLNamedClass)
	 * cls).getLocalName(), slotname, slotvalue); }
	 * 
	 */
	public boolean removeAnnotationProperty(OWLNamedClass cls,
			String propertyname, String propertyvalue) {
		if (cls == null)
			return false;

		RDFProperty prop = getRDFProperty(propertyname);

		if (prop != null) {
			// 101006
			// owlModel.removeOwnSlotValue(cls, slot, propertyvalue);
			// RDFProperty prop = (RDFProperty) slot;
			Collection c = cls.getPropertyValues(prop);
			if (c == null || c.size() == 0)
				return true;

			//propertyvalue = ComplexPropertyParser.reformatComplexProperty(
					//propertyname, propertyvalue);

			Iterator it = cls.getPropertyValues(prop).iterator();
			while (it.hasNext()) {
				// String val = (String) it.next();
				Object obj = (Object) it.next();
				String val = getObjectValue(obj);
				////val = ComplexPropertyParser.reformatComplexProperty(
						//propertyname, val);

				if (val.equals(propertyvalue)) {
					cls.removePropertyValue(prop, obj);
					return true;
				}
			}
		}
		return false;
	}

	public boolean addObjectProperty(OWLNamedClass owl_cls,
			String associationname, String associationvalue) {
		RDFProperty slot = owlModel.getRDFProperty(associationname);
		RDFSClass aClass = (RDFSClass) owlModel
				.getRDFSNamedClass(associationvalue);

		try {
			if (slot != null) {
				owl_cls.addPropertyValue(slot, aClass);
				return true;
			}
		} catch (Exception e) {
			logger.severe("Error in addObjectProperty with args: ");
			logger.severe(owl_cls.getBrowserText() + " " + associationname
					+ " " + associationvalue);
			logger.severe(e.getLocalizedMessage());
			return false;
		}

		return false;
	}

	public boolean removeSuperclasses(OWLNamedClass cls, String browserText) {
		Collection parent_vec = cls.getSuperclasses(false);
		if (parent_vec == null || parent_vec.isEmpty()) {
			return false;
		}

		if (parent_vec != null) {
			Iterator j = parent_vec.iterator();
			while (j.hasNext()) {
				Object obj = j.next();
				RDFSClass r = (RDFSClass) obj;
				if (r != null) {
					if (r.getBrowserText().compareTo(browserText) == 0) {
						try {
							cls.removeSuperclass(r);
						} catch (Exception e) {

						}
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Method for adding a new definition to a class. It handles all the cases:
	 * if there was no definition before; if there was just one restriction; or
	 * if the existing definition is an intersection. The new definition is
	 * added to the first defintion of the class. (Anyway in NCIEditTab the
	 * assumption is that each class has only one definition.
	 * 
	 * @param cls -
	 *            the class where the new definition is added
	 * @param newDefinition -
	 *            the new definition to be added (can be a named class, a
	 *            restriction, or a logical class)
	 * @return true - if adding was successful, false - otherwise
	 */
	public boolean addEquivalentDefinition(OWLNamedClass cls,
			RDFSClass newDefinition) {
		if (cls == null || newDefinition == null)
			return false;

		RDFSClass definition = cls.getDefinition();

		try {
			// no defintion previously
			if (definition == null) {
				cls.addEquivalentClass(newDefinition);
				return true;
			}

			// if it is an intersection, add a new operand
			if (definition instanceof OWLIntersectionClass) {
				((OWLIntersectionClass) definition).addOperand(newDefinition);

				if (newDefinition instanceof RDFSNamedClass)
					addDirectSuperclass(cls, newDefinition);
				
				return true;
			}

			// else, if is only a restriction, which needs to be transformed
			// into an intersection
			RDFSClass clonedOldEquivalentClass = definition;

			if (definition instanceof OWLAnonymousClass) {
				logger.warning("Creating clone of "
						+ definition.getBrowserText());
				clonedOldEquivalentClass = definition.createClone();
			}

			OWLIntersectionClass newClassDefinition = owlModel
					.createOWLIntersectionClass();
			newClassDefinition.addOperand(clonedOldEquivalentClass);
			newClassDefinition.addOperand(newDefinition);

			if (newDefinition instanceof RDFSNamedClass)
				addDirectSuperclass(cls, newDefinition);

			cls.addEquivalentClass(newClassDefinition);
			cls.removeEquivalentClass(definition);

			if (clonedOldEquivalentClass instanceof OWLNamedClass)
				cls.addSuperclass(clonedOldEquivalentClass);
			

		} catch (Exception e) {
			logger.warning("Error at adding definition "
					+ newDefinition.getBrowserText() + " to class "
					+ cls.getBrowserText());
			return false;
		}
		

		return true;
	}

	public boolean removeEquivalentDefinition(OWLNamedClass cls,
			RDFSClass defintionToDelete) {
		if (cls == null || defintionToDelete == null)
			return false;

		RDFSClass definition = cls.getDefinition();

		if (definition == null)
			return true;
		// there is only one definitition and it should be deleted
		if (definition.equalsStructurally(defintionToDelete)) {
			if (definition instanceof OWLNamedClass) {
				cls.removeEquivalentClass(definition);
			} else {
				definition.delete();
			}
			return true;
		}

		// the definition is an intersection
		boolean operand_found = false;
		if (definition instanceof OWLIntersectionClass) {

			if (hasOperand(((OWLIntersectionClass) definition),
					defintionToDelete)) {
				OWLIntersectionClass intersectionCls = (OWLIntersectionClass) definition;
				intersectionCls.removeOperand(defintionToDelete);
				operand_found = true;
				Collection operands = new ArrayList(intersectionCls
						.getOperands());
				if (operands.size() == 1) {
					RDFSClass remainder = (RDFSClass) operands.iterator()
							.next();
					logger.warning("Warning clone of "
							+ remainder.getBrowserText());
					RDFSClass copy = remainder.createClone();
					cls.removeEquivalentClass(intersectionCls);
					cls.addSuperclass(copy);
				}
			} else {
				Collection operands = new ArrayList(
						((OWLIntersectionClass) definition).getOperands());
				Iterator it = operands.iterator();
				while (it.hasNext()) {
					RDFSClass next = (RDFSClass) it.next();
					if (next instanceof OWLIntersectionClass) {
						if (hasOperand(((OWLIntersectionClass) next),
								defintionToDelete)) {
							OWLIntersectionClass intersectionCls_2 = (OWLIntersectionClass) next;
							intersectionCls_2.removeOperand(defintionToDelete);
							operand_found = true;
							Collection operands_2 = new ArrayList(
									intersectionCls_2.getOperands());
							if (operands_2.size() == 1) {
								RDFSClass remainder = (RDFSClass) operands_2
										.iterator().next();
								logger.warning("Warning clone of "
										+ remainder.getBrowserText());
								RDFSClass copy = remainder.createClone();
								cls.removeEquivalentClass(intersectionCls_2);
								cls.addEquivalentClass(copy);
							}
							break;
						}
					}
				}
			}

		}

		/*
		 * else //just one restriction as defintion {
		 * cls.removeEquivalentClass(defintionToDelete); }
		 */

		if (cls.getNamedSuperclasses(false).size() == 0) {
			cls.addSuperclass(owlModel.getOWLThingClass());
		}

		// KLO, 040407
		if (defintionToDelete instanceof OWLNamedClass) {
			cls.removeSuperclass(defintionToDelete);
		}

		else {
			if (operand_found) {
				defintionToDelete.delete();
			}
		}

		return true;
	}

	public boolean removeEquivalentDefinitionNew(OWLNamedClass cls,
			RDFSClass defintionToDelete) {
		if (cls == null || defintionToDelete == null)
			return false;

		RDFSClass definition = cls.getDefinition();

		if (definition == null)
			return true;
		// there is only one definitition and it should be deleted
		if (definition.equalsStructurally(defintionToDelete)) {
			if (definition instanceof OWLNamedClass) {
				cls.removeEquivalentClass(definition);
			} else {
				definition.delete();
			}
			return true;
		}

		// the definition is an intersection
		boolean operand_found = false;
		if (definition instanceof OWLIntersectionClass) {

			if (hasOperand(((OWLIntersectionClass) definition),
					defintionToDelete)) {
				OWLIntersectionClass intersectionCls = (OWLIntersectionClass) definition;
				intersectionCls.removeOperand(defintionToDelete);
				operand_found = true;

				Collection operands = new ArrayList(intersectionCls
						.getOperands());
				if (operands.size() == 0) {
					cls.removeEquivalentClass(intersectionCls);
					/**
					 * RDFSClass remainder = (RDFSClass) operands.iterator()
					 * .next(); logger.warning("Warning clone of " +
					 * remainder.getBrowserText()); RDFSClass copy =
					 * remainder.createClone();
					 * cls.removeEquivalentClass(intersectionCls);
					 * cls.addEquivalentClass(copy);
					 */
				}

			} else {
				Collection operands = new ArrayList(
						((OWLIntersectionClass) definition).getOperands());
				Iterator it = operands.iterator();
				while (it.hasNext()) {
					RDFSClass next = (RDFSClass) it.next();
					if (next instanceof OWLIntersectionClass) {
						if (hasOperand(((OWLIntersectionClass) next),
								defintionToDelete)) {
							OWLIntersectionClass intersectionCls_2 = (OWLIntersectionClass) next;
							intersectionCls_2.removeOperand(defintionToDelete);
							operand_found = true;
							Collection operands_2 = new ArrayList(
									intersectionCls_2.getOperands());
							if (operands_2.size() == 0) {
								cls.removeEquivalentClass(intersectionCls_2);
								/**
								 * RDFSClass remainder = (RDFSClass) operands_2
								 * .iterator().next(); logger.warning("Warning
								 * clone of " + remainder.getBrowserText());
								 * RDFSClass copy = remainder.createClone();
								 * cls.removeEquivalentClass(intersectionCls_2);
								 * cls.addEquivalentClass(copy);
								 */
							}
							break;
						}
					}
				}
			}

		}

		/*
		 * else //just one restriction as defintion {
		 * cls.removeEquivalentClass(defintionToDelete); }
		 */

		if (cls.getNamedSuperclasses(false).size() == 0) {
			cls.addSuperclass(owlModel.getOWLThingClass());
		}

		// KLO, 040407
		if (defintionToDelete instanceof OWLNamedClass) {
			cls.removeSuperclass(defintionToDelete);
		}

		else {
			if (operand_found) {
				defintionToDelete.delete();
			}
		}

		return true;
	}

	private boolean hasOperand(OWLIntersectionClass intersection,
			RDFSClass operand) {
		for (Iterator iter = intersection.getOperands().iterator(); iter
				.hasNext();) {
			RDFSClass oper = (RDFSClass) iter.next();
			if (oper.equals(operand)) {
				return true;
			}
		}

		return false;
	}

	// =======================================================================================================
	// Methods for validating batch editor input file:
	// =======================================================================================================

	public Vector getAssociations(OWLNamedClass subject) {
		Vector<String> w = new Vector<String>();
		Vector<RDFProperty> v = new Vector<RDFProperty>();
		Vector<String> excludedProperties = new Vector<String>();
		excludedProperties.add("ID");

		Collection allProperties = owlModel
				.getVisibleUserDefinedRDFProperties();
		for (Iterator it = allProperties.iterator(); it.hasNext();) {
			RDFProperty property = (RDFProperty) it.next();
			if (property.isAnnotationProperty() && property.hasObjectRange()
					&& property instanceof DefaultOWLObjectProperty
					&& !excludedProperties.contains(property.getPrefixedName())) {
				v.add(property);
			}
		}

		v.add(owlModel.getOWLDifferentFromProperty());
		v.add(owlModel.getOWLDisjointWithProperty());
		v.add(owlModel.getOWLEquivalentPropertyProperty());
		v.add(owlModel.getOWLSameAsProperty());
		v.add(owlModel.getRDFSIsDefinedByProperty());

		RDFProperty seeAlsoSlot = owlModel
				.getRDFProperty(RDFSNames.Slot.SEE_ALSO);
		v.add(seeAlsoSlot);

		for (int i = 0; i < v.size(); i++) {
			RDFProperty property = (RDFProperty) v.elementAt(i);
			if (property.isVisible()) {
				Collection c = subject.getPropertyValues(property);
				for (Iterator it = c.iterator(); it.hasNext();) {
					Object value = it.next();
					OWLNamedClass target = (OWLNamedClass) value;
					String clsName = getInternalName(target);

					w.add(property.getPrefixedName() + "|" + clsName);
				}
			}
		}
		v.clear();
		return w;
	}

	private Vector supportedAnnotationProps = null;

	public Vector<String> getSupportedAnnotationProperties() {

		if (supportedAnnotationProps == null) {
			Vector<String> v = new Vector<String>();
			Collection c = owlModel.getRDFProperties();
			Object[] propertiesArray = c.toArray();
			for (int i = 0; i < propertiesArray.length; i++) {
				RDFProperty property = (RDFProperty) propertiesArray[i];
				if (property.isAnnotationProperty()
						&& !property.hasObjectRange()) {
					v.add(property.getPrefixedName());
				}
			}

			supportedAnnotationProps = v;
		}
		return supportedAnnotationProps;
		
	}

	private Vector supportedRoles = null;

	public Vector<String> getSupportedRoles() {
		if (supportedRoles == null) {

			Vector<String> v = new Vector<String>();
			Collection c = owlModel.getRDFProperties();
			Object[] propertiesArray = c.toArray();
			for (int i = 0; i < propertiesArray.length; i++) {
				RDFProperty property = (RDFProperty) propertiesArray[i];
				if (!property.isAnnotationProperty()
						&& property.hasObjectRange()) {
					if (property.getPrefixedName().indexOf(":") == -1) {
						v.add(property.getPrefixedName());
					}
				}
			}

			supportedRoles = v;
		}
		return supportedRoles;
	}

	private Vector supportedAssociations = null;

	public Vector<String> getSupportedAssociations() {

		if (supportedAssociations == null) {
			Vector<String> v = new Vector<String>();
			Collection c = owlModel.getRDFProperties();
			Object[] propertiesArray = c.toArray();
			for (int i = 0; i < propertiesArray.length; i++) {
				RDFProperty property = (RDFProperty) propertiesArray[i];
				if (property.isAnnotationProperty()
						&& property.hasObjectRange()) {
					//if (property.getName().indexOf(":") == -1) {
						v.add(property.getPrefixedName());
					//}
				}
			}

			supportedAssociations = v;
		}
		return supportedAssociations;
	}

	public Vector getDirectSuperclassNames(Cls hostClass) {
		Vector<String> v = new Vector<String>();
		for (Iterator it = hostClass.getDirectSuperclasses().iterator(); it
				.hasNext();) {
			Cls superCls = (Cls) it.next();
			if (superCls instanceof OWLNamedClass) {
				v.add(((OWLNamedClass)superCls).getPrefixedName());
			}
		}
		return v;
	}

	public boolean hasAssociation(OWLNamedClass hostClass, String name,
			String value) {
		String t = name + "|" + value;
		Vector w = getAssociations(hostClass);
		for (int i = 0; i < w.size(); i++) {
			String s = (String) w.elementAt(i);
			if (s.compareTo(t) == 0)
				return true;
		}
		return false;
	}

	public boolean hasProperty(OWLNamedClass cls, String name, String value) {
		RDFProperty property = owlModel.getRDFProperty(name);
		Collection c = cls.getPropertyValues(property);
		if (c.isEmpty()) {
			return false;
		}

		Object[] valuesArray = c.toArray();
		for (int i = 0; i < valuesArray.length; i++) {
			Object val = valuesArray[i];
			if (val.toString().compareTo(value) == 0)
				return true;
		}
		return false;
	}

	public boolean hasRole(OWLNamedClass hostClass, String name, String value) {
		for (Iterator it = hostClass.getSuperclasses(false).iterator(); it
				.hasNext();) {
			Cls superCls = (Cls) it.next();
			if (superCls instanceof OWLSomeValuesFrom) {
				OWLSomeValuesFrom r = (OWLSomeValuesFrom) superCls;
				RDFProperty property = r.getOnProperty();
				if (property.getPrefixedName().compareTo(name) == 0) {
					OWLNamedClass filler = (OWLNamedClass) r.getFiller();
					if (filler.getPrefixedName().compareTo(value) == 0) {
						return true;
					}
				}
			} else if (superCls instanceof OWLAllValuesFrom) {
				OWLAllValuesFrom r = (OWLAllValuesFrom) superCls;
				RDFProperty property = r.getOnProperty();
				if (property.getPrefixedName().compareTo(name) == 0) {
					OWLNamedClass filler = (OWLNamedClass) r.getFiller();
					if (filler.getPrefixedName().compareTo(value) == 0) {
						return true;
					}
				}
			} else if (superCls instanceof OWLIntersectionClass) {
				OWLIntersectionClass ointc = (OWLIntersectionClass) superCls;
            	Collection ops = ointc.getOperands();
            	Iterator itc = ops.iterator();
            	while (itc.hasNext()) {
            		RDFSClass rc = (RDFSClass) itc.next();
            		if (rc instanceof OWLSomeValuesFrom) {
        				OWLSomeValuesFrom r = (OWLSomeValuesFrom) rc;
        				RDFProperty property = r.getOnProperty();
        				if (property.getPrefixedName().compareTo(name) == 0) {
        					OWLNamedClass filler = (OWLNamedClass) r.getFiller();
        					if (filler.getPrefixedName().compareTo(value) == 0) {
        						return true;
        					}
        				}
        			} else if (rc instanceof OWLAllValuesFrom) {
        				OWLAllValuesFrom r = (OWLAllValuesFrom) rc;
        				RDFProperty property = r.getOnProperty();
        				if (property.getPrefixedName().compareTo(name) == 0) {
        					OWLNamedClass filler = (OWLNamedClass) r.getFiller();
        					if (filler.getPrefixedName().compareTo(value) == 0) {
        						return true;
        					}
        				}
        			}
            	}       		
				
			}
		}
		return false;
	}

	public static boolean containsSimpleRestrictionsOnly(
			RDFSClass equivalentClass) {
		if (equivalentClass instanceof OWLIntersectionClass) {
			Collection operands = ((OWLIntersectionClass) equivalentClass)
					.getOperands();
			for (Iterator oit = operands.iterator(); oit.hasNext();) {
				RDFSClass operand = (RDFSClass) oit.next();
				if (operand instanceof OWLIntersectionClass)
					return false;
				else if (operand instanceof OWLUnionClass)
					return false;
				else if (operand instanceof OWLNamedClass)
					return false;
			}
			return true;
		} else if (equivalentClass instanceof OWLUnionClass) {
			return false;
		} else {
			return false;
		}
	}

	// ===================================================================================================

	public void removeDirectSuperclasses(OWLNamedClass cls) {
		Collection sups = cls.getSuperclasses(false);
		if (sups == null)
			return;
		for (Iterator iterator = sups.iterator(); iterator.hasNext();) {
			RDFSClass sup = (RDFSClass) iterator.next();
			if (sup instanceof OWLNamedClass) {
				if (getInternalName(sup).compareTo(
						NCIEditTab.RETIRED_CONCEPTS) != 0) {
					cls.removeSuperclass(sup);
				}
			} else {
				removeEquivalentDefinition((OWLNamedClass) cls, sup);
			}
		}
	}

	public ArrayList<RDFSClass> getDirectSuperclassItems(Cls hostClass,
			int set_type) {
		ArrayList<RDFSClass> v = new ArrayList<RDFSClass>();
		if (set_type == NCIConditionsTableModel.SET_SUPERCLASS) {

			for (Iterator it = ((OWLNamedClass) hostClass).getSuperclasses(
					false).iterator(); it.hasNext();) {
				Cls superCls = (Cls) it.next();
				if (superCls instanceof RDFSNamedClass) {
					RDFSClass aClass = (RDFSClass) superCls;
					if (aClass instanceof OWLNamedClass) {
						if (!v.contains(aClass)) {
							v.add(aClass);
						}
					}
				}
			}
		} else if (set_type == NCIConditionsTableModel.SET_RESTRICTION) {
			// for (Iterator it = hostClass.getSuperclasses().iterator();
			// it.hasNext();)
			for (Iterator it = hostClass.getDirectSuperclasses().iterator(); it
					.hasNext();) {
				Cls superCls = (Cls) it.next();
				if (superCls instanceof OWLAnonymousClass) {
					RDFSClass r = (RDFSClass) superCls;

					// if (aClass instanceof OWLAnonymousClass && !(aClass
					// instanceof OWLIntersectionClass))
					if (r instanceof OWLAnonymousClass) {
						// need to exclude equivalent classes
						boolean isDefinition = containsDefinition(
								(OWLNamedClass) hostClass, r);
						if (!isDefinition) {
							if (!v.contains((RDFSClass) r)) {
								v.add((RDFSClass) r);
							}

						}
					}
				}
			}
		}
		return v;
	}

	public ArrayList<RDFSClass> getDefinitionItems(Cls hostClass, int set_type) {
		ArrayList<RDFSClass> v = new ArrayList<RDFSClass>();
		for (Iterator it = ((RDFSClass) hostClass).getSuperclasses(false)
				.iterator(); it.hasNext();) {
			Cls superCls = (Cls) it.next();

			if (superCls instanceof RDFSClass
					&& ((RDFSClass) superCls).getSuperclasses(false).contains(
							hostClass)) {
				RDFSClass equivalentClass = (RDFSClass) superCls;
				if (equivalentClass instanceof OWLIntersectionClass) {
					OWLIntersectionClass intersectionCls = (OWLIntersectionClass) equivalentClass;
					// 121406 KLO
					if (set_type == NCIConditionsTableModel.SET_RESTRICTION
							&& containsSimpleRestrictionsOnly(intersectionCls)) {
						if (!v.contains(equivalentClass)) {
							v.add((RDFSClass) equivalentClass);
						}

					} else {
						Collection operands = ((OWLIntersectionClass) equivalentClass)
								.getOperands();
						for (Iterator oit = operands.iterator(); oit.hasNext();) {
							RDFSClass operand = (RDFSClass) oit.next();

							if (set_type == NCIConditionsTableModel.SET_RESTRICTION
									&& operand instanceof OWLAnonymousClass) {

								if (!v.contains(operand)) {
									v.add((RDFSClass) operand);
								}

							} else if (set_type == NCIConditionsTableModel.SET_SUPERCLASS
									&& operand instanceof OWLNamedClass) {
								if (!v.contains(operand)) {
									v.add((RDFSClass) operand);
								}

							}
							// hset.add(edit_panel.getNCIEditTab().getBrowserText(operand));
						}
					}

				}

				else if (equivalentClass instanceof OWLUnionClass) {
					if (set_type == NCIConditionsTableModel.SET_RESTRICTION
							&& equivalentClass instanceof OWLAnonymousClass) {

						if (!v.contains(equivalentClass)) {
							v.add((RDFSClass) equivalentClass);
						}

					} else if (set_type == NCIConditionsTableModel.SET_SUPERCLASS
							&& equivalentClass instanceof OWLNamedClass) {

						if (!v.contains(equivalentClass)) {
							v.add((RDFSClass) equivalentClass);
						}

					}
				} else // None of the above
				{
					if (set_type == NCIConditionsTableModel.SET_RESTRICTION
							&& equivalentClass instanceof OWLAnonymousClass) {

						if (!v.contains(equivalentClass)) {
							v.add((RDFSClass) equivalentClass);
						}

					} else if (set_type == NCIConditionsTableModel.SET_SUPERCLASS
							&& equivalentClass instanceof OWLNamedClass) {

						if (!v.contains(equivalentClass)) {
							v.add((RDFSClass) equivalentClass);
						}

					}
				}
			}
		}
		return v;
	}

}
