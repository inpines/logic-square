package org.dotspace.oofp.support.test.tokenizer;

import java.util.Date;

public class PojoMappingCollectorTestPojo {
	
	private String name;

	private Integer age;

	private Date birthday;
	
	private Boolean merried;

	public PojoMappingCollectorTestPojo() {
		super();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public Boolean getMerried() {
		return merried;
	}

	public void setMerried(Boolean merried) {
		this.merried = merried;
	}

}
