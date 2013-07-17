package org.bom.behave2unitgen.example.test;

import java.util.HashMap;

import org.bom.behave2unitgen.example.service.ContactDataService;
import org.bom.jbehaveasmunit.annotations.Story;
import org.bom.jbehaveasmunit.annotations.StoryParameter;
import org.bom.jbehaveasmunit.util.ExampleTable2DataSetUtil;
import org.bom.jbehaveasmunit.util.ExampleTable2DataSetUtil.ColumnMapper;
import org.bom.springtestdbunitext.DataSet;
import org.bom.springtestdbunitext.TypedDataSetLoader;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
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
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
/**
 * Simple Testcase to Delete a Contact
 * 
 * @author Carsten Severin
 *
 */


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:application-context.xml" })
@DbUnitConfiguration(dataSetLoader = TypedDataSetLoader.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class,
		TransactionalTestExecutionListener.class,
		DbUnitTestExecutionListener.class })
@Story(name = "/example/stories/contactDelete.story")
@FixMethodOrder(MethodSorters.JVM)
public class ContactDeleteStoryTest {

	@Autowired
	private ContactDataService service;

	@Test
	@Given(value = "Given the Contacts:$contactsBefore")
	@DatabaseSetup(type = DatabaseOperation.CLEAN_INSERT, value = "method:getInitData")
	public void givenThreeContacts() {
		// Nothing to do here
	}

	@Test
	@When(value = "When Contact with Lastname $lastname is deleted by Owner $owner")
	public void whenAContactIsDeleted() {
		service.deleteContactByLastnameAndOwner(getLastname(), getOwner());
	}

	@Test
	@Then(value = "Then the Contacts are:$contactsAfter")
	@ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value="method:getResultData")
	public void thenTwoContacts() {
		// Nothing to do here
	}
	
	@StoryParameter(name = "contactsAfter")
	public static ExamplesTable getContactsAfter() {
		// return null because method will be implemented by behave2unitgen
		return null;
	}
	
	public static DataSet getResultData() {
		return ExampleTable2DataSetUtil.createDataSet(getContactsAfter(),
				"CONTACT", createColumnMapper());
	}
	
	
	@StoryParameter(name="lastname")
	public String getLastname() {
		// return null because method will be implemented by behave2unitgen
		return null;
	}

	@StoryParameter(name="owner")
	public String getOwner() {
		// return null because method will be implemented by behave2unitgen
		return null;
	}

	public static HashMap<String, String> createColumnMapper() {
		return new ColumnMapper().addColumn("firstname", "FIRST_NAME")
				.addColumn("lastname", "LAST_NAME").addColumn("owner", "OWNER")
				.getColumns();
	}

	public static DataSet getInitData() {
		return ExampleTable2DataSetUtil.createDataSet(getContactsBefore(),
				"CONTACT", createColumnMapper());
	}

	@StoryParameter(name = "contactsBefore")
	public static ExamplesTable getContactsBefore() {
		// return null because method will be implemented by behave2unitgen
		return null;
	}

}
