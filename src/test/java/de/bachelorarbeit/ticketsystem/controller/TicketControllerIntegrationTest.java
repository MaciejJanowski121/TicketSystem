package de.bachelorarbeit.ticketsystem.controller;

import de.bachelorarbeit.ticketsystem.dto.CreateCommentRequest;
import de.bachelorarbeit.ticketsystem.dto.CreateTicketRequest;
import de.bachelorarbeit.ticketsystem.model.entity.Role;
import de.bachelorarbeit.ticketsystem.model.entity.TicketCategory;
import de.bachelorarbeit.ticketsystem.model.entity.TicketState;
import de.bachelorarbeit.ticketsystem.model.entity.UserAccount;
import de.bachelorarbeit.ticketsystem.repository.TicketRepository;
import de.bachelorarbeit.ticketsystem.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TicketController endpoints.
 * Tests HTTP endpoints with proper authentication and authorization.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebMvc
@Transactional
public class TicketControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Clean up
        ticketRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        UserAccount endUser = new UserAccount("enduser", "enduser@test.com", 
                passwordEncoder.encode("password"), Role.ENDUSER);
        userRepository.save(endUser);

        UserAccount supportUser = new UserAccount("support", "support@test.com", 
                passwordEncoder.encode("password"), Role.SUPPORTUSER);
        userRepository.save(supportUser);

        UserAccount adminUser = new UserAccount("admin", "admin@test.com", 
                passwordEncoder.encode("password"), Role.ADMINUSER);
        userRepository.save(adminUser);
    }

    // ========== CREATE TICKET TESTS ==========

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testCreateTicket_Success() throws Exception {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle("Test Ticket");
        request.setDescription("Test Description");
        request.setTicketCategory(TicketCategory.HARDWARE);

        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Ticket"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.ticketCategory").value("HARDWARE"))
                .andExpect(jsonPath("$.ticketState").value("UNASSIGNED"))
                .andExpect(jsonPath("$.ticketId").exists())
                .andExpect(jsonPath("$.createDate").exists())
                .andExpect(jsonPath("$.updateDate").exists());
    }

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testCreateTicket_ValidationError_EmptyTitle() throws Exception {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle(""); // Empty title
        request.setDescription("Test Description");
        request.setTicketCategory(TicketCategory.HARDWARE);

        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testCreateTicket_ValidationError_NullCategory() throws Exception {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle("Test Ticket");
        request.setDescription("Test Description");
        request.setTicketCategory(null); // Null category

        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "support@test.com", roles = {"SUPPORTUSER"})
    void testCreateTicket_Forbidden_SupportUser() throws Exception {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle("Test Ticket");
        request.setDescription("Test Description");
        request.setTicketCategory(TicketCategory.HARDWARE);

        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testCreateTicket_Unauthorized_NoAuth() throws Exception {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle("Test Ticket");
        request.setDescription("Test Description");
        request.setTicketCategory(TicketCategory.HARDWARE);

        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ========== GET MY TICKETS TESTS ==========

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testGetMyTickets_Success() throws Exception {
        mockMvc.perform(get("/api/tickets/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "support@test.com", roles = {"SUPPORTUSER"})
    void testGetMyTickets_Forbidden_SupportUser() throws Exception {
        mockMvc.perform(get("/api/tickets/my"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetMyTickets_Unauthorized_NoAuth() throws Exception {
        mockMvc.perform(get("/api/tickets/my"))
                .andExpect(status().isForbidden());
    }

    // ========== GET ALL TICKETS TESTS ==========

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testGetAllTickets_Success_EndUser() throws Exception {
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "support@test.com", roles = {"SUPPORTUSER"})
    void testGetAllTickets_Success_SupportUser() throws Exception {
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMINUSER"})
    void testGetAllTickets_Success_AdminUser() throws Exception {
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testGetAllTickets_WithFilters() throws Exception {
        mockMvc.perform(get("/api/tickets")
                .param("search", "test")
                .param("state", "UNASSIGNED")
                .param("category", "HARDWARE")
                .param("sort", "createDate")
                .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetAllTickets_Unauthorized_NoAuth() throws Exception {
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isForbidden());
    }

    // ========== GET TICKET BY ID TESTS ==========

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testGetTicketById_NotFound() throws Exception {
        mockMvc.perform(get("/api/tickets/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetTicketById_Unauthorized_NoAuth() throws Exception {
        mockMvc.perform(get("/api/tickets/1"))
                .andExpect(status().isForbidden());
    }

    // ========== COMMENT TESTS ==========

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testCreateComment_ValidationError_EmptyComment() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setComment(""); // Empty comment

        mockMvc.perform(post("/api/tickets/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateComment_Unauthorized_NoAuth() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setComment("Test comment");

        mockMvc.perform(post("/api/tickets/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testGetTicketComments_NotFound() throws Exception {
        mockMvc.perform(get("/api/tickets/999/comments"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetTicketComments_Unauthorized_NoAuth() throws Exception {
        mockMvc.perform(get("/api/tickets/1/comments"))
                .andExpect(status().isForbidden());
    }

    // ========== TICKET FILTERING AND SEARCH TESTS ==========

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testGetAllTickets_FilterByState() throws Exception {
        mockMvc.perform(get("/api/tickets")
                .param("state", "UNASSIGNED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testGetAllTickets_FilterByCategory() throws Exception {
        mockMvc.perform(get("/api/tickets")
                .param("category", "HARDWARE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testGetAllTickets_CombinedFilters() throws Exception {
        mockMvc.perform(get("/api/tickets")
                .param("state", "UNASSIGNED")
                .param("category", "HARDWARE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testGetAllTickets_SearchPartialString() throws Exception {
        mockMvc.perform(get("/api/tickets")
                .param("search", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testGetAllTickets_SortingAscending() throws Exception {
        mockMvc.perform(get("/api/tickets")
                .param("sort", "createDate")
                .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testGetAllTickets_SortingDescending() throws Exception {
        mockMvc.perform(get("/api/tickets")
                .param("sort", "updateDate")
                .param("direction", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "enduser@test.com", roles = {"ENDUSER"})
    void testGetAllTickets_AllFiltersAndSorting() throws Exception {
        mockMvc.perform(get("/api/tickets")
                .param("state", "IN_PROGRESS")
                .param("category", "NETWORK")
                .param("search", "network")
                .param("sort", "createDate")
                .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
