package de.bachelorarbeit.ticketsystem.repository;

import de.bachelorarbeit.ticketsystem.model.entity.Ticket;
import de.bachelorarbeit.ticketsystem.model.entity.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Ticket entity.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * Find all tickets created by a specific end user.
     *
     * @param endUser the end user
     * @return a list of tickets created by the end user
     */
    List<Ticket> findByEndUser(UserAccount endUser);

    /**
     * Find all tickets assigned to a specific support user.
     *
     * @param assignedSupport the support user
     * @return a list of tickets assigned to the support user
     */
    List<Ticket> findByAssignedSupport(UserAccount assignedSupport);

    /**
     * Find all tickets created by a specific end user with pagination.
     *
     * @param endUser the end user
     * @param pageable pagination information
     * @return a page of tickets created by the end user
     */
    Page<Ticket> findByEndUser(UserAccount endUser, Pageable pageable);

    /**
     * Find all tickets assigned to a specific support user with pagination.
     *
     * @param assignedSupport the support user
     * @param pageable pagination information
     * @return a page of tickets assigned to the support user
     */
    Page<Ticket> findByAssignedSupport(UserAccount assignedSupport, Pageable pageable);

    /**
     * Find all unassigned tickets (tickets with no assigned support user).
     *
     * @return a list of unassigned tickets
     */
    List<Ticket> findByAssignedSupportIsNull();

    /**
     * Find all unassigned tickets with pagination.
     *
     * @param pageable pagination information
     * @return a page of unassigned tickets
     */
    Page<Ticket> findByAssignedSupportIsNull(Pageable pageable);
}