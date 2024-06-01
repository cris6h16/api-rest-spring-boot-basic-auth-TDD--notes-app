package org.cris6h16.apirestspringboot.Controllers;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Controllers.MetaAnnotations.MyId;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.controller.UserControllerTransversalException;
import org.cris6h16.apirestspringboot.Service.UserServiceImpl;
import org.cris6h16.apirestspringboot.DTOs.CreateUpdateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

//@RestController
@Controller
@ResponseBody
@RequestMapping(UserController.path)
public class UserController {
    public static final String path = "/api/users";

    UserServiceImpl userService;

    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }


    @PostMapping(consumes = "application/json")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> create(@RequestBody CreateUpdateUserDTO user) {
        Long id = userService.create(user);
        URI uri = URI.create(path + "/" + id);
        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PublicUserDTO> get(@PathVariable Long id,
                                             @MyId Long principalId) {
        verifyOwnership(id, principalId);
        PublicUserDTO u = userService.get(id);
        return ResponseEntity.ok(u);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> update(@PathVariable Long id,
                                       @RequestBody CreateUpdateUserDTO dto,
                                       @MyId Long principalId) {//TODO: Impl boudary cases for all @CONTROLLERS
        verifyOwnership(id, principalId);
        userService.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @MyId Long principalId) {
        verifyOwnership(id, principalId);
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole(T(org.cris6h16.apirestspringboot.Entities.ERole).ROLE_ADMIN)")
    public ResponseEntity<List<PublicUserDTO>> getUsers(Pageable pageable) {
        List<PublicUserDTO> l = userService.get(pageable);
        return ResponseEntity.ok(l);
    }

    void verifyOwnership(Long id, Long principalId) {
        if (!id.equals(principalId)) {
            throw new UserControllerTransversalException(Cons.Auth.Fails.IS_NOT_YOUR_ID_MSG, HttpStatus.FORBIDDEN);
        }
    }
}
