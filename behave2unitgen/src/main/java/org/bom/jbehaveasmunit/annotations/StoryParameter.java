package org.bom.jbehaveasmunit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
/**
 * Use this annotation to annotate a "getter-Method" in your JUnit-Test. Behave2UnitGen will then try to replace the method with a parameter that is given by a certain scenario. Example:<br/>
 * @StoryParameter("$price")<br/>
 * public int getPrice(){<br/>
 *   return -1;<br/>
 * }<br/>
 * 
 * @author Carsten Severin
 *
 */
public @interface StoryParameter {

	String name();
	
}
