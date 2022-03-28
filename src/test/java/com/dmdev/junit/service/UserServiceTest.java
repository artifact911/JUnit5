package com.dmdev.junit.service;

import com.dmdev.junit.dto.User;
import com.dmdev.junit.paramresolver.UserServiceParamResolver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// @TestInstance(TestInstance.Lifecycle.PER_METHOD) // по умолчанию. Инстанс создается для каждого метода и требует BeforeAll и AfterAll
// в static
@Tag("fast")
@Tag("user")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.DisplayName.class) // Избегать это юзать
@ExtendWith({
        UserServiceParamResolver.class
})
public class UserServiceTest {

    private static final User PETR = User.of(2, "Petr", "111");
    private static final User IVAN = User.of(1, "Ivan", "123");

    private UserService userService;

    UserServiceTest(TestInfo testInfo) {
        System.out.println();
    }

    @BeforeAll
    void init() {
        System.out.println("Before all: " + this);
    }

    @BeforeEach
    void prepare(UserService userService) {
        System.out.println("Before each: " + this);
        this.userService = userService;
    }

    @Test
    @Order(1)
    @DisplayName("users will be empty")
    void usersIsEmptyIfNoUsersAdded(UserService userService) {
        System.out.println("Test 1: " + this);
        var users = userService.getAll();

//        MatcherAssert.assertThat(users, IsEmptyCollection.empty()); // Hamcrest
        assertTrue(users.isEmpty(), () -> "User list should be empty");
    }

    @Test
    @Order(2)
    void usersSizeIfUserAdded() {
        System.out.println("Test 2: " + this);
        userService.add(IVAN, PETR);

        var users = userService.getAll();

        assertThat(users).hasSize(2); // AssertJ
//        assertEquals(2, users.size()); // JUnit
    }

    @Test
    void usersConvertedToMapById() {
        System.out.println("Test 5: " + this);
        userService.add(IVAN, PETR);

        Map<Integer, User> users = userService.getAllConvertedById();

//        MatcherAssert.assertThat(users, IsMapContaining.hasKey(IVAN.getId())); // Hamcrest
        assertAll(
                () -> assertThat(users).containsKeys(IVAN.getId(), PETR.getId()),
                () -> assertThat(users).containsValues(IVAN, PETR)
        );
    }

    @AfterEach
    void deleteDataFromDB() {
        System.out.println("After each: " + this);
    }

    @AfterAll
    void closeConnectionPool() {
        System.out.println("After all: " + this);
    }

    @Nested
    @DisplayName("Test user login functionality")
    @Tag("login")
    class LoginTest {

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
        void loginFailIfPasswordIsNotCorrect() {
            System.out.println("Test 6: " + this);
            userService.add(IVAN);

            var mayBeUser = userService.login(IVAN.getName(), "4434");

            assertTrue(mayBeUser.isEmpty());
        }

        @Test
        void loginFailIfUserDosNotExist() {
            System.out.println("Test 7: " + this);
            userService.add(IVAN);

            var mayBeUser = userService.login("roy", IVAN.getPassword());

            assertTrue(mayBeUser.isEmpty());
        }

        @Test
//    @org.junit.Test(expected = IllegalArgumentException.class) // было в JUnit4
        void throwExceptionIfUserNameOrPasswordIsNull() {
            System.out.println("Test 4: " + this);

            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () -> userService.login(null, IVAN.getPassword())),
                    () -> assertThrows(IllegalArgumentException.class, () -> userService.login(IVAN.getName(), null))
            );
        }

        @ParameterizedTest(name = "{arguments} test")
//        @ArgumentsSource()
//        @NullSource // работа с одним параметром
//        @EmptySource // работа с одним параметром
//        @NullAndEmptySource // заменитель двух верхних
//        @ValueSource(strings = {
//                "Ivan", "Petr"
//        }) // работа с одним параметром, например name. Пердаем
//        @EnumSource
        @MethodSource("com.dmdev.junit.service.UserServiceTest#getArgumentsForLoginTest")
//        @CsvFileSource(resources = "/login-test-data.csv", delimiter = ',', numLinesToSkip = 1)
//        @CsvSource({            // аналог верхнего но не нужно создавать файл
//                "Ivan", "123",
//                "Petr", "111"
//        })
        @DisplayName("login param test")
        void loginParametrizedTest(String name, String password, Optional<User> user) {
            userService.add(IVAN, PETR);

            var maybeUser = userService.login(name, password);
            assertThat(maybeUser).isEqualTo(user);
        }
    }

    static Stream<Arguments> getArgumentsForLoginTest() {
        return Stream.of(
                Arguments.of("Ivan", "123", Optional.of(IVAN)),
                Arguments.of("Petr", "111", Optional.of(PETR)),
                Arguments.of("Petr", "dummy", Optional.empty()),
                Arguments.of("dummy", "111", Optional.empty())
        );
    }
}
