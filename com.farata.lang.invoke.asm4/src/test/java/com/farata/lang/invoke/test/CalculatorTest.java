package com.farata.lang.invoke.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.farata.lang.invoke.test.eval.Calculator;
import com.farata.lang.invoke.test.eval.IExpressionNodeEvaluator;
import com.farata.lang.invoke.test.eval.StringSerializer;
import com.farata.lang.invoke.test.expression.IExpressionNode;

import static com.farata.lang.invoke.test.expression.ExpressionBuilder.*;

public class CalculatorTest {

	@Test
	public void testCalculator() {
		final IExpressionNode exp = 
			multiply(
				val(10),
				plus(
					minus(var("a"), minus(var("b"))), 
					divide(var("c"), val(5))
				)
			);
		final Map<String, Double> bindings = new HashMap<String, Double>();
		bindings.put("a", 11.0);
		bindings.put("b", 1.0);
		bindings.put("c", 25.0);
		
		final StringSerializer serializer = new StringSerializer();
		final String formula = serializer.serialize(exp);

		final IExpressionNodeEvaluator calc = new Calculator();
		final double result = calc.eval(exp, bindings);
		
		System.out.println(formula + " = " + result);
	}
	
}
