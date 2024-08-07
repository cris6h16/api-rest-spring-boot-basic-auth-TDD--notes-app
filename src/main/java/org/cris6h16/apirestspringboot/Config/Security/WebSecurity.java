package org.cris6h16.apirestspringboot.Config.Security;

import org.cris6h16.apirestspringboot.Config.Security.CustomUser.UserWithId;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;
import java.util.stream.Stream;

@Component
public class WebSecurity {
    private static final Logger log = LoggerFactory.getLogger(WebSecurity.class);


    public boolean checkIfIsAdminOrUserAndHasThisIdAsPrincipalId(Supplier<Authentication> supplier, String userId) {
        boolean granted = false;
        try {
            Authentication auth = supplier.get();
            if (!hasAnyRole(auth, ERole.ROLE_ADMIN, ERole.ROLE_USER)) return false;
            granted = principalIdEqualsTo(auth, userId);
        } catch (Exception e) {
            log.debug("Debug Exception on checkIfIsAdminOrUserAndHasThisIdAsPrincipalId: {}", e.toString());
        }
        log.debug("Debug checkIfIsAdminOrUserAndHasThisIdAsPrincipalId: {}", granted);
        return granted;
    }


    private boolean principalIdEqualsTo(Authentication authentication, String userId) {
        return ((UserWithId) (authentication.getPrincipal()))
                .getId().equals(Long.parseLong(userId));
    }

    private boolean hasAnyRole(Authentication authentication, ERole... eRoles) {
//        return authentication.getAuthorities().stream() --> I have a custom implementation of UserDetails, then the authorities I should get from the principal
        return ((UserWithId) authentication.getPrincipal()).getAuthorities().stream()
                .anyMatch(r -> Stream.of(eRoles)
                        .map(ERole::toString)
                        .anyMatch(role -> role.equals(r.getAuthority())));
    }
}
