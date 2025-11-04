package org.dotspace.oofp.utils;

import lombok.Getter;

import java.util.*;

public class Pagination<T> {
    static final Set<String> paginationIds = Collections.synchronizedSet(new HashSet<>());

    @Getter
    private String paginationId;

    private final List<T> data;
    private final int limit;

    protected static <T> Pagination<T> paginate(List<T> data, int limit) {
        return new Pagination<>(data, limit);
    }

    private Pagination(List<T> data, int limit) {
        super();

        synchronized (paginationIds) {
            do {
                this.paginationId = UUID.randomUUID().toString();
            } while (paginationIds.contains(this.paginationId));
            paginationIds.add(this.paginationId);
        }

        this.data = data;
        this.limit = limit;
    }

    public List<T> of(int n) {
        return data.stream()
                .skip((long) (n - 1) * limit)
                .limit(limit)
                .toList();
    }

}
