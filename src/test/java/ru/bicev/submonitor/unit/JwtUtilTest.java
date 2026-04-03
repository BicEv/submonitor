package ru.bicev.submonitor.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import ru.bicev.submonitor.security.JwtUtil;
import ru.bicev.submonitor.security.UserDetailsImpl;

@ExtendWith(MockitoExtension.class)
public class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private String jwtSecret = "05ec53d133777eeaefc192c695d26efe33a4f45e516f566cdbcb49635349b405";
    private int jwtExpirationMs = 604800000;
    private UserDetailsImpl principal;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", jwtExpirationMs);
        principal = new UserDetailsImpl(1L, "testUser", "encodedPass", "test@mail.com", List.of());
    }

    @Test
    @DisplayName("Must generate JWT")
    void generateToken_Success() {
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null);

        var result = jwtUtil.generateToken(auth);

        assertNotNull(result);
        assertTrue(result.length() > 1);
    }

    @Test
    @DisplayName("Must throw exception when auth is null")
    void generateToken_ThrowsException() {
        assertThrows(Exception.class, () -> jwtUtil.generateToken(null));
    }

    @Test
    @DisplayName("Must return username from token")
    void getUsernameFromJwtToken_Success() {
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null);

        var token = jwtUtil.generateToken(auth);

        var result = jwtUtil.getUsernameFromJwtToken(token);

        assertEquals(principal.getUsername(), result);
    }

    @Test
    @DisplayName("Must throw an exception when token is invalid")
    void getUsernameFromJwtToken_ThrowsException() {
        String invalidToken = "abcdabcd";

        assertThrows(Exception.class, () -> jwtUtil.getUsernameFromJwtToken(invalidToken));
    }

    @Test
    @DisplayName("Must return id from valid token")
    void getIdFromJwtToken_Success() {
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null);

        var token = jwtUtil.generateToken(auth);

        var result = jwtUtil.getIdFromJwtToken(token);

        assertEquals(principal.getId(), result);
    }

    @Test
    @DisplayName("Must throw an exception when token is invalid")
    void getIdFromJwtToken_ThrowsException() {
        String invalidToken = "zxcvngjdngkjngkjnkjgnsjdgnlkjdsnglksng";

        assertThrows(Exception.class, () -> jwtUtil.getIdFromJwtToken(invalidToken));
    }

    @Test
    @DisplayName("Must validate valid token")
    void validateJwtToken_Success() {
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null);

        var token = jwtUtil.generateToken(auth);

        assertTrue(jwtUtil.validateJwtToken(token));
    }

    @Test
    @DisplayName("Must not validate invalid token")
    void validateJwtToken_InvalidToken() {
        String invalidToken = "ehnfiujsnhfoiwjfpjwognerwgnwmgpoqwejfpoqm";

        assertFalse(jwtUtil.validateJwtToken(invalidToken));
    }

}
