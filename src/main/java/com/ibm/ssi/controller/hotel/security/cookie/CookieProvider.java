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

package com.ibm.ssi.controller.hotel.security.cookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;

import com.ibm.ssi.controller.hotel.service.exceptions.InvalidCookieException;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class CookieProvider {
    private final Logger log = LoggerFactory.getLogger(CookieProvider.class);

    private SecureRandom secureRandom = new SecureRandom();

    public CookieProvider() {}

    public String resolveCookieFingerprint() {
        byte[] randomFgp = new byte[50];
        secureRandom.nextBytes(randomFgp);
        String userFingerprint = DatatypeConverter.printHexBinary(randomFgp);
        String fingerprintCookie = "Secure-Fgp=" + userFingerprint + "; SameSite=Strict; HttpOnly; Secure; Path=/";

        return fingerprintCookie;
    }

    public String resolveUserFingerprintHash(String userFingerprint) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] userFingerprintDigest = digest.digest(userFingerprint.getBytes("utf-8"));
        String userFingerprintHash = DatatypeConverter.printHexBinary(userFingerprintDigest);

        return userFingerprintHash;
    }

    public boolean validateCookie(HttpServletRequest request, String authToken) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidCookieException {
        String userFingerprint = null;

        if (request.getCookies() != null && request.getCookies().length > 0) {
            List<Cookie> cookies = Arrays.stream(request.getCookies()).collect(Collectors.toList());
            Optional<Cookie> cookie = cookies.stream().filter(c -> "Secure-Fgp".equals(c.getName())).findFirst();

            if (cookie.isPresent()) {
                userFingerprint = cookie.get().getValue();
            }
        } else {
            throw new InvalidCookieException();
        }

        try {
            String userFingerprintHash = resolveUserFingerprintHash(userFingerprint);
            
            Jwts
            .parserBuilder()
            .setSigningKey(userFingerprintHash)
            .require("HS512", SignatureAlgorithm.HS512)
            .require("userFingerprint", userFingerprintHash)
            .build();

            return true;
        } catch (NoSuchAlgorithmException e) {
            log.error("No such algorithm.", e);
        } catch (JwtException e) {
            log.error("Invalid JWT token.", e);
        } catch (IllegalArgumentException e) {
            log.error("Invalid JWT fingerprint trace.", e);
        }

        return false;
    }
}
