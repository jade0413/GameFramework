package com.yp.gameframwrok.web.contorller;
import com.yp.gameframwrok.web.model.Result;
import com.yp.gameframwrok.web.model.dto.LoginDTO;
import com.yp.gameframwrok.web.model.dto.RegisterDTO;
import com.yp.gameframwrok.web.model.vo.LoginResultVO;
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
     @PostMapping("/login")
     public Result<LoginResultVO> login(@RequestBody LoginDTO loginDTO) {
         return Result.success(userService.login(loginDTO));
     }

     @PostMapping("/register")
     public Result<Void> register(@RequestBody RegisterDTO registerDTO) {
         userService.register(registerDTO);
         return Result.success();
     }
}
