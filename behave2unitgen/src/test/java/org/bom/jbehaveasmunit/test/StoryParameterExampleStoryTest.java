package org.bom.jbehaveasmunit.test;

import org.junit.Assert;
import org.junit.FixMethodOrder;

import org.bom.jbehaveasmunit.annotations.Story;
import org.bom.jbehaveasmunit.annotations.StoryParameter;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.MethodSorters;

@RunWith(BlockJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.JVM)
@Story(name="/org/bom/stories/exampleWithParameter.story")
public class StoryParameterExampleStoryTest {

	@Test
    @Given("given a stock of symbol $symbol and a threshold of $threshold")
    public void aStock() {
        // nothing to do
    }
 
	@Test
    @When("when the stock is traded at $price")
    public void theStockIsTradedAt() {
		// nothing to do
    }
 
	@Test
    @Then("then the alert status should be $status")
    public void theAlertStatusShouldBe() {
       
		Assert.assertEquals(getStockName(), getStatus(), StockCalculator.isStack_ON(getThreshold(), getTradedValue()));
		
    }


	@StoryParameter(name="symbol")
	public static String getStockName(){
		return "";
	}
	
	@StoryParameter(name="threshold")
	public static double getThreshold(){
		return -1;
	}
	
	@StoryParameter(name="price")
	public static double getTradedValue(){
		return -1;
	}

	@StoryParameter(name="status")
	public static boolean getStatus(){
		return new Boolean("true").booleanValue();
	}
	
	
	
}
