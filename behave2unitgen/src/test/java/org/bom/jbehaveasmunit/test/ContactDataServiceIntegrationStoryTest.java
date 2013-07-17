package org.bom.jbehaveasmunit.test;


import org.bom.jbehaveasmunit.annotations.Story;
import org.bom.springtestdbunitext.DataSet;
import org.bom.springtestdbunitext.TypedDataSetLoader;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations =  { "classpath:application-context.xml" })
@DbUnitConfiguration(dataSetLoader=TypedDataSetLoader.class)
@Story(name="/org/bom/stories/test1.story")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@FixMethodOrder(MethodSorters.JVM) 
public class ContactDataServiceIntegrationStoryTest {

	static int step;
	
	@Test
	@DatabaseSetup(type=DatabaseOperation.CLEAN_INSERT, value="method:dataTestInsert")
	@ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value="method:dataTestSelect")
	public void testInsert(){
		
	}
	

	@Test
	@org.jbehave.core.annotations.Given(value="GIVEN_STEP1")
	public void test_Given1(){
		if (step==0)
		step = 1;
	}

	@Test
	@org.jbehave.core.annotations.Given(value="GIVEN_STEP2")
	public void test_Given2(){
		step = 2;
	}

	
	@Test
	@org.jbehave.core.annotations.Given(value="GIVEN_STEP3")
	public void test_Given3(){
		step = 3;
	}

	@Test
	@org.jbehave.core.annotations.When(value="WHEN_STEP1")
	public void test_When1(){
		step *= 2;
	}
	
	@Test
	@org.jbehave.core.annotations.When(value="WHEN_STEP2")
	public void test_When2(){
		step *= 4;
	}
	
	@Test
	@org.jbehave.core.annotations.When(value="WHEN_STEP3")
	public void testWhen3(){
		step *= 6;
	}
	
	
	@Test
	@org.jbehave.core.annotations.Then(value="THEN_STEP1")
	public void test_Then1(){
		Assert.assertEquals(2,  step);
	}
	
	@Test
	@org.jbehave.core.annotations.Then(value="THEN_STEP2")
	public void test_Then2(){
		Assert.assertEquals(8,  step);
	}
	
	@Test
	@org.jbehave.core.annotations.Then(value="THEN_STEP3")
	public void test_Then3(){
		Assert.assertEquals(18,  step);
	}
	
	public static org.bom.springtestdbunitext.DataSet dataTestSelect(){
		DataSet ds = new DataSet();
		ds.row("CONTACT").attr("FIRST_NAME",  "Pina").attr("LAST_NAME", "Severin");
		ds.row("CONTACT").attr("FIRST_NAME",  "Carsten").attr("LAST_NAME", "Severin");
		return ds;
	}
	
	public static DataSet dataTestInsert(){
		DataSet ds = new DataSet();
		ds.row("CONTACT").attr("ID", "1").attr("FIRST_NAME",  "Pina").attr("LAST_NAME", "Severin");
		ds.row("CONTACT").attr("ID", "2").attr("FIRST_NAME",  "Carsten").attr("LAST_NAME", "Severin");	
		return ds;
	}
	
}
