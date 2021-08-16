package com.mytour.auth.service;

import com.mytour.auth.config.security.jwt.JwtUtils;
import com.mytour.auth.config.security.service.RefreshTokenService;
import com.mytour.auth.config.security.service.UserDetailsImpl;
import com.mytour.auth.domain.*;
import com.mytour.auth.payload.RESULT_CODE;
import com.mytour.auth.payload.request.LoginRequest;
import com.mytour.auth.payload.request.SignupRequest;
import com.mytour.auth.payload.response.JwtResponse;
import com.mytour.auth.payload.response.Result;
import com.mytour.auth.payload.response.TokenRefreshResponse;
import com.mytour.auth.repository.MemberAgreeRepo;
import com.mytour.auth.repository.MemberRepo;
import com.mytour.auth.repository.RoleRepo;
import com.mytour.auth.util.ValidateMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final MemberRepo memberRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final MemberAgreeRepo memberAgreeRepo;

    @Autowired
    AuthService(MemberRepo memberRepo,
                RoleRepo roleRepo,
                PasswordEncoder encoder,
                AuthenticationManager authenticationManager,
                JwtUtils jwtUtils,
                MemberAgreeRepo memberAgreeRepo) {
        this.memberRepo = memberRepo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.memberAgreeRepo = memberAgreeRepo;
    }

    public Result signup(SignupRequest request) {
        Result result = new Result();
        result.setResult_code(RESULT_CODE.FAIL);

        try {
            if (!ValidateMember.validateMember(request)) {
                result.setMsg("입력 형식을 확인해주세요.");
                System.out.println("입력 형식 이상");
                return result;
            }

            if (existMember(request) || existEmail(request)) { 
                System.out.println("존재하는 유저");
                return result;}
            
            MemberDTO member = new MemberDTO();
            member.setUsername(request.getUsername());
            member.setEmail(request.getEmail());
            member.setPassword(encoder.encode(request.getPassword()));
            member.setRoles(getMemberRoles(request.getRole()));

            memberRepo.save(member);

            for(EAgreement agree : request.getAgreements()) {
                System.out.println(agree.toString());
                memberAgreeRepo.save(new MemberAgreeDTO(request.getUsername(), agree));
            }

        } catch (Exception e) {
            result.setResult_code(RESULT_CODE.ERROR);
        }

        return result;
    }

    public Result signin(LoginRequest request) {
        Result result = new Result();

        try {
            Optional<MemberDTO> findMember = memberRepo.findByUsername(request.getUsername());

            findMember.ifPresent(member -> {
                String inputPassword = encoder.encode(request.getPassword());

                if (!member.getPassword().equals(inputPassword)) {
                    Authentication authentication = authenticationManager.authenticate(
                                                        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

                    String jwt = jwtUtils.generateJwtToken(userDetails);

                    List<String> roles = userDetails.getAuthorities()
                                                    .stream()
                                                    .map(item -> item.getAuthority())
                                                    .collect(Collectors.toList());

                    JwtResponse jwtResponse = new JwtResponse();
                    jwtResponse.setUsername(request.getUsername());
                    jwtResponse.setToken(jwt);
                    jwtResponse.setEmail(member.getEmail());
                    jwtResponse.setRoles(roles);

                    result.setData(jwtResponse);
                    result.setResult_code(RESULT_CODE.SUCCESS);
                }

            });
        } catch (Exception e) {

        }
        return result;
    }

    public String generateToken(String username) {
        return jwtUtils.generateTokenFromUsername(username);
    }

    public Set<Role> getMemberRoles(List<String> strRoles) {
        Set<Role> roles = new HashSet<>();

        try {
            if (strRoles==null) {
                Role userRole = roleRepo.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);
            } else {
                strRoles.forEach(role -> {
                    switch (role) {
                        case "ADMIN":
                            Role adminRole = roleRepo.findByName(ERole.ROLE_ADMIN)
                                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                            roles.add(adminRole);
                            break;
                        default:
                            Role userRole = roleRepo.findByName(ERole.ROLE_USER)
                                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                            roles.add(userRole);
                    }
                });
            }
        } catch (Exception e) {

        }
        System.out.println(roles.toString());
        return roles;

    }

    public boolean existMember(SignupRequest member) {
        return memberRepo.existsByUsername(member.getUsername());
    }

    public boolean existEmail(SignupRequest member) {
        return memberRepo.existsByEmail(member.getEmail());
    }
}
