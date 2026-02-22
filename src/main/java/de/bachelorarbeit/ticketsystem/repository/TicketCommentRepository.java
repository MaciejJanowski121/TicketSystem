package de.bachelorarbeit.ticketsystem.repository;

import de.bachelorarbeit.ticketsystem.model.entity.Ticket;
import de.bachelorarbeit.ticketsystem.model.entity.TicketComment;
import de.bachelorarbeit.ticketsystem.model.entity.TicketCommentPk;
import de.bachelorarbeit.ticketsystem.model.entity.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TicketComment entity.
 */
@Repository
public interface TicketCommentRepository extends JpaRepository<TicketComment, TicketCommentPk> {

    /**
     * Find all comments for a specific ticket.
     *
     * @param ticket the ticket
     * @return a list of comments for the specified ticket
     */
    List<TicketComment> findByTicket(Ticket ticket);

    /**
     * Find all comments by a specific user.
     *
     * @param commentUser the user who made the comments
     * @return a list of comments made by the specified user
     */
    List<TicketComment> findByCommentUser(UserAccount commentUser);

    /**
     * Find all comments for a specific ticket with pagination.
     *
     * @param ticket the ticket
     * @param pageable pagination information
     * @return a page of comments for the specified ticket
     */
    Page<TicketComment> findByTicket(Ticket ticket, Pageable pageable);

    /**
     * Find all comments by a specific user with pagination.
     *
     * @param commentUser the user who made the comments
     * @param pageable pagination information
     * @return a page of comments made by the specified user
     */
    Page<TicketComment> findByCommentUser(UserAccount commentUser, Pageable pageable);

    /**
     * Delete all comments for a specific ticket.
     *
     * @param ticket the ticket
     */
    void deleteByTicket(Ticket ticket);

    /**
     * Delete all comments by a specific user.
     *
     * @param commentUser the user who made the comments
     */
    void deleteByCommentUser(UserAccount commentUser);
}