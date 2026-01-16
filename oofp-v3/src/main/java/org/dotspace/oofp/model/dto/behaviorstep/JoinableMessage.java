package org.dotspace.oofp.model.dto.behaviorstep;

import org.dotspace.oofp.utils.dsl.Joinable;
import org.dotspace.oofp.utils.functional.monad.Maybe;

import lombok.AllArgsConstructor;
import lombok.Getter;

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
