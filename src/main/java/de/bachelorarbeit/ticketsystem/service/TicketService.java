package de.bachelorarbeit.ticketsystem.service;

import de.bachelorarbeit.ticketsystem.dto.CreateCommentRequest;
import de.bachelorarbeit.ticketsystem.dto.CreateTicketRequest;
import de.bachelorarbeit.ticketsystem.dto.SupportTicketUpdateRequest;
import de.bachelorarbeit.ticketsystem.dto.TicketCommentResponse;
import de.bachelorarbeit.ticketsystem.dto.TicketResponse;
import de.bachelorarbeit.ticketsystem.dto.TicketListItemResponse;
import de.bachelorarbeit.ticketsystem.model.entity.Role;
import de.bachelorarbeit.ticketsystem.model.entity.SupportTicketAssignment;
import de.bachelorarbeit.ticketsystem.model.entity.Ticket;
import de.bachelorarbeit.ticketsystem.model.entity.TicketCategory;
import de.bachelorarbeit.ticketsystem.model.entity.TicketComment;
import de.bachelorarbeit.ticketsystem.model.entity.TicketState;
import de.bachelorarbeit.ticketsystem.model.entity.UserAccount;
import de.bachelorarbeit.ticketsystem.model.entity.UserTicket;
import de.bachelorarbeit.ticketsystem.model.entity.UserTicketPk;
import de.bachelorarbeit.ticketsystem.repository.SupportTicketAssignmentRepository;
import de.bachelorarbeit.ticketsystem.repository.TicketCommentRepository;
import de.bachelorarbeit.ticketsystem.repository.TicketRepository;
import de.bachelorarbeit.ticketsystem.repository.UserRepository;
import de.bachelorarbeit.ticketsystem.repository.UserTicketRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
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
    private final SupportTicketAssignmentRepository supportTicketAssignmentRepository;
    private final UserTicketRepository userTicketRepository;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository,
                        TicketCommentRepository ticketCommentRepository,
                        SupportTicketAssignmentRepository supportTicketAssignmentRepository,
                        UserTicketRepository userTicketRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.ticketCommentRepository = ticketCommentRepository;
        this.supportTicketAssignmentRepository = supportTicketAssignmentRepository;
        this.userTicketRepository = userTicketRepository;
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

        // Create UserTicket for the creator so the ticket is not unread for them
        // Use savedTicket.getUpdateDate() as lastViewed to ensure it's not considered unread
        UserTicket userTicket = new UserTicket(savedTicket, endUser);
        userTicket.setLastViewed(savedTicket.getUpdateDate());
        userTicketRepository.save(userTicket);

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
     * @return list of tickets created by the current user, sorted by unread first, then updateDate descending
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

        // Get all UserTicket records for this user
        List<UserTicket> userTickets = userTicketRepository.findByEndUser(currentUser);

        // Create a map for quick lookup of UserTicket by ticket ID
        Map<Long, UserTicket> userTicketMap = userTickets.stream()
                .collect(Collectors.toMap(ut -> ut.getTicket().getTicketId(), ut -> ut));

        // Map tickets to responses with unread status and sort by unread first, then updateDate DESC
        return tickets.stream()
                .map(ticket -> mapToTicketResponseWithUnread(ticket, userTicketMap.get(ticket.getTicketId())))
                .sorted((a, b) -> {
                    // First, sort by unread status (unread first)
                    if (a.isUnread() != b.isUnread()) {
                        return a.isUnread() ? -1 : 1; // unread tickets first
                    }
                    // Then sort by updateDate descending
                    return b.getUpdateDate().compareTo(a.getUpdateDate());
                })
                .collect(Collectors.toList());
    }

    /**
     * Get a specific ticket by ID for the current authenticated user.
     * Updates the lastViewed timestamp for the user.
     *
     * @param ticketId the ID of the ticket to retrieve
     * @param authentication the authentication object containing current user info
     * @return the ticket details
     * @throws IllegalArgumentException if user not found or ticket not found or not owned by user
     */
    @Transactional
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

        // Update or create UserTicket record to track lastViewed
        // Use existsById with EmbeddedId for idempotent upsert logic
        UserTicketPk userTicketPk = new UserTicketPk(ticketId, currentUser.getMail());
        if (userTicketRepository.existsById(userTicketPk)) {
            // Load existing UserTicket and update lastViewed
            UserTicket userTicket = userTicketRepository.findById(userTicketPk).get();
            userTicket.updateLastViewed();
            userTicketRepository.save(userTicket);
        } else {
            // Create new UserTicket with proper PK and entity fields
            UserTicket userTicket = new UserTicket(ticket, currentUser);
            userTicketRepository.save(userTicket);
        }

        return mapToTicketResponse(ticket);
    }


    /**
     * Get all tickets with optional filtering and sorting.
     *
     * @param search optional search term to match title, description, creator username, or assigned support username
     * @param state optional state filter
     * @param category optional category filter
     * @param sort sort field (createDate or updateDate)
     * @param direction sort direction (ASC or DESC)
     * @param authentication the authentication object containing current user info
     * @return list of tickets matching the criteria
     */
    @Transactional(readOnly = true)
    public List<TicketListItemResponse> getAllTickets(String search, TicketState state, TicketCategory category,
                                                     String sort, String direction, Authentication authentication) {
        // Verify user is authenticated (this method is accessible to all authenticated users)
        getCurrentUser(authentication);

        // Validate sort field
        if (sort == null || (!sort.equals("createDate") && !sort.equals("updateDate"))) {
            sort = "updateDate";
        }

        // Validate direction
        if (direction == null || (!direction.equals("ASC") && !direction.equals("DESC"))) {
            direction = "DESC";
        }

        // Get tickets with filters and sorting based on sort field and direction
        List<Ticket> tickets;

        // Check if search is null or blank - use non-search methods to avoid PostgreSQL bytea issues
        boolean hasSearch = search != null && !search.isBlank();

        if ("createDate".equals(sort)) {
            if ("ASC".equals(direction)) {
                if (hasSearch) {
                    tickets = ticketRepository.findTicketsWithFiltersOrderByCreateDateAsc(search, state, category);
                } else {
                    // No search - use simple filtering or findAll if no filters
                    if (state == null && category == null) {
                        tickets = ticketRepository.findAllByOrderByCreateDateAsc();
                    } else {
                        tickets = ticketRepository.findTicketsWithFiltersNoSearchOrderByCreateDateAsc(state, category);
                    }
                }
            } else {
                if (hasSearch) {
                    tickets = ticketRepository.findTicketsWithFiltersOrderByCreateDateDesc(search, state, category);
                } else {
                    // No search - use simple filtering or findAll if no filters
                    if (state == null && category == null) {
                        tickets = ticketRepository.findAllByOrderByCreateDateDesc();
                    } else {
                        tickets = ticketRepository.findTicketsWithFiltersNoSearchOrderByCreateDateDesc(state, category);
                    }
                }
            }
        } else { // updateDate (default)
            if ("ASC".equals(direction)) {
                if (hasSearch) {
                    tickets = ticketRepository.findTicketsWithFiltersOrderByUpdateDateAsc(search, state, category);
                } else {
                    // No search - use simple filtering or findAll if no filters
                    if (state == null && category == null) {
                        tickets = ticketRepository.findAllByOrderByUpdateDateAsc();
                    } else {
                        tickets = ticketRepository.findTicketsWithFiltersNoSearchOrderByUpdateDateAsc(state, category);
                    }
                }
            } else {
                if (hasSearch) {
                    tickets = ticketRepository.findTicketsWithFiltersOrderByUpdateDateDesc(search, state, category);
                } else {
                    // No search - use simple filtering or findAll if no filters
                    if (state == null && category == null) {
                        tickets = ticketRepository.findAllByOrderByUpdateDateDesc();
                    } else {
                        tickets = ticketRepository.findTicketsWithFiltersNoSearchOrderByUpdateDateDesc(state, category);
                    }
                }
            }
        }

        return tickets.stream()
                .map(this::mapToTicketListItemResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific ticket by ID for any authenticated user.
     * Updates lastViewed timestamp for SUPPORT/ADMIN users if they are assigned to the ticket.
     *
     * @param ticketId the ID of the ticket to retrieve
     * @param authentication the authentication object containing current user info
     * @return the ticket details
     * @throws IllegalArgumentException if ticket not found
     */
    @Transactional
    public TicketResponse getTicketById(Long ticketId, Authentication authentication) {
        UserAccount currentUser = getCurrentUser(authentication);

        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found");
        }

        Ticket ticket = ticketOpt.get();

        // Update lastViewed for SUPPORT/ADMIN users if they are assigned to this ticket
        if (currentUser.getRole() == Role.SUPPORTUSER || currentUser.getRole() == Role.ADMINUSER) {
            Optional<SupportTicketAssignment> assignmentOpt = supportTicketAssignmentRepository.findByTicketAndSupportUser(ticket, currentUser);
            if (assignmentOpt.isPresent()) {
                SupportTicketAssignment assignment = assignmentOpt.get();
                assignment.updateLastViewed();
                supportTicketAssignmentRepository.save(assignment);
            }
        }

        // Fetch comments for the ticket within the transaction
        List<TicketComment> comments = ticketCommentRepository.findByTicket(ticket);

        // Map comments to TicketCommentResponse DTOs
        List<TicketCommentResponse> commentResponses = comments.stream()
                .map(this::mapToTicketCommentResponse)
                .collect(Collectors.toList());

        // Map to response DTO with all details including creator info within the transaction
        TicketResponse response = mapToTicketResponseWithAllDetails(ticket);

        // Set comments in the response (empty list if no comments)
        response.setComments(commentResponses);

        return response;
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
     * Map Ticket entity to TicketResponse DTO with creator username.
     *
     * @param ticket the ticket entity
     * @return the ticket response DTO with creator username
     */
    private TicketResponse mapToTicketResponseWithCreator(Ticket ticket) {
        return new TicketResponse(
                ticket.getTicketId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getTicketState(),
                ticket.getTicketCategory(),
                ticket.getCreateDate(),
                ticket.getUpdateDate(),
                ticket.getAssignedSupport() != null ? ticket.getAssignedSupport().getUsername() : null,
                ticket.getEndUser().getUsername()
        );
    }

    /**
     * Map Ticket entity to TicketResponse DTO with all details including creator info and closedDate.
     *
     * @param ticket the ticket entity
     * @return the ticket response DTO with all details
     */
    private TicketResponse mapToTicketResponseWithAllDetails(Ticket ticket) {
        return new TicketResponse(
                ticket.getTicketId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getTicketState(),
                ticket.getTicketCategory(),
                ticket.getCreateDate(),
                ticket.getUpdateDate(),
                ticket.getClosedDate(),
                ticket.getAssignedSupport() != null ? ticket.getAssignedSupport().getUsername() : null,
                ticket.getEndUser().getUsername(),
                ticket.getEndUser().getMail()
        );
    }

    /**
     * Get all comments for a specific ticket.
     *
     * @param ticketId the ID of the ticket
     * @param authentication the authentication object containing current user info
     * @return list of comments for the ticket
     * @throws IllegalArgumentException if ticket not found
     */
    @Transactional(readOnly = true)
    public List<TicketCommentResponse> getTicketComments(Long ticketId, Authentication authentication) {
        // Verify user is authenticated (this method is accessible to all authenticated users for reading comments)
        getCurrentUser(authentication);

        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found");
        }

        Ticket ticket = ticketOpt.get();

        // No authorization restriction for reading comments - any authenticated user can view comments
        // Only comment creation is restricted by role/ownership in createTicketComment method

        List<TicketComment> comments = ticketCommentRepository.findByTicket(ticket);

        return comments.stream()
                .map(this::mapToTicketCommentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create a new comment on a ticket.
     *
     * @param ticketId the ID of the ticket
     * @param request the comment creation request
     * @param authentication the authentication object containing current user info
     * @return the created comment as TicketCommentResponse
     * @throws IllegalArgumentException if ticket not found or access denied
     */
    @Transactional
    public TicketCommentResponse createTicketComment(Long ticketId, CreateCommentRequest request, Authentication authentication) {
        UserAccount currentUser = getCurrentUser(authentication);

        // Validate ticket exists and get it within the same transaction
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found");
        }

        Ticket ticket = ticketOpt.get();

        // Authorization logic:
        // - ENDUSER can only comment on own tickets
        // - SUPPORTUSER can only comment on tickets assigned to them
        // - ADMINUSER can comment on any ticket
        if (currentUser.getRole() == Role.ENDUSER) {
            // End users can only comment on their own tickets
            if (!ticket.getEndUser().equals(currentUser)) {
                throw new SecurityException("Access denied: You cannot comment on this ticket.");
            }
        } else if (currentUser.getRole() == Role.SUPPORTUSER) {
            // Support users can only comment on tickets assigned to them
            Optional<SupportTicketAssignment> assignment = supportTicketAssignmentRepository.findByTicket(ticket);
            if (assignment.isEmpty() || !assignment.get().getSupportUser().getMail().equals(currentUser.getMail())) {
                throw new SecurityException("Access denied: You can only comment on tickets assigned to you.");
            }
        }
        // ADMINUSER has no restrictions - can comment on any ticket

        try {
            // Create and save the comment
            TicketComment comment = new TicketComment(ticket, currentUser, request.getComment());
            TicketComment savedComment = ticketCommentRepository.save(comment);

            // Update ticket's updateDate (this will be handled automatically by @PreUpdate in Ticket entity)
            ticket.setUpdateDate(java.time.Instant.now());
            ticketRepository.save(ticket);

            return mapToTicketCommentResponse(savedComment);
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
     * Map TicketComment entity to TicketCommentResponse DTO.
     *
     * @param comment the ticket comment entity
     * @return the ticket comment response DTO
     */
    private TicketCommentResponse mapToTicketCommentResponse(TicketComment comment) {
        // Use username if available, otherwise use email
        String authorUsername = comment.getCommentUser().getUsername();
        if (authorUsername == null || authorUsername.trim().isEmpty()) {
            authorUsername = comment.getCommentUser().getMail();
        }

        return new TicketCommentResponse(
                comment.getComment(),
                comment.getCommentDate(),
                authorUsername
        );
    }

    /**
     * Map Ticket entity to TicketListItemResponse DTO.
     *
     * @param ticket the ticket entity
     * @return the ticket list item response DTO
     */
    private TicketListItemResponse mapToTicketListItemResponse(Ticket ticket) {
        return new TicketListItemResponse(
                ticket.getTicketId(),
                ticket.getTitle(),
                ticket.getTicketState(),
                ticket.getTicketCategory(),
                ticket.getCreateDate(),
                ticket.getUpdateDate(),
                ticket.getClosedDate(),
                ticket.getEndUser().getUsername(),
                ticket.getEndUser().getMail(),
                ticket.getAssignedSupport() != null ? ticket.getAssignedSupport().getUsername() : null,
                false // Default unread to false for backward compatibility
        );
    }

    /**
     * Map Ticket entity to TicketListItemResponse DTO with unread status for support users.
     *
     * @param ticket the ticket entity
     * @param assignment the support ticket assignment containing lastViewed info
     * @return the ticket list item response DTO with unread status
     */
    private TicketListItemResponse mapToTicketListItemResponseWithUnread(Ticket ticket, SupportTicketAssignment assignment) {
        boolean unread = ticket.getUpdateDate().isAfter(assignment.getLastViewed());
        return new TicketListItemResponse(
                ticket.getTicketId(),
                ticket.getTitle(),
                ticket.getTicketState(),
                ticket.getTicketCategory(),
                ticket.getCreateDate(),
                ticket.getUpdateDate(),
                ticket.getClosedDate(),
                ticket.getEndUser().getUsername(),
                ticket.getEndUser().getMail(),
                ticket.getAssignedSupport() != null ? ticket.getAssignedSupport().getUsername() : null,
                unread
        );
    }

    /**
     * Map Ticket entity to TicketResponse DTO with unread status for end users.
     *
     * @param ticket the ticket entity
     * @param userTicket the user ticket containing lastViewed info (can be null if never viewed)
     * @return the ticket response DTO with unread status
     */
    private TicketResponse mapToTicketResponseWithUnread(Ticket ticket, UserTicket userTicket) {
        // If no UserTicket record exists, consider it unread
        boolean unread = userTicket == null || ticket.getUpdateDate().isAfter(userTicket.getLastViewed());

        TicketResponse response = new TicketResponse(
                ticket.getTicketId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getTicketState(),
                ticket.getTicketCategory(),
                ticket.getCreateDate(),
                ticket.getUpdateDate(),
                ticket.getAssignedSupport() != null ? ticket.getAssignedSupport().getUsername() : null
        );
        response.setUnread(unread);
        return response;
    }

    // ========== SUPPORT WORKFLOW METHODS ==========

    /**
     * Get tickets assigned to the current support user.
     *
     * @param search optional search term
     * @param state optional state filter
     * @param category optional category filter
     * @param sort sort field (createDate or updateDate)
     * @param direction sort direction (ASC or DESC)
     * @param authentication the authentication object containing current user info
     * @return list of tickets assigned to current support user
     * @throws IllegalArgumentException if user not found or not authorized
     */
    @Transactional(readOnly = true)
    public List<TicketListItemResponse> getMySupportTickets(String search, TicketState state, TicketCategory category,
                                                           String sort, String direction, Authentication authentication) {
        UserAccount currentUser = getCurrentUser(authentication);

        // Verify user has SUPPORTUSER or ADMINUSER role
        if (currentUser.getRole() != Role.SUPPORTUSER && currentUser.getRole() != Role.ADMINUSER) {
            throw new IllegalArgumentException("Access denied: Only support users can access this endpoint");
        }

        // Fetch assignments using assignmentRepository.findBySupportUser(currentUser)
        List<SupportTicketAssignment> assignments = supportTicketAssignmentRepository.findBySupportUser(currentUser);

        // Apply filters if provided
        if (search != null && !search.isBlank()) {
            String searchLower = search.toLowerCase();
            assignments = assignments.stream()
                    .filter(assignment -> {
                        Ticket ticket = assignment.getTicket();
                        return ticket.getTitle().toLowerCase().contains(searchLower) ||
                               ticket.getDescription().toLowerCase().contains(searchLower) ||
                               (ticket.getEndUser().getUsername() != null && ticket.getEndUser().getUsername().toLowerCase().contains(searchLower));
                    })
                    .collect(Collectors.toList());
        }

        if (state != null) {
            assignments = assignments.stream()
                    .filter(assignment -> assignment.getTicket().getTicketState() == state)
                    .collect(Collectors.toList());
        }

        if (category != null) {
            assignments = assignments.stream()
                    .filter(assignment -> assignment.getTicket().getTicketCategory() == category)
                    .collect(Collectors.toList());
        }

        // Apply sorting: unread first, then by specified sort field and direction
        if (sort == null || (!sort.equals("createDate") && !sort.equals("updateDate"))) {
            sort = "updateDate";
        }
        if (direction == null || (!direction.equals("ASC") && !direction.equals("DESC"))) {
            direction = "DESC";
        }

        final String finalSort = sort;
        final String finalDirection = direction;

        assignments.sort((a, b) -> {
            Ticket ticketA = a.getTicket();
            Ticket ticketB = b.getTicket();

            // First, sort by unread status (unread first)
            boolean unreadA = ticketA.getUpdateDate().isAfter(a.getLastViewed());
            boolean unreadB = ticketB.getUpdateDate().isAfter(b.getLastViewed());

            if (unreadA != unreadB) {
                return unreadA ? -1 : 1; // unread tickets first
            }

            // Then sort by the specified field and direction
            if ("createDate".equals(finalSort)) {
                int comparison = ticketA.getCreateDate().compareTo(ticketB.getCreateDate());
                return "ASC".equals(finalDirection) ? comparison : -comparison;
            } else { // updateDate
                int comparison = ticketA.getUpdateDate().compareTo(ticketB.getUpdateDate());
                return "ASC".equals(finalDirection) ? comparison : -comparison;
            }
        });

        return assignments.stream()
                .map(assignment -> mapToTicketListItemResponseWithUnread(assignment.getTicket(), assignment))
                .collect(Collectors.toList());
    }

    /**
     * Assign a ticket to the current support user.
     *
     * @param ticketId the ID of the ticket to assign
     * @param authentication the authentication object containing current user info
     * @return the updated ticket response
     * @throws IllegalArgumentException if ticket not found or already assigned
     */
    @Transactional
    public TicketResponse assignTicketToCurrentSupport(Long ticketId, Authentication authentication) {
        UserAccount currentUser = getCurrentUser(authentication);

        // Verify user has SUPPORTUSER or ADMINUSER role
        if (currentUser.getRole() != Role.SUPPORTUSER && currentUser.getRole() != Role.ADMINUSER) {
            throw new IllegalArgumentException("Access denied: Only support users can assign tickets");
        }

        // Load ticket, if not found -> 404
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found");
        }

        Ticket ticket = ticketOpt.get();

        // If ticket state is CLOSED -> return conflict
        if (ticket.getTicketState() == TicketState.CLOSED) {
            throw new IllegalArgumentException("Ticket is closed");
        }

        // Check if assignment already exists
        if (supportTicketAssignmentRepository.existsByTicket(ticket)) {
            // If SUPPORTUSER and assignment exists -> conflict "Ticket already assigned"
            if (currentUser.getRole() == Role.SUPPORTUSER) {
                throw new IllegalArgumentException("Ticket already assigned");
            }
            // If ADMINUSER and assignment exists -> delete existing assignment (takeover)
            if (currentUser.getRole() == Role.ADMINUSER) {
                supportTicketAssignmentRepository.deleteByTicket(ticket);
            }
        }

        // Create new SupportTicketAssignment(ticket, currentUser)
        SupportTicketAssignment newAssignment = new SupportTicketAssignment(ticket, currentUser);

        // Save assignment
        supportTicketAssignmentRepository.save(newAssignment);

        // Update ticket:
        // - set ticketState = IN_PROGRESS
        // - set updateDate = now
        // - set assignedSupport = currentUser (keep this as cache for DTO compatibility)
        ticket.setTicketState(TicketState.IN_PROGRESS);
        ticket.setUpdateDate(java.time.Instant.now());
        ticket.setAssignedSupport(currentUser);

        // Save ticket
        Ticket savedTicket = ticketRepository.save(ticket);

        // Return updated TicketResponse
        return mapToTicketResponseWithAllDetails(savedTicket);
    }

    /**
     * Release a ticket from the current support user.
     *
     * @param ticketId the ID of the ticket to release
     * @param authentication the authentication object containing current user info
     * @return the updated ticket response
     * @throws IllegalArgumentException if ticket not found or not authorized
     */
    @Transactional
    public TicketResponse releaseTicket(Long ticketId, Authentication authentication) {
        UserAccount currentUser = getCurrentUser(authentication);

        // Verify user has SUPPORTUSER or ADMINUSER role
        if (currentUser.getRole() != Role.SUPPORTUSER && currentUser.getRole() != Role.ADMINUSER) {
            throw new IllegalArgumentException("Access denied: Only support users can release tickets");
        }

        // Load ticket + assignment
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found");
        }

        Ticket ticket = ticketOpt.get();

        // If no assignment exists -> conflict
        Optional<SupportTicketAssignment> assignmentOpt = supportTicketAssignmentRepository.findByTicket(ticket);
        if (assignmentOpt.isEmpty()) {
            throw new IllegalArgumentException("Ticket is not assigned");
        }

        SupportTicketAssignment assignment = assignmentOpt.get();

        // Allow release only if:
        // - current user is assigned support OR
        // - role is ADMINUSER
        if (!assignment.getSupportUser().getMail().equals(currentUser.getMail()) && currentUser.getRole() != Role.ADMINUSER) {
            throw new IllegalArgumentException("Access denied: You can only release tickets assigned to you");
        }

        // Delete assignment
        supportTicketAssignmentRepository.deleteByTicket(ticket);

        // Update ticket:
        // - set ticketState = UNASSIGNED
        // - set updateDate = now
        // - set assignedSupport = null (keep cache consistent)
        ticket.setTicketState(TicketState.UNASSIGNED);
        ticket.setUpdateDate(java.time.Instant.now());
        ticket.setAssignedSupport(null);

        // Save ticket
        Ticket savedTicket = ticketRepository.save(ticket);

        // Return updated TicketResponse
        return mapToTicketResponseWithAllDetails(savedTicket);
    }

    /**
     * Update ticket status and/or category.
     *
     * @param ticketId the ID of the ticket to update
     * @param request the update request containing optional state and category
     * @param authentication the authentication object containing current user info
     * @return the updated ticket response
     * @throws IllegalArgumentException if ticket not found, not authorized, or invalid state transition
     */
    @Transactional
    public TicketResponse updateSupportTicket(Long ticketId, SupportTicketUpdateRequest request, Authentication authentication) {
        UserAccount currentUser = getCurrentUser(authentication);

        // Verify user has SUPPORTUSER or ADMINUSER role
        if (currentUser.getRole() != Role.SUPPORTUSER && currentUser.getRole() != Role.ADMINUSER) {
            throw new IllegalArgumentException("Access denied: Only support users can update tickets");
        }

        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found");
        }

        Ticket ticket = ticketOpt.get();

        // Load assignment by ticket
        Optional<SupportTicketAssignment> assignmentOpt = supportTicketAssignmentRepository.findByTicket(ticket);

        // Allow update only if assignment exists and matches current user OR role is ADMINUSER
        if (assignmentOpt.isEmpty() || 
            (!assignmentOpt.get().getSupportUser().getMail().equals(currentUser.getMail()) && currentUser.getRole() != Role.ADMINUSER)) {
            throw new IllegalArgumentException("Access denied: You can only update tickets assigned to you");
        }

        boolean updated = false;

        // Update ticket state if provided
        if (request.getTicketState() != null) {
            TicketState newState = request.getTicketState();

            // Validate state transitions
            if (newState == TicketState.CLOSED) {
                ticket.setClosedDate(java.time.Instant.now());
            } else if (newState == TicketState.UNASSIGNED) {
                // If state becomes UNASSIGNED: delete assignment and set ticket.assignedSupport = null
                supportTicketAssignmentRepository.deleteByTicket(ticket);
                ticket.setAssignedSupport(null);
            }

            ticket.setTicketState(newState);
            updated = true;
        }

        // Update ticket category if provided
        if (request.getTicketCategory() != null) {
            ticket.setTicketCategory(request.getTicketCategory());
            updated = true;
        }

        if (updated) {
            ticket.setUpdateDate(java.time.Instant.now());
            Ticket savedTicket = ticketRepository.save(ticket);
            return mapToTicketResponseWithAllDetails(savedTicket);
        }

        // Return current ticket if no updates were made
        return mapToTicketResponseWithAllDetails(ticket);
    }

    /**
     * Close a ticket (shortcut method).
     *
     * @param ticketId the ID of the ticket to close
     * @param authentication the authentication object containing current user info
     * @return the updated ticket response
     * @throws IllegalArgumentException if ticket not found or not authorized
     */
    @Transactional
    public TicketResponse closeTicket(Long ticketId, Authentication authentication) {
        SupportTicketUpdateRequest request = new SupportTicketUpdateRequest();
        request.setTicketState(TicketState.CLOSED);
        return updateSupportTicket(ticketId, request, authentication);
    }
}
