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

public class RevealedAttrCorporateId {

    @JsonProperty("sub_proof_index")
    private Integer subProofIndex;

    @JsonProperty("values")
    private RevealedAttrValuesCorporateId values;

    public RevealedAttrCorporateId() {}

    public Integer getSubProofIndex() {
        return subProofIndex;
    }

    public void setSubProofIndex(Integer subProofIndex) {
        this.subProofIndex = subProofIndex;
    }

    public RevealedAttrValuesCorporateId getValues() {
        return values;
    }

    public void setValues(RevealedAttrValuesCorporateId values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "RevealedAttrCorporateId [subProofIndex=" + subProofIndex + ", values=" + values + "]";
    }

}
