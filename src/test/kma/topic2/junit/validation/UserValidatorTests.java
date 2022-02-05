package kma.topic2.junit.validation;

import kma.topic2.junit.exceptions.ConstraintViolationException;
import kma.topic2.junit.exceptions.LoginExistsException;
import kma.topic2.junit.model.NewUser;
import kma.topic2.junit.repository.UserRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserValidatorTests {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserValidator service;

    @org.junit.jupiter.api.Test
    void validateNewUser_ValidUserPassed_SuccessfullyValidated() {
        service.validateNewUser(
                NewUser.builder().login("login").password("1As").fullName("user").build());

        verify(userRepository).isLoginExists("login");
    }

    @org.junit.jupiter.api.Test
    void validateNewUser_ExistedUserPassed_ExceptionThrown() {
        when(userRepository.isLoginExists("login")).thenReturn(true);

        assertThatThrownBy(() -> service.validateNewUser(
                NewUser.builder().login("login").password("123").fullName("user").build()))
                .isInstanceOf(LoginExistsException.class);
    }

    @ParameterizedTest
    @MethodSource("invalidSizePasswordTestData")
    void validateNewUser_PasswordWithInvalidSizePassed_ExceptionThrown(String password, List<String> errors) {
        assertThatThrownBy(() -> service.validateNewUser(
                NewUser.builder().login("login").password(password).fullName("user").build()))
                .isInstanceOfSatisfying(
                        ConstraintViolationException.class,
                        ex -> assertThat(ex.getErrors()).isEqualTo(errors));
    }

    @org.junit.jupiter.api.Test
    void validateNewUser_PasswordFailedRegex_ExceptionThrown() {
        assertThatThrownBy(() -> service.validateNewUser(
                NewUser.builder().login("login").password("12*j").fullName("user").build()))
                .isInstanceOfSatisfying(
                        ConstraintViolationException.class,
                        ex -> assertThat(ex.getErrors()).isEqualTo(Arrays.asList("Password doesn't match regex")));
    }

    @ParameterizedTest
    @MethodSource("passwordWithMultipleErrorsTestData")
    void validateNewUser_PasswordWithMultipleErrorsPassed_ExceptionThrown(String password, List<String> errors) {
        assertThatThrownBy(() -> service.validateNewUser(
                NewUser.builder().login("TestLogin").password(password).fullName("Test").build()))
                .isInstanceOfSatisfying(ConstraintViolationException.class,
                        ex -> assertThat(ex.getErrors()).isEqualTo(errors));

    }

    private static Stream<Arguments> invalidSizePasswordTestData(){
        return Stream.of(
                Arguments.of("aa", Arrays.asList("Password has invalid size")),
                Arguments.of("aaaaaaaaaa", Arrays.asList("Password has invalid size"))
        );
    }

    private static Stream<Arguments> passwordWithMultipleErrorsTestData(){
        return Stream.of(
                Arguments.of("12(345678",Arrays.asList("Password has invalid size","Password doesn't match regex")),
                Arguments.of("$$",Arrays.asList("Password has invalid size","Password doesn't match regex"))
        );
    }
}