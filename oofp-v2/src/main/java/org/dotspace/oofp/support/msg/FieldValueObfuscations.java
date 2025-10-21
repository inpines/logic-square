package org.dotspace.oofp.support.msg;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 欄位值遮蔽處理
 * <p>
 * 這個類別提供了將欄位值進行遮蔽的功能，特別是針對結合欄位解析與遮蔽的處理。
 * </p>
 */
@AllArgsConstructor
public class FieldValueObfuscations {

    private static final String VALUE_ONLY_PREFIX = "\u0000VAL#";

    private MessageSupport messageSupport;

    /**
     * 封裝「像 value 一樣」的遮罩流程（集合/巢狀/鍵值 or 原地遮罩）
     */
    private String maskValueLike(String value, String mask) {
        return maybeNestedObjectContent(value, mask)
                .or(() -> maybeKeyValuesContent(value, mask))
                .or(() -> maybeCollectionContent(value, mask))
                .orElseGet(() -> isNoData(value) ? messageSupport.getObfuscatedString(value, mask) : value);
    }

    /**
     * 入口：先判斷是否為「純 value」（頂層無 '='）
     */
    public String maskFields(String input, String mask) {
        if (input == null || input.isBlank()) {
            return input;
        }

        // 1) 先處理 k=v 清單（頂層存在 '='）
        if (hasTopLevelEquals(input)) {
            return TopLevelContext.splitTopLevel(input).stream()
                    .map(String::trim)                    // 每個 segment：可能是 "k=v" 或 value-only
                    .filter(s -> !s.isEmpty())
                    .map(seg -> rewriteKvSegment(seg, mask))
                    .collect(Collectors.joining(", "));
        }

        // 2) 沒有頂層 '='：把整段視為「value」
        return maybeAutoContent(input, mask) // 自動偵測 {} / [] / ()
                .orElseGet(
                        () -> isNoData(input) ?
                                messageSupport.getObfuscatedString(input, mask) : input
                );
    }

    /** 把一個 "k=v" 或 value-only segment 轉成遮罩後的字串；保留 value 兩側原空白 */
    private String rewriteKvSegment(String seg, String mask) {
        int eq = TopLevelContext.indexOfTopLevel(seg, '=');
        if (eq < 0) {
            // value-only：遞迴處理 value
            String[] ws = peelWhitespace(seg);
            return ws[0] + maskValueLike(ws[1], mask) + ws[2];
        }
        String key = seg.substring(0, eq).trim();
        String[] ws = peelWhitespace(seg.substring(eq + 1));
        String masked = maskValueLike(ws[1], mask);
        return key + "=" + ws[0] + masked + ws[2];
    }

    /** 嘗試自動判斷 value 的外層括號與語義，並回傳遮罩後結果。 */
    private Optional<String> maybeAutoContent(String value, String mask) {
        ParsedOpen po = detectOuter(value);
        if (po.isNotFound()) return Optional.empty();
        String maskedInner = po.objectLike()
                ? maskObjectInner(po.inner(), mask)
                : maskArrayInner(po.inner(), mask);
        return Optional.of(po.rebuild(maskedInner));
    }

    /** 針對 [...] / (...) 內文：保留原逗號與空白，逐元素遞迴 maskFields。 */
    private String maskArrayInner(String inner, String mask) {
        StringBuilder out = new StringBuilder();
        int i = 0;
        int n = inner.length();
        while (i < n) {
            int end = indexOfTopLevelFrom(inner, i, ',');
            if (end < 0) end = n;
            String seg = inner.substring(i, end);
            String[] ws = peelWhitespace(seg);          // [leftWs, core, rightWs]
            String maskedCore = maskFields(ws[1], mask); // 遞迴
            out.append(ws[0]).append(maskedCore).append(ws[2]);
            if (end < n) { out.append(','); i = end + 1; } else { break; }
        }
        return out.toString();
    }

    /** 針對物件內文（頂層含 '='）：保留原 key、逗號與空白，逐 value 遞迴 maskFields。 */
    private String maskObjectInner(String inner, String mask) {
        StringBuilder out = new StringBuilder();
        int i = 0;
        int n = inner.length();
        while (i < n) {
            int eq = indexOfTopLevelFrom(inner, i, '=');
            if (eq < 0) {
                // 可能存在 value-only 片段（無 '='），也做遞迴處理並保留空白
                String tail = inner.substring(i);
                String[] ws = peelWhitespace(tail);
                String maskedCore = maskFields(ws[1], mask);
                out.append(ws[0]).append(maskedCore).append(ws[2]);
                break;
            }
            // 保留從 i 到 '='（含），完整 key 與等號前的格式
            out.append(inner, i, eq + 1);

            int end = indexOfTopLevelFrom(inner, eq + 1, ',');
            if (end < 0) end = n;

            String rawVal = inner.substring(eq + 1, end);
            String[] ws = peelWhitespace(rawVal);
            String maskedCore = maskFields(ws[1], mask);
            out.append(ws[0]).append(maskedCore).append(ws[2]);

            // 不主動吃逗號；交由下輪的 `out.append(inner, i, eq+1)` 保留原格式
            i = end;
            if (i < n) { out.append(','); i++; }
        }
        return out.toString();
    }

    private boolean isNoData(String value) {
        return !"null".equals(value)
                && StringUtils.isNotBlank(value)
                && !"[]".equals(value)
                && !"{}".equals(value);
    }

    /**
     * 以「頂層逗號」切段，再以「頂層第一個 '='」切 key/value；無 '=' 視為 value-only
     */
    private Map<String, String> parseFieldMap(String input) {
        List<String> fields = TopLevelContext.splitTopLevel(input);
        Map<String, String> map = new LinkedHashMap<>();
        int seq = 0;

        for (String raw : fields) {
            String f = raw.trim();
            if (f.isEmpty()) continue;

            int eq = TopLevelContext.indexOfTopLevel(f, '=');
            if (eq < 0) {
                map.put(VALUE_ONLY_PREFIX + (seq++), f);
            } else {
                String key = f.substring(0, eq).trim();
                String val = f.substring(eq + 1).trim();
                map.put(key, val);
            }
        }
        return map;
    }

    /**
     * 集合處理：去外層 []，以頂層逗號分割，元素 trim 後遞迴遮罩，再組回 []
     */
    private Optional<String> maybeCollectionContent(String value, String mask) {
        if (!(value.startsWith("[") && value.endsWith("]"))) return Optional.empty();
        String inner = value.substring(1, value.length() - 1);
        String masked = TopLevelContext.splitTopLevel(inner).stream()
                .map(String::trim)
                .map(v -> maskFields(v, mask))
                .collect(Collectors.joining(", "));
        return Optional.of("[" + masked + "]");
    }

    private Optional<String> maybeNestedObjectContent(String value, String mask) {
        ParsedOpen po = detectOuter(value);
        if (po.isNotFound()) {
            return Optional.empty();
        }

        int left = TopLevelContext.indexOfTopLevel(value, po.open());
        if (left <= 0) {
            return Optional.empty();
        }

        int right = findMatchingRightBraceAtTopLevel(value, left, po.open(), po.close());
        if (right < 0) {
            return Optional.empty();
        }

        String prefix = value.substring(0, left).trim();           // 可能是類名或空字串
        String inner  = value.substring(left + 1, right);          // a=1, b=[x,y]
        String tail   = value.substring(right + 1);      // 罕見情況保留收尾

        Map<String, String> map = parseFieldMap(inner);
        String masked = map.entrySet().stream()
                .map(e -> e.getKey().startsWith(VALUE_ONLY_PREFIX)
                        ? maskValueLike(e.getValue(), mask)
                        : e.getKey() + "=" + maskValueLike(e.getValue(), mask))
                .collect(Collectors.joining(", "));

        String head = prefix.isEmpty() ? "" : prefix;
        return Optional.of(head + "{" + masked + "}" + tail);
    }

    static int findMatchingRightBraceAtTopLevel(String s, int left, char open, char close) {
        TopLevelContext ctx = new TopLevelContext();
        for (int k = 0; k < left; k++) ctx.feed(s.charAt(k)); // 餵到 left 之前
        int depth = 1; // s[left] 已是第一層 open
        for (int i = left + 1; i < s.length(); i++) {
            char ch = s.charAt(i);
            boolean topOther = ctx.isTopLevelIgnoring(open);
            if (topOther && ch == open) {
                depth++;
            } else if (topOther && ch == close) {
                depth--;
                if (depth == 0) return i;
            }
            ctx.feed(ch);
        }
        return -1;
    }

    // 找出頂層第一個屬於 targets 的開括號位置
    static int indexOfTopLevelAny(String s, char... opens) {
        Set<Character> set = new HashSet<>();
        for (char o : opens) set.add(o);
        TopLevelContext ctx = new TopLevelContext();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ctx.isTopLevel() && set.contains(ch)) return i;
            ctx.feed(ch);
        }
        return -1;
    }

    // 解析外層：決定 open/close 與語義
    static ParsedOpen detectOuter(String s) {
        int i = indexOfTopLevelAny(s, '{', '[', '(');
        if (i < 0) return ParsedOpen.none();
        char open = s.charAt(i);
        char close = Map.of('{', '}', '[', ']', '(', ')')
                .get(open);
        int j = findMatchingRightBraceAtTopLevel(s, i, open, close);
        if (j < 0) return ParsedOpen.none();
        String prefix = s.substring(0, i).trim();
        String inner  = s.substring(i + 1, j);
        boolean objectLike = /*(open == '{') || */hasTopLevelEquals(inner);
        return new ParsedOpen(prefix, open, close, inner, objectLike);
    }

    static boolean hasTopLevelEquals(String inner) {
        return TopLevelContext.indexOfTopLevel(inner, '=') >= 0;
    }

    private Optional<String> maybeKeyValuesContent(String value, String mask) {
        ParsedOpen po = detectOuter(value);
        if (po.isNotFound()) {
            return Optional.empty();
        }

        if (!(value.startsWith(String.valueOf(po.open()))
                && value.endsWith(String.valueOf(po.close())))) {
            return Optional.empty();
        }

        String inner = value.substring(1, value.length() - 1);
        if (!hasTopLevelEquals(inner)) {
            return Optional.empty();
        }

        StringBuilder out = new StringBuilder(String.valueOf(po.open()));

        int i = 0;
        int n = inner.length();
        while (i < n) {
            // 找頂層 '='（key/value 分隔）
            int eq = indexOfTopLevelFrom(inner, i, '=');
            if (eq < 0) { // 後面沒有成對的 key=value，原樣附上並結束
                out.append(inner.substring(i));
                break;
            }

            // 保留從 i 到 '='（含）之間的原樣（含 key 與任何空白）
            out.append(inner, i, eq + 1);

            // 值的結束：下一個頂層 ','，若無則到結尾
            int end = indexOfTopLevelFrom(inner, eq + 1, ',');
            if (end < 0) end = n;

            // 取值片段，去掉左右邊空白做遮罩，再把空白還回去
            String rawVal = inner.substring(eq + 1, end);
            String[] parts = peelWhitespace(rawVal); // [leftWs, core, rightWs]
            String maskedCore = maskFields(parts[1], mask);

            out.append(parts[0]).append(maskedCore).append(parts[2]);

            // 繼續往後；逗號與空白會在下輪或最後一次 append(inner.substring(i)) 被原樣保留
            i = end;
            // 不手動吃掉逗號，因為我們要保留它與其後的空白到下輪 append
        }

        out.append(po.close());
        return Optional.of(out.toString());
    }

    // 尋找從 start 起「頂層」的目標字元；找不到回 -1
    private static int indexOfTopLevelFrom(String s, int start, char target) {
        TopLevelContext ctx = new TopLevelContext();
        for (int i = 0; i < start; i++) ctx.feed(s.charAt(i));
        for (int i = start; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == target && ctx.isTopLevel()) return i;
            ctx.feed(ch);
        }
        return -1;
    }

    // 去除左右邊空白但保留以便還原
    private static String[] peelWhitespace(String s) {
        int l = 0;
        int r = s.length();
        while (l < r && Character.isWhitespace(s.charAt(l))) l++;
        while (r > l && Character.isWhitespace(s.charAt(r-1))) r--;
        return new String[] { s.substring(0, l), s.substring(l, r), s.substring(r) };
    }

}