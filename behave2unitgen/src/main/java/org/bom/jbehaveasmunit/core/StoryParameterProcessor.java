package org.bom.jbehaveasmunit.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bom.jbehaveasmunit.util.AnnotationConstants;
import org.bom.jbehaveasmunit.util.AnnotationParameterParserUtil;
import org.bom.jbehaveasmunit.util.AnnotationParameterParserUtil.AnnotationParserResult;
import org.bom.jbehaveasmunit.util.AnnotationValueUtil;
import org.jbehave.core.steps.StepType;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This method compares the annotation value with the storys scenario value. It
 * determines which parameters are used und tries to find out the proper values.
 * it also checks if the annotation value is equal to the scenarios value. This
 * method also tries to find the @StoryParameter-methods and replaces the
 * implementation with the right values.
 * 
 * @author Carsten Severin
 * 
 */
public class StoryParameterProcessor {

	HashMap<String, String> parameter = new HashMap<String, String>();

	HashMap<String, ParameterMethodGenerator> paramMethodGenerators;

	AssertMethodFailureWriter failureWriter;

	private static final String ANNOTATION_STORY_PARAM = "Lorg/bom/jbehaveasmunit/annotations/StoryParameter;";

	ConfigParameter configBean;

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public StoryParameterProcessor(AssertMethodFailureWriter failureWriter,
			ConfigParameter configBean) {
		this.failureWriter = failureWriter;
		this.configBean = configBean;

		paramMethodGenerators = new HashMap<String, StoryParameterProcessor.ParameterMethodGenerator>();
		new IntegerParameterGenerator().register(paramMethodGenerators);
		new IntegerTypeMethodGenerator().register(paramMethodGenerators);
		new DoubleParameterGenerator().register(paramMethodGenerators);
		new DoubleTypeMethodGenerator().register(paramMethodGenerators);
		new BooleanParameterGenerator().register(paramMethodGenerators);
		new BooleanTypeMethodGenerator().register(paramMethodGenerators);
		new StringMethodGenerator().register(paramMethodGenerators);
		new ExampleTableMethodGenerator().register(paramMethodGenerators);
		new JSONMethodListGenerator().register(paramMethodGenerators);
		new JSONMethodGenerator().register(paramMethodGenerators);
	}

	public boolean processParameter(String stringWithPlaceholder,
			String stringWithValues, String annotationType) {

		StepType type = evaluateStepTypeByAnnotationClass(annotationType);

		AnnotationParserResult result = AnnotationParameterParserUtil
				.parseScenario(stringWithPlaceholder, stringWithValues, type);

		if (result.isMatch()) {
			parameter.putAll(result.getValues());
		} else {
			logger.info("parameters are not processed because annotation and scenario differ: "
					+ stringWithPlaceholder + " <> " + stringWithValues);
		}

		return result.isMatch();

	}

	private StepType evaluateStepTypeByAnnotationClass(String annotationType) {
		StepType type = StepType.GIVEN;
		if (AnnotationConstants.ANNOTATION_WHEN.equals(annotationType)) {
			type = StepType.WHEN;
		} else if (AnnotationConstants.ANNOTATION_THEN.equals(annotationType)) {
			type = StepType.THEN;
		}
		return type;
	}

	public void reset() {
		parameter.clear();
	}

	public void insertParameter(ClassNode classNode) {

		ArrayList<InsertParamMethod> toBeInserted = new ArrayList<StoryParameterProcessor.InsertParamMethod>();

		ArrayList<AssertFailTemp> failures = new ArrayList<StoryParameterProcessor.AssertFailTemp>();

		for (Iterator it = classNode.methods.iterator(); it.hasNext();) {
			MethodNode methodNode = (MethodNode) it.next();

			if (methodNode.visibleAnnotations != null) {
				for (Iterator it2 = methodNode.visibleAnnotations.iterator(); it2
						.hasNext();) {
					AnnotationNode annotationNode = (AnnotationNode) it2.next();
					if (ANNOTATION_STORY_PARAM.equals(annotationNode.desc)) {
						String valueKey = (String) AnnotationValueUtil
								.getValueFromAnnotation(annotationNode, "name");
						if (valueKey != null) {

							// Insert
							String value = parameter.get(valueKey.trim());
							if (value == null) {

								logger.info("@StoryParameter('"
										+ valueKey
										+ "') is null so the value is not available in the test");

								it.remove();

								toBeInserted
										.add(new InsertParamMethod(
												methodNode.name,
												methodNode.desc,
												methodNode.signature,
												"Parameter "
														+ valueKey
														+ " is null, allowed values are: "
														+ parameter,
												methodNode.access == Opcodes.ACC_PUBLIC
														+ Opcodes.ACC_STATIC,
												true));

							} else {

								// Remove Method
								it.remove();

								logger.info("Method "
										+ methodNode.name
										+ " is deleted in order to be replaced!");

								toBeInserted.add(new InsertParamMethod(
										methodNode.name, methodNode.desc,
										methodNode.signature, value,
										methodNode.access == Opcodes.ACC_PUBLIC
												+ Opcodes.ACC_STATIC, false));
							}
						}
					}

				}
			}
		}

		// Neue Methoden einfügen
		for (Iterator<InsertParamMethod> it = toBeInserted.iterator(); it
				.hasNext();) {

			InsertParamMethod insert = it.next();

			logger.info("Insert new method: " + insert.methodName
					+ " with return parameter: " + insert.returnType
					+ " and value: " + insert.value);

			ParameterMethodGenerator generator = paramMethodGenerators
					.get(insert.returnType);
			if (generator == null) {
				failureWriter.insertFailureMethod(classNode,
						"unknownReturnTypeOfParameterMethod",
						"Return type of method " + insert.methodName
								+ " is unknown. Allowed Types are: "
								+ paramMethodGenerators.keySet().toString(),
						true);
			} else {

				try {

					if (insert.exeption) {

						generator.insertMethod(classNode, insert.methodName,
								insert.signature, insert.value,
								insert.publicStatic);

					} else {

						generator.insertMethod(classNode, insert.methodName,
								insert.signature, insert.value,
								insert.publicStatic);
					}
				} catch (Exception e) {
					failureWriter.insertFailureMethod(classNode,
							"storyParameterCannotBeSet",
							"Unable to set @StoryParameter with value: "
									+ insert.value + " and Type "
									+ insert.returnType, true);
				}

			}

		}

		// Failures
		for (Iterator<AssertFailTemp> it = failures.iterator(); it.hasNext();) {
			AssertFailTemp a = it.next();
			failureWriter.insertFailureMethod(classNode, a.method, a.message,
					true);
		}

	}

	class InsertParamMethod {

		String methodName;

		String returnType;

		String signature;

		String value;

		boolean exeption;

		boolean publicStatic;

		InsertParamMethod(String methodName, String returnType,
				String signature, String value, boolean publicStatic,
				boolean exception) {

			this.methodName = methodName;
			this.returnType = returnType;
			this.publicStatic = publicStatic;
			this.value = value;
			this.signature = signature;
			this.exeption = exception;

		}

	}

	abstract class ParameterMethodGenerator {

		public abstract void register(
				HashMap<String, ParameterMethodGenerator> list);

		public abstract void insertMethod(ClassNode classNode, String method,
				String signature, String value, boolean publicStatic);

		public void insertMethodWithException(ClassNode classNode,
				String method, String signature, String message,
				boolean publicStatic) {

			MethodVisitor mv = classNode.visitMethod(Opcodes.ACC_PUBLIC
					+ (publicStatic ? Opcodes.ACC_STATIC : 0), method,
					"()Ljava/lang/Integer;", signature, null);

			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(53, l0);
			mv.visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException");
			mv.visitInsn(Opcodes.DUP);
			mv.visitLdcInsn(message);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
					"java/lang/RuntimeException", "<init>",
					"(Ljava/lang/String;)V");
			mv.visitInsn(Opcodes.ATHROW);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", "L" + classNode + ";", null, l0, l1,
					0);
			mv.visitMaxs(3, 1);
			mv.visitEnd();

		}
	}

	class IntegerParameterGenerator extends ParameterMethodGenerator {

		@Override
		public void register(HashMap<String, ParameterMethodGenerator> list) {
			list.put("()Ljava/lang/Integer;", this);

		}

		@Override
		public void insertMethod(ClassNode classNode, String method,
				String signature, String value, boolean publicStatus) {
			MethodVisitor mv = classNode.visitMethod(Opcodes.ACC_PUBLIC
					+ (publicStatus ? Opcodes.ACC_STATIC : 0), method,
					"()Ljava/lang/Integer;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(48, l0);
			mv.visitTypeInsn(Opcodes.NEW, "java/lang/Integer");
			mv.visitInsn(Opcodes.DUP);
			mv.visitLdcInsn(value);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Integer",
					"<init>", "(Ljava/lang/String;)V");
			mv.visitInsn(Opcodes.ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", "L" + classNode + ";", null, l0, l1,
					0);
			mv.visitMaxs(3, 1);
			mv.visitEnd();
		}

	}

	class DoubleParameterGenerator extends ParameterMethodGenerator {

		@Override
		public void register(HashMap<String, ParameterMethodGenerator> list) {
			list.put("()Ljava/lang/Double;", this);

		}

		@Override
		public void insertMethod(ClassNode classNode, String method,
				String signature, String value, boolean publicStatus) {
			MethodVisitor mv = classNode.visitMethod(Opcodes.ACC_PUBLIC
					+ (publicStatus ? Opcodes.ACC_STATIC : 0), method,
					"()Ljava/lang/Double;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(48, l0);
			mv.visitTypeInsn(Opcodes.NEW, "java/lang/Double");
			mv.visitInsn(Opcodes.DUP);
			mv.visitLdcInsn(value);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Double",
					"<init>", "(Ljava/lang/String;)V");
			mv.visitInsn(Opcodes.ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", "L" + classNode + ";", null, l0, l1,
					0);
			mv.visitMaxs(3, 1);
			mv.visitEnd();
		}

	}

	class IntegerTypeMethodGenerator extends ParameterMethodGenerator {

		@Override
		public void register(HashMap<String, ParameterMethodGenerator> list) {
			list.put("()I", this);

		}

		@Override
		public void insertMethod(ClassNode classNode, String method,
				String signature, String value, boolean publicStatus) {
			MethodVisitor mv = classNode.visitMethod(Opcodes.ACC_PUBLIC
					+ (publicStatus ? Opcodes.ACC_STATIC : 0), method, "()I",
					null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(48, l0);
			mv.visitTypeInsn(Opcodes.NEW, "java/lang/Integer");
			mv.visitInsn(Opcodes.DUP);
			mv.visitLdcInsn(value);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Integer",
					"<init>", "(Ljava/lang/String;)V");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer",
					"intValue", "()I");
			mv.visitInsn(Opcodes.IRETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			// mv.visitLocalVariable("this",
			// "Lorg/bom/jbehaveasmunit/test/StoryParameterExampleTest;", null,
			// l0, l1, 0);
			mv.visitLocalVariable("this", "L" + classNode.name + ";", null, l0,
					l1, 0);
			mv.visitMaxs(3, 1);
			mv.visitEnd();

		}

	}

	class DoubleTypeMethodGenerator extends ParameterMethodGenerator {

		@Override
		public void register(HashMap<String, ParameterMethodGenerator> list) {
			list.put("()D", this);

		}

		@Override
		public void insertMethod(ClassNode classNode, String method,
				String signature, String value, boolean publicStatus) {
			MethodVisitor mv = classNode.visitMethod(Opcodes.ACC_PUBLIC
					+ (publicStatus ? Opcodes.ACC_STATIC : 0), method, "()D",
					null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(42, l0);
			mv.visitTypeInsn(Opcodes.NEW, "java/lang/Double");
			mv.visitInsn(Opcodes.DUP);
			mv.visitLdcInsn(value);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Double",
					"<init>", "(Ljava/lang/String;)V");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Double",
					"doubleValue", "()D");
			mv.visitInsn(Opcodes.DRETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this",
					"Lorg/bom/jbehaveasmunit/test/StoryParameterExampleTest;",
					null, l0, l1, 0);
			mv.visitMaxs(3, 1);
			mv.visitEnd();

		}

	}

	class BooleanParameterGenerator extends ParameterMethodGenerator {

		@Override
		public void register(HashMap<String, ParameterMethodGenerator> list) {
			list.put("()Ljava/lang/Boolean;", this);

		}

		@Override
		public void insertMethod(ClassNode classNode, String method,
				String signature, String value, boolean publicStatus) {
			MethodVisitor mv = classNode.visitMethod(Opcodes.ACC_PUBLIC
					+ (publicStatus ? Opcodes.ACC_STATIC : 0), method,
					"()Ljava/lang/Boolean;", null, null);

			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(58, l0);
			mv.visitTypeInsn(Opcodes.NEW, "java/lang/Boolean");
			mv.visitInsn(Opcodes.DUP);
			mv.visitLdcInsn("true".equals(value) ? "true" : "false");
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Boolean",
					"<init>", "(Ljava/lang/String;)V");
			mv.visitInsn(Opcodes.ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			// mv.visitLocalVariable("this",
			// "Lorg/bom/jbehaveasmunit/test/StoryParameterExampleTest;", null,
			// l0, l1, 0);
			mv.visitLocalVariable("this", "L" + classNode.name + ";", null, l0,
					l1, 0);
			mv.visitMaxs(3, 1);
			mv.visitEnd();
		}

	}

	class BooleanTypeMethodGenerator extends ParameterMethodGenerator {

		@Override
		public void register(HashMap<String, ParameterMethodGenerator> list) {
			list.put("()Z", this);

		}

		@Override
		public void insertMethod(ClassNode classNode, String method,
				String signature, String value, boolean publicStatus) {
			MethodVisitor mv = classNode.visitMethod(Opcodes.ACC_PUBLIC
					+ (publicStatus ? Opcodes.ACC_STATIC : 0), method, "()Z",
					null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(43, l0);
			mv.visitTypeInsn(Opcodes.NEW, "java/lang/Boolean");
			mv.visitInsn(Opcodes.DUP);
			mv.visitLdcInsn("true".equals(value) ? "true" : "false");
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Boolean",
					"<init>", "(Ljava/lang/String;)V");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean",
					"booleanValue", "()Z");
			mv.visitInsn(Opcodes.IRETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", "L" + classNode.name + ";", null, l0,
					l1, 0);
			mv.visitMaxs(3, 1);
			mv.visitEnd();
		}

	}

	class StringMethodGenerator extends ParameterMethodGenerator {

		@Override
		public void register(HashMap<String, ParameterMethodGenerator> list) {
			list.put("()Ljava/lang/String;", this);

		}

		@Override
		public void insertMethod(ClassNode classNode, String method,
				String signature, String value, boolean publicStatus) {
			MethodVisitor mv = classNode.visitMethod(Opcodes.ACC_PUBLIC
					+ (publicStatus ? Opcodes.ACC_STATIC : 0), method,
					"()Ljava/lang/String;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(43, l0);
			mv.visitLdcInsn(value);
			mv.visitInsn(Opcodes.ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			// mv.visitLocalVariable("this",
			// "Lorg/bom/jbehaveasmunit/test/StoryParameterExampleTest;", null,
			// l0, l1, 0);
			mv.visitLocalVariable("this", "L" + classNode.name + ";", null, l0,
					l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();

		}

	}

	class ExampleTableMethodGenerator extends ParameterMethodGenerator {

		@Override
		public void register(HashMap<String, ParameterMethodGenerator> list) {
			list.put("()Lorg/jbehave/core/model/ExamplesTable;", this);

		}

		@Override
		public void insertMethod(ClassNode classNode, String method,
				String signature, String value, boolean publicStatus) {
			MethodVisitor mv = classNode.visitMethod(Opcodes.ACC_PUBLIC
					+ (publicStatus ? Opcodes.ACC_STATIC : 0), method,
					"()Lorg/jbehave/core/model/ExamplesTable;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(26, l0);
			mv.visitTypeInsn(Opcodes.NEW,
					"org/jbehave/core/model/ExamplesTable");
			mv.visitInsn(Opcodes.DUP);
			mv.visitLdcInsn(value);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
					"org/jbehave/core/model/ExamplesTable", "<init>",
					"(Ljava/lang/String;)V");
			mv.visitInsn(Opcodes.ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", "L" + classNode.name + ";", null, l0,
					l1, 0);
			mv.visitMaxs(3, 1);
			mv.visitEnd();

		}

	}

	class JSONMethodListGenerator extends ParameterMethodGenerator {

		@Override
		public void register(HashMap<String, ParameterMethodGenerator> list) {
			list.put("()Lorg/bom/jbehaveasmunit/beans/JSONList;", this);

		}

		@Override
		public void insertMethod(ClassNode classNode, String method,
				String signature, String value, boolean publicStatus) {
			MethodVisitor mv = classNode.visitMethod(Opcodes.ACC_PUBLIC
					+ (publicStatus ? Opcodes.ACC_STATIC : 0), method,
					"()Lorg/bom/jbehaveasmunit/beans/JSONList;", signature,
					null);
			mv.visitCode();
			Label l0 = new Label();
			Label l1 = new Label();
			Label l2 = new Label();
			mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Exception");
			Label l3 = new Label();
			mv.visitLabel(l3);
			mv.visitLineNumber(151, l3);
			mv.visitTypeInsn(Opcodes.NEW,
					"org/codehaus/jackson/map/ObjectMapper");
			mv.visitInsn(Opcodes.DUP);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
					"org/codehaus/jackson/map/ObjectMapper", "<init>", "()V");
			mv.visitVarInsn(Opcodes.ASTORE, 1);
			mv.visitLabel(l0);
			mv.visitLineNumber(153, l0);
			mv.visitTypeInsn(Opcodes.NEW,
					"org/bom/jbehaveasmunit/beans/JSONList");
			mv.visitInsn(Opcodes.DUP);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitLdcInsn(value);

			signature = signature.replaceAll(
					"\\(\\)Lorg/bom/jbehaveasmunit/beans/JSONList\\<", "[");
			signature = signature.replaceAll("\\>\\;", "");

			mv.visitLdcInsn(Type.getType(signature));
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
					"org/codehaus/jackson/map/ObjectMapper", "readValue",
					"(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;");
			mv.visitTypeInsn(Opcodes.CHECKCAST, signature);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
					"org/bom/jbehaveasmunit/beans/JSONList", "<init>",
					"([Ljava/lang/Object;)V");
			mv.visitLabel(l1);
			mv.visitInsn(Opcodes.ARETURN);
			mv.visitLabel(l2);
			mv.visitLineNumber(154, l2);
			mv.visitFrame(Opcodes.F_FULL, 2, new Object[] { classNode.name,
					"org/codehaus/jackson/map/ObjectMapper" }, 1,
					new Object[] { "java/lang/Exception" });
			mv.visitVarInsn(Opcodes.ASTORE, 2);
			Label l4 = new Label();
			mv.visitLabel(l4);
			mv.visitLineNumber(155, l4);
			mv.visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException");
			mv.visitInsn(Opcodes.DUP);
			mv.visitVarInsn(Opcodes.ALOAD, 2);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
					"java/lang/RuntimeException", "<init>",
					"(Ljava/lang/Throwable;)V");
			mv.visitInsn(Opcodes.ATHROW);
			Label l5 = new Label();
			mv.visitLabel(l5);
			mv.visitLocalVariable("this", "L" + classNode.name + ";", null, l0,
					l1, 0);
			mv.visitLocalVariable("om",
					"Lorg/codehaus/jackson/map/ObjectMapper;", null, l0, l5, 1);
			mv.visitLocalVariable("e", "Ljava/lang/Exception;", null, l4, l5, 2);
			mv.visitMaxs(5, 3);
			mv.visitEnd();

		}

	}

	class JSONMethodGenerator extends ParameterMethodGenerator {

		@Override
		public void register(HashMap<String, ParameterMethodGenerator> list) {
			list.put("()Lorg/bom/jbehaveasmunit/beans/JSONObject;", this);

		}

		@Override
		public void insertMethod(ClassNode classNode, String method,
				String signature, String value, boolean publicStatus) {

			MethodVisitor mv = classNode.visitMethod(Opcodes.ACC_PUBLIC
					+ (publicStatus ? Opcodes.ACC_STATIC : 0), method,
					"()Lorg/bom/jbehaveasmunit/beans/JSONObject;", signature,
					null);
			mv.visitCode();
			Label l0 = new Label();
			Label l1 = new Label();
			Label l2 = new Label();
			mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Exception");
			Label l3 = new Label();
			mv.visitLabel(l3);
			mv.visitLineNumber(19, l3);
			mv.visitTypeInsn(Opcodes.NEW,
					"org/codehaus/jackson/map/ObjectMapper");
			mv.visitInsn(Opcodes.DUP);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
					"org/codehaus/jackson/map/ObjectMapper", "<init>", "()V");
			mv.visitVarInsn(Opcodes.ASTORE, 1);
			mv.visitLabel(l0);
			mv.visitLineNumber(21, l0);
			mv.visitTypeInsn(Opcodes.NEW,
					"org/bom/jbehaveasmunit/beans/JSONObject");
			mv.visitInsn(Opcodes.DUP);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitLdcInsn(value);

			signature = signature.replaceAll(
					"\\(\\)Lorg/bom/jbehaveasmunit/beans/JSONObject\\<", "");
			signature = signature.replaceAll("\\>\\;", "");

			mv.visitLdcInsn(Type.getType(signature));
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
					"org/codehaus/jackson/map/ObjectMapper", "readValue",
					"(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;");

			// mv.visitTypeInsn(Opcodes.CHECKCAST,
			// "org/bom/jbehaveasmunit/test/ContactTestBean");
			mv.visitTypeInsn(Opcodes.CHECKCAST, signature);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
					"org/bom/jbehaveasmunit/beans/JSONObject", "<init>",
					"(Ljava/lang/Object;)V");
			mv.visitLabel(l1);
			mv.visitInsn(Opcodes.ARETURN);
			mv.visitLabel(l2);
			mv.visitLineNumber(22, l2);
			mv.visitFrame(Opcodes.F_FULL, 2, new Object[] { classNode.name,
					"org/codehaus/jackson/map/ObjectMapper" }, 1,
					new Object[] { "java/lang/Exception" });
			mv.visitVarInsn(Opcodes.ASTORE, 2);
			Label l4 = new Label();
			mv.visitLabel(l4);
			mv.visitLineNumber(23, l4);
			mv.visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException");
			mv.visitInsn(Opcodes.DUP);
			mv.visitVarInsn(Opcodes.ALOAD, 2);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
					"java/lang/RuntimeException", "<init>",
					"(Ljava/lang/Throwable;)V");
			mv.visitInsn(Opcodes.ATHROW);
			Label l5 = new Label();
			mv.visitLabel(l5);
			mv.visitLocalVariable("this", "L" + classNode.name + ";", null, l0,
					l1, 0);
			mv.visitLocalVariable("om",
					"Lorg/codehaus/jackson/map/ObjectMapper;", null, l0, l5, 1);
			mv.visitLocalVariable("e", "Ljava/lang/Exception;", null, l4, l5, 2);
			mv.visitMaxs(5, 3);
			mv.visitEnd();
		}

	}

	class AssertFailTemp {

		String method;

		String message;

		AssertFailTemp(String method, String message) {
			this.message = message;
			this.method = method;
		}
	}

}
