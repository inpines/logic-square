package org.dotspace.oofp.utils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PageGrouping<T> {

    private final Map<String, List<T>> data;

    protected static <T> PageGrouping<T> groupingBy(
            List<T> data, Function<T, String> classifier) {
        return new PageGrouping<>(data, classifier);
    }

    protected PageGrouping(List<T> lineItems, Function<T, String> classifier) {
        if (null == lineItems) {
            this.data = Collections.emptyMap();
            return;
        }

        this.data = lineItems.stream()
                .collect(Collectors.groupingBy(classifier));
    }

    public Set<String> getGroupingKeys() {
        return data.keySet();
    }

    public List<String> getGroupingKeys(Comparator<String> comparator) {
        return data.keySet().stream()
                .sorted(comparator)
                .toList();
    }

    public List<T> of(String key) {
        return data.get(key);
    }

}
