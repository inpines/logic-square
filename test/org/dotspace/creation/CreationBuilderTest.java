package org.dotspace.creation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.dotspace.creation.constructor.Constructors;
import org.junit.jupiter.api.Test;

class CreationBuilderTest {

	@Test
	void testHashCreation() {
		Map<String, Object> hash = Creations.construct(
				Constructors.forHashMap(String.class, Object.class))
				.set(MemberAccessors.put("name", Object.class), "peter")
				.build();
		
		assertNotNull(hash);
		assertTrue(!hash.isEmpty());
		assertEquals("peter", hash.get("name"));
		
	}

}
