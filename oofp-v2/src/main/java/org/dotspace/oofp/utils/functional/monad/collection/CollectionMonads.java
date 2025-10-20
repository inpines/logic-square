package org.dotspace.oofp.utils.functional.monad.collection;

import lombok.experimental.UtilityClass;
import org.dotspace.oofp.utils.functional.monad.Maybe;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@UtilityClass
public class CollectionMonads {

    public <K, V> V getValue(K key, Map<K, V> map) {
        return maybeMapValue(key, map)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .orElse(null);
    }

    private <K, V> Maybe<Optional<V>> maybeMapValue(K key, Map<K, V> map) {
        Maybe<K> found = Maybe.given(map)
                .flatMap(m -> Maybe.given(key)
                        .filter(m::containsKey));
        if (found.isEmpty()) {
            return Maybe.empty();
        }
        return found.map(map::get)
                .map(Optional::of)
                .or(Maybe.given(Optional.empty()));
    }

    public <K, V> V getValue(K key, Map<K, V> map, Supplier<V> defaultValueSupplier) {
        return maybeMapValue(key, map)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .orElseGet(defaultValueSupplier);
    }

    public <K, V> List<V> listValues(List<K> keys, Map<K, V> map) {
        return keys.stream()
                .flatMap(k -> maybeMapValue(k, map).stream())
                .toList()
                .stream()
                .map(opt -> opt.orElse(null))
                .toList();
    }

}
