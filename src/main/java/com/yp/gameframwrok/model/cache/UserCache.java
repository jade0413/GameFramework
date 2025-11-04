package com.yp.gameframwrok.model.cache;

import lombok.Data;

/**
 * @author yyp
 */
@Data
public class UserCache {

     private int userId;

     private String loginId;

     private int gold;

     private int status;

     protected String merchant;

     protected String agent;

     protected int playerType = PlayerType.VISITOR;
}
