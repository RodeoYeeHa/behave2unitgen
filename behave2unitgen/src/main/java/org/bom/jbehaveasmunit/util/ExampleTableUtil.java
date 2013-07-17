package org.bom.jbehaveasmunit.util;

import java.util.Iterator;
import java.util.Map;

import org.jbehave.core.model.ExamplesTable;

/**
 * Util to check and find rows/values in an ExampleTable
 * 
 * @author Carsten Severin
 * 
 */
public class ExampleTableUtil {

	/**
	 * Checks wether there is a certain value in a certain column
	 * 
	 * @param t
	 *            ExampleTable
	 * @param column
	 *            Name of the Column
	 * @param value
	 *            Value to compare
	 * @param caseSensitive
	 *            Compare value lowercase or raw
	 * @return true, if there is the expected value in the excepted column
	 */
	public static boolean contains(ExamplesTable t, String column,
			String value, boolean caseSensitive) {
		for (Iterator<Map<String, String>> it = t.getRows().iterator(); it
				.hasNext();) {
			Map<String, String> row = it.next();
			for (Iterator<String> it2 = row.keySet().iterator(); it2.hasNext();) {
				String key = it2.next();
				if (key.toLowerCase().equals(column.toLowerCase())) {

					String value0 = row.get(key);

					if (caseSensitive) {
						if (value0.equals(value)) {
							return true;
						}
					} else {
						if (value0.toLowerCase().equals(value.toLowerCase())) {
							return true;
						}
					}

					continue;

				}

			}
		}
		return false;
	}

}
