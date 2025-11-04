package com.yp.gameframwrok.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author yyp
 */
@Data
@TableName("user")
public class Account {
     @TableId(type = IdType.AUTO)
     private Integer userId;
}
