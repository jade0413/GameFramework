package com.yp.gameframwrok.web.model.vo;

import lombok.Data;

/**
 * @author yyp
 */
@Data
public class LoginResultVO {

     private Integer userId;


     private String nickname;


     private Integer state;

     private Integer gold;

     private String token;
}
