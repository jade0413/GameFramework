package com.yp.gameframwrok.model.cache;

import lombok.Data;

/**
 * @author yyp
 */
@Data
public class UserCache {

     private int userId;

     private String nickname;

     private String loginId;

     private int gold;

     private int state;

     protected String merchant;

     protected String agent;

     private String token;

     protected int playerType = PlayerType.VISITOR;
}
