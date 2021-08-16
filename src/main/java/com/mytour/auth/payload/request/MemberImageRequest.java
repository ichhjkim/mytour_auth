package com.mytour.auth.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberImageRequest {
    private String username;
    private String imagePath;
}
