package com.proposal.Nature.Heaven.service;

import com.proposal.Nature.Heaven.DTO.UserRegistrationDto;
import com.proposal.Nature.Heaven.model.User;
import com.proposal.Nature.Heaven.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public User registerUser(UserRegistrationDto registrationDto, String role) {
        // Check if the username or email already exists
        if (userRepository.findByUsername(registrationDto.getUsername()) != null) {
            throw new RuntimeException("Username already taken");
        }
        if (userRepository.findByEmail(registrationDto.getEmail()) != null) {
            throw new RuntimeException("Email already taken");
        }

        // Create a new User object
        User user = new User();
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

        // Ensure the role has the correct prefix
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role.toUpperCase();
        }
// Validate that the role is either ROLE_USER or ROLE_ADMIN
        try {
            user.setRole(User.Role.valueOf(role)); // Directly maps to enum
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role specified");
        }

        // Save the user to the database
        return userRepository.save(user);
    }
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    public List<User> findUsersByUsername(String username) {
        return userRepository.findByUsernameContaining(username); // Use a method in repository
    }
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
    public Set<String> getDefaultRoles() {
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        return roles;
    }
    // Method to get all users with ROLE_USER
    public List<User> getAllUsersWithRoleUser() {
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream()
                .filter(user -> user.getRole() == User.Role.ROLE_USER)
                .collect(Collectors.toList());
    }

}