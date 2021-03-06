/*
 * Author(s): Natasha Noy (noy@smi.stanford.edu)
  * 
*/
package edu.stanford.smi.protegex.prompt.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protegex.prompt.*;
import edu.stanford.smi.protegex.prompt.operation.editor.*;
import edu.stanford.smi.protegex.prompt.util.*;

public class TraversalDefinitionDialog extends JPanel {
	private KnowledgeBase _kb = ProjectsAndKnowledgeBases.getKnowledgeBase(ProjectsAndKnowledgeBases.EXTRACT_SOURCE_INDEX);
	private Collection _allSlots = null;
	private HashMap _slotsWithValues = null;
	private HashMap _oldSlotsWithValues = null;
	
	private SingleSlotPanel _levelsPanel = null;
	private int _commonLevel = TraversalDirective.NO_LEVEL_SET;
	
	private JComponent _slotTable = null;
	private JButton _selectAll = null;
	private JButton _deselectAll = null;
	private Class _frameType;
	
	
	public TraversalDefinitionDialog (Class frameType) {
		_frameType = frameType;
		initializeStructures ();
		createUI ();
	}
	
	public Object getValue() {
//		HashMap result = new HashMap (_allSlots.size());
//		Iterator i = _allSlots.iterator();
//		while (i.hasNext()) {
//			Slot next = (Slot)i.next();
//			Object value = _slotsWithValues.get(next);
//			if (value != null)
//				result.put (next, value);
//		}
		return _slotsWithValues;
	}
	
	public int getCommonLevel () {
		return _commonLevel;	
	}

	public void clear() {
		
	}
	
	
	private void createUI () {
		setLayout (new BorderLayout ());
		add (createLabelPanel (), BorderLayout.NORTH);
		add (createSlotListPanel (), BorderLayout.CENTER);
	}
	
	private JComponent createLabelPanel () {
		String html = "<html><center>";
		html += "Number of levels to copy for specific slots<br>";
		html += "<it>(checking the box and leaving the field blank<br>";
		html += "sets levels to &quot;unlimited&quot;) </it>";
		html += "</center>  </html>";
		JLabel label = new JLabel (html);
		return label;
	}
	
	private JComponent createSlotListPanel () {
		JPanel slotList =  new JPanel();
		slotList.setLayout (new BorderLayout (0, 10));
		slotList.add(createLevelsPanel (), BorderLayout.NORTH);
		slotList.add(createSlotsPanel (), BorderLayout.CENTER);
		return slotList;
	}
	
	private JPanel createLevelsPanel () {
		_levelsPanel = new SingleSlotPanel (null, null, "every slot");
		JPanel levelsPanel = new JPanel();
		levelsPanel.setLayout (new BorderLayout());
		levelsPanel.add (_levelsPanel, BorderLayout.CENTER);
		levelsPanel.setBorder (BorderFactory.createRaisedBevelBorder());
		
		final JCheckBox checkBox = _levelsPanel.getCheckBox();
		checkBox.addChangeListener(new ChangeListener () {
			public void stateChanged(ChangeEvent arg0) {
				if (checkBox.isSelected()) {
					slotTableEnabled (false);
				}
				else {
 					slotTableEnabled (true);	
				}
			}
		});
		return levelsPanel;
	}

	private JPanel createSlotsPanel () {
		JPanel slotsPanel = new JPanel();
		slotsPanel.setLayout(new BorderLayout ());
		slotsPanel.add(createSlotTablePanel (), BorderLayout.CENTER);
		slotsPanel.add(createButtonPanel (), BorderLayout.SOUTH);
		return slotsPanel;
	}
	
	private static final int PREFERRED_NUMBER_OF_SLOTS_TO_DISPLAY = 10;
	private JComponent createSlotTablePanel () {
		int numberOfSpecialSlots = 0;
		if (Cls.class.isAssignableFrom(_frameType)) 
			numberOfSpecialSlots += 3; //subclasses, superclasses, instances
		if (Slot.class.isAssignableFrom(_frameType)) 
			numberOfSpecialSlots += 2; //subslots, superslots
		
		int totalSlots = _allSlots.size() + numberOfSpecialSlots;
		if (totalSlots == 0) totalSlots = 1;
			
		_slotTable = new JPanel();	
		_slotTable.setLayout(new GridLayout (totalSlots, 0, 0, 0));
		
		addSpecialSlots ();
		addRegularSlots ();
		JComponent scrollPane = ComponentFactory.createScrollPane (_slotTable);
		
		if (_allSlots.size() >= PREFERRED_NUMBER_OF_SLOTS_TO_DISPLAY)
			scrollPane.setPreferredSize(new Dimension(scrollPane.getPreferredSize().width, 
													  _levelsPanel.getPreferredSize().height * PREFERRED_NUMBER_OF_SLOTS_TO_DISPLAY));
			
		return scrollPane;
	}

	private void addSpecialSlots () {
		if (Cls.class.isAssignableFrom(_frameType)) {
			_slotTable.add(new SingleSlotPanel (_kb.getSlot(Model.Slot.DIRECT_SUBCLASSES), Icons.getClsesIcon(), Editor.COPY_SUBCLASSES));
			_slotTable.add(new SingleSlotPanel (_kb.getSlot(Model.Slot.DIRECT_INSTANCES), Icons.getInstancesIcon(), Editor.COPY_INSTANCES));
			_slotTable.add(new SingleSlotPanel (_kb.getSlot(Model.Slot.DIRECT_SUPERCLASSES), Icons.getClsesIcon(), Editor.DEEP_COPY));
		}
		if (Slot.class.isAssignableFrom(_frameType)) {
			_slotTable.add(new SingleSlotPanel (_kb.getSlot(Model.Slot.DIRECT_SUBSLOTS), Icons.getSlotsIcon(), Editor.COPY_SUBSLOTS));
			_slotTable.add(new SingleSlotPanel (_kb.getSlot(Model.Slot.DIRECT_SUPERSLOTS), Icons.getSlotsIcon(), Editor.DEEP_COPY_SLOTS));
		}
		
	}
	
	private void addRegularSlots () {
		Iterator i = _allSlots.iterator();
		SingleSlotPanel nextPanel = null;
		while (i.hasNext()) {
			Slot nextSlot = (Slot)i.next();
			nextPanel = new SingleSlotPanel (nextSlot, Icons.getSlotIcon(), nextSlot.getName());
			_slotTable.add(nextPanel);
		}
	}
	
	
	
	private void slotTableEnabled (boolean b) {
		Component[] components = _slotTable.getComponents();
		for (int i = 0; i < components.length; i++) {
			if (components[i] instanceof SingleSlotPanel)	
				((SingleSlotPanel)components[i]).setEnabled(b);
		}
		_selectAll.setEnabled (b);
		_deselectAll.setEnabled(b);
	}
	
	private JPanel createButtonPanel () {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout (0, 2, 0, 10));
		
		_selectAll = ComponentFactory.createButton(new AbstractAction () {
			public void actionPerformed(ActionEvent event) {
				selectAll();	
			}
		});
		_selectAll.setText("Select All");
		buttonPanel.add (_selectAll);

		_deselectAll = ComponentFactory.createButton(new AbstractAction () {
			public void actionPerformed(ActionEvent event) {
				deselectAll();	
			}
		});
		_deselectAll.setText("Deselect All");
		buttonPanel.add (_deselectAll);
		
		return buttonPanel;
	}
	
	public void reload () {
		_oldSlotsWithValues = new HashMap (_slotsWithValues);
		createUI();
	}
	
	public void restoreOld (boolean b) {
		if (b)
			_slotsWithValues = _oldSlotsWithValues;
		else
			_oldSlotsWithValues = null;
	}
	
	
//	private void setLevelForAll (int value) {
//		Log.enter (this, "setLevelForAll", new Integer (value));
//	}
//	
	private void selectAll () {
		setSelectedForAll (true);
	}
	
	private void setSelectedForAll (boolean b) {
		Component [] components = _slotTable.getComponents();
		for (int i = 0; i < components.length; i++) {
			if (components[i] instanceof SingleSlotPanel)
				((SingleSlotPanel)components[i]).setSelected (b);	
		}
	}
	
	private void deselectAll () {
		setSelectedForAll (false);
	}
	
	
	private void initializeStructures (){
		_allSlots = getInstanceAndClassValuedSlots();
		_slotsWithValues = new HashMap (_allSlots.size());
//		initializeMap ();
	}

//	private void initializeMap () {
//		Iterator i = _allSlots.iterator();
//		while (i.hasNext()) {
//			Slot next = (Slot)i.next();
//			_slotsWithValues.put(next, new Integer (TraversalDirective.NO_LEVEL_SET));
//		}
//	}
	
	private Collection getInstanceAndClassValuedSlots () {
		ArrayList result = new ArrayList ();
		Collection allSlots = _kb.getSlots();
		Iterator i = allSlots.iterator();
		while (i.hasNext()) {
			Slot next = (Slot)i.next();
			if (!Util.isSystem(next) && (next.getValueType().equals(ValueType.INSTANCE) || next.getValueType().equals(ValueType.CLS))) {
				result.add(next);
			}
		}
		Collections.sort(result);
		return result;
	}
	
	public boolean copySubclasses () { return getBooleanValue (_kb.getSlot(Model.Slot.DIRECT_SUBCLASSES));}
	public boolean copySuperclasses () { return getBooleanValue (_kb.getSlot(Model.Slot.DIRECT_SUPERCLASSES));}
	public boolean copyInstances () { return getBooleanValue (_kb.getSlot(Model.Slot.DIRECT_INSTANCES));}
	public boolean copySubslots () { return getBooleanValue (_kb.getSlot(Model.Slot.DIRECT_SUBSLOTS));}
	public boolean copySuperslots () { return getBooleanValue (_kb.getSlot(Model.Slot.DIRECT_SUPERSLOTS));}
	public boolean copyEverythingRelated () { return getBooleanValue (null);}
	
	private boolean getBooleanValue (Slot slot) {
		if (slot == null) 
			return (_commonLevel != TraversalDirective.NO_LEVEL_SET);
		else 
			return (_slotsWithValues.get(slot) != null);
	}
	
	public void copySubclasses (boolean b) {
		setDefaultValue (_kb.getSlot(Model.Slot.DIRECT_SUBCLASSES),  b);
	}
	
	public void copySuperclasses (boolean b) {
		setDefaultValue (_kb.getSlot(Model.Slot.DIRECT_SUPERCLASSES),  b);
	}

	public void copyInstances (boolean b) {
		setDefaultValue (_kb.getSlot(Model.Slot.DIRECT_INSTANCES),  b);
	}
	
	public void copySubslots (boolean b) {
		setDefaultValue (_kb.getSlot(Model.Slot.DIRECT_SUBSLOTS),  b);
	}
	
	public void copySuperslots (boolean b) {
		setDefaultValue (_kb.getSlot(Model.Slot.DIRECT_SUPERSLOTS),  b);
	}
	
	public void copyEverythingRelated (boolean b) {
		setDefaultValue (null,  b);
	}
	
	
	private void setDefaultValue (Slot slot, boolean b) {
		if (slot == null) {
			if (_commonLevel == TraversalDirective.NO_LEVEL_SET && b == true)
				_commonLevel = TraversalDirective.INFINITY;
			if (_commonLevel != TraversalDirective.NO_LEVEL_SET && b == false)
				_commonLevel = TraversalDirective.NO_LEVEL_SET;
		} else {
		Object value = _slotsWithValues.get(slot);
		if (value == null && b == true) 
			_slotsWithValues.put(slot, new Integer (TraversalDirective.INFINITY));
		if (value != null && b == false)
			_slotsWithValues.remove(slot);
		}
	}
	
	public class SingleSlotPanel extends JPanel {
		JCheckBox _checkBox = null;
		LevelComponent _level = null;
		
		// either slot or label must be not null (both can be not null)
		SingleSlotPanel (Slot slot, Icon icon, String label) {
			setLayout (new BorderLayout ());
			setBorder(LineBorder.createGrayLineBorder());
			_checkBox = ComponentFactory.createCheckBox();
			add (_checkBox, BorderLayout.WEST);
			
			add (new JLabel (label == null ? slot.getName() : label, icon, SwingConstants.LEFT), BorderLayout.CENTER);
			
			_level = new LevelComponent(_checkBox, slot);
			_checkBox.addChangeListener(new ChangeListener () {
				public void stateChanged(ChangeEvent arg0) {
					if (_checkBox.isSelected())
						_level.setDefaultValue ();
					else
						_level.removeValue();
				}
				
			});

			add (_level, BorderLayout.EAST);
		}
		
		public void setSelected (boolean b) {
			_checkBox.setSelected(b);
		}
		
		public void setEnabled (boolean  b) {
			_checkBox.setEnabled (b);
			_level.setEnabled(b);
		}
		
		
		public JCheckBox getCheckBox () {return _checkBox;}
	
	public class LevelComponent extends JPanel {
		private final Color INVALID_COLOR = Color.red;
		private final static String NO_LIMIT_TEXT = "none";
		private JTextField _textField;
		private Color _defaultColor;
		private boolean _isDirty;
		private Integer _value = null;
		private boolean _noLimit = true;
		private JCheckBox _checkBox = null;
		private Slot _slot = null;

		public LevelComponent (JCheckBox checkBox) {
			_checkBox = checkBox;
			initialize();
		}
		
		public LevelComponent (JCheckBox checkBox, Slot slot) {
			_checkBox = checkBox;
			_slot = slot;
			initialize ();
		}
		
		public void initialize() {
			_textField = ComponentFactory.createTextField();
			_textField.setColumns(4);
			_textField.getDocument().addDocumentListener(_documentListener);
			_textField.addFocusListener(_focusListener);
			_textField.addKeyListener(_keyListener);
			_defaultColor = _textField.getForeground();
			
			Object value = getValue();
			if (value != null) {
				if (value.equals(new Integer (TraversalDirective.INFINITY))) {
					_noLimit = true;
					_checkBox.setSelected(true);
					setText ("");
				} else {
					_value = (Integer)value;
					setText(_value.toString());
					_checkBox.setSelected(true);
				}
			}
			
			add (_textField);
		}

		public void setEnabled (boolean b) {
			if (b == false) {
				removeValue ();
			}
			_textField.setEnabled(b);
		}

		public void setDefaultValue () {
			if (_value == null) {
				_noLimit = true;
				setText ("");
				valueChanged();
			} 
		}
		
		private void removeValue () {
			_value = null;
			_noLimit = false;
			setText (null);
			_checkBox.setSelected(false);
			valueChanged();
		}
		
		private Object getValue () {
			if (_slot != null)
				return _slotsWithValues.get(_slot);
			else if (_commonLevel == TraversalDirective.NO_LEVEL_SET)
				return null;
			else
				return new Integer(_commonLevel); 
		}
		
		private void setValue (Integer value) {
			if (_slot == null) {// common level
				if (value == null)
					_commonLevel = TraversalDirective.NO_LEVEL_SET;
				else
					_commonLevel = value.intValue();
			} else {
				if (value == null)
					_slotsWithValues.remove(_slot);
				else
					_slotsWithValues.put (_slot, value);
			}
		}
		
		private void valueChanged () {
		if (_noLimit || _value != null)
			setValue (_noLimit ? new Integer (TraversalDirective.INFINITY) : _value);
		else
			setValue (null);
		}
		
		private void setCheckBoxValue (boolean b) {
			if (_checkBox != null)
				_checkBox.setSelected(b);
		}

		private static final String INVALID_TEXT_DESCRIPTION = "Value must be a positive number";
		protected boolean validateText(String text) {
			if (text.length() == 0) {
				_noLimit = true;
				_value = null;
				return true;
			} 
			try {
			  _value = new Integer(text);
			  if (_value.intValue() <= 0) {
			  	invalidText ();
			  	return false;
			  }
			  _noLimit = false;
			  _textField.setForeground(_defaultColor);
			  _textField.setToolTipText(null);
			  return true;
			} catch (NumberFormatException e) {
			  invalidText ();
			  return false;
			}
		}
		
		private void invalidText () {
			  _textField.setForeground(INVALID_COLOR);
			  _textField.setToolTipText(INVALID_TEXT_DESCRIPTION);
			  _noLimit = false;
			  _value = null;
		}
		

		public String getText() {
			String s = _textField.getText().trim();
			return s;
//			return s.length() == 0 ? null : s;
		}

		public void markDirty(boolean b) {
			_isDirty = b;
		}


		public void setText(String text) {
			// _isDirty = false;
			_documentListener.disable();
			_textField.setText(text == null ? "" : text);
			_documentListener.enable();
//			if (text != null) {
//				validateText(text);
//			}
		}

		private DocumentChangedListener _documentListener = new DocumentChangedListener() {
			public void stateChanged(ChangeEvent event) {
				validateText(getText());
				_isDirty = true;
			}
		};
		private FocusListener _focusListener = new FocusAdapter() {
			public void focusLost(FocusEvent event) {
				commit();
			}
		};

		private KeyListener _keyListener = new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_ENTER) {
					commit();
				}
			}
		};

		private void commit() {
			if (_isDirty) {
				if (!validateText (getText()))
					setCheckBoxValue (false);
				if (_value != null || _noLimit)
					setCheckBoxValue (true);
				valueChanged();
			}
		}
		
	}
  }	
}
