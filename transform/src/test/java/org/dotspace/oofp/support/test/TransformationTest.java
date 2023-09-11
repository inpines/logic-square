package org.dotspace.oofp.support.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import org.dotspace.oofp.support.builder.GeneralBuilders;
import org.dotspace.oofp.support.builder.writer.GeneralBuildingWriters;
import org.dotspace.oofp.support.test.dto.TransformationTestResult;
import org.dotspace.oofp.support.test.dto.TransformationTestSourceInfo;
import org.dotspace.oofp.support.transform.TransformAction;
import org.dotspace.oofp.support.transform.TransformActions;
import org.dotspace.oofp.support.transform.TransformationContext;
import org.dotspace.oofp.support.transform.Transformations;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(locations={"/applicationContext-test-transform.xml"})
public class TransformationTest {

	private SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
	
//	@Autowired
//	private GeneralTransformation generalTransformation;
	
	@Autowired
	private Transformations transformations;
	
	@Before
	public void setUp() throws Exception {
	}

	/*
	@Test
	public void testGeneralTransformation() {

		TransformationTestSourceInfo sourceInfo = getSourceInfo();
		
		List<TransformMapping> transformMappers = GeneralBuilders
				.of(Suppliers.newList(TransformMapping.class))
				.with(GeneralBuildingWriters.set(BiConsumers.forListOf(TransformMapping.class), 
						GeneralBuilders.of(TransformMapping::new)
						.with(GeneralBuildingWriters.set(TransformMapping::setMapperItemSequence, 1))
						.with(GeneralBuildingWriters.set(TransformMapping::setSourceExpression, "name"))
						.with(GeneralBuildingWriters.set(TransformMapping::setDestinationExpression, "name"))
						.build()))
				.with(GeneralBuildingWriters.set(BiConsumers.forListOf(TransformMapping.class), 
						GeneralBuilders.of(TransformMapping::new)
						.with(GeneralBuildingWriters.set(TransformMapping::setMapperItemSequence, 2))
						.with(GeneralBuildingWriters.set(TransformMapping::setSourceExpression, "age"))
						.with(GeneralBuildingWriters.set(TransformMapping::setDestinationExpression, "age"))
						.build()))
				.with(GeneralBuildingWriters.set(BiConsumers.forListOf(TransformMapping.class), 
						GeneralBuilders.of(TransformMapping::new)
						.with(GeneralBuildingWriters.set(TransformMapping::setMapperItemSequence, 3))
						.with(GeneralBuildingWriters.set(TransformMapping::setSourceExpression, "localDatetime"))
						.with(GeneralBuildingWriters.set(TransformMapping::setValueMapperName, "toDateTime"))
						.with(GeneralBuildingWriters.set(TransformMapping::setDestinationExpression, "ldt"))
						.build()))
				.with(GeneralBuildingWriters.set(BiConsumers.forListOf(TransformMapping.class), 
						GeneralBuilders.of(TransformMapping::new)
						.with(GeneralBuildingWriters.set(TransformMapping::setMapperItemSequence, 4))
						.with(GeneralBuildingWriters.set(TransformMapping::setSourceExpression, "amts"))
						.with(GeneralBuildingWriters.set(TransformMapping::setCollectorName, "countToInteger"))
						.with(GeneralBuildingWriters.set(TransformMapping::setDestinationExpression, "count"))
						.build()))
				.with(GeneralBuildingWriters.set(BiConsumers.forListOf(TransformMapping.class), 
						GeneralBuilders.of(TransformMapping::new)
						.with(GeneralBuildingWriters.set(TransformMapping::setMapperItemSequence, 5))
						.with(GeneralBuildingWriters.set(TransformMapping::setSourceExpression, "amts"))
						.with(GeneralBuildingWriters.set(TransformMapping::setCollectorName, "totalLong"))
						.with(GeneralBuildingWriters.set(TransformMapping::setDestinationExpression, "total"))
						.build()))
				.with(GeneralBuildingWriters.set(BiConsumers.forListOf(TransformMapping.class), 
						GeneralBuilders.of(TransformMapping::new)
						.with(GeneralBuildingWriters.set(TransformMapping::setMapperItemSequence, 6))
						.with(GeneralBuildingWriters.set(TransformMapping::setSourceExpression, "amts"))
						.with(GeneralBuildingWriters.set(TransformMapping::setPredicateName, "anyGt"))
						.with(GeneralBuildingWriters.set(TransformMapping::setPredicateOptions, "300L"))
						.with(GeneralBuildingWriters.set(TransformMapping::setCollectorName, "toList"))
						.with(GeneralBuildingWriters.set(TransformMapping::setDestinationExpression, "anyGeThreeAllAmts"))
						.build()))
				.with(GeneralBuildingWriters.set(BiConsumers.forListOf(TransformMapping.class), 
						GeneralBuilders.of(TransformMapping::new)
						.with(GeneralBuildingWriters.set(TransformMapping::setMapperItemSequence, 7))
						.with(GeneralBuildingWriters.set(TransformMapping::setSourceExpression, "amts"))
						.with(GeneralBuildingWriters.set(TransformMapping::setPredicateName, "allGt"))
						.with(GeneralBuildingWriters.set(TransformMapping::setPredicateOptions, "300L"))
						.with(GeneralBuildingWriters.set(TransformMapping::setCollectorName, "toList"))
						.with(GeneralBuildingWriters.set(TransformMapping::setDestinationExpression, "allGeThreeAllAmts"))
						.build()))
				.build();
		
		GeneralTransformationRequest<?, TransformationTestResult> request = 
				new GeneralTransformationRequest<>(TransformationTestResult::new, 
						sourceInfo, transformMappers);
		
		TransformationTestResult result = generalTransformation.transform(request);
		
		assertNotNull(result);
		assertEquals(sourceInfo.getName(), result.getName());
		assertEquals(sourceInfo.getAge(), result.getAge());
		assertEquals(sourceInfo.getLocalDatetime().longValue(), Long.parseLong(format.format(result.getLdt())));
		assertEquals(5L, result.getCount());
		assertNotNull(result.getAnyGeThreeAllAmts());
		assertTrue(result.getAnyGeThreeAllAmts().size() == 5);
		assertNull(result.getAllGeThreeAllAmts());
		assertEquals(1500L, result.getTotal());
	}
	*/
	
	private TransformationTestSourceInfo getSourceInfo() {
		Date date = Calendar.getInstance().getTime();
		Long julianDatetime = Long.parseLong(format.format(date));
		
		TransformationTestSourceInfo sourceInfo = GeneralBuilders.of(TransformationTestSourceInfo::new)
				.with(GeneralBuildingWriters.set(TransformationTestSourceInfo::setName, "John"))
				.with(GeneralBuildingWriters.set(TransformationTestSourceInfo::setAge, 25))
				.with(GeneralBuildingWriters.set(TransformationTestSourceInfo::setLocalDatetime, julianDatetime))
				.with(GeneralBuildingWriters.setForEach(TransformationTestSourceInfo::getAmts, i -> new Long(i) * 100, 
						Arrays.asList(1,2,3,4,5)))
				.build();
		return sourceInfo;
	}
	
	@Test
	public void testTransformationContext() {

		TransformationTestSourceInfo sourceInfo = getSourceInfo();

		TransformationTestResult result = transformations
				.into(TransformationTestResult::new)
				.with(TransformActions.read("name")
						.write("setName(#value)"))
				.with(TransformActions.read("age")
						.write("setAge(#value)"))
				.with(TransformActions.read("localDatetime")
						.map("toDateTime")
						.write("ldt"))
				.with(TransformActions.read("amts")
						.collect("count","countToInteger"))
				.with(TransformActions.read("amts")
						.collect("total", "totalLong"))
				.with(TransformActions.read("amts")
						.filter("anyGt", "300L")
						.collect("anyGeThreeAllAmts", "toList"))
				.with(TransformActions.read("amts")
						.filter("allGt", "300L")
						.collect("allGeThreeAllAmts", "toList"))
				.transform(sourceInfo);
		
		assertNotNull(result);
		assertEquals(sourceInfo.getName(), result.getName());
		assertEquals(sourceInfo.getAge(), result.getAge());
		assertEquals(sourceInfo.getLocalDatetime().longValue(), 
				Long.parseLong(format.format(result.getLdt())));
		assertEquals(5L, result.getCount());
		assertNotNull(result.getAnyGeThreeAllAmts());
		assertTrue(result.getAnyGeThreeAllAmts().size() == 5);
		assertNull(result.getAllGeThreeAllAmts());
		assertEquals(1500L, result.getTotal());

	}
	
	@Test
	public void testTransformationContextWithAction() {
		
		TransformationTestSourceInfo sourceInfo = getSourceInfo();
		TransformationContext<TransformationTestResult, ?> transformation = 
				getTransformation(TransformationTestResult::new);
		
		assertNotNull(transformation);
		
		TransformationTestResult result = transformation.transform(sourceInfo);
		
		assertNotNull(result);
		assertEquals(sourceInfo.getName(), result.getName());
		assertEquals(sourceInfo.getAge(), result.getAge());
		assertEquals(sourceInfo.getLocalDatetime().longValue(), 
				Long.parseLong(format.format(result.getLdt())));
		assertEquals(5L, result.getCount());
		assertNotNull(result.getAnyGeThreeAllAmts());
		assertTrue(result.getAnyGeThreeAllAmts().size() == 5);
		assertNull(result.getAllGeThreeAllAmts());
		assertEquals(1500L, result.getTotal());

	}

	private <T> TransformationContext<T, ?> getTransformation(
			Supplier<T> constructor) {

		File file;
		try {
			file = new ClassPathResource("/testTransformActions.json").getFile();
			
			ObjectMapper objectMapper = new ObjectMapper();
			List<TransformAction> transformActions = objectMapper.readValue(
					file, new TypeReference<List<TransformAction>>() {});
			
			return transformations
					.into(constructor, transformActions);

		} catch (IOException e) {
			return null;
		}
	}

}
