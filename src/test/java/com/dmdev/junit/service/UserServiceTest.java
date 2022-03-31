package com.dmdev.junit.service;

import com.dmdev.junit.TestBase;
import com.dmdev.junit.dao.UserDao;
import com.dmdev.junit.dto.User;
import com.dmdev.junit.extension.ConditionalExtension;
import com.dmdev.junit.extension.GlobalExtension;
import com.dmdev.junit.extension.PostProcessingExtension;
import com.dmdev.junit.extension.ThrowableExtension;
import com.dmdev.junit.extension.UserServiceParamResolver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.RepeatedTest.LONG_DISPLAY_NAME;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

// @TestInstance(TestInstance.Lifecycle.PER_METHOD) // по умолчанию. Инстанс создается для каждого метода и требует BeforeAll и AfterAll
// в static
@Tag("fast")
@Tag("user")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.DisplayName.class) // Избегать это юзать
@ExtendWith({
        UserServiceParamResolver.class,
        PostProcessingExtension.class,
        ConditionalExtension.class,
        MockitoExtension.class
//        ThrowableExtension.class
//        GlobalExtension.class // вынести в TestBase
})
public class UserServiceTest extends TestBase {

    private static final User PETR = User.of(2, "Petr", "111");
    private static final User IVAN = User.of(1, "Ivan", "123");

    @Mock(lenient = true)
    private UserDao userDao;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<Integer> argumentCaptor;

    UserServiceTest(TestInfo testInfo) {
        System.out.println();
    }

    @BeforeAll
    void init() {
        System.out.println("Before all: " + this);
    }

    @BeforeEach
    void prepare() {
        System.out.println("Before each: " + this);
//        lenient().when(userDao.delete(IVAN.getId())).thenReturn(true);
        doReturn(true).when(userDao).delete(IVAN.getId());

        // теперь есть аннотации
      /*  this.userDao = Mockito.mock(UserDao.class); // for mock
        this.userDao = Mockito.spy(new UserDao()); // for spy
        this.userService = new UserService(userDao);*/
    }

    @Test
    void throwExceptionIfDataBaseIsNotAvailable() {
        doThrow(RuntimeException.class).when(userDao).delete(IVAN.getId());

        assertThrows(RuntimeException.class, () -> userService.delete(IVAN.getId()));
    }

    @Test
    void shouldDeleteExistedUser() {
        userService.add(IVAN);
//        Mockito.doReturn(true).when(userDao).delete(IVAN.getId()); // унесли в @BeforeEach
//        Mockito.doReturn(true).when(userDao).delete(Mockito.any());
//        Mockito.when(userDao.delete(IVAN.getId()))
//               .thenReturn(true)
//               .thenReturn(false);

        var deleteResult = userService.delete(IVAN.getId());
        System.out.println(userService.delete(IVAN.getId()));
        System.out.println(userService.delete(IVAN.getId()));

//        Mockito.verify(userDao).delete(IVAN.getId()); // проверили что вызвался метод 1 раз
//        Mockito.verify(userDao, Mockito.atLeast(2)).delete(IVAN.getId()); // вызвался метод как минимум 2 раза
//        Mockito.verify(userDao, Mockito.times(3)).delete(IVAN.getId()); // вызвался метод конкретно 3 раза

//        var argumentCaptor = ArgumentCaptor.forClass(Integer.class); // отловитлавливатель Integer в mock // аннотация теперь есть
        verify(userDao, Mockito.times(3)).delete(argumentCaptor.capture()); // тут отловит

        assertThat(argumentCaptor.getValue()).isEqualTo(IVAN.getId()); // а тут проверит

        reset(userDao); // очистит мок, если нужно переиспользовать. Лучше в @BeforeEach создавать новый

        assertThat(deleteResult).isTrue();
    }

    @Test
    @Order(1)
    @DisplayName("users will be empty")
    void usersIsEmptyIfNoUsersAdded(UserService userService) {
//        if(true){throw new RuntimeException();} // для 57 строки
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
    @Timeout(value = 200L, unit = TimeUnit.MILLISECONDS) // укладываемся ли мы во время выполнения. Лучше подходит для integration
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
        @Disabled("flaky, need to see")
        void loginFailIfPasswordIsNotCorrect() {
            System.out.println("Test 6: " + this);
            userService.add(IVAN);

            var mayBeUser = userService.login(IVAN.getName(), "4434");

            assertTrue(mayBeUser.isEmpty());
        }

        //        @Test
        @RepeatedTest(value = 5, name = LONG_DISPLAY_NAME) // повтоярем, на случай, что тест завалится
        void loginFailIfUserDosNotExist(RepetitionInfo repetitionInfo) { // инфо о повторениях и возможно захочем это как-то заюзать
            System.out.println("Test 7: " + this);
            userService.add(IVAN);
            var mayBeUser = userService.login("roy", IVAN.getPassword());
            assertTrue(mayBeUser.isEmpty());
        }

        @Test
        @Disabled
        void checkFunctionalityPerformance() {

           /* // для запуска нашего теста в другом потоке
            System.out.println(Thread.currentThread().getName());
            var result = assertTimeoutPreemptively(Duration.ofMillis(200), () -> {
                System.out.println(Thread.currentThread().getName());
                Thread.sleep(300);
                return userService.login("dummy", IVAN.getPassword());*/

                var result = assertTimeout(Duration.ofMillis(200), () -> {
                    Thread.sleep(300);
                    return userService.login("dummy", IVAN.getPassword());
                });
            }

            @Test
//    @org.junit.Test(expected = IllegalArgumentException.class) // было в JUnit4
            void throwExceptionIfUserNameOrPasswordIsNull () {
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
            void loginParametrizedTest (String name, String password, Optional < User > user){
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
