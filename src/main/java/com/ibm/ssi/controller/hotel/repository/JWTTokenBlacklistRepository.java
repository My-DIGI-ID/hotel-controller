package com.ibm.ssi.controller.hotel.repository;

import java.util.List;

import com.ibm.ssi.controller.hotel.service.dto.JWTTokenDTO;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JWTTokenBlacklistRepository extends MongoRepository<JWTTokenDTO, String> {
  List<JWTTokenDTO> findByToken(String token);
}
