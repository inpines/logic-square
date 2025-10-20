package org.dotspace.oofp.support.msg;

public record ParsedOpen(
        String prefix,   // "Type" 或空字串
        char open,       // '{' / '[' / '('
        char close,      // 對應的 '}' / ']' / ')'
        String inner,    // 夾在括號內的內容
        boolean objectLike // true: 當作 key=value 物件；false: 當作集合/位置列表
) {
    public static ParsedOpen none() {
        return new ParsedOpen("", '\0', '\0', "", false);
    }

    public boolean isNotFound() {
        return open == '\0';
    }

    public boolean hasPrefix() {
        return prefix != null && !prefix.isBlank();
    }

    /** 用已遮罩的 inner 重建輸出（保留前綴與括號類型） */
    public String rebuild(String maskedInner) {
        return (hasPrefix() ? prefix : "") + open + maskedInner + close;
    }
}