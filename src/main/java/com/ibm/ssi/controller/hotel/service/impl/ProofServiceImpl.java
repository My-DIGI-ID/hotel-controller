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

import java.math.BigInteger;
import java.net.URI;
import java.security.DrbgParameters;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.ssi.controller.hotel.client.ACAPYClient;
import com.ibm.ssi.controller.hotel.client.model.*;
import com.ibm.ssi.controller.hotel.domain.CheckInCredential;
import com.ibm.ssi.controller.hotel.service.CheckInCredentialService;
import com.ibm.ssi.controller.hotel.service.NotificationService;
import com.ibm.ssi.controller.hotel.service.ProofService;
import com.ibm.ssi.controller.hotel.service.dto.CorporateIdDTO;
import com.ibm.ssi.controller.hotel.service.dto.MasterIdDTO;
import com.ibm.ssi.controller.hotel.service.dto.WebhookPresentProofDTO;
import com.ibm.ssi.controller.hotel.service.exceptions.CheckinCredentialNotFoundException;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import static java.security.DrbgParameters.Capability.RESEED_ONLY;

@Service
public class ProofServiceImpl implements ProofService {

    private static final Logger LOG = LoggerFactory.getLogger(ProofServiceImpl.class);
    private static final String DIDCOMM_URL = "didcomm://example.org?m=";
    private static final String ARIES_MESSAGE_TYPE = "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/present-proof/1.0/request-presentation";
    private static final String ARIES_ATTACH_ID = "libindy-request-presentation-0";

    @Autowired
    private ACAPYClient acapyClient;

    @Autowired
    private CheckInCredentialService checkInCredentialService;

    @Autowired
    private NotificationService notificationService;

    @Value("${ssibk.hotel.controller.agent.apikey}")
    private String apikey;

    @Value("${ssibk.hotel.controller.agent.endpoint}")
    private String agentEndpoint;

    @Value("${ssibk.hotel.controller.agent.endpoint_name}")
    private String agentEndpointName;

    @Value("${ssibk.hotel.controller.agent.recipientkey}")
    private String agentRecipientKey;

    @Value("${ssibk.hotel.controller.agent.masterid.credential_definition_ids}")
    private String masterIdCredDefIdsString;

    @Value("${ssibk.hotel.controller.agent.corporateid.schema_ids}")
    private String corporateIdSchemaIdsString;

    @Value("${ssibk.hotel.controller.agent.corporateid.issuer_dids}")
    private String corporateIdIssuerDidsString;

    @Override
    public URI getProofURI(String hotelId, String deskId) {

        // prepare a proof request DTO and send it to the agent
        ProofRequestDTO connectionlessProofCreationRequest = prepareConnectionlessProofRequest();
        ProofResponseDTO proofResponseDTO = acapyClient.createProofRequest(apikey,
            connectionlessProofCreationRequest);
        if (LOG.isDebugEnabled()) {
            LOG.debug("agent created a proof request: {}", proofResponseDTO);
        }

        // create a new entry for this presentationExchangeId in the database
        checkInCredentialService.createCheckInCredential(hotelId, deskId,
            proofResponseDTO.getPresentationExchangeId());

        // prepare a connectionless proof request
        ConnectionlessProofRequest connectionlessProofRequest = prepareConnectionlessProofRequest(proofResponseDTO);

        try {
            ObjectMapper mapper = new ObjectMapper();
            String msg = mapper.writeValueAsString(connectionlessProofRequest);
            if (LOG.isDebugEnabled()) {
                LOG.debug(msg);
            }

            // Base64 encode the connectionless proof request
            String encodedUrl = Base64.getEncoder()
                .encodeToString(msg.getBytes());

            // return a URI that is consumable by the wallet app
            return URI.create(DIDCOMM_URL + encodedUrl);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

    }

    private ConnectionlessProofRequest prepareConnectionlessProofRequest(ProofResponseDTO proofResponseDTO) {
        String threadId = proofResponseDTO.getThreadId();
        ConnectionlessProofRequest connectionlessProofRequest = new ConnectionlessProofRequest();
        connectionlessProofRequest.setId(threadId);
        connectionlessProofRequest.setType(ARIES_MESSAGE_TYPE);

        RequestPresentationAttach requestPresentationAttach = new RequestPresentationAttach();
        requestPresentationAttach.setId(ARIES_ATTACH_ID);
        requestPresentationAttach.setMimeType(MediaType.APPLICATION_JSON_VALUE);
        Base64Payload base64Payload = new Base64Payload();
        base64Payload.setBase64(
            proofResponseDTO.getProofRequestDict().getRequestPresentationsAttach()[0].getData().getBase64());
        requestPresentationAttach.setData(base64Payload);

        RequestPresentationAttach[] requestPresentationAttaches = new RequestPresentationAttach[1];
        requestPresentationAttaches[0] = requestPresentationAttach;

        connectionlessProofRequest.setRequestPresentationAttach(requestPresentationAttaches);

        ProofRequestService proofRequestService = new ProofRequestService();
        String[] keys = new String[1];
        keys[0] = agentRecipientKey;
        proofRequestService.setRecipientKeys(keys);
        String[] routingKeys = new String[0];
        proofRequestService.setRoutingKeys(routingKeys);
        proofRequestService.setServiceEndpoint(agentEndpoint);
        proofRequestService.setEndpointName(agentEndpointName);
        connectionlessProofRequest.setService(proofRequestService);

        ProofRequestThread proofRequestThread = new ProofRequestThread();
        EmptyDTO empty = new EmptyDTO();
        proofRequestThread.setReceivedOrders(empty);
        proofRequestThread.setThreadId(threadId);
        connectionlessProofRequest.setThread(proofRequestThread);

        return connectionlessProofRequest;
    }

    private ProofRequestDTO prepareConnectionlessProofRequest() {

        RequestedPredicates requestedPredicates = new RequestedPredicates();
        RequestedAttributes requestedAttributes = new RequestedAttributes();
        requestedAttributes.setMasterId(this.createMasterIdAttributes());
//        requestedAttributes.setSelfAttested(this.createSelfAttestedAttributes());
        requestedAttributes.setCorporateId(this.createCorporateIdAttributes());

        // Composing the proof request
        ProofRequest proofRequest = new ProofRequest();
        proofRequest.setName("Proof request");
        proofRequest.setRequestedPredicates(requestedPredicates);
        proofRequest.setRequestedAttributes(requestedAttributes);
        proofRequest.setVersion("0.1");
        proofRequest.setNonce(generateNonce(80));

        ProofRequestDTO connectionlessProofCreationRequest = new ProofRequestDTO();
        connectionlessProofCreationRequest.setComment("string"); // TODO: Do we use the comment field?

        // Debug
        if (LOG.isDebugEnabled()) {
            LOG.debug(ReflectionToStringBuilder.toString(proofRequest));
        }

        connectionlessProofCreationRequest.setProofRequest(proofRequest);

        return connectionlessProofCreationRequest;
    }

    /**
     * Generate a decimal nonce, using SecureRandom and DRBG algorithm
     * @param numberOfBits The length of the nonce in bit
     * @return A string representation of the nonce
     */
    public String generateNonce(int numberOfBits) {
        byte[] nonce = new byte[numberOfBits / 8];
        try {
            SecureRandom rand = SecureRandom.getInstance("DRBG",
                DrbgParameters.instantiation(128, RESEED_ONLY, null));
            rand.nextBytes(nonce);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Error during nonce generation", e);
        }

        return new BigInteger(nonce).toString();
    }

    private ProofRequestProperty createMasterIdAttributes() {
        // Restriction regarding CredDefs for masterId
        String[] masterIdCredDefIds = masterIdCredDefIdsString.split(",");
        List<Map<String, String>> masterIdRestrictions = new ArrayList<>(masterIdCredDefIds.length);
        for (String masterIdCredDefId : masterIdCredDefIds) {
            Map<String, String> temp = new HashMap<>();
            temp.put("cred_def_id", masterIdCredDefId);
            masterIdRestrictions.add(temp);
        }

        ProofRequestProperty proofRequestMasterId = new ProofRequestProperty();
        List<String> names = Arrays.asList(
            "firstName", "familyName", "addressStreet", "addressZipCode", "addressCountry", "addressCity",
            "dateOfExpiry", "dateOfBirth", "hardwareDid");
        proofRequestMasterId.setNames(names);

        // Restriction regarding Revocation
        NonRevokedRestriction nonRevokedRestriction = new NonRevokedRestriction();
        nonRevokedRestriction.setFrom(0);
        nonRevokedRestriction.setTo((int) Instant.now().getEpochSecond());

        proofRequestMasterId.setNonRevokedRestriction(nonRevokedRestriction);
        proofRequestMasterId.setRestrictions(masterIdRestrictions);
        return proofRequestMasterId;
    }

    private ProofRequestSelfAttestedProperty createSelfAttestedAttributes() {
        ProofRequestSelfAttestedProperty proofRequestSelfAttested = new ProofRequestSelfAttestedProperty();

//        List<String> names = Arrays.asList("hardwareDidProof");
//        proofRequestSelfAttested.setNames(names);

        proofRequestSelfAttested.setName("hardwareDidProof");

        return proofRequestSelfAttested;
    }

    private ProofRequestProperty createCorporateIdAttributes() {

        // Restriction regarding schemas for corporateId
        String[] corporateIdSchemaIds = corporateIdSchemaIdsString.split(",");
        String[] corporateIdIssuerDids = corporateIdIssuerDidsString.split(",");
        List<Map<String, String>> corporateIdRestrictions = new ArrayList<>();
        for (String schemaID : corporateIdSchemaIds) {
            for (String issuerDID : corporateIdIssuerDids) {
                Map<String, String> temp = new HashMap<>();
                temp.put("schema_id", schemaID);
                temp.put("issuer_did", issuerDID);
                corporateIdRestrictions.add(temp);
            }
        }

        // Restriction regarding Revocation
        NonRevokedRestriction nonRevokedRestriction = new NonRevokedRestriction();
        nonRevokedRestriction.setFrom(0);
        nonRevokedRestriction.setTo((int) Instant.now().getEpochSecond());

        ProofRequestProperty proofRequestCorporateId = new ProofRequestProperty();
        List<String> names = Arrays.asList("firmCity", "firmStreet", "firmPostalcode", "firmName", "firmSubject", "firstName", "lastName");
        proofRequestCorporateId.setNames(names);
        proofRequestCorporateId.setNonRevokedRestriction(nonRevokedRestriction);
        proofRequestCorporateId.setRestrictions(corporateIdRestrictions);
        return proofRequestCorporateId;
    }

    @Override
    public void handleProofWebhook(WebhookPresentProofDTO webhookPresentProofDTO) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("presentation exchange record is in state {}", webhookPresentProofDTO.getState());
        }

        String presentationExchangeId = webhookPresentProofDTO.getPresentationExchangeId();

        if (Objects.equals(webhookPresentProofDTO.getState(), "verified")) {
            handleVerified(webhookPresentProofDTO, presentationExchangeId);
        } else if (Objects.equals(webhookPresentProofDTO.getState(), "presentation_received")) {
            handlePresentationReceived(presentationExchangeId);
        } else if (LOG.isDebugEnabled()) {
            LOG.debug("ignore this state: {}", webhookPresentProofDTO.getState());
        }
    }

    private void handlePresentationReceived(String presentationExchangeId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Update mongodb entry and delete information from agent");
            LOG.debug("Getting presentation information from the agent");
        }

        // get the proof record from the agent
        ProofRecordDTO proofRecordDTO = acapyClient.getProofRecord(apikey, presentationExchangeId);

        // construct a corporateId out of the proof
        CorporateIdDTO corporateId = createCorporateIdDTO(proofRecordDTO);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Created corporate id: {}", corporateId);
        }

        // construct a masterId out of the proof
        MasterIdDTO masterId = createMasterIdDTO(proofRecordDTO);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Created master id: {}", masterId);
        }

        try {
            // Updating the mongo db entry with the data received via the proof
            checkInCredentialService.updateCheckinCredential(presentationExchangeId, masterId, corporateId);
        } catch (CheckinCredentialNotFoundException e) {
            // log but do not rethrow
            LOG.error("A matching CheckInCredential was not found", e);
        }
    }

    private void handleVerified(WebhookPresentProofDTO webhookPresentProofDTO, String presentationExchangeId) {
        boolean proofVerified = webhookPresentProofDTO.getVerified() != null
            && webhookPresentProofDTO.getVerified().equals("true");
        try {
            CheckInCredential checkInCredential = checkInCredentialService.updateValidity(presentationExchangeId,
                proofVerified);
            // inform subscribers about the new checkin credential
            notificationService.sendNotificationAboutNewCheckinCredentials(checkInCredential.getHotelId(),
                checkInCredential.getDeskId());
        } catch (CheckinCredentialNotFoundException e) {
            // log but do not rethrow
            LOG.error("A matching CheckInCredential was not found", e);
        } finally {
            // Delete proof presentation info from agent
            acapyClient.deleteProofRecord(apikey, presentationExchangeId);
        }
    }

    private MasterIdDTO createMasterIdDTO(ProofRecordDTO proofRecordDTO) {
        MasterIdDTO masterId = new MasterIdDTO();
        if (LOG.isDebugEnabled()) {
            LOG.debug(proofRecordDTO.toString());
        }

        RevealedAttrValuesMasterId values = proofRecordDTO.getPresentation().getRequestedProof().getRevealedAttrGroups().getMasterId().getValues();
        masterId.setFirstName(values.getFirstName().getRaw());
        masterId.setFamilyName(values.getFamilyName().getRaw());
        masterId.setAddressStreet(values.getAddressStreet().getRaw());
        masterId.setAddressZipCode(values.getAddressZipCode().getRaw());
        masterId.setAddressCity(values.getAddressCity().getRaw());
        masterId.setAddressCountry(values.getAddressCountry().getRaw());
        masterId.setDateOfBirthFromString(values.getDateOfBirth().getRaw());
        masterId.setDateOfExpiryFromString(values.getDateOfExpiry().getRaw());
        masterId.setHardwareDid(values.getHardwareDid().getRaw());

        return masterId;
    }

    private CorporateIdDTO createCorporateIdDTO(ProofRecordDTO proofRecordDTO) {
        CorporateIdDTO corporateId = new CorporateIdDTO();

        if (LOG.isDebugEnabled()) {
            LOG.debug(proofRecordDTO.toString());
        }

        RevealedAttrValuesCorporateId values = proofRecordDTO.getPresentation().getRequestedProof().getRevealedAttrGroups().getCorporateId().getValues();
        corporateId.setFirstName(values.getFirstName().getRaw());
        corporateId.setFamilyName(values.getLastName().getRaw());
        corporateId.setCompanyName(values.getFirmName().getRaw());
        corporateId.setCompanySubject(values.getFirmSubject().getRaw());
        corporateId.setCompanyAddressStreet(values.getFirmStreet().getRaw());
        corporateId.setCompanyAddressZipCode(values.getFirmPostalcode().getRaw());
        corporateId.setCompanyAddressCity(values.getFirmCity().getRaw());

        return corporateId;
    }

}
