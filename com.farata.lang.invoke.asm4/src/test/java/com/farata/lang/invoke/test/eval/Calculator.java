package com.farata.lang.invoke.test.eval;

import java.util.Map;

import com.farata.lang.invoke.MultiMethods;
import com.farata.lang.invoke.test.expression.Addition;
import com.farata.lang.invoke.test.expression.Constant;
import com.farata.lang.invoke.test.expression.Division;
import com.farata.lang.invoke.test.expression.IExpressionNode;
import com.farata.lang.invoke.test.expression.Multiplication;
import com.farata.lang.invoke.test.expression.Negation;
import com.farata.lang.invoke.test.expression.Subtraction;
import com.farata.lang.invoke.test.expression.UnaryPlus;
import com.farata.lang.invoke.test.expression.Variable;

public class Calculator {

	final private IExpressionNodeEvaluator evaluator;
	
	public Calculator() {
		evaluator = MultiMethods.create(
			IExpressionNodeEvaluator.class, 
			this,
			MultiMethods.publicMethodsByName("eval.*")
		);
	}
	
	public double calculate(final IExpressionNode e, final Map<String, Double> bindings) {
		return evaluator.eval(e, bindings);
	}
	
	public double eval(final Constant e, final Map<String, Double> bindings) {
		return e.value;
	}

	public double eval(final Variable e, final Map<String, Double> bindings) {
		final Double value = bindings.get(e.name);
		if (null == value) {
			throw new RuntimeException("Variable is not bound: " + e.name);
		}
		return value.doubleValue();
	}

	public double eval(final Negation e, final Map<String, Double> bindings) {
		return - evaluator.eval(e.operand, bindings);
	}

	public double eval(final UnaryPlus e, final Map<String, Double> bindings) {
		return + evaluator.eval(e.operand, bindings);
	}
	
	public double eval(final Addition e, final Map<String, Double> bindings) {
		return evaluator.eval(e.loperand, bindings) + evaluator.eval(e.roperand, bindings);
	}
	
	public double eval(final Subtraction e, final Map<String, Double> bindings) {
		return evaluator.eval(e.loperand, bindings) - evaluator.eval(e.roperand, bindings);
	}
	
	public double eval(final Division e, final Map<String, Double> bindings) {
		return evaluator.eval(e.loperand, bindings) / evaluator.eval(e.roperand, bindings);
	}
	
	public double eval(final Multiplication e, final Map<String, Double> bindings) {
		return evaluator.eval(e.loperand, bindings) * evaluator.eval(e.roperand, bindings);
	}

}
