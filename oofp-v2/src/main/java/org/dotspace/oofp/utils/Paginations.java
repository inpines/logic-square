package org.dotspace.oofp.utils;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Paginations {

    public <T> Pagination<T> paginate(List<T> data, Paginator paginator) {
        return Pagination.paginate(data, paginator.getLimit());
    }

}
