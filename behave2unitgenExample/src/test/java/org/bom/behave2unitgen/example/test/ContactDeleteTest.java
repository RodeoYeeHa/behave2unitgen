package org.bom.behave2unitgen.example.test;

import org.bom.behave2unitgen.example.service.ContactDataService;
import org.bom.springtestdbunitext.DataSet;
import org.bom.springtestdbunitext.TypedDataSetLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
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


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:application-context.xml" })
@DbUnitConfiguration(dataSetLoader = TypedDataSetLoader.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class,
		TransactionalTestExecutionListener.class,
		DbUnitTestExecutionListener.class })
public class ContactDeleteTest {

	@Autowired
	private ContactDataService service;
	
	@Test
	@DatabaseSetup(type=DatabaseOperation.CLEAN_INSERT, value="method:setup")
	@ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value="classpath:dbunit/testdata/contactDeleteCompare.xml")
	public void testDeleteDelete(){
		service.deleteContactByLastnameAndOwner("Severin",  "umeier");
	}
	
	public static DataSet setup(){
		DataSet ds = new DataSet();
		ds.row("CONTACT").attr("FIRST_NAME",  "Pina").attr("LAST_NAME",  "Schmidt").attr("OWNER", "umeier");	
		ds.row("CONTACT").attr("FIRST_NAME",  "Carsten").attr("LAST_NAME",  "Severin").attr("OWNER", "umeier");	
		ds.row("CONTACT").attr("FIRST_NAME",  "Carsten").attr("LAST_NAME",  "Severin").attr("OWNER", "hschmidt");	
		return ds;
	}
	
}
