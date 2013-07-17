package org.bom.jbehaveasmunit.core;

import java.util.HashMap;
import java.util.Iterator;

import org.bom.jbehaveasmunit.util.AnnotationConstants;
import org.bom.jbehaveasmunit.util.AnnotationValueUtil;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.Scenario;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * This method does the following:<br/>
 * - parse all test methods of the given class<br/>
 * - creates a Map of parameter and parameter values<br/>
 * - deletes methods that does not belong to the current scenario<br/>
 * - replaces the @StoryParameter methods with the parameter values that belong
 * to the current scenario<br/>
 * <br/>
 * 
 * @author Carsten Severin
 * 
 */
public class MethodDeleteProcessor {

	

	HashMap<String, Integer> deleteCount = new HashMap<String, Integer>();
	HashMap<String, Integer> deleteCountTemp = new HashMap<String, Integer>();

	StoryParameterProcessor parameterProcessor;

	AssertMethodFailureWriter failureWriter;

	int szenarioId = 0;

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public MethodDeleteProcessor(StoryParameterProcessor parameterProcessor,
			AssertMethodFailureWriter failureWriter) {
		this.parameterProcessor = parameterProcessor;
		this.failureWriter = failureWriter;
	}

	private void registerMethod(String methodName) {
		if (!deleteCount.containsKey(methodName)) {
			deleteCount.put(methodName, new Integer(0));
		}
	}

	private void beginSzenario() {
		deleteCountTemp.clear();
	}

	private void endSzenario() {
		szenarioId += 1;
		for (Iterator<String> it = deleteCountTemp.keySet().iterator(); it
				.hasNext();) {
			String key = it.next();
			Integer numberCurrentSzenario = deleteCountTemp.get(key);

			Integer numberAll = deleteCount.get(key);
			if (numberAll == null) {
				deleteCount.put(key, numberCurrentSzenario);
			} else {
				deleteCount.put(key, new Integer(numberAll
						+ numberCurrentSzenario));
			}
		}
	}

	private void registerDelete(String methodName) {
		if (!deleteCount.containsKey(methodName)) {
			registerMethod(methodName);
		}
		deleteCount.put(methodName,
				new Integer(deleteCount.get(methodName) + 1));
	}

	public void processMethods(ClassNode classNode, Scenario scenario) {

		beginSzenario();

		// Method-Annotations parsen
		for (Iterator it = classNode.methods.iterator(); it.hasNext();) {
			MethodNode methodNode = (MethodNode) it.next();
			if (methodNode.visibleAnnotations != null) {
				if (checkAnnotationsForMethodRemove(scenario, methodNode)) {
					it.remove();
				}
			}
		}
		endSzenario();
	}

	private boolean checkAnnotationsForMethodRemove(Scenario scenario,
			MethodNode methodNode) {

		String given = getStepFromScenario(scenario, Keywords.GIVEN + " ");
		String when = getStepFromScenario(scenario, Keywords.WHEN + " ");
		String then = getStepFromScenario(scenario, Keywords.THEN + " ");

		for (Iterator it2 = methodNode.visibleAnnotations.iterator(); it2
				.hasNext();) {
			AnnotationNode annotationNode = (AnnotationNode) it2.next();

			// Given
			if (checkDeleteMethod(given, annotationNode, AnnotationConstants.ANNOTATION_GIVEN,
					methodNode.name)) {
				return true;
			}

			// When
			if (checkDeleteMethod(when, annotationNode, AnnotationConstants.ANNOTATION_WHEN,
					methodNode.name)) {
				return true;
			}

			// Then
			if (checkDeleteMethod(then, annotationNode, AnnotationConstants.ANNOTATION_THEN,
					methodNode.name)) {
				return true;
			}

		}

		return false;
	}

	private boolean checkDeleteMethod(String storyValue,
			AnnotationNode annotationNode, String expectedAnnotationClass,
			String methodName) {

		if (storyValue != null
				&& annotationNode.desc.equals(expectedAnnotationClass)) {

			String annotationValue = (String) AnnotationValueUtil
					.getValueFromAnnotation(annotationNode, "value");

			if (annotationValue != null) {

				// Methode registrieren, weil diese zumindest annotiert wurde
				registerMethod(methodName);

				// Falls keine Parameter, dann sollte Match sofort
				// funktionieren
				if (storyValue.toLowerCase().trim()
						.indexOf(annotationValue.trim().toLowerCase()) >= 0) {
					logger.debug("Story value and annotation value of method "
							+ methodName
							+ " are equal so there are no parameters");
					return false;
				}

				// Falls kein Match, dann Parameter prüfen
				if (!parameterProcessor.processParameter((annotationValue),
						storyValue, expectedAnnotationClass)) {

					logger.debug("Story value and annotation value of method"
							+ methodName
							+ " are not equal so method is registered to be deleted!");

					// Register delete
					registerDelete(methodName);

					return true;
				}

			}
		}

		return false;
	}

	/**
	 * This method checks if a method is deleted in all scenarios. in this case
	 * an Assert falure is inserted into the testclass in order to show that the
	 * test method is does never run.
	 * 
	 * @param classNode
	 */
	public void end(ClassNode classNode) {

		for (Iterator<String> it = deleteCount.keySet().iterator(); it
				.hasNext();) {
			String key = it.next();
			Integer amount = deleteCount.get(key);

			// TODO: Methode wird immer verwendet und verwendet selbst keine
			// Parameter
			// if (amount == 0) {
			//
			// failureWriter.insertFailureMethod(classNode,
			// "methodMatchesAlways",
			// "Methods annotated with @When, @Then or @Given must not match always: "
			// + key, true);
			//
			// }
			// else
			if (amount == szenarioId) {

				logger.warn("Test method "
						+ key
						+ " does never run! This result in an additional Assert fail method!");

				failureWriter
						.insertFailureMethod(
								classNode,
								"methodDoesNeverMatche",
								"Methods annotated with @When, @Then or @Given should at least match in one scenario:"
										+ key, true);

			}
		}

	}

	private String getStepFromScenario(Scenario s, String startsWith) {
		for (Iterator<String> it = s.getSteps().iterator(); it.hasNext();) {
			String step = it.next();
			if (step.startsWith(startsWith)) {
				return step;
			}
		}
		return null;
	}

	public void begin() {
		deleteCount.clear();
		deleteCountTemp.clear();
		szenarioId = 0;
	}

}
