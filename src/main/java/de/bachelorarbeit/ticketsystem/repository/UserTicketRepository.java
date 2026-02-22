package de.bachelorarbeit.ticketsystem.repository;

import de.bachelorarbeit.ticketsystem.model.entity.Ticket;
import de.bachelorarbeit.ticketsystem.model.entity.UserAccount;
import de.bachelorarbeit.ticketsystem.model.entity.UserTicket;
import de.bachelorarbeit.ticketsystem.model.entity.UserTicketPk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for UserTicket entity.
 */
@Repository
public interface UserTicketRepository extends JpaRepository<UserTicket, UserTicketPk> {

    /**
     * Find a user ticket by ticket and end user.
     *
     * @param ticket the ticket
     * @param endUser the end user
     * @return an Optional containing the user ticket if found, or empty if not found
     */
    Optional<UserTicket> findByTicketAndEndUser(Ticket ticket, UserAccount endUser);

    /**
     * Find all user tickets for a specific ticket.
     *
     * @param ticket the ticket
     * @return a list of user tickets for the specified ticket
     */
    List<UserTicket> findByTicket(Ticket ticket);

    /**
     * Find all user tickets for a specific end user.
     *
     * @param endUser the end user
     * @return a list of user tickets for the specified end user
     */
    List<UserTicket> findByEndUser(UserAccount endUser);

    /**
     * Delete all user tickets for a specific ticket.
     *
     * @param ticket the ticket
     */
    void deleteByTicket(Ticket ticket);

    /**
     * Delete all user tickets for a specific end user.
     *
     * @param endUser the end user
     */
    void deleteByEndUser(UserAccount endUser);
}