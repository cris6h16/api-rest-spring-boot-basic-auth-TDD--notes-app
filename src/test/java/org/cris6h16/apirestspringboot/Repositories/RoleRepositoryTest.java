package org.cris6h16.apirestspringboot.Repositories;

import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for {@link RoleRepository}.<br>
 * This class uses an embedded {@code H2} database to simulate the real database environment.<br>
 * <p>
 * Using the {@code H2} database provides the following benefits:
 * <ul>
 *   <li>Isolation: Tests run in an isolated environment, ensuring no interference with the real database.</li>
 *   <li>Speed: Embedded databases like H2 execute faster than real databases, speeding up test execution.</li>
 *   <li>Maintenance: There is no need to clean the database manually, even if the database structure changes.</li>
 * </ul>
 * <p>
 * Although you can configure tests to use the actual database, it is not recommended due to potential issues such as:
 * <ul>
 *   <li>Loss of isolation: Tests may interfere with real data, leading to inconsistent results.</li>
 *   <li>Slower execution: Real databases typically perform slower than in-memory databases like H2.</li>
 *   <li>Manual cleanup: Changes in the database structure may require manual cleanup, complicating test maintenance.</li>
 * </ul>
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) // remember add the dependency
@Transactional(rollbackFor = Exception.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;
    private List<RoleEntity> roles;

    /**
     * <ol>
     *     <li>Deletes all roles from the repository</li>
     *     <li>call to {@link #initializeAndPrepare()}</li>
     *     <li>Saves all roles to the repository</li>
     * </ol>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @BeforeEach
    void setUp() {
        roleRepository.deleteAll();
        roleRepository.flush(); // due to H2
        initializeAndPrepare();
        roleRepository.saveAllAndFlush(roles);
    }

    /**
     * Tests for the {@link RoleRepository#findByName(ERole)} method
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    void findByName() {
        //Arrange
        assertThat(roleRepository.count()).isEqualTo(2);

        //Act
        this.roles.forEach(role -> {
            RoleEntity fromDB = roleRepository.findByName(role.getName()).orElse(null);

            //Assert
            assertThat(fromDB).isNotNull();
            assertThat(fromDB.getName()).isEqualTo(role.getName());
        });
    }

    /**
     * Initializes and prepares the roles list
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    void initializeAndPrepare() {
        roles = List.of(
                RoleEntity.builder().name(ERole.ROLE_USER).build(),
                RoleEntity.builder().name(ERole.ROLE_ADMIN).build()
        );
    }


}