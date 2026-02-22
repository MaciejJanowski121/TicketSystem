package de.bachelorarbeit.ticketsystem.repository;

import de.bachelorarbeit.ticketsystem.model.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<UserAccount, String> {

    /**
     * Find a user by username.
     *
     * @param username the username to search for
     * @return an Optional containing the user if found, or empty if not found
     */
    Optional<UserAccount> findByUsername(String username);

    /**
     * Find a user by email (mail field, which is the primary key).
     *
     * @param mail the email to search for
     * @return an Optional containing the user if found, or empty if not found
     */
    Optional<UserAccount> findByMail(String mail);

    /**
     * Check if a user with the given username exists.
     *
     * @param username the username to check
     * @return true if a user with the username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Check if a user with the given email exists.
     *
     * @param mail the email to check
     * @return true if a user with the email exists, false otherwise
     */
    boolean existsByMail(String mail);
}
