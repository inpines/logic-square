package org.dotspace.oofp.support.validation.constraint;

import gov.acs.support.validation.annotation.BizKeyField;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.dotspace.oofp.support.expression.ExpressionEvaluations;
import org.dotspace.oofp.support.validation.annotation.BizKey;
import org.dotspace.oofp.utils.Reflections;
import org.dotspace.oofp.utils.builder.GeneralBuilders;
import org.dotspace.oofp.utils.builder.GeneralBuildingWriters;
import org.dotspace.oofp.utils.functional.BiConsumers;
import org.dotspace.oofp.utils.functional.Suppliers;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BizKeyValidator implements ConstraintValidator<BizKey, Object> {

    private Class<?> entityClazz;

    private String criteria;

    private String isPresentExpression;

    @PersistenceContext
    private EntityManager entityManager;

    private final ExpressionEvaluations expressionEvaluations;

    @Autowired
    public BizKeyValidator(ExpressionEvaluations expressionEvaluations) {
        this.expressionEvaluations = expressionEvaluations;
    }

    @Override
    public void initialize(BizKey bizKey) {
        this.entityClazz = bizKey.entityClazz();
        this.criteria = bizKey.criteria();
        this.isPresentExpression = bizKey.isPresentExpression();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {

        return (null != readEntity(entityClazz, value)) == Optional
                .ofNullable(expressionEvaluations.parse(isPresentExpression))
                .map(eval -> eval.<Boolean>getValue(value))
                .orElse(true);

    }

    private <T> T readEntity(Class<T> clazz, Object value) {

        if (null == value || null == clazz) {
            return null;
        }

        Map<String, Object> params = Optional.of(value)
                .filter(v -> v.getClass().isPrimitive()
                        || v.getClass().isAssignableFrom(CharSequence.class))
                .map(v -> GeneralBuilders
                        .of(Suppliers.newHashMap(String.class, Object.class))
                        .with(GeneralBuildingWriters.set(
                                BiConsumers.forMapOf("keyValue"), value))
                        .build())
                .orElseGet(() -> {
                    List<Field> fields = Reflections.getFields(value.getClass()).stream()
                            .filter(f -> f.getAnnotation(BizKeyField.class) != null)
                            .toList();
                    return GeneralBuilders
                            .of(Suppliers.newHashMap(String.class, Object.class))
                            .with(GeneralBuildingWriters.setForEach(
                                    (m, f) -> m.put(
                                            f.getName(), getFieldValue(
                                                    value, f)), fields))
                            .build();
                });

        Query query = entityManager.createQuery(String.format("select e from %s e where %s",
                clazz.getSimpleName(), criteria));
        params.forEach((n, v) -> setQueryParameter(n, v, query));
        List<?> entities = query.getResultList();

        return entities.stream()
                .map(clazz::cast)
                .findFirst()
                .orElse(null);
    }

    private Object getFieldValue(Object target, Field field) {
        return Optional.ofNullable(expressionEvaluations.parse(field.getName()))
                .map(ev -> ev.getValue(target))
                .orElse(null);
    }

    private void setQueryParameter(String n, Object v, Query query) {
        query.setParameter(n, v);
    }

}
