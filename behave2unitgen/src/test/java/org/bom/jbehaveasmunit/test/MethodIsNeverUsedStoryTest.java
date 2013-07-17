package org.bom.jbehaveasmunit.test;

import org.bom.jbehaveasmunit.annotations.Story;
import org.bom.springtestdbunit.TypedDataSetLoader;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.springtestdbunit.annotation.DbUnitConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations =  { "classpath:application-context.xml" })
@DbUnitConfiguration(dataSetLoader=TypedDataSetLoader.class)
@Story(name="/org/bom/stories/test1.story")
public class MethodIsNeverUsedStoryTest {

	@When("xy")
	public void testThatIsNeverUsed_When(){
		
	}

	@Then("xy")
	public void testThatIsNeverUsed_Then(){
		
	}
	
	@Given("xy")
	public void testThatIsNeverUsed_Given(){
		
	}
	
}
