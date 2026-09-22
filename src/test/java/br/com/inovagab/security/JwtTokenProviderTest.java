package br.com.inovagab.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private static final String SECRET =
            "my-dev-secret-for-jwt-generation-which-should-be-changed-in-production";

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", SECRET);
    }

    @Test
    void geraEValidaToken() {
        String token = provider.generateToken("user-123", "GESTOR");

        assertNotNull(token);
        assertTrue(provider.validateToken(token));
        assertEquals("user-123", provider.getUserIdFromJwt(token));
        assertEquals("GESTOR", provider.getRoleFromJwt(token));
    }

    @Test
    void tokenInvalidoEhRejeitado() {
        assertFalse(provider.validateToken("token.falso.invalido"));
        assertFalse(provider.validateToken(""));
    }

    @Test
    void tokenContemClaimDeGrupo() {
        String token = provider.generateToken("user-1", "LIDER", "GRP-ABC123");

        assertEquals("GRP-ABC123", provider.getGroupIdFromJwt(token));
    }
}