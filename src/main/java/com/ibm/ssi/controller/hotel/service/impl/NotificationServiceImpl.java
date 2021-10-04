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

import java.io.IOException;
import java.util.Map;

import com.ibm.ssi.controller.hotel.repository.EmitterRepository;
import com.ibm.ssi.controller.hotel.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    private EmitterRepository emitterRepository;

    @Override
    public void sendNotificationAboutNewCheckinCredentials(String hotelId, String deskId) {
        LOG.debug("Informing subscribers about new checkin-credential for hotelId and deskId: {} {}", hotelId, deskId);
        doSendNotification(hotelId, deskId, NEW_CHECKIN_CREDENTIAL);
    }

    private void doSendNotification(String hotelId, String deskId, String eventName) {
        Map<String, SseEmitter> emitters = emitterRepository.findByHotelIdAndDeskId(hotelId, deskId);
        LOG.debug("Found {} emitters for hotelId and deskId: {} {}", emitters.size(), hotelId, deskId);
        for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
            try {
                LOG.debug("Sending eventName: {} for hotelId and deskId: {} {}", eventName, hotelId, deskId);
                entry.getValue().send(event().name(eventName).data("{}"));
            } catch (IOException | IllegalStateException e) {
                LOG.debug("Error while sending eventName: {} for hotelId and deskId: {} {} - exception: {}", eventName, hotelId, deskId, e);
                emitterRepository.remove(entry.getKey());
            }
        }
    }

}
