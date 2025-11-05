package com.yp.gameframwrok.web.model.dto;

import lombok.Data;

/**
 * @author yyp
 */
@Data
public class LoginDTO {

     private String username;

     private String password;
    /**
     * 验证码
     */
    private String code;
}
