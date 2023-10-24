package dk.lundogbendsen.springbootcourse.urlshortener.controller;

import dk.lundogbendsen.springbootcourse.urlshortener.controller.security.SecurityIntercepter;
import dk.lundogbendsen.springbootcourse.urlshortener.model.User;
import dk.lundogbendsen.springbootcourse.urlshortener.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(MockitoExtension.class)
/**
 * This test case demonstrate how you could user Mockito with MockMvc completely without any Spring context.
 * The Controller layer is setup without security filters and Controller advices. They can optionally be
 * included if a test case requires them.
 */
public class InspirationalUserControllerTest {
    MockMvc mvc;

    @Mock
    UserService userService;

    @InjectMocks UserController userController;

    final User user = User.builder().username("cvw").password("pwd").build();

    @BeforeEach
    public void initMockMvc() {
        mvc = MockMvcBuilders
                .standaloneSetup(userController)
//                .setControllerAdvice(new ControllerAdvicerServiceLayer())
//                .addInterceptors(new SecurityIntercepter())
                .build();
    }

    @Test
    public void testGetUser() throws Exception {
        when(userService.getUser("cvw")).thenReturn(user);
        mvc.perform(
                        MockMvcRequestBuilders.get("/user/cvw")
                )
                .andExpect(jsonPath("$.username", is("cvw")))
                .andExpect(jsonPath("$.password", is("pwd")))
//                .andDo(print())
                .andReturn();
    }
    @Test
    public void testCreateUser() throws Exception {
        mvc.perform(
                        MockMvcRequestBuilders.post("/user/")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"cvw\", \"password\":\"pwd\"}")
                )
//                .andDo(print())
                .andReturn();

        verify(userService).create("cvw", "pwd");
    }
}
