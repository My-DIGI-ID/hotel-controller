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

import java.util.List;

import com.ibm.ssi.controller.hotel.client.IntegrationServiceClient;
import com.ibm.ssi.controller.hotel.repository.CheckInCredentialRepository;
import com.ibm.ssi.controller.hotel.service.IntegrationService;
import com.ibm.ssi.controller.hotel.service.NotificationService;
import com.ibm.ssi.controller.hotel.service.dto.BookingDataDTO;
import com.ibm.ssi.controller.hotel.service.dto.CheckInCredentialDTO;
import com.ibm.ssi.controller.hotel.service.dto.PMSDataDTO;
import com.ibm.ssi.controller.hotel.service.exceptions.CouldNotSendPMSDataException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class IntegrationServiceImpl implements IntegrationService {

    @Autowired
    IntegrationServiceClient integrationServiceClient;

    @Autowired
    CheckInCredentialRepository checkInCredentialRepository;

    @Autowired
    NotificationService notificationService;

    @Value("${ssibk.hotel.controller.integrationservice.apikey}")
    private String apiKey;

    @Override
    public void sendDataToPMS(PMSDataDTO pmsDataDTO) throws CouldNotSendPMSDataException {
        this.integrationServiceClient.sendDataToPMS(apiKey, pmsDataDTO);

        try {
            this.integrationServiceClient.sendDataToPMS(apiKey, pmsDataDTO);
        } catch (Exception e) {
            throw new CouldNotSendPMSDataException();
        }

        String id = pmsDataDTO.getCheckInCredential().getId();
        String hotelId = pmsDataDTO.getCheckInCredential().getHotelId();
        String deskId = pmsDataDTO.getCheckInCredential().getDeskId();
        this.checkInCredentialRepository.deleteById(id);
        this.notificationService.sendNotificationAboutNewCheckinCredentials(hotelId, deskId);
    }

    @Override
    public List<BookingDataDTO> lookup(String bookingNumber, CheckInCredentialDTO checkInCredentialDTO) {

        return this.integrationServiceClient.lookup(apiKey, bookingNumber, checkInCredentialDTO);
    }
}
