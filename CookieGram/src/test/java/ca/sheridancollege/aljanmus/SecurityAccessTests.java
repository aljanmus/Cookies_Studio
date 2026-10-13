
package ca.sheridancollege.aljanmus;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import ca.sheridancollege.aljanmus.controllers.MainController;
import ca.sheridancollege.aljanmus.security.SecurityConfig;

@WebMvcTest(MainController.class)
@Import(SecurityConfig.class)
public class SecurityAccessTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testLoginPagePublic() throws Exception {
        mockMvc.perform(get("/login"))
               .andExpect(status().isOk());
    }

    @Test
    void testAdminWithoutLogin() throws Exception {
        mockMvc.perform(get("/admin"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdminAccess() throws Exception {
        mockMvc.perform(get("/admin"))
               .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCustomerAccess() throws Exception {
        mockMvc.perform(get("/customer"))
               .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testEmployeeAccess() throws Exception {
        mockMvc.perform(get("/employee"))
               .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCustomerCannotAccessAdmin() throws Exception {
        mockMvc.perform(get("/admin"))
               .andExpect(status().isForbidden());
    }
}
