package org.dotspace.oofp.utils.functional.monad.text;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.dotspace.oofp.utils.functional.monad.Maybe;

import java.util.function.Supplier;

@UtilityClass
public class StringMonads {

    public String getString(String text) {
        return maybeStringContent(text)
                .orElse(StringUtils.EMPTY);
    }

    public Maybe<String> maybeStringContent(String text) {
        return Maybe.given(text)
                .filter(StringUtils::isNotBlank);
    }

    public String getString(String text, Supplier<String> defaultValueSupplier) {
        return maybeStringContent(text)
                .orElseGet(defaultValueSupplier);
    }

    public String getTrimmedString(String text) {
        return maybeStringContent(text)
                .map(String::trim)
                .orElse(StringUtils.EMPTY);
    }
}
