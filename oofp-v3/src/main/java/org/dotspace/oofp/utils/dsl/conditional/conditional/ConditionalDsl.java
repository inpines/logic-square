package org.dotspace.oofp.utils.dsl.conditional.conditional;

public class ConditionalDsl {

    public <T> ConditionalRules<T> rules() {
       return new ConditionalRules<>();
    }
}
