package org.dotspace.creation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.dotspace.creation.functional.Constructors;
import org.dotspace.creation.policy.CreationCondition;
import org.dotspace.creation.policy.PluralCreationPolicy;
import org.dotspace.creation.policy.RootCreationPolicy;
import org.dotspace.creation.policy.SingularCreationPolicy;
import org.junit.jupiter.api.Test;

class CreationBuilderTest {

	public class MyPojoDetail {

		private String dtlName;

		public String getDtlName() {
			return dtlName;
		}

		public void setDtlName(String dtlName) {
			this.dtlName = dtlName;
		}
		
	}

	public class DataItem {

		private String name;
		
		private String deptName;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDeptName() {
			return deptName;
		}

		public void setDeptName(String deptName) {
			this.deptName = deptName;
		}
		
	}

	public class Department {

		private String name;

		protected String getName() {
			return name;
		}

		protected void setName(String name) {
			this.name = name;
		}
		
	}

	public class MyPojo {

		private String name;

		private Department department;
				
		private List<MyPojoDetail> details;
		
		protected String getName() {
			return name;
		}

		protected void setName(String name) {
			this.name = name;
		}

		protected Department getDepartment() {
			return Optional.ofNullable(department)
					.orElseGet(() -> {
						Department result = new Department();
						setDepartment(result);
						return result;
					});
		}

		protected void setDepartment(Department department) {
			this.department = department;
		}

		protected void setDataItem(DataItem dataItem) {
			this.name = dataItem.name;
			this.getDepartment().setName(dataItem.deptName);
		}

		public List<MyPojoDetail> getDetails() {
			return Optional.ofNullable(details)
					.orElseGet(() -> {
						details = new ArrayList<>();
						return details;
					});
		}

		public void setDetails(List<MyPojoDetail> details) {
			this.details = details;
		}
		
	}

	@Test
	public void testHashCreation() {
		Date nullDate = null;
		Map<String, Object> hash = Creations.construct(
				Constructors.forHashMap(String.class, Object.class))
				.take(RootCreationPolicy.withAssignment(
						AdtAccessors.forMapToPutByKeyOf(
								"name", Object.class), "peter"))
				.take(RootCreationPolicy.withAssignment(
						AdtAccessors.forMapToPutByKeyOf(
								"date", Object.class), nullDate)
						.when(CreationCondition.forValuePresent(nullDate)))
				.take(RootCreationPolicy.withAssignment(
						AdtAccessors.forMapToPutByKeyOf(
								"nullValue", Object.class), null))
				.build();
		
		assertNotNull(hash);
		assertTrue(!hash.isEmpty());
		assertEquals("peter", hash.get("name"));
		
		assertTrue(!hash.containsKey("date"));
		assertTrue(hash.containsKey("nullValue"));
		assertTrue(null == hash.get("nullValue"));
		
		Date curDate = Calendar.getInstance().getTime();
		
		Map<String, Object> hash1 = Creations.construct(
				Constructors.forHashMap(String.class, Object.class))
				.take(RootCreationPolicy.withAssignment(
						AdtAccessors.forMapToPutByKeyOf("date", Object.class), curDate)
						.when(CreationCondition.forValuePresent(curDate)))
				.build();
		assertNotNull(hash1);
		assertTrue(!hash1.isEmpty());
		assertTrue(hash1.containsKey("date"));
		assertNotNull(hash1.get("date"));

	}

	@Test
	public void testPojoCreation() {
		MyPojo pojo = Creations.construct(MyPojo::new)
				.take(RootCreationPolicy.withAssignment(MyPojo::setName, "myName"))
				.take(SingularCreationPolicy.withAssignment(
						MyPojo::getDepartment, Department::setName, "myDepart"))
				.build();
		
		assertNotNull(pojo);
		assertNotNull(pojo.getName());
		assertEquals("myName", pojo.getName());
		assertNotNull(pojo.getDepartment());
		assertEquals("myDepart", pojo.getDepartment().getName());
		
		MyPojo pojo1 = Creations.construct(MyPojo::new)
				.take(RootCreationPolicy.withAssignment(MyPojo::setName, "has value")
						.when(CreationCondition.forPredicate(x -> "a".equals(x), "a")))
				.take(SingularCreationPolicy.withAssignment(
						MyPojo::getDepartment, Department::setName, "has myDepart")
						.when(CreationCondition.forPredicate(x -> "a".equals(x), "a")))
				.build();
		
		assertNotNull(pojo1);
		assertEquals("has value", pojo1.getName());
		assertNotNull(pojo1.getDepartment());
		assertEquals("has myDepart", pojo1.getDepartment().getName());
		
		MyPojo pojo2 = Creations.construct(MyPojo::new)
				.take(RootCreationPolicy.withAssignment(MyPojo::setName, "has value")
						.when(CreationCondition.forPredicate(x -> !"a".equals(x), "a")))
				.take(SingularCreationPolicy.withAssignment(
						MyPojo::getDepartment, Department::setName, "has myDepart")
						.when(CreationCondition.forPredicate(x -> !"a".equals(x), "a")))
				.build();
		
		assertNotNull(pojo2);
		assertEquals(null, pojo2.getName());
		assertEquals(null, pojo2.department);
		assertNotNull(pojo2.getDepartment());
		assertEquals(null, pojo2.getDepartment().getName());

	}
	
	@Test
	public void testPluralPojos() {
		
		List<String> dtls = Arrays.asList("dtl1", "dtl2", "dtl3");
		
		MyPojo pojo = Creations.construct(MyPojo::new)
				.take(PluralCreationPolicy.withAssignment(MyPojo::getDetails, 
						name -> Creations
						.construct(MyPojoDetail::new)
						.take(RootCreationPolicy.withAssignment(
								MyPojoDetail::setDtlName, name))
						.build(), dtls))
				.build();
		
		assertNotNull(pojo);
		assertNotNull(pojo.getDetails());
		assertTrue(!pojo.getDetails().isEmpty());
		
		assertEquals(dtls.get(0), pojo.getDetails().get(0).dtlName);
		assertEquals(dtls.get(1), pojo.getDetails().get(1).dtlName);
		assertEquals(dtls.get(2), pojo.getDetails().get(2).dtlName);
		
	}
}
