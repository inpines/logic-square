package org.dotspace.oofp.utils.functional.monad.date;

import org.dotspace.oofp.utils.functional.monad.Maybe;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.function.Supplier;

@UtilityClass
public class DateMonads {

    private static final String DATE_TIME_MILLI = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String DATE_TIME_HUNDRED_NANA = "yyyy-MM-dd HH:mm:ss.SSSSSSS";

    private static final DateTimeFormatter FLEX_LDT =
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    // 年
                    .appendValue(ChronoField.YEAR, 4)
                    // 可選：月
                    .optionalStart().appendLiteral('-').appendValue(ChronoField.MONTH_OF_YEAR, 2).optionalEnd()
                    // 可選：日
                    .optionalStart().appendLiteral('-').appendValue(ChronoField.DAY_OF_MONTH, 2).optionalEnd()
                    // 可選：時間（T 或空白當分隔都可）
                    .optionalStart()
                    .optionalStart().appendLiteral('T').optionalEnd()
                    .optionalStart().appendLiteral(' ').optionalEnd()
                    .appendValue(ChronoField.HOUR_OF_DAY, 2)
                    .optionalStart().appendLiteral(':').appendValue(ChronoField.MINUTE_OF_HOUR, 2).optionalEnd()
                    .optionalStart().appendLiteral(':').appendValue(ChronoField.SECOND_OF_MINUTE, 2).optionalEnd()
                    .optionalStart().appendFraction(
                            ChronoField.NANO_OF_SECOND, 1, 9, true).optionalEnd()
                    .optionalEnd()
                    // 關鍵：補預設值
                    .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                    .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
                    .toFormatter()
                    .withResolverStyle(ResolverStyle.SMART);

    public LocalDateTime getLocalDateTime(String dateTimeText) {

        return maybeLocalDateTime(dateTimeText)
                .orElse(null);

    }

    private Maybe<LocalDateTime> maybeLocalDateTime(String dateTimeText, String pattern) {
        return Maybe.given(dateTimeText)
                .map(ldt -> {
                    String dp = pattern.substring(0, ldt.length());
                    return parseLocalDateTime(ldt, dp);
                });
    }

    private static LocalDateTime parseLocalDateTime(String ldt, String datePattern) {
        try {
            return LocalDateTime.parse(ldt, DateTimeFormatter.ofPattern(datePattern));
        } catch (Exception ex) {
            return null;
        }
    }

    private static Maybe<LocalDateTime> maybeLocalDateTime(String text) {
        return Maybe.given(text)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(DateMonads::parseLocalDateTime);
    }

    private static LocalDateTime parseLocalDateTime(String ldt) {
        try {
            return LocalDateTime.parse(ldt, DateMonads.FLEX_LDT);
        } catch (Exception ex) {
            return null;
        }
    }


    public LocalDateTime getLocalDateTime(String dateTimeText, Supplier<LocalDateTime> defaultDateTimeSupplier) {

        return maybeLocalDateTime(dateTimeText)
                .orElseGet(defaultDateTimeSupplier);

    }

    public LocalDateTime getLocalDateTimeWithMilli(String dateTimeText) {

        return maybeLocalDateTime(dateTimeText, DATE_TIME_MILLI)
                .orElse(null);

    }

    public LocalDateTime getLocalDateTimeWithHundredNanoSecond(String dateTimeText) {

        return maybeLocalDateTime(dateTimeText, DATE_TIME_HUNDRED_NANA)
                .orElse(null);

    }

    public LocalDateTime getLocalDate(String dateTimeText) {

        return maybeLocalDateTime(dateTimeText)
                .orElse(null);

    }

}
