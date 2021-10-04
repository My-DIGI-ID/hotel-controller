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

package com.ibm.ssi.controller.hotel.service.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {
    private LoadingCache<String, Integer> attemptsCache;
    private final int MAX_ATTEMPT = 2;
    private final int TIME_AMOUNT_TO_UNBLOCK_IP = 2;
    private final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);
    private int attempts = 0;
    private Instant timeWhenIPisUnblocked; 
    
    public LoginAttemptService() {
        super();
        attemptsCache = CacheBuilder.newBuilder().expireAfterWrite(TIME_AMOUNT_TO_UNBLOCK_IP, TimeUnit.HOURS).build(new CacheLoader<String, Integer>() {
            public Integer load(String key) {
                return 0;
            }
        });
    }

    public int getRemainingLoginAttempts() {
        return MAX_ATTEMPT - attempts + 1;
    }

    public Instant getTimeWhenIPisUnblocked() {
        return timeWhenIPisUnblocked;
    }

    public void loginSucceeded(String key) {
        attemptsCache.invalidate(key);
        attempts = 0;
    }

    public void loginFailed(String key) {
        log.debug("Attempts failed: " + attempts);
        try {
            attempts = attemptsCache.get(key);
        } catch (ExecutionException e) {
            attempts = 1;
        }
        generateTimeStamp();
        attempts++;
        attemptsCache.put(key, attempts);
    }

    public boolean isBlocked(String key) {
        try {
            log.debug("Key: " + key + " Cache: " + attemptsCache.get(key));
            if (timeWhenIPisUnblocked != null) {
                if (Instant.now().getEpochSecond() - timeWhenIPisUnblocked.getEpochSecond() >= 0) {
                    unblockIP(key);
                    return false;
                }
            }
            return attemptsCache.get(key) >= MAX_ATTEMPT;
        } catch (ExecutionException e) {
            return false;
        }
    }

    public void unblockIP(String key) {
        attemptsCache.invalidate(key);
        attempts = 0;
        timeWhenIPisUnblocked = null;
    }

    public void generateTimeStamp() {
        if (attempts == MAX_ATTEMPT) {
            Instant currentTime = Instant.now();
            timeWhenIPisUnblocked = currentTime.plus(TIME_AMOUNT_TO_UNBLOCK_IP, ChronoUnit.HOURS);
        }
    }   
}
