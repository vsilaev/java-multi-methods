package com.farata.lang.invoke.test.expression;

public class ExpressionBuilder {

	public static UnaryPlus plus(final IExpressionNode e) {
		return new UnaryPlus(e);
	}
	
	public static Addition plus(final IExpressionNode a, final IExpressionNode b) { 
		return new Addition(a, b);
	}

	public static UnaryMinus minus(final IExpressionNode e) {
		return new UnaryMinus(e);
	}
	
	public static Subtraction minus(final IExpressionNode a, final IExpressionNode b) { 
		return new Subtraction(a, b);
	}
	
	public static Division divide(final IExpressionNode a, final IExpressionNode b) { 
		return new Division(a, b);
	}

	public static Multiplication multiply(final IExpressionNode a, final IExpressionNode b) { 
		return new Multiplication(a, b);
	}

	public static Constant val(final double value) {
		return new Constant(value);
	}
	
	public static Variable var(final String name) {
		return new Variable(name);
	}
}
