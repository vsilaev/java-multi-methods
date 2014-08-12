package com.farata.lang.invoke.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.farata.lang.invoke.spi.IntrospectionUtils;

public class InheritanceDistanceTest {
	
	@Test
	public void testPrimitives() {
		assertEquals(IntrospectionUtils.inheritanceDistance(void.class, void.class), 0);
		assertEquals(IntrospectionUtils.inheritanceDistance(int.class, int.class), 0);
		assertEquals(IntrospectionUtils.inheritanceDistance(int.class, void.class), -1);
	}
	
	@Test
	public void testArrays() {
		assertEquals(IntrospectionUtils.inheritanceDistance(Integer[].class, Integer[].class), 0);
		assertEquals(IntrospectionUtils.inheritanceDistance(Integer[].class, Number[].class), 1);
		assertEquals(IntrospectionUtils.inheritanceDistance(Integer[].class, Object[].class), 2);
		assertEquals(IntrospectionUtils.inheritanceDistance(Integer[].class, Object.class), 1);
		assertEquals(IntrospectionUtils.inheritanceDistance(Object[].class, Integer[].class), -1);
		assertEquals(IntrospectionUtils.inheritanceDistance(Integer[].class, Integer.class), -1);
	}
	
	
	@Test
	public void testInterfaces() {
		assertEquals(IntrospectionUtils.inheritanceDistance(Appendable.class, Appendable.class), 0);
		assertEquals(IntrospectionUtils.inheritanceDistance(Appendable.class, Object.class), 1);
		assertEquals(IntrospectionUtils.inheritanceDistance(Appendable.class, CharSequence.class), -1);
		assertEquals(IntrospectionUtils.inheritanceDistance(String.class, CharSequence.class), 1);
		assertEquals(IntrospectionUtils.inheritanceDistance(StringBuilder.class, Appendable.class), 2);
	}
}
