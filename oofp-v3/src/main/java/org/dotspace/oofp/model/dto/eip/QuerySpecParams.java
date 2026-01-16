package org.dotspace.oofp.model.dto.eip;

import org.dotspace.oofp.model.dto.behaviorstep.Violations;
import org.dotspace.oofp.utils.eip.AttrKey;
import org.dotspace.oofp.utils.functional.Casters;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import lombok.NonNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class QuerySpecParams {

    private final Map<String, Object> raw;

    private QuerySpecParams(Map<String, Object> raw) {
        this.raw = Collections.unmodifiableMap(new LinkedHashMap<>(raw == null ? Map.of() : raw));
    }

    public static QuerySpecParams of(Map<String, Object> raw) {
        return new QuerySpecParams(raw);
    }

    public Map<String, Object> raw() {
        return raw;
    }

    public <V> Maybe<V> get(AttrKey<V> key) {
        Object value = raw.get(key.name());   // 這裡用 AttrKey 的 name 當 map key

        return cast(key, value);
    }

    private <V> Maybe<V> cast(AttrKey<V> key, Object value) {
        return Maybe.given(value)
                .map(Casters.cast(key.typeRef()));
    }

    public <V> Validation<Violations, V> require(@NonNull AttrKey<V> key) {
        return Maybe.given(raw.get(key.name()))
                .toValidation(Violations.violate(
                        "query-params-require.missing",
                        "required query spec param is missing: " + key.name()))
                .flatMap(o -> cast(key, o)
                        .toValidation(Violations.violate("query-params-require.invalid",
                                "query spec param is invalid: " + key.name())
                        )
                );
    }

    public <V> QuerySpecParams with(AttrKey<V> key, V value) {
        var copy = new java.util.LinkedHashMap<>(raw);
        copy.put(key.name(), value);
        return new QuerySpecParams(copy);
    }

}
