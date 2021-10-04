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

package com.ibm.ssi.controller.hotel.client;

import com.ibm.ssi.controller.hotel.service.dto.BookingDataDTO;
import com.ibm.ssi.controller.hotel.service.dto.CheckInCredentialDTO;
import com.ibm.ssi.controller.hotel.service.dto.PMSDataDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "HotelIntegrationClient", url = "${ssibk.hotel.controller.integrationservice.apiurl}")
public interface IntegrationServiceClient {

    @RequestMapping(method = RequestMethod.POST, value = "/lookup")
    public List<BookingDataDTO> lookup(@RequestHeader("X-API-KEY") String apiKey,
                                       @RequestParam(value="buchungsnummer", required = false) String bookingNumber,
                                       @RequestBody CheckInCredentialDTO checkInCredentialDTO);

    @RequestMapping(method = RequestMethod.POST, value = "/sendDataToPMS")
    public PMSDataDTO sendDataToPMS(@RequestHeader("X-API-KEY") String apiKey,
                                    @RequestBody PMSDataDTO pmsDataDTO);

}
