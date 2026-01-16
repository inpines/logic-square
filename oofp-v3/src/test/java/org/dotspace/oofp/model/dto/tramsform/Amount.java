package org.dotspace.oofp.model.dto.tramsform;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class Amount {

	private BigDecimal amt;
	private String ccy;

	public Amount(BigDecimal amt, String ccy) {
		this.amt = amt;
		this.ccy = ccy;
	}

}
