package com.yp.gameframwrok.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author yyp
 */
@Data
@TableName("user")
public class Account {
     @TableId(type = IdType.AUTO)
     private Integer userId;

     private String password;

     private String username;

     private Integer gold;

     private Date createTime;

     private String nickname;


     private Integer state;

}
