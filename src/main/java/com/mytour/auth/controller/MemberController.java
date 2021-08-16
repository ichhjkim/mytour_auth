package com.mytour.auth.controller;

import com.mytour.auth.config.security.jwt.JwtUtils;
import com.mytour.auth.payload.RESULT_CODE;
import com.mytour.auth.payload.request.MemberUpdateRequest;
import com.mytour.auth.payload.response.Result;
import com.mytour.auth.service.MemberService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/api/member")
public class MemberController {

    private final MemberService memberService;
    private final JwtUtils jwtUtils;

    @Autowired
    public MemberController(MemberService memberService,
                            JwtUtils jwtUtils) {
        this.memberService = memberService;
        this.jwtUtils = jwtUtils;
    }

    private Logger logger = LoggerFactory.getLogger(MemberService.class);


    @PutMapping(value = "/edit")
    public ResponseEntity<?> updateMember(HttpServletRequest request, @RequestBody MemberUpdateRequest memberUpdateRequest) {
        Result result = new Result();
        String headerAuth = request.getHeader("Authorization");
        String jwt = headerAuth.substring(7, headerAuth.length());
        String username = jwtUtils.getUserNameFromJwtToken(jwt);

        if (StringUtils.equals(username, memberUpdateRequest.getUsername())) {
            result = memberService.updateMember(memberUpdateRequest);
        } else {
            logger.info("############ UNAUTHORIZED ACCESS USERNAME: {}, ATTACK_USERNAME: {}", memberUpdateRequest.getUsername(), username);
            result.setResult_code(RESULT_CODE.FAIL);
            result.setMsg("허용되지 않은 접근입니다.");
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/me")
    public ResponseEntity<?> getMyInfo(HttpServletRequest request) {

        String headerAuth = request.getHeader("Authorization");
        String jwt = headerAuth.substring(7, headerAuth.length());
        String username = jwtUtils.getUserNameFromJwtToken(jwt);

        Result result = memberService.getMyInfo(username);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/info")
    public ResponseEntity<?> getMemberinfo(HttpServletRequest request, @RequestParam String username) {
        Result result = new Result();

        String headerAuth = request.getHeader("Authorization");
        String jwt = headerAuth.substring(7, headerAuth.length());
        String jwtUsername = jwtUtils.getUserNameFromJwtToken(jwt);

        if (StringUtils.equals(jwtUsername, username)) {
            result = memberService.getMyInfo(username);
        } else {
            result = memberService.getMemberInfo(username);
        }
        return ResponseEntity.ok(result);
    }


}
