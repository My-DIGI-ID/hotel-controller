package com.ibm.ssi.controller.hotel.service.impl;

import java.util.List;

import com.ibm.ssi.controller.hotel.repository.JWTTokenBlacklistRepository;
import com.ibm.ssi.controller.hotel.service.JWTTokenService;
import com.ibm.ssi.controller.hotel.service.dto.JWTTokenDTO;
import com.ibm.ssi.controller.hotel.service.exceptions.JWTTokenAlreadyBlacklisted;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JWTTokenServiceImpl implements JWTTokenService {

  @Autowired
  JWTTokenBlacklistRepository jwtTokenBlacklistRepository;

  @Override
  public JWTTokenDTO addToBlacklist(JWTTokenDTO jwtToken) throws JWTTokenAlreadyBlacklisted {
    List<JWTTokenDTO> tokens = jwtTokenBlacklistRepository.findByToken(jwtToken.token);

    if (!tokens.isEmpty()) {
      throw new JWTTokenAlreadyBlacklisted();
    }

    return jwtTokenBlacklistRepository.insert(jwtToken);
  }  
}
