package dk.lundogbendsen.springbootcourse.urlshortener.controller;

import dk.lundogbendsen.springbootcourse.urlshortener.controller.security.SecurityContext;
import dk.lundogbendsen.springbootcourse.urlshortener.model.User;
import dk.lundogbendsen.springbootcourse.urlshortener.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
/**
 * This test class shows how to test the security filter. It uses the full Spring context created by
 * @SpringBootTest and on top of that uses @AutoConfigureMockMvc to configure the mockMvc.
 *
 * The tests mocks the actual controller because the fokus id entirely on the Security.
 *
 */
public class InspirationalSecurityTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;
    @MockBean
    TokenController tokenController;

    final User user = User.builder().username("cvw").password("pwd").build();


    @Test
    public void listTokensValidAuthentication() throws Exception {
        when(userService.getUser("cvw")).thenReturn(user);
        final String encodedUsernamePassword = Base64.getEncoder().encodeToString("cvw:pwd".getBytes());
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/token/").header("Authorization", "Basic " + encodedUsernamePassword)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

    }

    @Test
    public void listTokensInvalidAuthentication() throws Exception {
        when(userService.getUser("cvw")).thenReturn(user);
        final String encodedUsernamePassword = Base64.getEncoder().encodeToString("cvw:pwdWrong".getBytes());
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/token/")
                                .header("Authorization", "Basic " + encodedUsernamePassword)
                )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andReturn();
        assertNull(SecurityContext.getUser());
    }
}
