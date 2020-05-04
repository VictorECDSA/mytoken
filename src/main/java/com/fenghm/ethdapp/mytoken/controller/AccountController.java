package com.fenghm.ethdapp.mytoken.controller;

import java.util.List;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.fenghm.ethdapp.mytoken.service.AccountService;
import com.fenghm.ethdapp.mytoken.service.AccountService4BC;

@Controller
@RequestMapping("/account")
@Validated
public class AccountController {

	// 使用传统中心化数据库的Service
	@Autowired
	AccountService accountService;

	// 使用基于区块链的Service
	@Autowired
	AccountService4BC accountService4BC;

	@RequestMapping("/list")
	public ModelAndView list() {
		System.out.println("call on account list");
		List<String> accountIds = accountService4BC.list();
		ModelAndView mode = new ModelAndView("account/list");
		mode.addObject("accountIds", accountIds);
		return mode;
	}

	// 注册账户
	@RequestMapping("/register")
	public ModelAndView register(@NotBlank String password) {
		System.out.println("call on account register");
		String accountId = accountService4BC.register(password);
		ModelAndView mode = new ModelAndView("account/register");
		mode.addObject("accountId", accountId);
		return mode;
	}

}
