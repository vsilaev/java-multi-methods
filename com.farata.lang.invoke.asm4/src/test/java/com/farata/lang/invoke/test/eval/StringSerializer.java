package com.farata.lang.invoke.test.eval;

import java.math.BigDecimal;

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

	public void eval(final UnaryMinus e, final StringBuilder out) {
		out.append('-').append('(');
		evaluator.eval(e.operand, out);
		out.append(')');
	}

	public void eval(final UnaryPlus e, final StringBuilder out) {
		out.append('+').append('(');
		evaluator.eval(e.operand, out);
		out.append(')');
	}
	
	public void eval(final Addition e, final StringBuilder out) {
		out.append('(');
		evaluator.eval(e.loperand, out);
		out.append(" + ");
		evaluator.eval(e.roperand, out);
		out.append(')');
	}
	
	public void eval(final Subtraction e, final StringBuilder out) {
		out.append('(');
		evaluator.eval(e.loperand, out);
		out.append(" - ");
		evaluator.eval(e.roperand, out);
		out.append(')');
	}
	
	public void eval(final Division e, final StringBuilder out) {
		out.append('(');
		evaluator.eval(e.loperand, out);
		out.append(" / ");
		evaluator.eval(e.roperand, out);
		out.append(')');
	}
	
	public void eval(final Multiplication e, final StringBuilder out) {
		out.append('(');
		evaluator.eval(e.loperand, out);
		out.append(" * ");
		evaluator.eval(e.roperand, out);
		out.append(')');
	}

}
