package com.newsapp.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT 工具类单元测试
 */
@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private static final String TEST_SECRET = "TestSecretKeyForJWTTokenGenerationMustBeLongEnough";
    private static final Long TEST_EXPIRATION = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        // 使用反射设置私有字段
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", TEST_EXPIRATION);
    }

    @Test
    void testGenerateToken() {
        String username = "testuser";
        Long userId = 1L;

        String token = jwtUtil.generateToken(username, userId);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains(".")); // JWT 应该包含点
    }

    @Test
    void testGetUsernameFromToken() {
        String username = "testuser";
        Long userId = 1L;
        String token = jwtUtil.generateToken(username, userId);

        String extractedUsername = jwtUtil.getUsernameFromToken(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    void testGetUserIdFromToken() {
        String username = "testuser";
        Long userId = 1L;
        String token = jwtUtil.generateToken(username, userId);

        Long extractedUserId = jwtUtil.getUserIdFromToken(token);

        assertEquals(userId, extractedUserId);
    }

    @Test
    void testValidateToken_Valid() {
        String username = "testuser";
        Long userId = 1L;
        String token = jwtUtil.generateToken(username, userId);

        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testValidateToken_Invalid() {
        String invalidToken = "invalid.token.string";

        assertFalse(jwtUtil.validateToken(invalidToken));
    }

    @Test
    void testValidateToken_Empty() {
        assertFalse(jwtUtil.validateToken(""));
    }

    @Test
    void testValidateToken_Null() {
        assertFalse(jwtUtil.validateToken(null));
    }

    @Test
    void testGetExpirationTime() {
        Long expiration = jwtUtil.getExpirationTime();

        assertNotNull(expiration);
        assertEquals(TEST_EXPIRATION, expiration);
    }

    @Test
    void testGenerateToken_DifferentUsers() {
        String token1 = jwtUtil.generateToken("user1", 1L);
        String token2 = jwtUtil.generateToken("user2", 2L);

        assertNotEquals(token1, token2);
        assertEquals("user1", jwtUtil.getUsernameFromToken(token1));
        assertEquals("user2", jwtUtil.getUsernameFromToken(token2));
        assertEquals(1L, jwtUtil.getUserIdFromToken(token1));
        assertEquals(2L, jwtUtil.getUserIdFromToken(token2));
    }
}
