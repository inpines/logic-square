package org.dotspace.oofp.support.tokenizer;

import org.apache.commons.lang3.StringUtils;
import org.dotspace.oofp.support.MessageConverter;
import org.dotspace.oofp.support.common.ExpressionEvaluations;
import org.dotspace.oofp.util.Associable;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Function;

public class TokenizationFunctors implements Associable<ExpressionEvaluations> {

	private ExpressionEvaluations expressions;

	private MessageConverter msgConverter;
	
	public Function<String, String> getText() {
		return s -> s;
	}

	public Function<String, byte[]> getTextToUtf8() {
		return s -> msgConverter.convertBig5ToUTF8(s);
	}
	
	public Function<String, Long> parseLongValue() {
		return s -> Long.valueOf(s);
	}
	
	public Function<String, Integer> parseIntValue() {
		return s -> StringUtils.isNotBlank(s) ? Integer.parseInt(s) : 0;
	}
	
	public Function<String, BigDecimal> parseDecimalValue(int franctionLength) {
		return s -> {
			BigDecimal value = new BigDecimal(s);
			BigDecimal divisor = BigDecimal.TEN.pow(franctionLength);
			return value.divide(divisor);
		};
	}
	
	public Function<String, Date> parseDatetime(String dateFormat) {
		return s -> {
			try {
				return new SimpleDateFormat(dateFormat)
						.parse(s);
			} catch (ParseException e) {
				return null;
			}
		};
	}
	
	public Function<String, Boolean> parseBoolean(String expression) {
		return s -> {
			Boolean result = expressions.parse(expression)
					.getValue(Boolean.class, s);
			return result;
		};
	}

	@Override
	public void associate(ExpressionEvaluations expressions) {
		this.expressions = expressions;
	}

	public MessageConverter getMsgConverter() {
		return msgConverter;
	}

	public void setMsgConverter(MessageConverter msgConverter) {
		this.msgConverter = msgConverter;
	}
}
