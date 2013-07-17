package org.bom.jbehaveasmunit.core;

import java.util.HashMap;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

/**
 * Writer to insert an Assert fail method into a Unit-Test
 * 
 * @author Carsten Severin
 *
 */
public class AssertMethodFailureWriter {

	HashMap<String, Integer> map = new HashMap<String, Integer>();

	/**
	 * This is a helper class that inserts a method into the given class. This
	 * method will produce an Assert to fail when the class is run with a
	 * JUnit-Runner.
	 * 
	 * @param classNode
	 * @param methodName
	 * @param message
	 * @param appendId
	 */
	public void insertFailureMethod(ClassNode classNode, String methodName,
			String message, boolean appendId) {

		String methodName0 = methodName;
		if (appendId) {
			Integer id = map.get(classNode.name + "." + methodName);
			if (id == null) {
				id = new Integer(1);
			} else {
				id = new Integer(id + 1);
			}
			map.put(classNode.name + "." + methodName, id);
			methodName0 += id;
		}

		MethodVisitor mv = classNode.visitMethod(Opcodes.ACC_PUBLIC,
				methodName0, "()V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(62, l0);
		mv.visitLdcInsn(message);
		mv.visitInsn(Opcodes.ICONST_0);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "org/junit/Assert",
				"assertTrue", "(Ljava/lang/String;Z)V");
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLineNumber(63, l1);
		mv.visitInsn(Opcodes.RETURN);
		Label l2 = new Label();
		mv.visitLabel(l2);
		
		mv.visitLocalVariable("this", "L" + classNode.name + ";", null, l0, l2,
				0);
		mv.visitMaxs(2, 1);

		AnnotationVisitor av = mv.visitAnnotation("Lorg/junit/Test;", true);
		av.visitEnd();

		mv.visitEnd();

	}

}
