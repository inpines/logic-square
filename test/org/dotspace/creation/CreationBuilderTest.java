package org.dotspace.creation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Map;

import org.dotspace.creation.constructor.Constructors;
import org.junit.jupiter.api.Test;

class CreationBuilderTest {

	private class MyPojo {

		private String name;

		protected String getName() {
			return name;
		}

		protected void setName(String name) {
			this.name = name;
		}
		
	}

	@Test
	public void testHashCreation() {
		Map<String, Object> hash = Creations.construct(
				Constructors.forHashMap(String.class, Object.class))
				.set(MemberAccessors.put("name"), "peter")
				.setIfPresent(MemberAccessors.put("date"), null)
				.set(MemberAccessors.put("nullValue"), null)
				.build();
		
		assertNotNull(hash);
		assertTrue(!hash.isEmpty());
		assertEquals("peter", hash.get("name"));
		
		assertTrue(!hash.containsKey("date"));
		assertTrue(hash.containsKey("nullValue"));
		assertTrue(null == hash.get("nullValue"));
		Map<String, Object> hash1 = Creations.construct(
				Constructors.forHashMap(String.class, Object.class))
				.setIfPresent(MemberAccessors.put("date"), 
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
				.build();
		
		assertNotNull(pojo);
		assertNotNull(pojo.getName());
		assertEquals("myName", pojo.getName());
		
	}
}
