package org.cris6h16.apirestspringboot.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cris6h16.apirestspringboot.Service.Interfaces.UserService;
import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.cris6h16.apirestspringboot.DTOs.UpdateUserDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Repository.NoteRepository;
import org.cris6h16.apirestspringboot.Repository.RoleRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest { //TODO: improve HARDCODE

    @Autowired
    TestRestTemplate rt;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    ObjectMapper objectMapper; // for parsing JSON

    @Autowired
    NoteRepository noteRepository;
    @Autowired
    UserService userService;
    @Autowired
    PasswordEncoder passwordEncoder; // for comparing passwords


    @Test
    @DirtiesContext
    void shouldCreateAUser() {
        String url = "/api/users";
        String username = "cris6h16";
        String pass = "12345678";
        String email = "cristianmherrera21@gmail.com";

        // Create a user
        HttpEntity<CreateUserDTO> user = new HttpEntity<>(new CreateUserDTO(username, pass, email));
        ResponseEntity<Void> res = rt.exchange(url, HttpMethod.POST, user, Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // get id from Location Header
        String[] parts = res.getHeaders().getLocation().toString().split("/");
        Long id = Long.parseLong(parts[parts.length - 1]);

        // Get the user
        Optional<UserEntity> userEntity = userRepository.findByIdEagerly(id);
        assertThat(userEntity.isPresent()).isTrue();


        // Check the user
        UserEntity fromDB = userEntity.get();
//        assertThat(fromDB.getNotes()).isEmpty();
        assertThat(fromDB.getId()).isNotNull();
        assertThat(fromDB.getUpdatedAt()).isNull();
        assertThat(fromDB.getDeletedAt()).isNull();
        assertThat(fromDB.getCreatedAt()).isNotNull();
        assertThat(fromDB.getEmail()).isEqualTo(email);
        assertThat(fromDB.getUsername()).isEqualTo(username);
        assertThat(fromDB.getRoles()).size().isEqualTo(1);
        assertThat(passwordEncoder.matches(pass, fromDB.getPassword())).isTrue();
        assertThat(fromDB.getRoles()
                .stream()
                .filter(r -> (r.getName().equals(ERole.ROLE_USER)) && r.getId() > 0)
                .count()).isEqualTo(1);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateAUser_usernameAlreadyExists() {

        String url = "/api/users";
        String username = "cris6h16";
        String pass = "12345678";
        String email = "cristianmherrera21@gmail.com";
        String failBodyMssg = "Username already exists";

        // count users -> before
        long countB = userRepository.count();

        // Create a user
        HttpEntity<CreateUserDTO> user = new HttpEntity<>(new CreateUserDTO(username, pass, email));
        ResponseEntity<Void> res = rt.exchange(url, HttpMethod.POST, user, Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Wheres the verification if was it created? --> We already test it in the previous test (good practice)

        // Create the same username
        user = new HttpEntity<>(new CreateUserDTO(username, (pass + "hello"), ("word" + email)));
        ResponseEntity<String> res2 = rt.exchange(url, HttpMethod.POST, user, String.class);
        assertThat(res2.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(res2.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB + 1);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateAUser_EmailAlreadyExists() {
        String url = "/api/users";
        String username = "cris6h16";
        String pass = "12345678";
        String email = "cristianmherrera21@gmail.com";
        String failBodyMssg = "Email already exists";


        // count users -> before
        long countB = userRepository.count();

        // Create a user
        HttpEntity<CreateUserDTO> user = new HttpEntity<>(new CreateUserDTO(username, pass, email));
        ResponseEntity<Void> res = rt.exchange(url, HttpMethod.POST, user, Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Create the same email
        user = new HttpEntity<>(new CreateUserDTO((username + "hello"), (pass + "hello"), email));
        ResponseEntity<String> res2 = rt.exchange(url, HttpMethod.POST, user, String.class);
        assertThat(res2.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(res2.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB + 1);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateAUser_PasswordTooShort() {
        String url = "/api/users";
        String username = "cris6h16";
        String pass = "1234567";
        String email = "cristianmherrera21@gmail.com";
        String failBodyMssg = "Password must be at least 8 characters";

        // count users -> before
        long countB = userRepository.count();

        // Create a user
        HttpEntity<CreateUserDTO> user = new HttpEntity<>(new CreateUserDTO(username, pass, email));
        ResponseEntity<String> res = rt.exchange(url, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateAUser_EmailIsInvalid() {

        String url = "/api/users";
        String username = "cris6h16";
        String pass = "12345678";
        String email = "thisisaninvalidemail";
        String failBodyMssg = "Email is invalid";

        // count users -> before
        long countB = userRepository.count();

        // Create a user
        HttpEntity<CreateUserDTO> user = new HttpEntity<>(new CreateUserDTO(username, pass, email));
        ResponseEntity<String> res = rt.exchange(url, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody().split("\"")[3]).isEqualTo(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateAUser_EmailIsNullOrEmpty() {
        String url = "/api/users";
        String username = "cris6h16";
        String pass = "12345678";
        String email = "";
        String failBodyMssg = "Email is required";

        // count users -> before
        long countB = userRepository.count();

        // email is empty
        HttpEntity<CreateUserDTO> user = new HttpEntity<>(new CreateUserDTO(username, pass, email));
        ResponseEntity<String> res = rt.exchange(url, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

        // email is null
        user = new HttpEntity<>(new CreateUserDTO(username, pass, null));
        res = rt.exchange(url, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateAUser_UsernameIsNullOrEmpty() {
        String url = "/api/users";
        String username = "";
        String pass = "12345678";
        String email = "cris6h16@gmailcom";
        String failBodyMssg = "Username mustn't be blank";

        // count users -> before
        long countB = userRepository.count();

        // username is empty
        HttpEntity<CreateUserDTO> user = new HttpEntity<>(new CreateUserDTO(username, pass, email));
        ResponseEntity<String> res = rt.exchange(url, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

        // username is null
        user = new HttpEntity<>(new CreateUserDTO(null, pass, email));
        res = rt.exchange(url, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateAUser_PasswordIsNullOrEmpty() {
        String url = "/api/users";
        String username = "cris6h16";
        String pass = "";
        String email = "cris6h16@gmail.com";
        String failBodyMssg = "Password must be at least 8 characters";

        // count users -> before
        long countB = userRepository.count();

        // password is empty
        HttpEntity<CreateUserDTO> user = new HttpEntity<>(new CreateUserDTO(username, pass, email));
        ResponseEntity<String> res = rt.exchange(url, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        System.out.println(res);
        assertThat(res.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

        // password is null
        user = new HttpEntity<>(new CreateUserDTO(username, null, email));
        res = rt.exchange(url, HttpMethod.POST, user, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

        // count users -> after
        long countA = userRepository.count();
        assertThat(countA).isEqualTo(countB);
    }

    @Nested
            //@DirtiesContext // Really I don't understand why some tests fail, it runs like if @DirtiesContext doesn't work in class level
    class withAUserInDB { // TODO: Centralize "hardcoded" & try to avoid it

        String url = "/api/users";
        String username = "cris6h16";
        String pass = "12345678";
        String email = "cristianmherrera21@gmail.com";
        UserEntity before;

        @BeforeEach
        void setUp() {
            // Create a user (we already tested it)
            System.out.println(userRepository.findAllEager());
            HttpEntity<CreateUserDTO> user = new HttpEntity<>(new CreateUserDTO(username, pass, email));
            ResponseEntity<Void> res = rt.exchange(url, HttpMethod.POST, user, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            String[] parts = res.getHeaders().getLocation().toString().split("/");
            Long id = Long.parseLong(parts[parts.length - 1]);
            before = userRepository.findByIdEagerly(id).get();


            // PATCH isn't supported by TestRestTemplate
            rt
                    .getRestTemplate()
                    .setRequestFactory(new HttpComponentsClientHttpRequestFactory()); // remember add dependency: org.apache.httpcomponents.client5:httpclient5
        }

        @Test
        @DirtiesContext
        void shouldUpdateUsername() {
            UpdateUserDTO forUPDT;
            UserEntity updated;

            forUPDT = new UpdateUserDTO(before.getId());
            forUPDT.setUsername("github.com/cris6h16");

            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange((url + "/" + forUPDT.getId()), HttpMethod.PATCH, httpEntity, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            updated = userRepository.findByIdEagerly(forUPDT.getId()).get();
            assertThat(updated.getUsername()).isEqualTo(forUPDT.getUsername());

            {   // restore the old values for comparison
                updated.setUsername((String) before.getUsername());
                updated.setUpdatedAt((Date) before.getUpdatedAt());
                assertThat(updated.equals(before)).isTrue();
            }
        }

        @Test
        @DirtiesContext
        void shouldUpdateEmail() {
            UpdateUserDTO forUPDT;
            UserEntity updated;

            forUPDT = new UpdateUserDTO(before.getId());
            forUPDT.setEmail("githubcomcris6h16@gmail.com");

            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange((url + "/" + forUPDT.getId()), HttpMethod.PATCH, httpEntity, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            updated = userRepository.findByIdEagerly(forUPDT.getId()).get();
            assertThat(updated.getEmail()).isEqualTo(forUPDT.getEmail());

            {   // restore the old values for comparison
                updated.setEmail((String) before.getEmail());
                updated.setUpdatedAt((Date) before.getUpdatedAt());
                assertThat(updated.equals(before)).isTrue();
            }
        }

        @Test
        @DirtiesContext
        void shouldUpdatePassword() {
            UpdateUserDTO forUPDT;
            UserEntity updated;

            forUPDT = new UpdateUserDTO(before.getId());
            forUPDT.setPassword("iwannagotoliveinusa");

            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange((url + "/" + forUPDT.getId()), HttpMethod.PATCH, httpEntity, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            updated = userRepository.findByIdEagerly(forUPDT.getId()).get();
            assertThat(passwordEncoder.matches(forUPDT.getPassword(), updated.getPassword())).isTrue();

            {   // restore the old values for comparison
                updated.setPassword((String) before.getPassword());
                updated.setUpdatedAt((Date) before.getUpdatedAt());
                assertThat(updated.equals(before)).isTrue();
            }
        }

        @Test
        @DirtiesContext
        void shouldUpdateUsernameEmailPassword() {
            UpdateUserDTO forUPDT;
            UserEntity updated;

            forUPDT = new UpdateUserDTO(before.getId());
            forUPDT.setUsername("newusername");
            forUPDT.setEmail("newemail@gmail.com");
            forUPDT.setPassword("newpassword");

            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange((url + "/" + forUPDT.getId()), HttpMethod.PATCH, httpEntity, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            updated = userRepository.findByIdEagerly(before.getId()).get();
            assertThat(updated.getUsername()).isEqualTo(forUPDT.getUsername());
            assertThat(updated.getEmail()).isEqualTo(forUPDT.getEmail());
            assertThat(passwordEncoder.matches(forUPDT.getPassword(), updated.getPassword())).isTrue();


            {   // restore the old values for comparison
                updated.setUsername((String) before.getUsername());
                updated.setEmail((String) before.getEmail());
                updated.setPassword((String) before.getPassword());
                updated.setUpdatedAt((Date) before.getUpdatedAt());
                assertThat(updated.equals(before)).isTrue();
            }

        }

        @Test
        @DirtiesContext
        void shouldBeGreaterUpdatedAt() {
            UpdateUserDTO forUPDT;
            UserEntity updated;

            // updated `username`
            forUPDT = new UpdateUserDTO(before.getId());
            forUPDT.setUsername("github.com/cris6h16");

            //first update then `UpdatedAt` pass from `null` -> `Date`
            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange((url + "/" + forUPDT.getId()), HttpMethod.PATCH, httpEntity, Void.class);

            updated = userRepository.findByIdEagerly(before.getId()).get();
            assertThat(updated.getUpdatedAt()).isNotNull();
            assertThat(updated.getUpdatedAt()).isAfter(new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 48))); // 48 hours ago
        }

        @Test
        @DirtiesContext
        void updateShouldNotChange_DeletedAtCreatedAtRolesNotes() {
            UpdateUserDTO forUPDT;
            UserEntity updated;

            // updated `username`, `email` & `password`
            forUPDT = new UpdateUserDTO(before.getId());
            forUPDT.setUsername("newusername");
            forUPDT.setEmail("newemail@gmail.com");
            forUPDT.setPassword("newpassword");

            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange((url + "/" + forUPDT.getId()), HttpMethod.PATCH, httpEntity, Void.class);

            updated = userRepository.findByIdEagerly(before.getId()).get();
            assertThat(updated.getCreatedAt()).isEqualTo(before.getCreatedAt());
            assertThat(updated.getDeletedAt()).isEqualTo(before.getDeletedAt());
            assertThat(updated.getRoles()).isEqualTo(before.getRoles());
//            assertThat(updated.getNotes()).isEqualTo(before.getNotes());

        }

        @Test
        @DirtiesContext
        void shouldNotUpdateUsernameAlreadyExists() {
            String failBodyMssg = "Username already exists";

            UpdateUserDTO forUPDT = new UpdateUserDTO(before.getId());
            forUPDT.setUsername(username);
            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<>(forUPDT);

            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange((url + "/" + forUPDT.getId()), HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(re.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg); //TODO: remember doc about the importance of format responses

            assertThat(userRepository.findByIdEagerly(forUPDT.getId()).get()).isEqualTo(before);
        }


        @Test
        @DirtiesContext
        void shouldNotUpdateEmailAlreadyExists() {
            String failBodyMssg = "Email already exists";

            UpdateUserDTO forUPDT = new UpdateUserDTO(before.getId());
            forUPDT.setEmail(email);

            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange((url + "/" + forUPDT.getId()), HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(re.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg); //TODO: remember doc about the importance of format responses

            assertThat(userRepository.findByIdEagerly(forUPDT.getId()).get()).isEqualTo(before);
        }

        @Test
        @DirtiesContext
        void shouldNotUpdateYouNeedToBeAuthenticated() {
            String failBodyMssg = "You must be authenticated to perform this action";

            UpdateUserDTO forUPDT = new UpdateUserDTO(before.getId());
            forUPDT.setUsername("github.com/cris6h16");
            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<>(forUPDT);

            ResponseEntity<String> re = rt.exchange((url + "/" + forUPDT.getId()), HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(re.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

            assertThat(userRepository.findByIdEagerly(forUPDT.getId()).get()).isEqualTo(before);
        }

        @Test
        @DirtiesContext
        void shouldNotUpdateYouCannotUpdateOtherUserAccount() {
            String failBodyMssg = "You aren't the owner of this id";

            UpdateUserDTO forUPDT = new UpdateUserDTO(before.getId() + 1);
            forUPDT.setUsername("other-username");

            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange((url + "/" + forUPDT.getId()), HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(re.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

        }

        @Test
        @DirtiesContext
//        void shouldNotUpdateNonexistentIdForbidden() {
        void shouldNotUpdateNonexistentIdUnauthorized() {
            String failBodyMssg = "You aren't the owner of this id";

            UpdateUserDTO forUPDT = new UpdateUserDTO(before.getId() + 99999);
            forUPDT.setUsername("other-username");

            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange((url + "/" + forUPDT.getId()), HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(re.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

            assertThat(userRepository.findByIdEagerly(forUPDT.getId())).isNotPresent();
        }

        @Test
        @DirtiesContext
        void shouldNotUpdateEmailIsInvalid() {
            String failBodyMssg = "Email is invalid";

            UpdateUserDTO forUPDT = new UpdateUserDTO(before.getId());
            forUPDT.setEmail("thisisaninvalidemail");

            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange((url + "/" + forUPDT.getId()), HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(re.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

            assertThat(userRepository.findByIdEagerly(forUPDT.getId()).get()).isEqualTo(before);
        }

        @Test
        @DirtiesContext
        void shouldNotUpdatePasswordTooShort() {
            String failBodyMssg = "Password must be at least 8 characters";

            UpdateUserDTO forUPDT = new UpdateUserDTO(before.getId());
            forUPDT.setPassword("1234567");

            HttpEntity<UpdateUserDTO> httpEntity = new HttpEntity<UpdateUserDTO>(forUPDT);
            ResponseEntity<String> re = rt
                    .withBasicAuth(username, pass)
                    .exchange((url + "/" + forUPDT.getId()), HttpMethod.PATCH, httpEntity, String.class);
            assertThat(re.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(re.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

            assertThat(userRepository.findByIdEagerly(forUPDT.getId()).get()).isEqualTo(before);
        }


        @Test
        @DirtiesContext
            // Others tests can affect the stored in `before`
        void shouldGetAUserById() {
            Long id = before.getId();

            ResponseEntity<PublicUserDTO> res = rt
                    .withBasicAuth(username, pass)
                    .getForEntity(url + "/" + id, PublicUserDTO.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

            PublicUserDTO user = res.getBody();
            assertThat(user.getId()).isEqualTo(id);
            assertThat(user.getUsername()).isEqualTo(username);
            assertThat(user.getEmail()).isEqualTo(email);
            assertThat(user.getRoles().size()).isEqualTo(1);
            assertThat(user.getNotes()).isEmpty();
        }

        @Test
        @DirtiesContext
            // necessary because `@BeforeEach` can lead in CONFLICT (creating the same user)
        void shouldNotGetIsNotAuthenticated() {
            String failBodyMssg = "You must be authenticated to perform this action";

            Long id = before.getId();

            ResponseEntity<String> res = rt
                    .getForEntity(url + "/" + id, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(res.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);
        }

        @Test
        @DirtiesContext
            // necessary because `@BeforeEach` can lead in CONFLICT (creating the same user)
        void shouldNotGetUserYouAreNotTheOwner() {
            String failBodyMssg = "You aren't the owner of this ID";

            Long id = before.getId() + 1091;

            ResponseEntity<String> res = rt
                    .withBasicAuth(username, pass)
                    .getForEntity(url + "/" + id, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(res.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);
        }

        @Test
        @DirtiesContext
        void shouldDeleteAUser() {
            Long id = before.getId();

            assertThat(userRepository.findById(id)).isPresent();

            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange(url + "/" + id, HttpMethod.DELETE, null, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            assertThat(userRepository.findById(id)).isEmpty();
        }

        @Test
        @DirtiesContext
        void shouldNotDeleteAUserIsNotAuthenticated() {
            String failBodyMssg = "You must be authenticated to perform this action";
            Long id = before.getId();

            assertThat(userRepository.findById(id)).isPresent();

            ResponseEntity<String> res = rt
                    .exchange(url + "/" + id, HttpMethod.DELETE, null, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(res.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

            assertThat(userRepository.findById(id)).isPresent();
        }

        @Test
        @DirtiesContext
        void shouldNotDeleteAUserYouAreNotTheOwner() throws JsonProcessingException {
            String failBodyMssg = "You aren't the owner of this ID";
            Long id = before.getId() + 1;

            ResponseEntity<String> res = rt
                    .withBasicAuth(username, pass)
                    .exchange(url + "/" + id, HttpMethod.DELETE, null, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(res.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);

            assertThat(userRepository.findById(id)).isEmpty();
        }
        // If we let multiple sessions & generally it isn't with basic auth...
//        @Test
//        @DirtiesContext
//        void userNotFound() {
//            String failBodyMssg = "User not found";
//            Long id = before.getId();
//
//            ResponseEntity<String> res = rt
//                    .withBasicAuth(username, pass)
//                    .exchange(url + "/" + id, HttpMethod.DELETE, null, String.class);
//            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
//            assertThat(res.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);
//        }

        @Test
        @DirtiesContext
        void shouldDeleteAllNotesWhenDeleteAUser() {
            Long id = before.getId();
            String urlNotes = "/api/notes";

            // create notes --> test/resources/NotesEntities.txt => json
            try (
                    InputStream in = getClass().getClassLoader().getResourceAsStream("NotesEntities.txt");
                    BufferedReader bf = new BufferedReader(new InputStreamReader(in));
            ) {
                String line;
                while ((line = bf.readLine()) != null) {
                    NoteEntity ne = objectMapper
                            .readerFor(NoteEntity.class)
                            .readValue(line);
                    CreateNoteDTO cnd = CreateNoteDTO.builder()
                            .title(ne.getTitle())
                            .content(ne.getContent())
                            .build();
                    HttpEntity<CreateNoteDTO> httpEntity = new HttpEntity<>(cnd);
                    ResponseEntity<Void> res = rt
                            .withBasicAuth(username, pass)
                            .exchange(urlNotes, HttpMethod.POST, httpEntity, Void.class);
                    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // check if notes were created
            assertThat(noteRepository.findByUserId(id)).hasSizeGreaterThan(10);

            // delete user
            ResponseEntity<Void> res = rt
                    .withBasicAuth(username, pass)
                    .exchange(url + "/" + id, HttpMethod.DELETE, null, Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // check if user was deleted
            assertThat(userRepository.findById(id)).isEmpty();

            // check if notes were deleted
            assertThat(noteRepository.findByUserId(id)).isEmpty();
        }
    }

    @Nested
    @DirtiesContext
    class with27Users1AdminInDB {

        String adminUsername = "cris6h16InGithub";
        String adminPass = "cris6h16";
        static List<Long> ids;
        static String url = "/api/users";
        static List<CreateUserDTO> users;
        @Autowired
        Environment env;


        @BeforeAll
        static void beforeAll() {
            ids = new ArrayList<>();
            users = new ArrayList<>();
            ObjectMapper objectMapper = new ObjectMapper();

            // save users in static List
            try (
                    InputStream in = with27Users1AdminInDB.class.getClassLoader().getResourceAsStream("CreateUserDTOs.txt");
                    BufferedReader bf = new BufferedReader(new InputStreamReader(in));
            ) {
                String line;
                while ((line = bf.readLine()) != null) {
                    CreateUserDTO user = objectMapper
                            .readerFor(CreateUserDTO.class)
                            .readValue(line);
                    users.add(user);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            assertThat(users).hasSize(27);
        }

        @BeforeEach
        void setUp() {
            //create an admin
            userRepository.executeInTransaction(() -> {
                RoleEntity role = roleRepository.findByName(ERole.ROLE_ADMIN).orElse(null);
                if (role == null) role = RoleEntity.builder().name(ERole.ROLE_ADMIN).build();

                UserEntity ue = UserEntity.builder()
                        .username(adminUsername)
                        .password(passwordEncoder.encode(adminPass))
                        .email(adminUsername + "@gmail.com")
                        .roles(Set.of(role))
                        .createdAt(new Date(System.currentTimeMillis()))
                        .build();
                userRepository.save(ue);
            });

            // create users
            for (CreateUserDTO user : users) {
                HttpEntity<CreateUserDTO> en = new HttpEntity<>(user);
                ResponseEntity<Void> res = rt
                        .exchange(url, HttpMethod.POST, en, Void.class);
                assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                String[] parts = res.getHeaders().getLocation().toString().split("/");
                ids.add(Long.parseLong(parts[parts.length - 1]));
            }
            assertThat(userRepository.count()).isEqualTo(28);

            Assertions.assertTrue(userRepository.findByUsername(adminUsername).isPresent(), "Admin not found, first insert an Admin in DB");
            Assertions.assertTrue(userRepository.findByUsername(adminUsername).get().getRoles().stream()
                            .anyMatch(roleEntity -> roleEntity.getName().equals(ERole.ROLE_ADMIN)),
                    "used Admin credentials doesn't have ROLE_ADMIN");
        }


        @Test
        @DirtiesContext
        void shouldListAllUsersInPagesIsPageable() {
            String pageParam = env.getProperty("spring.data.web.pageable.page-parameter");
            String sizeParam = env.getProperty("spring.data.web.pageable.default-page-size");
            short page = 0;
            short size = 3;

            //CREATE ADMIN --> directly in DB

            //DESC
            while (true) {
                ParameterizedTypeReference<List<PublicUserDTO>> ptr = new ParameterizedTypeReference<List<PublicUserDTO>>() {
                };
                URI uri = UriComponentsBuilder.fromUriString(url)
                        .queryParam(pageParam, page++)
                        .queryParam(sizeParam, size)
                        .queryParam("sort", "id,desc")
                        .build()
                        .toUri();
                ResponseEntity<List<PublicUserDTO>> pu = rt
                        .withBasicAuth(adminUsername, adminPass)
                        .exchange(uri, HttpMethod.GET, null, ptr);
                assertThat(pu.getStatusCode()).isEqualTo(HttpStatus.OK);

                List<PublicUserDTO> listPU = pu.getBody();
                assertThat(listPU.stream()
                        .map(PublicUserDTO::getId)
                        .collect(Collectors.toCollection(() -> new ArrayList<>()))
                ).isSortedAccordingTo(Collections.reverseOrder());
                // if retrieved has less size than the page size, then it's the last page
                if (listPU.size() < size) break;
            }

            //ASC
            while (true) {
                ParameterizedTypeReference<List<PublicUserDTO>> ptr = new ParameterizedTypeReference<List<PublicUserDTO>>() {
                };
                URI uri = UriComponentsBuilder.fromUriString(url)
                        .queryParam(pageParam, page++)
                        .queryParam(sizeParam, size)
                        .queryParam("sort", "id,asc")
                        .build()
                        .toUri();
                ResponseEntity<List<PublicUserDTO>> pu = rt
                        .withBasicAuth(adminUsername, adminPass)
                        .exchange(uri, HttpMethod.GET, null, ptr);
                assertThat(pu.getStatusCode()).isEqualTo(HttpStatus.OK);

                List<PublicUserDTO> listPU = pu.getBody();
                // if retrieved has less size than the page size, then it's the last page
                if (listPU.size() < size) break;
            }

        }

        @Test
        @DirtiesContext
// necessary because `@BeforeEach` can lead in CONFLICT (creating the same user)
        void shouldNotListAllUserIsPageable() {
            Integer users = (int) userRepository.count();

            ParameterizedTypeReference<List<PublicUserDTO>> ptr = new ParameterizedTypeReference<List<PublicUserDTO>>() {
            };
            ResponseEntity<List<PublicUserDTO>> res = rt
                    .withBasicAuth(adminUsername, adminPass)
                    .exchange(url, HttpMethod.GET, null, ptr);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

            assertThat(res.getBody()).hasSizeLessThan(users);
        }

        @Test
        @DirtiesContext
        void shouldListAllUsersDefaultConfigNotUrlParams() {
            Integer users = (int) userRepository.count();
            Short size = env.getProperty("spring.data.web.pageable.default-page-size", Short.class);


            ParameterizedTypeReference<List<PublicUserDTO>> ptr = new ParameterizedTypeReference<List<PublicUserDTO>>() {
            };
            ResponseEntity<List<PublicUserDTO>> res = rt
                    .withBasicAuth(adminUsername, adminPass)
                    .exchange(url, HttpMethod.GET, null, ptr);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

            assertThat(res.getBody()).hasSize(size);
        }

        @Test
        @DirtiesContext
        void shouldNotListAllUserIsNotAdmin() {
            String failBodyMssg = "You must be an admin to perform this action";
//            UserEntity ue = userRepository.findById(ids.getFirst()).get();
//            later: rt.withBasicAuth(ue.getUsername(), ue.getPassword()) -> X password is encrypted --> this stolen to me a lot of time until I realize it
            ResponseEntity<String> res = rt
                    .withBasicAuth(users.getFirst().getUsername(), users.getFirst().getPassword())
                    .exchange(url, HttpMethod.GET, null, String.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(res.getBody().split("\"")[3]).isEqualToIgnoringCase(failBodyMssg);
        }


    }

}





