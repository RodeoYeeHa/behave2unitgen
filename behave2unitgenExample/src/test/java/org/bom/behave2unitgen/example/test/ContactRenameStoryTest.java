package org.bom.behave2unitgen.example.test;

import java.util.HashMap;

import org.bom.behave2unitgen.example.service.ContactDataService;
import org.bom.jbehaveasmunit.annotations.Story;
import org.bom.jbehaveasmunit.annotations.StoryParameter;
import org.bom.jbehaveasmunit.beans.JSONObject;
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
 * 
 * Simple Testcase that shows how to use JSON-Strings with Jackson-Parser
 * instead of the standard ExampleTable. In this case, a single JSON-String is
 * read instead of a list.
 * 
 * @author Carsten Severin
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:application-context.xml" })
@DbUnitConfiguration(dataSetLoader = TypedDataSetLoader.class)
@Story(name = "/example/stories/contactRenameJSON.story")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class,
		TransactionalTestExecutionListener.class,
		DbUnitTestExecutionListener.class })
@FixMethodOrder(MethodSorters.JVM)
public class ContactRenameStoryTest {

	
	@Autowired
	private ContactDataService service;
	
	@Test
	@Given(value="Given the Contacts:$allContactsBefore")
	@DatabaseSetup(type=DatabaseOperation.CLEAN_INSERT, value="method:getInitData")
	public void givenTheContacts(){
		// nothing to do because everything is done by @DatabaseSetup-Annotation
	}
	
	@Test
	@When(value="When the contact is renamed:$renameValues")
	public void whenContactIsRenamed(){
		ContactRenameValuesBean b = getRenameValues().getObject();
		service.renameContact(b.getLastnameOld(), b.getLastnameNew());
	}
	
	@Test
	@Then(value="Then the Contacts returned are:$allContactsAfter")
	@ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value="method:getResultData")
	public void ThenContactAre(){
		// nothing to do because everything is done by @ExpectedDatabase-Annotation
	}
	
	@StoryParameter(name="renameValues")
	public JSONObject<ContactRenameValuesBean> getRenameValues(){
		return null;
	}
	
	public static DataSet getInitData(){
		// In this method, you can generate all DataSets you need
		return ExampleTable2DataSetUtil.createDataSet(getAllContactsBefore(), "CONTACT", createColumnMapper());
	}

	public static DataSet getResultData(){
		// In this method, you can generate all DataSets you need
		return ExampleTable2DataSetUtil.createDataSet(getAllContactsAfter(), "CONTACT", createColumnMapper());
	}
	
	@StoryParameter(name="allContactsBefore")
	public static ExamplesTable getAllContactsBefore(){
		// will be implementied by behave2unitgen
		return null;
	}
	
	@StoryParameter(name="allContactsAfter")
	public static ExamplesTable getAllContactsAfter(){
		// will be implementied by behave2unitgen
		return null;
	}
	
	public static HashMap<String,String> createColumnMapper(){
		return new ColumnMapper().addColumn("firstname", "FIRST_NAME")
		.addColumn("lastname", "LAST_NAME").getColumns();
	}
	
	
}
