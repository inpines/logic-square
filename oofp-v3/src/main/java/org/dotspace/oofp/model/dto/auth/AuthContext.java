package org.dotspace.oofp.model.dto.auth;

import org.dotspace.oofp.utils.builder.GeneralBuilders;
import org.dotspace.oofp.utils.builder.operation.WriteOperations;
import lombok.Data;

import java.util.Set;

@Data
public class AuthContext {
    String principalId;
    String tenantId; // optional
    Set<String> roles; // 身份
    Set<String> roleGroups; // 歸屬/範圍
    Set<String> authorities; // 權限
    Set<String> effectiveAuthorities; // 包含從角色繼承的權限
    String tokenId; // jti, optional

    public static AuthContext anonymous() {
        return GeneralBuilders.supply(AuthContext::new)
                .with(WriteOperations.set(AuthContext::setPrincipalId, "anonymous"))
                .with(WriteOperations.set(AuthContext::setRoles, Set.of()))
                .with(WriteOperations.set(AuthContext::setRoleGroups, Set.of()))
                .with(WriteOperations.set(AuthContext::setAuthorities, Set.of()))
                .build();
    }
}
