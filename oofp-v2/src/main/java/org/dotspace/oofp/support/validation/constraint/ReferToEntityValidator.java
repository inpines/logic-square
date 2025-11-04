package org.dotspace.oofp.support.validation.constraint;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import org.dotspace.oofp.support.validation.annotation.ReferToEntity;
import org.dotspace.oofp.utils.builder.GeneralBuilders;
import org.dotspace.oofp.utils.builder.GeneralBuildingWriters;
import org.dotspace.oofp.utils.functional.BiConsumers;
import org.dotspace.oofp.utils.functional.Casters;
import org.dotspace.oofp.utils.functional.Suppliers;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class ReferToEntityValidator implements ConstraintValidator<ReferToEntity, Object> {

    private Class<?> entityClazz;

    private String keyFieldName;

    private boolean isPresent;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void initialize(ReferToEntity entityExists) {
        this.entityClazz = entityExists.entityClazz();
        this.keyFieldName = entityExists.keyFieldName();
        this.isPresent = entityExists.isPresent();
    }

    @Override
    public boolean isValid(
            Object value, ConstraintValidatorContext context) {

        Object entity = readEntity(entityClazz, value);

        return Optional.ofNullable(entity).isPresent() == isPresent;
    }

    private <T> T readEntity(Class<T> clazz, Object value) {
        Map<String, Object> params = GeneralBuilders
                .of(Suppliers.newHashMap(String.class, Object.class))
                .with(GeneralBuildingWriters.set(
                        BiConsumers.forMapOf(keyFieldName), value))
                .build();

        List<T> entities = Optional.ofNullable(keyFieldName)
                .filter(kfn -> !StringUtils.isBlank(kfn))
                .map(kfn -> Optional.ofNullable(
                                findByKeyFieldName(clazz, params, kfn))
                        .orElse(Collections.<T>emptyList()))
                .orElse(findById(clazz, value));

        return entities.isEmpty() ? null : entities
                .stream()
                .findFirst()
                .orElse(null);
    }

    private <T> List<T> findByKeyFieldName(Class<T> clazz, Map<String, Object> params,
                                           String kfn) {
        return find(new StringBuilder()
                .append(String.format(
                        "select e from %s", clazz.getSimpleName()))
                .append(String.format(" where %s = :%s", kfn, kfn))
                .toString(), params);
    }

    private <T> List<T> findById(Class<T> clazz, Object value) {
        return Optional.ofNullable(entityManager.find(clazz, value))
                .map(r -> Stream.<T>builder()
                        .add(r)
                        .build()
                        .toList())
                .orElse(Collections.<T>emptyList());
    }

    private <T> List<T> find(String qlString, Map<String, Object> params) {
        Query query = entityManager.createQuery(qlString);
        params.forEach(query::setParameter);

        return Optional.ofNullable(query.getResultList())
                .map(Casters.<T>forList())
                .orElse(Collections.emptyList());
    }
}
