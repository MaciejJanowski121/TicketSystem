package de.bachelorarbeit.ticketsystem.repository;

import de.bachelorarbeit.ticketsystem.model.entity.SupportTicketAssignment;
import de.bachelorarbeit.ticketsystem.model.entity.Ticket;
import de.bachelorarbeit.ticketsystem.model.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SupportTicketAssignment entity.
 */
@Repository
public interface SupportTicketAssignmentRepository extends JpaRepository<SupportTicketAssignment, Long> {

    /**
     * Find a support ticket assignment by ticket and support user.
     *
     * @param ticket the ticket
     * @param supportUser the support user
     * @return an Optional containing the support ticket assignment if found, or empty if not found
     */
    Optional<SupportTicketAssignment> findByTicketAndSupportUser(Ticket ticket, UserAccount supportUser);

    /**
     * Find the support ticket assignment for a specific ticket.
     *
     * @param ticket the ticket
     * @return an Optional containing the support ticket assignment if found, or empty if not found
     */
    Optional<SupportTicketAssignment> findByTicket(Ticket ticket);

    /**
     * Find all support ticket assignments for a specific support user.
     *
     * @param supportUser the support user
     * @return a list of support ticket assignments for the specified support user
     */
    List<SupportTicketAssignment> findBySupportUser(UserAccount supportUser);

    /**
     * Check if a support ticket assignment exists for a specific ticket.
     *
     * @param ticket the ticket
     * @return true if an assignment exists, false otherwise
     */
    boolean existsByTicket(Ticket ticket);

    /**
     * Delete all support ticket assignments for a specific ticket.
     *
     * @param ticket the ticket
     */
    void deleteByTicket(Ticket ticket);

    /**
     * Delete all support ticket assignments for a specific support user.
     *
     * @param supportUser the support user
     */
    void deleteBySupportUser(UserAccount supportUser);

}
