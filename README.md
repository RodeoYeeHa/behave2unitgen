behave2unitgen
==============

ATTENTION!!! THIS DESCRIPTION IS STILL IN PROGRESS!!!

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

Behavior Driven Development (BDD)
=================================

I first heard about BDD when I started reading the book "Specification by Example". I was confused at first when they used the term "executable specification". How can one execute a specification?

In most projects i have seen, there are 3 different types of tests:
- Unit Tests
- Integration Tests
- Acceptance Tests

Unit Tests and Integration Tests are usually written by the developer himself. They are often far too technical and too detailed for a business specification. On the other hand, the Acceptance Tests are often executed manually in order to test the business specification. From the point of view of a developer, they are often not detailed an precise enough.

The idea of BDD is the following:
- Make the specification better by adding examples to it
- Make the requirements executable by using the examples for Integration Tests
- Track your Project status any time (how much of our Project is implemented so far?)
- Make your specification testable automated 


Once you have implemented the Unit Tests for your requirements, you can test then any time.

How to use behave2unitgen
=========================

TODO

Use DBUnit
==========
Most of our web applications rely on database queries that can only be testet completely with Integration Tests. 

DBUnit is extremely useful for Integration Tests for it provides you some features to prepare the database before your tests start and even to compare the data after the test run with the expected values automatically.

There is a library called "spring-test-dbunit" that integrates the Spring Test Framework will DBUnit. 

Lets assume, we want to implement an adressbook application. The requirement we want to test is: 

    The system must allow the user to delete any of his contacts. 
    The user is not allowed to delete contacts of other owners. 

This example shows how difficult it is to describe a relatively simple requirement. Let's write a DBUnit-Test for this. In this example, there are two contacts with the same names stored for different owners. One ower should only be able to delete his or her database entry: 

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

This example is completely written without BDD. With DBUnit i can simply define XML-Data for the @DatabaseSetup:

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

Very simple so far! Spring-Test-Framework together with DBUnit make it very easy to write an Integration Test!

If the Testdata is too small for a separate XML-File or if you want to geneate the Testdata, you can simply define the data within the test class in a static method. Therefore, you have to extend the class level Annotations a bit:

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = { "classpath:application-context.xml" })
    @DbUnitConfiguration(dataSetLoader = TypedDataSetLoader.class)
    @TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    		DirtiesContextTestExecutionListener.class,
    		TransactionalTestExecutionListener.class,
    		DbUnitTestExecutionListener.class })
    public class ContactDeleteTest { 
    ...

The @DBUnitConfiguration-Annotation is imporant to notice. Here you define a special DataSetLoader, that enables you to reference methods instead of XML-Files. Here is how you use it:

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

The @DatabaseSetup-Annotation uses a value prefixed with "method:", which means, it should read the test data out of the method setup().

Keep this in mind for later. With this, we can simply pass test data from our stories to DBUnit!


Usecase "Delete a Contact"
==========================

"The Adressbook-App should allow the use to delete one of his stored Contacts"

Writing the Story
-----------------

Just to remember, here is our requirement again:

    The system must allow the user to delete any of his contacts.
    The user is not allowed to delete contacts of other owners.

This kind of requirement is extremely hard to read for customers. It is also not very precise and thus hard to implement for the programmer.

An example would help the customer to better understand the requirement. The example can also be used by the programmer to test if the implementation matches the requirement. BDD helps us with that.

In our project (within the classpath), we put the following story in a file named "contactDelete.story":

    Story: 
    The system must allow the user to delete any of his contacts.
    The user is not allowed to delete contacts of other owners.

    Scenario: 
    "Delete by Lastname" only deletes contacts of the current user itself

    Given the Contacts: 
    |lastname|firstname|owner|
    |Severin|Carsten|umeier|
    |Schmidt|Pina|umeier|
    |Severin|Carsten|hschmidt|

    When Contact with Lastname Severin is deleted by Owner umeier

    Then the Contacts are:
    |lastname|firstname|owner|
    |Schmidt|Pina|umeier|
    |Severin|Carsten|hschmidt|

After you get used to the syntax you will find it very easy to write a story yourself. Pay attention to the test data: it is already included in the story and as you will see, JBehave will provide you that data in your Unit Test!

Writing the sceleton of a Unit Test
-----------------------------------

Next, we will write a simple Unit Tests for this story:

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = { "classpath:application-context.xml" })
    @DbUnitConfiguration(dataSetLoader = TypedDataSetLoader.class)
    @TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    		DirtiesContextTestExecutionListener.class,
    		TransactionalTestExecutionListener.class,
    		DbUnitTestExecutionListener.class })
    @Story(name="/example/stories/contactDelete.story")
    @FixMethodOrder(MethodSorters.JVM)
    public class ContactDeleteStoryTest {
    
    	@Autowired
    	private ContactDataService service;
    		
    }

This Unit Test will fail because there is no test method so far. As you can see, i extended the code with the  Annotations @Story. I use this Annotation to reference the story i want to write a Unit Test for.

Another thing you might have noticed is the @FixMethodOrder-Annotation. Usually, all Unit Tests should be independant of each another, so the order in which the methods are called are not predictable in JUnit 4 by default.

In out story a scenario constists of tree steps: "Given..." (the pre-condition), "When..." (the condition) and "Then...". It can be very useful to split these three steps into separate test methods. If we do this, we need to make shure, that the methods are called in the right order (e.g. the @Given-Annotated method should be called before or together with the @When-Annotated one).

Normally, it is not a good idea to make test method dependent of each another. In this special case, it helps us to implement the three steps separately within the Unit Test. Therefore, we use the @FixMethodOrder-Annotation.

Implementing the @Given
-----------------------

Now we implement the first test-method:

    	@Test
    	@Given(value="Given the Contacts:$contactsBefore")
     	public void givenThreeContacts(){
    		// Nothing to do here
    	}
    	
    	@StoryParameter(name="contactsBefore")
    	public static ExamplesTable getContactsBefore(){
    		// return null because method will be implemented by behave2unitgen
    		return null;
    	}

With these two methods, we map the "Given..." part of our storys scenario with the right methods in our Unit Test. We tell behave2unitgen to store the Datatable of the story into a parameter called $contactBefore. 

In JBehave, the parameter would be directly passed into the test-method as a parameter. Here we use a separate method and mark it with the @StoryParameter-Annotation. This method will be implemented by behave2unitgen later, so we only have to return null.

Now we extend this a bit to use DBUnit to store the data for us:

    	@Test
    	@Given(value = "Given the Contacts:$contactsBefore")
    	@DatabaseSetup(type=DatabaseOperation.CLEAN_INSERT, value="method:getInitData")
    	public void givenThreeContacts() {
    		// Nothing to do here
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

This time we use DBUnit to read the test data from the method getInitData(). The method itself reads the test data from getContactsBefore(), which is implemented by behave2unitgen and returns the test data of the story.

Lets start the test with gradle:

    gradle --daemon test

The test succeeds as we can see in the JUnit Reports:

![JUnit Reports](https://raw.github.com/cseverin/behave2unitgen/master/images/contactDelete_analyse1.jpg)

Have a look at the testclasses: behave2unitgen generated a new class named "ContactDeleteStoryTest_1.class". You can also see this when you watch the class files in the build folder:

![JUnit Reports](https://raw.github.com/cseverin/behave2unitgen/master/images/contactDelete_gen1.jpg)

In this generated class all the work is done for you: all parameters are included. You are also able to fade certain methods out. This depends on wether the values of your @Given-, @When- and @Then-Annotation match or not.


Implementing the @When
----------------------

After implementing "@Given", we should implement the next step "@When":

    	@Test
    	@When(value = "When Contact with Lastname $lastname is deleted by Owner $owner")
    	public void whenAContactIsDeleted() {
    		service.deleteContactByLastnameAndOwner(getLastname(), getOwner());
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
 
The parameter $owner and $lastname will be read out of the story. behave2unitgen will provide the data by the annotated methods getOwner() and getLastname().  

The method whenAContactIsDeleted() is Annotated with @Test and contains the code to delete a Contact.

Lets run gradle again to start the tests:

    gradle --daemon test

As expected, the tests succeeds as well. Again, we have a look at the JUnit Reports:

![JUnit Reports](https://raw.github.com/cseverin/behave2unitgen/master/images/contactDelete_analyse2.jpg)

Implementing the @Then
----------------------

At last we have to check the result. To do this, we implement the last of the three steps @Then:

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

We use the annotated method getContactsAfter() to get the ExampleTable out of the story. With the method getResultData() we feed DBUnit directly with this data and DBUnit compares the result data with the supposed data itself.

That's it! Now, all three steps are implemented: @Given, @When and @Then.

When we start the test again, we see in the JUnit-Reports that all three tests succeed:

    gradle --daemon test

Here is the JUnit-Report:

![JUnit Reports](https://raw.github.com/cseverin/behave2unitgen/master/images/contactDelete_analyse3.jpg)

Use JSON as data format
-----------------------

Let's have a look back at method that is annotated with @When. There, we read two separate parameters. Wouldn't it be nice to have both values in one objects?

One way to have this is to use JSON as data format! To use this, you must not forget to include the following dependency into your gradle-script:

    testCompile 'org.codehaus.jackson:jackson-mapper-asl:1.8.5'

Now we can change the step "When..." in the story and use a JSON-String instead of two separate parameters:

    ...
    When Contact {"lastname":"Severin", "owner":"umeier"} is deleted
    ...

After we have changed the story, we must also change the value of the @When-Annotation in order to make it still match. Both parameters "owner" and "lastname" are now delivered by the method toBeDeleted() at once:

    	@Test
    	@When(value = "When Contact $toBeDeleted is deleted")
    	public void whenAContactIsDeleted() {
    		service.deleteContactByLastnameAndOwner(toBeDeleted().getObject()
    				.getLastname(), toBeDeleted().getObject().getOwner());
    	}
    
    	@StoryParameter(name = "toBeDeleted")
    	public JSONObject<ContactTestBean> toBeDeleted() {
    		// return null because method will be implemented by behave2unitgen
    		return null;
    	}

The JSON-String is automatically transformed into in instance of ContactTestBean, which is a simple POJO. Of cause you can also use JSON to parse a list of objects (see the examples).

Extend the story
----------------

After some time, you might find a bug that you want to write a test for before the programmer fixes the bug. For example, when the user tries to delete a Contact, that does not exist, a NullPointerException is thrown.

To test this, you only have to extend the story. Add this at the end of your story:

    Story: 
    The system must allow the user to delete any of his contacts.
    The user is not allowed to delete contacts of other owners.
    
    Scenario: "Delete by Lastname" only deletes contacts of the current user itself
    
    Given the Contacts: 
    |lastname|firstname|owner|
    |Severin|Carsten|umeier|
    |Schmidt|Pina|umeier|
    |Severin|Carsten|hschmidt|
    
    When Contact {"lastname":"Severin", "owner":"umeier"} is deleted
    
    Then the Contacts are:
    |lastname|firstname|owner|
    |Schmidt|Pina|umeier|
    |Severin|Carsten|hschmidt|    
    
    Scenario: "Delete by Lastname" when no such contact exists for the user
    
    Given the Contacts: 
    |lastname|firstname|owner|
    |Severin|Carsten|hschmidt|

    When Contact {"lastname":"Severin", "owner":"umeier"} is deleted
    
    Then the Contacts are:
    |lastname|firstname|owner|
    |Severin|Carsten|hschmidt|

We simply run the test again without changing our Unittest:

    gradle --daemon test

Lets have a look at the JUnit-reports:

![JUnit Reports](https://raw.github.com/cseverin/behave2unitgen/master/images/contactDelete_analyse4.jpg)

behave2unitgen automatically generates one test for each scenario! You can also see this when you have a look at the generated classes:

![JUnit Reports](https://raw.github.com/cseverin/behave2unitgen/master/images/contactDelete_gen2.jpg)

In this way, you are able to extend your Unit Tests without changing the sourcecode of the test classes! As we can see it is always good to separate the model (=test data) from the implementation. 
 




