behave2unitgen
==============

Behave2UnitGen generates executable JUnit test cases from a set of given story files. It uses ASM to generate the test cases.

The project depends on JBehave:
http://jbehave.org

It also depends on JUnit:
https://github.com/junit-team/junit

The examples also depend on Unit testing with Spring and DBUnit:
https://github.com/springtestdbunit/spring-test-dbunit

If you would like to inject test data from the stories via JSON, you also need the Jackson-Library.

Introduction
============

JBehave is excellent for using Behavior Driven Development in your projects. 
However, I had the following difficulties with its standard implematation:
- I was not able to use Spring Test Framework and DBUnit the way I wanted to. E.g. I had to use special annotations to integrate Spring 
- For a simple one-story-per-test case, I don't need to separate the steps into their own classes

Behavior Driven Development (BDD)
=================================

First time I heard about BDD when I started reading the book "Specification by Example". In the first instance I was confused about the term "executable specification". How can one execute a specification?

So far I was aware of three different types of tests:
- Unit Tests
- Integration Tests
- Acceptance Tests

As Unit Test and Integration Tests are usually designed by the developer himselfthey run the risk of being far too technical and detailed for a business specification.
Acceptance Tests have the disadvantage to be executed manually in order to test the business specification. From the developer's point of view they are often not detailed and precise enough.

The idea of BDD is the following:
- Improve the quality of specifications by adding examples to it
- Guarantee the execution of requirements by using the examples for Integration Tests
- Track your project status any time
- Make your specification testable  


Once you have implemented the Unit Tests for your requirements, you can test any time.

How to use behave2unitgen
=========================

The sample project uses gradle as the prefered build system. To download behave2unitgen you need to extend your maven repositories:

    	repositories {
    		mavenCentral()
    		maven{
    			url 'https://github.com/cseverin/maven-repo/raw/master'
    		}
    	}

Then you have to include the dependencies:

    compile "org.springframework:spring-test:3.1.2.RELEASE"
    compile "org.springframework:spring-beans:3.1.2.RELEASE"
    
    compile 'org.jbehave:jbehave-core:4.0-beta-3'

    testCompile "org.ow2.asm:asm-all:4.1"


    compile ("org.bom.behave2unitgen:behave2unitgen:1.0.0-SNAPSHOT"){
        changing = true
    }
You might need further dependencies. Please have a look at the complete gradle-script of the sample project:

https://raw.github.com/cseverin/behave2unitgen/master/behave2unitgenExample/build.gradle

In case you have not installed gradle, you could use the gradle-wrapper simply by calling 

    gradlew --daemon test

instead of

    gradle --daemon test

In order to run the generation automatically before the test-task you have to add the following to your gradle-script as well:

    	test {
    		dependsOn "behave2unitgen"
    	}
    	
    	
    	task(behave2unitgen, dependsOn: 'classes', type: JavaExec) {
    		main = 'org.bom.jbehaveasmunit.runner.Behave2UnitGenRunner'
    		classpath = sourceSets.test.runtimeClasspath
    		args 'parseUnusedStories=true'
    	}
	

Valid parameters are:

| Parameter                | Default Value       |
| ------------------------ |:-------------------:|
| searchPatternTestClasses | **/*StoryTest.class |
| outDir                   | build/classes/test/ |
| parseUnusedStories       | false               | 



Use DBUnit
==========
Most of the web applications rely on database queries that can only be testet completely with Integration Tests. 

DBUnit is extremely useful for Integration Tests. It provides you several features to prepare the database before your test start. And you are able to compare the data after the test have run with the expected values automatically.

There is a library called "spring-test-dbunit" that integrates the Spring test framework with DBUnit. 

Let us assume that we would like to implement an adress book application. The requirement we want to test is: 

    The system must allow the user to delete any of his contacts. 
    The user is not allowed to delete contacts of other owners. 

This example shows how difficult it is to describe a relatively simple requirement. Let's write a DBUnit-Test for this. In this example there are two contacts with the same names stored for different owners. One owner should only be able to delete his or her database entry: 

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

This example is completely written without BDD. With DBUnit I can simply define XML-Data for the @DatabaseSetup:

    <?xml version="1.0" encoding="UTF-8"?>
    <dataset>
      <CONTACT FIRST_NAME="Pina" LAST_NAME="Schmidt" OWNER="umeier"/>
      <CONTACT FIRST_NAME="Carsten" LAST_NAME="Severin" OWNER="umeier"/>
      <CONTACT FIRST_NAME="Carsten" LAST_NAME="Severin" OWNER="hschmidt"/>
    </dataset>

I specify how the data should be like after I deleted 'Severin' for owner 'umeier':

    <?xml version="1.0" encoding="UTF-8"?>
    <dataset>
      <CONTACT FIRST_NAME="Pina" LAST_NAME="Schmidt" OWNER="umeier"/>
      <CONTACT FIRST_NAME="Carsten" LAST_NAME="Severin" OWNER="hschmidt"/>
    </dataset>

Very simple so far! Spring test framework combined with DBUnit makes it very easy to write an Integration Test!

If the test data is too small for a separate XML file or if you would like to geneate the test data you can simply define the data within the test class in a static method. Therefore, you have to extend the class level annotations:

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = { "classpath:application-context.xml" })
    @DbUnitConfiguration(dataSetLoader = TypedDataSetLoader.class)
    @TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    		DirtiesContextTestExecutionListener.class,
    		TransactionalTestExecutionListener.class,
    		DbUnitTestExecutionListener.class })
    public class ContactDeleteTest { 
    ...

Please also notice @DBUnitConfiguration annotation! Here you define a special DataSetLoader that enables you to reference methods instead of XML-Files. How you use it:

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

The @DatabaseSetup-Annotation uses a value prefixed with "method:". That means, it has to read the test data out of the method setup().

Keep this in mind! With this method we can simply pass test data from our stories to DBUnit!


Usecase "Delete a Contact"
==========================

"The adress-book-app should allow the user to delete one of his or her stored contacts"

Writing the Story
-----------------

Just to remember, here is our requirement again:

    The system must allow the user to delete any of his contacts.
    The user is not allowed to delete contacts of other owners.

This kind of requirement is extremely hard to read for customers. It is also not very precise and thus hard to implement for the programmer.

An example would help the customer to conceive the requirement. The example can also be used by the programmer to test if the implementation matches the requirement. BDD helps us with that.

In our project (within the classpath), we put the following story in a file named "contactDelete.story":

    Story: 
    The system must allow the user to delete any of his contacts.
    The user is not allowed to delete contacts of other owners.

    Scenario: 
    "Delete by lastname" only deletes contacts of the current user itself.

    Given the Contacts: 
    |lastname|firstname|owner|
    |Severin|Carsten|umeier|
    |Schmidt|Pina|umeier|
    |Severin|Carsten|hschmidt|

    When contact with lastname "Severin" is deleted by owner "umeier"

    then the contacts are:
    |lastname|firstname|owner|
    |Schmidt|Pina|umeier|
    |Severin|Carsten|hschmidt|

As soon as you get used to the syntax you will find it very easy to write a story yourself. Pay attention to the test data: it is already included in the story and you will see: JBehave will provide you that data in your Unit Test!

Writing the skeleton of a Unit Test
-----------------------------------

Next we will write a simple Unit Tests for this story:

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

This Unit Test will fail because there is no test method so far. As you can see I extended the code with the annotation @Story. I use this annotation to refer to the story I write a Unit Test for.

You also might have noticed the annotation @FixMethodOrder. Usually, all Unit Tests should be independant of one another, so the order in which the methods are called are not predictable in JUnit 4 by default.

In our story a scenario consists of tree steps: "Given..." (the pre-condition), "When..." (the condition) and "Then...". It can be very useful to split these three steps into separate test methods. If we do so, we need to make sure, that the methods are called in the right order (e.g. the @Given-Annotated method should be called before or together with the @When-Annotated one).

Normally, it is not a good idea to make test method dependent of each other. In this special case, it helps us to implement the three steps separately within the Unit Test. Therefore we use the @FixMethodOrder-annotation.

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

With these two methods we map the "Given..." part of our story's scenario with the right methods in our Unit Test. We tell behave2unitgen to store the Datatable of the story into a parameter called $contactBefore. 

In JBehave the parameter would be passed directly into the test-method as a parameter. Here we use a separate method and mark it with the @StoryParameter-Annotation. This method will be implemented by behave2unitgen later, so we only have to return null.

Now we extend this to use DBUnit to store the data for us:

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

Let's start the test with gradle:

    gradle --daemon test

The test succeeds as we can see in the JUnit Reports:

![JUnit Reports](https://raw.github.com/cseverin/behave2unitgen/master/images/contactDelete_analyse1.jpg)

Have a look at the test classes: behave2unitgen generates a new class named "ContactDeleteStoryTest_1.class". You can also see this when you watch the class files in the build folder:

![JUnit Reports](https://raw.github.com/cseverin/behave2unitgen/master/images/contactDelete_gen1.jpg)

In this generated class all the work is done for you: all parameters are included. You are also able to fade out certain methods. It depends on whether the values of your @Given-, @When- and @Then-Annotation match or not.


Implementing the @When
----------------------

After implementing "@Given" we implement the next step "@When":

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

The method whenAContactIsDeleted() is annotated with @Test and contains the code to delete a contact.

Let's run gradle again to start the tests:

    gradle --daemon test

As expected the tests succeeds as well. Again we have a look at the JUnit Reports:

![JUnit Reports](https://raw.github.com/cseverin/behave2unitgen/master/images/contactDelete_analyse2.jpg)

Implementing the @Then
----------------------

At last we have to check the result. Therefore we implement the last of the three steps @Then:

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

That's it! Now all three steps are implemented: @Given, @When and @Then.

When we start the test again, we see in the JUnit-Reports that all three tests succeed:

    gradle --daemon test

Here is the JUnit-Report:

![JUnit Reports](https://raw.github.com/cseverin/behave2unitgen/master/images/contactDelete_analyse3.jpg)

Use JSON as data format
-----------------------

Let's have a look back at the  method that is annotated with @When. There we read two separate parameters. Wouldn't it be nice to have both values in one object?

Therefore you have to use JSON as data format! Pay attention: You must not forget to include the following dependency into your gradle-script:

    testCompile 'org.codehaus.jackson:jackson-mapper-asl:1.8.5'

We change the step "When..." in the story and use a JSON-string instead of two separate parameters:

    ...
    When Contact {"lastname":"Severin", "owner":"umeier"} is deleted
    ...

After we have changed the story we must also change the value of the @When-Annotation in order to make sure that they still match. Both parameters "owner" and "lastname" are now delivered by the method toBeDeleted() at once:

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

The JSON-string is automatically transformed into an instance of ContactTestBean, which is a simple POJO. Of course you can also use JSON to parse a list of objects (see the examples).

Extend the story
----------------

After a while you might find a bug that you would like to write a test for before the programmer fixes the bug. For example, when the user tries to delete a contact, that does not exist and a NullPointerException is thrown.

In this case you only have to extend the story. Add the following at the end of your story:

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

Let's have a look at the JUnit-reports:

![JUnit Reports](https://raw.github.com/cseverin/behave2unitgen/master/images/contactDelete_analyse4.jpg)

behave2unitgen automatically generates one test for each scenario! To check it just have a look at the generated classes:

![JUnit Reports](https://raw.github.com/cseverin/behave2unitgen/master/images/contactDelete_gen2.jpg)

That is how you are able to extend your Unit Tests without changing the sourcecode of the test classes! As we can see it is always clever to separate the model (=test data) from the implementation. 
 




