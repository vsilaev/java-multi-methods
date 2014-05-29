package com.farata.lang.invoke.asm4;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import com.farata.lang.invoke.MultiMethodException;
import com.farata.lang.invoke.spi.IntrospectionUtils;

import static org.objectweb.asm.Opcodes.*;

public class MultiMethodDispatcherClassGenerator {

	final private static AtomicLong COUNTER = new AtomicLong(0);
	
	public byte[] generateClass(final Class<?> interfaceClass, final Method samInterfaceMethod, final Class<?> delegateClass, final Collection<Method> implementationMethods) {
		final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		
		final String suffix = "_$MD$_" + COUNTER.incrementAndGet();

		final String interfaceClassName = Type.getInternalName(interfaceClass);
		final String implementationClassName = Type.getInternalName(delegateClass) + suffix; 
		final Type delegateType = Type.getType(delegateClass);
		cw.visit(
			V1_7, ACC_PUBLIC + ACC_SUPER, 
			implementationClassName, 
			null, 
			OBJECT_CLASS_NAME, 
			new String[] { interfaceClassName }
		);
		
		generateMethodNamesConst(cw, implementationClassName, implementationMethods);
		generateMethodAmbiguityErrorMethod(cw, implementationClassName);
		
		// Delegate field
		final FieldVisitor delegateField = cw.visitField(ACC_PRIVATE + ACC_FINAL, DELEGATE_FIELD_NAME, delegateType.getDescriptor(), null, null);
		delegateField.visitEnd();
		
		// Constructor
		final MethodVisitor constructorMethod = cw.visitMethod(ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(delegateClass)), null, null);
		constructorMethod.visitCode();
		constructorMethod.visitVarInsn(ALOAD, 0);
		constructorMethod.visitMethodInsn(INVOKESPECIAL, OBJECT_CLASS_NAME, "<init>", "()V");
		constructorMethod.visitVarInsn(ALOAD, 0);
		constructorMethod.visitVarInsn(ALOAD, 1);
		constructorMethod.visitFieldInsn(PUTFIELD, implementationClassName, DELEGATE_FIELD_NAME, delegateType.getDescriptor());
		constructorMethod.visitInsn(RETURN);
		constructorMethod.visitMaxs(2, 2);
		constructorMethod.visitEnd();
				
		generateDispatcherMethod(cw, implementationClassName, delegateClass, samInterfaceMethod, implementationMethods);
		generateIndexerMethod(cw, implementationClassName, samInterfaceMethod, implementationMethods);
		
		cw.visitEnd();
		return cw.toByteArray();
	}
	
	
	protected void generateMethodNamesConst(final ClassWriter cw, final String implementationClassName, final Collection<Method> methods) {

		final FieldVisitor fv = cw.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, IMPL_METHOD_NAMES_FIELD_NAME, FIELD_ARRAY_CLASS_DESCRIPTOR, null, null);
		fv.visitEnd();
		final MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
		mv.visitCode();
		intConst(mv, methods.size());
		mv.visitTypeInsn(ANEWARRAY, Type.getInternalName(String.class));
		
		int methodIdx = 0;
		for (final Method m : methods) {
			mv.visitInsn(DUP);
			intConst(mv, methodIdx);
			mv.visitLdcInsn(m.toGenericString());
			mv.visitInsn(AASTORE);
			methodIdx++;
		}
		
		mv.visitFieldInsn(PUTSTATIC, implementationClassName, IMPL_METHOD_NAMES_FIELD_NAME, FIELD_ARRAY_CLASS_DESCRIPTOR);
		
		mv.visitInsn(RETURN);
		mv.visitMaxs(4, 0);
		mv.visitEnd();
	}
	
	protected void generateMethodAmbiguityErrorMethod(final ClassWriter cw, final String implementationClassName) {
		final MethodVisitor mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC, METHOD_AMBIGUITY_ERROR_METHOD_NAME, "(II)V", null, null);
		mv.visitCode();
		mv.visitTypeInsn(NEW, STRING_BUILDER_CLASS_NAME);
		mv.visitInsn(DUP);
		mv.visitLdcInsn("Ambiguent methods found:\n");
		mv.visitMethodInsn(INVOKESPECIAL, STRING_BUILDER_CLASS_NAME, "<init>", "(Ljava/lang/String;)V"/*, false*/);
		mv.visitVarInsn(ASTORE, 2);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitFieldInsn(GETSTATIC, implementationClassName, IMPL_METHOD_NAMES_FIELD_NAME, FIELD_ARRAY_CLASS_DESCRIPTOR);
		mv.visitVarInsn(ILOAD, 0);
		mv.visitInsn(AALOAD);
		mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_CLASS_NAME, "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"/*, false*/);
		mv.visitInsn(POP);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitIntInsn(BIPUSH, 10);
		mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_CLASS_NAME, "append", "(C)Ljava/lang/StringBuilder;"/*, false*/);
		mv.visitInsn(POP);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitFieldInsn(GETSTATIC, implementationClassName, IMPL_METHOD_NAMES_FIELD_NAME, FIELD_ARRAY_CLASS_DESCRIPTOR);
		mv.visitVarInsn(ILOAD, 1);
		mv.visitInsn(AALOAD);
		mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_CLASS_NAME, "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"/*, false*/);
		mv.visitInsn(POP);
		mv.visitTypeInsn(NEW, MULTI_METHOD_EXCEPTION_CLASS_NAME);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_CLASS_NAME, "toString", "()Ljava/lang/String;"/*, false*/);
		mv.visitMethodInsn(INVOKESPECIAL, MULTI_METHOD_EXCEPTION_CLASS_NAME, "<init>", "(Ljava/lang/String;)V"/*, false*/);
		mv.visitInsn(ATHROW);
		mv.visitMaxs(3, 3);
		mv.visitEnd();
	}
	
	protected void generateDispatcherMethod(final ClassWriter cw, final String implementationClassName, final Class<?> delegateClass, final Method samInterfaceMethod, final Collection<Method> implementationMethods) {
		final Type delegateType = Type.getType(delegateClass);
		final Type interfaceMethodType = Type.getType(samInterfaceMethod);
		final Class<?>[] interfaceMethodParameterTypes = samInterfaceMethod.getParameterTypes();
		final int arity = interfaceMethodParameterTypes.length;
		final Type returnType = samInterfaceMethod.getReturnType() == void.class || samInterfaceMethod.getReturnType() == null ? null : interfaceMethodType.getReturnType();
		
		final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, samInterfaceMethod.getName(), interfaceMethodType.getDescriptor(), null, exceptionTypeNamesOf(samInterfaceMethod));
		mv.visitCode();
		
		// Get index of method to invoke
		for (int paramIdx = 1 /* shifted via "this" */; paramIdx <= arity; paramIdx++) {
			mv.visitVarInsn(Type.getType(interfaceMethodParameterTypes[paramIdx - 1]).getOpcode(ILOAD), paramIdx);
		}
		mv.visitMethodInsn(INVOKESTATIC, implementationClassName, INDEXER_METHOD_NAME, getIndexerMethodDescriptor(samInterfaceMethod));
		
		final int idxVarIndex = arity + 1;		
		mv.visitVarInsn(ISTORE, idxVarIndex);
		mv.visitVarInsn(ILOAD, idxVarIndex);
		
		final int multiMethodsCount = implementationMethods.size();
		final Label[] labels = new Label[multiMethodsCount];
		for (int i = 0; i < multiMethodsCount; i++) {
			labels[i] = new Label();
		}
		final Label defaultLabel = new Label();
		final Label exitLabel = null != returnType ? null : new Label();

		mv.visitTableSwitchInsn(0, multiMethodsCount - 1, defaultLabel, labels);
		int idx = 0;
		for (final Method impleMethod : implementationMethods) {
			mv.visitLabel(labels[idx]);
			
			final boolean isStaticMethod = Modifier.isStatic(impleMethod.getModifiers());
			if (!isStaticMethod) {
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, implementationClassName, DELEGATE_FIELD_NAME, delegateType.getDescriptor());
			}
			
			final Class<?>[] implementationMethodTypes = impleMethod.getParameterTypes();
			for (int paramIdx = 1 /*shifted via this*/; paramIdx <= arity; paramIdx++ ) {
				final Class<?> interfaceMethodParameterType = interfaceMethodParameterTypes[paramIdx - 1];
				mv.visitVarInsn(Type.getType(interfaceMethodParameterType).getOpcode(ILOAD), paramIdx);
				final Class<?> implementationMethodParameterType = implementationMethodTypes[paramIdx - 1];
				if (implementationMethodParameterType != interfaceMethodParameterType) {
					mv.visitTypeInsn(CHECKCAST, Type.getInternalName(implementationMethodParameterType));
				}
			}
			if (isStaticMethod) {
				mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(impleMethod.getDeclaringClass()), impleMethod.getName(), Type.getType(impleMethod).getDescriptor());
			} else {
				mv.visitMethodInsn(
					delegateClass.isInterface() ? INVOKEINTERFACE :	INVOKEVIRTUAL, 
					delegateType.getInternalName(), impleMethod.getName(), Type.getType(impleMethod).getDescriptor()
				);
			}
			
			if (null == returnType) {
				mv.visitJumpInsn(GOTO, exitLabel);
			} else {
				mv.visitInsn(returnType.getOpcode(IRETURN));
			}
			
			idx++;
		}
		
		mv.visitLabel(defaultLabel);
		mv.visitTypeInsn(NEW, MULTI_METHOD_EXCEPTION_CLASS_NAME);
		mv.visitInsn(DUP);
		mv.visitLdcInsn("Unable to dynamically dispatch call -- no matching method found");
		mv.visitMethodInsn(INVOKESPECIAL, MULTI_METHOD_EXCEPTION_CLASS_NAME, "<init>", "(Ljava/lang/String;)V");
		mv.visitInsn(ATHROW);
		
		if (null == returnType) {
			mv.visitLabel(exitLabel);
			mv.visitInsn(RETURN);
		}
		mv.visitMaxs(arity + 1, arity + 2);
		mv.visitEnd();
	}
	
	protected void generateIndexerMethod(final ClassWriter cw, final String implementationClassName, final Method samInterfaceMethod, final Collection<Method> implementationMethods) {
		final Class<?>[] interfaceMethodParameterTypes = samInterfaceMethod.getParameterTypes();
		final int arity = interfaceMethodParameterTypes.length;
		
		final MethodVisitor mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC, INDEXER_METHOD_NAME, getIndexerMethodDescriptor(samInterfaceMethod), null, new String[]{});
		mv.visitCode();
		
		int extraVars = arity;
		for (int paramIdx = 0; paramIdx < arity; paramIdx++) {
			final Class<?> interfaceMethodParameterType = interfaceMethodParameterTypes[paramIdx];
			if (interfaceMethodParameterType.isPrimitive())
				continue;
			
			mv.visitVarInsn(ALOAD, paramIdx);
			final Label ifNonNull = new Label();
			mv.visitJumpInsn(IFNONNULL, ifNonNull);
			mv.visitInsn(ACONST_NULL);
			final Label endIf = new Label();
			mv.visitJumpInsn(GOTO, endIf);
			mv.visitLabel(ifNonNull);
			mv.visitVarInsn(ALOAD, paramIdx);
			mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_CLASS_NAME, "getClass", "()Ljava/lang/Class;");
			mv.visitLabel(endIf);
			mv.visitVarInsn(ASTORE, extraVars);
			extraVars++;
		}
		
		extraVars--;
		final int parameterDistanceVarIdx = ++extraVars;
		final int methodDistanceVarIdx = ++extraVars;
		final int minMethodDistanceVarIdx = ++extraVars;
		final int resultVarIdx = ++extraVars;
		
		mv.visitInsn(ICONST_M1);
		mv.visitVarInsn(ISTORE, resultVarIdx);

		mv.visitLdcInsn(new Integer(Integer.MAX_VALUE));
		mv.visitVarInsn(ISTORE, minMethodDistanceVarIdx);
		
		int methodIndex = 0;
		for (final Method impleMethod : implementationMethods) {
			int typeVariableIdx = arity;
			final Class<?>[] implementationMethodTypes = impleMethod.getParameterTypes();
			
			final Label breakMethodDistanceCalculation = new Label();
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, methodDistanceVarIdx);
			
			for (int paramIdx = 0; paramIdx < arity; paramIdx++ ) {
				final Class<?> interfaceMethodParameterType = interfaceMethodParameterTypes[paramIdx];
				if (interfaceMethodParameterType.isPrimitive())
					continue;
				
				final Class<?> implementationMethodParameterType = implementationMethodTypes[paramIdx];
					
				mv.visitVarInsn(ALOAD, typeVariableIdx);
				final Label ifNotNullTypeVar = new Label();
				mv.visitJumpInsn(IFNONNULL, ifNotNullTypeVar);
				mv.visitInsn(ICONST_0);
				final Label assignParameterDistanceVar = new Label();
				mv.visitJumpInsn(GOTO, assignParameterDistanceVar);
				mv.visitLabel(ifNotNullTypeVar);
				mv.visitVarInsn(ALOAD, typeVariableIdx);
				mv.visitLdcInsn(Type.getType(implementationMethodParameterType));
				mv.visitMethodInsn(INVOKESTATIC, INTROSPECTION_UTILS_CLASS_NAME, "inheritanceDistance", "(Ljava/lang/Class;Ljava/lang/Class;)I");
				mv.visitLabel(assignParameterDistanceVar);
				mv.visitVarInsn(ISTORE, parameterDistanceVarIdx);

				mv.visitVarInsn(ILOAD, parameterDistanceVarIdx);
				final Label incrementMethodDistance = new Label();
				mv.visitJumpInsn(IFGE, incrementMethodDistance);
				mv.visitInsn(ICONST_M1);
				mv.visitVarInsn(ISTORE, methodDistanceVarIdx);
				mv.visitJumpInsn(GOTO, breakMethodDistanceCalculation);
				mv.visitLabel(incrementMethodDistance);
				mv.visitVarInsn(ILOAD, methodDistanceVarIdx);
				mv.visitVarInsn(ILOAD, parameterDistanceVarIdx);
				mv.visitInsn(IADD);
				mv.visitVarInsn(ISTORE, methodDistanceVarIdx);
				
				typeVariableIdx++;

			}
			mv.visitLabel(breakMethodDistanceCalculation);

			
			mv.visitVarInsn(ILOAD, methodDistanceVarIdx);
			final Label resumeWithNextMethod = new Label();
			mv.visitJumpInsn(IFLT, resumeWithNextMethod);
			mv.visitVarInsn(ILOAD, minMethodDistanceVarIdx);
			mv.visitVarInsn(ILOAD, methodDistanceVarIdx);
			final Label noBetterMatch = new Label();
			mv.visitJumpInsn(IF_ICMPLE, noBetterMatch);
			mv.visitVarInsn(ILOAD, methodDistanceVarIdx);
			mv.visitVarInsn(ISTORE, minMethodDistanceVarIdx);
			intConst(mv, methodIndex);
			mv.visitVarInsn(ISTORE, resultVarIdx);
			mv.visitJumpInsn(GOTO, resumeWithNextMethod);
			mv.visitLabel(noBetterMatch);
			mv.visitVarInsn(ILOAD, minMethodDistanceVarIdx);
			mv.visitVarInsn(ILOAD, methodDistanceVarIdx);
			final Label noAmbiguityCheck = new Label();
			mv.visitJumpInsn(IF_ICMPNE, noAmbiguityCheck);
			mv.visitVarInsn(ILOAD, resultVarIdx);
			mv.visitJumpInsn(IFLT, resumeWithNextMethod);
			mv.visitVarInsn(ILOAD, resultVarIdx);
			intConst(mv, methodIndex);
			mv.visitMethodInsn(INVOKESTATIC, implementationClassName, METHOD_AMBIGUITY_ERROR_METHOD_NAME, "(II)V"/*, false*/);
			mv.visitJumpInsn(GOTO, resumeWithNextMethod);
			mv.visitLabel(noAmbiguityCheck);
			mv.visitVarInsn(ILOAD, resultVarIdx);
			mv.visitJumpInsn(IFLT, resumeWithNextMethod);
			mv.visitVarInsn(ILOAD, resultVarIdx);
			mv.visitInsn(IRETURN);
			mv.visitLabel(resumeWithNextMethod);
			
			methodIndex++;
		}

		
		mv.visitVarInsn(ILOAD, resultVarIdx);
		mv.visitInsn(IRETURN);

		mv.visitMaxs(arity, arity + extraVars);
		mv.visitEnd();
	}
	
	private static String[] exceptionTypeNamesOf(final Method method) {
		final Class<?>[] exceptionTypes = method.getExceptionTypes();
		final String[] result = new String[exceptionTypes.length];
		int idx = 0;
		for (final Class<?> exceptionType : exceptionTypes) {
			result[idx++] = Type.getInternalName(exceptionType);
		}
		return result;
	}
	
	private static String getIndexerMethodDescriptor(final Method samInterfaceMethod) {
		final Type methodType = Type.getType(samInterfaceMethod);
		return Type.getMethodDescriptor(Type.INT_TYPE, methodType.getArgumentTypes());
	}
	
	private static void intConst(final MethodVisitor mv, final int value) {
		switch (value) {
			case 0: mv.visitInsn(ICONST_0); break;
			case 1: mv.visitInsn(ICONST_1); break;
			case 2: mv.visitInsn(ICONST_2); break;
			case 3: mv.visitInsn(ICONST_3); break;
			case 4: mv.visitInsn(ICONST_4); break;
			case 5: mv.visitInsn(ICONST_5); break;
			default: mv.visitIntInsn(BIPUSH, value);
		}
	}

	final private static String OBJECT_CLASS_NAME = Type.getInternalName(Object.class);
	final private static String STRING_BUILDER_CLASS_NAME = Type.getInternalName(StringBuilder.class);
	final private static String FIELD_ARRAY_CLASS_DESCRIPTOR = Type.getDescriptor(String[].class);
	final private static String MULTI_METHOD_EXCEPTION_CLASS_NAME = Type.getInternalName(MultiMethodException.class);
	final private static String INTROSPECTION_UTILS_CLASS_NAME = Type.getInternalName(IntrospectionUtils.class);
	
	final private static String DELEGATE_FIELD_NAME = "delegate";
	final private static String INDEXER_METHOD_NAME = "__$$dispatch_index$$__";
	final private static String IMPL_METHOD_NAMES_FIELD_NAME = "__$$methods$$__";
	final private static String METHOD_AMBIGUITY_ERROR_METHOD_NAME = "__$$method_ambiguity_error$$__";
}
