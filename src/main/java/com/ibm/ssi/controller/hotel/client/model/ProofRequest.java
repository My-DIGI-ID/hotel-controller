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

package com.ibm.ssi.controller.hotel.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProofRequest {

    @JsonProperty("name")
    private String name;
    @JsonProperty("requested_predicates")
    private RequestedPredicates requestedPredicates;
    @JsonProperty("requested_attributes")
    private RequestedAttributes requestedAttributes;
    @JsonProperty("version")
    private String version;
    @JsonProperty("nonce")
    private String nonce;

    public ProofRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RequestedPredicates getRequestedPredicates() {
        return requestedPredicates;
    }

    public void setRequestedPredicates(RequestedPredicates requestedPredicates) {
        this.requestedPredicates = requestedPredicates;
    }

    public RequestedAttributes getRequestedAttributes() {
        return requestedAttributes;
    }

    public void setRequestedAttributes(RequestedAttributes requestedAttributes) {
        this.requestedAttributes = requestedAttributes;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }
}
