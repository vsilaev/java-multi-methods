package com.farata.lang.invoke.test.eval;

import java.util.Map;

import com.farata.lang.invoke.test.expression.IExpressionNode;

public interface IExpressionNodeEvaluator {
	abstract public double eval(IExpressionNode expression, Map<String, Double> bindings);
}
