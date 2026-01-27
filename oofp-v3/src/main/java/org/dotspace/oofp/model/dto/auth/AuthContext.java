package org.dotspace.oofp.model.dto.auth;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class AuthContext {
    String principalId;
    String tenantId; // optional
    Set<String> roles; // 身份
    Set<String> roleGroups; // 歸屬/範圍
    Set<String> authorities; // 權限
    Set<String> effectiveAuthorities; // 包含從角色繼承的權限
    String tokenId; // jti, optional

    public static AuthContext anonymous() {
        return AuthContext.builder()
                .principalId("anonymous")
                .roles(Set.of())
                .roleGroups(Set.of())
                .authorities(Set.of())
                .build();
    }
}
