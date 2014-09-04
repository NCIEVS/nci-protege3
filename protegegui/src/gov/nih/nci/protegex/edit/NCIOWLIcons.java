package gov.nih.nci.protegex.edit;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;

/**
 *@Author: NGIT, Kim Ong; Iris Guo
 */

public class NCIOWLIcons extends OWLIcons{

    static Map<String, ImageIcon> theNCIOWLIcons = new HashMap<String, ImageIcon>();


    public static ImageIcon getImageIcon(String name) {
        ImageIcon icon = (ImageIcon) theNCIOWLIcons.get(name);
        if (icon == null || icon.getIconWidth() == -1) {
            String partialName = "images/" + name;
            if (name.lastIndexOf('.') < 0) {
                partialName += ".gif";
            }
            icon = loadIcon(NCIOWLIcons.class, partialName);
            if (icon == null && !name.equals("Ugly")) {
                icon = getImageIcon("Ugly");
            }
            theNCIOWLIcons.put(name, icon);
        }
        return icon;
    }


    private static ImageIcon loadIcon(Class cls, String name) {
        ImageIcon icon = null;
        URL url = cls.getResource(name);
        if (url != null) {
            icon = new ImageIcon(url);
            if (icon.getIconWidth() == -1) {
                Log.getLogger().severe("failed to load" +  NCIOWLIcons.class.getName() + " loadImageIcon" + cls.getName() + name);
            }
        }
        return icon;
    }

}
