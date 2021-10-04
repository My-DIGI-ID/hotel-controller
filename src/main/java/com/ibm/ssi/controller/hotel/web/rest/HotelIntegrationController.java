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

import com.ibm.ssi.controller.hotel.security.AuthoritiesConstants;
import com.ibm.ssi.controller.hotel.service.CheckInCredentialService;
import com.ibm.ssi.controller.hotel.service.IntegrationService;
import com.ibm.ssi.controller.hotel.service.dto.BookingDataDTO;
import com.ibm.ssi.controller.hotel.service.dto.CheckInCredentialDTO;
import com.ibm.ssi.controller.hotel.service.dto.HotelDTO;
import com.ibm.ssi.controller.hotel.service.dto.PMSDataDTO;
import com.ibm.ssi.controller.hotel.service.HotelService;
import com.ibm.ssi.controller.hotel.service.exceptions.CouldNotSendPMSDataException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.List;
import java.util.Optional;

@RestController
@Tag(name = "Hotel Integration Controller", description = "Communication to the Integration Controller to fetch or update data from hotel backens")
@RequestMapping("/api")
public class HotelIntegrationController {

    private final Logger log = LoggerFactory.getLogger(HotelIntegrationController.class);

    @Autowired
    IntegrationService integrationService;

    @Autowired
    HotelService hotelService;

    CheckInCredentialService checkInCredentialService;

    @PostMapping("/lookup")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.USER + "\")")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<BookingDataDTO>> lookup(
            @RequestParam(name = "buchungsnummer", required = false, defaultValue = "") String bookingNumber,
            @Valid @RequestBody CheckInCredentialDTO checkInCredentialDTO) {

        log.debug("REST request to look for Buchungsnummer {} and Firstname {}, Lastname {}", bookingNumber,
                checkInCredentialDTO.getCorporateId().getFirstName(),
                checkInCredentialDTO.getCorporateId().getFamilyName());

        List<BookingDataDTO> foundGuests = integrationService.lookup(bookingNumber, checkInCredentialDTO);

        return new ResponseEntity<List<BookingDataDTO>>(foundGuests, new HttpHeaders(), HttpStatus.OK);
    }

    /**
     * {@code POST  /pms-data} : Create pms-data
     *
     * @param pmsDataDTO the pmsData to create
     * @return the {@link ResponseEntity} with status {@code 200 (Created)} and with
     *         the body the pmsData.
     */
    @PostMapping("/sendPMSData")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.USER + "\")")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PMSDataDTO> sendPMSData(@Valid @RequestBody PMSDataDTO pmsDataDTO) {
        log.debug("REST request to send PMS data: {}", pmsDataDTO);
        log.debug("Hotel ID: {}", pmsDataDTO.getCheckInCredential().getHotelId());

        try {
            Optional<HotelDTO> hotelDTO = this.hotelService.getHotel(pmsDataDTO.getCheckInCredential().getHotelId());
            pmsDataDTO.setHotel(hotelDTO.get());
            this.integrationService.sendDataToPMS(pmsDataDTO);
        } catch (CouldNotSendPMSDataException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.noContent().build();
    }
}
