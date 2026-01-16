package org.dotspace.oofp.support.msg;

import org.dotspace.oofp.support.expression.ExpressionEvaluations;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@AllArgsConstructor
@Component
public class MessageSupport {

    public static final String NULL = "<null>";
    private ExpressionEvaluations expressionEvaluations;

    /**
     * 取代字串中 ${...} 表達式，從 model 中取值
     */
    public <T> String getMessageUsingProperties(@NonNull String msgFormat, T model) {
        Matcher m = matchPattern(msgFormat, "\\$\\{([^{$}]+)}");

        return processMatcher(msgFormat, model, m, this::evaluate);
    }

    /**
     * 取代 ${...} 再取代 #{...}，後者從 Map 參數中取值
     */
    public <T> String getMessageUsingProperties(@NonNull String msgFormat, T model, Map<String, Object> parameters) {
        return Optional.of(getMessageUsingProperties(msgFormat, model))
                .map(s -> {
                    Matcher m = matchPattern(s, "#\\{([^{#}]+)}");

                    return processMatcher(s, model, m, (t, name) -> evaluateParameterValue(parameters, name));
                })
                .orElse(StringUtils.EMPTY);
    }

    private <T> String processMatcher(
            @NonNull String msgFormat, T model, Matcher m, BiFunction<T, String, Optional<String>> reader) {
        StringBuilder msgBuilder = new StringBuilder();
        int last = 0;
        while (m.find()) {
            String found = m.group(1);
            Optional<String> content = reader.apply(model, found);
            msgBuilder.append(msgFormat, last, m.start());
            msgBuilder.append(content.orElse(NULL));
            last = m.end();
        }
        msgBuilder.append(msgFormat, last, msgFormat.length());
        return msgBuilder.toString();
    }

    private <T> Optional<String> evaluate(T model, String propWithDefault) {
        String[] parts = propWithDefault.split(":", 2);
        String prop = parts[0];
        String defaultValue = parts.length > 1 ? parts[1] : NULL;

        return Optional.ofNullable(prop)
                .map(expressionEvaluations::evaluate)
                .map(ev -> ev.getValue(model))
                .map(Objects::toString)
                .or(() -> Optional.of(defaultValue));
    }

    private Optional<String> evaluateParameterValue(Map<String, Object> parameters, String nameWithDefault) {
        String[] parts = nameWithDefault.split(":", 2);
        String key = parts[0];
        String defaultValue = parts.length > 1 ? parts[1] : NULL;
        return Optional.ofNullable(parameters.get(key))
                .map(Objects::toString)
                .or(() -> Optional.of(defaultValue));
    }

    private Matcher matchPattern(String msgFormat, String regex) {
        Pattern p = Pattern.compile(regex);
        return p.matcher(msgFormat);
    }

    /**
     * 取得隱碼字串
     *
     * @param str 需要隱碼的字串
     * @param mask 隱碼符號
     * @return 隱碼後的字串
     */
    public String getObfuscatedString(String str, String mask) {
        if (StringUtils.isBlank(str) || null == mask) {
            return str;
        }

        int len = str.length();

        if (len <= 2) {
            return mask.repeat(len);
        }

        int half = len / 2;
        List<Pair<Integer, int[]>> parts = List.of(
                Pair.of(1, new int[] {0, half/2}),
                Pair.of(0, new int[] {half - half/2, half + half/2 + 1}),
                Pair.of(1, new int[] {half + half/2, len}));

        return parts.stream()
                .map(e -> (e.getLeft() != 0 ? str : StringUtils.repeat(mask, len))
                        .substring(e.getRight()[0], e.getRight()[1]))
                .collect(Collectors.joining());
    }

}
