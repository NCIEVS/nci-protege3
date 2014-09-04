/*
 * Contributor(s): Sandhya Kunnatur kunnatur@smi.stanford.edu
 * 				   Natasha Noy noy@smi.stanford.edu
*/

package edu.stanford.smi.protegex.prompt.ui.diffUI;

import java.awt.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protegex.prompt.promptDiff.structures.*;

public class DiffSlotPairRenderer extends SlotPairRenderer {

	private boolean _underline = false;
	private boolean _strikeOut = false;
	Cls _cls;
	Slot _slot;
	ResultTable _diffTable;
	
	public DiffSlotPairRenderer(ResultTable diffTable){
		super();
		_diffTable = diffTable;
	}

	public Color getTextColor() {
		_underline = false;
		_strikeOut = false;
	
		int status = AcceptorRejector.getSlotStatus(_diffTable,_cls,_slot);
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
	  if (_cls == null || _slot == null) return result;
	  int status = AcceptorRejector.getSlotStatus(_diffTable,_cls,_slot);

	  switch(status){
		case FrameDifferenceElement.OP_DELETED: {
		  _strikeOut = true;
		  return result;
		}
		case FrameDifferenceElement.OP_ADDED: {
		  _underline = true;
		  return result;
		}
	  }
	
	 if (AcceptorRejector.slotChanged (_diffTable, _cls, _slot))
	 	result = result.deriveFont(Font.BOLD);

	  return result;
	}

	public void load(Object value)
	{
		if(value instanceof FrameSlotCombination){
			FrameSlotCombination frameSlotCombo = (FrameSlotCombination)value;
			_cls = (Cls)frameSlotCombo.getFrame();
			_slot = frameSlotCombo.getSlot();
			
		}else{
			_cls = null;
			_slot = null;
		}
		
		super.load(value);
		//int status = AcceptorRejector.getSlotStatus(_diffTable,_cls,_slot);
			
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
	  if (_slot == null) return;
	  if (_strikeOut)
		g.drawLine(x1, y1+1, x2, y2+1);
	  if (_underline) {
//		  g.drawLine(x1, y1*2+1, x2, y2*2+1);
		g.drawLine(x1, y1*2, x2, y2*2);
	  }
	}
}