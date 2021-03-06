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

package com.ibm.ssi.controller.hotel.repository;

import java.util.List;
import java.util.Optional;

import com.ibm.ssi.controller.hotel.domain.CheckInCredential;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckInCredentialRepository extends MongoRepository<CheckInCredential, String> {

    List<CheckInCredential> findByHotelIdAndDeskIdAndSendDateIsNotNullOrderByScanDateAsc(String hotelId, String deskId);

    Optional<CheckInCredential> findOneByPresentationExchangeId(String presentationExchangeId);
}
