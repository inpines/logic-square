package org.dotspace.oofp.support.dsl.conditional;

public class ConditionalDsl {

    public <T> ConditionalRules<T> rules() {
       return new ConditionalRules<>();
    }
}
