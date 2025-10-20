package org.dotspace.oofp.support.expression.transform;

import lombok.AllArgsConstructor;
import org.dotspace.oofp.support.expression.ExpressionEvaluators;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 提供欄位轉換支援：將來源資料依照 Transitions 規則轉為 Map 或寫入 POJO。
 */
@AllArgsConstructor
public class TransformationSupport {

    private final ExpressionEvaluators evaluators;

    private final TransformationExceptionHandlers transformationExceptionHandlers;

    /**
     * 將轉換結果寫入 Map&lt;String, Object&gt;
     * @param source 來源物件
     * @param transitions 轉換規則
     * @return 回傳轉換結果
     * @param <T> 資料來源類型
     */
    public <T> Map<String, Object> transform(T source, Transitions transitions) {
        return transitions.stream().collect(Collectors.toMap(
                TransformMapping::writerExpr,
                mapping -> read(source, mapping)
        ));
    }

    private <T> Object read(T source, TransformMapping mapping) {
        return evaluators.readerOf(mapping.readerExpr()).apply(source);
    }

    /**
     * 將轉換結果寫入目標物件（POJO）
     */
    public <T, R> R transform(
            T source, Transitions transitions, Supplier<R> supplier) {
        R target = supplier.get();
        transitions.stream().forEach(mapping -> {
            Object value = evaluators.readerOf(mapping.readerExpr()).apply(source);
            write(mapping, target, value);
        });
        return target;
    }

    private <R> void write(
            TransformMapping mapping, R target, Object value) {
        evaluators.supplyWriter(mapping.writerExpr(), transformationExceptionHandlers.getExceptionGenerator())
                .accept(target, value);
    }

}
