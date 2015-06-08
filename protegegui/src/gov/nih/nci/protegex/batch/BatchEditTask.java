package gov.nih.nci.protegex.batch;

import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import gov.nih.nci.protegex.edit.NCIEditTab;
import gov.nih.nci.protegex.edit.OWLWrapper;
import gov.nih.nci.protegex.util.ComplexPropertyParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;

/**
 * @Author: NGIT, Kim Ong; Iris Guo
 */

public class BatchEditTask extends BatchTask {

	/**
	 * Requests that the task be cancelled. This method will only be called if
	 * the task can be cancelled.
	 */
	NCIEditTab tab = null;

	OWLModel owlModel = null;

	OWLWrapper wrapper = null;

	Vector<String> supportedRoles = null;
	Vector<String> supportedProperties = null;
	Vector<String> supportedAssociations = null;

	public BatchEditTask(NCIEditTab tab, String infile, String outfile) {
		super(tab.getBatchProcessPanel());
		this.tab = tab;
		this.infile = infile;
		this.outfile = outfile;
		this.owlModel = tab.getOWLModel();
		this.wrapper = tab.getWrapper();

		supportedRoles = wrapper.getSupportedRoles();
		supportedProperties = wrapper.getSupportedAnnotationProperties();
		supportedAssociations = wrapper.getSupportedAssociations();

		data_vec = getData(infile);
		setMax(data_vec.size());
		setMessage("Batch Edit processing in progress, please wait ...");
	}

	/*
	 * 1 concept name 2 edit|new|delete|del-all 3
	 * property|role|parent|association|propertyqualifier|associationqualifier 4
	 * property name|role name|parent name|qualifier name|association name 5
	 * property value|role value|qualifier value|association value 6 new
	 * property value|new role value|new association value|new qualifier value 7
	 * qualified property name|qualified association name|role
	 * modifier|bar-delimited property qualifier names 8 qualified property
	 * value|qualified association value|bar-delimited property qualifier values
	 * 9 new bar-delimited property qualifier values (this field is for edit
	 * property with qualifiers only)
	 */

	public boolean processTask(int taskId) {

		String s = (String) data_vec.elementAt(taskId);

		super.print("processing: " + s);

		try {

			Vector<String> w = getTokenStr(s, 9);
			String name = (String) w.elementAt(0);

			if (super.checkNoErrors(w, taskId)) {
				// ok
			} else {
				return false;
			}

			String edit = (String) w.elementAt(1);

			String attribute = (String) w.elementAt(2);

			String attributeName = (String) w.elementAt(3);

			String attributeValue = (String) w.elementAt(4);

			String newAttributeValue = (String) w.elementAt(5);

			owlModel.beginTransaction("BatchEdit. Processing " + s);

			// this should be false???
			boolean retval = false;
			if (edit.compareToIgnoreCase("new") == 0) {
				if (attribute.compareToIgnoreCase("property") == 0) {

					retval = wrapper.addAnnotationProperty(name, attributeName,
							attributeValue);

				} else if (attribute.compareToIgnoreCase("parent") == 0) {
					OWLNamedClass hostClass = wrapper.getOWLNamedClass(name);
					OWLNamedClass targetClass = wrapper
							.getOWLNamedClass(attributeName);
					retval = wrapper
							.addDirectSuperclass(hostClass, targetClass);
				}

				else if (attribute.compareToIgnoreCase("association") == 0) {
					OWLNamedClass hostClass = wrapper.getOWLNamedClass(name);
					retval = wrapper.addObjectProperty(hostClass,
							attributeName, attributeValue);
				}

				else if (attribute.compareToIgnoreCase("role") == 0) {
					int pos = attributeValue.indexOf('|');
					String modifier = attributeValue.substring(0, pos);
					String value = attributeValue.substring(pos + 1);
					retval = wrapper.addRestriction(name, attributeName, value,
							modifier);
				}
			} else if (edit.compareToIgnoreCase("delete") == 0) {
				if (attribute.compareToIgnoreCase("property") == 0) {
					retval = wrapper.removeAnnotationProperty(name,
							attributeName, attributeValue);
				} else if (attribute.compareToIgnoreCase("parent") == 0) {
					OWLNamedClass hostClass = wrapper.getOWLNamedClass(name);
					OWLNamedClass targetClass = wrapper
							.getOWLNamedClass(attributeName);
					if (targetClass.isDefinedClass()) {
						retval = wrapper.removeEquivalentDefinitionNew(hostClass, targetClass);
					} else {
						retval = wrapper.removeDirectSuperclass(hostClass,
							targetClass);
					}
				}

				else if (attribute.compareToIgnoreCase("association") == 0) {
					OWLNamedClass hostClass = wrapper.getOWLNamedClass(name);
					retval = wrapper.removeObjectProperty(hostClass,
							attributeName, attributeValue);
				}

				else if (attribute.compareToIgnoreCase("role") == 0) {
					int pos = attributeValue.indexOf('|');
					String modifier = attributeValue.substring(0, pos);
					String value = attributeValue.substring(pos + 1);
					retval = wrapper.removeRestriction(name, attributeName,
							value, modifier);
				}
			} else if (edit.compareToIgnoreCase("edit") == 0) {
				if (attribute.compareToIgnoreCase("property") == 0) {

					retval = wrapper.modifyAnnotationProperty(name,
							attributeName, attributeValue, newAttributeValue);
					possiblySyncPreferredTerm(name, attributeName,
							newAttributeValue);

				} else if (attribute.compareToIgnoreCase("role") == 0) {
					int pos = attributeValue.indexOf('|');
					String modifier = attributeValue.substring(0, pos);
					String value = attributeValue.substring(pos + 1);

					pos = newAttributeValue.indexOf('|');
					String newmodifier = newAttributeValue.substring(0, pos);
					String newvalue = newAttributeValue.substring(pos + 1);

					retval = wrapper.modifyRestriction(name, attributeName,
							value, modifier, newvalue, newmodifier);
				} else if (attribute.compareToIgnoreCase("association") == 0) {
					OWLNamedClass hostClass = wrapper.getOWLNamedClass(name);

					retval = wrapper.removeObjectProperty(hostClass,
							attributeName, attributeValue);
					retval = wrapper.addObjectProperty(hostClass,
							attributeName, newAttributeValue);
				}
			}

			// to be implemented
			/*
			 * else if (edit.compareToIgnoreCase("delete-all") == 0) { }
			 */

			if (retval) {
				tab.recordHistory(NCIEditTab.EVSHistoryAction.MODIFY, wrapper
						.getOWLNamedClass(name), "");
				super.print("\t Done.");
			} else {
				super.print("\t Failed.");
			}
			owlModel.commitTransaction();

		} catch (Exception ex) {

			owlModel.rollbackTransaction();
			print("Server Error occurred:");
			ex.printStackTrace();
			super.print(" Failed.");
			data_vec.remove(taskId);
			this.setMax(max - 1);
			return false;
		}

		return true;
	}

	public void possiblySyncPreferredTerm(String cls_name, String name,
			String value) {
		if (name.compareTo(NCIEditTab.ALTLABEL) == 0) {

			String tn = ComplexPropertyParser.getPtNciTermName(value);
			if (tn != null) {
				// need to mod preferred name and rdfs:label
				OWLNamedClass cls = wrapper.getOWLNamedClass(cls_name);
				String pn = wrapper.getPropertyValue(cls, NCIEditTab.PREFLABEL);
				String rdl = wrapper.getPropertyValue(cls, "rdfs:label");
				wrapper.modifyAnnotationProperty(cls_name,
						NCIEditTab.PREFLABEL, pn, tn);
				wrapper.modifyAnnotationProperty(cls_name, "rdfs:label", rdl,
						tn);

			}
		} else if (name.compareTo(NCIEditTab.PREFLABEL) == 0) {

			OWLNamedClass cls = wrapper.getOWLNamedClass(cls_name);
			ArrayList<String> pvals = wrapper.getPropertyValues(cls,
					NCIEditTab.ALTLABEL);
			for (String s : pvals) {
				String tn = ComplexPropertyParser.getPtNciTermName(s);
				if (tn != null) {
					HashMap<String, String> hm = ComplexPropertyParser
							.parseXML(s);
					String newfspt = ComplexPropertyParser.replaceFullSynValue(
							hm, "term-name", value);
					wrapper.modifyAnnotationProperty(cls_name,
							NCIEditTab.ALTLABEL, s, newfspt);
				}
			}

			String rdl = wrapper.getPropertyValue(cls, "rdfs:label");
			wrapper
					.modifyAnnotationProperty(cls_name, "rdfs:label", rdl,
							value);

		}

	}

	public Vector<String> validateData(Vector<String> v) {

		// keep a vector or error messages, may be more than one
		Vector<String> w = new Vector<String>();

		try {

			String cls_name = (String) v.elementAt(0);
			String action = (String) v.elementAt(1);
			String attribute = (String) v.elementAt(2);
			String attributename = (String) v.elementAt(3);
			String attributevalue_1 = (String) v.elementAt(4);
			String attributevalue_2 = (String) v.elementAt(5);

			OWLNamedClass hostClass = wrapper.getOWLNamedClass(cls_name);

			Vector superclasses = new Vector();
			if (hostClass == null) {
				String error_msg = " -- concept " + cls_name
						+ " does not exist.";
				w.add(error_msg);

			} else if (wrapper.isRetired(hostClass)) {
				w.add(" -- concept " + cls_name + " is retired, cannot edit");
			}

			if (action.compareToIgnoreCase("new") != 0
					&& action.compareToIgnoreCase("edit") != 0
					&& action.compareToIgnoreCase("delete") != 0) {
				String error_msg = " -- action " + action
						+ " is not supported.";
				w.add(error_msg);
			}

			if (attribute.compareToIgnoreCase("parent") != 0
					&& attribute.compareToIgnoreCase("role") != 0
					&& attribute.compareToIgnoreCase("property") != 0
					&& attribute.compareToIgnoreCase("association") != 0) {
				String error_msg = " -- attribute " + attribute
						+ " is not supported.";
				w.add(error_msg);
			}

			if (action.compareToIgnoreCase("new") == 0) {
				if (hostClass != null) {
					if (attribute.compareToIgnoreCase("role") == 0) {
						if (!supportedRoles.contains(attributename)) {
							String error_msg = " -- role " + attributename
									+ " is not identifiable.";
							w.add(error_msg);
						} else {
							int pos = attributevalue_1.indexOf("|");
							if (pos == -1) {
								String error_msg = " -- missing modifier or filler.";
								w.add(error_msg);
							} else {
								String filler = attributevalue_1.substring(
										pos + 1, attributevalue_1.length());

								OWLNamedClass targetClass = (OWLNamedClass) owlModel
										.getRDFSNamedClass(filler);
								if (targetClass == null) {
									String error_msg = " -- concept " + filler
											+ " does not exist.";
									w.add(error_msg);
								} else {
									if (wrapper.hasRole(hostClass,
											attributename, filler)) {
										String error_msg = " -- role already exists.";
										w.add(error_msg);
									}

								}
							}
						}
					}

					else if (attribute.compareToIgnoreCase("parent") == 0) {

						OWLNamedClass superClass = (OWLNamedClass) owlModel
								.getRDFSNamedClass(attributename);
						if (superClass == null) {
							String error_msg = " -- superconcept does not exist.";
							w.add(error_msg);

						} else {

							if (wrapper.isPremerged(superClass)
									|| wrapper.isPreretired(superClass)
									|| wrapper.isRetired(superClass)) {
								String error_msg = "superconcept cannot be premerged, preretired, or retired.";
								w.add(error_msg);

							}

						}

					}

					else if (attribute.compareToIgnoreCase("property") == 0) {
						if (!supportedProperties.contains(attributename)) {
							String error_msg = " -- property " + attributename
									+ " is not identifiable.";
							w.add(error_msg);
						} else {
							if (wrapper.hasProperty(hostClass, attributename,
									attributevalue_1)) {
								String error_msg = " -- property already exists.";
								w.add(error_msg);
							}

						}
						if (this.tab.getFilter().checkBatchProperty(
								attributename, attributevalue_1)
								&& this.tab
										.getFilter()
										.checkBatchPropertyNotFullSynPT(
												attributename, attributevalue_1)) {

						} else {
							w.add(tab.getFilter().getErrorMessage());
						}
					} else if (attribute.compareToIgnoreCase("association") == 0) {
						if (!supportedAssociations.contains(attributename)) {
							String error_msg = " -- association "
									+ attributename + " is not identifiable.";
							w.add(error_msg);
						} else {
							OWLNamedClass targetClass = (OWLNamedClass) owlModel
									.getRDFSNamedClass(attributevalue_1);
							if (targetClass == null) {
								String error_msg = " -- concept "
										+ attributevalue_1 + " does not exist.";
								w.add(error_msg);
							} else {
								if (wrapper.hasAssociation(hostClass,
										attributename, attributevalue_1)) {
									String error_msg = " -- association already exists.";
									w.add(error_msg);
								}
							}
						}
					}
				}
			}

			else if (action.compareToIgnoreCase("edit") == 0
					|| action.compareToIgnoreCase("delete") == 0) {
				if (hostClass != null) {
					if (attribute.compareToIgnoreCase("parent") == 0) {
						if (action.compareToIgnoreCase("delete") == 0) {
							OWLNamedClass superClass = (OWLNamedClass) owlModel
									.getRDFSNamedClass(attributename);
							if (superClass == null) {
								String error_msg = " -- superconcept "
										+ attributename + " does not exist.";
								w.add(error_msg);
							} else if (wrapper.getDirectSuperclassNames(
									hostClass).size() == 1) {
								String error_msg = " -- can't delete last superconcept "
										+ attributename;
								w.add(error_msg);

							}

						} else {
							String error_msg = " -- edit parent action is not supported. Use delete and add actions instead.";
							w.add(error_msg);
						}
					}

					else if (attribute.compareTo("role") == 0) {
						if (!supportedRoles.contains(attributename)) {
							String error_msg = " -- role " + attributename
									+ " is not identifiable.";
							w.add(error_msg);
						} else {
							int pos = attributevalue_1.indexOf("|");
							if (pos == -1) {
								String error_msg = " -- missing modifier or filler.";
								w.add(error_msg);
							} else {
								String filler = attributevalue_1.substring(
										pos + 1, attributevalue_1.length());

								OWLNamedClass targetClass = (OWLNamedClass) owlModel
										.getRDFSNamedClass(filler);
								if (targetClass == null) {
									String error_msg = " -- concept " + filler
											+ " does not exist.";
									w.add(error_msg);
								} else {
									if (!wrapper.hasRole(hostClass,
											attributename, filler)) {
										String error_msg = " -- role does not exist.";
										w.add(error_msg);

									}

									if (action.compareTo("edit") == 0) {
										pos = attributevalue_2.indexOf("|");
										if (pos == -1) {
											String error_msg = " -- missing modifier or filler.";
											w.add(error_msg);
										} else {
											filler = attributevalue_2
													.substring(pos + 1,
															attributevalue_2
																	.length());

											targetClass = (OWLNamedClass) owlModel
													.getRDFSNamedClass(filler);
											if (targetClass == null) {
												String error_msg = " -- concept "
														+ filler
														+ " does not exist.";
												w.add(error_msg);
											}
										}
									}
								}
							}
						}
					} else if (attribute.compareToIgnoreCase("property") == 0) {
						if (!supportedProperties.contains(attributename)) {
							String error_msg = " -- property " + attributename
									+ " is not identifiable.";
							w.add(error_msg);
						} else {
							Boolean editable = wrapper
									.isReadOnlyProperty(attributename);
							if (editable.equals(Boolean.TRUE)) {
								String error_msg = " -- property "
										+ attributename + ", it is read-only.";
								w.add(error_msg);
							}

							if (!wrapper.hasProperty(hostClass, attributename,
									attributevalue_1)) {

								String error_msg = " -- property " + "("
										+ attributename + ", "
										+ attributevalue_1
										+ ") does not exist.";
								w.add(error_msg);

							}

							if (action.compareToIgnoreCase("edit") == 0) {
								if (wrapper.hasProperty(hostClass,
										attributename, attributevalue_2)) {
									String error_msg = " -- property " + "("
											+ attributename + ", "
											+ attributevalue_2
											+ ") already exists.";
									w.add(error_msg);
								} else if (attributevalue_2
										.equalsIgnoreCase("NA")) {
									String error_msg = " -- property " + "("
											+ attributename
											+ ") new value is not specified.";
									w.add(error_msg);
								}
							} else {
								if (this.tab.getFilter().checkBatchProperty(
										attributename, attributevalue_1)) {

								} else {
									w.add(tab.getFilter().getErrorMessage());
								}

							}
						}
					} else if (attribute.compareTo("association") == 0) {
						if (!supportedAssociations.contains(attributename)) {
							String error_msg = " -- association "
									+ attributename + " is not identifiable.";
							w.add(error_msg);
						}

						if (action.compareToIgnoreCase("delete") == 0) {
							OWLNamedClass targetClass = (OWLNamedClass) owlModel
									.getRDFSNamedClass(attributevalue_1);
							if (targetClass == null) {
								String error_msg = " -- concept "
										+ attributevalue_1 + " does not exist.";
								w.add(error_msg);
							} else {
								if (!wrapper.hasAssociation(hostClass,
										attributename, attributevalue_1)) {
									String error_msg = " -- association does not exist.";
									w.add(error_msg);
								}
							}
						} else {
							String error_msg = " -- edit association action is not supported. Use delete and add actions instead.";
							w.add(error_msg);
						}
					}
				}
			}

		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Exception caught", e);
		}

		return w;
	}
}

/**
 * TDE batch modifications include
 * 
 * 1) editing of an existing property, 2) deletion of an existing property, 3)
 * addition of a new property, 4) editing of an existing role, 5) deletion of an
 * existing role 6) addition of a new role 7) addition of a new parent 8)
 * deletion of an existing parent 9) editing of an existing property/association
 * qualifier 10 deletion of an existing property/association qualifier 11)
 * addition of a new property/association qualifier
 * 
 * Role descriptions include the role modifier, the default role modifier in all
 * operations is �some� (allowed values: some, all, poss)
 * 
 * File format is tab-delimited, the column fields are
 * 
 * 1 concept name 2 edit|new|delete|del-all 3
 * property|role|parent|association|propertyqualifier|associationqualifier 4
 * property name|role name|parent name|qualifier name|association name 5
 * property value|role value|qualifier value|association value 6 new property
 * value|new role value|new association value|new qualifier value 7 qualified
 * property name|qualified association name|role modifier|bar-delimited property
 * qualifier names 8 qualified property value|qualified association
 * value|bar-delimited property qualifier values 9 new bar-delimited property
 * qualifier values (this field is for edit property with qualifiers only)
 * 
 * 
 * conceptName\tedit\tproperty\tproperty name\tproperty value\tnew property
 * value\tqualified property name\tqualified property value\tnew bar-delimited
 * property qualifier values
 * 
 * 
 * Olfactory_Cistern\tnew\tproperty\tSynonym\t\tOlfactory_Cistern_Synonym_1\t\t\
 * t Olfactory_Cistern
 * \tnew\tproperty\tSynonym\t\tOlfactory_Cistern_Synonym_2\t\t\t
 * Olfactory_Cistern
 * \tnew\tproperty\tSynonym\t\tOlfactory_Cistern_Synonym_3\t\t\t
 * Olfactory_Cistern
 * \tnew\tproperty\tSynonym\t\tOlfactory_Cistern_Synonym_4\t\t\t
 * Olfactory_Cistern
 * \tnew\tproperty\tSynonym\t\tOlfactory_Cistern_Synonym_5\t\t\t
 * Olfactory_Cistern
 * \tnew\tproperty\tSynonym\t\tOlfactory_Cistern_Synonym_6\t\t\t
 **/
