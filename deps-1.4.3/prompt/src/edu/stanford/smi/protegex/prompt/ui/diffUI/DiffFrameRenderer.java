package edu.stanford.smi.protegex.prompt.ui.diffUI;

import java.awt.*;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class DiffFrameRenderer extends FrameRenderer {

	private boolean _underline = false;
	private boolean _strikeOut = false;
	Frame _frame;
	ResultTable _diffTable;
	
	public DiffFrameRenderer(ResultTable diffTable){
		super();
		_diffTable = diffTable;
	}

	public Color getTextColor() {
		_underline = false;
		_strikeOut = false;
	
		int status = Util.getStatus(_diffTable,_frame);
		switch(status){
			case FrameDifferenceElement.OP_ADDED :
				return Color.BLUE;
				
			case FrameDifferenceElement.OP_DELETED :
				return Color.RED;
				
			default:
				return Color.BLACK;
		}
		
	}
		
	public Font getFont () {
	  Font result = super.getFont ();
	  if (_frame == null) return result;
	  int status = Util.getStatus(_diffTable,_frame);

	  switch(status){
		case FrameDifferenceElement.OP_DELETED: {
		  _strikeOut = true;
		  break;
		}
		
		case FrameDifferenceElement.OP_ADDED: {
		  _underline = true;
		  break;
		}
		
		case FrameDifferenceElement.OP_CHANGED: {
			result = result.deriveFont(Font.BOLD);
			break;
		}
		
		default:{
			//result.deriveFont(Font.PLAIN);
			break;
			
		}
	  }
	
	  return result;
	}

	public void load(Object value)
	{
		if(value instanceof Frame){
			_frame = (Frame)value;			
		}else{
			_frame = null;
		}
		
		super.load(value);
	}


	protected void paintString(Graphics graphics, String text, Point position, Color color, Dimension size) {
	 if (color != null) {
		 graphics.setColor(color);
	 }

	 graphics.setFont(getFont());
	 int y = (size.height + _fontMetrics.getAscent())/2 -2;	// -2 is a bizarre fudge factor that makes it look better!
	 graphics.drawString(text, position.x, y);
	 drawLine (graphics, position.x, (_fontMetrics.getHeight())/2, position.x + _fontMetrics.stringWidth(text), (_fontMetrics.getHeight())/2);
	 position.x += _fontMetrics.stringWidth(text);
   }

   private void drawLine (Graphics g, int x1, int y1, int x2, int y2) { 
	 if (_frame == null) return;
	 if (_strikeOut)
	   g.drawLine(x1, y1+1, x2, y2+1);
	 if (_underline) {
	   g.drawLine(x1, y1*2, x2, y2*2);
	 }
   }

		
}
