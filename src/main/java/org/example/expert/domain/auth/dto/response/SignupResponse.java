package org.example.expert.domain.auth.dto.response;

import lombok.Getter;
import org.example.expert.config.jwt.dto.JwtToken;

@Getter
public class SignupResponse {

    private final JwtToken jwtToken;

    public SignupResponse(JwtToken jwtToken) {
        this.jwtToken = jwtToken;
    }
}
