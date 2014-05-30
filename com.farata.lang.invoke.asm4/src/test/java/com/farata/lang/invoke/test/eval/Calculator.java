package com.farata.lang.invoke.test.eval;

import java.util.Map;

import com.farata.lang.invoke.MultiMethods;
import com.farata.lang.invoke.test.expression.Addition;
import com.farata.lang.invoke.test.expression.Constant;
import com.farata.lang.invoke.test.expression.Division;
import com.farata.lang.invoke.test.expression.IExpressionNode;
import com.farata.lang.invoke.test.expression.Multiplication;
import com.farata.lang.invoke.test.expression.UnaryMinus;
import com.farata.lang.invoke.test.expression.Subtraction;
import com.farata.lang.invoke.test.expression.UnaryPlus;
import com.farata.lang.invoke.test.expression.Variable;

public class Calculator implements IExpressionNodeEvaluator {

	final private IExpressionNodeEvaluator dispatcher;
	
	public Calculator() {
		dispatcher = MultiMethods.create(
			IExpressionNodeEvaluator.class, 
			this,
			MultiMethods.publicMethodsByName("doEval.*")
		);
	}
	
	public double eval(final IExpressionNode e, final Map<String, Double> bindings) {
		return dispatcher.eval(e, bindings);
	}
	
	public double doEval(final Constant e, final Map<String, Double> bindings) {
		return e.value;
	}

	public double doEval(final Variable e, final Map<String, Double> bindings) {
		final Double value = bindings.get(e.name);
		if (null == value) {
			throw new RuntimeException("Variable is not bound: " + e.name);
		}
		return value.doubleValue();
	}

	public double doEval(final UnaryMinus e, final Map<String, Double> bindings) {
		return - dispatcher.eval(e.operand, bindings);
	}

	public double doEval(final UnaryPlus e, final Map<String, Double> bindings) {
		return + dispatcher.eval(e.operand, bindings);
	}
	
	public double doEval(final Addition e, final Map<String, Double> bindings) {
		return dispatcher.eval(e.loperand, bindings) + dispatcher.eval(e.roperand, bindings);
	}
	
	public double doEval(final Subtraction e, final Map<String, Double> bindings) {
		return dispatcher.eval(e.loperand, bindings) - dispatcher.eval(e.roperand, bindings);
	}
	
	public double doEval(final Division e, final Map<String, Double> bindings) {
		return dispatcher.eval(e.loperand, bindings) / dispatcher.eval(e.roperand, bindings);
	}
	
	public double doEval(final Multiplication e, final Map<String, Double> bindings) {
		return dispatcher.eval(e.loperand, bindings) * dispatcher.eval(e.roperand, bindings);
	}

}
