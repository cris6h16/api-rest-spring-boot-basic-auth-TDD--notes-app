package org.cris6h16.apirestspringboot.Config.Security.EventListener;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Utils.FilesUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link AuthenticationListener}
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@Tag("UnitTest")
class AuthenticationListenerTest {


    @Mock
    private FilesUtils filesUtils;

    @InjectMocks
    private AuthenticationListener authenticationListener;

    @BeforeEach
    void setUp() {
        authenticationListener.successData.clear();
        authenticationListener.failureData.clear();

        authenticationListener.lastSuccessFlushed = 0L;
        authenticationListener.lastFailureFlushed = 0L;

        clearInvocations(filesUtils);
        reset(filesUtils);

    }


    @Test
    void testOnSuccess() {
        // Arrange
        AuthenticationSuccessEvent successEvent = mock(AuthenticationSuccessEvent.class);
        Authentication authentication = mock(Authentication.class);
        when(successEvent.getAuthentication()).thenReturn(authentication);

        // Act
        authenticationListener.onSuccess(successEvent);

        // Assert
        // Verify that the `successData` list is updated and flushInFile is called
        assertTrue(authenticationListener.successData.isEmpty());
        verify(filesUtils, atLeastOnce()).appendToFile(
                eq(Path.of(Cons.Logs.SUCCESS_AUTHENTICATION_FILE)),
                anyString()
        );
    }


    @Test
    void testOnFailure() {
        // Arrange
        AbstractAuthenticationFailureEvent failureEvent = mock(AbstractAuthenticationFailureEvent.class);
        Authentication authentication = mock(Authentication.class);
        AuthenticationException exception = mock(AuthenticationException.class);
        when(failureEvent.getAuthentication()).thenReturn(authentication);
        when(failureEvent.getException()).thenReturn(exception);

        // Act
        authenticationListener.onFailure(failureEvent);

        // Assert
        // Verify that the `failureData` list is updated and flushInFile is called
        assertTrue(authenticationListener.failureData.isEmpty());
        verify(filesUtils, atLeastOnce()).appendToFile(
                eq(Path.of(Cons.Logs.FAIL_AUTHENTICATION_FILE)),
                anyString()
        );
    }


    @Test
    void flushSuccessInFile_justCollected_successData() {
        // Arrange
        AuthenticationListener.SuccessData successData =
                new AuthenticationListener.SuccessData(
                        mock(Authentication.class),
                        System.currentTimeMillis()
                );

        authenticationListener.successData.add(successData);

        // Act
        authenticationListener.flushSuccessInFile();

        // Assert
        // Verify that `appendToFile` is just 1 time with the exact provided content
        assertTrue(authenticationListener.successData.isEmpty());
        verify(filesUtils, times(1)).appendToFile(
                eq(Path.of(Cons.Logs.SUCCESS_AUTHENTICATION_FILE)),
                argThat(content -> content.equals(successData.toString() + "\n"))// make sure that tha las char is a new line
        );
        // Verify that `appendToFile` is never called for failureData
        verify(filesUtils, never()).appendToFile(
                eq(Path.of(Cons.Logs.FAIL_AUTHENTICATION_FILE)),
                anyString()
        );
    }


    @Test
    void flushFailureInFile_justCollected_failureData() {
        // Arrange
        AuthenticationListener.FailureData failureData =
                new AuthenticationListener.FailureData(
                        mock(Authentication.class),
                        mock(AuthenticationException.class),
                        System.currentTimeMillis()
                );

        authenticationListener.failureData.add(failureData);

        // Act
        authenticationListener.flushFailureInFile();

        // Assert
        // Verify that `appendToFile` is just 1 time with the exact provided content
        assertTrue(authenticationListener.failureData.isEmpty());
        verify(filesUtils, times(1)).appendToFile(
                eq(Path.of(Cons.Logs.FAIL_AUTHENTICATION_FILE)),
                argThat(content -> content.equals(failureData.toString() + "\n"))// make sure that tha las char is a new line
        );

        // Verify that `appendToFile` is never called for successData
        verify(filesUtils, never()).appendToFile(
                eq(Path.of(Cons.Logs.SUCCESS_AUTHENTICATION_FILE)),
                anyString()
        );
    }


    @Test
    @Tag("flushInFile")
    void testFlushInFile_bothCollected() {
        // Arrange
        AuthenticationListener.SuccessData successData =
                new AuthenticationListener.SuccessData(
                        mock(Authentication.class),
                        System.currentTimeMillis()
                );

        AuthenticationListener.FailureData failureData =
                new AuthenticationListener.FailureData(
                        mock(Authentication.class),
                        mock(AuthenticationException.class),
                        System.currentTimeMillis()
                );

        authenticationListener.successData.add(successData);
        authenticationListener.failureData.add(failureData);

        // Act
        authenticationListener.flushFailureInFile();
        authenticationListener.flushSuccessInFile();

        // Assert
        // Verify that `appendToFile` is called 2 times, one for successData and one for failureData
        assertTrue(authenticationListener.successData.isEmpty());
        verify(filesUtils, times(1)).appendToFile(
                eq(Path.of(Cons.Logs.SUCCESS_AUTHENTICATION_FILE)),
                argThat(content -> content.equals(successData.toString() + "\n"))
        );

        assertTrue(authenticationListener.failureData.isEmpty());
        verify(filesUtils, times(1)).appendToFile(
                eq(Path.of(Cons.Logs.FAIL_AUTHENTICATION_FILE)),
                argThat(content -> content.equals(failureData.toString() + "\n"))
        );
    }


    @Test
    @Tag("both")
    void testFlushInFile_bothCollected_List10Elements() {
        // Arrange
        for (int i = 0; i < 10; i++) {
            AuthenticationListener.SuccessData successData =
                    new AuthenticationListener.SuccessData(
                            mock(Authentication.class),
                            System.currentTimeMillis()
                    );

            AuthenticationListener.FailureData failureData =
                    new AuthenticationListener.FailureData(
                            mock(Authentication.class),
                            mock(AuthenticationException.class),
                            System.currentTimeMillis()
                    );

            authenticationListener.successData.add(successData);
            authenticationListener.failureData.add(failureData);
        }

        // Act
        authenticationListener.flushFailureInFile();
        authenticationListener.flushSuccessInFile();

        // Assert
        // Verify that `appendToFile` is called 2 times, one for successData and one for failureData
        assertTrue(authenticationListener.successData.isEmpty());
        verify(filesUtils, times(1)).appendToFile(
                eq(Path.of(Cons.Logs.SUCCESS_AUTHENTICATION_FILE)),
                argThat(content -> content.split("\n").length == 10)
        );

        assertTrue(authenticationListener.failureData.isEmpty());
        verify(filesUtils, times(1)).appendToFile(
                eq(Path.of(Cons.Logs.FAIL_AUTHENTICATION_FILE)),
                argThat(content -> content.split("\n").length == 10)
        );
    }


    @Test
    void testFlushInFile_bothCollected_Concurrent() {
        // Arrange
        ExecutorService executor = Executors.newFixedThreadPool(50);

        for (int j = 0; j < 50; j++) {
            executor.submit(() -> {
                for (int i = 0; i < 30; i++) {
                    AuthenticationSuccessEvent successEvent = mock(AuthenticationSuccessEvent.class);
                    Authentication authentication = mock(Authentication.class);
                    when(successEvent.getAuthentication()).thenReturn(authentication);

                    AbstractAuthenticationFailureEvent failureEvent = mock(AbstractAuthenticationFailureEvent.class);
                    AuthenticationException exception = mock(AuthenticationException.class);
                    when(failureEvent.getAuthentication()).thenReturn(authentication);
                    when(failureEvent.getException()).thenReturn(exception);

                    authenticationListener.onSuccess(successEvent);
                    authenticationListener.onFailure(failureEvent);
                }
            });
        }


        // Shutdown the executor and wait for all tasks to complete
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    fail("Executor did not terminate");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }


        // e.g 4 threads hit the 2nd lock at the same time (then arr.length == 4), then the 1st lock is released (3 waiting), when it leaves the lock, It would have passed to be saved 4 list elements, then the expected value would be ( (50 * 30) - 4 )
        int successDataSize = 50 * 30;
        int failureDataSize = 50 * 30;
        int afterSuccessSize = authenticationListener.successData.size();
        int afterFailureSize = authenticationListener.failureData.size();

        assertThat(afterSuccessSize).isLessThan(successDataSize); // can be less based on the threads performance
        assertThat(afterFailureSize).isLessThan(failureDataSize); // can be less based on the threads performance

        int successDataSaved = successDataSize - afterSuccessSize; // can be more than 1 ( based on the threads speed )
        int failureDataSaved = failureDataSize - afterFailureSize; // can be more than 1 ( based on the threads speed )

        verify(filesUtils, times(1)).appendToFile(
                eq(Path.of(Cons.Logs.SUCCESS_AUTHENTICATION_FILE)),
                argThat(str -> str.split("\n").length == successDataSaved)
        );

        verify(filesUtils, times(1)).appendToFile(
                eq(Path.of(Cons.Logs.FAIL_AUTHENTICATION_FILE)),
                argThat(str -> str.split("\n").length == failureDataSaved)
        );
    }
}