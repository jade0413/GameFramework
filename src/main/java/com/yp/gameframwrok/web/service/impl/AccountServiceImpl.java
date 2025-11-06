package com.yp.gameframwrok.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yp.gameframwrok.engine.core.UserCacheManger;
import com.yp.gameframwrok.exception.ServiceException;
import com.yp.gameframwrok.model.cache.UserCache;
import com.yp.gameframwrok.web.entity.Account;
import com.yp.gameframwrok.web.mapper.AccountMapper;
import com.yp.gameframwrok.web.model.dto.LoginDTO;
import com.yp.gameframwrok.web.model.dto.RegisterDTO;
import com.yp.gameframwrok.web.model.vo.LoginResultVO;
import com.yp.gameframwrok.web.service.AccountService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author yyp
 */
@Log4j2
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Autowired
    UserCacheManger userCacheManger;

    @Override
    public LoginResultVO login(LoginDTO loginDTO)  {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        Account account = this.lambdaQuery().eq(Account::getUsername, username).eq(Account::getPassword, password).one();
        if (account == null) {
            throw new ServiceException("账号或密码错误");
        }
        String token = getToken(account.getUserId());
        UserCache userCache = new UserCache();
        userCache.setUserId(account.getUserId());
        userCache.setGold(account.getGold());
        userCache.setNickname(account.getNickname());
        userCache.setState(account.getState());
        userCache.setToken(token);
        // 这里如果用redis缓存的换 可以件Web端和游戏端分开部署 游戏端从redis缓存中获取用户信息
        userCacheManger.putUser(account.getUserId(), userCache);
        LoginResultVO loginResultVO = new LoginResultVO();
        loginResultVO.setUserId(account.getUserId());
        loginResultVO.setGold(account.getGold());
        loginResultVO.setNickname(account.getNickname());
        loginResultVO.setState(account.getState());
        loginResultVO.setToken(token);
        return loginResultVO;
    }

    @Override
    public void register(RegisterDTO registerDTO) {
        if (this.lambdaQuery().eq(Account::getUsername, registerDTO.getUsername()).one() != null) {
            throw new RuntimeException("账号已存在");
        }
        Account account = new Account();
        account.setUsername(registerDTO.getUsername());
        account.setPassword(registerDTO.getPassword());
        account.setNickname(registerDTO.getNickname());
        this.save(account);
    }

    public String getToken(Integer userId) {
        // 可以使用jwt生成token 或者自定义
        return userId + "-" + "test_token";
    }

}
