package org.dotspace.creation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Map;
import java.util.Optional;

import org.dotspace.creation.constructor.Constructors;
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
		Map<String, Object> hash = Creations.construct(
				Constructors.forHashMap(String.class, Object.class))
				.set(AdtAccessors.forMapToPutByKeyOf("name"), "peter")
				.setIfPresent(AdtAccessors.forMapToPutByKeyOf("date"), null)
				.set(AdtAccessors.forMapToPutByKeyOf("nullValue"), null)
				.build();
		
		assertNotNull(hash);
		assertTrue(!hash.isEmpty());
		assertEquals("peter", hash.get("name"));
		
		assertTrue(!hash.containsKey("date"));
		assertTrue(hash.containsKey("nullValue"));
		assertTrue(null == hash.get("nullValue"));
		Map<String, Object> hash1 = Creations.construct(
				Constructors.forHashMap(String.class, Object.class))
				.setIfPresent(AdtAccessors.forMapToPutByKeyOf("date"), 
						Calendar.getInstance().getTime())
				.build();
		assertNotNull(hash1);
		assertTrue(!hash1.isEmpty());
		assertTrue(hash1.containsKey("date"));
		assertNotNull(hash1.get("date"));

	}

	@Test
	public void testPojoCreation() {
		MyPojo pojo = Creations.construct(MyPojo::new)
				.set(MyPojo::setName, "myName")
				.set(MyPojo::getDepartment, Department::setName, "myDepart")
				.build();
		
		assertNotNull(pojo);
		assertNotNull(pojo.getName());
		assertEquals("myName", pojo.getName());
		assertNotNull(pojo.getDepartment());
		assertEquals("myDepart", pojo.getDepartment().getName());
		
		MyPojo pojo1 = Creations.construct(MyPojo::new)
				.set(MyPojo::setName, "has value", x -> "a".equals(x), "a")
				.set(MyPojo::getDepartment, Department::setName, "has myDepart", 
						x -> "a".equals(x), "a")
				.build();
		
		assertNotNull(pojo1);
		assertEquals("has value", pojo1.getName());
		assertNotNull(pojo1.getDepartment());
		assertEquals("has myDepart", pojo1.getDepartment().getName());
		
		MyPojo pojo2 = Creations.construct(MyPojo::new)
				.set(MyPojo::setName, "has value", x -> !"a".equals(x), "a")
				.set(MyPojo::getDepartment, Department::setName, "has myDepart", 
						x -> !"a".equals(x), "a")
				.build();
		
		assertNotNull(pojo2);
		assertEquals(null, pojo2.getName());
		assertEquals(null, pojo2.department);
		assertNotNull(pojo2.getDepartment());
		assertEquals(null, pojo2.getDepartment().getName());

	}
}
