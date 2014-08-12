package com.farata.lang.invoke.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.farata.lang.invoke.MultiMethodEntry;
import com.farata.lang.invoke.MultiMethodsCollector;
import com.farata.lang.invoke.spi.IntrospectionUtils;

public class ReflectiveMultimethodsCollector implements MultiMethodsCollector {
	final private String methodName; 
	final private Set<MethodModifier> allowedModifiers;
	final private SignatureMismatchBehavior onSignatureMismatch;
	final private int hashCode;
	
	public ReflectiveMultimethodsCollector(final String methodName) {
		this(methodName, EnumSet.of(MethodModifier.PUBLIC, MethodModifier.INSTANCE, MethodModifier.STATIC));
	}
	
	public ReflectiveMultimethodsCollector(final String methodName, final Set<MethodModifier> allowedMethodModifiers) {
		this(methodName, allowedMethodModifiers, SignatureMismatchBehavior.Standard.SKIP);
	}
	
	public ReflectiveMultimethodsCollector(final String methodName, final Set<MethodModifier> allowedMethodModifiers, final SignatureMismatchBehavior onSignatureMismatch) {
		this.methodName = methodName;
		this.allowedModifiers = allowedMethodModifiers;
		this.onSignatureMismatch = onSignatureMismatch;
		this.hashCode = methodName.hashCode() * 31 ^ allowedMethodModifiers.hashCode();
	}

	public List<MultiMethodEntry> collect(final Method interfaceMethod, final Class<?> implementationClass) {
		final List<MultiMethodEntry> collectedMethods = new ArrayList<MultiMethodEntry>();
		_collect(interfaceMethod, implementationClass, collectedMethods);
		return collectedMethods;
	}
	
	protected void _collect(final Method interfaceMethod, final Class<?> implementationClass, final Collection<MultiMethodEntry> result) {
		
		final Class<?>[] interfaceMethodParameterTypes = interfaceMethod.getParameterTypes();
		
		final Pattern methodNamePattern = Pattern.compile(methodName);
		
		// Below check is necessary of public methods vs all methods
		for (final Method implementationMethod : implementationClass.getDeclaredMethods()) {
			if (implementationMethod.isBridge()) {
				// No bridges please -- there are real methods anyway 
				continue;
			}
			
			final Set<?> methodModifiers = modifiersOf(implementationMethod);
			methodModifiers.removeAll(allowedModifiers);
			if (!methodModifiers.isEmpty()) {
				// disallowed modifiers
				continue;
			}
			
			final Matcher matcher = methodNamePattern.matcher(implementationMethod.getName());
			if (!matcher.matches())
				continue;
			
			final Class<?>[] implementationMethodParameterTypes = implementationMethod.getParameterTypes();
			if (interfaceMethodParameterTypes.length != implementationMethodParameterTypes.length) {
				onSignatureMismatch.report(
					implementationMethod, interfaceMethod,
					String.format("Method %s has wrong number of arguments; expected signature is %s", implementationMethod.toGenericString(), interfaceMethod.toGenericString())
				);
				continue;
			}
			
			int methodDistance = 0;
			for (int i = implementationMethodParameterTypes.length -1; i >=0; i--) {
				final int distance = IntrospectionUtils.inheritanceDistance(implementationMethodParameterTypes[i], interfaceMethodParameterTypes[i]);
				if (distance < 0) {
					methodDistance = -1;
					break;
				}
				else {
					methodDistance += distance;
				}
			}
			
			if (methodDistance < 0) {
				onSignatureMismatch.report(
					implementationMethod, interfaceMethod,
					String.format("Method %s has wrong argument types; expected signature is %s", implementationMethod.toGenericString(), interfaceMethod.toGenericString())
				);
				continue;
			}
			if (!isOverrideExists(result, implementationMethod)) {
				result.add(new MultiMethodEntry(implementationMethod, methodDistance));
			}
		}
		
		final Class<?> superClass = implementationClass.getSuperclass();
		if (superClass != null && superClass != Object.class) {
			_collect(interfaceMethod, superClass, result);
		}
		final Class<?>[] superInterfaces = implementationClass.getInterfaces();
		for (final Class<?> superInterface : superInterfaces) {
			_collect(interfaceMethod, superInterface, result);
		}
	}
	
	

	public static Set<?> modifiersOf(final Method method) {
		final EnumSet<MethodModifier> result = EnumSet.noneOf(MethodModifier.class);
		final int modifiers = method.getModifiers();
		if (Modifier.isStatic(modifiers))
			result.add(MethodModifier.STATIC);
		else
			result.add(MethodModifier.INSTANCE);
		if (!Modifier.isPrivate(modifiers)) {
			if (Modifier.isProtected(modifiers))
				result.add(MethodModifier.PROTECTED);
			else if (Modifier.isPublic(modifiers))
				result.add(MethodModifier.PUBLIC);
			else
				result.add(MethodModifier.PACKAGE_PROTECTED);
			return result;
		} else {
			final Set<Object> invalidResult = new HashSet<Object>(result);
			invalidResult.add("<PRIVATE>");
			return invalidResult;
		}
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public boolean equals(final Object other) {
		if (null == other || this.getClass() != other.getClass())
			return false;
		final ReflectiveMultimethodsCollector c = (ReflectiveMultimethodsCollector)other;
		return eq(methodName, c.methodName) && eq(allowedModifiers, c.allowedModifiers);
	}
	
	protected boolean isOverrideExists(final Iterable<MultiMethodEntry> collectedSubclassMethods, final Method superClassMethod) {
		if (Modifier.isStatic(superClassMethod.getModifiers())) {
			return false;
		}
		
		for (final MultiMethodEntry e : collectedSubclassMethods) {
			final Method subClassMethod = e.method;
			if (Modifier.isStatic(subClassMethod.getModifiers())) {
				continue;
			}
			if (isOverride(subClassMethod, superClassMethod)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isOverride(final Method subClassMethod, final Method superClassMethod) {
		if (!superClassMethod.getDeclaringClass().isAssignableFrom(subClassMethod.getDeclaringClass())) {
			return false;
		}
		if (!subClassMethod.getName().equals(superClassMethod.getName())) {
			return false;
		}
		final Class<?>[] subClassParameterTypes = subClassMethod.getParameterTypes();
		final Class<?>[] superClassParameterTypes = superClassMethod.getParameterTypes();
		final int arity = subClassParameterTypes.length; 
		if (arity != superClassParameterTypes.length) {
			return false;
		}
		for (int i = arity - 1; i >=0; i--) {
			if (subClassParameterTypes[i] != superClassParameterTypes[i]) {
				return false;
			}
		}
		return true;
	}
	
	private static boolean eq(final Object a, final Object b) {
		return null == a ? null == b : a.equals(b);
	}
	
}
