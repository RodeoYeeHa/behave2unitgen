package org.bom.jbehaveasmunit.util;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.parsers.StepMatcher;
import org.jbehave.core.steps.StepType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Carsten Severin
 *
 */
public class AnnotationParameterParserUtil {
	
	static Logger logger = LoggerFactory.getLogger(AnnotationParameterParserUtil.class);
	
	static Pattern PATTERN_GIVEN = Pattern.compile("given(:)?(\\ *)");
	
	static Pattern PATTERN_WHEN = Pattern.compile("when(:)?(\\ *)");
	
	static Pattern PATTERN_THEN = Pattern.compile("then(:)?(\\ *)");
	
	public static AnnotationParserResult parseScenario(String stringWithPlaceholder, String stringWithValues, StepType type){
		
		stringWithPlaceholder = removeTypeFromString(stringWithPlaceholder, type);
		stringWithValues = removeTypeFromString(stringWithValues, type);
		
		RegexPrefixCapturingPatternParser p = new RegexPrefixCapturingPatternParser();
		StepMatcher m2 = p.parseStep(StepType.GIVEN, stringWithPlaceholder);

		boolean match = m2.matches(stringWithValues);
		HashMap<String,String>values = new HashMap<String, String>();
		if (match){
			String s[] = m2.parameterNames();
			for (int i = 0; i < s.length; i++) {
				values.put(s[i], m2.parameter(i + 1));
			}			
		}
		
		AnnotationParserResult result = new AnnotationParserResult();
		result.setMatch(match);
		result.setValues(values);
		
		return result;
	}
	
	
	private static String removeTypeFromString(String s, StepType t){
		
		Matcher m = null;
		if (t==StepType.GIVEN){
			m =PATTERN_GIVEN.matcher(s.toLowerCase());	
		}
		if (t==StepType.WHEN){
			m =PATTERN_WHEN.matcher(s.toLowerCase());	
		}
		if (t==StepType.THEN){
			m =PATTERN_THEN.matcher(s.toLowerCase());	
		}	
		
		if (m!=null){
			if(m.find()){
				return s.substring(m.end());
			}			
		}
		
		return s;
	}
	

	public static class AnnotationParserResult{
		
		boolean match;
		
		HashMap<String,String> values;

		public boolean isMatch() {
			return match;
		}

		public void setMatch(boolean match) {
			this.match = match;
		}

		public HashMap<String, String> getValues() {
			return values;
		}

		public void setValues(HashMap<String, String> values) {
			this.values = values;
		}
		
		
	}
	
	
}
