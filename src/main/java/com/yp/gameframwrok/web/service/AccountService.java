package com.yp.gameframwrok.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yp.gameframwrok.exception.ServiceException;
import com.yp.gameframwrok.web.entity.Account;
import com.yp.gameframwrok.web.model.dto.LoginDTO;
import com.yp.gameframwrok.web.model.dto.RegisterDTO;
import com.yp.gameframwrok.web.model.vo.LoginResultVO;

/**
 * @author yyp
 */
public interface AccountService extends IService<Account> {

    LoginResultVO login(LoginDTO loginDTO) throws ServiceException;

    void register(RegisterDTO registerDTO);
}
