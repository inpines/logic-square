package org.dotspace.oofp.util.functional.collector;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.apache.commons.lang3.tuple.Pair;
import org.dotspace.oofp.support.builder.GeneralBuilders;
import org.dotspace.oofp.support.builder.GeneralBuildingWriter;
import org.dotspace.oofp.support.builder.writer.GeneralBuildingWriters;
import org.dotspace.oofp.util.Reflections;
import org.dotspace.oofp.util.TypeConversions;
import org.dotspace.oofp.util.functional.Suppliers;
import org.springframework.util.ReflectionUtils;

public class ClazzMappingCollector<R> implements Collector<Map.Entry<String, Object>,  Map<String, Object>, R> {

	private Class<R> clazz;
	
	private Consumer<Map<String, Object>> summerizer = null;
	
	public ClazzMappingCollector(Class<R> pojoClazz) {
		this.clazz = pojoClazz;
	}

	public ClazzMappingCollector(Class<R> pojoClazz, Consumer<Map<String, Object>> summerizer) {
		this.clazz = pojoClazz;
		this.summerizer = summerizer;
	}
	
	@Override
	public Supplier<Map<String, Object>> supplier() {
		return Suppliers.newHashMap(String.class, Object.class);
	}

	@Override
	public BiConsumer<Map<String, Object>, Map.Entry<String, Object>> accumulator() {
		return (acc, e) -> {
			acc.put(e.getKey(), e.getValue());
		};
	}

	@Override
	public BinaryOperator<Map<String, Object>> combiner() {
		return (first, second) -> {
			first.putAll(second);
			return first;
		};
	}

	@Override
	public Function<Map<String, Object>, R> finisher() {
		
		return acc -> {
		
			if (null != summerizer) {
				summerizer.accept(acc);
			}
			
			Map<String, Object> m = new HashMap<>(acc);
//			ClazzAttributeAccessor<R> caa = AttributeAccessors
//					.forClazz(clazz);
			
			List<GeneralBuildingWriter<R, Object>> assignmentExpressions =
					new ArrayList<>();
			
			Iterator<Entry<String, Object>> itr = m.entrySet().iterator();
			
			for (;itr.hasNext();) {
				Entry<String, Object> e = itr.next();
				Optional<Pair<Field, Object>> fieldContent = TypeConversions
						.convertFieldContent(clazz, e);

				fieldContent.ifPresent(fldcnt -> {
					assignmentExpressions.add(GeneralBuildingWriters.set(
							(t, v) -> setFieldValue(t, fldcnt.getKey(), fldcnt.getValue()),
							e.getValue()));					
				});
			}
			/*
					m.entrySet().stream()
					.filter(e -> {
						Field field = ReflectionUtils.findField(
								pojoClazz, e.getKey());
						return field.getModifiers() != Modifier.STATIC;
					})
					.map(e -> Assignments.set(
							caa.forWriting(e.getKey()), e.getValue()))
					.collect(Collectors.toList());
			*/
			return GeneralBuilders.of(() -> newResult())
					.with(assignmentExpressions)
					.build();
		};
	}

	private synchronized <T, V> void setFieldValue(T instance, Field field, V value) {
		boolean isAccessible = field.isAccessible();
				//field.canAccess(instance);
		try {
			if (!isAccessible) {
				field.setAccessible(true);
			}
			ReflectionUtils.setField(field, instance, value);			
		}
		finally {
			if (!isAccessible) {
				field.setAccessible(isAccessible);
			}
		}
	}
	
	private R newResult() {
		return Reflections.construct(clazz);
	}

	@Override
	public Set<Characteristics> characteristics() {
		return EnumSet.of(Characteristics.CONCURRENT, 
				Characteristics.UNORDERED);
	}

}
