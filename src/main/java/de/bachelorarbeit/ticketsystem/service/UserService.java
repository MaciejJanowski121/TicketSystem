package de.bachelorarbeit.ticketsystem.service;

import de.bachelorarbeit.ticketsystem.dto.UserResponse;
import de.bachelorarbeit.ticketsystem.model.entity.Role;
import de.bachelorarbeit.ticketsystem.model.entity.UserAccount;
import de.bachelorarbeit.ticketsystem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for user management operations.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Get all users in the system.
     *
     * @return list of all users as UserResponse DTOs
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update a user's role.
     *
     * @param mail the email of the user to update
     * @param newRole the new role to assign
     * @return the updated user as UserResponse DTO
     * @throws IllegalArgumentException if user not found
     */
    @Transactional
    public UserResponse updateUserRole(String mail, Role newRole) {
        UserAccount user = userRepository.findByMail(mail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + mail));

        user.setRole(newRole);
        UserAccount savedUser = userRepository.save(user);

        return mapToUserResponse(savedUser);
    }

    /**
     * Map UserAccount entity to UserResponse DTO.
     *
     * @param user the user entity
     * @return the user response DTO
     */
    private UserResponse mapToUserResponse(UserAccount user) {
        return new UserResponse(
                user.getMail(),
                user.getUsername(),
                user.getRole()
        );
    }
}