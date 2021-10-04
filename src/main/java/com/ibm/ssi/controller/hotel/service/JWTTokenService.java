package com.ibm.ssi.controller.hotel.service;

import com.ibm.ssi.controller.hotel.service.dto.JWTTokenDTO;
import com.ibm.ssi.controller.hotel.service.exceptions.JWTTokenAlreadyBlacklisted;

public interface JWTTokenService {
  public JWTTokenDTO addToBlacklist(JWTTokenDTO jwtToken) throws JWTTokenAlreadyBlacklisted;
}
