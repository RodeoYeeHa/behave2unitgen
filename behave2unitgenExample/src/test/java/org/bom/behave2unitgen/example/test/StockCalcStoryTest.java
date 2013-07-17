package org.bom.behave2unitgen.example.test;

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
@Story(name="/example/stories/stockCalcExample.story")
public class StockCalcStoryTest {

	@Test
    @Given("given a stock of symbol $symbol and a threshold of $threshold")
    public void givenIsAStock() {
		// nothing to do because @Given filters out the $symbol and $threshold to be used later
    }
 
	@Test
    @When("when the stock is traded at $price")
    public void theStockIsTradedAt() {
		// nothing to do because @When filters out the $price to be used later
    }
 
	@Test
    @Then("then the alert status should be $status")
    public void theAlertStatusShouldBe() {
		Assert.assertEquals(getStockName(), getStatus(), StockCalculator.isStock_ON(getThreshold(), getTradedValue()));
    }


	@StoryParameter(name="symbol")
	public String getStockName(){
		return "";
	}
	
	@StoryParameter(name="threshold")
	public double getThreshold(){
		return -1;
	}
	
	@StoryParameter(name="price")
	public double getTradedValue(){
		return -1;
	}

	@StoryParameter(name="status")
	public boolean getStatus(){
		return true;
	}
	
	
	
}
