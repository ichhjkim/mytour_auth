package com.mytour.auth.payload.request;

import com.mytour.auth.domain.EAgreement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class SignupRequest {

    private String username;
    private String email;
    private String password;
    private List<String> role;

    private List<EAgreement> agreements;
}
