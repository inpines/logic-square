package org.dotspace.oofp.support.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.dotspace.oofp.support.FixedLengthTokenizer;
import org.dotspace.oofp.support.FixedLengthTokenizers;
import org.dotspace.oofp.support.builder.GeneralBuilders;
import org.dotspace.oofp.support.builder.writer.GeneralBuildingWriters;
import org.dotspace.oofp.support.test.dto.TestMessage;
import org.dotspace.oofp.support.test.tokenizer.PojoMappingCollectorTestPojo;
import org.dotspace.oofp.support.test.tokenizer.Student;
import org.dotspace.oofp.support.test.tokenizer.TokenizerTestInfo;
import org.dotspace.oofp.support.test.tokenizer.Worker;
import org.dotspace.oofp.support.tokenizer.FixedLengthTokenizationAction;
import org.dotspace.oofp.support.tokenizer.FixedLengthTokenizationActions;
import org.dotspace.oofp.support.tokenizer.TokenizationResult;
import org.dotspace.oofp.util.functional.BiConsumers;
import org.dotspace.oofp.util.functional.Suppliers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/applicationContext-fixedlengthtokenizer.xml"})
public class FixedLengthTokenizerTest {

	@Autowired
	private FixedLengthTokenizers fixedLengthTokenizers;
	
	@Qualifier("tokenizertestAction")
	@Autowired
	private FixedLengthTokenizationActions<TokenizerTestInfo> testFixedLengthTokenizationActions;
	
	@Test
	public void testTokenizePojo() {				
		FixedLengthTokenizer<?, PojoMappingCollectorTestPojo> tokenizer = 
				fixedLengthTokenizers.tokenize(PojoMappingCollectorTestPojo::new)
				.add("name", Object::toString, 6)
				.add("age", Integer::parseInt, 2)
				.add("birthday", s -> toDate(s), 8)
				.add("merried", this::isYOrT, 1);
		
		TokenizationResult<PojoMappingCollectorTestPojo> result = tokenizer
				.split("myName2220220101y");
		
		PojoMappingCollectorTestPojo pojo = result.getRoot();
		assertNotNull(pojo);
		assertEquals("myName", pojo.getName());
		assertEquals(22, pojo.getAge().intValue());
		assertEquals("20220101", toDateText(pojo.getBirthday()));
		assertTrue(pojo.getMerried());
		assertTrue(17L == tokenizer.getTotalSize());
	}

	private boolean isYOrT(String s) {
		return Arrays.asList("Y", "T").stream()
				.anyMatch(x -> x.equals(s.toUpperCase()));
	}

	private Date toDate(String s) {
		try {
			return new SimpleDateFormat("yyyyMMdd").parse(s);
		} catch (ParseException e) {
			return null;
		}
	}

	private String toDateText(Date date) {
		return new SimpleDateFormat("yyyyMMdd").format(date);
	}
	
	@Test
	public void testTokenizePojoWithFixedLengthTokenizationActions() {
		
		TokenizationResult<PojoMappingCollectorTestPojo> result = 
				FixedLengthTokenizationActions.into(
						PojoMappingCollectorTestPojo.class, GeneralBuilders
						.of(Suppliers.newList(FixedLengthTokenizationAction.class))
						.with(GeneralBuildingWriters.set(
								BiConsumers.forListOf(
										FixedLengthTokenizationAction.class), 
								GeneralBuilders.of(FixedLengthTokenizationAction::new)
								.with(GeneralBuildingWriters.set(
										FixedLengthTokenizationAction::setPropertyPath, 
										"name"))
								.with(GeneralBuildingWriters.set(
										FixedLengthTokenizationAction::setTextLength, 6))
								.with(GeneralBuildingWriters.set(
										FixedLengthTokenizationAction::setValueMapperExpression, 
										"getText"))
								.build()))
						.with(GeneralBuildingWriters.set(
								BiConsumers.forListOf(FixedLengthTokenizationAction.class), 
								GeneralBuilders.of(FixedLengthTokenizationAction::new)
								.with(GeneralBuildingWriters.set(
										FixedLengthTokenizationAction::setPropertyPath, "age"))
								.with(GeneralBuildingWriters.set(
										FixedLengthTokenizationAction::setTextLength, 2))
								.with(GeneralBuildingWriters.set(
										FixedLengthTokenizationAction::setValueMapperExpression, 
										"parseIntValue"))
								.build()))
						.with(GeneralBuildingWriters.set(
								BiConsumers.forListOf(FixedLengthTokenizationAction.class), 
								GeneralBuilders.of(FixedLengthTokenizationAction::new)
								.with(GeneralBuildingWriters.set(
										FixedLengthTokenizationAction::setPropertyPath, "birthday"))
								.with(GeneralBuildingWriters.set(
										FixedLengthTokenizationAction::setTextLength, 8))
								.with(GeneralBuildingWriters.set(
										FixedLengthTokenizationAction::setValueMapperExpression, 
										"parseDatetime(#options)"))
								.with(GeneralBuildingWriters.set(
										FixedLengthTokenizationAction::setOptionsExpression, 
										"'yyyyMMdd'"))
								.build()))
						.with(GeneralBuildingWriters.set(
								BiConsumers.forListOf(FixedLengthTokenizationAction.class), 
								GeneralBuilders.of(FixedLengthTokenizationAction::new)
								.with(GeneralBuildingWriters.set(
										FixedLengthTokenizationAction::setPropertyPath, "merried"))
								.with(GeneralBuildingWriters.set(
										FixedLengthTokenizationAction::setTextLength, 1))
								.with(GeneralBuildingWriters.set(
										FixedLengthTokenizationAction::setValueMapperExpression, 
										"parseBoolean(#options)"))
								.with(GeneralBuildingWriters.set(
										FixedLengthTokenizationAction::setOptionsExpression, 
										"'''Y''.equals(#root.toUpperCase())'"))
								.build()))
						.build())
//				.parse("name", 6, "getText")
//				.parse("age", 2, "parseIntValue")
//				.parse("birthday", 8, "parseDatetime(#options)", "'yyyyMMdd'")
//				.parse("merried", 1, "parseBoolean(#options)", 
//						"'''Y''.equals(#root.toUpperCase())'")
				.tokenize(fixedLengthTokenizers, "myName2220220101y");
				
		PojoMappingCollectorTestPojo pojo = result.getRoot();
		
		assertNotNull(pojo);
		assertEquals("myName", pojo.getName());
		assertEquals(22, pojo.getAge().intValue());
		assertEquals("20220101", toDateText(pojo.getBirthday()));
		assertTrue(pojo.getMerried());
		
		assertEquals(17L, result.getTokenizedTextSize().longValue());
	}

	@Test
	public void testTokenizePojoWithAddingActions() {
		TokenizationResult<PojoMappingCollectorTestPojo> result = 
				fixedLengthTokenizers
				.tokenize(PojoMappingCollectorTestPojo::new)
				.addAll(GeneralBuilders
						.of(Suppliers.newList(FixedLengthTokenizationAction.class))
						.with(GeneralBuildingWriters.set(
								BiConsumers.forListOf(FixedLengthTokenizationAction.class), GeneralBuilders.of(FixedLengthTokenizationAction::new)
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setPropertyPath, "name"))
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setTextLength, 6))
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setValueMapperExpression, "getText"))
								.build()))
						.with(GeneralBuildingWriters.set(
								BiConsumers.forListOf(FixedLengthTokenizationAction.class), GeneralBuilders.of(FixedLengthTokenizationAction::new)
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setPropertyPath, "age"))
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setTextLength, 2))
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setValueMapperExpression, "parseIntValue"))
								.build()))
						.with(GeneralBuildingWriters.set(
								BiConsumers.forListOf(FixedLengthTokenizationAction.class), GeneralBuilders.of(FixedLengthTokenizationAction::new)
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setPropertyPath, "birthday"))
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setTextLength, 8))
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setValueMapperExpression, "parseDatetime(#options)"))
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setOptionsExpression, "'yyyyMMdd'"))
								.build()))
						.with(GeneralBuildingWriters.set(
								BiConsumers.forListOf(FixedLengthTokenizationAction.class), GeneralBuilders.of(FixedLengthTokenizationAction::new)
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setPropertyPath, "merried"))
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setTextLength, 1))
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setValueMapperExpression, "parseBoolean(#options)"))
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setOptionsExpression, "'''Y''.equals(#root.toUpperCase())'"))
								.build()))
						.build())
//				.add("name", Object::toString, 6)
//				.add("age", Integer::parseInt, 2)
//				.add("birthday", s -> toDate(s), 8)
//				.add("merried", this::isYOrT, 1)
				.split("myName2220220101y");
		
		PojoMappingCollectorTestPojo pojo = result.getRoot();
		
		assertNotNull(pojo );
		assertEquals("myName", pojo.getName());
		assertEquals(22, pojo.getAge().intValue());
		assertEquals("20220101", toDateText(pojo.getBirthday()));
		assertTrue(pojo.getMerried());

	}

	@Test
	public void testTokenizeMapWithAddingActions() {
		TokenizationResult<TestMessage> result = fixedLengthTokenizers
				.tokenize(TestMessage::new)
				.addAll(GeneralBuilders
						.of(Suppliers.newList(FixedLengthTokenizationAction.class))
						.with(GeneralBuildingWriters.set(
								BiConsumers.forListOf(FixedLengthTokenizationAction.class), GeneralBuilders.of(FixedLengthTokenizationAction::new)
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setPropertyPath, "body[name]"))
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setTextLength, 6))
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setValueMapperExpression, "getText"))
								.build()))
						.with(GeneralBuildingWriters.set(
								BiConsumers.forListOf(FixedLengthTokenizationAction.class), GeneralBuilders.of(FixedLengthTokenizationAction::new)
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setPropertyPath, "body[age]"))
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setTextLength, 2))
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setValueMapperExpression, "parseIntValue"))
								.build()))
						.with(GeneralBuildingWriters.set(
								BiConsumers.forListOf(FixedLengthTokenizationAction.class), GeneralBuilders.of(FixedLengthTokenizationAction::new)
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setPropertyPath, "body[birthday]"))
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setTextLength, 8))
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setValueMapperExpression, "parseDatetime(#options)"))
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setOptionsExpression, "'yyyyMMdd'"))
								.build()))
						.with(GeneralBuildingWriters.set(
								BiConsumers.forListOf(FixedLengthTokenizationAction.class), GeneralBuilders.of(FixedLengthTokenizationAction::new)
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setPropertyPath, "body[merried]"))
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setTextLength, 1))
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setValueMapperExpression, "parseBoolean(#options)"))
								.with(GeneralBuildingWriters.set(FixedLengthTokenizationAction::setOptionsExpression, "'''Y''.equals(#root.toUpperCase())'"))
								.build()))
						.build())
				.split("myName2220220101y");
		
		TestMessage tmsg = result.getRoot();
		assertNotNull(tmsg);
		assertEquals("myName", tmsg.getBody().get("name"));
		assertEquals(22, tmsg.getBody().get("age"));
		assertEquals("20220101", toDateText((Date) tmsg.getBody().get("birthday")));
		assertTrue((Boolean) tmsg.getBody().get("merried"));		
	}
	
	@Test
	public void testTokenizeDetail() {
		TokenizationResult<TokenizerTestInfo> result = testFixedLengthTokenizationActions
				.tokenize(fixedLengthTokenizers, "myName7890202201010mySchool90001");
		
		assertNotNull(result);
		
		TokenizerTestInfo testInfo = result.getRoot();
		assertNotNull(testInfo);
		assertEquals("myName7890", testInfo.getName());
		assertEquals("20220101", toDateText(testInfo.getBirthday()));
		assertEquals(0, testInfo.getRoleId());
		assertEquals(1, testInfo.getAge().intValue());
		
		assertEquals(32L, result.getTokenizedTextSize().longValue());
		
		assertTrue(!result.getMappingItemEntries().isEmpty());
		
		Student student = (Student) result.findMappingItem("role:0");
		
		assertNotNull(student);
		assertEquals("mySchool90", student.getSchool());
		
		TokenizationResult<TokenizerTestInfo> result1 = testFixedLengthTokenizationActions
				.tokenize(fixedLengthTokenizers, "yourName90202202021yourJob89012345n030");
		
		assertNotNull(result1);
		
		TokenizerTestInfo testInfo1 = result1.getRoot();
		assertNotNull(testInfo1);
		assertEquals("yourName90", testInfo1.getName());
		assertEquals("20220202", toDateText(testInfo1.getBirthday()));
		assertEquals(1, testInfo1.getRoleId());
		assertEquals(30, testInfo1.getAge().intValue());
		
		assertEquals(38L, result1.getTokenizedTextSize().longValue());
		
		assertTrue(!result1.getMappingItemEntries().isEmpty());
		
		Worker worker = (Worker) result1.findMappingItem("role:1");
		assertNotNull(worker);
		assertEquals("yourJob89012345", worker.getJob());
		assertTrue(!worker.isMerried());
		
	}
}
