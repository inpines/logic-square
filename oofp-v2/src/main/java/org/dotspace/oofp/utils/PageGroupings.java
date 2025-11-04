package org.dotspace.oofp.utils;

import java.util.List;
import java.util.function.Function;

public class PageGroupings {

    public <T> PageGrouping<T> grouping(List<T> data, Function<T, String> classifier) {
        return PageGrouping.groupingBy(data, classifier);
    }

}
