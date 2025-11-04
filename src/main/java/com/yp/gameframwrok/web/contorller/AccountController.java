package com.yp.gameframwrok.web.contorller;

import com.yp.gameframwrok.web.entity.Account;
import com.yp.gameframwrok.web.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户模块 登录注册 等操作
 */
@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountService userService;

     @PostMapping("/test")
     public String test() {
         userService.save(new Account());
         return "success";
     }
}
