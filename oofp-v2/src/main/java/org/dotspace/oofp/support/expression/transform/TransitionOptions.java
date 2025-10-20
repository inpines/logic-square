package org.dotspace.oofp.support.expression.transform;

import lombok.Builder;
import lombok.Getter;

/**
 * Options for transitions in a transformation process.
 * This class allows for configuration of transition behaviors, such as whether to overwrite existing data.
 */
@Builder
public class TransitionOptions {

    @Getter
    @Builder.Default
    private boolean overwriting = false;

    public static TransitionOptions defaultOptions() {
        return TransitionOptions.builder().build();
    }

}
