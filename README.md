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

== Introduction
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



 




