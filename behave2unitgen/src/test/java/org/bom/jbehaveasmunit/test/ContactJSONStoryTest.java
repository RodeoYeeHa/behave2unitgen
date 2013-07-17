package org.bom.jbehaveasmunit.test;

import java.util.ArrayList;


import org.bom.jbehaveasmunit.annotations.Story;
import org.bom.jbehaveasmunit.annotations.StoryParameter;
import org.bom.jbehaveasmunit.beans.JSONList;
import org.bom.jbehaveasmunit.beans.JSONObject;
import org.bom.springtestdbunitext.DataSet;
import org.bom.springtestdbunitext.TypedDataSetLoader;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:application-context.xml" })
@DbUnitConfiguration(dataSetLoader = TypedDataSetLoader.class)
@Story(name = "/org/bom/stories/contactJSON.story")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class,
		TransactionalTestExecutionListener.class,
		DbUnitTestExecutionListener.class })
@FixMethodOrder(MethodSorters.JVM)
public class ContactJSONStoryTest {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Test
	@Given(value = "Given the Contacts:$allContacts")
	@DatabaseSetup(type=DatabaseOperation.CLEAN_INSERT, value="method:getInitData")
	public void testGiven() {
		// Nothing to do
	}

	@Test
	@When(value = "When Contacts are subset to $filter by firstname")
	@Then(value = "Then the Contacts returned are:$selectedContacts")
	public void whenThen() {

		// Lade die Liste der Namen
		ArrayList<String>firstnames = new ArrayList<String>();
		ContactTestBean[] selected = getSelectedContacts().getList();
		for (int i=0; i < selected.length; i++){
			ContactTestBean b = selected[i];
			firstnames.add(b.getFirstname());
		}
		
		Assert.assertEquals(true, firstnames.contains("Carsten"));
		Assert.assertEquals(true, firstnames.contains("Johan"));
		Assert.assertEquals(false, firstnames.contains("Birgit"));
		Assert.assertEquals(false, firstnames.contains("Pina"));
		
	}

	
	public static DataSet getInitData() {
		ContactTestBean[] all = getAllContacts().getList();
		DataSet ds = new DataSet();
		for (int i=0; i < all.length; i++){
			ds.row("CONTACT").attr("first_name", all[i].getFirstname()).attr("last_name", all[i].getLastname());
		}
		return ds;
	}


	
	@StoryParameter(name = "allContacts")
	public static JSONList<ContactTestBean> getAllContacts() {
		return null;
	}

	@StoryParameter(name = "selectedContacts")
	public static JSONList<ContactTestBean> getSelectedContacts() {
		return null;
	}

	@StoryParameter(name = "filter")
	public JSONObject<ContactFilterBean> getFilter() {
		return null;
	}
	
}
