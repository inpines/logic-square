package org.dotspace.oofp.support.test.dto;

import java.util.Date;
import java.util.List;

public class TransformationTestResult {

	private String name;
	
	private int age;
	
	private Date ldt;
	
	private int count;
	
	private long total;

	private List<Long> anyGeThreeAllAmts;
	
	private List<Long> allGeThreeAllAmts;
	
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

	public Date getLdt() {
		return ldt;
	}

	public void setLdt(Date ldt) {
		this.ldt = ldt;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public List<Long> getAnyGeThreeAllAmts() {
		return anyGeThreeAllAmts;
	}

	public void setAnyGeThreeAllAmts(List<Long> anyGeThreeAllAmts) {
		this.anyGeThreeAllAmts = anyGeThreeAllAmts;
	}

	public List<Long> getAllGeThreeAllAmts() {
		return allGeThreeAllAmts;
	}

	public void setAllGeThreeAllAmts(List<Long> allGeThreeAllAmts) {
		this.allGeThreeAllAmts = allGeThreeAllAmts;
	}
	
}
