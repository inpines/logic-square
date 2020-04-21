package org.dotspace.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.dotspace.creation.Creations;
import org.dotspace.creation.expression.SingularPath;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;

public class SerializationTest {

	public class MyModel {

		private String name;
		
		private Integer value;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Integer getValue() {
			return value;
		}

		public void setValue(Integer value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return "MyModel [name=" + name + ", value=" + value + "]";
		}
		
	}

	@Test
	public void test() {
		MyModel model = Creations.construct(MyModel::new)
				.take(SingularPath.getRootToSet(MyModel::setName).assign("tom"))
				.take(SingularPath.getRootToSet(MyModel::setValue).assign(10))
				.build();
		
		Map<String, Object> result = Serializations.from(model).trace(x -> {
			System.out.print(x);
			return x;
		}).transform(new TypeReference<Map<String, Object>>() {});
		
		assertNotNull(result);
		assertTrue(result.size() == 2);
		assertEquals(result.get("name"), "tom");
		assertEquals(result.get("value"), 10);
	}

}
