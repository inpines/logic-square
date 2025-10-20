package org.dotspace.oofp.utils.functional.predicate;

import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.util.List;

@UtilityClass
public class ObjectPredicates {

    public boolean isNotEmpty(@Nullable Object object) {
        return !ObjectUtils.isEmpty(object);
    }

    public boolean isNotEmpty(@Nullable Object[] array) {
        return array != null && array.length != 0;
    }

    public boolean isExistElements(@Nullable List<?> list) {
        return list != null && !list.isEmpty();
    }

}
