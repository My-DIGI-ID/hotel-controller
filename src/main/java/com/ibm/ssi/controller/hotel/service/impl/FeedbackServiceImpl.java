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

import com.ibm.ssi.controller.hotel.client.TRELLOClient;
import com.ibm.ssi.controller.hotel.service.FeedbackService;
import com.ibm.ssi.controller.hotel.service.dto.FeedbackDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    private final Logger log = LoggerFactory.getLogger(FeedbackServiceImpl.class);

    @Autowired
    private TRELLOClient trelloClient;

    @Value("${ssibk.hotel.controller.trello.apikey}")
    private String apiKey;

    @Value("${ssibk.hotel.controller.trello.apitoken}")
    private String apiToken;

    @Value("${ssibk.hotel.controller.trello.idlist}")
    private String idList;

    @Value("${ssibk.hotel.controller.trello.issue}")
    private String issue;

    @Value("${ssibk.hotel.controller.trello.question}")
    private String question;

    @Value("${ssibk.hotel.controller.trello.comment}")
    private String comment;


    /**
     * Save Feedback in Trello
     *
     * @param feedbackDTO the entity to save.
     * @return the persisted entity.
     */

    @Override
    public void createFeedback(FeedbackDTO feedbackDTO) {

        log.debug(feedbackDTO.toString());
        String topic = this.getTopic(feedbackDTO);
        this.trelloClient.createCard(apiKey, apiToken, idList, feedbackDTO.getFeedback(), topic);
    }

    private String getTopic(FeedbackDTO feedbackDto) {
        String feedback = feedbackDto.getTopic().toString().toLowerCase();

        switch(feedback)
        {
            case "issue":
                feedback = issue;
                break;
            case "question":
                feedback = question;
                break;
            case "comment":
                feedback = comment;
                break;
            default:
                throw new IllegalArgumentException("Invalid label");
        }

        return feedback;
    }
}
