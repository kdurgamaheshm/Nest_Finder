package com.nestfinder.nestfinderbackend.controllers;

import com.nestfinder.nestfinderbackend.model.User;
import com.nestfinder.nestfinderbackend.payload.request.UserProfileUpdateRequest;
import com.nestfinder.nestfinderbackend.payload.response.MessageResponse;
import com.nestfinder.nestfinderbackend.repository.UserRepository;
import com.nestfinder.nestfinderbackend.security.services.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private UserController userController;

    private UserDetailsImpl userDetails;
    private User user;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        userDetails = mock(UserDetailsImpl.class);
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");

        when(userDetails.getId()).thenReturn(1L);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testGetUserProfile_UserFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = userController.getUserProfile();

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof User);
        User returnedUser = (User) response.getBody();
        assertEquals("testuser", returnedUser.getUsername());
    }

    @Test
    public void testGetUserProfile_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.getUserProfile();

        assertEquals(404, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse message = (MessageResponse) response.getBody();
        assertEquals("User not found.", message.getMessage());
    }

    @Test
    public void testUpdateUserProfile_Success() {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        ResponseEntity<?> response = userController.updateUserProfile(request);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse message = (MessageResponse) response.getBody();
        assertEquals("User profile updated successfully!", message.getMessage());
    }

    @Test
    public void testUpdateUserProfile_UsernameTaken() {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setUsername("existinguser");
        request.setEmail("newuser@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        ResponseEntity<?> response = userController.updateUserProfile(request);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse message = (MessageResponse) response.getBody();
        assertEquals("Error: Username is already taken!", message.getMessage());
    }

    @Test
    public void testUpdateUserProfile_EmailTaken() {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setUsername("newuser");
        request.setEmail("existingemail@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existingemail@example.com")).thenReturn(true);

        ResponseEntity<?> response = userController.updateUserProfile(request);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse message = (MessageResponse) response.getBody();
        assertEquals("Error: Email is already in use!", message.getMessage());
    }

    @Test
    public void testUpdateUserProfile_UserNotFound() {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.updateUserProfile(request);

        assertEquals(404, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse message = (MessageResponse) response.getBody();
        assertEquals("User not found.", message.getMessage());
    }
}
