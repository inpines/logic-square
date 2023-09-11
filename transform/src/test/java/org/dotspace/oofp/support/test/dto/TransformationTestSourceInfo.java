package org.dotspace.oofp.support.test.dto;

import java.util.ArrayList;
import java.util.List;

public class TransformationTestSourceInfo {

	private String name;
	
	private int age;
	
	private Long localDatetime;
	
	private List<Long> amts = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Long getLocalDatetime() {
		return localDatetime;
	}

	public void setLocalDatetime(Long localDatetime) {
		this.localDatetime = localDatetime;
	}

	public List<Long> getAmts() {
		return amts;
	}

}
