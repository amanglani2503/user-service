package com.example.user_service.service;

import com.example.user_service.model.Role;
import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // getUserByEmail()

    @Test
    void testGetUserByEmail_ShouldReturnUser_WhenFound() {
        String email = "test@example.com";
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(email);
        mockUser.setName("Test");
        mockUser.setRole(Role.PASSENGER);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        User result = userService.getUserByEmail(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void testGetUserByEmail_ShouldThrowException_WhenNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserByEmail("missing@example.com"));
    }

    // extractToken()

    @Test
    void testExtractToken_ShouldReturnToken_WhenHeaderIsValid() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer abc.def.ghi");

        String token = userService.extractToken(request);
        assertEquals("abc.def.ghi", token);
    }

    @Test
    void testExtractToken_ShouldReturnNull_WhenHeaderIsInvalid() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");

        assertNull(userService.extractToken(request));
    }

    @Test
    void testExtractToken_ShouldReturnNull_WhenHeaderIsMissing() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        assertNull(userService.extractToken(request));
    }

    // updateUser()

    @Test
    void testUpdateUser_ShouldUpdateFields_WhenUserExists() {
        Integer userId = 1;
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("old@example.com");
        existingUser.setName("Old Name");
        existingUser.setPassword("oldpass");
        existingUser.setRole(Role.ADMIN);

        User updates = new User();
        updates.setEmail("new@example.com");
        updates.setName("New Name");
        updates.setPassword("newpass");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userService.updateUser(userId, updates);

        assertEquals("New Name", updatedUser.getName());
        assertEquals("new@example.com", updatedUser.getEmail());
        assertNotEquals("newpass", updatedUser.getPassword()); // Should be encoded
    }

    @Test
    void testUpdateUser_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

        User dummyUpdate = new User();
        dummyUpdate.setName("Update");

        assertThrows(RuntimeException.class, () -> userService.updateUser(99, dummyUpdate));
    }
}
