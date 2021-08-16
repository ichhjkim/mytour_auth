package com.mytour.auth.payload.response;

import com.mytour.auth.payload.RESULT_CODE;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Result {
    private RESULT_CODE result_code;
    private String msg;
    private Object data;
}
