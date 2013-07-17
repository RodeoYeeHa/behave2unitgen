package org.bom.jbehaveasmunit.core;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.bom.jbehaveasmunit.util.AnnotationValueUtil;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * This is the central class of Behave2UnitGen so far. It was the root of this
 * little tool which i extended step by step. This has to be refactored a lot
 * since it is written in a trial and succeed-manner.
 * 
 * @author Carsten Severin
 * 
 */
public class ClassManipulator {

	private static final String STORY_PARSER_TEST_GEN = "org/bom/jbehaveasmunit/junit/StoryNotImplemented_Gen";

	private static final String ANNOTATION_STORY = "Lorg/bom/jbehaveasmunit/annotations/Story;";

	StoryReader reader;

	StoryParameterProcessor parameterProcessor;

	MethodDeleteProcessor methodDeleteProcessor;

	AssertMethodFailureWriter failureWriter;

	ConfigParameter configBean;

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public ClassManipulator(StoryReader reader, ConfigParameter configBean) {
		this.reader = reader;
		this.configBean = configBean;
		this.failureWriter = new AssertMethodFailureWriter();
		this.parameterProcessor = new StoryParameterProcessor(
				this.failureWriter, this.configBean);
		this.methodDeleteProcessor = new MethodDeleteProcessor(
				parameterProcessor, failureWriter);
	}

	public void storyNotImplemented(List<String> notImplementedList)
			throws IOException {
		InputStream in = ClassManipulator.class
				.getResourceAsStream("/org/bom/jbehaveasmunit/junit/StoryNotImplemented_Template.class");
		ClassReader classReader = new ClassReader(in);
		ClassNode classNode = new ClassNode();
		classReader.accept(new ClassRenamer(classNode,
				STORY_PARSER_TEST_GEN), 0);

		for (Iterator<String> it = notImplementedList.iterator(); it.hasNext();) {
			String key = it.next();
//			Story story = reader.readStory(key);
//
//			String message = key
//					+ " > "
//					+ (story.getName() != null && story.getName().length() > 0 ? story
//							.getName()
//							: (story.getDescription() != null ? story
//									.getDescription().asString() : "none"));

			failureWriter.insertFailureMethod(classNode,
					"testNotImplementedStories", key, true);

		}

		storeNewClass(classNode, configBean.getOutDir() + "/"
				+ STORY_PARSER_TEST_GEN + ".class");
	}


	/**
	 * Checks wether the given Resource is a class that uses the
	 * 
	 * @Story-Annotation. If true, the class tries to read the proper story und
	 *                    creates Test-Classes from the story-steps. After that,
	 *                    the
	 * @Test-Annotation is removed from the original test class.
	 * 
	 * @param resource
	 *            Class from Classpath to be transformed
	 * @return true, if the given class uses the @Story-Annotation
	 * @throws IOException
	 */
	public boolean manipulateClass(Resource resource) throws IOException {
		ClassReader classReader = openClass(resource);

		int step = 1;

		ClassNode classNode = new ClassNode();
		classReader.accept(new ClassRenamer(classNode, step), 0);

		// Load Story
		String storyLocation = getStoryLocationFromClass(classNode);
		if (storyLocation == null) {
			// TODO: Skip! Keine Story
			return false;
		}

		Story story = reader.readStory(storyLocation);

		if (story != null) {

			//reader.markStoryAsUsed(storyLocation);

			methodDeleteProcessor.begin();

			for (Iterator<Scenario> itSzenario = story.getScenarios()
					.iterator(); itSzenario.hasNext();) {

				Scenario scenario = itSzenario.next();

				parameterProcessor.reset();

				// Delete Methods
				methodDeleteProcessor.processMethods(classNode, scenario);

				// insert parameter
				parameterProcessor.insertParameter(classNode);

				if (!itSzenario.hasNext()) {
					methodDeleteProcessor.end(classNode);
				}

				// Printout/Store
				storeNewClass(classNode, configBean.getOutDir() + "/"
						+ classNode.name + ".class");

				// reload Class for next step
				if (itSzenario.hasNext()) {
					classNode = new ClassNode();
					classReader.accept(new ClassRenamer(classNode, ++step), 0);
				}

			}

		}

		// Original-Klasse soll keine
		classNode = new ClassNode();
		classReader.accept(classNode, 0);

		if (story == null) {
			failureWriter.insertFailureMethod(classNode, "storyNotFound",
					"Story " + storyLocation
							+ " not found! Valid stories are: "
							+ reader.stories.keySet().toString(), false);
		} else {
			classNode.visibleAnnotations.clear();
			deleteAllAnnotations(classNode);
		}

		storeNewClass(classNode, configBean.getOutDir() + "/" + classNode.name
				+ ".class");

		return true;

	}

	private void storeNewClass(ClassNode classNode, String filename)
			throws IOException {
		// We are done now. so dump the class
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS
				| ClassWriter.COMPUTE_FRAMES);
		classNode.accept(cw);

		int i = filename.lastIndexOf("/");
		logger.debug("Store new Class " + filename);
		if (i > 0) {
			String path = filename.substring(0, i);
			File fileOutDir = new File(path);
			if (!fileOutDir.exists()) {
				fileOutDir.mkdirs();
			}
		}

		// File fileOutDir=new File(outDir);
		// fileOutDir.mkdirs();
		DataOutputStream dout = new DataOutputStream(new FileOutputStream(
				new File(filename)));
		dout.write(cw.toByteArray());
		dout.flush();
		dout.close();
	}

	private void deleteAllAnnotations(ClassNode classNode) {

		for (Iterator it = classNode.methods.iterator(); it.hasNext();) {
			MethodNode methodNode = (MethodNode) it.next();
			if (methodNode.visibleAnnotations != null) {
				methodNode.visibleAnnotations.clear();
			}
		}

	}

	private String getStoryLocationFromClass(ClassNode classNode) {
		if (classNode.visibleAnnotations != null) {
			for (Iterator it = classNode.visibleAnnotations.iterator(); it
					.hasNext();) {
				AnnotationNode annotationNode = (AnnotationNode) it.next();
				if (annotationNode.desc.equals(ANNOTATION_STORY)) {
					String storyLocation = (String) AnnotationValueUtil
							.getValueFromAnnotation(annotationNode, "name");
					return storyLocation;
				}
			}
		}
		return null;
	}

	public Resource[] findUnitTests() throws IOException {
		Resource[] resources = new PathMatchingResourcePatternResolver()
				.getResources("classpath*:"
						+ configBean.getSearchPatternTestClasses());
		return resources;
	}

	private ClassReader openClass(Resource resource) throws IOException {
		ClassReader cr = new ClassReader(resource.getInputStream());
		return cr;
	}

	class ClassRenamer extends ClassVisitor implements Opcodes {

		String oldName;

		String newName;

		int step = -1;

		public ClassRenamer(ClassVisitor cv, int step) {
			super(ASM4, cv);
			this.step = step;
		}

		public ClassRenamer(ClassVisitor cv, String newName) {
			super(ASM4, cv);
			this.newName = newName;
		}

		public void visit(int version, int access, String name,
				String signature, String superName, String[] interfaces) {
			this.oldName = name;
			if (step >= 0) {
				this.newName = name + "_" + step;
			}
			cv.visit(version, ACC_PUBLIC, this.newName, signature, superName,
					interfaces);
		}

		public MethodVisitor visitMethod(int access, String name, String desc,
				String signature, String[] exceptions) {
			MethodVisitor mv = cv.visitMethod(access, name, desc,
					fix(signature), exceptions);
			if (mv != null && (access & ACC_ABSTRACT) == 0) {
				mv = new MethodRenamer(mv, this.oldName, this.newName);
			}
			return mv;
		}

		class MethodRenamer extends MethodVisitor {

			String oldName;

			String newName;

			public MethodRenamer(final MethodVisitor mv, String oldName,
					String newName) {
				super(ASM4, mv);
				this.oldName = oldName;
				this.newName = newName;
			}

			public void visitTypeInsn(int i, String s) {
				if (s.equals(oldName)) {
					s = newName;
				}
				mv.visitTypeInsn(i, s);
			}

			public void visitFieldInsn(int opcode, String owner, String name,
					String desc) {
				// System.out.println("******** OWNER: " + name + ":" + owner);
				if (oldName.equals(owner)) {
					mv.visitFieldInsn(opcode, newName, name, fix(desc));
				} else {
					mv.visitFieldInsn(opcode, owner, name, fix(desc));
				}
			}

			public void visitMethodInsn(int opcode, String owner, String name,
					String desc) {
				// System.out.println("******** OWNER: " + owner);
				if (oldName.equals(owner)) {
					mv.visitMethodInsn(opcode, newName, name, fix(desc));
				} else {
					mv.visitMethodInsn(opcode, owner, name, fix(desc));
				}
			}
		}

		private String fix(String s) {
			// System.out.println("##### FIX: " + s);
			if (s != null && newName != null && s.indexOf(oldName) >= 0) {
				return s.replaceAll(oldName, newName);
			}
			return s;
		}
	}

}
