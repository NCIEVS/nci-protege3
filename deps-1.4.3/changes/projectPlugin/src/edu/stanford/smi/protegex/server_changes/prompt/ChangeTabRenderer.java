package edu.stanford.smi.protegex.server_changes.prompt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;

import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.DefaultRenderer;
import edu.stanford.smi.protege.util.Log;


public class ChangeTabRenderer extends DefaultRenderer {
    private static final long serialVersionUID = -6563461006874742252L;

    private KnowledgeBase old_kb, new_kb;

    private Ontology_Component ontologyComp;

    private boolean _underline = false;
    private boolean _strikeOut = false;

    public ChangeTabRenderer(KnowledgeBase old_kb, KnowledgeBase new_kb){
        super();
        this.old_kb = old_kb;
        this.new_kb = new_kb;
    }

    @Override
	public Color getTextColor() {
        _underline = false;
        _strikeOut = false;

        switch (ontologyComp.getInternalStatus()) {
        case CREATED_AND_DELETED:
            return Color.ORANGE;
        case CREATED:
            return Color.BLUE;
        case DELETED:
            return Color.RED;
        default:
            return Color.BLACK;
        }
    }

    @Override
	public Font getFont () {
        Font result = super.getFont ();
        if (ontologyComp == null) {
			return result;
		}

        _underline = false;
        _strikeOut = false;

        switch(ontologyComp.getInternalStatus()) {
        case CREATED_AND_DELETED:
            result = result.deriveFont(Font.ITALIC);
            break;
        case CREATED:
            _underline = true;
            break;
        case DELETED:
            _strikeOut = true;
            break;
        case CHANGED:
            result = result.deriveFont(Font.BOLD);
            break;
        default:
            break;

        }
        return result;
    }



    @Override
	public void load(Object value) {
        if(value instanceof Ontology_Component) {
            ontologyComp = (Ontology_Component) value;
            value = calculateValueString(ontologyComp);
        } else {
            ontologyComp = null;
        }
        super.load(value);
    }

    private String calculateValueString(Ontology_Component ontoComp) {
        String name = null;
        switch (ontoComp.getInternalStatus()) {
        case CHANGED:
        case UNCHANGED:
        case CREATED:
            name = ontoComp.getCurrentName();
            if (new_kb != null) {
                try {
                	Frame frame = new_kb.getFrame(name);
                	if (frame != null) {
                		name = frame.getBrowserText();
                	}
                }
                catch (Throwable t) {
                    Log.getLogger().warning("Could not get browser text for new frame " + name + ", " + t);
                }
            }
            break;
        case DELETED:
            name = ontoComp.getInitialName();
            if (old_kb != null) {
                try {
                	Frame frame = old_kb.getFrame(name);
                	if (frame != null) {
                		name = frame.getBrowserText();
                	}
                }
                catch(Throwable t) {
                    Log.getLogger().warning("Could not get browser text for old frame " + name + ", " + t);
                }
            }
            break;
        }
        switch (ontoComp.getInternalStatus()) {
        case UNCHANGED:
            return "Unchanged: " + name;
        case CHANGED:
            return "Modified: " + name;
        case CREATED:
            return "New: " + name;
        case DELETED:
            return "Deleted: " + name;
        case CREATED_AND_DELETED:
            return "Created and Deleted Object";
        default:
            throw new RuntimeException("Developer missed a case");
        }
    }

    @Override
	protected void paintString(Graphics graphics, String text, Point position, Color color, Dimension size) {
     if (color != null) {
         graphics.setColor(color);
     }

     graphics.setFont(getFont());
     int y = (size.height + _fontMetrics.getAscent())/2 -2; // -2 is a bizarre fudge factor that makes it look better!
     graphics.drawString(text, position.x, y);
     drawLine (graphics, position.x, _fontMetrics.getHeight()/2, position.x + _fontMetrics.stringWidth(text), _fontMetrics.getHeight()/2);
     position.x += _fontMetrics.stringWidth(text);
   }

   private void drawLine (Graphics g, int x1, int y1, int x2, int y2) {
     if (ontologyComp == null) {
		return;
	}
     if (_strikeOut) {
		g.drawLine(x1, y1+1, x2, y2+1);
	}
     if (_underline) {
       g.drawLine(x1, y1*2, x2, y2*2);
     }
   }


}

