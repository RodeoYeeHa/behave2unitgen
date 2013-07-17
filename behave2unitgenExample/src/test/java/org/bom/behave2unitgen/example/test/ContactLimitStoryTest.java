package org.bom.behave2unitgen.example.test;

import org.bom.behave2unitgen.example.beans.Contact;
import org.bom.behave2unitgen.example.service.ContactDataService;
import org.bom.behave2unitgen.example.service.TooManyContactsException;
import org.bom.jbehaveasmunit.annotations.Story;
import org.bom.jbehaveasmunit.annotations.StoryParameter;
import org.bom.springtestdbunit.DataSet;
import org.bom.springtestdbunit.TypedDataSetLoader;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * Simple Testcase that shows how to generate a lot (>=100 datasets) with DBUnit according to the referenced story
 * 
 * @author Carsten Severin
 *
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:application-context.xml" })
@DbUnitConfiguration(dataSetLoader = TypedDataSetLoader.class)
@Story(name = "/example/stories/contactLimit.story")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class,
		TransactionalTestExecutionListener.class,
		DbUnitTestExecutionListener.class })
@FixMethodOrder(MethodSorters.JVM)
public class ContactLimitStoryTest {

	@Autowired
	private ContactDataService service;

	static final String[] firstname = { "horst", "peter", "paula", "tina",
			"ronja", "norbert" };

	static final String[] lastname = { "schmidt", "schneider", "mueller",
			"hansen", "schroeder", "albrecht" };

	
	
	@Test
	@DatabaseSetup(type=DatabaseOperation.CLEAN_INSERT, value="method:insertData")
	@When("When insert one dataset")
	@Then("Then $maximum datasets are stored")
	public void insertSucceed(){
		Contact c = new Contact("pina", "severin");
		try {
			service.insertContact(c);
			Contact storedContact = service.getContact(c.getId());
			Assert.assertNotNull(storedContact);
			Assert.assertEquals(c.getFirstname(),  storedContact.getFirstname());
			Assert.assertEquals(c.getLastname(),  storedContact.getLastname());
			Assert.assertEquals(getMaximum() + 1,  service.countContact());
		} catch (TooManyContactsException e) {
			Assert.assertTrue(e.toString() + " not expected with " + getMaximum() + " records in total", false);
		}
	}	


	@Test(expected=TooManyContactsException.class)
	@DatabaseSetup(type=DatabaseOperation.CLEAN_INSERT, value="method:insertData")
	@When("When insert one dataset")
	@Then("Then insert fails")
	public void insertFail() throws TooManyContactsException{
		Contact c = new Contact("pina", "severin");
			service.insertContact(c);
	}
	
	@StoryParameter(name="maximum")
	public static int getMaximum(){
		// behave2unitgen will implement that for you
		return -1;
	}
	
	@Given(value="Given $maximum datasets")
	public static DataSet insertData() {
		DataSet ds = new DataSet();
		for (int i = 0; i < getMaximum() ; i++) {
			int index1 = (int) (Math.random() * 5.0);
			int index2 = (int) (Math.random() * 5.0);
			ds.row("CONTACT").attr("ID", String.valueOf(i))
					.attr("FIRST_NAME", firstname[index1])
					.attr("LAST_NAME", lastname[index2]);
		}
		return ds;
	}

}
