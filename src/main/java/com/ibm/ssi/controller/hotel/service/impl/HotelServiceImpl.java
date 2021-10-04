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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.ibm.ssi.controller.hotel.domain.Hotel;
import com.ibm.ssi.controller.hotel.domain.User;
import com.ibm.ssi.controller.hotel.repository.HotelRepository;
import com.ibm.ssi.controller.hotel.repository.UserRepository;
import com.ibm.ssi.controller.hotel.security.SecurityUtils;
import com.ibm.ssi.controller.hotel.service.HotelService;
import com.ibm.ssi.controller.hotel.service.dto.DeskDTO;
import com.ibm.ssi.controller.hotel.service.dto.HotelDTO;
import com.ibm.ssi.controller.hotel.service.exceptions.HotelAlreadyExistsException;
import com.ibm.ssi.controller.hotel.service.exceptions.HotelNotFoundException;
import com.ibm.ssi.controller.hotel.service.exceptions.HotelHasDesksWithDuplicateIdsException;
import com.ibm.ssi.controller.hotel.service.exceptions.HotelHasDesksWithDuplicateNamesException;
import com.ibm.ssi.controller.hotel.service.mapper.DeskMapper;
import com.ibm.ssi.controller.hotel.service.mapper.HotelMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HotelServiceImpl implements HotelService {

    private final Logger log = LoggerFactory.getLogger(HotelServiceImpl.class);

    @Autowired
    HotelRepository hotelRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    HotelMapper hotelMapper;

    @Autowired
    DeskMapper deskMapper;

    @Override
    public HotelDTO createHotel(HotelDTO hotelDTO) throws HotelAlreadyExistsException, HotelHasDesksWithDuplicateIdsException, HotelHasDesksWithDuplicateNamesException {

        log.debug("create hotel");
        if (this.hotelRepository.existsById(hotelDTO.getId())) {
            throw new HotelAlreadyExistsException();
        }

        if (hotelHasDesksWithDuplicateIds(hotelDTO.getDesks())) {
            throw new HotelHasDesksWithDuplicateIdsException();
        };

        if (hotelHasDesksWithDuplicateNames(hotelDTO.getDesks())) {
            throw new HotelHasDesksWithDuplicateNamesException();
        };

        Hotel hotel = hotelMapper.hotelDTOToHotel(hotelDTO);
        Hotel createdHotel = this.hotelRepository.insert(hotel);

        HotelDTO createdHotelDTO = hotelMapper.hotelToHotelDTO(createdHotel);
        return createdHotelDTO;
    }

    @Override
    public HotelDTO updateHotel(HotelDTO hotelDTO) throws HotelNotFoundException, HotelHasDesksWithDuplicateIdsException, HotelHasDesksWithDuplicateNamesException {

        log.debug("update hotel");
        if (!this.hotelRepository.existsById(hotelDTO.getId())) {
            throw new HotelNotFoundException();
        }

        if (hotelHasDesksWithDuplicateIds(hotelDTO.getDesks())) {
            throw new HotelHasDesksWithDuplicateIdsException();
        };

        if (hotelHasDesksWithDuplicateNames(hotelDTO.getDesks())) {
            throw new HotelHasDesksWithDuplicateNamesException();
        };

        Hotel hotel = hotelMapper.hotelDTOToHotel(hotelDTO);
        Hotel updatedHotel = this.hotelRepository.save(hotel);

        HotelDTO updatedHotelDTO = hotelMapper.hotelToHotelDTO(updatedHotel);
        return updatedHotelDTO;
    }

    @Override
    public List<HotelDTO> getAllHotels() {
        log.debug("get all hotels");
        return this.hotelRepository.findAll().stream().map(hotelMapper::hotelToHotelDTO)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Optional<HotelDTO> getHotel(String id) {
        log.debug("get hotel by id");
        return this.hotelRepository.findById(id).map(hotelMapper::hotelToHotelDTO);
    }

    @Override
    public Optional<HotelDTO> getMyHotel() {

        Optional<HotelDTO> hotel = null;

        Optional<String> login = SecurityUtils.getCurrentUserLogin();
        if (login.isPresent()) {
            Optional<User> user = this.userRepository.findOneByLogin(login.get());
            if (user.isPresent()) {
                String hotelId = user.get().getHotelId();
                hotel = this.hotelRepository.findById(hotelId).map(hotelMapper::hotelToHotelDTO);
            }
        }

        return hotel;
    }

    @Override
    public void deleteHotel(String id) {
        log.debug("delete hotel");
        this.hotelRepository.deleteById(id);
    }

    private boolean hotelHasDesksWithDuplicateIds(List<DeskDTO> desks) {
        List<String> deskIds = new ArrayList<String>();
        
        for (DeskDTO desk: desks) {
            deskIds.add(desk.getId());
        }

        return !areAllUnique(deskIds);
    }

    private boolean hotelHasDesksWithDuplicateNames(List<DeskDTO> desks) {
        List<String> deskNames = new ArrayList<String>();
        
        for (DeskDTO desk: desks) {
            deskNames.add(desk.getName());
        }

        return !areAllUnique(deskNames);
    }

    public static <T> boolean areAllUnique(List<T> list){
        Set<T> set = new HashSet<T>();

        return list.stream().allMatch(t -> set.add(t));
    }
}
