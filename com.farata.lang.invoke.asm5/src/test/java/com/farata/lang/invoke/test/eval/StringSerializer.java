package com.farata.lang.invoke.test.eval;

import java.math.BigDecimal;

import com.farata.lang.invoke.MultiMethods;
import com.farata.lang.invoke.test.expression.BinaryOperator;
import com.farata.lang.invoke.test.expression.Constant;
import com.farata.lang.invoke.test.expression.IExpressionNode;
import com.farata.lang.invoke.test.expression.UnaryOperator;
import com.farata.lang.invoke.test.expression.Variable;

public class StringSerializer {
	final private IExpressionNodeSerializer dispatcher;
	
	public StringSerializer() {
		dispatcher = MultiMethods.create(
			IExpressionNodeSerializer.class, 
			this,
			MultiMethods.publicMethodsByName("doSerialize.*")
		);
	}
	
	public String serialize(final IExpressionNode e) {
		final StringBuilder out = new StringBuilder();
		dispatcher.serialize(e, out);
		return out.toString();
	}
	
	public void doSerialize(final Constant e, final StringBuilder out) {
		out.append(BigDecimal.valueOf(e.value).setScale(2));
	}

	public void doSerialize(final Variable e, final StringBuilder out) {
		out.append(e.name);
	}

	// We may handle all UnaryOperator-s uniformly
	public void doSerialize(final UnaryOperator e, final StringBuilder out) {
		out.append(e.name).append('(');
		dispatcher.serialize(e.operand, out);
		out.append(')');
	}

	// We may handle all BinaryOperator-s uniformly
	public void doSerialize(final BinaryOperator e, final StringBuilder out) {
		out.append('(');
		dispatcher.serialize(e.loperand, out);
		out.append(' ').append(e.name).append(' ');
		dispatcher.serialize(e.roperand, out);
		out.append(')');
	}

}
