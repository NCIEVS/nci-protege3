package edu.stanford.smi.protegex.server_changes.prompt;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Class;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Individual;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Property;
import edu.stanford.smi.protege.code.generator.wrapping.OntologyJavaMappingUtil;

public class FilterPanel extends JPanel {
    private Map<ComponentFilter, JCheckBox> buttons = new EnumMap<ComponentFilter, JCheckBox>(ComponentFilter.class);

    public FilterPanel(boolean isOwl, Set<ComponentFilter> existing_filters) {

        BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
        setLayout(layout);

        for (ComponentFilter filter : ComponentFilter.values()) {
            if (!isOwl && filter == ComponentFilter.ANONYMOUS) {
				continue;
			}

            JCheckBox button = new JCheckBox(filter.getTitle(isOwl));
            button.setSelected(existing_filters.contains(filter));
            add(button);
            buttons.put(filter, button);
        }
    }

    public EnumSet<ComponentFilter> getResult() {
        EnumSet<ComponentFilter> result = EnumSet.noneOf(ComponentFilter.class);

        for (Map.Entry<ComponentFilter, JCheckBox> entry : buttons.entrySet()) {
            ComponentFilter filter = entry.getKey();
            JCheckBox button = entry.getValue();

            if (button.isSelected()) {
                result.add(filter);
            }
        }
        return result;
    }

    public enum ComponentFilter {
        CLASS("Show Classes"),
        PROPERTY("Show Slots", "Show Properties"),
        INDIVIDUAL("Show Instances", "Show Individuals"),
        ANONYMOUS("Show Anonymous Ontology Components")
        ;

        private String title;
        private String owlTitle;
        private ComponentFilter(String title) {
            this(title, title);
        }

        private ComponentFilter(String title, String owlTitle) {
            this.title = title;
            this.owlTitle = owlTitle;
        }

        public String getTitle(boolean isOwl) {
            if (isOwl) {
                return owlTitle;
            }
            else {
                return title;
            }
        }

        public boolean allow(Ontology_Component frame) {
            switch (this) {
            case CLASS:
                return OntologyJavaMappingUtil.canAs(frame, Ontology_Class.class);
            case PROPERTY:
                return OntologyJavaMappingUtil.canAs(frame, Ontology_Property.class);
            case INDIVIDUAL:
            	return OntologyJavaMappingUtil.canAs(frame, Ontology_Individual.class);
            case ANONYMOUS:
                return true;
            default:
                throw new UnsupportedOperationException("Programmer missed a case");
            }
        }

    }

}
