package com.farata.lang.invoke.test.eval;

import java.math.BigDecimal;

import com.farata.lang.invoke.MultiMethods;
import com.farata.lang.invoke.test.expression.BinaryOperator;
import com.farata.lang.invoke.test.expression.Constant;
import com.farata.lang.invoke.test.expression.IExpressionNode;
import com.farata.lang.invoke.test.expression.UnaryOperator;
import com.farata.lang.invoke.test.expression.Variable;

public class StringSerializer {
	final private IExpressionNodeSerializer evaluator;
	
	public StringSerializer() {
		evaluator = MultiMethods.create(
			IExpressionNodeSerializer.class, 
			this,
			MultiMethods.publicMethodsByName("eval.*")
		);
	}
	
	public String serialize(final IExpressionNode e) {
		final StringBuilder out = new StringBuilder();
		evaluator.eval(e, out);
		return out.toString();
	}
	
	public void eval(final Constant e, final StringBuilder out) {
		out.append(BigDecimal.valueOf(e.value).setScale(2));
	}

	public void eval(final Variable e, final StringBuilder out) {
		out.append(e.name);
	}

	public void eval(final UnaryOperator e, final StringBuilder out) {
		out.append(e.name).append('(');
		evaluator.eval(e.operand, out);
		out.append(')');
	}

	public void eval(final BinaryOperator e, final StringBuilder out) {
		out.append('(');
		evaluator.eval(e.loperand, out);
		out.append(' ').append(e.name).append(' ');
		evaluator.eval(e.roperand, out);
		out.append(')');
	}

}
