package org.example.expert.domain.auth.dto.response;

import lombok.Getter;
import org.example.expert.config.jwt.dto.JwtToken;

@Getter
public class SigninResponse {

    private final JwtToken jwtToken;

    public SigninResponse(JwtToken jwtToken) {
        this.jwtToken = jwtToken;
    }
}
