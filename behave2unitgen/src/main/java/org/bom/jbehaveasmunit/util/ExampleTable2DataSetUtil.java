package org.bom.jbehaveasmunit.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bom.springtestdbunitext.DataSet;
import org.bom.springtestdbunitext.Row;
import org.jbehave.core.model.ExamplesTable;

/**
 * Little helper to transform a JBehave ExampleTable into a DataSet in order to
 * push the data from a story into dbunit.
 * 
 * @author Carsten Severin
 * 
 */
public class ExampleTable2DataSetUtil {

	/**
	 * Transforms an ExampleTable into a DataSet in order to push it into
	 * dbunit. <br/>
	 * 
	 * @param table
	 *            Data from JBehave
	 * @param tableName
	 *            Name of the Table where the data should be inserted
	 * @param ucKeyValue
	 *            uppercase the key(=column), when false key is taken as it is
	 * @return
	 */
	public static final DataSet createDataSet(ExamplesTable table,
			String tableName, HashMap<String,String> columnMapper) {

		DataSet ds = new DataSet();

		for (Iterator<Map<String, String>> it = table.getRows().iterator(); it
				.hasNext();) {
			Map<String, String> m = it.next();

			Row r = ds.row(tableName);
			
			for (Iterator<String> it2 = m.keySet().iterator(); it2.hasNext();) {
				String key = it2.next();
				String value = m.get(key);
				
				String realColumn = columnMapper.get(key.toLowerCase());
				if (realColumn !=null){
					r.attr(realColumn,
						value);
				}
			}
		}

		return ds;

	}
	
	static public class ColumnMapper{
		
		HashMap<String,String> columns = new HashMap<String, String>();
		
		public ColumnMapper addColumn(String columnInStory, String columnInDB){
			columns.put(columnInStory, columnInDB);
			return this;
		}
		
		public HashMap<String, String> getColumns(){
			return columns;
		}
		
	}
	

}
