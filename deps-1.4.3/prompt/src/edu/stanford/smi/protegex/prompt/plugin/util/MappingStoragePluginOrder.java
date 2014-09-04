/*
 * Contributor(s): Natasha Noy noy@smi.stanford.edu
 */
package edu.stanford.smi.protegex.prompt.plugin.util;

import java.util.*;

import edu.stanford.smi.protegex.prompt.plugin.model.*;

public class MappingStoragePluginOrder {
	public static void putSimplePluginFirst (LinkedList<MappingStoragePlugin> list) {
		if (list.isEmpty()) {
			return;
		}
		MappingStoragePlugin first = list.getFirst();
		if (shouldBeFirst (first))
			return;
		Iterator<MappingStoragePlugin> i = list.iterator();
		while (i.hasNext()) {
			MappingStoragePlugin next = i.next();
			if (shouldBeFirst (next)) {
				i.remove();
				list.addFirst(next);
				return;
			}
		}
			
	}
	
	private static boolean shouldBeFirst (MappingStoragePlugin plugin) {
		return plugin.getClass().getName().endsWith("SimpleMappingStorage");
	}

}
