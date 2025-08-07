package com.bank.gateway.controller;

import com.bank.gateway.dto.UserRequest;
import com.bank.gateway.entity.User;
import com.bank.gateway.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // POST /api/users/create
    @PostMapping("/create")
    public ResponseEntity<User> createUser(@RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    // PUT /api/users/{userId}/status
    @PutMapping("/{userId}/status")
    public ResponseEntity<User> updateStatus(@PathVariable Long userId, @RequestParam boolean enabled) {
        return ResponseEntity.ok(userService.updateUserStatus(userId, enabled));
    }

    // GET /api/users/{userId}
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    // GET /api/users/by-username?username=customer_123456789012
    @GetMapping("/by-username")
    public ResponseEntity<User> getByUsername(@RequestParam String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    // DELETE /api/users/{userId}
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
