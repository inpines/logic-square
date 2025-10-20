package org.dotspace.oofp.utils.violation.joinable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import org.dotspace.oofp.utils.violation.Joinable;

@Getter
@AllArgsConstructor(staticName = "of")
public class JoinableMessage implements Joinable<JoinableMessage> {

    private String message;

    @Override
    public JoinableMessage join(JoinableMessage other) {
        return Maybe.given(message)
                .fold(s -> JoinableMessage.of(s.concat("\n").concat(other.message)), () -> other);
    }
}
