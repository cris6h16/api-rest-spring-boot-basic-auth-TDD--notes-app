package org.cris6h16.apirestspringboot.Config.Security.UserDetailsService;

import org.cris6h16.apirestspringboot.Config.Security.CustomUser.UserWithId;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Test class for {@link UserDetailsServiceImpl}
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a> where
 * @see UserDetailsServiceImpl
 * @since 1.0
 */
@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        clearInvocations(userRepository);
        reset(userRepository);
    }

    /**
     * Test method for {@link UserDetailsServiceImpl#loadUserByUsername(String)}
     *
     * @autor <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @since 1.0
     */
    @Test
    void loadUserByUsername_UsernameNotFoundException() {
        // Arrange
        when(userRepository.findByUsername("username")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("username"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(Cons.User.Fails.NOT_FOUND);
    }

    /**
     * Test method for {@link UserDetailsServiceImpl#loadUserByUsername(String)}
     *
     * @autor <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @since 1.0
     */
    @Test
    void UserFoundWithRolesNull() {
        // Arrange
        UserEntity usr = UserEntity.builder()
                .id(1L)
                .username("cris6h16")
                .password("12345678")
                .email("cristianmherrera21@gmail.com")
                .roles(null)
                .build();

        when(userRepository.findByUsername("username"))
                .thenReturn(Optional.of(usr));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("username");

        // Assert
        assertThat(userDetails)
                .isNotNull()
                .isInstanceOf(UserWithId.class)
                .hasFieldOrPropertyWithValue("username", usr.getUsername())
                .hasFieldOrPropertyWithValue("password", usr.getPassword())
                .hasFieldOrPropertyWithValue("id", usr.getId());
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo(ERole.ROLE_USER.name());
    }

    /**
     * Test method for {@link UserDetailsServiceImpl#loadUserByUsername(String)}
     *
     * @autor <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @since 1.0
     */
    @Test
    void UserFoundWithRolesEmpty() { // todo: rename tests all, remove the """standard""" format from the name
        // Arrange
        UserEntity usr = UserEntity.builder()
                .id(1L)
                .username("cris6h16")
                .password("12345678")
                .email("cristianmherrera21@gmail.com")
                .roles(new HashSet<>())
                .build();

        when(userRepository.findByUsername("cris6h16"))
                .thenReturn(Optional.of(usr));

        // Act & Assert
        UserDetails userDetails = userDetailsService.loadUserByUsername("cris6h16");
        assertThat(userDetails)
                .isNotNull()
                .isInstanceOf(UserDetails.class)
                .hasFieldOrPropertyWithValue("username", usr.getUsername())
                .hasFieldOrPropertyWithValue("password", usr.getPassword());
        assertThat(userDetails
                .getAuthorities()
                .iterator()
                .next()
                .getAuthority()
        ).isEqualTo(ERole.ROLE_USER.name());
    }

    /**
     * Test method for {@link UserDetailsServiceImpl#loadUserByUsername(String)}
     *
     * @autor <a href="https://www.github.com/cris6h16" target="_blank"> Cristian Herrera </a>
     * @since 1.0
     */
    @Test
    void UserFound_Successful() {
        // Arrange
        Set<RoleEntity> roles = Collections.singleton(RoleEntity.builder()
                .id(1L)
                .name(ERole.ROLE_USER)
                .build());

        UserEntity usr = UserEntity.builder()
                .id(1L)
                .username("cris6h16")
                .password("12345678")
                .email("cristianmherrera21@gmail.com")
                .roles(roles)
                .build();

        when(userRepository.findByUsername(usr.getUsername()))
                .thenReturn(Optional.of(usr));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(usr.getUsername());

        // Assert
        assertThat(userDetails)
                .isNotNull()
                .isInstanceOf(UserDetails.class)
                .hasFieldOrPropertyWithValue("username", usr.getUsername())
                .hasFieldOrPropertyWithValue("password", usr.getPassword());
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority())
                .isEqualTo(usr.getRoles().iterator().next().getName().name());
    }

    // userDetails.getAuthorities().iterator().next().getAuthority()

}