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

import javax.validation.Valid;

import com.ibm.ssi.controller.hotel.service.FeedbackService;
import com.ibm.ssi.controller.hotel.service.dto.FeedbackDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Feedback", description = "CRUD for Feedback operations")
@RestController
@RequestMapping("/api")
public class FeedbackController {

    private final Logger log = LoggerFactory.getLogger(FeedbackController.class);

    @Autowired
    FeedbackService feedbackService;


        /**
     * {@code POST  /feedback} : Create new Feedback.
     *
     * @body feedbackDTO
     * @return the {@link ResponseEntity} with status {@code 204 or with status {@code 400 (Bad Request)} if
     *         something goes wrong.
     */
    @PostMapping("/feedback")
    @Operation
    @SecurityRequirement(name = "X-API-Key")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> createFeedback(@Valid @RequestBody FeedbackDTO feedbackDTO) {

        this.feedbackService.createFeedback(feedbackDTO);

        log.debug("REST request to create new Feedback: {}", feedbackDTO);


        return ResponseEntity.noContent().build();
    }

}
