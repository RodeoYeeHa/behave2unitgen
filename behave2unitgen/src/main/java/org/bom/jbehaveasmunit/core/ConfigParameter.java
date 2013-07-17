package org.bom.jbehaveasmunit.core;

/**
 * Bean-Class to read an pass the configuration. This class also contains the default-Configuration,
 * that can be overridden by using the setter()-methods.
 * 
 * @author Carsten Severin
 **/
public class ConfigParameter {

	String outDir = "build/classes/test/";
	
	boolean parseUnusedStories = false;
	
	String searchPatternTestClasses = "**/*StoryTest.class";
	
	boolean failOnParameterIsNull = true;

	public String getOutDir() {
		return outDir;
	}

	public void setOutDir(String outDir) {
		this.outDir = outDir;
	}

	public boolean getParseUnusedStories() {
		return parseUnusedStories;
	}

	public void setParseUnusedStories(boolean parseUnusedStories) {
		this.parseUnusedStories = parseUnusedStories;
	}


	public String getSearchPatternTestClasses() {
		return searchPatternTestClasses;
	}

	public void setSearchPatternTestClasses(String searchPatternTestClasses) {
		this.searchPatternTestClasses = searchPatternTestClasses;
	}

	public boolean getFailOnParameterIsNull() {
		return failOnParameterIsNull;
	}

	public void setFailOnParameterIsNull(boolean failOnParameterIsNull) {
		this.failOnParameterIsNull = failOnParameterIsNull;
	}

	
	
}
