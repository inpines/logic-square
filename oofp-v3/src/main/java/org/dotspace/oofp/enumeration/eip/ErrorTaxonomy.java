package org.dotspace.oofp.enumeration.eip;

public enum ErrorTaxonomy {
    VALIDATION,              // 缺欄位/格式錯/不符合 schema
    UNAUTHORIZED,            // claims 不符
    NOT_FOUND,               // 外部依賴找不到（可視情況 retry 或 dlq）
    CONFLICT,                // 冪等衝突 / 已處理
    TRANSIENT_DEPENDENCY,    // 外部暫時性失敗（timeout / 5xx / network）
    INTERNAL                 // 程式錯 / 不可預期
}
