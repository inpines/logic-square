package org.dotspace.oofp.model.dto.tramsform;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TransformationTestSourceInfo {

	private String name;
	
	private int age;
	
	private Long localDatetime;
	
	private List<Long> amts = new ArrayList<>();

}
