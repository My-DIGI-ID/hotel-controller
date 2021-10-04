package com.ibm.ssi.controller.hotel.service.dto;

import java.util.Date;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="JWTTokenBlacklist")
public class JWTTokenDTO {
  @NotNull
  @NotEmpty
  @Size(max = 500)
  public String token;

  private Date createdAt = new Date();

  public JWTTokenDTO() {
  }

  public JWTTokenDTO(@NotNull @NotEmpty @Size(max = 200) String token) {
    this.token = token;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public String toString() {
      return "JWTTokenDTO [token=" + token + ", createdAt=" + createdAt + "]";
  }
}
