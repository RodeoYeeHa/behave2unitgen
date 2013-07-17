package org.bom.behave2unitgen.example.test;

import java.util.Iterator;
import java.util.List;

import org.bom.behave2unitgen.example.beans.Contact;
import org.bom.behave2unitgen.example.service.ContactDataService;
import org.bom.jbehaveasmunit.annotations.Story;
import org.bom.jbehaveasmunit.annotations.StoryParameter;
import org.bom.jbehaveasmunit.util.ExampleTable2DataSetUtil;
import org.bom.jbehaveasmunit.util.ExampleTable2DataSetUtil.ColumnMapper;
import org.bom.jbehaveasmunit.util.ExampleTableUtil;
import org.bom.springtestdbunitext.DataSet;
import org.bom.springtestdbunitext.TypedDataSetLoader;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Simple Testcase that shows how to use ExampleTables
 * 
 * @author Carsten Severin
 *
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:application-context.xml" })
@DbUnitConfiguration(dataSetLoader = TypedDataSetLoader.class)
@Story(name = "/example/stories/contactExampleTable.story")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class,
		TransactionalTestExecutionListener.class,
		DbUnitTestExecutionListener.class })
@FixMethodOrder(MethodSorters.JVM)
public class ContactExampleTableStoryTest {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ContactDataService service;
	
	@Test
	@Given(value = "Given the Contacts:$allContacts")
	@DatabaseSetup(type=DatabaseOperation.CLEAN_INSERT, value="method:getInitData")
	public void testGiven() {
		// nothing to do because everything is done by @DatabaseSetup-Annotation
	}

	@Test
	@When(value = "When Contacts are subset to $filter by firstname")
	@Then(value = "Then the Contacts returned are:$selectedContacts")
	public void whenThen() {

		List<Contact> result = service.findContacts(getFilter());
		
		// Lade die Liste der Namen
		ExamplesTable ex = getSelectedContacts();
	
		Assert.assertEquals(ex.getRowCount(), result.size());
		for (Iterator<Contact> it = result.iterator(); it.hasNext();){
			Assert.assertEquals(true, ExampleTableUtil.contains(ex, "firstname", it.next().getFirstname(), true));
		}
		
	}
	
	public static DataSet getInitData() {
		return ExampleTable2DataSetUtil.createDataSet(getAllContacts(),
				"CONTACT",
				new ColumnMapper().addColumn("firstname", "FIRST_NAME")
						.addColumn("lastname", "LAST_NAME").getColumns());
	}


	
	@StoryParameter(name = "allContacts")
	public static ExamplesTable getAllContacts() {
		// behave2unitgen will implement that for you
		return null;
	}

	@StoryParameter(name = "selectedContacts")
	public static ExamplesTable getSelectedContacts() {
		// behave2unitgen will implement that for you
		return null;
	}

	@StoryParameter(name = "filter")
	public static String getFilter() {
		// behave2unitgen will implement that for you
		return null;
	}
	
}
