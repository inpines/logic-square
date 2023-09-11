package org.dotspace.oofp.support.test.dto;

import java.math.BigDecimal;
import java.util.Date;

public class TestReportModel {

	private String name;
	
	private Date date;
	
	private String ccy;
	
	private BigDecimal amt;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCcy() {
		return ccy;
	}

	public void setCcy(String ccy) {
		this.ccy = ccy;
	}

	public BigDecimal getAmt() {
		return amt;
	}

	public void setAmt(BigDecimal amt) {
		this.amt = amt;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
}
