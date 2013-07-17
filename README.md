behave2unitgen
==============

Behave2UnitGen generated executable JUnit-Testcases from a set of given Story-files. It uses ASM to generate the Testcases.

The project depends on JBehave:
http://jbehave.org

It also depends on JUnit:
https://github.com/junit-team/junit

The Examples also depend on Unit testing with Spring and DBUnit:
https://github.com/springtestdbunit/spring-test-dbunit

When you want to inject Testdata from the Stories via JSON, you also need the Jackson-Library.

Introduction
============
Why generating Unittests out of Behave-Stories instead of using JBehave directly? 

JBehave come with an excellent JUnit-Integration. You can extend your Testcases from JUnitStories or from JUnitStory. The testclass then is a bridge between your stories an the Step-Class.

First, in most cases, i would like to write a Unittests that depends on one single Story. I such simple cases, there is no need to have the Testcase and the Steps separated. Second, using JUnitStory or JUnitStories forces you to use special implementation e.g. for Spring- and DBUnit-Integration.

The idea is to write your unit-tests as they are an "connect" them to the stories.

When you never used JBehave, you could ask, if this realy helps you writing your tests. JBehave helps both: the specification (it becomes more precise by explaning you requirements in different examples) as well as your test.

While using JBehave, you can separate the test data from the Testcases. You can write a single Testcase an run this Testcase multiple times according to the number of examples (=Scenarios) that belong to the story.

With JBehave, testing can become very easy. You can combine it perfectly with Sipring-Test-Framework and DBUnit. This is especially useful if you want to test your Web-Applications with integration-tests.

To come back to the question of the beginning: why generate the Testcases instead of using JBehave directly? As you will see in the examples, this way, JBehave is extremely simple to use. Using it with gradle means, the generation takes place just before testing. In most cases, you would not even take notice of it.

When you begin to dive in into the world of JBehave, this can be a good starting point. Of cause, when you want to use all the features of JBehave, you should use it the way it is.
  
Lets begin to drive in into the world of behave :-)


How to use behave2unitgen
=========================

Use DBUnit
==========
Most of our Web-Applications rely on database-access-code that can only be testet completely with integration tests. 

DBUnit is extremely useful for integration tests for it provides you some features to fill the database before your tests and to compare the data after the test with the expected values.

There is a library called "spring-test-dbunit" that integrates the Spring Test Framework will DBUnit. 

The requirement we want to test is: 

    The system must allow the user to delete any of his contacts. The user is not allowed to delete contacts of other owners. 

This example show how difficult it is to describe a relatively complex requirement. The following testcase make shure, that e.g. when a Contact with the name 'Severin' exists for multiple owners, the user can only delete his own Contacts: 

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = { "classpath:application-context.xml" })
    @TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
       		DirtiesContextTestExecutionListener.class,
    		TransactionalTestExecutionListener.class,
    		DbUnitTestExecutionListener.class })
    public class ContactDeleteTest {

    	@Autowired
    	private ContactDataService service;
	
    	@Test
    	@DatabaseSetup(type=DatabaseOperation.CLEAN_INSERT, value="classpath:dbunit/testdata/contactDeleteSetup.xml")
    	@ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value="classpath:dbunit/testdata/contactDeleteCompare.xml")
    	public void testDeleteDelete(){
    		service.deleteContactByLastnameAndOwner("Severin",  "umeier");
    	}
    	
    } 

This example is completely without BDD. With DBUnit i can simply define XML-Data for the @DatabaseSetup:

    <?xml version="1.0" encoding="UTF-8"?>
    <dataset>
      <CONTACT FIRST_NAME="Pina" LAST_NAME="Schmidt" OWNER="umeier"/>
      <CONTACT FIRST_NAME="Carsten" LAST_NAME="Severin" OWNER="umeier"/>
      <CONTACT FIRST_NAME="Carsten" LAST_NAME="Severin" OWNER="hschmidt"/>
    </dataset>

And i define, how the Data should look like after i deleted 'Severin' for owner 'umeier':

    <?xml version="1.0" encoding="UTF-8"?>
    <dataset>
      <CONTACT FIRST_NAME="Pina" LAST_NAME="Schmidt" OWNER="umeier"/>
      <CONTACT FIRST_NAME="Carsten" LAST_NAME="Severin" OWNER="hschmidt"/>
    </dataset>

Very simple so far! Spring-Test-Framework together with DBUnit make it very easy to write an integration test!





Example 1: TODO
===============

...
 




