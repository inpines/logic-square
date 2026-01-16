package org.dotspace.oofp.utils.functional.monad.number;

import org.dotspace.oofp.utils.functional.monad.text.StringMonads;
import lombok.experimental.UtilityClass;

@UtilityClass
public class NumberMonads {

    public Long getLong(String value) {
        try {
            return Long.parseLong(StringMonads.getString(value));
        }catch(NumberFormatException e) {
            return 0L;
        }
    }

    public Integer getInteger(String value) {
        try {
            return Integer.parseInt(StringMonads.getString(value));
        }catch(NumberFormatException e) {
            return 0;
        }
    }

    public Double getDouble(String value) {
        try {
            return Double.parseDouble(StringMonads.getString(value));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
