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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ibm.ssi.controller.hotel.HotelControllerApp;
import com.ibm.ssi.controller.hotel.client.ACAPYClient;
import com.ibm.ssi.controller.hotel.service.CheckInCredentialService;
import com.ibm.ssi.controller.hotel.service.ProofService;
import com.ibm.ssi.controller.hotel.service.dto.WebhookPresentProofDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//import com.ibm.ssi.controller.hotel.service.impl.GuestServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Webhooks for ACAPY", description = "https://github.com/hyperledger/aries-cloudagent-python/blob/master/AdminAPI.md#administration-api-webhooks")
@RestController
@RequestMapping("/topic")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(HotelControllerApp.class);

    @Autowired
    ACAPYClient acapyClient;

    @Autowired
    ProofService proofService;

    @Autowired
    CheckInCredentialService checkInCredentialService;

    @PostMapping("/present_proof")
    @Operation(security = @SecurityRequirement(name = "X-API-Key"))
    public ResponseEntity<Void> onProofRequestWebhook(@RequestBody WebhookPresentProofDTO webhookPresentProofDTO) throws JsonProcessingException {

        log.debug("Webhook for proof request with thread_id {}", webhookPresentProofDTO.getThreadId());
        log.debug("State of the proof: {}", webhookPresentProofDTO.getState());
        log.debug("Proof verified: {}", webhookPresentProofDTO.getVerified());

        this.proofService.handleProofWebhook(webhookPresentProofDTO);

        return ResponseEntity.noContent().build();
    }

}
