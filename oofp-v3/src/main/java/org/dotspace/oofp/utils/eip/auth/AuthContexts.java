package org.dotspace.oofp.utils.eip.auth;

import org.dotspace.oofp.model.dto.auth.AuthContext;
import org.dotspace.oofp.utils.builder.GeneralBuilders;
import org.dotspace.oofp.utils.builder.operation.WriteOperations;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.Set;

// 用於合併 AuthContext 的工具類別
@UtilityClass
public class AuthContexts {

    // 將前一個 AuthContext 與 EntitlementsResolver 所解析出的授權資料合併
    public AuthContext enrich(
            @NonNull AuthContext prev,
            @NonNull EntitlementsResolver.Entitlements ent) {

        // 既有資料（來自 token / bindAuthContext）
        Set<String> prevRoles =
                prev.getRoles() == null ? Set.of() : prev.getRoles();

        Set<String> prevAuthorities =
                prev.getAuthorities() == null ? Set.of() : prev.getAuthorities();

        // resolver 補齊的資料（來自 DB / IAM / if-else）
        Set<String> entRoles =
                ent.roles() == null ? Set.of() : ent.roles();

        Set<String> entAuthorities =
                ent.authorities() == null ? Set.of() : ent.authorities();

        // 嚴格只做 union（不可覆寫）
        Set<String> mergedRoles = union(prevRoles, entRoles);
        Set<String> mergedAuthorities = union(prevAuthorities, entAuthorities);

        Set<String> prevGroups = prev.getRoleGroups() == null ? Set.of() : prev.getRoleGroups();
        Set<String> entGroups  = ent.roleGroups() == null ? Set.of() : ent.roleGroups();
        Set<String> mergedGroups = union(prevGroups, entGroups);

        return GeneralBuilders.supply(AuthContext::new)
                // 身分相關：永遠沿用舊的（不可被 resolver 改）
                .with(WriteOperations.set(AuthContext::setPrincipalId, prev.getPrincipalId()))
                .with(WriteOperations.set(AuthContext::setTenantId, prev.getTenantId()))
                .with(WriteOperations.set(AuthContext::setTokenId, prev.getTokenId()))

                // 授權相關：只補齊
                .with(WriteOperations.set(AuthContext::setRoles, mergedRoles))
                .with(WriteOperations.set(AuthContext::setRoleGroups, mergedGroups))
                .with(WriteOperations.set(AuthContext::setAuthorities, mergedAuthorities))

                .build();
    }

    private <T> Set<T> union(Set<T> a, Set<T> b) {
        if (a == null || a.isEmpty()) {
            return b == null ? Set.of() : Set.copyOf(b);
        }
        if (b == null || b.isEmpty()) {
            return Set.copyOf(a);
        }
        var s = new java.util.HashSet<>(a);
        s.addAll(b);
        return java.util.Collections.unmodifiableSet(s);
    }
}
