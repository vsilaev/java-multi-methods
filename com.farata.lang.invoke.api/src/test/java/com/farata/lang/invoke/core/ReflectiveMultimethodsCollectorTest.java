package com.farata.lang.invoke.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.farata.lang.invoke.MultiMethodEntry;
import com.farata.lang.invoke.util.MethodModifier;
import com.farata.lang.invoke.util.ReflectiveMultimethodsCollector;

public class ReflectiveMultimethodsCollectorTest {

	public static interface MMCall {
		public void visit(Object o);
	}
	
	public static interface IDemo1 {
		public void visit(Number n);
	}
	
	public static interface IDemo2 extends IDemo1 {
		public void visit(CharSequence s);
	}
	
	abstract static public class Superclass implements IDemo1 {
		public void visit(Number n) {
			System.out.println("Number :" + n);
		}
		
		public void visit(String s) {
			System.out.println("String :" + s);
		}
		
		protected static void visit(Integer i) {
			System.out.print("Integer [STATIC] :" + i);
		};

	}
	
	public static class Subclass extends Superclass implements IDemo2 {
		@Override
		public void visit(Number n) {
			System.out.println("Number [OVERRIDE]:" + n);
		}
		
		public void visit(CharSequence s) {
			System.out.println("CharSequence :" + s);
		}
		
		protected void visit(Double n) {
			System.out.println("Double :" + n);
		}		
		
		public void visit(StringBuilder s) {
			System.out.println("StringBuilder :" + s);
		}
		
		public static void visit(StringBuffer s) {
			System.out.print("StringBuffer [STATIC] :" + s);
		};
		
	}

	
	
	@Test
	public void testComplexInheritance() throws Exception {
		final ReflectiveMultimethodsCollector collector = new ReflectiveMultimethodsCollector("visit", EnumSet.of(MethodModifier.PUBLIC, MethodModifier.PROTECTED, MethodModifier.INSTANCE, MethodModifier.STATIC));
		final List<MultiMethodEntry> entries = collector.collect(MMCall.class.getMethod("visit", Object.class), Subclass.class);
		final Set<Method> methods = new HashSet<Method>();
		for (final MultiMethodEntry e : entries) {
			methods.add(e.method);
		}
		assertEquals(methods.size(), 7); // 5 from Sublcass and 2 (out of 3) from Superclass -- one is overriden, all interface methods are implemented
		
		assertTrue(methods.contains(Subclass.class.getDeclaredMethod("visit", Double.class))); // protected

		// Override wins
		assertTrue(methods.contains(Subclass.class.getDeclaredMethod("visit", Number.class)));
		assertFalse(methods.contains(Superclass.class.getDeclaredMethod("visit", Number.class)));
		
		// Both public and protected static
		assertTrue(methods.contains(Subclass.class.getDeclaredMethod("visit", StringBuffer.class)));
		assertTrue(methods.contains(Superclass.class.getDeclaredMethod("visit", Integer.class)));
		
		// No implemented methods from interface
		assertFalse(methods.contains(IDemo1.class.getDeclaredMethod("visit", Number.class)));

	}
}
