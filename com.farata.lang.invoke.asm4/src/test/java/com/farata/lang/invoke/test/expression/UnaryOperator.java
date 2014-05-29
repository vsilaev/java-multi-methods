package com.farata.lang.invoke.test.expression;

abstract public class UnaryOperator implements IExpressionNode {
	final public String name;
	final public IExpressionNode operand;
	
	public UnaryOperator(final String name, final IExpressionNode operand) {
		this.name = name;
		this.operand = operand;
	}
}
