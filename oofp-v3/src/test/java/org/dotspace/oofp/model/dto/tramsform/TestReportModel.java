package org.dotspace.oofp.model.dto.tramsform;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Setter
@Getter
public class TestReportModel {

	private String name;
	
	private Date date;
	
	private String ccy;
	
	private BigDecimal amt;

}
