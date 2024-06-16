//package org.cris6h16.apirestspringboot.Repository;
//
//import org.cris6h16.apirestspringboot.Entities.ERole;
//import org.cris6h16.apirestspringboot.Entities.RoleEntity;
//import org.cris6h16.apirestspringboot.Entities.UserEntity;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Optional;
//import java.util.Set;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
///**
// * Test class for {@link UserRepository}.<br>
// * This class uses an embedded {@code H2} database to simulate the real database environment.<br>
// * <p>
// * Using the {@code H2} database provides the following benefits:
// * <ul>
// *   <li>Isolation: Tests run in an isolated environment, ensuring no interference with the real database.</li>
// *   <li>Speed: Embedded databases like H2 execute faster than real databases, speeding up test execution.</li>
// *   <li>Maintenance: There is no need to clean the database manually, even if the database structure changes.</li>
// * </ul>
// * <p>
// * Although you can configure tests to use the actual database, it is not recommended due to potential issues such as:
// * <ul>
// *   <li>Loss of isolation: Tests may interfere with real data, leading to inconsistent results.</li>
// *   <li>Slower execution: Real databases typically perform slower than in-memory databases like H2.</li>
// *   <li>Manual cleanup: Changes in the database structure may require manual cleanup, complicating test maintenance.</li>
// * </ul>
// *
// * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
// * @since 1.0
// */
//@DataJpaTest
//@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) // remember add the dependency
//@Transactional(rollbackFor = Exception.class)
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//public class UserRepositoryTest {
//
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private RoleRepository roleRepository;
//    private UserEntity usr;
//
//    /**
//     * <ol>
//     *     <li>Deletes all from the {@link UserRepository} & {@link RoleRepository}</li>
//     *     <li>call to {@link #initializeAndPrepare()} </li>
//     * </ol>
//     *
//     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
//     * @since 1.0
//     */
//    @BeforeEach
//    void setUp() {
//        userRepository.deleteAll();
//        roleRepository.deleteAll();
//
//        // necessary with H2
//        userRepository.flush();
//        roleRepository.flush();
//
//        // `usr`
//        initializeAndPrepare();
//    }
//
//    /**
//     * Test {@link UserRepository#findByUsername(String)}.<br>
//     *
//     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
//     * @since 1.0
//     */
//    @Test
//    @Order(1)
//    void UserRepository_findByUsername_returnANonemptyOptional() {
//        // Arrange
//        userRepository.saveAndFlush(usr);
//
//        // Act
//        Optional<UserEntity> result = userRepository.findByUsername(usr.getUsername());
//
//        // Assert
//        assertThat(result).isNotEmpty();
//        assertThat(result.get().getId()).isNotNull();
//        assertThat(result.get().getUsername()).isEqualTo(usr.getUsername());
//    }
//
//    /**
//     * Test {@link UserRepository#findByEmail(String)}.<br>
//     *
//     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
//     * @since 1.0
//     */
//    @Test
//    @Order(2)
//    void UserRepository_findByEmail_returnANonemptyOptional() {
//        // Arrange
//        userRepository.saveAndFlush(usr);
//
//        // Act
//        Optional<UserEntity> result = userRepository.findByEmail(usr.getEmail());
//
//        // Assert
//        assertThat(result).isNotEmpty();
//        assertThat(result.get().getId()).isNotNull();
//        assertThat(result.get().getEmail()).isEqualTo(usr.getEmail());
//    }
//
//    /**
//     * Test {@link UserRepository#executeInTransaction(Runnable)}.<br>
//     *
//     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
//     * @since 1.0
//     */
//    @Test
//    @Order(3)
//    void UserRepository_executeInTransaction_returnTrue() {
//        // Arrange
//        userRepository.saveAndFlush(usr);
//        boolean completed = true;
//
//        // Act
//        try {
//            userRepository.executeInTransaction(() -> {
//                userRepository.delete(usr);
//                userRepository.flush();
//            });
//        } catch (Exception e) {
//            completed = false;
//        }
//
//
//        // Assert
//        assertThat(completed).isTrue();
//        assertThat(userRepository.existsById(usr.getId())).isFalse();
//
//    }
//
//
//    /**
//     * Initializes the {@link #usr} with {@link ERole#ROLE_USER}, for the tests.
//     *
//     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
//     * @since 1.0
//     */
//    void initializeAndPrepare() {
//        RoleEntity roles = RoleEntity.builder().name(ERole.ROLE_USER).build();
//        usr = UserEntity.builder()
//                .id(null)
//                .username("cris6h16")
//                .password("12345678")
//                .email("cris6h16@gmail.com")
//                .roles(Set.of(roles))
//                .build();
//    }
//
//}