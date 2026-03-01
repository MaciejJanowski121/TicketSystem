package de.bachelorarbeit.ticketsystem.repository;

import de.bachelorarbeit.ticketsystem.model.entity.Ticket;
import de.bachelorarbeit.ticketsystem.model.entity.TicketCategory;
import de.bachelorarbeit.ticketsystem.model.entity.TicketState;
import de.bachelorarbeit.ticketsystem.model.entity.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Find all tickets created by a specific end user, sorted by updateDate descending.
     *
     * @param endUser the end user
     * @return a list of tickets created by the end user, sorted by updateDate descending
     */
    List<Ticket> findByEndUserOrderByUpdateDateDesc(UserAccount endUser);

    /**
     * Find all tickets sorted by updateDate descending.
     *
     * @return a list of all tickets sorted by updateDate descending
     */
    List<Ticket> findAllByOrderByUpdateDateDesc();

    /**
     * Find all tickets sorted by updateDate ascending.
     *
     * @return a list of all tickets sorted by updateDate ascending
     */
    List<Ticket> findAllByOrderByUpdateDateAsc();

    /**
     * Find all tickets sorted by createDate descending.
     *
     * @return a list of all tickets sorted by createDate descending
     */
    List<Ticket> findAllByOrderByCreateDateDesc();

    /**
     * Find all tickets sorted by createDate ascending.
     *
     * @return a list of all tickets sorted by createDate ascending
     */
    List<Ticket> findAllByOrderByCreateDateAsc();

    /**
     * Find tickets with state and category filtering (no search), sorted by updateDate descending.
     *
     * @param state optional state filter
     * @param category optional category filter
     * @return list of tickets matching the criteria, sorted by updateDate descending
     */
    @Query("SELECT t FROM Ticket t " +
           "WHERE (:state IS NULL OR t.ticketState = :state) " +
           "AND (:category IS NULL OR t.ticketCategory = :category) " +
           "ORDER BY t.updateDate DESC")
    List<Ticket> findTicketsWithFiltersNoSearchOrderByUpdateDateDesc(@Param("state") TicketState state,
                                                                    @Param("category") TicketCategory category);

    /**
     * Find tickets with state and category filtering (no search), sorted by updateDate ascending.
     *
     * @param state optional state filter
     * @param category optional category filter
     * @return list of tickets matching the criteria, sorted by updateDate ascending
     */
    @Query("SELECT t FROM Ticket t " +
           "WHERE (:state IS NULL OR t.ticketState = :state) " +
           "AND (:category IS NULL OR t.ticketCategory = :category) " +
           "ORDER BY t.updateDate ASC")
    List<Ticket> findTicketsWithFiltersNoSearchOrderByUpdateDateAsc(@Param("state") TicketState state,
                                                                   @Param("category") TicketCategory category);

    /**
     * Find tickets with state and category filtering (no search), sorted by createDate descending.
     *
     * @param state optional state filter
     * @param category optional category filter
     * @return list of tickets matching the criteria, sorted by createDate descending
     */
    @Query("SELECT t FROM Ticket t " +
           "WHERE (:state IS NULL OR t.ticketState = :state) " +
           "AND (:category IS NULL OR t.ticketCategory = :category) " +
           "ORDER BY t.createDate DESC")
    List<Ticket> findTicketsWithFiltersNoSearchOrderByCreateDateDesc(@Param("state") TicketState state,
                                                                    @Param("category") TicketCategory category);

    /**
     * Find tickets with state and category filtering (no search), sorted by createDate ascending.
     *
     * @param state optional state filter
     * @param category optional category filter
     * @return list of tickets matching the criteria, sorted by createDate ascending
     */
    @Query("SELECT t FROM Ticket t " +
           "WHERE (:state IS NULL OR t.ticketState = :state) " +
           "AND (:category IS NULL OR t.ticketCategory = :category) " +
           "ORDER BY t.createDate ASC")
    List<Ticket> findTicketsWithFiltersNoSearchOrderByCreateDateAsc(@Param("state") TicketState state,
                                                                   @Param("category") TicketCategory category);

    /**
     * Find tickets with optional filtering, sorted by updateDate descending.
     *
     * @param search optional search term to match title, description, creator username, or assigned support username
     * @param state optional state filter
     * @param category optional category filter
     * @return list of tickets matching the criteria, sorted by updateDate descending
     */
    @Query("SELECT t FROM Ticket t " +
           "LEFT JOIN t.endUser eu " +
           "LEFT JOIN t.assignedSupport sup " +
           "WHERE (:search IS NULL OR " +
           "       t.title LIKE '%' || :search || '%' OR " +
           "       eu.username LIKE '%' || :search || '%' OR " +
           "       sup.username LIKE '%' || :search || '%') " +
           "AND (:state IS NULL OR t.ticketState = :state) " +
           "AND (:category IS NULL OR t.ticketCategory = :category) " +
           "ORDER BY t.updateDate DESC")
    List<Ticket> findTicketsWithFiltersOrderByUpdateDateDesc(@Param("search") String search,
                                                            @Param("state") TicketState state,
                                                            @Param("category") TicketCategory category);

    /**
     * Find tickets with optional filtering, sorted by updateDate ascending.
     *
     * @param search optional search term to match title, description, creator username, or assigned support username
     * @param state optional state filter
     * @param category optional category filter
     * @return list of tickets matching the criteria, sorted by updateDate ascending
     */
    @Query("SELECT t FROM Ticket t " +
           "LEFT JOIN t.endUser eu " +
           "LEFT JOIN t.assignedSupport sup " +
           "WHERE (:search IS NULL OR " +
           "       t.title LIKE '%' || :search || '%' OR " +
           "       eu.username LIKE '%' || :search || '%' OR " +
           "       sup.username LIKE '%' || :search || '%') " +
           "AND (:state IS NULL OR t.ticketState = :state) " +
           "AND (:category IS NULL OR t.ticketCategory = :category) " +
           "ORDER BY t.updateDate ASC")
    List<Ticket> findTicketsWithFiltersOrderByUpdateDateAsc(@Param("search") String search,
                                                           @Param("state") TicketState state,
                                                           @Param("category") TicketCategory category);

    /**
     * Find tickets with optional filtering, sorted by createDate descending.
     *
     * @param search optional search term to match title, description, creator username, or assigned support username
     * @param state optional state filter
     * @param category optional category filter
     * @return list of tickets matching the criteria, sorted by createDate descending
     */
    @Query("SELECT t FROM Ticket t " +
           "LEFT JOIN t.endUser eu " +
           "LEFT JOIN t.assignedSupport sup " +
           "WHERE (:search IS NULL OR " +
           "       t.title LIKE '%' || :search || '%' OR " +
           "       eu.username LIKE '%' || :search || '%' OR " +
           "       sup.username LIKE '%' || :search || '%') " +
           "AND (:state IS NULL OR t.ticketState = :state) " +
           "AND (:category IS NULL OR t.ticketCategory = :category) " +
           "ORDER BY t.createDate DESC")
    List<Ticket> findTicketsWithFiltersOrderByCreateDateDesc(@Param("search") String search,
                                                            @Param("state") TicketState state,
                                                            @Param("category") TicketCategory category);

    /**
     * Find tickets with optional filtering, sorted by createDate ascending.
     *
     * @param search optional search term to match title, description, creator username, or assigned support username
     * @param state optional state filter
     * @param category optional category filter
     * @return list of tickets matching the criteria, sorted by createDate ascending
     */
    @Query("SELECT t FROM Ticket t " +
           "LEFT JOIN t.endUser eu " +
           "LEFT JOIN t.assignedSupport sup " +
           "WHERE (:search IS NULL OR " +
           "       t.title LIKE '%' || :search || '%' OR " +
           "       eu.username LIKE '%' || :search || '%' OR " +
           "       sup.username LIKE '%' || :search || '%') " +
           "AND (:state IS NULL OR t.ticketState = :state) " +
           "AND (:category IS NULL OR t.ticketCategory = :category) " +
           "ORDER BY t.createDate ASC")
    List<Ticket> findTicketsWithFiltersOrderByCreateDateAsc(@Param("search") String search,
                                                           @Param("state") TicketState state,
                                                           @Param("category") TicketCategory category);
}
