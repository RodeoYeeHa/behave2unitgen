package org.bom.jbehaveasmunit.runner;

import java.util.List;
import java.util.StringTokenizer;

import org.bom.jbehaveasmunit.core.ClassManipulator;
import org.bom.jbehaveasmunit.core.ConfigParameter;
import org.bom.jbehaveasmunit.core.StoryReader;
import org.slf4j.Logger;
import org.springframework.core.io.Resource;

/**
 * Java-Runner to be run in gradle/maven
 * Runs the behave2unitgen-generation with grade, maven or as executable java
 * 
 * @author Carsten Severin
 *
 */
public class Behave2UnitGenRunner {

	static Logger logger = org.slf4j.LoggerFactory.getLogger(Behave2UnitGenRunner.class);
	
	/**
	 * Main-Class ist started via gradle
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		long t1 = System.currentTimeMillis();
		
		StoryReader storyReader = new StoryReader(true);

		ConfigParameterImpl configBean = new ConfigParameterImpl();
		configBean.parseParameter(args);

		ClassManipulator cm = new ClassManipulator(storyReader, configBean);

		// cm.insertParameterMethod(params.get(BUILD_PATH_TEST));

		Resource[] resources = cm
				.findUnitTests();

		for (int i = 0; i < resources.length; i++) {
			logger.debug("Open Test-Class: " + resources[i].getFilename());
			boolean done = cm.manipulateClass(resources[i]);
			if (done){
				logger.debug("Test-Class " + resources[i].getFilename() + " was processed");
			}else{
				logger.debug("Test-Class " + resources[i].getFilename() + " does not contain the @Story-Annotation and is not processed!");
			}
		}

		// Unread stories
		if (configBean.getParseUnusedStories()){
			List<String> unread = storyReader.listUnusedStories();
			cm.storyNotImplemented(unread);
		}
	
		logger.warn("Behave2UnitGen finished in " + (System.currentTimeMillis() - t1) + "msecs.");

	}

	static class ConfigParameterImpl extends ConfigParameter {

		private static final String SEARCH_PATTERN_TEST_CLASSES = "searchPatternTestClasses";
		private static final String BUILD_PATH_TEST = "outDir";
		private static final String PARSE_UNUSED_STORIES = "parseUnusedStories";

		public void parseParameter(String s[]) {
			if (s != null) {
				for (int i = 0; i < s.length; i++) {
					StringTokenizer st = new StringTokenizer(s[i], "=");
					if (st.countTokens() == 2) {
						String key = st.nextToken();
						String value = st.nextToken();

						if (SEARCH_PATTERN_TEST_CLASSES.equals(key)) {
							this.setSearchPatternTestClasses(value);
						} else if (BUILD_PATH_TEST.equals(key)) {
							this.setOutDir(value);
						} else if (PARSE_UNUSED_STORIES.equals(key)) {
							this.setParseUnusedStories("true".equals(value));
						} else {
							throw new RuntimeException("Unknown parameter: "
									+ key + ", allowed: "
									+ SEARCH_PATTERN_TEST_CLASSES + ";"
									+ BUILD_PATH_TEST + ";"
									+ PARSE_UNUSED_STORIES);
						}

					} else {
						throw new RuntimeException("Parameter " + s[i]
								+ " has no Value");
					}
				}
			}
		}

	}

}