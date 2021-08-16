package com.mytour.auth.service;

import com.mytour.auth.controller.MemberController;
import com.mytour.auth.payload.RESULT_CODE;
import com.mytour.auth.payload.request.MemberUpdateRequest;
import com.mytour.auth.payload.request.SignupRequest;
import com.mytour.auth.payload.response.Result;
import com.mytour.auth.repository.MemberRepo;
import com.mytour.auth.util.PrivacyInfoUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MemberService {

    private final MemberRepo memberRepo;
    private Logger logger = LoggerFactory.getLogger(MemberController.class);

    @Autowired
    public MemberService(MemberRepo memberRepo) {
        this.memberRepo = memberRepo;
    }

    public Result updateMember(MemberUpdateRequest memberUpdateRequest) {
        Result result = new Result();
        result.setResult_code(RESULT_CODE.FAIL);

        try {
            memberRepo.findByUsername(memberUpdateRequest.getUsername()).ifPresent(member -> {

                if (memberUpdateRequest.getEmail()!=null && !existEmail(memberUpdateRequest)) {
                        member.setEmail(memberUpdateRequest.getEmail());
                    }
                    if (memberUpdateRequest.getPhone() != null) {
                        member.setPhone(memberUpdateRequest.getPhone());
                    }
                    if ((Integer)memberUpdateRequest.getAge() != null) {
                        member.setAge(memberUpdateRequest.getAge());
                    }
                    if (memberUpdateRequest.getGender() != null) {
                        member.setGender(memberUpdateRequest.getGender());
                    }

                    memberRepo.save(member);
                    result.setResult_code(RESULT_CODE.SUCCESS);
                    result.setData(member);
            });
        } catch (Exception e) {
            logger.error("###### UPDATEMEMBER USERNAME: {}", memberUpdateRequest.getUsername());
            result.setResult_code(RESULT_CODE.ERROR);
        }
        return result;
    }

    public Result getMyInfo(String username) {
        Result result = new Result();
        result.setResult_code(RESULT_CODE.FAIL);

        try {
            memberRepo.findByUsername(username).ifPresent(memberDTO -> {
                memberDTO.setPassword("");

                result.setData(memberDTO);
                result.setResult_code(RESULT_CODE.SUCCESS);
            });
        } catch (Exception e) {
            logger.error("####### getMyInfo EXCEPTION: {}", e);
            result.setResult_code(RESULT_CODE.ERROR);

        }
        return result;
    }

    public Result getMemberInfo(String username) {
        Result result = new Result();
        result.setResult_code(RESULT_CODE.FAIL);

        try {
            memberRepo.findByUsername(username).ifPresent(memberDTO -> {

                memberDTO.setPassword("");
                memberDTO.setPhone("");
                memberDTO.setUsername(PrivacyInfoUtil.maskingName(username));
                memberDTO.setRoles(null);

                result.setResult_code(RESULT_CODE.SUCCESS);
                result.setData(memberDTO);
            });

        } catch (Exception e) {
            logger.error("###### getMemberInfo EXCEPTION: {}", e);
            result.setResult_code(RESULT_CODE.ERROR);
        }

        return result;
    }

    public boolean existEmail(MemberUpdateRequest member) {
        return memberRepo.existsByEmail(member.getEmail());
    }

}
