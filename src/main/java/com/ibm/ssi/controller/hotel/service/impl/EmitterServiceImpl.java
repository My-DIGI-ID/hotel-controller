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

import java.util.UUID;

import com.ibm.ssi.controller.hotel.repository.EmitterRepository;
import com.ibm.ssi.controller.hotel.service.EmitterService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class EmitterServiceImpl implements EmitterService {

    private static final Logger LOG = LoggerFactory.getLogger(EmitterServiceImpl.class);

    @Autowired
    private EmitterRepository repository;

    @Value("${ssibk.hotel.controller.events.connection.timeout}")
    private long timeout;

    @Override
    public SseEmitter createEmitter(String hotelId, String deskId) {
        String uuid = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(timeout);

        emitter.onCompletion(() -> {
            LOG.debug("onCompletion called on emitter {}", emitter);
            repository.remove(uuid);
        });
        emitter.onTimeout(() -> {
            LOG.debug("onTimeout called on emitter {}", emitter);
            repository.remove(uuid);
        });
        emitter.onError(e -> {
            LOG.debug("onError called on emitter {}", emitter);
            repository.remove(uuid);
        });
        repository.addEmitter(uuid, hotelId, deskId, emitter);

        return emitter;
    }
}
