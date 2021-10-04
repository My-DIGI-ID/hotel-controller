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

package com.ibm.ssi.controller.hotel.service.dto;

import javax.validation.constraints.NotNull;

import com.ibm.ssi.controller.hotel.util.validation.CustomPattern;

public class CorporateIdDTO {

    @NotNull
    @CustomPattern
    private String firstName;

    @NotNull
    @CustomPattern
    private String familyName;

    @CustomPattern
    private String companyName;

    @CustomPattern
    private String companySubject;

    @CustomPattern
    private String companyAddressStreet;

    @CustomPattern
    private String companyAddressZipCode;

    @CustomPattern
    private String companyAddressCity;

    @NotNull
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(@NotNull String firstName) {
        this.firstName = firstName;
    }

    @NotNull
    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(@NotNull String familyName) {
        this.familyName = familyName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanySubject() {
        return companySubject;
    }

    public void setCompanySubject(String companySubject) {
        this.companySubject = companySubject;
    }

    public String getCompanyAddressStreet() {
        return companyAddressStreet;
    }

    public void setCompanyAddressStreet(String companyAddressStreet) {
        this.companyAddressStreet = companyAddressStreet;
    }

    public String getCompanyAddressZipCode() {
        return companyAddressZipCode;
    }

    public void setCompanyAddressZipCode(String companyAddressZipCode) {
        this.companyAddressZipCode = companyAddressZipCode;
    }

    public String getCompanyAddressCity() {
        return companyAddressCity;
    }

    public void setCompanyAddressCity(String companyAddressCity) {
        this.companyAddressCity = companyAddressCity;
    }

    @Override
    public String toString() {
        return "CorporateIdDTO [companyAddressCity=" + companyAddressCity + ", companyAddressStreet="
                + companyAddressStreet + ", companyAddressZipCode=" + companyAddressZipCode + ", companyName="
                + companyName + ", companySubject=" + companySubject + ", familyName=" + familyName + ", firstName="
                + firstName + "]";
    }


}
