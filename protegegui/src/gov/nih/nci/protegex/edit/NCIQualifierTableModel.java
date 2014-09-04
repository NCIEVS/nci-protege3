package gov.nih.nci.protegex.edit;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;


/**
 *@Author: NGIT, Kim Ong; Iris Guo
 */

public class NCIQualifierTableModel extends AbstractTableModel {
	
	public static final long serialVersionUID = 123456805L;

    static final int COL_NAME = 0;
    static final int COL_VALUE = 1;

    String[] columnNames;
    Object[][] data;

    NCIQualifierTable table;

    Vector<String> text_vec = null;

    public NCIQualifierTableModel(String[] columnNames) {
		this.columnNames = new String[columnNames.length];
		for (int i=0;i<columnNames.length; i++)
		{
			this.columnNames[i] = columnNames[i];
		}
        text_vec = new Vector<String>();
        text_vec.add("def-definition");
        text_vec.add("go-term");

        data = new String[5][2];
    }

    public String getColumnName(int col) {
        return columnNames[col].toString();
    }

    private boolean notMainTerm(String nam) {
    	for (int i = 0; i < text_vec.size(); i++) {
    		String foo = (String) text_vec.elementAt(i);
    		if (foo.equalsIgnoreCase(nam)) {
    			return false;
    		}
    	}
    	return true;
    }

    public void setData(HashMap hmap, ArrayList<String> qualifiers) // key: qualifier name; value: qualifier value
    {
		 Set set = hmap.keySet();
		 int size = set.size();
		 String text = "";
		 for (int i=0; i<text_vec.size(); i++)
		 {
			 text = (String) text_vec.elementAt(i);
			 if (hmap.containsKey(text))
			 {
				 size = size - 1;
				 break;
			 }
		 }

		 if (size == 0) return;
		 data = new String [size+1][2];
		 int row = 0;
		 int col = 0;
		 
		 for (String nam : qualifiers) {
			 if (hmap.containsKey(nam) && notMainTerm(nam)) {
				 
				 col = 0;
				 data[row][col] = nam;
				 //fireTableCellUpdated(row, col);
				 col++;


                 String value = (String) hmap.get(nam);
				 data[row][col] = value;
				 //fireTableCellUpdated(row, col);
				 row++;
				 
			 }
		 }

		 
	}




    public void initialize() // key: qualifier name; value: qualifier value
    {
		 int n = getRowCount();
		 if (n == 0) return;
		 data = new String [n][2];
		 for (int i=0; i<n; i++)
		 {
			 for (int j=0; j<2; j++)
			 {
				data[i][j] = "";
			 	fireTableCellUpdated(i, j);
		     }
		 }
	}

    public int getRowCount() { return data.length; }

    public int getColumnCount() { return columnNames.length; }

    public Object getValueAt(int row, int col) {
        return data[row][col];
    }
    
    

	public void setTable(NCIQualifierTable table) {
        this.table = table;
    }
}

