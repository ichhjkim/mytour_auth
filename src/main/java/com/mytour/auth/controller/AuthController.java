package com.mytour.auth.controller;

import com.mytour.auth.config.security.service.RefreshTokenService;
import com.mytour.auth.domain.RefreshToken;
import com.mytour.auth.payload.RESULT_CODE;
import com.mytour.auth.payload.request.LogOutRequest;
import com.mytour.auth.payload.request.LoginRequest;
import com.mytour.auth.payload.request.SignupRequest;
import com.mytour.auth.payload.request.TokenRefreshRequest;
import com.mytour.auth.payload.response.JwtResponse;
import com.mytour.auth.payload.response.MessageResponse;
import com.mytour.auth.payload.response.Result;
import com.mytour.auth.payload.response.TokenRefreshResponse;
import com.mytour.auth.service.AuthService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    private Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    AuthController(AuthService authService,
                   RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {

        Result result = authService.signup(signUpRequest);

        logger.info("###### SIGNUP - NEW_MEMBER: {}", signUpRequest.getUsername());
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/login")
    public ResponseEntity<?> loginMember(@RequestBody LoginRequest request) {

        Result result = authService.signin(request);
        if (RESULT_CODE.SUCCESS.equals(result.getResult_code())) {
            refreshTokenService.deleteByUserId(request.getUsername());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(request.getUsername());
            JwtResponse jwtResponse = (JwtResponse) result.getData();
            jwtResponse.setRefreshToken(refreshToken.getToken());

            result.setData(jwtResponse);
            return ResponseEntity.ok(result);
        }

        logger.info("###### LOGIN - MEMBER: {}", request.getUsername());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        Result result = getRefreshToken(requestRefreshToken);

        if (RESULT_CODE.SUCCESS.equals(result.getResult_code())) {
            TokenRefreshResponse response = new TokenRefreshResponse(result.getData().toString(), requestRefreshToken);
            result.setData(response);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestBody LogOutRequest logOutRequest) {
        refreshTokenService.deleteByUserId(logOutRequest.getUsername());
        return ResponseEntity.ok(new MessageResponse("Log out successful!"));
    }

    public Result getRefreshToken(String requestRefreshToken) {
        Result result = new Result();
        result.setResult_code(RESULT_CODE.FAIL);

        try {
            RefreshToken refresh = refreshTokenService.findByToken(requestRefreshToken).get();

            if (refresh==null || refresh.getToken()==null) {
                logger.info("###### REFRESH TOKEN IS NULL");
                return result;
            }

            RefreshToken refreshToken = refreshTokenService.verifyExpiration(refresh);
            String username = refreshToken.getMemberDTO().getUsername();
            String token = authService.generateToken(username);

            result.setResult_code(RESULT_CODE.SUCCESS);
            result.setData(token);
        } catch (Exception e) {
            logger.error("####### JWT TOKEN REFRESH EXCEPTION: {}", e.toString());
            result.setResult_code(RESULT_CODE.ERROR);
        }
        return result;
    }

}
