package com.farata.lang.invoke.test.eval;

import com.farata.lang.invoke.test.expression.IExpressionNode;

public interface IExpressionNodeSerializer {
	abstract public void eval(IExpressionNode expression, StringBuilder out);
}
