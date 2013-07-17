package org.bom.jbehaveasmunit.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.bom.jbehaveasmunit.runner.Behave2UnitGenRunner;
import org.bom.jbehaveasmunit.util.AnnotationParameterParserUtil;
import org.bom.jbehaveasmunit.util.AnnotationParameterParserUtil.AnnotationParserResult;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.Story;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.StepType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class AnnotationParameterParserTest {

	@Test
	public void testFindParameter1() {

		String s1 = "The Name of my daughter is Pina, her last Name is Severin";
		String s2 = "The Name of my daughter is $NAME1, her last Name is $NAME2";

		AnnotationParserResult result = AnnotationParameterParserUtil.parseScenario(s2, s1, StepType.WHEN);
		Assert.assertTrue(result.isMatch());
		Assert.assertEquals("Pina", result.getValues().get("NAME1"));
		Assert.assertEquals("Severin", result.getValues().get("NAME2"));
	}

	@Test
	public void testFindParameter2() {

		String s1 = "Ich Ÿbergebe die Werte HORST, SCHNEIDER, OsnabrŸck";
		String s2 = "Ich Ÿbergebe die Werte $VORNAME, $NAME, $ORT";

		AnnotationParserResult result = AnnotationParameterParserUtil.parseScenario(s2, s1, StepType.WHEN);
		Assert.assertTrue(result.isMatch());
		Assert.assertEquals("HORST", result.getValues().get("VORNAME"));
		Assert.assertEquals("SCHNEIDER", result.getValues().get("NAME"));
		Assert.assertEquals("OsnabrŸck", result.getValues().get("ORT"));
	}

	@Test
	public void testFindParameter3() {
		String s1 = "Given a stock of symbol STK1 and a threshold of 10.0";
		String s2 = "Given a stock of symbol $symbol and a threshold of $threshold";
		
		AnnotationParserResult result = AnnotationParameterParserUtil.parseScenario(s2, s1, StepType.GIVEN);
		Assert.assertTrue(result.isMatch());
		Assert.assertEquals("10.0", result.getValues().get("threshold"));
		Assert.assertEquals("STK1", result.getValues().get("symbol"));
	}

	@Test
	public void testFindParameter4() {
		String s1 = "When the stock is traded at 5.0";
		String s2 = "when the stock is traded at $price";
		
		AnnotationParserResult result = AnnotationParameterParserUtil.parseScenario(s2, s1, StepType.WHEN);
		Assert.assertTrue(result.isMatch());
		Assert.assertEquals("5.0", result.getValues().get("price"));
	}



	@Test
	public void testWithExampleData() {
		String s1 = "Given the traders ranks are:\n " + "|name|rank|\n"
				+ "|Larry|Stooge 3|\n" + "|Moe|Stooge 1|\n"
				+ "|Curly|Stooge 2|";
		String s2 = "Given the traders ranks are: $ranksTable";

		AnnotationParserResult result = AnnotationParameterParserUtil.parseScenario(s2, s1, StepType.GIVEN);
		Assert.assertTrue(result.isMatch());
	

	}
//	
//	@Test
//	public void testWithExampleData1() throws IOException {
//		InputStream in = Behave2UnitGenRunner.class
//				.getResourceAsStream("/org/bom/stories/contactExampleTable.story");
//		BufferedReader br = new BufferedReader(new InputStreamReader(in));
//		String next;
//		StringBuffer storyText = new StringBuffer();
//		while ((next = br.readLine()) != null) {
//			storyText.append(next).append("\n");
//		}
//		
//		RegexStoryParser p = new RegexStoryParser();
//		Story s = p.parseStory(storyText.toString());
//		String step = s.getScenarios().get(0).getSteps().get(0);
//		
//		String annot = "Given the Contacts:$allContacts";
//		
//		AnnotationParserResult result = AnnotationParameterParserUtil.parseScenario(annot, step, StepType.GIVEN);
//		
//		System.out.println(">> " + result.isMatch() + "/" + result.getValues());
//
//		System.out.println("DS: " + new DataSet().toXML());
//		
//	}	
	
	
	public void testJacksonList() throws JsonParseException, JsonMappingException, IOException{
		
		String jsonString = "[{\"firstname\":\"carsten\", \"lastname\":\"severin\"},{\"firstname\":\"birgit\", \"lastname\":\"severin\"}]";
		
		ObjectMapper om = new ObjectMapper();
		Contact1[] someClassList =
			    om.readValue(jsonString, Contact1[].class);
		Assert.assertEquals(2,  someClassList.length);
		Assert.assertEquals(someClassList[0].firstname,  "carsten");
		Assert.assertEquals(someClassList[0].lastname,  "severin");
	}
		
	
	
	class Contact1{
		String firstname;
		
		String lastname;

		public String getFirstname() {
			return firstname;
		}

		public void setFirstname(String firstname) {
			this.firstname = firstname;
		}

		public String getLastname() {
			return lastname;
		}

		public void setLastname(String lastname) {
			this.lastname = lastname;
		}
		
		
	}
	
	
	@Test
	public void testWithExampleData1() throws IOException {
		InputStream in = Behave2UnitGenRunner.class
				.getResourceAsStream("/org/bom/stories/parametrisedExample.story");
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String next;
		StringBuffer storyText = new StringBuffer();
		while ((next = br.readLine()) != null) {
			storyText.append(next).append("\n");
		}
		
		 ParameterConverters parameterConverters = new ParameterConverters();
	        // factory to allow parameter conversion and loading from external resources (used by StoryParser too)
	        ExamplesTableFactory examplesTableFactory = new ExamplesTableFactory(new LocalizedKeywords(), new LoadFromClasspath(this.getClass()), parameterConverters);
		
		RegexStoryParser p = new RegexStoryParser(examplesTableFactory);
		
		Story s = p.parseStory(storyText.toString());
		String step = s.getScenarios().get(0).getSteps().get(0);

		System.out.println(">>>" + s.getScenarios().size());
		
		String annot = "Given a stock of $a and a $b";
		
		AnnotationParserResult result = AnnotationParameterParserUtil.parseScenario(annot, step, StepType.GIVEN);
		
		System.out.println(">> " + result.isMatch() + "/" + result.getValues());

		//System.out.println("DS: " + new DataSet().toXML());
		
	}	
	
}
