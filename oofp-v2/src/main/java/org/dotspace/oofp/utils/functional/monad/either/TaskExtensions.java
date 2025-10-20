package org.dotspace.oofp.utils.functional.monad.either;

import lombok.experimental.UtilityClass;
import org.dotspace.oofp.utils.functional.monad.Task;

@UtilityClass
public class TaskExtensions {
    public <T> Task<Try<T>> asEither(Task<T> task) {
        return Task.actionAsync(() -> task.run()
                .handle((value, ex) -> {
                    if (ex != null) return Try.failed(ex);
                    else return Try.success(value);
                }));
    }
}
