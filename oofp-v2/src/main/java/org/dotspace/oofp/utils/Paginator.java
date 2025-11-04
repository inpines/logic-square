package org.dotspace.oofp.utils;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "with")
public class Paginator {

    private int limit;

}
