package com.ibm.ssi.controller.hotel.service.impl;

import java.util.List;
import java.util.Optional;

import com.ibm.ssi.controller.hotel.domain.CheckInCredential;
import com.ibm.ssi.controller.hotel.domain.CorporateId;
import com.ibm.ssi.controller.hotel.domain.MasterId;
import com.ibm.ssi.controller.hotel.repository.CheckInCredentialRepository;
import com.ibm.ssi.controller.hotel.service.HotelService;
import com.ibm.ssi.controller.hotel.service.NotificationService;
import com.ibm.ssi.controller.hotel.service.dto.*;
import com.ibm.ssi.controller.hotel.service.exceptions.CannotFindMyHotelException;
import com.ibm.ssi.controller.hotel.service.exceptions.CheckinCredentialNotFoundException;
import com.ibm.ssi.controller.hotel.service.mapper.CheckInCredentialMapper;
import com.ibm.ssi.controller.hotel.service.mapper.CorporateIdMapper;
import com.ibm.ssi.controller.hotel.service.mapper.MasterIdMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CheckInCredentialServiceImplTest {
    @Mock
    Logger LOG;

    @Mock
    HotelService hotelService;

    @Mock
    CheckInCredentialMapper checkInCredentialMapper;

    @Mock
    MasterIdMapper masterIdMapper;

    @Mock
    CorporateIdMapper corporateIdMapper;

    @Mock
    CheckInCredentialRepository checkInCredentialRepository;

    @Mock
    NotificationService notificationService;

    @InjectMocks
    CheckInCredentialServiceImpl checkInCredentialServiceImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testGetDeskCredentials() throws CannotFindMyHotelException {
        DeskDTO deskDTO = new DeskDTO();
        deskDTO.setId("deskId");
        HotelDTO hotelDTO = new HotelDTO();
        hotelDTO.setId("hotelId");
        hotelDTO.setDesks(List.of(deskDTO));
        when(hotelService.getMyHotel()).thenReturn(Optional.of(hotelDTO));
        CheckInCredential checkInCredential = new CheckInCredential("hotelId", "deskId", "presentationExchangeId");
        when(checkInCredentialRepository.findByHotelIdAndDeskIdAndSendDateIsNotNullOrderByScanDateAsc(anyString(), anyString())).thenReturn(List.of(checkInCredential));
        CheckInCredentialDTO checkInCredentialDTO = new CheckInCredentialDTO("id", "hotelId", "deskId");
        when(checkInCredentialMapper.checkInCredentialToCheckInCredentialDTO(checkInCredential)).thenReturn(checkInCredentialDTO);

        List<CheckInCredentialDTO> result = checkInCredentialServiceImpl.getDeskCredentials("deskId");

        Assertions.assertEquals(List.of(checkInCredentialDTO), result);
    }

    @Test
    void testCreateCheckInCredential() {
        checkInCredentialServiceImpl.createCheckInCredential("hotelId", "deskId", "presentationExchangeId");

        ArgumentCaptor<CheckInCredential> captor = ArgumentCaptor.forClass(CheckInCredential.class);
        verify(this.checkInCredentialRepository).save(captor.capture());
        assertThat(captor.getValue())
            .hasFieldOrPropertyWithValue("hotelId", "hotelId")
            .hasFieldOrPropertyWithValue("deskId", "deskId");
        verify(this.notificationService).sendNotificationAboutNewCheckinCredentials("hotelId", "deskId");
    }

    @Test
    void testUpdateCheckinCredential() throws CheckinCredentialNotFoundException {
        when(masterIdMapper.masterIdDTOToMasterId(any())).thenReturn(new MasterId());
        when(corporateIdMapper.corporateIdDTOToCorporateId(any())).thenReturn(new CorporateId());
        CheckInCredential checkInCredential = new CheckInCredential("hotelId", "deskId", "presentationExchangeId");
        when(checkInCredentialRepository.findOneByPresentationExchangeId("presentationExchangeId")).thenReturn(Optional.of(checkInCredential));
        when(checkInCredentialRepository.save(checkInCredential)).thenReturn(checkInCredential);

        CheckInCredential result = checkInCredentialServiceImpl.updateCheckinCredential("presentationExchangeId", new MasterIdDTO(), new CorporateIdDTO());

        Assertions.assertEquals(checkInCredential, result);
        verify(this.notificationService).sendNotificationAboutNewCheckinCredentials("hotelId", "deskId");
    }

    @Test
    void testUpdateValidity() throws CheckinCredentialNotFoundException {
        CheckInCredential checkInCredential = new CheckInCredential("hotelId", "deskId", "presentationExchangeId");
        when(checkInCredentialRepository.findOneByPresentationExchangeId(anyString())).thenReturn(Optional.of(checkInCredential));
        when(checkInCredentialRepository.save(checkInCredential)).thenReturn(checkInCredential);

        CheckInCredential result = checkInCredentialServiceImpl.updateValidity("presentationExchangeId", true);

        Assertions.assertEquals(checkInCredential, result);
        verify(this.notificationService).sendNotificationAboutNewCheckinCredentials("hotelId", "deskId");
    }

    @Test
    void testGetCheckInCredentialById() {
        Optional<CheckInCredentialDTO> result = checkInCredentialServiceImpl.getCheckInCredentialById("id");

        Assertions.assertEquals(Optional.empty(), result);
    }
}
