package org.dotspace.oofp.model.dto.tramsform;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TransformationTestResult {

	private String name;
	
	private int age;
	
	private Date ldt;
	
	private int count;
	
	private long total;

	private List<Long> anyGeThreeAllAmts;
	
	private List<Long> allGeThreeAllAmts;
	
}
