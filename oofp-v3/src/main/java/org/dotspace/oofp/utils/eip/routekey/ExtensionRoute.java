package org.dotspace.oofp.utils.eip.routekey;

import org.dotspace.oofp.utils.functional.monad.Maybe;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.Strings;

public record ExtensionRoute(String name) implements RouteKey {

    public ExtensionRoute {
        name = normalize(name);
    }

    private static String normalize(String s) {
        return Maybe.given(s)
                .filter(StringUtils::isNotBlank)
                .map(Strings::toLowerCase)
                .orElseThrow(() -> new IllegalArgumentException("ExtensionRoute.name is blank"));
    }

}
