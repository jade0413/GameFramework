package com.yp.gameframwrok.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yp.gameframwrok.web.entity.Account;
import com.yp.gameframwrok.web.mapper.AccountMapper;
import com.yp.gameframwrok.web.service.AccountService;
import org.springframework.stereotype.Service;

/**
 * @author yyp
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {


}
