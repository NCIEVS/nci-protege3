 /*
  * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */

package edu.stanford.smi.protegex.prompt.ui;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class SlotBindingsChoice extends JPanel {
  Slot _slot;
  Cls _cls = null;

  SlotBindingsChoice (Slot s) {
    _slot = s;

    initialize();
  }

  SlotBindingsChoice (FrameSlotCombination o) {
    _slot = o.getSlot();
    _cls = (Cls)o.getFrame();

    initialize();
  }

  private void initialize () {

    setLayout (new BorderLayout ());
    setBorder(BorderFactory.createTitledBorder (BorderFactory.createLineBorder(Color.black) ));

    add (createFacetsPanel(), BorderLayout.CENTER);

  }

  private JPanel createFacetsPanel() {
    JPanel facets = new JPanel (new BorderLayout());

    String type = createTypeFacet (facets);
    String cardinality = createCardinalityFacet(facets);

    String otherFacets = createOtherFacets (facets);

    JPanel titles = new JPanel(new GridLayout (0, 1, 10, 0));
    JPanel values = new JPanel(new GridLayout (0, 1));
    Color color = KnowledgeBaseInMerging.getFrameColor(_slot);

    titles.add (new JLabel ("Type: "));
    titles.add (new JLabel ("Cardinality: "));
    values.add (DisplayUtilities.createDisabledTextField (type, color));
    values.add (DisplayUtilities.createDisabledTextField(cardinality, color));

    ValueType valueType = _slot.getValueType ();
    if (valueType == ValueType.INSTANCE || valueType == ValueType.CLS) {
		titles.add (new JLabel ("Allowed classes: " ));
        values.add (createAllowedClsesPanel ());
    }
    if (otherFacets.length() > 0) {
    	titles.add  (new JLabel ("Other facets: "));
	    values.add (DisplayUtilities.createDisabledTextField (otherFacets));
    }

    facets.add (titles, BorderLayout.WEST);
    facets.add (values, BorderLayout.CENTER);

    return facets;
  }

	private Component createAllowedClsesPanel () {
        Collection clses;
        if (_slot.getValueType() == ValueType.INSTANCE)
        	clses = (_cls == null) ? _slot.getAllowedClses() : _cls.getTemplateSlotAllowedClses(_slot);
        else
			clses = (_cls == null) ? _slot.getAllowedParents() : _cls.getTemplateSlotAllowedParents(_slot);
        if (clses != null && clses.size() > 0)
        	clses = getPrototypes (clses);
        JList component = ComponentFactory.createSelectableList(null, true);
        component.setCellRenderer(FrameRenderer.createInstance());
        ComponentUtilities.addListValues(component, clses);
        return component;
  	}

    private Collection getPrototypes (Collection clses) {
    	Collection result = new ArrayList();
     	Iterator i = clses.iterator();
        while (i.hasNext()) {
         	Cls next = (Cls)i.next();
            if (DummyFrame.isDummyFrame(next)) {
             	result.add (CollectionUtilities.getFirstItem(Mappings.getSources (next)));
            } else
            	result.add (next);
        }
        return result;
    }

  private String createTypeFacet (JPanel panel) {
    return getValueType().toString();
  }

  private ValueType getValueType () {
  	ValueType type;
    if (_cls == null)
    	type = _slot.getValueType();
    else
     	type = _cls.getTemplateSlotValueType(_slot);
    return type;

  }

  private String createCardinalityFacet (JPanel panel) {
    boolean allowsMultiple;
    if (_cls == null)
    	allowsMultiple = _slot.getAllowsMultipleValues();
    else
    	allowsMultiple = _cls.getTemplateSlotAllowsMultipleValues(_slot);
    return allowsMultiple ? "Multiple" : "Single";
  }

  private String createOtherFacets (JPanel panel) {

    StringBuffer text = new StringBuffer();

    addRangeText(text);
    addAllowedValuesText(text);
//    addAllowedClsesText(text);

    return text.toString();
  }

    private void addRangeText(StringBuffer s) {
    	Number min;
        if (_cls == null)
        	min = _slot.getMinimumValue();
        else
        	min = _cls.getTemplateSlotMinimumValue(_slot);
      	Number max;
        if (_cls == null)
        	max = _slot.getMinimumValue();
        else
        	max = _cls.getTemplateSlotMaximumValue(_slot);
        if (min != null) {
            s.append(min.toString());
        }
        if (min != null || max != null) {
            s.append(':');
        }
        if (max != null) {
            s.append(max.toString());
        }
        if (min != null || max != null) {
            s.append(' ');
        }
    }

    private void addAllowedValuesText(StringBuffer s) {
		Collection values;
        ValueType type = getValueType ();
        if (type == ValueType.SYMBOL) {
        	if (_cls == null)
                values = _slot.getAllowedValues();
			else
            	values = _cls.getTemplateSlotAllowedValues(_slot);
	        appendValues(s, "values", values);
        }
    }

    private void addAllowedClsesText(StringBuffer s) {
        ValueType type = getValueType ();
        Collection clses;
        if (type == ValueType.INSTANCE || type == ValueType.CLS) {
        	if (_cls == null)
            	clses = _slot.getAllowedClses();
            else
            	clses = _cls.getTemplateSlotAllowedClses(_slot);
        	addFrameNames(s, "classes", clses);
        }
    }

    private void addFrameNames(StringBuffer s, String text, Collection frames) {
    	Collection frameNames = new ArrayList();
        Iterator i = frames.iterator();
        while (i.hasNext()) {
        	Frame frame = (Frame) i.next();
            String name = (frame == null) ? "<null>" : frame.getName();
            frameNames.add(name);
        }
        appendValues(s, text, frameNames);
    }

    private void appendValues(StringBuffer s, String text, Collection values) {
        boolean first = true;
        s.append(text);
        s.append("={");
        Iterator i = values.iterator();
        while (i.hasNext()) {
            if (first) {
                first = false;
            } else {
                s.append(',');
            }
            s.append(i.next());
        }
        s.append("} ");
    }

}
