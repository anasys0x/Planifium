package com.diro.ift2255.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.diro.ift2255.model.User;
import com.diro.ift2255.service.UserService;

import io.javalin.http.Context;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService mockService;

    @Mock
    private Context mockContext;

    private UserController controller;
    private long testStartTime;

    @BeforeAll
    static void printHeader() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("UserController Tests");
        System.out.println("=".repeat(80));
    }

    @BeforeEach
    void setup(TestInfo testInfo) {
        controller = new UserController(mockService);
        testStartTime = System.currentTimeMillis();

        System.out.println("\nTEST: " + testInfo.getDisplayName());
        System.out.println("    ├─ Method: " + testInfo.getTestMethod().get().getName());
        System.out.println("    ├─ Assertions:");
    }

    @AfterEach
    void tearDown(TestInfo testInfo) {
        long duration = System.currentTimeMillis() - testStartTime;
        System.out.println("    └─ Duration: " + duration + " ms");
    }

    @Test
    @DisplayName("Create user should reject invalid email format")
    void testCreateUserWithInvalidEmail() {
        // ARRANGE
        User userWithInvalidEmail = new User(0, "Test User", "invalid-email");
        
        when(mockContext.bodyAsClass(User.class)).thenReturn(userWithInvalidEmail);
        when(mockContext.status(400)).thenReturn(mockContext);
        
        // ACT
        controller.createUser(mockContext);
        
        // ASSERT
        try {
            verify(mockContext).bodyAsClass(User.class);
            OK("User extracted from request body", false);
            
            verify(mockContext).status(400);
            OK("Status 400 set for invalid email", false);
            
            verify(mockContext).json("Invalid email format");
            OK("Error message returned", false);
            
            verify(mockService, never()).createUser(any());
            OK("Service was not called with invalid email");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("Create user should accept valid email and create user")
    void testCreateUserWithValidEmail() {
        // ARRANGE
        User validUser = new User(0, "Alice Wonderland", "alice@example.com");
        
        when(mockContext.bodyAsClass(User.class)).thenReturn(validUser);
        when(mockContext.status(201)).thenReturn(mockContext);
        
        // ACT
        controller.createUser(mockContext);
        
        // ASSERT
        try {
            verify(mockContext).bodyAsClass(User.class);
            OK("Valid user extracted from body", false);
            
            verify(mockService).createUser(validUser);
            OK("Service called to create user", false);
            
            verify(mockContext).status(201);
            OK("Status 201 (Created) set", false);
            
            verify(mockContext).json(validUser);
            OK("User returned in response");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @AfterAll
    static void printFooter() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("COMPLETED: UserController Tests");
        System.out.println("=".repeat(80) + "\n");
    }

    // Helper methods
    private void printMessage(String message, boolean isOk, boolean isLast) {
        String symbol = isLast ? "└─" : "├─";
        String status = isOk ? "[PASS]" : "[FAIL]";
        System.out.println("    │   " + symbol + " " + status + " " + message);
    }

    private void OK(String message) {
        printMessage(message, true, true);
    }

    private void OK(String message, boolean isLast) {
        printMessage(message, true, isLast);
    }

    private void Err(String message) {
        printMessage(message, false, true);
    }
}