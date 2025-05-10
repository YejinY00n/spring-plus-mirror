package org.example.expert.config.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtToken {
  private String grantType;
  private String accessToken;
  private String refreshToken;
}
