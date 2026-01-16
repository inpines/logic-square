package org.dotspace.oofp.utils.oofp.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.dotspace.oofp.utils.builder.GeneralBuilders;
import org.dotspace.oofp.utils.builder.operation.ContainerOps;
import org.dotspace.oofp.utils.builder.operation.WriteConditions;
import org.dotspace.oofp.utils.builder.operation.WriteOperations;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.junit.Test;

import static org.junit.Assert.*;

public class GeneralBuilderTest {

	@Data
	public static class MyPojoDetail {

		private String dtlName;

	}

	@Data
	public static class DataItem {

		private String name;
		
		private String deptName;

	}

	@Data
	public static class Department {

		private String name;

	}

	public static class MyPojo {

		@Getter
		@Setter
		private String name;

		@Setter
		private Department department;
				
		@Setter
        private List<MyPojoDetail> details;

		protected Department getDepartment() {
			return Optional.ofNullable(department)
					.orElseGet(() -> {
						Department result = new Department();
						setDepartment(result);
						return result;
					});
		}

		public List<MyPojoDetail> getDetails() {
			return Optional.ofNullable(details)
					.orElseGet(() -> {
						details = new ArrayList<>();
						return details;
					});
		}

    }

	@Test
	public void testBuildHash() {
		Date nullDate = null;
		Map<String, Object> hash = GeneralBuilders
				.supply(ContainerOps.<String, Object>supplyMap())
				.with(WriteOperations.set(
						ContainerOps.consumePutting(
								"name"), "peter"))
				.with(WriteOperations
						.set(ContainerOps.<String, Object>consumePutting("date"), nullDate)
						.require(WriteConditions.present(nullDate)))
				.with(WriteOperations
						.set(ContainerOps.consumePutting("nullValue"), null))
				.build();

		assertNotNull(hash);
        assertFalse(hash.isEmpty());
		assertEquals("peter", hash.get("name"));

        assertFalse(hash.containsKey("date"));
		assertTrue(hash.containsKey("nullValue"));
        assertNull(hash.get("nullValue"));
		
		Date curDate = Calendar.getInstance().getTime();
		
		Map<String, Object> hash1 = GeneralBuilders
				.supply(ContainerOps.<String, Object>supplyMap())
				.with(WriteOperations
						.set(ContainerOps.consumePutting("date"), curDate))
				.build();
		
		assertNotNull(hash1);
        assertFalse(hash1.isEmpty());
		assertTrue(hash1.containsKey("date"));
		assertNotNull(hash1.get("date"));

	}

	@Test
	public void testPojoCreation() {
		MyPojo pojo = GeneralBuilders.supply(MyPojo::new)
				.with(WriteOperations.set(MyPojo::setName, "myName"))
				.with(WriteOperations.set(
						MyPojo::getDepartment, Department::setName, "myDepart"))
				.build();
		
		assertNotNull(pojo);
		assertNotNull(pojo.getName());
		assertEquals("myName", pojo.getName());
		assertNotNull(pojo.getDepartment());
		assertEquals("myDepart", pojo.getDepartment().getName());
		
		MyPojo pojo1 = GeneralBuilders.supply(MyPojo::new)
				.with(WriteOperations.set(MyPojo::setName, "has value")
						.require(WriteConditions.matchContext("a"::equals, "a")))
				.with(WriteOperations.set(MyPojo::getDepartment, 
						Department::setName, "has myDepart")
						.require(WriteConditions.matchContext("a"::equals, "a")))
				.build();
		
		assertNotNull(pojo1);
		assertEquals("has value", pojo1.getName());
		assertNotNull(pojo1.getDepartment());
		assertEquals("has myDepart", pojo1.getDepartment().getName());
		
		MyPojo pojo2 = GeneralBuilders.supply(MyPojo::new)
				.with(WriteOperations.set(MyPojo::setName, "has value")
						.require(WriteConditions.matchContext(x -> !"a".equals(x), "a")))
				.with(WriteOperations.set(MyPojo::getDepartment, 
						Department::setName, "has myDepart")
						.require(WriteConditions.matchContext(x -> !"a".equals(x), "a")))
				.build();
		
		assertNotNull(pojo2);
        assertNull(pojo2.getName());
        assertNull(pojo2.department);
		assertNotNull(pojo2.getDepartment());
        assertNull(pojo2.getDepartment().getName());

	}
	
	@Test
	public void testPluralPojos() {
		
		List<String> dtls = Arrays.asList("dtl1", "dtl2", "dtl3");
		
		MyPojo pojo = GeneralBuilders.supply(MyPojo::new)
				.with(WriteOperations.setForEach(
						MyPojo::getDetails, this::getPojoDetail, dtls))
				.build();
		
		assertNotNull(pojo);
		assertNotNull(pojo.getDetails());
        assertFalse(pojo.getDetails().isEmpty());
		
		assertEquals(dtls.get(0), pojo.getDetails().get(0).dtlName);
		assertEquals(dtls.get(1), pojo.getDetails().get(1).dtlName);
		assertEquals(dtls.get(2), pojo.getDetails().get(2).dtlName);
		
	}
	
	private MyPojoDetail getPojoDetail(String name) {
		return GeneralBuilders.supply(MyPojoDetail::new)
				.with(WriteOperations.set(MyPojoDetail::setDtlName, name))
				.build();
	}

}
