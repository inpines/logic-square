package org.dotspace.oofp.support.test.functional;

import java.math.BigDecimal;

public class Amount {

	private BigDecimal amt;
	private String ccy;

	public Amount(BigDecimal amt, String ccy) {
		this.amt = amt;
		this.ccy = ccy;
	}

	public BigDecimal getAmt() {
		return amt;
	}

	public void setAmt(BigDecimal amt) {
		this.amt = amt;
	}

	public String getCcy() {
		return ccy;
	}

	public void setCcy(String ccy) {
		this.ccy = ccy;
	}
}
