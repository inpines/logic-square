package org.dotspace.oofp.utils.functional.monad.either;

import org.dotspace.oofp.utils.functional.monad.Task;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class TaskExtensions {
    public <T> Task<Try<T>> asEither(Task<T> task) {
        return Task.actionAsync(() -> task.run()
                .handle((value, ex) -> {
                    if (ex != null) return Try.failed(ex);
                    else return Try.success(value);
                }));
    }
}
