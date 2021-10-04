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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class ProofRequestProperty {

    @JsonProperty(value = "names", required = true)
    private List<String> names;

    @JsonProperty("non_revoked")
    private NonRevokedRestriction nonRevokedRestriction;

    @JsonProperty("restrictions")
    private List<Map<String, String>> restrictions = null;

    public ProofRequestProperty() {}

    public void setNames(List<String> names) {
        this.names = names;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public void setNonRevokedRestriction(NonRevokedRestriction nonRevokedRestriction) {
        this.nonRevokedRestriction = nonRevokedRestriction;
    }

    public void setRestrictions(List<Map<String, String>> restrictions) {
        this.restrictions = restrictions;
    }
}