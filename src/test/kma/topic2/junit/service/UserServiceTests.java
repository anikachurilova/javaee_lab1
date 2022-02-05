package kma.topic2.junit.service;

import kma.topic2.junit.exceptions.UserNotFoundException;
import kma.topic2.junit.model.NewUser;
import kma.topic2.junit.model.User;
import kma.topic2.junit.repository.UserRepository;
import kma.topic2.junit.validation.UserValidator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
class UserServiceTests {
    @Autowired
    private UserService service;
    @Autowired
    private UserRepository userRepository;
    @SpyBean
    private UserValidator validator;
    @Captor
    private ArgumentCaptor<Logger> logCaptor;

    // getUserByLogin method tests
    @Test
    void getUserByLogin_ExistingLoginPassed_UserSuccessfullyReturned() {
        userRepository.saveNewUser(
                NewUser.builder().login("login").password("1234").fullName("user").build());

        assertThat(service.getUserByLogin("login"))
                .returns("login", User::getLogin)
                .returns("1234", User::getPassword)
                .returns("user", User::getFullName);
    }

    @Test
    void getUserByLogin_NonExistingLoginPassed_ExceptionThrown() {
        assertThatThrownBy(() -> service.getUserByLogin("login"))
                .isInstanceOfSatisfying(
                        UserNotFoundException.class,
                        ex -> assertThat(ex.getMessage()).isEqualTo("Can't find user by login: login"));
    }

    // createNewUser method tests
    @Test
    void createNewUser_ValidUserPassed_UserSuccessfullyAdded() {
        service.createNewUser(
                NewUser.builder().login("login").password("1234").fullName("user").build());

        assertThat(userRepository.getUserByLogin("login"))
                .returns("login", User::getLogin)
                .returns("1234", User::getPassword)
                .returns("user", User::getFullName);

        verify(validator).validateNewUser(
                NewUser.builder().login("login").password("1234").fullName("user").build());

        verify(validator).validateNewUser(any());
    }
}