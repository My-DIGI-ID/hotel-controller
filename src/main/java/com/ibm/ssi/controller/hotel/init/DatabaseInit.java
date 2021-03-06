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

package com.ibm.ssi.controller.hotel.init;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import com.ibm.ssi.controller.hotel.domain.Authority;
import com.ibm.ssi.controller.hotel.domain.Desk;
import com.ibm.ssi.controller.hotel.domain.Hotel;
import com.ibm.ssi.controller.hotel.domain.User;
import com.ibm.ssi.controller.hotel.domain.Address;
import com.ibm.ssi.controller.hotel.repository.HotelRepository;
import com.ibm.ssi.controller.hotel.repository.UserRepository;
import com.ibm.ssi.controller.hotel.security.AuthoritiesConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInit {

    @Autowired
    UserRepository userRepository;

    @Autowired
    HotelRepository hotelRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private Environment environment;

    @Value("${ssibk.hotel.controller.admin.username}")
    private String adminUsername;

    @Value("${ssibk.hotel.controller.admin.password}")
    private String adminPassword;

    @PostConstruct
    public void init() {
        initAdminUser();

        if (environment.acceptsProfiles(Profiles.of("dev"))) {
            initUser();
            initHotel();
        }
    }

    private void initAdminUser() {
        Authority adminAuthority = new Authority();
        adminAuthority.setName(AuthoritiesConstants.ADMIN);

        String id = "admin";
        User adminUser = new User();

        adminUser.setId(id);
        adminUser.setLogin(adminUsername);
        adminUser.setPassword(this.passwordEncoder.encode(adminPassword));
        adminUser.setFirstName("admin");
        adminUser.setLastName("Administrator");
        adminUser.setEmail("admin@localhost");
        adminUser.setCreatedBy("system");
        adminUser.setCreatedDate(Instant.now());
        adminUser.getAuthorities().add(adminAuthority);

        if (this.userRepository.existsById(id) == false) {
            userRepository.insert(adminUser);
        } else {
            userRepository.save(adminUser);
        }
    }

    private void initUser() {
        Authority userAuthority = new Authority();
        userAuthority.setName(AuthoritiesConstants.USER);

        String id = "user-1";

        if (this.userRepository.existsById(id) == false) {
            User userUser = new User();
            userUser.setId(id);
            userUser.setLogin("user");
            userUser.setPassword(this.passwordEncoder.encode("user"));
            userUser.setFirstName("User");
            userUser.setLastName("User");
            userUser.setEmail("user@localhost");
            userUser.setCreatedBy("system");
            userUser.setCreatedDate(Instant.now());
            userUser.getAuthorities().add(userAuthority);
            userUser.setHotelId("hotel-1");

            userRepository.insert(userUser);
        }
    }

    private void initHotel() {
        String id = "hotel-1";

        if (this.hotelRepository.existsById(id) == false) {
            Hotel hotel = new Hotel();
            hotel.setId("hotel-1");
            hotel.setName("IBM Hotel");

            Address address = new Address();
            address.setCity("Munich");
            address.setHouseNumber("123");
            address.setStreet("Berliner Strasse");
            address.setPostalCode("123456");
            hotel.setAddress(address);

            List<Desk> desks = new ArrayList<Desk>();
            desks.add(new Desk("desk1", "Blue Desk"));
            desks.add(new Desk("desk2", "Red Desk"));
            desks.add(new Desk("desk3", "Yellow Desk"));
            desks.add(new Desk("desk4", "Green Desk"));

            hotel.setDesks(desks);

            hotelRepository.insert(hotel);
        }
    }
}
