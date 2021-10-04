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

import java.util.List;

public class ProofRequestSelfAttestedProperty {

//    @JsonProperty(value = "names", required = true)
//    private List<String> names;
//
//    public void setNames(List<String> names) {
//        this.names = names;
//    }

    @JsonProperty(value = "name", required = true)
    private String name;

    public void setName(String name) {
        this.name = name;
    }
}
