package org.dotspace.oofp.utils.eip;

import org.dotspace.oofp.enumeration.eip.CoreRouteKey;
import org.dotspace.oofp.utils.dsl.BehaviorStep;
import org.dotspace.oofp.model.dto.behaviorstep.Violations;
import org.dotspace.oofp.utils.eip.routekey.CoreRoute;
import org.dotspace.oofp.utils.eip.routekey.ExtensionRoute;
import org.dotspace.oofp.utils.eip.routekey.RouteKey;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import lombok.RequiredArgsConstructor;

import java.util.EnumMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

@RequiredArgsConstructor
public final class RouteTable<T> implements Function<RouteKey, BehaviorStep<T>> {

    private final EnumMap<CoreRouteKey, BehaviorStep<T>> core;
    private final Map<String, BehaviorStep<T>> ext;

    @Override
    public BehaviorStep<T> apply(RouteKey key) {
        if (key instanceof CoreRoute cr) {
            return require(core.get(cr.key()),"core route missing: " + cr.key());
        }

        if (key instanceof ExtensionRoute er) {
            return require(ext.get(er.name()), "extension route missing: " + er.name());
        }

        return sc -> Validation.invalid(Violations.violate(
                "route-table.error", "invalid routeKey!!"));
    }

    private static <X> X require(X x, String msg) {
        if (x == null) {
            throw new NoSuchElementException(msg);
        }
        return x;
    }
}
