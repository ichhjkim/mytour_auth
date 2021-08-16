package com.mytour.auth.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberUpdateRequest {
    private String username;
    private String email;
    private String phone;
    private String name;
    private int age;
    private String gender;
    private String imagePath;
}
