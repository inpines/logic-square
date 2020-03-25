package org.dotspace.creation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.dotspace.creation.functional.Constructors;
import org.dotspace.creation.policy.CreationCondition;
import org.dotspace.creation.policy.RootCreationPolicy;
import org.dotspace.creation.policy.SingularCreationPolicy;
import org.junit.jupiter.api.Test;

class CreationBuilderTest {

	public class Department {

		private String name;

		protected String getName() {
			return name;
		}

		protected void setName(String name) {
			this.name = name;
		}
		
	}

	private class MyPojo {

		private String name;

		private Department department;
		
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
}
