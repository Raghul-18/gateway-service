package com.bank.gateway.service;

import com.bank.gateway.dto.UserRequest;
import com.bank.gateway.entity.User;

public interface UserService {
    User createUser(UserRequest request);
    User updateUserStatus(Long userId, boolean enabled);
    User getUserById(Long userId);
    User getUserByUsername(String username);
    void deleteUser(Long userId);
}
