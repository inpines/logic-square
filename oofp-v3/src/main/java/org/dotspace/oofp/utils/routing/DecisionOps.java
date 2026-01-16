package org.dotspace.oofp.utils.routing;

import lombok.experimental.UtilityClass;

import java.util.function.*;

@UtilityClass
public class DecisionOps {

    // ========= Core (通用) =========

    public <T, C, R> BiFunction<T, C, R> decide(
            BiPredicate<T, C> decider,
            BiFunction<T, C, R> local,
            BiFunction<T, C, R> remote) {
        return (t, c) -> decider.test(t, c) ? remote.apply(t, c) : local.apply(t, c);
    }

    public <T, C, R> BiFunction<T, C, R> decide(
            BiPredicate<T, C> decider,
            BiFunction<T, C, R> local,
            BiFunction<T, C, R> remote,
            boolean fallbackToLocalOnRemoteFailure) {
        if (!fallbackToLocalOnRemoteFailure) return decide(decider, local, remote);

        return (t, c) -> {
            if (!decider.test(t, c)) return local.apply(t, c);
            try { return remote.apply(t, c); }
            catch (RuntimeException ex) { return local.apply(t, c); }
        };
    }

    // ========= Overload: decider 只看 T =========

    public <T, C, R> BiFunction<T, C, R> decide(
            Predicate<T> decider,
            BiFunction<T, C, R> local,
            BiFunction<T, C, R> remote) {
        return decide((t, c) -> decider.test(t), local, remote);
    }

    public <T, C, R> BiFunction<T, C, R> decide(
            Predicate<T> decider,
            BiFunction<T, C, R> local,
            BiFunction<T, C, R> remote,
            boolean fallbackToLocalOnRemoteFailure) {
        return decide((t, c) -> decider.test(t), local, remote, fallbackToLocalOnRemoteFailure);
    }

    // ========= Overload: decider 只看 C =========

    public <T, C, R> BiFunction<T, C, R> routeByContext(
            Predicate<C> decider,
            BiFunction<T, C, R> local,
            BiFunction<T, C, R> remote) {
        return decide((t, c) -> decider.test(c), local, remote);
    }

    public <T, C, R> BiFunction<T, C, R> routeByContext(
            Predicate<C> decider,
            BiFunction<T, C, R> local,
            BiFunction<T, C, R> remote,
            boolean fallbackToLocalOnRemoteFailure) {
        return decide((t, c) -> decider.test(c), local, remote, fallbackToLocalOnRemoteFailure);
    }
}
