package org.bom.jbehaveasmunit.util;

import java.util.List;

import org.objectweb.asm.tree.AnnotationNode;

/**
 * This class helps to find out a certain attribute-value of an annotation
 * 
 * @author Carsten Severin
 *
 */
public class AnnotationValueUtil {

	public static Object getValueFromAnnotation(AnnotationNode node, String key) {
		if (node != null && node.values != null) {
			List attributes = node.values;
			for (int i = 0; attributes != null && i < attributes.size(); i += 2) {
				String key0 = (String) attributes.get(i);
				if (key0.equals(key)) {
					return attributes.get(i + 1);
				}
			}
		}
		return null;
	}

}
