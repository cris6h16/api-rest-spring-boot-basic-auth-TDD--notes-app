package org.cris6h16.apirestspringboot.DTOs.Public;

import lombok.*;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;

/**
 * DTO for {@link RoleEntity}, used to expose only the name of the role.
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@AllArgsConstructor
@NoArgsConstructor // required for Jackson
@Getter
@Builder
@EqualsAndHashCode
@ToString
public class PublicRoleDTO {
    private ERole name;
}
