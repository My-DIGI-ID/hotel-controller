/*
 * Copyright 2021 Bundesrepublik Deutschland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.ssi.controller.hotel.security.jwt;

import com.ibm.ssi.controller.hotel.security.AuthoritiesConstants;
import com.ibm.ssi.controller.hotel.security.cookie.CookieProvider;
import com.ibm.ssi.controller.hotel.repository.JWTTokenBlacklistRepository;
import com.ibm.ssi.controller.hotel.service.dto.JWTTokenDTO;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import io.github.jhipster.config.JHipsterProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@SpringBootTest()
public class TokenProviderTest {

    @MockBean
	private JWTTokenBlacklistRepository mockRepository;

    private static final long ONE_MINUTE = 60000;

    private Key key;
    private TokenProvider tokenProvider;

    @BeforeEach
    public void setup() {
        tokenProvider = new TokenProvider( new JHipsterProperties(), new CookieProvider());
        key = Keys.hmacShaKeyFor(Decoders.BASE64
            .decode("fd54a45s65fds737b9aafcb3412e07ed99b267f33413274720ddbb7f6c5e64e9f14075f2d7ed041592f0b7657baf8"));

        ReflectionTestUtils.setField(tokenProvider, "key", key);
        ReflectionTestUtils.setField(tokenProvider, "tokenValidityInMilliseconds", ONE_MINUTE);
        ReflectionTestUtils.setField(tokenProvider, "jwtTokenBlacklistRepository", mockRepository);
    }

    @Test
    public void testReturnFalseWhenJWThasInvalidSignature() {
        mockEmptyBlacklistRepositoryCall();

        boolean isTokenValid = tokenProvider.validateToken(createTokenWithDifferentSignature());

        assertThat(isTokenValid).isEqualTo(false);
    }

    @Test
    public void testReturnFalseWhenJwtIsBlacklisted() {
        String token = createTokenWithDifferentSignature();
        mockFilledBlacklistRepositoryCall(token);

        boolean isTokenValid = tokenProvider.validateToken(token);

        assertThat(isTokenValid).isEqualTo(false);
    }

    @Test
    public void testReturnFalseWhenJWTisMalformed() throws UnsupportedEncodingException {
        Authentication authentication = createAuthentication();
        String token = tokenProvider.createToken(authentication, false);
        String invalidToken = token.substring(1);
        mockEmptyBlacklistRepositoryCall();

        boolean isTokenValid = tokenProvider.validateToken(invalidToken);

        assertThat(isTokenValid).isEqualTo(false);
    }

    @Test
    public void testReturnFalseWhenJWTisExpired() throws UnsupportedEncodingException {
        ReflectionTestUtils.setField(tokenProvider, "tokenValidityInMilliseconds", -ONE_MINUTE);

        Authentication authentication = createAuthentication();
        String token = tokenProvider.createToken(authentication, false);
        mockEmptyBlacklistRepositoryCall();

        boolean isTokenValid = tokenProvider.validateToken(token);

        assertThat(isTokenValid).isEqualTo(false);
    }

    @Test
    public void testReturnFalseWhenJWTisUnsupported() {
        String unsupportedToken = createUnsupportedToken();
        mockEmptyBlacklistRepositoryCall();

        boolean isTokenValid = tokenProvider.validateToken(unsupportedToken);

        assertThat(isTokenValid).isEqualTo(false);
    }

    @Test
    public void testReturnFalseWhenJWTisInvalid() {
        mockEmptyBlacklistRepositoryCall();

        boolean isTokenValid = tokenProvider.validateToken("");

        assertThat(isTokenValid).isEqualTo(false);
    }

    private Authentication createAuthentication() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.ANONYMOUS));
        return new UsernamePasswordAuthenticationToken("anonymous", "anonymous", authorities);
    }

    private String createUnsupportedToken() {
        return Jwts.builder()
            .setPayload("payload")
            .signWith(key, SignatureAlgorithm.HS512)
            .compact();
    }

    private String createTokenWithDifferentSignature() {
        Key otherKey = Keys.hmacShaKeyFor(Decoders.BASE64
            .decode("Xfd54a45s65fds737b9aafcb3412e07ed99b267f33413274720ddbb7f6c5e64e9f14075f2d7ed041592f0b7657baf8"));

        return Jwts.builder()
            .setSubject("anonymous")
            .signWith(otherKey, SignatureAlgorithm.HS512)
            .setExpiration(new Date(new Date().getTime() + ONE_MINUTE))
            .compact();
    }

    private void mockEmptyBlacklistRepositoryCall() {
        List<JWTTokenDTO> mockList = new ArrayList<JWTTokenDTO>();
        doReturn(mockList).when(mockRepository).findByToken(anyString());
    }

    private void mockFilledBlacklistRepositoryCall(String token) {
        List<JWTTokenDTO> mockList = new ArrayList<JWTTokenDTO>();
        mockList.add(new JWTTokenDTO(token));

        doReturn(mockList).when(mockRepository).findByToken(anyString());
    }
}
