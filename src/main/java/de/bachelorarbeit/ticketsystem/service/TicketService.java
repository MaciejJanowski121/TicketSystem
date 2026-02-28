package de.bachelorarbeit.ticketsystem.service;

import de.bachelorarbeit.ticketsystem.dto.CreateCommentRequest;
import de.bachelorarbeit.ticketsystem.dto.CreateTicketRequest;
import de.bachelorarbeit.ticketsystem.dto.CommentResponse;
import de.bachelorarbeit.ticketsystem.dto.TicketResponse;
import de.bachelorarbeit.ticketsystem.model.entity.Role;
import de.bachelorarbeit.ticketsystem.model.entity.Ticket;
import de.bachelorarbeit.ticketsystem.model.entity.TicketComment;
import de.bachelorarbeit.ticketsystem.model.entity.TicketState;
import de.bachelorarbeit.ticketsystem.model.entity.UserAccount;
import de.bachelorarbeit.ticketsystem.repository.TicketCommentRepository;
import de.bachelorarbeit.ticketsystem.repository.TicketRepository;
import de.bachelorarbeit.ticketsystem.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for ticket operations.
 */
@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketCommentRepository ticketCommentRepository;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository, 
                        TicketCommentRepository ticketCommentRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.ticketCommentRepository = ticketCommentRepository;
    }

    /**
     * Create a new ticket.
     *
     * @param request the ticket creation request
     * @param authentication the authentication object containing current user info
     * @return the created ticket as TicketResponse
     * @throws IllegalArgumentException if user not found
     */
    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request, Authentication authentication) {
        // Determine current user identity from authentication.getName()
        String userIdentifier = authentication.getName();

        // Load UserAccount based on whether name contains '@' (email) or not (username)
        UserAccount endUser;
        if (userIdentifier.contains("@")) {
            endUser = userRepository.findByMail(userIdentifier)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        } else {
            endUser = userRepository.findByUsername(userIdentifier)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        }

        // Create Ticket using existing constructor
        Ticket ticket = new Ticket(
                request.getTitle(),
                request.getDescription(),
                request.getTicketCategory(),
                endUser
        );

        // Save ticket with TicketRepository
        Ticket savedTicket = ticketRepository.save(ticket);

        // Map saved entity to TicketResponse and return
        return new TicketResponse(
                savedTicket.getTicketId(),
                savedTicket.getTitle(),
                savedTicket.getDescription(),
                savedTicket.getTicketState(),
                savedTicket.getTicketCategory(),
                savedTicket.getCreateDate(),
                savedTicket.getUpdateDate(),
                savedTicket.getAssignedSupport() != null ? savedTicket.getAssignedSupport().getUsername() : null
        );
    }

    /**
     * Get all tickets for the current authenticated user.
     *
     * @param authentication the authentication object containing current user info
     * @return list of tickets created by the current user, sorted by updateDate descending
     * @throws IllegalArgumentException if user not found or authentication is null
     */
    @Transactional(readOnly = true)
    public List<TicketResponse> getMyTickets(Authentication authentication) {
        // Defensive check for missing authentication
        if (authentication == null) {
            throw new IllegalArgumentException("Authentication is required");
        }

        UserAccount currentUser = getCurrentUser(authentication);

        // Get tickets sorted by updateDate descending using repository-level sorting
        List<Ticket> tickets = ticketRepository.findByEndUserOrderByUpdateDateDesc(currentUser);

        return tickets.stream()
                .map(this::mapToTicketResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific ticket by ID for the current authenticated user.
     *
     * @param ticketId the ID of the ticket to retrieve
     * @param authentication the authentication object containing current user info
     * @return the ticket details
     * @throws IllegalArgumentException if user not found or ticket not found or not owned by user
     */
    @Transactional(readOnly = true)
    public TicketResponse getMyTicketById(Long ticketId, Authentication authentication) {
        UserAccount currentUser = getCurrentUser(authentication);

        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found");
        }

        Ticket ticket = ticketOpt.get();

        // Check if the ticket belongs to the current user
        if (!ticket.getEndUser().equals(currentUser)) {
            throw new IllegalArgumentException("Access denied: Ticket does not belong to current user");
        }

        return mapToTicketResponse(ticket);
    }

    /**
     * Get current user from authentication.
     *
     * @param authentication the authentication object
     * @return the current user
     * @throws IllegalArgumentException if user not found
     */
    private UserAccount getCurrentUser(Authentication authentication) {
        String userIdentifier = authentication.getName();

        if (userIdentifier.contains("@")) {
            return userRepository.findByMail(userIdentifier)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        } else {
            return userRepository.findByUsername(userIdentifier)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        }
    }

    /**
     * Map Ticket entity to TicketResponse DTO.
     *
     * @param ticket the ticket entity
     * @return the ticket response DTO
     */
    private TicketResponse mapToTicketResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getTicketId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getTicketState(),
                ticket.getTicketCategory(),
                ticket.getCreateDate(),
                ticket.getUpdateDate(),
                ticket.getAssignedSupport() != null ? ticket.getAssignedSupport().getUsername() : null
        );
    }

    /**
     * Get all comments for a specific ticket.
     *
     * @param ticketId the ID of the ticket
     * @param authentication the authentication object containing current user info
     * @return list of comments for the ticket
     * @throws IllegalArgumentException if ticket not found or access denied
     */
    @Transactional(readOnly = true)
    public List<CommentResponse> getTicketComments(Long ticketId, Authentication authentication) {
        UserAccount currentUser = getCurrentUser(authentication);

        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found");
        }

        Ticket ticket = ticketOpt.get();

        // Authorization: ENDUSER can only access own tickets, SUPPORTUSER and ADMINUSER can access any ticket
        if (currentUser.getRole() == Role.ENDUSER && !ticket.getEndUser().equals(currentUser)) {
            throw new SecurityException("Access denied: You can only view comments on your own tickets");
        }

        List<TicketComment> comments = ticketCommentRepository.findByTicket(ticket);

        return comments.stream()
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create a new comment on a ticket.
     *
     * @param ticketId the ID of the ticket
     * @param request the comment creation request
     * @param authentication the authentication object containing current user info
     * @return the created comment as CommentResponse
     * @throws IllegalArgumentException if ticket not found or access denied
     */
    @Transactional
    public CommentResponse createTicketComment(Long ticketId, CreateCommentRequest request, Authentication authentication) {
        UserAccount currentUser = getCurrentUser(authentication);

        // Validate ticket exists and get it within the same transaction
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found");
        }

        Ticket ticket = ticketOpt.get();

        // Authorization: ENDUSER can only comment on own tickets, SUPPORTUSER and ADMINUSER can comment on any ticket
        if (currentUser.getRole() == Role.ENDUSER && !ticket.getEndUser().equals(currentUser)) {
            throw new SecurityException("Access denied: You can only comment on your own tickets");
        }

        try {
            // Create and save the comment
            TicketComment comment = new TicketComment(ticket, currentUser, request.getComment());
            TicketComment savedComment = ticketCommentRepository.save(comment);

            // Update ticket's updateDate (this will be handled automatically by @PreUpdate in Ticket entity)
            ticket.setUpdateDate(java.time.Instant.now());
            ticketRepository.save(ticket);

            return mapToCommentResponse(savedComment);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Handle foreign key constraint violation
            if (e.getMessage() != null && e.getMessage().contains("ticket_id")) {
                throw new IllegalArgumentException("Ticket not found or has been deleted");
            }
            throw e; // Re-throw if it's a different constraint violation
        } catch (org.springframework.dao.InvalidDataAccessApiUsageException e) {
            // Handle Hibernate TransientPropertyValueException when ticket becomes transient
            if (e.getMessage() != null && e.getMessage().contains("transient instance must be saved")) {
                throw new IllegalArgumentException("Ticket not found or has been deleted");
            }
            throw e; // Re-throw if it's a different usage exception
        }
    }

    /**
     * Map TicketComment entity to CommentResponse DTO.
     *
     * @param comment the ticket comment entity
     * @return the comment response DTO
     */
    private CommentResponse mapToCommentResponse(TicketComment comment) {
        return new CommentResponse(
                comment.getTc_pk().getTicketId(),
                comment.getCommentUser().getMail(),
                comment.getCommentUser().getUsername(),
                comment.getCommentDate(),
                comment.getComment()
        );
    }
}
