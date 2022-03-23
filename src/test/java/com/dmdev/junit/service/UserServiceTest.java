package com.dmdev.junit.service;

import com.dmdev.junit.dto.User;
import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// @TestInstance(TestInstance.Lifecycle.PER_METHOD) // по умолчанию. Инстанс создается для каждого метода и требует BeforeAll и AfterAll в static
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserServiceTest {

    private static final User PETR = User.of(2, "Petr", "111");
    private static final User IVAN = User.of(1, "Ivan", "123");

    private UserService userService;

    @BeforeAll
    void init() {
        System.out.println("Before all: " + this);
    }

    @BeforeEach
    void prepare() {
        System.out.println("Before each: " + this);
        userService = new UserService();
    }

    @Test
    void usersIsEmptyIfNoUsersAdded() {
        System.out.println("Test 1: " + this);
        var users = userService.getAll();

//        MatcherAssert.assertThat(users, IsEmptyCollection.empty()); // Hamcrest
        assertTrue(users.isEmpty(), () -> "User list should be empty");
    }

    @Test
    void usersSizeIfUserAdded() {
        System.out.println("Test 2: " + this);
        userService.add(IVAN, PETR);

        var users = userService.getAll();

        assertThat(users).hasSize(2); // AssertJ
//        assertEquals(2, users.size()); // JUnit
    }

    @Test
    void loginSuccessIfUserExist() {
        System.out.println("Test 3: " + this);
        userService.add(IVAN);

        Optional<User> mayBeUser = userService.login(IVAN.getName(), IVAN.getPassword());

        assertThat(mayBeUser).isPresent();
//        assertTrue(mayBeUser.isPresent());
        mayBeUser.ifPresent(user -> assertThat(user).isEqualTo(IVAN));
//        mayBeUser.ifPresent(user -> assertEquals(IVAN, user));
    }

    @Test
    void usersConvertedToMapById() {
        System.out.println("Test 6: " + this);
        userService.add(IVAN, PETR);

        Map<Integer, User> users = userService.getAllConvertedById();

//        MatcherAssert.assertThat(users, IsMapContaining.hasKey(IVAN.getId())); // Hamcrest

        assertAll(
                () -> assertThat(users).containsKeys(IVAN.getId(), PETR.getId()),
                () -> assertThat(users).containsValues(IVAN, PETR)
        );
    }

    @Test
    void loginFailIfPasswordIsNotCorrect() {
        System.out.println("Test 4: " + this);
        userService.add(IVAN);

        var mayBeUser = userService.login(IVAN.getName(), "4434");

        assertTrue(mayBeUser.isEmpty());
    }

    @Test
    void loginFailIfUserDosNotExist() {
        System.out.println("Test 5: " + this);
        userService.add(IVAN);

        var mayBeUser = userService.login("roy", IVAN.getPassword());

        assertTrue(mayBeUser.isEmpty());
    }

    @AfterEach
    void deleteDataFromDB() {
        System.out.println("After each: " + this);
    }

    @AfterAll
    void closeConnectionPool() {
        System.out.println("After all: " + this);
    }
}
