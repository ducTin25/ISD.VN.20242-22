package com.edu.hust.major.service;

import com.edu.hust.major.model.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface UserService {
    List<User> getAllUser();
    void updateUser(User user);
    void removeUserById(int id);
    Optional<User> getUserById(int id);
    Optional<User> getUserByEmail(String email);
}
