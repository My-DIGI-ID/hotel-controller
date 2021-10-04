package com.ibm.ssi.controller.hotel.service.impl;

import com.ibm.ssi.controller.hotel.client.ACAPYClient;
import com.ibm.ssi.controller.hotel.client.model.*;
import com.ibm.ssi.controller.hotel.domain.CheckInCredential;
import com.ibm.ssi.controller.hotel.service.CheckInCredentialService;
import com.ibm.ssi.controller.hotel.service.NotificationService;
import com.ibm.ssi.controller.hotel.service.dto.CorporateIdDTO;
import com.ibm.ssi.controller.hotel.service.dto.MasterIdDTO;
import com.ibm.ssi.controller.hotel.service.dto.WebhookPresentProofDTO;
import com.ibm.ssi.controller.hotel.service.exceptions.CheckinCredentialNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProofServiceImplTest {
    private static final String APIKEY = "apikey";
    private static final String ANY_ID = "any_id";
    private static final String DIDCOMM_URL = "didcomm://example.org?m=";
    private static final String FIRM_CITY = "FirmCity";
    private static final String FIRM_POSTALCODE = "FirmPostalcode";
    private static final String FIRM_STREET = "FirmStreet";
    private static final String LAST_NAME = "LastName";
    private static final String FIRST_NAME = "FirstName";
    private static final String FIRM_SUBJECT = "FirmSubject";
    private static final String FIRM_NAME = "FirmName";
    private static final String HARDWARE_DID = "HardwareDid";
    private static final String ADDRESS_COUNTRY = "AddressCountry";
    private static final String ADDRESS_CITY = "AddressCity";
    private static final String ADDRESS_ZIP_CODE = "AddressZipCode";
    private static final String ADDRESS_STREET = "AddressStreet";
    private static final String FAMILY_NAME = "FamilyName";

    @Mock
    Logger LOG;

    @Mock
    SecureRandom secureRandom;

    @Mock
    ACAPYClient acapyClient;

    @Mock
    CheckInCredentialService checkInCredentialService;

    @Mock
    NotificationService notificationService;

    @InjectMocks
    ProofServiceImpl proofServiceImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(proofServiceImpl, "apikey", APIKEY);
        ReflectionTestUtils.setField(proofServiceImpl, "corporateIdSchemaIdsString", "coorporateIDs");
        ReflectionTestUtils.setField(proofServiceImpl, "corporateIdIssuerDidsString", "coorporateDIDs");
        ReflectionTestUtils.setField(proofServiceImpl, "masterIdCredDefIdsString", "masterDID");
    }

    @Test
    void testGetProofURI() {
        ProofRequestDict requestDict = new ProofRequestDict();
        RequestPresentationAttach presentationAttach = new RequestPresentationAttach();
        presentationAttach.setData(new Base64Payload());
        requestDict.setRequestPresentationsAttach(new RequestPresentationAttach[]{presentationAttach});
        ProofResponseDTO proofResponseDTO = new ProofResponseDTO();
        ReflectionTestUtils.setField(proofResponseDTO, "proofRequestDict", requestDict);
        ArgumentCaptor<ProofRequestDTO> proofRequestCaptor = ArgumentCaptor.forClass(ProofRequestDTO.class);
        when(acapyClient.createProofRequest(anyString(), proofRequestCaptor.capture())).thenReturn(proofResponseDTO);

        URI result = proofServiceImpl.getProofURI("hotelId", "deskId");

        assertThat(result).matches(val -> val.toString().startsWith(DIDCOMM_URL), "starts with " + DIDCOMM_URL);
        assertThat(proofRequestCaptor.getValue())
            .extracting("proofRequest.requestedAttributes.masterId.names").asList().containsExactlyInAnyOrderElementsOf(Arrays.asList(
            "firstName", "familyName", "addressStreet", "addressZipCode", "addressCountry", "addressCity",
            "dateOfExpiry", "dateOfBirth", "hardwareDid"));
    }

    @Test
    void testHandleProofWebhookVerified() throws CheckinCredentialNotFoundException {
        when(acapyClient.getProofRecord(eq(APIKEY), anyString())).thenReturn(new ProofRecordDTO());
        when(checkInCredentialService.updateValidity(anyString(), anyBoolean())).thenReturn(new CheckInCredential("hotelId", "deskId", ANY_ID));

        WebhookPresentProofDTO proofDTO = new WebhookPresentProofDTO();
        ReflectionTestUtils.setField(proofDTO, "state", "verified");
        ReflectionTestUtils.setField(proofDTO, "presentationExchangeId", ANY_ID);

        proofServiceImpl.handleProofWebhook(proofDTO);

        verify(acapyClient).deleteProofRecord(APIKEY, ANY_ID);
    }

    @Test
    void testHandleProofWebhookReceived() throws CheckinCredentialNotFoundException {
        RevealedAttrValuesMasterId masterId = new RevealedAttrValuesMasterId();
        masterId.setFirstName(new Property());
        masterId.getFirstName().setRaw(FIRST_NAME);
        masterId.setFamilyName(new Property());
        masterId.getFamilyName().setRaw(FAMILY_NAME);
        masterId.setAddressStreet(new Property());
        masterId.getAddressStreet().setRaw(ADDRESS_STREET);
        masterId.setAddressZipCode(new Property());
        masterId.getAddressZipCode().setRaw(ADDRESS_ZIP_CODE);
        masterId.setAddressCity(new Property());
        masterId.getAddressCity().setRaw(ADDRESS_CITY);
        masterId.setAddressCountry(new Property());
        masterId.getAddressCountry().setRaw(ADDRESS_COUNTRY);
        masterId.setDateOfBirth(new Property());
        masterId.getDateOfBirth().setRaw("20010101");
        masterId.setDateOfExpiry(new Property());
        masterId.getDateOfExpiry().setRaw("20210101");
        masterId.setHardwareDid(new Property());
        masterId.getHardwareDid().setRaw(HARDWARE_DID);
        RevealedAttrMasterId attrMasterId = new RevealedAttrMasterId();
        attrMasterId.setValues(masterId);

        RevealedAttrValuesCorporateId corporateId = new RevealedAttrValuesCorporateId();
        corporateId.setFirmName(new Property());
        corporateId.getFirmName().setRaw(FIRM_NAME);
        corporateId.setFirmSubject(new Property());
        corporateId.getFirmSubject().setRaw(FIRM_SUBJECT);
        corporateId.setFirstName(new Property());
        corporateId.getFirstName().setRaw(FIRST_NAME);
        corporateId.setLastName(new Property());
        corporateId.getLastName().setRaw(LAST_NAME);
        corporateId.setFirmStreet(new Property());
        corporateId.getFirmStreet().setRaw(FIRM_STREET);
        corporateId.setFirmPostalcode(new Property());
        corporateId.getFirmPostalcode().setRaw(FIRM_POSTALCODE);
        corporateId.setFirmCity(new Property());
        corporateId.getFirmCity().setRaw(FIRM_CITY);
        RevealedAttrCorporateId attrCorporateId = new RevealedAttrCorporateId();
        attrCorporateId.setValues(corporateId);

        RevealedAttrGroups revealedAttrGroups = new RevealedAttrGroups();
        revealedAttrGroups.setMasterId(attrMasterId);
        revealedAttrGroups.setCorporateId(attrCorporateId);

        RequestedProof requestedProof = new RequestedProof();
        ReflectionTestUtils.setField(requestedProof, "revealedAttrGroups", revealedAttrGroups);
        Presentation presentation = new Presentation();
        ReflectionTestUtils.setField(presentation, "requestedProof", requestedProof);
        ProofRecordDTO recordDTO = new ProofRecordDTO();
        ReflectionTestUtils.setField(recordDTO, "presentation", presentation);
        when(acapyClient.getProofRecord(APIKEY, ANY_ID)).thenReturn(recordDTO);

        WebhookPresentProofDTO proofDTO = new WebhookPresentProofDTO();
        ReflectionTestUtils.setField(proofDTO, "state", "presentation_received");
        ReflectionTestUtils.setField(proofDTO, "presentationExchangeId", ANY_ID);
        proofServiceImpl.handleProofWebhook(proofDTO);

        ArgumentCaptor<MasterIdDTO> masterIdCaptor = ArgumentCaptor.forClass(MasterIdDTO.class);
        ArgumentCaptor<CorporateIdDTO> corporateIdCaptor = ArgumentCaptor.forClass(CorporateIdDTO.class);
        verify(acapyClient, times(0)).deleteProofRecord(anyString(), any());
        verify(checkInCredentialService).updateCheckinCredential(eq(ANY_ID), masterIdCaptor.capture(), corporateIdCaptor.capture());
//        assertThat(masterIdCaptor.getValue()).extracting("hardwareDid").containsAll( Arrays.asList(HARDWARE_DID));
    }

    @Test
    void testGetNonce() {
        int numberOfBits = 80;
        String nonce = proofServiceImpl.generateNonce(numberOfBits);
        assertThat(nonce).isNotNull();
        assertThat(new BigInteger(nonce).toByteArray().length).isEqualTo(numberOfBits / 8);
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme
