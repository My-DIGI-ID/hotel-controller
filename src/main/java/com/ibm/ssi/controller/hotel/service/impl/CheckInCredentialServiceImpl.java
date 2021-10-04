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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ibm.ssi.controller.hotel.domain.CheckInCredential;
import com.ibm.ssi.controller.hotel.repository.CheckInCredentialRepository;
import com.ibm.ssi.controller.hotel.service.CheckInCredentialService;
import com.ibm.ssi.controller.hotel.service.HotelService;
import com.ibm.ssi.controller.hotel.service.NotificationService;
import com.ibm.ssi.controller.hotel.service.dto.CheckInCredentialDTO;
import com.ibm.ssi.controller.hotel.service.dto.CorporateIdDTO;
import com.ibm.ssi.controller.hotel.service.dto.HotelDTO;
import com.ibm.ssi.controller.hotel.service.dto.MasterIdDTO;
import com.ibm.ssi.controller.hotel.service.exceptions.CannotFindMyHotelException;
import com.ibm.ssi.controller.hotel.service.exceptions.CheckinCredentialNotFoundException;
import com.ibm.ssi.controller.hotel.service.mapper.CheckInCredentialMapper;
import com.ibm.ssi.controller.hotel.service.mapper.CorporateIdMapper;
import com.ibm.ssi.controller.hotel.service.mapper.MasterIdMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CheckInCredentialServiceImpl implements CheckInCredentialService {

    private static final Logger LOG = LoggerFactory.getLogger(CheckInCredentialServiceImpl.class);

    private final HotelService hotelService;

    private final CheckInCredentialMapper checkInCredentialMapper;

    private final MasterIdMapper masterIdMapper;

    private final CorporateIdMapper corporateIdMapper;

    private final CheckInCredentialRepository checkInCredentialRepository;

    private final NotificationService notificationService;

    public CheckInCredentialServiceImpl(HotelService hotelService, CheckInCredentialMapper checkInCredentialMapper, MasterIdMapper masterIdMapper, CorporateIdMapper corporateIdMapper, CheckInCredentialRepository checkInCredentialRepository, NotificationService notificationService) {
        this.hotelService = hotelService;
        this.checkInCredentialMapper = checkInCredentialMapper;
        this.masterIdMapper = masterIdMapper;
        this.corporateIdMapper = corporateIdMapper;
        this.checkInCredentialRepository = checkInCredentialRepository;
        this.notificationService = notificationService;
    }

    @Override
    public List<CheckInCredentialDTO> getDeskCredentials(String deskId) throws CannotFindMyHotelException {
        LOG.debug("Getting the DeskId");
        Optional<HotelDTO> getMyHotel = hotelService.getMyHotel();
        if (!getMyHotel.isPresent()) {
            throw new CannotFindMyHotelException();
        }
        return checkInCredentialRepository
            .findByHotelIdAndDeskIdAndSendDateIsNotNullOrderByScanDateAsc(getMyHotel.get().getId(), deskId).stream()
            .map(checkInCredentialMapper::checkInCredentialToCheckInCredentialDTO)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void createCheckInCredential(String hotelId, String deskId, String presentationExchangeId) {

        CheckInCredential checkInCredential = new CheckInCredential(hotelId, deskId, presentationExchangeId);
        checkInCredential.setScanDate(new Date());

        checkInCredentialRepository.save(checkInCredential);
        notificationService.sendNotificationAboutNewCheckinCredentials(hotelId, deskId);
    }

    @Override
    public CheckInCredential updateCheckinCredential(String presentationExchangeId, MasterIdDTO masterIdDTO, CorporateIdDTO corporateIdDTO)
        throws CheckinCredentialNotFoundException {

        Optional<CheckInCredential> checkInCredentialOptional = checkInCredentialRepository.findOneByPresentationExchangeId(presentationExchangeId);
        if (checkInCredentialOptional.isPresent()) {
            CheckInCredential checkInCredential = checkInCredentialOptional.get();
            checkInCredential.setCorporateId(corporateIdMapper.corporateIdDTOToCorporateId(corporateIdDTO));
            checkInCredential.setMasterId(masterIdMapper.masterIdDTOToMasterId(masterIdDTO));
            checkInCredential.setSendDate(new Date());

            CheckInCredential updatedCredential = checkInCredentialRepository.save(checkInCredential);
            notificationService.sendNotificationAboutNewCheckinCredentials(updatedCredential.getHotelId(), updatedCredential.getDeskId());

            return updatedCredential;
        } else {
            throw new CheckinCredentialNotFoundException();
        }
    }

    @Override
    public CheckInCredential updateValidity(String presentationExchangeId, boolean proofVerified)
        throws CheckinCredentialNotFoundException {
        Optional<CheckInCredential> checkInCredentialOptional = checkInCredentialRepository.findOneByPresentationExchangeId(presentationExchangeId);
        if (checkInCredentialOptional.isPresent()) {

            // get the existing checkInCredential
            CheckInCredential checkInCredential = checkInCredentialOptional.get();

            // sets the checkinCredential valid if the proof is verified
            checkInCredential.setValid(proofVerified);

            CheckInCredential updatedCredential = checkInCredentialRepository.save(checkInCredential);
            notificationService.sendNotificationAboutNewCheckinCredentials(updatedCredential.getHotelId(), updatedCredential.getDeskId());

            return updatedCredential;
        } else {
            throw new CheckinCredentialNotFoundException();
        }
    }

    public Optional<CheckInCredentialDTO> getCheckInCredentialById(String id) {
        return checkInCredentialRepository.findById(id).map(checkInCredentialMapper::checkInCredentialToCheckInCredentialDTO);
    }
}

