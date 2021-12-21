package com.jvj28.homeworks.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvj28.homeworks.auth.contract.ApiAuthErrorResponse;
import com.jvj28.homeworks.auth.contract.ApiAuthRequest;
import com.jvj28.homeworks.auth.contract.ApiAuthResponse;
import com.jvj28.homeworks.model.Model;
import com.jvj28.homeworks.model.db.entity.UsersEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {
        ApiAuthController.class,
        ApiAuthControllerAdvice.class,
        JwtTokenUtil.class,
        ApiAuthControllerService.class,
        ApiRequestAuthorizationFilter.class})
@WebMvcTest
class TestApiAuthenticationController {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    ApiAuthControllerService apiAuthService;
    @Autowired
    ApiAuthController controller;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private ApiRequestAuthorizationFilter filter;
    @MockBean
    AuthenticationManager authenticationManager;
    @MockBean
    Model model;

    @Test
    void staticJwtTokenTest() {
        String username = "bubbauser";
        String password = "bubbapass";

        when(model.getUserByUsername(username)).thenReturn(new UsersEntity(username, password));

        try {
            assertNotNull(mockMvc);
            assertNotNull(authenticationManager);
            assertNotNull(jwtTokenUtil);
            assertNotNull(apiAuthService);
            assertNotNull(controller);
            assertNotNull(filter);

            // Check to see if our MOCK works!!!
            UserDetails ud = apiAuthService.loadUserByUsername(username);
            assertEquals(username, ud.getUsername());
            assertEquals(password, ud.getPassword());

            // Mock the behavior of AuthenticationManager
            UsernamePasswordAuthenticationToken upat = new UsernamePasswordAuthenticationToken(username, password);
            when(authenticationManager.authenticate(upat)).thenReturn(getAuthentication(upat, ud));

            // Authenticate to springboot.  This function ONLY calls 'AuthenticationManager.authenticate'
            ApiAuthRequest authData = new ApiAuthRequest(username, password);
            MvcResult result = this.mockMvc.perform(
                    post("/authenticate").content(toJson(authData))
                            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();
            assertNotNull(result);
            MockHttpServletResponse apiResponse = result.getResponse();
            ApiAuthResponse jwt = fromJson(apiResponse.getContentAsString());
            assertNotNull(jwt.getToken());
            assertTrue(jwt.getToken().length()>0);

            // Validate that the API Filter can deserialize and decrypt te token, the token is valid
            // This function in the filter also stores the credentials in a SecurityContextHolder
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("Authorization")).thenReturn(String.format("Bearer %s", jwt.getToken()));
            HttpServletResponse response = mock(HttpServletResponse.class);
            FilterChain chain = mock(FilterChain.class);
            filter.doFilter(request, response, chain);

            // Check the security context and verify that the credentials are indeed in there.
            SecurityContext context = SecurityContextHolder.getContext();
            assertNotNull(context);
            Authentication authentication = context.getAuthentication();
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof User))
                throw new Exception("Invalid Credentials");
            assertEquals(username, ((User)principal).getUsername());
            assertEquals(password, ((User)principal).getPassword());
            
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testAthenticationExceptions() {
        try {
            String username = "bubbauser";
            String password = "bubbapass";

            ErrorExpect[] exceptionList = List.of(
                    new ErrorExpect(new BadCredentialsException("Bad Credentials"), status().isUnauthorized()),
                    new ErrorExpect(new DisabledException("Account Disabled"), status().isUnauthorized()),
                    new ErrorExpect(new LockedException("Account Locked"), status().isLocked()),
                    new ErrorExpect(new AccountExpiredException("Account Expired"), status().isUnauthorized()),
                    new ErrorExpect(new AuthenticationCredentialsNotFoundException("Credentials Not Found"), status().isUnauthorized()),
                    new ErrorExpect(new AuthenticationServiceException("Authentication Service Error"), status().isInternalServerError()),
                    new ErrorExpect(new CredentialsExpiredException("Credentials Expired"), status().isForbidden()),
                    new ErrorExpect(new InsufficientAuthenticationException("Not Enough Privileges"), status().isForbidden()),
                    new ErrorExpect(new InternalAuthenticationServiceException("Internal Authentication Error"), status().isInternalServerError()),
                    new ErrorExpect(new ProviderNotFoundException("Authentication Provider Unavailable"), status().isUnauthorized())
            ).toArray(new ErrorExpect[0]);

            UsernamePasswordAuthenticationToken upat = new UsernamePasswordAuthenticationToken(username, password);
            when(authenticationManager.authenticate(upat))
                    .thenThrow(exceptionList[0].getException())
                    .thenThrow(exceptionList[1].getException())
                    .thenThrow(exceptionList[2].getException())
                    .thenThrow(exceptionList[3].getException())
                    .thenThrow(exceptionList[4].getException())
                    .thenThrow(exceptionList[5].getException())
                    .thenThrow(exceptionList[6].getException())
                    .thenThrow(exceptionList[7].getException())
                    .thenThrow(exceptionList[8].getException())
                    .thenThrow(exceptionList[9].getException())
            ;

            for (ErrorExpect ae: exceptionList) {

                System.out.printf("Testing %s\n", ae.getException().getMessage());

                // Authenticate to springboot.  This function ONLY calls 'AuthenticationManager.authenticate'
                MvcResult result = this.mockMvc.perform(
                        post("/authenticate").content(toJson(new ApiAuthRequest(username, password)))
                                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                ).andExpect(ae.getStatus()).andReturn();
                assertNotNull(result);
                ApiAuthErrorResponse er = new ObjectMapper().readValue(result.getResponse().getContentAsString(), ApiAuthErrorResponse.class);
                assertEquals(ae.getException().getMessage(), er.getMessage());
            }

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    static class ErrorExpect {
        private final AuthenticationException ae;
        private final ResultMatcher status;

        public ErrorExpect(AuthenticationException ae, ResultMatcher status) {
            this.ae = ae;
            this.status = status;
        }
        public AuthenticationException getException() {
            return ae;
        }
        public ResultMatcher getStatus() {
            return status;
        }

    }

    // Mock the behavior of the AuthenticationManager as it's mocked.  And, we'll just be sure the upat is valid.
    private Authentication getAuthentication(UsernamePasswordAuthenticationToken upat, UserDetails ud) {
        // Presumably what the REAL authentication will do is compare the user credentials
        Object principal = upat.getPrincipal();
        Object credentials = upat.getCredentials();
        if ((principal instanceof String) && (credentials instanceof String)) {
            // presumably the REAL AuthenticationManager will query userDetailsService, but we can't do that inside the mock
            // so, we did so earlier to populate the ud parameter.
            if (!ud.isEnabled())
                throw new DisabledException("Account is disabled");
            if (principal.equals(ud.getUsername()) && credentials.equals(ud.getPassword()))
                return upat;
        }
        throw new BadCredentialsException("Bad Credentials");
    }

    private ApiAuthResponse fromJson(String json) throws JsonProcessingException {
        return new ObjectMapper().readValue(json, ApiAuthResponse.class);
    }

    private String toJson(ApiAuthRequest authData) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(authData);
    }

}