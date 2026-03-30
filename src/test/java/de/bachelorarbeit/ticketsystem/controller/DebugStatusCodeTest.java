package de.bachelorarbeit.ticketsystem.controller;

import de.bachelorarbeit.ticketsystem.dto.CreateTicketRequest;
import de.bachelorarbeit.ticketsystem.model.entity.Role;
import de.bachelorarbeit.ticketsystem.model.entity.TicketCategory;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Debug test to see what status codes are actually returned.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebMvc
@Transactional
public class DebugStatusCodeTest {

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
    }

    @Test
    @WithMockUser(username = "support@test.com", roles = {"SUPPORTUSER"})
    void debugCreateTicketStatusCode() throws Exception {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle("Test Ticket");
        request.setDescription("Test Description");
        request.setTicketCategory(TicketCategory.HARDWARE);

        MvcResult result = mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        int actualStatus = result.getResponse().getStatus();
        String content = result.getResponse().getContentAsString();

        // This will fail and show us the actual status code
        if (actualStatus != 403) {
            throw new AssertionError("Expected 403 but got " + actualStatus + ". Content: " + content);
        }
    }

    @Test
    @WithMockUser(username = "support@test.com", roles = {"SUPPORTUSER"})
    void debugGetMyTicketsStatusCode() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/tickets/my"))
                .andReturn();

        int actualStatus = result.getResponse().getStatus();
        String content = result.getResponse().getContentAsString();

        // This will fail and show us the actual status code
        if (actualStatus != 500) {
            throw new AssertionError("Expected 500 but got " + actualStatus + ". Content: " + content);
        }
    }
}
