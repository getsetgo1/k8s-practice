package org.beyond.ordersystem.common.auth;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {
    public String getEmailFromSecurityContext() {
        return SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
    }
}
