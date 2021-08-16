package com.mytour.auth.service;

import com.mytour.auth.config.security.jwt.JwtUtils;
import com.mytour.auth.config.security.service.UserDetailsImpl;
import com.mytour.auth.domain.*;
import com.mytour.auth.payload.RESULT_CODE;
import com.mytour.auth.payload.request.LoginRequest;
import com.mytour.auth.payload.request.SignupRequest;
import com.mytour.auth.payload.response.JwtResponse;
import com.mytour.auth.payload.response.Result;
import com.mytour.auth.repository.MemberAgreeRepo;
import com.mytour.auth.repository.MemberRepo;
import com.mytour.auth.repository.RoleRepo;
import com.mytour.auth.util.ValidateMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private Logger logger = LoggerFactory.getLogger(AuthService.class);

    public Result signup(SignupRequest request) {
        Result result = new Result();
        result.setResult_code(RESULT_CODE.FAIL);

        try {
            if (!ValidateMember.validateMember(request)) {
                result.setMsg("입력 형식을 확인해주세요.");
                logger.info("####### SIGNUP username: {}, email: {}, password: {}", request.getUsername(), request.getEmail(), request.getPassword());
                return result;
            }

            if (existMember(request) || existEmail(request)) {
                logger.info("####### SIGNUP username: {}", request.getUsername());
                return result;}
            
            MemberDTO member = new MemberDTO();
            member.setUsername(request.getUsername());
            member.setEmail(request.getEmail());
            member.setPassword(encoder.encode(request.getPassword()));
            member.setRoles(getMemberRoles(request.getRole()));

            logger.info("##### SIGNUP MEMBER: {}", member.toString());

            memberRepo.save(member);

            for(EAgreement agree : request.getAgreements()) {
                logger.info("##### SIGNUP MEMBER: {}", agree.toString());
                memberAgreeRepo.save(new MemberAgreeDTO(request.getUsername(), agree));
            }

        } catch (Exception e) {
            logger.error("##### SIGNUP EXCEPTION: {}", e.toString());
            result.setResult_code(RESULT_CODE.ERROR);
        }

        return result;
    }

    public Result signin(LoginRequest request) {
        Result result = new Result();
        result.setResult_code(RESULT_CODE.FAIL);

        try {
            Optional<MemberDTO> findMember = memberRepo.findByUsername(request.getUsername());

            findMember.ifPresent(member -> {

                if (encoder.matches(request.getPassword(), member.getPassword())) {
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
                else {
                    logger.info("###### LOGIN PASSWORD NOT MATCHED USERNAME: {}, PASSWORD: {}", request.getUsername(), request.getPassword());
                }
            });
        } catch (Exception e) {
            logger.error("##### SIGNIN EXCEPTION: {}", e.toString());
            logger.info("###### LOGIN USERNAME: {}", request.getUsername());
            result.setResult_code(RESULT_CODE.ERROR);
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
            logger.error("##### SIGNIN GENERATE_TOKEN EXCEPTION: {}", e.toString());
        }
        return roles;
    }

    public boolean existMember(SignupRequest member) {
        return memberRepo.existsByUsername(member.getUsername());
    }

    public boolean existEmail(SignupRequest member) {
        return memberRepo.existsByEmail(member.getEmail());
    }
}
