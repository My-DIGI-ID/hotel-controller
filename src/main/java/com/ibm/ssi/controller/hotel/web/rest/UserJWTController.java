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

package com.ibm.ssi.controller.hotel.web.rest;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.ibm.ssi.controller.hotel.security.cookie.CookieProvider;
import com.ibm.ssi.controller.hotel.security.jwt.JWTFilter;
import com.ibm.ssi.controller.hotel.security.jwt.TokenProvider;
import com.ibm.ssi.controller.hotel.service.JWTTokenService;
import com.ibm.ssi.controller.hotel.service.dto.JWTTokenDTO;
import com.ibm.ssi.controller.hotel.service.exceptions.JWTTokenAlreadyBlacklisted;
import com.ibm.ssi.controller.hotel.service.impl.LoginAttemptService;
import com.ibm.ssi.controller.hotel.web.rest.vm.LoginVM;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.WebUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * Controller to authenticate users.
 */
@Tag(name = "Authentication")
@RestController
@RequestMapping("/api")
public class UserJWTController {

    private final TokenProvider tokenProvider;
    
    private final CookieProvider cookieProvider;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private JWTTokenService jwtTokenService;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public UserJWTController(TokenProvider tokenProvider, CookieProvider cookieProvider, AuthenticationManagerBuilder authenticationManagerBuilder) {
        this.tokenProvider = tokenProvider;
        this.cookieProvider = cookieProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<JWTToken> authorize(@Valid @RequestBody LoginVM loginVM) throws UnsupportedEncodingException {
        HttpHeaders httpHeaders = new HttpHeaders();
        String ip = getClientIP();
        Boolean ipBlocked = loginAttemptService.isBlocked(ip);
        try {
            if (loginAttemptService.getTimeWhenIPisUnblocked() == null) {
                return authenticate(loginVM, httpHeaders);
            } else {
                return this.returnTimeWhenIPunblocked(httpHeaders);
            }
        } catch (RuntimeException e) {
            if (ipBlocked) {
                return this.returnTimeWhenIPunblocked(httpHeaders);
            }
            return this.returnRemainingLoginAttempts(httpHeaders);
        }
    }

    @PostMapping("/logout")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<JWTTokenDTO> logout(@Valid @RequestBody JWTTokenDTO jwtToken, HttpServletRequest request, HttpServletResponse response) {
        JWTTokenDTO blacklistedToken;
        
        try {
            blacklistedToken = this.jwtTokenService.addToBlacklist(jwtToken);
        } catch (JWTTokenAlreadyBlacklisted e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        Cookie secureFgpCookie = WebUtils.getCookie(request, "Secure-Fgp");
        if (secureFgpCookie != null) {
            secureFgpCookie.setMaxAge(0);
            secureFgpCookie.setPath("/");
            response.addCookie(secureFgpCookie);
        }
        
        return new ResponseEntity<JWTTokenDTO>(blacklistedToken, HttpStatus.CREATED);
    }

    private ResponseEntity<JWTToken> authenticate(@Valid @RequestBody LoginVM loginVM, HttpHeaders httpHeaders) throws UnsupportedEncodingException {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginVM.getUsername(), loginVM.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        boolean rememberMe = (loginVM.isRememberMe() == null) ? false : loginVM.isRememberMe();
        String jwt = tokenProvider.createToken(authentication, rememberMe);

        httpHeaders.add(JWTFilter.SET_COOKIE_HEADER, cookieProvider.resolveCookieFingerprint());
        httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);

        return new ResponseEntity<>(new JWTToken(jwt), httpHeaders, HttpStatus.OK);
    }

    private String getClientIP() {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    private ResponseEntity<JWTToken> returnTimeWhenIPunblocked(HttpHeaders httpHeaders) {
        String timeWhenLoginIsUnblocked = loginAttemptService.getTimeWhenIPisUnblocked().toString();
        JWTToken response = new JWTToken(timeWhenLoginIsUnblocked);
        return new ResponseEntity<>(response, httpHeaders, HttpStatus.LOCKED);
    }

    private ResponseEntity<JWTToken> returnRemainingLoginAttempts(HttpHeaders httpHeaders) {
        int remainingLoginAttempts = loginAttemptService.getRemainingLoginAttempts();
        JWTToken response = new JWTToken(Integer.toString(remainingLoginAttempts));
        return new ResponseEntity<>(response, httpHeaders, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Object to return as body in JWT Authentication.
     */
    static class JWTToken {

        @NotNull
        private String idToken;

        JWTToken(String idToken) {
            this.idToken = idToken;
        }

        @JsonProperty("id_token")
        String getIdToken() {
            return idToken;
        }

        void setIdToken(String idToken) {
            this.idToken = idToken;
        }
    }
}
