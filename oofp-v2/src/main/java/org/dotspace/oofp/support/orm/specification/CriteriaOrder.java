package org.dotspace.oofp.support.orm.specification;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE, staticName = "of")
public class CriteriaOrder {

    private String type;
    private String name;

    public static CriteriaOrder asc(String name) {
        return of("ascending", name);
    }

    public static CriteriaOrder desc(String name) {
        return of("descending", name);
    }

}
