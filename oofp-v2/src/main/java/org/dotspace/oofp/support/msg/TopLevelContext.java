package org.dotspace.oofp.support.msg;

import lombok.NoArgsConstructor;

import java.util.*;

// ===== 副作用集中：括號/引號感知的掃描 Context =====
@NoArgsConstructor
final class TopLevelContext {
    // 小中大括號
    private int p;
    private int b;
    private int c;

    // 單雙引號
    private boolean inS;
    private boolean inD;

    /** 依序餵入字元以更新上下文 */
    void feed(char ch) {
        // quote 切換（若需跳脫字元可再擴充）
        if (!inD && ch == '\'' && !inS) { inS = true;  return; }
        if ( inS && ch == '\'')         { inS = false; return; }
        if (!inS && ch == '"'  && !inD) { inD = true;  return; }
        if ( inD && ch == '"')          { inD = false; return; }

        if (inS || inD) return; // 引號內不計深度

        switch (ch) {
            case '(' -> p++;
            case ')' -> p--;
            case '[' -> b++;
            case ']' -> b--;
            case '{' -> c++;
            case '}' -> c--;
            default -> {
                // do nothing
            }
        }
    }

    /** 是否位於「最外層」（非引號且三種深度皆為 0） */
    boolean isTopLevel() { return !inS && !inD && p == 0 && b == 0 && c == 0; }

    /** 頂層第一個 target 位置；找不到回 -1 */
    static int indexOfTopLevel(String s, char target) {
        TopLevelContext ctx = new TopLevelContext();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == target && ctx.isTopLevel()) return i;
            ctx.feed(ch);
        }
        return -1;
    }

    /** 以頂層 delimiter 分割，保留引號與括號內的 delimiter */
    static List<String> splitTopLevel(String s) {
        TopLevelContext ctx = new TopLevelContext();
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == ',' && ctx.isTopLevel()) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
            ctx.feed(ch);
        }
        out.add(cur.toString());
        return out;
    }

    boolean isTopLevelIgnoring(char openKind) {
        // 忽略 openKind 對應的那一類括號深度，只看其它兩類與引號
        boolean okP = (openKind=='(') || (p==0);
        boolean okB = (openKind=='[') || (b==0);
        boolean okC = (openKind=='{') || (c==0);
        return !inS && !inD && okP && okB && okC;
    }
}