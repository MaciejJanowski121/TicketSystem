package de.bachelorarbeit.ticketsystem.service;

import de.bachelorarbeit.ticketsystem.dto.UserResponse;
import de.bachelorarbeit.ticketsystem.model.entity.Role;
import de.bachelorarbeit.ticketsystem.model.entity.UserAccount;
import de.bachelorarbeit.ticketsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserAccount endUser;
    private UserAccount supportUser;
    private UserAccount adminUser;

    @BeforeEach
    void setUp() {
        endUser = new UserAccount("enduser", "end@example.com", "hash1", Role.ENDUSER);
        supportUser = new UserAccount("supportuser", "support@example.com", "hash2", Role.SUPPORTUSER);
        adminUser = new UserAccount("adminuser", "admin@example.com", "hash3", Role.ADMINUSER);
    }

    @Test
    void testGetAllUsersSuccess() {
        // Given
        List<UserAccount> users = Arrays.asList(endUser, supportUser, adminUser);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<UserResponse> result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        
        // Verify first user
        UserResponse firstUser = result.get(0);
        assertEquals(endUser.getMail(), firstUser.getMail());
        assertEquals(endUser.getUsername(), firstUser.getUsername());
        assertEquals(endUser.getRole(), firstUser.getRole());
        
        // Verify second user
        UserResponse secondUser = result.get(1);
        assertEquals(supportUser.getMail(), secondUser.getMail());
        assertEquals(supportUser.getUsername(), secondUser.getUsername());
        assertEquals(supportUser.getRole(), secondUser.getRole());
        
        // Verify third user
        UserResponse thirdUser = result.get(2);
        assertEquals(adminUser.getMail(), thirdUser.getMail());
        assertEquals(adminUser.getUsername(), thirdUser.getUsername());
        assertEquals(adminUser.getRole(), thirdUser.getRole());

        verify(userRepository).findAll();
    }

    @Test
    void testGetAllUsersEmptyList() {
        // Given
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<UserResponse> result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void testUpdateUserRoleSuccess() {
        // Given
        String email = "end@example.com";
        Role newRole = Role.SUPPORTUSER;
        UserAccount updatedUser = new UserAccount(endUser.getUsername(), endUser.getMail(), endUser.getPasswordHash(), newRole);

        when(userRepository.findByMail(email)).thenReturn(Optional.of(endUser));
        when(userRepository.save(any(UserAccount.class))).thenReturn(updatedUser);

        // When
        UserResponse result = userService.updateUserRole(email, newRole);

        // Then
        assertNotNull(result);
        assertEquals(email, result.getMail());
        assertEquals(endUser.getUsername(), result.getUsername());
        assertEquals(newRole, result.getRole());

        verify(userRepository).findByMail(email);
        verify(userRepository).save(endUser);
        assertEquals(newRole, endUser.getRole()); // Verify the role was actually set on the entity
    }

    @Test
    void testUpdateUserRoleUserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        Role newRole = Role.SUPPORTUSER;

        when(userRepository.findByMail(email)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> userService.updateUserRole(email, newRole));

        assertEquals("User not found with email: " + email, exception.getMessage());
        verify(userRepository).findByMail(email);
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateUserRoleToEndUser() {
        // Given
        String email = "support@example.com";
        Role newRole = Role.ENDUSER;
        UserAccount updatedUser = new UserAccount(supportUser.getUsername(), supportUser.getMail(), supportUser.getPasswordHash(), newRole);

        when(userRepository.findByMail(email)).thenReturn(Optional.of(supportUser));
        when(userRepository.save(any(UserAccount.class))).thenReturn(updatedUser);

        // When
        UserResponse result = userService.updateUserRole(email, newRole);

        // Then
        assertNotNull(result);
        assertEquals(email, result.getMail());
        assertEquals(supportUser.getUsername(), result.getUsername());
        assertEquals(newRole, result.getRole());

        verify(userRepository).findByMail(email);
        verify(userRepository).save(supportUser);
        assertEquals(newRole, supportUser.getRole());
    }

    @Test
    void testUpdateUserRoleToAdminUser() {
        // Given
        String email = "end@example.com";
        Role newRole = Role.ADMINUSER;
        UserAccount updatedUser = new UserAccount(endUser.getUsername(), endUser.getMail(), endUser.getPasswordHash(), newRole);

        when(userRepository.findByMail(email)).thenReturn(Optional.of(endUser));
        when(userRepository.save(any(UserAccount.class))).thenReturn(updatedUser);

        // When
        UserResponse result = userService.updateUserRole(email, newRole);

        // Then
        assertNotNull(result);
        assertEquals(email, result.getMail());
        assertEquals(endUser.getUsername(), result.getUsername());
        assertEquals(newRole, result.getRole());

        verify(userRepository).findByMail(email);
        verify(userRepository).save(endUser);
        assertEquals(newRole, endUser.getRole());
    }

    @Test
    void testUpdateUserRoleToSameRole() {
        // Given
        String email = "end@example.com";
        Role currentRole = Role.ENDUSER;
        UserAccount updatedUser = new UserAccount(endUser.getUsername(), endUser.getMail(), endUser.getPasswordHash(), currentRole);

        when(userRepository.findByMail(email)).thenReturn(Optional.of(endUser));
        when(userRepository.save(any(UserAccount.class))).thenReturn(updatedUser);

        // When
        UserResponse result = userService.updateUserRole(email, currentRole);

        // Then
        assertNotNull(result);
        assertEquals(email, result.getMail());
        assertEquals(endUser.getUsername(), result.getUsername());
        assertEquals(currentRole, result.getRole());

        verify(userRepository).findByMail(email);
        verify(userRepository).save(endUser);
        assertEquals(currentRole, endUser.getRole());
    }
}